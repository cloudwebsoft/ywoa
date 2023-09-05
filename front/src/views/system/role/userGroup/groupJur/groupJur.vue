<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    @ok="handleSubmit"
  >
    <BasicTable @register="registerTable">
      <template #toolbar> </template>
    </BasicTable>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicColumn } from '/@/components/Table';
  import { getGroupPriv, getSetGroupPrivs } from '/@/api/system/system';

  import { BasicTable, useTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'groupJur',
    components: {
      BasicTable,
      BasicDrawer,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const treeData = ref<TreeItem[]>([]);
      let dataRef = reactive({});
      const { createMessage } = useMessage();
      let title = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于：${dataRef.desc} 的权限`;
        setProps({
          searchInfo: {
            groupCode: dataRef.code,
          },
        });
        handleSuccess();
      });

      const getTitle = computed(() => title.value);
      async function handleSubmit() {
        try {
          let checkes = ref([]);
          checkes.value = getSelectRows();
          if (checkes.value.length === 0) {
            createMessage.warning('选择数据不能为空');
            return;
          }
          setDrawerProps({ confirmLoading: true });
          // TODO custom api  privs  priv[]
          let params = {
            privs: checkes.value.map((item) => item.code).join(','),
            groupCode: dataRef.code,
          };
          await getSetGroupPrivs(params);
          closeDrawer();
          // dataRef = {};
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }
      const columns: BasicColumn[] = [
        {
          title: '权限',
          dataIndex: 'name',
          width: 100,
          ellipsis: true,
        },
        {
          title: '菜单项',
          dataIndex: 'fullDeptName',
          width: 150,
          ellipsis: true,
        },
      ];
      const [registerTable, { reload, getSelectRows, setProps, setSelectedRowKeys }] = useTable({
        title: '权限列表',
        api: getGroupPriv,
        columns,
        formConfig: {
          labelWidth: 120,
        },
        rowKey: 'code',
        rowSelection: {
          type: 'checkbox',
        },
        searchInfo: {}, //额外的参数
        resizeHeightOffset: 70,
        immediate: false,
        pagination: false,
        useSearchForm: false,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
      });

      async function getGroupPrivList() {
        await getGroupPriv({ groupCode: dataRef.code }).then((res) => {
          let keys = ref([]);
          if (res.list && res.list.length > 0) {
            res.list.forEach((item) => {
              if (item.authorized) {
                keys.value.push(item.code);
              }
            });
            setSelectedRowKeys(unref(keys));
          }
        });
      }

      function handleSuccess() {
        reload();
        getGroupPrivList();
      }

      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        treeData,
        dataRef,
        registerTable,
        handleSuccess,
      };
    },
  });
</script>
