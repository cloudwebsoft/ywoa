<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleGotoKind"> 类型 </a-button>
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
            {
              icon: 'ant-design:unordered-list-outlined',
              onClick: handleEditOptions.bind(null, record),
            },
          ]"
        />
      </template>
    </BasicTable>
    <SelectDrawer @register="registerDrawer" @success="handleSuccess" />
    <OptionDrawer @register="optionDrawer" @success="handleSuccess" />
    <TreeDrawer @register="treeDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getListBasicData, getDelBasic } from '/@/api/system/system';
  import { useDrawer } from '/@/components/Drawer';
  import SelectDrawer from './selectDrawer.vue';
  import OptionDrawer from './optionInfo/optionInfo.vue';
  import TreeDrawer from './treeInfo/index.vue';
  import { columns, searchFormSchema } from './select.data';
  import { useGo } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'BasicDataManagement',
    components: {
      BasicTable,
      SelectDrawer,
      OptionDrawer,
      TreeDrawer,
      TableAction,
    },
    setup() {
      const go = useGo();

      const [registerDrawer, { openDrawer: openDrawerSelect }] = useDrawer();
      const [optionDrawer, { openDrawer: openDrawerOption }] = useDrawer();
      const [treeDrawer, { openDrawer: openDrawerTree }] = useDrawer();
      const [registerTable, { reload }] = useTable({
        title: '基础数据列表',
        api: getListBasicData,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
          autoSubmitOnEnter: true,
        },
        searchInfo: {
          // op: 'search',
        }, // 额外的参数
        canResize: true,
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        showIndexColumn: true,
        indexColumnProps: { width: 50 },
        pagination: true,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      function handleCreate() {
        openDrawerSelect(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        openDrawerSelect(true, {
          record: {
            ...record,
            desc: record.description,
          },
          isUpdate: true,
        });
      }

      function handleEditOptions(record: Recordable) {
        console.log(record);
        if (record.type == 0) {
          openDrawerOption(true, {
            record: {
              ...record,
              desc: record.name,
            },
            isUpdate: true,
          });
        } else {
          openDrawerTree(true, {
            record: {
              ...record,
              desc: record.name,
            },
          });
        }
      }

      function handleGotoKind() {
        go('/basicdataKind');
      }

      async function handleDelete(record: Recordable) {
        await getDelBasic({ code: record.code }).then(() => {
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
        optionDrawer,
        treeDrawer,
        handleCreate,
        handleEdit,
        handleEditOptions,
        handleDelete,
        handleSuccess,
        handleGotoKind,
      };
    },
  });
</script>
