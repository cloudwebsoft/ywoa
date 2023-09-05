<template>
  <div>
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
          :dropDownActions="[
            {
              label: '成员管理',
              onClick: handleActions.bind(null, record, 'people'),
            },
          ]"
        />
      </template>
    </BasicTable>
    <PositionDrawer @register="registerDrawer" @success="handleSuccess" />
    <MemberManagementDrawer @register="registermemberManagementDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref, nextTick } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getPostList, getPostDel } from '/@/api/system/system';
  import { useDrawer } from '/@/components/Drawer';
  import PositionDrawer from './PositionDrawer.vue';
  import MemberManagementDrawer from './memberManagementDrawer.vue';

  import { columns, searchFormSchema } from './postion.data';

  export default defineComponent({
    name: 'Position',
    components: { BasicTable, PositionDrawer, TableAction, MemberManagementDrawer },
    setup() {
      let searchInfo = reactive<any>({ deptCode: 'root', op: 'search' });
      const [registerDrawer, { openDrawer }] = useDrawer();
      const [registerTable, { reload, setProps }] = useTable({
        title: '岗位列表',
        api: getPostList,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
        },
        searchInfo: searchInfo,
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        showIndexColumn: false,
        immediate: false,
        indexColumnProps: { width: 50 },
        canResize: true,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      function handleCreate() {
        openDrawer(true, {
          isUpdate: false,
          record: { deptCode: searchInfo.deptCode },
        });
      }

      function handleEdit(record: any) {
        openDrawer(true, {
          record: {
            ...record,
            // deptCode: searchInfo.deptCode,
          },
          isUpdate: true,
        });
      }

      async function handleDelete(record: any) {
        await getPostDel({ ids: record.id });
        handleSuccess();
      }

      function handleActions(record, type) {
        switch (type) {
          case 'people':
            createUserRoleInfoDrawer(record);
            break;
          default:
            break;
        }
      }

      //成员管理
      const [registermemberManagementDrawer, { openDrawer: openDrawermember }] = useDrawer();
      function createUserRoleInfoDrawer(record) {
        openDrawermember(true, {
          record,
        });
      }

      function handleSuccess() {
        reload();
      }
      const fromRef = async (code) => {
        searchInfo = {
          ...searchInfo,
          deptCode: code,
        };
        setProps({
          searchInfo: searchInfo,
        });
        await nextTick();
        handleSuccess();
      };

      return {
        registerTable,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        fromRef,
        handleActions,
        registermemberManagementDrawer,
      };
    },
  });
</script>
