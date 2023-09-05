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

    <SelectUser @register="registerModal" @success="handleSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicColumn } from '/@/components/Table';
  import { getRolePostList, getRolePostDel } from '/@/api/system/system';
  import SelectUser from './positionInfoModal.vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  export default defineComponent({
    name: 'PositionInfo',
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
        title.value = `属于角色：${dataRef.description} 的职位`;
        setProps({
          searchInfo: {
            roleCode: dataRef.code,
          },
        });
        handleSuccess();
      });

      const getTitle = computed(() => title.value);
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
      const columns: BasicColumn[] = [
        {
          title: '名称',
          dataIndex: 'name',
          width: 100,
          ellipsis: true,
        },
        {
          title: '部门',
          dataIndex: 'fullDeptName',
          width: 150,
          ellipsis: true,
        },
        {
          title: '描述',
          dataIndex: 'description',
          width: 150,
          ellipsis: true,
        },
        {
          title: '限定人数',
          dataIndex: 'numLimited',
          width: 100,
          ellipsis: true,
        },
        {
          title: '成员',
          dataIndex: 'member',
          width: 50,
          ellipsis: true,
        },
      ];
      const [registerTable, { reload, setProps }] = useTable({
        title: '职位列表',
        api: getRolePostList,
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
          record: dataRef,
        });
      }
      async function handleDelete(record: Recordable) {
        await getRolePostDel({ ids: record.id }).then((res) => {
          handleSuccess();
        });
      }

      const [registerModal, { openModal }] = useModal();

      function handleSuccess() {
        reload();
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
      };
    },
  });
</script>
