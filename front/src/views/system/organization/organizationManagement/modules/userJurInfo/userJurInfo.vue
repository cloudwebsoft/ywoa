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
  import { getUserPriv, getSetUserPrivs } from '/@/api/system/system';

  import { BasicTable, useTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'userJurInfo',
    components: {
      BasicTable,
      BasicDrawer,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const treeData = ref<TreeItem[]>([]);
      let dataRef = reactive({});
      let title = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于用户：${dataRef.realName} 的权限`;
        setProps({
          searchInfo: {
            userName: dataRef.user.name,
          },
        });
        handleSuccess();
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
            userName: dataRef.user.name,
          };
          await getSetUserPrivs(params);
          closeDrawer();
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
        // {
        //   title: '菜单项',
        //   dataIndex: 'fullDeptName',
        //   width: 150,
        //   ellipsis: true,
        // },
        {
          title: '有权限的角色',
          dataIndex: 'fullDeptName',
          width: 150,
          ellipsis: true,
          customRender: ({ record }) => {
            return record.groupList && record.roleList.length > 0
              ? record.roleList.map((item) => item.name).join(',')
              : '';
          },
        },
        {
          title: '有权限的用户组',
          dataIndex: 'fullDeptName',
          width: 150,
          ellipsis: true,
          customRender: ({ record }) => {
            return record.groupList && record.groupList.length > 0
              ? record.groupList.map((item) => item.description).join(',')
              : '';
          },
        },
      ];
      const [registerTable, { reload, getSelectRows, setProps, setSelectedRowKeys }] = useTable({
        title: '权限列表',
        api: getUserPriv,
        columns,
        formConfig: {
          labelWidth: 120,
        },
        rowKey: 'code',
        rowSelection: {
          type: 'checkbox',
          getCheckboxProps(record: any) {
            // layer: 第一行（layer为0）的选择框禁用
            if (record.layer == '1') {
              return { disabled: true };
            } else {
              return { disabled: false };
            }
          },
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

      async function getUserPrivList() {
        await getUserPriv({ userName: dataRef.user.name }).then((res) => {
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
        getUserPrivList();
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
