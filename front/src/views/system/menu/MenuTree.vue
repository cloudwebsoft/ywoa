<template>
  <div class="m-4 mr-0 overflow-hidden bg-white">
    <Spin :spinning="spinning">
      <div class="p-2">
        <Button type="primary" class="mr-2" @click="addAction"> 新增 </Button>
        <Popconfirm title="确定删除么？" ok-text="确定" cancel-text="取消" @confirm="delAction">
          <Button type="primary"> 删除 </Button>
        </Popconfirm>
      </div>
      <div class="treeList">
        <BasicTree
          title="菜单列表"
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
          <template #title="item">
            <Dropdown :trigger="['contextmenu']">
              <span>{{ item.name }}</span>
              <template #overlay>
                <Menu @click="({ key: menuKey }) => onContextMenuClick(item.code, menuKey)">
                  <MenuItem key="1"
                    ><plus-outlined style="color: blue" class="mr-2" />添加</MenuItem
                  >
                  <Popconfirm
                    placement="top"
                    title="确定要删除吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="delAction"
                  >
                    <MenuItem key="3">
                      <delete-outlined style="color: red" class="mr-2" />删除
                    </MenuItem>
                  </Popconfirm>
                </Menu>
              </template>
            </Dropdown></template
          >
        </BasicTree>
      </div>
    </Spin>
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import { Spin, Popconfirm, Menu, Dropdown } from 'ant-design-vue';
  import { Button } from '/@/components/Button';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import componentSetting from '/@/settings/componentSetting';
  import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import {
    getMenuList,
    setMenuListAdd,
    setMenuListEdit,
    setMenuListDelete,
    setMoveMenu,
  } from '/@/api/system/system';
  export default defineComponent({
    name: 'DeptTree',
    components: {
      BasicTree,
      Button,
      Spin,
      Popconfirm,
      PlusOutlined,
      DeleteOutlined,
      Menu,
      MenuItem: Menu.Item,
      Dropdown,
    },

    emits: ['select', 'drop', 'addAction', 'delAction', 'ok'],
    setup(_, { emit }) {
      const treeData = ref<TreeItem[]>([]);
      let spinning = ref(false);
      let selectedKeys = ref([]);
      let expandedKeys = ref(['root']);
      async function fetch() {
        treeData.value = (await getMenuList())[componentSetting.table.fetchSetting.listField] || [];
        treeData.value = setChildren(treeData.value);
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

      function handleSelect(keys, node) {
        emit('select', keys[0], node);
      }

      function handleDrop({ dropPosition }, current, currentParent, data) {
        let params = {
          code: current.code,
          parentCode: currentParent.code,
          position: dropPosition,
        };
        spinning.value = true;
        setMoveMenu(params).then(() => {
          treeData.value = [];
          fetch();
          emit('ok', 1);
          spinning.value = false;
        });
        emit('drop', dropPosition, current, currentParent, data);
      }

      async function setMuneAdd(values): Promise<any> {
        let formData = Object.assign({}, { parentCode: 'root' }, values);
        let result = await setMenuListAdd(formData);
        emit('ok', 1);
        return result;
      }
      async function setMuneEdit(values): Promise<any> {
        await setMenuListEdit(values);
      }
      async function setMenuDelete(code): Promise<void> {
        let result = await setMenuListDelete({ code });
        emit('ok');
        fetch();
        selectedKeys.value = [];
        return result;
      }
      function addAction() {
        emit('addAction');
      }
      function delAction() {
        emit('delAction');
      }
      onMounted(() => {
        fetch();
      });

      const onContextMenuClick = (treeKey: string, menuKey: string | number) => {
        switch (menuKey) {
          case '1':
            addAction();
            break;
        }
      };

      return {
        treeData,
        handleSelect,
        handleDrop,
        setMuneAdd,
        setMuneEdit,
        setMenuDelete,
        fetch,
        addAction,
        delAction,
        selectedKeys,
        spinning,
        expandedKeys,
        onContextMenuClick,
      };
    },
  });
</script>
<style lang="less" scoped>
  .treeList {
    overflow: auto;
  }
</style>
