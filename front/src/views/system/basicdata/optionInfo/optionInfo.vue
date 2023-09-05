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
    @close="handleClose"
  >
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增 </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'clarity:note-edit-line',
              onClick: handleEdit.bind(null, record),
            },
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

    <optionInfoModel @register="registerModal" @success="handleSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicColumn } from '/@/components/Table';
  import { getBasicOptions, getDelBasicOption } from '/@/api/system/system';
  import optionInfoModel from './optionInfoModel.vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  export default defineComponent({
    name: 'OptionInfo',
    components: {
      BasicTable,
      TableAction,
      BasicDrawer,
      optionInfoModel,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let dataRef = reactive<any>({});
      let title = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于：${dataRef.name} 的选项`;
        setProps({
          searchInfo: {
            code: dataRef.code,
          },
        });
        handleSuccess();
      });

      const getTitle = computed(() => title.value);
      async function handleSubmit() {
        try {
          setDrawerProps({ confirmLoading: true });
          closeDrawer();
          dataRef = {};
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }
      const columns: BasicColumn[] = [
        {
          title: '排序号',
          dataIndex: 'orders',
          width: 100,
          ellipsis: true,
        },
        {
          title: '名称',
          dataIndex: 'name',
          width: 100,
          ellipsis: true,
        },
        {
          title: '值',
          dataIndex: 'value',
          width: 150,
          ellipsis: true,
        },
        {
          title: '是否启用',
          dataIndex: 'open',
          width: 100,
          ellipsis: true,
          customRender: function ({ record }) {
            return record.open ? '是' : '否';
          },
        },
        {
          title: '是否默认',
          dataIndex: 'default',
          width: 100,
          ellipsis: true,
          customRender: function ({ record }) {
            return record.default ? '是' : '否';
          },
        },
        {
          title: '颜色',
          dataIndex: 'color',
          width: 150,
          ellipsis: true,
        },
      ];
      const [registerTable, { reload, setProps }] = useTable({
        title: '选项列表',
        api: getBasicOptions,
        columns,
        formConfig: {
          labelWidth: 120,
        },
        searchInfo: {}, // 额外的参数
        resizeHeightOffset: 70,
        immediate: false,
        useSearchForm: false,
        showTableSetting: true,
        bordered: true,
        pagination: false,
        showIndexColumn: true,
        indexColumnProps: { width: 50 },
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
          code: dataRef.code,
          isUpdate: false,
          record: dataRef,
        });
      }

      function handleEdit(record: Recordable) {
        openModal(true, {
          code: dataRef.code,
          isUpdate: true,
          record: record,
        });
      }

      async function handleDelete(record: Recordable) {
        await getDelBasicOption({ id: record.id }).then((res) => {
          handleSuccess();
        });
      }

      const [registerModal, { openModal }] = useModal();

      function handleSuccess() {
        console.log('success');
        reload();
      }

      function handleClose() {
        emit('success');
      }

      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        dataRef,
        handleCreate,
        handleEdit,
        handleDelete,
        registerTable,
        registerModal,
        handleSuccess,
        handleClose,
      };
    },
  });
</script>
