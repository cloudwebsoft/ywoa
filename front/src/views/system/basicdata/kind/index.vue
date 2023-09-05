<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增类型 </a-button>
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
    <KindDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getListBasicDataKind, getDelBasicKind } from '/@/api/system/system';
  import { useDrawer } from '/@/components/Drawer';
  import KindDrawer from './KindDrawer.vue';
  import { columns, searchFormSchema } from './kind.data';

  export default defineComponent({
    name: 'BasicDataKindManagement',
    components: {
      BasicTable,
      KindDrawer,
      TableAction,
    },
    setup() {
      const [registerDrawer, { openDrawer: openDrawerIndex }] = useDrawer();
      const [registerTable, { reload }] = useTable({
        title: '基础数据类型列表',
        api: getListBasicDataKind,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
        },
        searchInfo: {
          // op: 'search',
        }, // 额外的参数
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        pagination: false,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      function handleCreate() {
        openDrawerIndex(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        openDrawerIndex(true, {
          record: {
            ...record,
            desc: record.description,
          },
          isUpdate: true,
        });
      }

      async function handleDelete(record: Recordable) {
        await getDelBasicKind({ id: record.id }).then(() => {
          handleSuccess();
        });
      }

      function handleSuccess() {
        reload();
      }

      onMounted(() => {
        // fetch();
        window.handleSuccess = handleSuccess;
      });
      return {
        registerTable,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
      };
    },
  });
</script>
