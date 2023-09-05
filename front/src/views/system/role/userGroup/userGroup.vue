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
              tooltip: '编辑',
              onClick: handleEdit.bind(null, record),
            },
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              ifShow: !record.isSystem,
              tooltip: '删除',
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record),
              },
            },
          ]"
          :dropDownActions="[
            {
              label: '用户',
              onClick: handleUser.bind(null, record, 'user'),
            },
            {
              label: '权限',
              onClick: handleUser.bind(null, record, 'jur'),
            },
          ]"
        />
      </template>
    </BasicTable>
    <UserGroupDrawer @register="registerDrawer" @success="handleSuccess" />
    <GroupUserInfo @register="groupUserInfoDrawer" @success="handleSuccess" />
    <GroupJur @register="groupJurDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, unref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getListGroup, getDelGroup } from '/@/api/system/system';
  import { useDrawer } from '/@/components/Drawer';
  import UserGroupDrawer from './userGroupDrawer.vue';
  import { columns, searchFormSchema } from './userGroup.data';
  import GroupUserInfo from './groupUserInfo/groupUserInfo.vue';
  import GroupJur from './groupJur/groupJur.vue';

  export default defineComponent({
    name: 'userGroup',
    components: {
      BasicTable,
      UserGroupDrawer,
      TableAction,
      GroupUserInfo,
      GroupJur,
    },
    setup() {
      const [registerDrawer, { openDrawer: openDrawerIndex }] = useDrawer();
      const [registerTable, { reload }] = useTable({
        title: '用户组列表',
        api: getListGroup,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
        },
        searchInfo: {
          op: 'search',
        }, //额外的参数
        useSearchForm: true,
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
        openDrawerIndex(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        openDrawerIndex(true, {
          record,
          isUpdate: true,
        });
      }

      async function handleDelete(record: Recordable) {
        await getDelGroup({ groupCode: record.code }).then((res) => {
          handleSuccess();
        });
      }

      async function handleUser(record: Recordable, type: string) {
        switch (type) {
          case 'user':
            console.log('用户');
            createUserInfoDrawer(record);
            break;
          case 'jur':
            console.log(type);
            createJurInfoDrawer(record);
            break;
          default:
            console.log('default');
        }
      }

      // 用户
      const [groupUserInfoDrawer, { openDrawer: openDrawerUser }] = useDrawer();
      function createUserInfoDrawer(record) {
        openDrawerUser(true, {
          record,
        });
      }
      //权限
      const [groupJurDrawer, { openDrawer: openDrawerJur }] = useDrawer();
      function createJurInfoDrawer(record) {
        openDrawerJur(true, {
          record,
        });
      }

      function handleSuccess() {
        reload();
      }
      return {
        registerTable,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        groupUserInfoDrawer,
        groupJurDrawer,
        handleUser,
      };
    },
  });
</script>
