<template>
  <Spin :spinning="spinning">
    <BasicTree
      title=""
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
      <template #title="{ key, title, code, layer }">
        <Dropdown
          :trigger="['contextmenu']"
          @visible-change="(visible) => visibleChange(visible, { key, title, code, layer })"
        >
          <div class="w-full">{{ title }}</div>
          <template #overlay>
            <Menu
              @click="
                ({ key: menuKey }) => onContextMenuClick(menuKey, { key, title, code, layer })
              "
            >
              <MenuItem key="1"
                ><Icon
                  icon="ant-design:plus-outlined"
                  style="color: blue"
                  class="mr-2"
                />添加</MenuItem
              >
              <MenuItem key="2">
                <Icon icon="ant-design:edit-outlined" style="color: blue" class="mr-2" />修改
              </MenuItem>
              <Popconfirm
                placement="top"
                title="确定要删除吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="delAction(key)"
              >
                <MenuItem key="3">
                  <Icon icon="ant-design:delete-filled" style="color: red" class="mr-2" />删除
                </MenuItem>
              </Popconfirm>
              <MenuItem key="4">
                <Icon icon="ant-design:bug-outlined" class="mr-2" />调试模式
              </MenuItem>
              <MenuItem key="5">
                <Icon icon="ant-design:check-outlined" class="mr-2" />正常模式
              </MenuItem>
            </Menu>
          </template>
        </Dropdown>
      </template>
    </BasicTree>
  </Spin>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import { Spin, Popconfirm, Dropdown, Menu } from 'ant-design-vue';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import componentSetting from '/@/settings/componentSetting';
  import {
    getMenuList,
    setMenuListAdd,
    setMenuListEdit,
    setMenuListDelete,
    setMoveMenu,
  } from '/@/api/system/system';
  import {
    getFlowGetDirTreeAll,
    getAdminFlowMoveNode,
    getAdminFlowDelNode,
  } from '/@/api/flowManage/flowManage';
  import Icon from '/@/components/Icon';
  import { useMessage } from '/@/hooks/web/useMessage';
  export default defineComponent({
    name: 'DeptTree',
    components: { BasicTree, Spin, Popconfirm, Dropdown, Menu, MenuItem: Menu.Item, Icon },

    emits: [
      'select',
      'drop',
      'addAction',
      'delAction',
      'ok',
      'treeContextMenu',
      'editAction',
      'rootRecord',
    ],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const treeData = ref<TreeItem[]>([]);
      let spinning = ref(false);
      let selectedKeys = ref([]);
      let expandedKeys = ref([]);
      const rootRecord = ref({});
      async function fetch() {
        treeData.value = (await getFlowGetDirTreeAll()) || [];
        treeData.value = setChildren(treeData.value);
        rootRecord.value = {};
        if (treeData.value.length) {
          rootRecord.value = treeData.value[0];
          emit('rootRecord', treeData.value[0].code);
          if (selectedKeys.value.length === 0) {
            selectedKeys.value = [rootRecord.value.code];
            emit('treeContextMenu', rootRecord.value);
          }
          if (expandedKeys.value.length === 0) {
            expandedKeys.value = [rootRecord.value.code];
          }
        }
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
        selectedKeys.value = [trc.node.dataRef.code];
        emit('select', keys[0], trc);
      }

      function handleDrop({ dropPosition }, current, currentParent, data) {
        // if (current.parentCode != currentParent.code) {
        //   createMessage.warning('层级不一致，不能拖动！');
        //   fetch();
        //   return;
        // }
        let params = {
          code: current.code,
          parent_code: currentParent.code,
          position: dropPosition,
        };
        spinning.value = true;
        getAdminFlowMoveNode(params).then(() => {
          treeData.value = [];
          fetch();
          emit('ok', 1);
          spinning.value = false;
        });
        emit('drop', dropPosition, current, currentParent, data);
      }

      const addAction = (treeKey, { layer }) => {
        if (layer - 0 >= 3) {
          createMessage.warning('流程类型层级不能超过两层');
          return;
        }
        emit('addAction', treeKey);
      };
      const editAction = (treeKey) => {
        emit('editAction', treeKey);
      };
      const delAction = async (code) => {
        // emit('delAction', treeKey);
        let result = await getAdminFlowDelNode({ code });
        selectedKeys.value = [rootRecord.value.code];
        emit('treeContextMenu', rootRecord.value);
        fetch();
        return result;
      };
      const debugAction = (treeKey) => {
        emit('debugAction', treeKey);
      };
      const normalAction = (treeKey) => {
        emit('normalAction', treeKey);
      };

      const onContextMenuClick = (menuKey: string, record: Record) => {
        switch (menuKey) {
          case '1':
            addAction(record.key, record);
            break;
          case '2':
            editAction(record.key);
            break;
          case '3':
            delAction(record.key);
            break;
          case '4':
            debugAction(record.key);
            break;
          case '5':
            normalAction(record.key);
            break;
        }
      };

      const visibleChange = (visible: boolean, record: Record) => {
        if (visible) {
          selectedKeys.value = [record.code];
          emit('treeContextMenu', record);
        }
      };

      onMounted(() => {
        fetch();
      });
      return {
        treeData,
        handleSelect,
        handleDrop,
        fetch,
        addAction,
        delAction,
        selectedKeys,
        spinning,
        expandedKeys,
        onContextMenuClick,
        visibleChange,
      };
    },
  });
</script>
