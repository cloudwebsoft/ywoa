<template>
  <div class="ml-2 mt-1 mb-2 mr-0 overflow-hidden bg-white">
    <Spin :spinning="spinning">
      <div class="treeList">
        <BasicTree
          title="类型列表"
          toolbar
          search
          draggable
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          @select="handleSelect"
          @drop="handleDrop"
          treeKey="code"
          :expandedKeys="expandedKeys"
        >
          <template #title="{ code: treeKey, name, show, isOpen, canManage }">
            <Dropdown :trigger="['contextmenu']">
              <template v-if="show">
                <Input
                  size="small"
                  style="width: 160px"
                  v-model:value="currentNode.name"
                  @blur="setMuneEdit"
                />
              </template>
              <template v-else>
                <div class="w-full" :style="isOpen ? '' : 'color: #ddd'">{{ name }}</div>
              </template>
              <template #overlay v-if="canManage">
                <Menu @click="({ key: menuKey }) => onContextMenuClick(treeKey, menuKey)">
                  <MenuItem key="1"
                    ><plus-outlined style="color: blue" class="mr-2" />添加</MenuItem
                  >
                  <MenuItem key="2">
                    <edit-outlined style="color: blue" class="mr-2" />重命名
                  </MenuItem>
                  <Popconfirm
                    placement="top"
                    title="确定要删除吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="delAction(treeKey)"
                  >
                    <MenuItem key="3">
                      <delete-outlined style="color: red" class="mr-2" />删除
                    </MenuItem>
                  </Popconfirm>
                  <MenuItem key="4" v-if="!isOpen">
                    <span> <check-outlined style="color: blue" class="mr-2" />启用 </span>
                  </MenuItem>
                  <MenuItem key="5" v-if="isOpen">
                    <span> <close-outlined style="color: blue" class="mr-2" />停用 </span>
                  </MenuItem>
                  <MenuItem key="6"
                    ><ProfileOutlined style="color: blue" class="mr-2" />
                    权限
                  </MenuItem>
                </Menu>
              </template>
            </Dropdown>
          </template>
        </BasicTree>
      </div>
    </Spin>
    <SmartTreeViewPrivDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, watch, unref } from 'vue';
  import { Spin, Menu, Dropdown, Input, Popconfirm } from 'ant-design-vue';
  import {
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    CloseOutlined,
    CheckOutlined,
    ProfileOutlined,
  } from '@ant-design/icons-vue';

  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { getModuleTree } from '/@/api/module/module';

  import {
    getBasicTree,
    getBasicCreateNode,
    getBasicUpdateNode,
    getBasicDelNode,
    getBasicMoveNode,
    getBasicOpenNode,
    getBasicCloseNode,
  } from '/@/api/system/system';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDrawer } from '/@/components/Drawer';
  import SmartTreeViewPrivDrawer from './smartTreeViewPrivDrawer.vue';
  export default defineComponent({
    name: 'DeptTree',
    components: {
      BasicTree,
      Spin,
      Menu,
      MenuItem: Menu.Item,
      Dropdown,
      PlusOutlined,
      EditOutlined,
      DeleteOutlined,
      CloseOutlined,
      CheckOutlined,
      Input,
      SmartTreeViewPrivDrawer,
      Popconfirm,
      ProfileOutlined,
    },
    props: {
      code: {
        type: String,
        default: '',
      },
      defaultNodeCode: {
        type: String,
        default: '',
      },
    },
    emits: ['select', 'drop', 'ok', 'setModuleCode'],
    setup(props, { emit }) {
      const treeData = ref<TreeItem[]>([]);
      const { createMessage } = useMessage();
      let spinning = ref(false);
      let selectedKeys = ref<String[]>([]);
      let expandedKeys = ref<String[]>([]);
      const code = ref('');

      const [registerDrawer, { openDrawer }] = useDrawer();

      async function fetch() {
        await getModuleTree({ code: unref(code) }).then((res) => {
          if (res.list && res.list.length > 0) {
            treeData.value = setChildren(res.list);
            const moduledCode =
              treeData.value?.length > 0 ? treeData.value[0].linkTo?.moduleCode : '';
            emit('setModuleCode', moduledCode);
          }
        });
      }

      function setChildren(node) {
        node.forEach((item) => {
          item.show = false;
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

      function handleSelect(keys, node) {
        // 防止切换选中状态
        selectedKeys.value = [node?.node?.dataRef?.key];
        emit('select', selectedKeys.value[0], node);
      }

      // onMounted(() => {
      //   fetch();
      // });

      watch(
        () => props.code,
        (newVal) => {
          if (newVal) {
            code.value = newVal;
            expandedKeys.value = [unref(code)];
            selectedKeys.value = [unref(code)];
            fetch();
          }
        },
        { deep: true },
      );

      watch(
        () => props.defaultNodeCode,
        (newVal) => {
          if (newVal) {
            selectedKeys.value = [newVal];
          }
        },
        { deep: true },
      );

      const currentNode = ref<Recordable>({});
      const onContextMenuClick = (treeKey: string, menuKey: string | number) => {
        console.log(`treeKey: ${treeKey}, menuKey: ${menuKey}`);
        switch (menuKey) {
          case '1':
            setMuneAdd(treeKey);
            break;
          case '2':
            searchCurrentNode(treeKey, 1);
            break;
          case '3':
            setMenuDelete(treeKey);
            break;
          case '4':
            searchCurrentNode(treeKey, 2);
            break;
          case '5':
            searchCurrentNode(treeKey, 3);
            break;
          case '6':
            searchCurrentNode(treeKey, 4);
            break;
        }
      };
      //新增
      async function setMuneAdd(code: string): Promise<any> {
        let formData = Object.assign({}, { parent_code: code, name: '新建类型' });
        console.log('formData', formData);
        let result = await getBasicCreateNode(formData);
        createMessage.success('操作成功');
        fetch();
        return result;
      }
      //编辑时查询当前
      function searchCurrentNode(code: string, type: number) {
        getBasicTree({ code: code }).then((res) => {
          currentNode.value = res.list && res.list[0] ? res.list[0] : {};
          if (type === 1) {
            treeData.value = setData(treeData.value);
          } else if (type === 2) {
            getBasicOpenNode({ code }).then(() => {
              fetch();
            });
          } else if (type === 3) {
            getBasicCloseNode({ code }).then(() => {
              fetch();
            });
          } else if (type === 4) {
            //权限
            openDrawer(true, {
              record: currentNode.value,
              code: props.code,
            });
          }
        });
      }
      const setData = function (data) {
        data.forEach((item) => {
          let isgo = true;
          if (item.code == currentNode.value.code) {
            item.show = true;
            isgo = false;
          }
          if (isgo && item.children) {
            setData(item.children);
          }
        });
        return data;
      };
      //编辑
      async function setMuneEdit(): Promise<any> {
        if (!currentNode.value.code) return;
        await getBasicUpdateNode(currentNode.value);
        currentNode.value = {};
        createMessage.success('操作成功');
        fetch();
        return;
      }
      //删除
      async function setMenuDelete(code: string): Promise<void> {
        let result = await getBasicDelNode({ code });
        createMessage.success('操作成功');
        fetch();
        selectedKeys.value = [];
        return result;
      }
      //移动
      function handleDrop({ dropPosition, dragNode, node }, current, currentParent, data) {
        let params = {
          code: current.code,
          parent_code: currentParent.code,
          position: dropPosition,
        };
        spinning.value = true;
        getBasicMoveNode(params).then(() => {
          treeData.value = [];
          fetch();
          spinning.value = false;
        });
        emit('drop', dropPosition, current, currentParent, data);
      }

      async function delAction(code: string) {
        setMenuDelete(code);
      }

      const handleSuccess = () => {};
      return {
        treeData,
        handleSelect,
        fetch,
        selectedKeys,
        spinning,
        expandedKeys,
        onContextMenuClick,
        currentNode,
        setMuneEdit,
        handleDrop,
        registerDrawer,
        handleSuccess,
        delAction,
      };
    },
  });
</script>
<style lang="less" scoped>
  .treeList {
    overflow: auto;
    height: 100%;
  }
</style>
