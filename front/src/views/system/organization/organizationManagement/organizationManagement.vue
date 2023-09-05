<template>
  <PageWrapper dense contentFullHeight contentClass="flex">
    <DeptTreeList class="w-1/4 xl:w-1/5 treeList" @select="handleSelect" />
    <div class="mt-4 mb-4 ml-1 w-3/4 xl:w-4/5">
      <Card
        :headStyle="{ padding: '0 16px' }"
        :bodyStyle="{ padding: 0 }"
        :tab-list="tabListNoTitle"
        :active-tab-key="noTitleKey"
        @tab-change="(key) => onTabChange(key)"
        class="h-79/80"
      >
        <p v-show="noTitleKey === '1'">
          <BasicTable
            @register="registerTable"
            :searchInfo="searchInfo"
            @resize-column="handleResizeColumn"
          >
            <template #toolbar>
              <Popconfirm
                placement="bottom"
                :title="isSyncWxToOA ? '确定从微信同步吗？' : '确定同步至微信吗？'"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleSynWx"
              >
                <a-button type="primary" size="small" v-if="isUseWx" :loading="isWxSyn"
                  >微信同步</a-button
                >
              </Popconfirm>
              <Popconfirm
                placement="bottom"
                :title="isSyncDingDingToOA ? '确定从钉钉同步吗？' : '确定同步至钉钉吗？'"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleSynDd"
              >
                <a-button type="primary" size="small" v-if="isUseDingDing" :loading="isDdSyn"
                  >钉钉同步</a-button
                >
              </Popconfirm>
              <a-button type="primary" size="small" @click="handleCreate">新增</a-button>
              <a-button type="primary" size="small" @click="handleOut">调出</a-button>
              <a-button type="primary" size="small" @click="handleIn">调入</a-button>
              <a-button type="primary" size="small" @click="handleExport" :loading="isExport"
                >导出</a-button
              >
              <ImpExcel :isFiles="false" @raw-file="rawFileFn" dateFormat="YYYY-MM-DD">
                <a-button type="primary" size="small" :loading="isImport">导入</a-button>
              </ImpExcel>
              <Popconfirm
                placement="bottom"
                title="确定删除吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="getSelectRowKeyList"
              >
                <a-button type="primary" size="small">批量删除</a-button>
              </Popconfirm>
            </template>
            <template #action="{ record }">
              <TableAction
                :actions="[
                  {
                    icon: 'clarity:note-edit-line',
                    tooltip: '编辑用户资料',
                    onClick: handleEdit.bind(null, record),
                  },
                  {
                    icon: 'ant-design:delete-outlined',
                    color: 'error',
                    tooltip: '删除此账号',
                    popConfirm: {
                      title: '是否确认删除',
                      placement: 'leftBottom',
                      confirm: handleDelete.bind(null, record),
                    },
                  },
                ]"
                :dropDownActions="[
                  {
                    label: '角色',
                    onClick: handleActions.bind(null, record, 'role'),
                  },
                  {
                    label: '权限',
                    onClick: handleActions.bind(null, record, 'jur'),
                  },
                  {
                    label: '管理部门',
                    onClick: handleActions.bind(null, record, 'dept'),
                  },
                ]"
              />
            </template>
          </BasicTable>
        </p>
        <p v-show="noTitleKey === '2'">
          <Position ref="positionRef" />
        </p>
      </Card>
    </div>
    <OrganizationManagementDrawer @register="registerDrawer" @success="handleSuccess" />
    <UserJurInfo @register="userJurInfoDrawer" />
    <UserDeptInfo @register="userDeptInfoDrawer" />
    <UserRoleInfo @register="userRoleInfoDrawer" @success="handleSuccess" />
    <CallOutDrawer @register="callOutDrawer" @success="handleSuccess" />
    <SelectUser @register="registerModal" @success="handleInSave" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref, unref, onMounted, watchEffect, h, nextTick } from 'vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import {
    getUerList,
    getUserDelUsers,
    getTransferUsers,
    getExportUser,
    getUserImportFinish,
    getUserImportConfirm,
    getConfigInfo,
    synAll,
  } from '/@/api/system/system';
  import { PageWrapper } from '/@/components/Page';
  import { Popconfirm, Card } from 'ant-design-vue';
  import { useDrawer } from '/@/components/Drawer';
  import OrganizationManagementDrawer from './organizationManagementDrawer.vue';

  import { columns, searchFormSchema, cardTabList, isChange } from './organizationManagement.data';
  // import { useGo } from '/@/hooks/web/usePage';
  import Position from './position/index.vue';
  import DeptTreeList from './deptTreeList.vue';
  import UserJurInfo from './modules/userJurInfo/userJurInfo.vue';
  import UserDeptInfo from './modules/userDeptInfo/userDeptInfo.vue';
  import UserRoleInfo from './modules/userRoleInfo/userRoleInfo.vue';
  import CallOutDrawer from './modules/callOutDrawer.vue';
  import { useModal } from '/@/components/Modal';
  import { SelectUser } from '/@/components/CustomComp';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { downloadByData } from '/@/utils/file/download';
  import { ImpExcel } from '/@/components/Excel';
  export default defineComponent({
    name: 'OrganizationManagement',
    components: {
      BasicTable,
      PageWrapper,
      DeptTreeList,
      OrganizationManagementDrawer,
      TableAction,
      Card,
      Position,
      UserJurInfo,
      UserDeptInfo,
      UserRoleInfo,
      CallOutDrawer,
      SelectUser,
      Popconfirm,
      ImpExcel,
    },
    setup() {
      // const go = useGo();
      let selectRows = ref([]);
      const { createMessage } = useMessage();
      const positionRef = ref<null | HTMLElement>(null);
      const isExport = ref(false);
      const isImport = ref(false);
      let key = ref('1');
      let noTitleKey = ref('1');
      const [registerDrawer, { openDrawer }] = useDrawer();
      // , isValid: '1', op: 'search'
      const searchInfo = reactive<Recordable>({ deptCode: 'root' });
      const isUseWx = ref(false);
      const isSyncWxToOA = ref(false);
      const isUseDingDing = ref(false);
      const isSyncDingDingToOA = ref(false);
      const isWxSyn = ref(false);
      const isDdSyn = ref(false);
      const [registerTable, { reload, getSelectRowKeys, setSelectedRowKeys, setProps }] = useTable({
        title: '人员列表',
        api: getUerList,
        immediate: false,
        rowKey: 'id',
        columns,
        formConfig: {
          labelWidth: 60,
          schemas: searchFormSchema,
          autoSubmitOnEnter: true,
        },
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        rowSelection: {
          type: 'checkbox',
        },
        searchInfo: {
          op: 'search',
          deptCode: 'root',
        }, //额外的参数
        canResize: true,
        // resizeHeightOffset: 50,
        actionColumn: {
          width: 160,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
        pagination: {
          pageSize: 50,
        },
      });

      function handleCreate() {
        openDrawer(true, {
          isUpdate: false,
          record: {},
        });
      }

      function handleEdit(record) {
        openDrawer(true, {
          record,
          isUpdate: true,
        });
      }

      function getSelectRowKeyList() {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('请选择数据');
          return;
        }
        getUserDelUsers({ ids: keys.join(',') }).then(() => {
          handleSuccess();
        });
      }

      async function handleDelete(record) {
        await getUserDelUsers({ ids: record.user.id }).then(() => {
          handleSuccess();
        });
      }

      function handleSelect(deptId = '') {
        searchInfo.deptCode = deptId;
        if (noTitleKey.value == '1') {
          setProps({ searchInfo: { op: 'search', deptCode: searchInfo.deptCode } });
          reload();
        } else if (noTitleKey.value == '2') {
          console.log('handleSelect positionRef', positionRef.value);
          positionRef.value?.fromRef(searchInfo.deptCode);
        }
      }

      function handleActions(record, type) {
        switch (type) {
          case 'role':
            createUserRoleInfoDrawer(record);
            break;
          case 'jur':
            createUserJurInfoDrawer(record);
            break;
          case 'dept':
            createUserDeptInfoDrawer(record);
            break;
          default:
            break;
        }
      }
      //角色
      const [userRoleInfoDrawer, { openDrawer: openDrawerUserRole }] = useDrawer();
      function createUserRoleInfoDrawer(record) {
        openDrawerUserRole(true, {
          record,
        });
      }

      //权限
      const [userJurInfoDrawer, { openDrawer: openDrawerUserJur }] = useDrawer();
      function createUserJurInfoDrawer(record) {
        openDrawerUserJur(true, {
          record,
        });
      }
      //部门
      const [userDeptInfoDrawer, { openDrawer: openDrawerUserDept }] = useDrawer();
      function createUserDeptInfoDrawer(record) {
        openDrawerUserDept(true, {
          record,
        });
      }

      //调出
      const [callOutDrawer, { openDrawer: openDrawerCallOut }] = useDrawer();
      function createCallOutoDrawer(selectRows) {
        openDrawerCallOut(true, {
          record: { ids: selectRows },
        });
      }
      //调出点击
      function handleOut() {
        selectRows.value = getSelectRowKeys() as any;
        if (unref(selectRows).length === 0) {
          createMessage.warning('请选择人员');
          return;
        }
        createCallOutoDrawer(unref(selectRows));
      }
      //调入
      const [registerModal, { openModal }] = useModal();
      function handleIn() {
        openModal(true, {
          isUpdate: false,
        });
      }

      //导出
      async function handleExport() {
        isExport.value = true;
        let excel = await getExportUser();
        try {
          await downloadByData(excel, '全部人员.xls');
        } finally {
          isExport.value = false;
        }
      }

      //获取files
      function rawFileFn(files) {
        let formData = new FormData();
        formData.append('att1', files);
        isImport.value = true;
        getUserImportConfirm(formData).then((res) => {
          if (res.res != 0) {
            createMessage.warn(h('pre', res.msg));
            isImport.value = false;
            return;
          }
          let successArr = res.successArr;
          getUserImportFinish({ info: JSON.stringify(successArr) }).then(() => {
            createMessage.success('操作成功');
            handleSuccess();
            isImport.value = false;
          });
        });
      }

      async function handleInSave(data) {
        if (data && data.length > 0) {
          let userNames = data.map((item) => item.name).join(',');
          let params = {
            deptCode: searchInfo.deptCode,
            userNames: userNames,
          };
          await getTransferUsers(params);
          await handleSuccess();
        }
      }

      function handleSuccess() {
        setSelectedRowKeys([]);
        reload();
      }
      // function handleView(record: Recordable) {
      //   go('/system/account_detail/' + record.id);
      // }
      const tabListNoTitle = ref<any>([]);
      const onTabChange = async (value: string) => {
        noTitleKey.value = value;
        await nextTick();
        if (value == '1') {
          setProps({ searchInfo: { op: 'search', deptCode: searchInfo.deptCode } });
          reload();
        } else if (value == '2') {
          positionRef.value.fromRef(searchInfo.deptCode);
          console.log(value, '--', type);
          console.log('onTabChange searchInfo.deptCode', searchInfo.deptCode);

          handleSelect(searchInfo.deptCode);
        }
      };

      watchEffect(() => {
        if (isChange.value) {
          handleSuccess();
          isChange.value = false;
        }
      }); //监听启用成功后isChange为true时刷新列表

      onMounted(() => {
        // window.handleSuccess = handleSuccess;
        getIsPostUsedInfo();
        reload();
      });

      function getIsPostUsedInfo() {
        getConfigInfo().then((res) => {
          let is = res.isPostUsed;
          tabListNoTitle.value = is ? cardTabList : [];

          isUseWx.value = res.isUseWx;
          isSyncWxToOA.value = res.isSyncWxToOA;
          isUseDingDing.value = res.isUseDingDing;
          isSyncDingDingToOA.value = res.isSyncDingDingToOA;
        });
      }

      function handleSynWx() {
        isWxSyn.value = true;
        let op = isSyncWxToOA.value ? 'syncWeixinToOA' : 'syncWeixin';
        synAll({ op })
          .then((data) => {
            if (data.res == 0) {
              createMessage.success('操作成功');
            }
          })
          .finally(() => {
            isWxSyn.value = false;
          });
      }

      function handleSynDd() {
        isDdSyn.value = true;
        let op = isSyncDingDingToOA.value ? 'syncDingdingToOA' : 'syncOAToDingding';
        synAll({ op })
          .then((data) => {
            if (data.res == 0) {
              createMessage.success('操作成功');
            }
          })
          .finally(() => {
            isDdSyn.value = false;
          });
      }

      return {
        registerTable,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleSelect,
        // handleView,
        searchInfo,
        tabListNoTitle,
        noTitleKey,
        onTabChange,
        getSelectRowKeyList,
        handleActions,
        userJurInfoDrawer,
        userDeptInfoDrawer,
        userRoleInfoDrawer,
        handleOut,
        callOutDrawer,
        registerModal,
        handleIn,
        handleInSave,
        positionRef,
        handleExport,
        rawFileFn,
        isExport,
        isImport,
        isUseWx,
        isUseDingDing,
        handleSynWx,
        handleSynDd,
        isWxSyn,
        isDdSyn,
        isSyncWxToOA,
        isSyncDingDingToOA,
        handleResizeColumn: (w, col) => {
          col.width = w;
        },
      };
    },
  });
</script>
<style lang="less" scoped>
  ::v-deep .vben-basic-table-form-container .ant-form {
    padding: 0;
    margin-bottom: 10px;
  }
  ::v-deep .vben-basic-table-form-container {
    padding-bottom: 0;
  }
</style>
