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
              ifShow: record.code != 'member',
            },
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              tooltip: '删除',
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record),
              },
              ifShow: record.code != 'member',
            },
          ]"
          :dropDownActions="
            record.code == 'member'
              ? [
                  {
                    label: '权限',
                    onClick: handleUser.bind(null, record, 'jur'),
                  },
                  {
                    label: '管理部门',
                    onClick: handleUser.bind(null, record, 'dept'),
                  },
                  {
                    label: '菜单',
                    onClick: handleUser.bind(null, record, 'menu'),
                  },
                ]
              : [
                  {
                    label: '用户',
                    onClick: handleUser.bind(null, record, 'user'),
                  },
                  {
                    label: '组织',
                    onClick: handleUser.bind(null, record, 'org'),
                  },
                  {
                    label: '职位',
                    onClick: handleUser.bind(null, record, 'position'),
                    ifShow: () => isPostUser,
                  },
                  {
                    label: '权限',
                    onClick: handleUser.bind(null, record, 'jur'),
                  },
                  {
                    label: '管理部门',
                    onClick: handleUser.bind(null, record, 'dept'),
                  },
                  {
                    label: '菜单',
                    onClick: handleUser.bind(null, record, 'menu'),
                  },
                ]
          "
        />
      </template>
    </BasicTable>
    <RoleDrawer @register="registerDrawer" @success="handleSuccess" />
    <UserInfoDrawer @register="userInfoDrawer" />
    <OrgInfoDrawer @register="orgInfoDrawer" />
    <PositionInfo @register="positionInfoDrawer" />
    <JurInfo @register="jurInfoDrawer" />
    <DeptInfo @register="deptInfoDrawer" />
    <MenuInfo @register="menuInfoDrawer" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getListRole, getDelRole, getConfigInfo } from '/@/api/system/system';
  import { useDrawer } from '/@/components/Drawer';
  import RoleDrawer from './RoleDrawer.vue';
  import { columns, searchFormSchema } from './role.data';
  import UserInfoDrawer from './modules/userInfo/userInfo.vue';
  import OrgInfoDrawer from './modules/orgInfo/orgInfo.vue';
  import PositionInfo from './modules/positionInfo/positionInfo.vue';
  import JurInfo from './modules/jurInfo/jurInfo.vue';
  import DeptInfo from './modules/deptInfo/deptInfo.vue';
  import MenuInfo from './modules/menuInfo/menuInfo.vue';

  export default defineComponent({
    name: 'RoleManagement',
    components: {
      BasicTable,
      RoleDrawer,
      TableAction,
      UserInfoDrawer,
      OrgInfoDrawer,
      PositionInfo,
      JurInfo,
      DeptInfo,
      MenuInfo,
    },
    setup() {
      const [registerDrawer, { openDrawer: openDrawerIndex }] = useDrawer();
      const [registerTable, { reload, setTableData }] = useTable({
        title: '角色列表',
        api: getListRole,
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
        afterFetch: afterFetch,
      });
      function afterFetch(res) {
        console.log('res==>', res);
        let data = res;
        if (data && data.length > 0) {
          data.forEach((item) => {
            item.oldOrders = item.orders;
          });
          console.log('data', data);
          setTableData(data);
        }
      }
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
        await getDelRole({ code: record.code }).then(() => {
          handleSuccess();
        });
      }

      async function handleUser(record: Recordable, type: string) {
        switch (type) {
          case 'user':
            createUserInfoDrawer(record);
            break;
          case 'org':
            createOrgInfoDrawer(record);
            break;
          case 'position':
            createPositionInfoDrawer(record);
            break;
          case 'jur':
            createJurInfoDrawer(record);
            break;
          case 'dept':
            createDeptInfoDrawer(record);
            break;
          case 'menu':
            createMenuInfoDrawer(record);
            break;
          default:
            break;
        }
      }
      // 用户
      const [userInfoDrawer, { openDrawer: openDrawerUser }] = useDrawer();
      function createUserInfoDrawer(record) {
        openDrawerUser(true, {
          record,
        });
      }
      //组织
      const [orgInfoDrawer, { openDrawer: openDrawerOrg }] = useDrawer();
      function createOrgInfoDrawer(record) {
        openDrawerOrg(true, {
          record,
        });
      }
      // 岗位
      const [positionInfoDrawer, { openDrawer: openDrawerPosition }] = useDrawer();
      function createPositionInfoDrawer(record) {
        openDrawerPosition(true, {
          record,
        });
      }
      //权限
      const [jurInfoDrawer, { openDrawer: openDrawerJur }] = useDrawer();
      function createJurInfoDrawer(record) {
        openDrawerJur(true, {
          record,
        });
      }
      //部门
      const [deptInfoDrawer, { openDrawer: openDrawerDept }] = useDrawer();
      function createDeptInfoDrawer(record) {
        openDrawerDept(true, {
          record,
        });
      }
      //菜单
      const [menuInfoDrawer, { openDrawer: openDrawerMenu }] = useDrawer();
      function createMenuInfoDrawer(record) {
        openDrawerMenu(true, {
          record,
        });
      }
      function handleSuccess() {
        reload();
      }
      const isPostUser = ref(true);
      onMounted(() => {
        // fetch();
        window.handleSuccess = handleSuccess;
        getIsPostUsedInfo();
      });

      function getIsPostUsedInfo() {
        getConfigInfo().then((res) => {
          isPostUser.value = res.isPostUsed;
        });
      }
      return {
        registerTable,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleUser,
        userInfoDrawer, //用户
        orgInfoDrawer, //组织
        positionInfoDrawer, //岗位
        jurInfoDrawer, //权限
        deptInfoDrawer, //部门
        menuInfoDrawer, //菜单
        isPostUser,
        // fetch,
      };
    },
  });
</script>
