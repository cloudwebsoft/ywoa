<template>
  <div class="m-2 mr-0 overflow-hidden bg-white">
    <Spin :spinning="spinning">
      <div class="p-2 flex justify-start">
        <a-button type="primary" size="small" class="mr-2" @click="handleCreate"> 新增 </a-button>
        <a-button type="primary" size="small" class="mr-2" @click="handleUpdate"> 编辑 </a-button>
        <Popconfirm
          placement="bottom"
          title="确定删除吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="handleDelete"
        >
          <a-button type="primary" size="small"> 删除 </a-button>
        </Popconfirm>
      </div>
      <div class="treeList">
        <BasicTree
          title="部门列表"
          toolbar
          search
          draggable
          :clickRowToExpand="false"
          :treeData="treeData"
          @select="handleSelect"
          treeKey="code"
          :expandedKeys="expandedKeys"
          :selectedKeys="selectedKeys"
          @drop="handleDrop"
        >
          <template #title="item">
            <Dropdown :trigger="['contextmenu']">
              <span
                v-if="item.deptType == 0"
                :class="['font-bold', item.isHide == 1 ? 'text-gray-400' : '']"
                >{{ item.name }}</span
              >
              <span v-else>
                <span :class="item.isHide == 1 ? 'text-gray-400' : ''">
                  {{ item.name }}
                </span>
              </span>
              <template #overlay>
                <Menu @click="({ key: menuKey }) => onContextMenuClick(item.code, menuKey)">
                  <MenuItem key="1"
                    ><plus-outlined style="color: blue" class="mr-2" />添加</MenuItem
                  >
                  <MenuItem key="2"
                    ><edit-outlined style="color: blue" class="mr-2" />修改</MenuItem
                  >
                  <Popconfirm
                    placement="top"
                    title="确定要删除吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleDelete"
                  >
                    <MenuItem key="3">
                      <delete-outlined style="color: red" class="mr-2" />删除
                    </MenuItem>
                  </Popconfirm>
                </Menu>
              </template>
            </Dropdown>
          </template>
        </BasicTree>
      </div>
    </Spin>
    <DeptTreeListModel @register="registerModal" @success="fetch" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, reactive, unref } from 'vue';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { useModal } from '/@/components/Modal';
  import { Popconfirm, Spin, Dropdown, Menu } from 'ant-design-vue';
  import {
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    CloseOutlined,
    CheckOutlined,
    ProfileOutlined,
  } from '@ant-design/icons-vue';
  import { getDepartment, getDepartmentDel, getDepartmentMove } from '/@/api/system/system';
  import DeptTreeListModel from './deptTreeListModel.vue';
  export default defineComponent({
    name: 'DeptTreeList',
    components: {
      BasicTree,
      DeptTreeListModel,
      Popconfirm,
      Spin,
      Dropdown,
      Menu,
      MenuItem: Menu.Item,
      PlusOutlined,
      EditOutlined,
      DeleteOutlined,
    },

    emits: ['select', 'drop', 'success'],
    setup(_, { emit }) {
      const treeData = ref<TreeItem[]>([]);
      let expandedKeys = ref(['root']);
      let selectedKeys = ref(['root']);
      let dataRef = reactive({});
      const spinning = ref(false);
      async function fetch() {
        treeData.value = [];
        const data = await getDepartment();
        treeData.value = setChildren(data);
        selectedKeys.value = ['root'];
        dataRef = unref(treeData)[0];
      }
      function setChildren(node) {
        node.forEach((item) => {
          item.key = item.code;
          item.title = item.name;
          if (item.children && Array.isArray(item.children) && item.children.length > 0) {
            item.isLeaf = false;
            setChildren(item.children);
          } else {
            item.isLeaf = true;
          }
        });
        return node;
      }

      function handleSelect(keys, trc) {
        selectedKeys.value = keys;
        if (keys.length === 1) {
          dataRef = trc.node.dataRef;
          // delete dataRef['title'];
        } else {
          dataRef = {};
        }
        emit('select', keys[0], dataRef);
      }

      const [registerModal, { openModal }] = useModal();
      function handleCreate() {
        openModal(true, {
          isUpdate: false,
          record: dataRef,
        });
        // emit('handleCreate');
      }
      function handleUpdate() {
        if (!dataRef['code']) {
          return;
        }
        openModal(true, {
          isUpdate: true,
          record: dataRef,
        });
      }
      async function handleDelete() {
        if (unref(selectedKeys).length === 0 || unref(selectedKeys)[0] == 'root') {
          return;
        }
        await getDepartmentDel({ code: unref(selectedKeys)[0] });
        fetch();
      }
      //移动
      function handleDrop({ dropPosition }, current, currentParent, data) {
        let params = {
          code: current.code,
          parentCode: currentParent.code,
          position: dropPosition,
        };
        spinning.value = true;
        getDepartmentMove(params).then(() => {
          treeData.value = [];
          fetch();
          emit('success', 1);
          spinning.value = false;
        });
        console.log('handleDrop', dropPosition, current, currentParent, data);
        emit('drop', dropPosition, current, currentParent, data);
      }
      onMounted(() => {
        fetch();
      });

      const onContextMenuClick = (treeKey: string, menuKey: string | number) => {
        console.log(`treeKey: ${treeKey}, menuKey: ${menuKey}`);
        switch (menuKey) {
          case '1':
            handleCreate();
            break;
          case '2':
            handleUpdate();
            break;
        }
      };

      return {
        fetch,
        treeData,
        handleSelect,
        expandedKeys,
        handleCreate,
        handleDelete,
        handleUpdate,
        registerModal,
        selectedKeys,
        handleDrop,
        spinning,
        onContextMenuClick,
      };
    },
  });
</script>
<style lang="less" scoped>
  .treeList {
    overflow: auto;
    height: calc(100vh - 140px);
  }
</style>
