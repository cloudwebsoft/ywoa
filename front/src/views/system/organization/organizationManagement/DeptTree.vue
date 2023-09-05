<template>
  <div class="m-4 mr-0 overflow-hidden bg-white">
    <BasicTree
      title="部门"
      toolbar
      search
      draggable
      :clickRowToExpand="false"
      :treeData="treeData"
      :fieldNames="{ key: 'id', title: 'deptName' }"
      @select="handleSelect"
      @drop="handleDrop"
    />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';

  import { BasicTree, TreeItem } from '/@/components/Tree';
  import {} from '/@/api/system/system';
  import { Tooltip } from 'ant-design-vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  export default defineComponent({
    name: 'DeptTree',
    components: { BasicTree, Tooltip, PlusOutlined },

    emits: ['select', 'drop'],
    setup(_, { emit }) {
      const treeData = ref<TreeItem[]>([]);

      async function fetch() {
        treeData.value = [];
      }

      function handleSelect(keys) {
        emit('select', keys[0]);
      }

      function handleDrop(data) {
        console.log('改变后的树', data);
        emit('drop', data);
      }
      onMounted(() => {
        fetch();
      });
      return { treeData, handleSelect, handleDrop };
    },
  });
</script>
