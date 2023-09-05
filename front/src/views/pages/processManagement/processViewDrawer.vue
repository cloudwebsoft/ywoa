<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="90%"
    :showOkBtn="false"
    :cancelText="'关闭'"
    ref="containerRef"
    :destroyOnClose="true"
    @close="closeCurrentDrawer"
  >
    <Row>
      <Button id="btnPrintForm" type="primary" size="small" class="ml-2" @click="printForm">
        <template #icon><PrinterOutlined /></template>打印表单
      </Button>
      <Button id="btnPrintAll" type="primary" size="small" class="ml-2" @click="printFormBox">
        <template #icon><PrinterOutlined /></template>打印全部
      </Button>
    </Row>
    <Row>
      <div class="border-1 border-solid border-gray-80 min-h-full w-full" id="flowFormViewBox">
        <form id="flowFormView">
          <input type="hidden" id="op" name="op" value="saveformvalue" />
          <input type="hidden" name="isAfterSaveformvalueBeforeXorCondSelect" />
          <div class="p-2" v-html="content"></div>
        </form>
        <BasicTable @register="registerHandleTable" />
      </div>
    </Row>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getViewShow } from '/@/api/process/process';
  import { Button, Row } from 'ant-design-vue';
  import { removeScript, removeLink, filterJS, ajaxGetJS, loadImg } from '/@/utils/utils';
  import { BasicTable, useTable } from '/@/components/Table';
  import { PrinterOutlined } from '@ant-design/icons-vue';
  import printJS from 'print-js';
  import { getShowImg } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';

  export default defineComponent({
    name: 'ProcessViewDrawer',
    components: {
      BasicDrawer,
      Button,
      Row,
      BasicTable,
      PrinterOutlined,
    },
    emits: ['success', 'register', 'handleCurrent'],
    setup(_, { emit }) {
      const { createMessage, createConfirm } = useMessage();
      const content = ref('');
      let getTitle = '查看视图';
      const { t } = useI18n();
      const flowId = ref('');
      const visitKey = ref('');
      let viewId = 0;
      const flowIsRemarkShow = ref(true);

      //初始化视图抽屉
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        console.log('data', data);
        setDrawerProps({ confirmLoading: false });
        viewId = data.viewId;
        flowId.value = data.flowId;
        visitKey.value = data.visitKey;
        getFlowProcessContent();
      });

      // -----------------------------------------------------获取当前流程信息开始-------------------------------------------------------------
      //获取当前流程信息
      let flowProcessData = ref<any>({});
      async function getFlowProcessContent() {
        let data = await getViewShow({ flowId: flowId.value, viewId: viewId });
        flowProcessData.value = data;
        getTitle = flowProcessData.value.flowTitle;

        // 页面风格
        let cont = data.content;
        let isPageStyleLight = data.isPageStyleLight;
        if (isPageStyleLight) {
          cont = cont.replace('tabStyle_8', 'tabStyle_1');
        }
        content.value = cont;

        flowIsRemarkShow.value = data.flowIsRemarkShow;

        await filterJS(data.content, '-src', o('flowFormView'));

        if (flowProcessData.value.aryMyAction && flowProcessData.value.aryMyAction.length > 0) {
          setTableData(flowProcessData.value.aryMyAction);
        } else {
          setTableData([]);
        }

        // 加载后台事件中配置的前台脚本
        if (data.formJs && data.formJs.length > 0) {
          let scriptFormJs = document.createElement('script');
          scriptFormJs.type = 'text/javascript';
          scriptFormJs.text = data.formJs;
          scriptFormJs.id = `${101}${srcId.value}`;
          document.getElementsByTagName('head')[0].appendChild(scriptFormJs);
        }

        loadImg('flowFormView');
      }

      //关闭抽屉
      function closeCurrentDrawer() {
        emit('success');
        removeScript();
        removeLink();
      }

      function printForm() {
        htmlPrint('flowFormView');
      }

      function printFormBox() {
        htmlPrint('flowFormViewBox');
      }

      function htmlPrint(ele: string) {
        printJS({
          printable: document.getElementById(ele),
          type: 'html',
          targetStyles: ['*'], // 继承原来的所有样式
          css: '/src/assets/css/css.css',
          scanStyles: false, // css库将不处理应用于正在打印的html的样式，与css参数联用
          // ignoreElements: ['no-print', 'bc', 'gb'],
          // maxWidth: 800,
          style: '@page {margin:10 0mm} .tabStyle_8 {width: 96% !important;}',
        });
      }

      // -----------------------------------------------------处理过程开始-------------------------------------------------------------
      const handleColumns: BasicColumn[] = [
        {
          title: '处理人',
          dataIndex: 'realName',
          align: 'center',
        },
        {
          title: '转交人',
          dataIndex: 'privRealName',
          align: 'center',
        },
        // {
        //   title: '代理人',
        //   dataIndex: 'isProxy',
        //   align: 'center',
        // },
        {
          title: '任务',
          dataIndex: 'actionTitle',
          align: 'center',
        },
        // {
        //   title: '到达状态',
        //   dataIndex: 'reachState',
        //   align: 'center',
        //   customRender: ({ record }) => {
        //     if (record.isDateDelayed) {
        //       if (record.isReason) {
        //         return (
        //           record.delay +
        //           '-' +
        //           record.receiveDate +
        //           '-' +
        //           record.statusName +
        //           '-' +
        //           record.reason
        //         );
        //       }
        //       return record.delay + '-' + record.receiveDate + '-' + record.statusName;
        //     }
        //   },
        // },
        {
          title: '签收时间',
          dataIndex: 'readDate',
          align: 'center',
        },
        {
          title: '处理时间',
          dataIndex: 'checkDate',
          align: 'center',
        },
        {
          title: '处理者',
          dataIndex: 'processor',
          align: 'center',
        },
        {
          title: '处理状态',
          dataIndex: 'checkStatusName',
          align: 'center',
        },
        {
          title: t('flow.leaveword'),
          dataIndex: 'result',
          align: 'center',
          ifShow: () => flowIsRemarkShow.value,
        },
      ];
      const [registerHandleTable, { setTableData }] = useTable({
        title: '处理过程',
        api: '',
        columns: handleColumns,
        formConfig: {},
        searchInfo: {}, //额外的参数
        useSearchForm: false,
        showTableSetting: false,
        bordered: true,
        showIndexColumn: true,
        indexColumnProps: { width: 50 },
        immediate: false,
        pagination: false,
        canResize: false,
      });

      onMounted(() => {
        let newWindow = window as any;
        newWindow.ajaxGetJS = ajaxGetJS;
        newWindow.filterJS = filterJS;
      });
      return {
        registerDrawer,
        content,
        getTitle,
        closeCurrentDrawer,
        flowProcessData,
        registerHandleTable,
        flowIsRemarkShow,
        printForm,
        printFormBox,
      };
    },
  });
</script>
<style scoped>
  @import '../../../assets/css/css.css';
  ::v-deep .ant-tabs-top > .ant-tabs-nav,
  .ant-tabs-bottom > .ant-tabs-nav,
  .ant-tabs-top > div > .ant-tabs-nav,
  .ant-tabs-bottom > div > .ant-tabs-nav {
    margin: 0 !important;
  }
</style>
