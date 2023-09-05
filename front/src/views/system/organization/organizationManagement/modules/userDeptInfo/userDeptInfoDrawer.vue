<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="500px"
    @ok="handleSubmit"
  >
    <BasicTree
      :treeData="treeData"
      checkable
      toolbar
      search
      :checkStrictly="true"
      title="部门"
      :checkedKeys="checkedKeys"
      @check="handleCheck"
      ref="treeRef"
      treeKey="code"
    />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { getDepartment } from '/@/api/system/system';

  import { useMessage } from '/@/hooks/web/useMessage';
  export default defineComponent({
    name: 'userDeptInfoDrawer',
    components: { BasicDrawer, BasicTree },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const isUpdate = ref(true);
      const treeData = ref<TreeItem[]>([]);
      const treeRef = ref(null);
      let checkedKeys = ref([]);
      let checkedRows = ref([]);
      let dataRef = reactive({});

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        if (unref(treeData).length === 0) {
          treeData.value = ((await getDepartment()) || []) as any as TreeItem[];
          treeData.value = setChildren(treeData.value);
        }
        isUpdate.value = !!data?.isUpdate;
        checkedKeys.value = [];
        dataRef = data.record;
      });

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
      const getTitle = '选择部门'; //computed(() => (!unref(isUpdate) ? '新增角色' : '编辑角色'));
      async function handleSubmit() {
        try {
          if (checkedKeys.value.length === 0) {
            createMessage.warning('选择数据不能为空');
            return;
          }
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          let params = {
            deptCodes: checkedKeys.value.join(','),
          };
          closeDrawer();
          emit('success', params.deptCodes, checkedRows.value);
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }
      function handleCheck(obj, node) {
        checkedKeys.value = obj.checked;
        checkedRows.value = node.checkedNodes;
      }

      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        treeData,
        dataRef,
        handleCheck,
        treeRef,
        checkedKeys,
        checkedRows,
      };
    },
  });
</script>
