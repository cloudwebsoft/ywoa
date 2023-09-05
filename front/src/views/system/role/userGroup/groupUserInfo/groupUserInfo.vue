<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    @ok="handleSubmit"
    :showOkBtn="false"
    :cancelText="'关闭'"
  >
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增 </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record),
              },
            },
          ]"
        />
      </template>
    </BasicTable>

    <SelectUser @register="registerModal" @success="handleModelSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicColumn } from '/@/components/Table';
  import { getListUserOfGroup, getAddGroupUser, getDelGroupUserBatch } from '/@/api/system/system';
  import { SelectUser } from '/@/components/CustomComp';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  export default defineComponent({
    name: 'groupUserInfo',
    components: {
      BasicTable,
      TableAction,
      BasicDrawer,
      SelectUser,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let dataRef = reactive({});
      let title = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于：${dataRef.desc} 的用户`;
        setProps({
          searchInfo: {
            groupCode: dataRef.code,
          },
        });
        handleSuccess();
      });

      const getTitle = computed(() => title.value);
      const columns: BasicColumn[] = [
        {
          title: '用户名',
          dataIndex: 'name',
          width: 100,
          ellipsis: true,
        },
        {
          title: '真实姓名',
          dataIndex: 'realName',
          width: 150,
          ellipsis: true,
        },
        {
          title: '性别',
          dataIndex: 'gender',
          width: 100,
          ellipsis: true,
        },
        {
          title: '所属部门',
          dataIndex: 'deptNames',
          align: 'left',
          width: 150,
          ellipsis: true,
        },
      ];
      const [registerTable, { reload, setProps }] = useTable({
        title: '用户列表',
        api: getListUserOfGroup,
        columns,
        formConfig: {
          labelWidth: 120,
        },
        searchInfo: {}, //额外的参数
        resizeHeightOffset: 70,
        immediate: false,
        useSearchForm: false,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });
      function handleCreate() {
        openModal(true, {
          isUpdate: false,
        });
      }

      async function handleDelete(record: Recordable) {
        let params = {
          userNames: record.name,
          groupCode: dataRef?.code,
          groupDesc: dataRef?.desc,
        };
        setDrawerProps({ confirmLoading: true });
        await getDelGroupUserBatch(params).then((res) => {
          setDrawerProps({ confirmLoading: false });
          handleSuccess();
        });
      }

      const [registerModal, { openModal }] = useModal();

      function handleSuccess() {
        reload();
      }

      function handleModelSuccess(data) {
        //选择用户后的回调
        let params = {
          userNames: '',
          userRealNames: '',
          groupCode: dataRef?.code,
          groupDesc: dataRef?.desc,
        };
        let userNames = ref([]);
        let userRealNames = ref([]);
        data.forEach((item) => {
          userNames.value.push(item.name);
          userRealNames.value.push(item.realName);
        });
        params.userNames = userNames.value.join(',');
        params.userRealNames = userRealNames.value.join(',');
        setDrawerProps({ confirmLoading: true });
        getAddGroupUser(params).then((res) => {
          setDrawerProps({ confirmLoading: false });
          handleSuccess();
        });
      }

      async function handleSubmit() {
        try {
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          closeDrawer();
          dataRef = {};
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }
      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        dataRef,
        handleCreate,
        handleDelete,
        registerTable,
        registerModal,
        handleSuccess,
        handleModelSuccess,
      };
    },
  });
</script>
