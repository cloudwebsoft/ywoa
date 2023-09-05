<template>
  <div>
    <Tabs class="bg-white" v-model:activeKey="activeKey" centered>
      <TabPane key="1" tab="抄送给我的" force-render />
      <TabPane key="2" tab="我抄送的" force-render />
    </Tabs>
    <Row v-show="activeKey == 1">
      <div>
        <BasicTable @register="registerTable">
          <template #toolbar>
            <!-- <a-button type="primary" @click="handleCreate"> 新增 </a-button> -->
          </template>
          <template #action="{ record }">
            <TableAction
              :actions="[
                {
                  icon: 'clarity:info-standard-line',
                  tooltip: '查看',
                  onClick: handleView.bind(null, record),
                },
                // {
                //   icon: 'clarity:cursor-hand-click-line',
                //   tooltip: '处理',
                //   onClick: handleEdit.bind(null, record),
                // },
                // {
                //   icon: 'clarity:deploy-line',
                //   tooltip: '抄送',
                //   onClick: handleDistribute.bind(null, record),
                // },
                // {
                //   icon: 'ant-design:delete-outlined',
                //   color: 'error',
                //   popConfirm: {
                //     title: '是否确认删除',
                //     placement: 'leftBottom',
                //     confirm: handleDelete.bind(null, record),
                //   },
                // },
              ]"
            />
          </template>
        </BasicTable>
      </div>
    </Row>
    <Row v-show="activeKey == 2">
      <div
        ><BasicTable @register="registerTableMine">
          <template #actionMine="{ record }">
            <TableAction
              :actions="[
                {
                  icon: 'clarity:info-standard-line',
                  tooltip: '查看',
                  onClick: handleView.bind(null, record),
                },
                {
                  icon: 'ant-design:delete-outlined',
                  color: 'error',
                  tooltip: '删除',
                  onClick: handleDelete.bind(null, record),
                },
              ]"
            />
          </template> </BasicTable
      ></div>
    </Row>
    <ProcessShowDrawer @register="registerViewDrawer" @success="handleSuccess" />
    <DistributeModal @register="registerDistributeModal" @success="handleDistributeCallBack" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, h, ref } from 'vue';
  import { Row, Tabs, TabPane } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import {
    getListDistributeToMe,
    getListMyDistribute,
    getDelDistribute,
  } from '/@/api/process/process';

  import { useDrawer } from '/@/components/Drawer';

  import { columns, searchFormSchema } from './flowDistribute.data';
  import DistributeModal from './modules/DistributeModal.vue';
  import ProcessShowDrawer from './processShowDrawer.vue';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'flowDistribute',
    components: { BasicTable, TableAction, DistributeModal, ProcessShowDrawer, Row, Tabs, TabPane },
    setup() {
      const activeKey = ref('1');

      const { createMessage, createConfirm } = useMessage();
      const { t } = useI18n();
      const [registerDrawer, { openDrawer }] = useDrawer();
      const [registerTable] = useTable({
        title: '流程抄送列表',
        api: getListDistributeToMe,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
          autoSubmitOnEnter: true,
        },
        searchInfo: {
          op: 'search',
        }, //额外的参数
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        beforeFetch: (info) => {
          let newInfo = info;
          if (newInfo.dates && newInfo.dates.length > 0) {
            newInfo.fromDate = newInfo.dates[0];
            newInfo.toDate = newInfo.dates[1];
            delete newInfo.dates;
          }
          return newInfo;
        },
        actionColumn: {
          width: 100,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      const [registerTableMine, { reload }] = useTable({
        title: '流程抄送列表',
        api: getListMyDistribute,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
          autoSubmitOnEnter: true,
        },
        searchInfo: {
          op: 'search',
        }, //额外的参数
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        beforeFetch: (info) => {
          let newInfo = info;
          if (newInfo.dates && newInfo.dates.length > 0) {
            newInfo.fromDate = newInfo.dates[0];
            newInfo.toDate = newInfo.dates[1];
            delete newInfo.dates;
          }
          return newInfo;
        },
        actionColumn: {
          width: 150,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'actionMine' },
          fixed: undefined,
        },
      });

      function handleCreate() {
        openDrawer(true, {
          isUpdate: false,
        });
      }

      const handleDelete = (record: Recordable) => {
        createConfirm({
          iconType: 'warning',
          title: () => h('span', t('common.prompt')),
          content: () => h('span', '您确定要删除么'),
          onOk: async () => {
            await getDelDistribute({ id: record.id }).then(() => {
              createMessage.success(t('common.opSuccess'));
              reload();
            });
          },
        });
      };

      function handleSuccess() {
        reload();
      }

      const [registerViewDrawer, { openDrawer: openViewDrawer }] = useDrawer();
      const handleView = (record: any) => {
        openViewDrawer(true, {
          flowId: record.flowId,
          visitKey: record.visitKey,
        });
      };

      // ------------------------------------抄送开始----------------------------------------------
      const [registerDistributeModal, { openModal: openDistributeModal }] = useModal();
      //展示modal
      const handleDistribute = () => {
        openDistributeModal(true, {
          isUpdate: true,
          record: {},
        });
      };
      //modal回调
      const handleDistributeCallBack = () => {};
      // ------------------------------------抄送结束----------------------------------------------

      return {
        registerTable,
        registerDrawer,
        handleCreate,
        handleDelete,
        handleSuccess,
        handleView,
        registerDistributeModal,
        handleDistributeCallBack,
        handleDistribute,
        registerViewDrawer,
        activeKey,
        registerTableMine,
      };
    },
  });
</script>
