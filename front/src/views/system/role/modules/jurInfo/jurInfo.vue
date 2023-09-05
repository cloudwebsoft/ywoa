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
  import { getRolePriv, getSetRolePrivs } from '/@/api/system/system';

  import { BasicTable, useTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'jurInfo',
    components: {
      BasicTable,
      BasicDrawer,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const treeData = ref<TreeItem[]>([]);
      let dataRef = reactive<Recordable>({});
      let title = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于角色：${dataRef.description} 的权限`;
        setProps({
          searchInfo: {
            roleCode: dataRef.code,
          },
        });
        handleSuccess();
        // 需要在setFieldsValue之前先填充treeData，否则Tree组件可能会报key not exist警告
      });

      const { createMessage } = useMessage();
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
            roleCode: dataRef.code,
          };
          await getSetRolePrivs(params);
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
        api: getRolePriv,
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

      async function getRolePrivList() {
        await getRolePriv({ roleCode: dataRef.code }).then((res) => {
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
        getRolePrivList();
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
