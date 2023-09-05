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
      title="成员"
      :checkedKeys="checkedKeys"
      @check="handleCheck"
      ref="treeRef"
      treeKey="userName"
    />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { getPostUserTransferPage, getPostUserUpdate } from '/@/api/system/system';

  import { useMessage } from '/@/hooks/web/useMessage';
  export default defineComponent({
    name: 'MemberManagementDrawer',
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
        dataRef = data.record;
        let params = {
          id: dataRef.id,
        };
        if (unref(treeData).length === 0) {
          treeData.value = ((await getPostUserTransferPage(params)).deptUserAry ||
            []) as any as TreeItem[];
          treeData.value = setChildren(treeData.value);
        }
        isUpdate.value = !!data?.isUpdate;
        checkedKeys.value = [];
      });

      function setChildren(node) {
        node.forEach((item) => {
          item.key = item.userName;
          item.title = item.realName;
          if (item.children && Array.isArray(item.children) && item.children.length > 0) {
            item.isLeaf = false;
            setChildren(item.children);
          } else {
            item.isLeaf = true;
          }
        });
        return node;
      }
      const getTitle = '选择成员';
      async function handleSubmit() {
        try {
          if (checkedKeys.value.length === 0) {
            createMessage.warning('选择数据不能为空');
            return;
          }
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          let params = {
            postId: dataRef.id,
            userNames: checkedKeys.value.join(','),
          };
          await getPostUserUpdate(params);
          closeDrawer();
          emit('success');
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
