<template>
  <div class="mt-3 ml-3 mb-4 mr-0 overflow-hidden bg-white">
    <Spin :spinning="spinning">
      <div class="treeList">
        <BasicTree
          title="类型列表"
          toolbar
          search
          draggable
          :showIcon="false"
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          @select="handleSelect"
          treeKey="code"
          :expandedKeys="expandedKeys"
        />
      </div>
    </Spin>
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import { Spin } from 'ant-design-vue';
  import { Button } from '/@/components/Button';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { getDirTreeForQuery } from '/@/api/process/process';
  export default defineComponent({
    name: 'DeptTree',
    components: { BasicTree, Button, Spin },

    emits: ['select', 'drop', 'ok'],
    setup(_, { emit }) {
      const treeData = ref<TreeItem[]>([]);
      let spinning = ref(false);
      let selectedKeys = ref([]);
      let expandedKeys = ref(['root']);
      async function fetch() {
        treeData.value = (await getDirTreeForQuery({ isIcon: false })) || [];
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

      onMounted(() => {
        fetch();
      });

      return {
        treeData,
        handleSelect,
        fetch,
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
