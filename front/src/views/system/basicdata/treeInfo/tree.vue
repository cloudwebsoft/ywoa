<template>
  <div class="m-4 mr-0 overflow-hidden bg-white">
    <Spin :spinning="spinning">
      <div class="p-2">
        <Button type="primary" class="mr-2" @click="addAction"> 新增 </Button>
        <Popconfirm
          placement="top"
          title="确定要删除吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="delAction"
        >
          <Button type="primary"> 删除 </Button>
        </Popconfirm>
      </div>
      <div class="treeList">
        <BasicTree
          title=""
          toolbar
          search
          draggable
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          :expandedKeys="expandedKeys"
          @select="handleSelect"
          @drop="handleDrop"
          treeKey="code"
        />
      </div>
    </Spin>
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, onActivated, watchEffect } from 'vue';
  import { Popconfirm, Spin } from 'ant-design-vue';
  import { Button } from '/@/components/Button';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import componentSetting from '/@/settings/componentSetting';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    getBasicTree,
    getBasicCreateNode,
    getBasicUpdateNode,
    getBasicDelNode,
    getBasicMoveNode,
  } from '/@/api/system/system';
  interface Data {
    [key: string]: unknown;
  }
  export default defineComponent({
    name: 'BasicDataTree',
    components: { BasicTree, Button, Spin, Popconfirm },
    props: {
      code: {
        type: String,
        required: true,
      },
    },
    emits: ['select', 'drop', 'addAction', 'delAction', 'ok'],
    setup(props: Data, { emit }) {
      const { createMessage } = useMessage();

      let expandedKeys = ref<any>([]);
      const treeData = ref<TreeItem[]>([]);
      let spinning = ref(false);
      let selectedKeys = ref([]);
      async function fetch() {
        treeData.value =
          (await getBasicTree({ code: props['code'] }))[
            componentSetting.table.fetchSetting.listField
          ] || [];
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

      watchEffect(() => {
        !!props.code && expandedKeys.value.push(props.code) && fetch();
      });

      function handleSelect(keys, node) {
        emit('select', keys[0], node);
      }

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
          emit('ok', 1);
          spinning.value = false;
        });
        emit('drop', dropPosition, current, currentParent, data);
      }

      async function setMuneAdd(values): Promise<any> {
        let formData = Object.assign({}, { parent_code: 'root' }, values);
        let result = await getBasicCreateNode(formData);
        emit('ok', 1);
        return result;
      }
      async function setMuneEdit(values): Promise<any> {
        return await getBasicUpdateNode(values);
      }
      async function setMenuDelete(code): Promise<void> {
        let result = await getBasicDelNode({ code });
        createMessage.success('操作成功');
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
        // fetch();
      });
      onActivated(() => {
        fetch();
      });
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
      };
    },
  });
</script>
<style lang="less" scoped>
  .treeList {
    overflow: auto;
  }
</style>
