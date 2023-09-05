<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="85%"
    @ok="handleSubmit"
    :cancelText="'关闭'"
    :destroyOnClose="true"
    @close="onClose"
    :show-ok-btn="false"
  >
    <!-- <Tabs
      v-model:activeKey="activeKey"
      centered
      @change="getActiveKey"
      v-if="dataRef.hasTab && dataRef.isNav"
    >
      <TabPane :tab="item.name" force-render v-for="(item, index) in dataRef.aryTab" :key="index" />
    </Tabs> -->
    <div v-show="activeKey == 0">
      <form id="visualForm" :name="getFormName" :formCode="formCode">
        <Spin :spinning="spinning">
          <!--  eslint-disable-next-line vue/no-v-html -->
          <div v-html="dataRef['rend']"> </div>
          <input id="cwsHelper" name="cwsHelper" value="1" type="hidden" />
        </Spin>
      </form>

      <BasicTable @register="registerTable" v-show="dataRef.isHasAttachment">
        <template #action="{ record, index }">
          <TableAction
            :actions="[
              {
                icon: 'ion:download-outline',
                tooltip: '下载',
                onClick: handleDownload.bind(null, record),
                loading: record.isDownloadAtt ? true : false,
              },
            ]"
          />
        </template>
      </BasicTable>

      <Row align="middle" style="clear: both">
        <div
          v-if="(!dataRef.buttons || dataRef.buttons.length == 0) && !spinning"
          style="
            text-align: center;
            margin-top: 10px;
            display: flex;
            justify-content: center;
            width: 100%;
          "
        >
          <Button id="btnPrintForm" type="primary" size="middle" class="ml-2" @click="printForm">
            打印
          </Button>
        </div>
      </Row>
    </div>
    <!-- 
      注释掉SmartModuleRelateTable
      因为当在smartModuleRelateTableDrawer的表单域宏控件选择时（即嵌套表格编辑时点击表单域选择宏控件的选择按钮时），在smartModuleSelDrawer中引用了SmartModuleTable，而后者引用了SmartModuleShowDrawer，若其中有SmartModuleRelateTable（引用了smartModuleRelateTableDrawer），而smartModuleRelateTableDrawer中也引用了SmartModuleSelDrawer，这样会引起循环引用:
      smartModuleSelDrawer -> SmartModuleTable -> SmartModuleShowDrawer -> SmartModuleRelateTable -> smartModuleRelateTableDrawer -> SmartModuleSelDrawer
      <div v-show="activeKey != 0"> <SmartModuleRelateTable :activeRecord="activeRecord" /> </div> 
    -->
  </BasicDrawer>
</template>
<script lang="ts">
  import {
    defineComponent,
    ref,
    unref,
    onMounted,
    inject,
    nextTick,
    computed,
    onUnmounted,
  } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { Tabs, TabPane, Row, Spin } from 'ant-design-vue';
  import {
    getVisualShowPage,
    getVisualListAtt,
    getVisualDownload,
    getViewJsScript,
  } from '/@/api/module/module';
  import { submitMyFile } from '/@/api/process/process';
  import { getShowImg } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { dateUtil as dayjs } from '/@/utils/dateUtil';
  import { downloadByData } from '/@/utils/file/download';
  import { useModal } from '/@/components/Modal';
  import printJS from 'print-js';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { Button } from 'ant-design-vue';
  import {
    removeScript,
    clearTimeoutAll,
    filterJS,
    ajaxPost,
    ajaxGet,
    ajaxGetJS,
    loadImg,
  } from '/@/utils/utils';

  // import SmartModuleRelateTable from './smartModuleRelateTable.vue';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModuleShowDrawer',
    components: {
      BasicDrawer,
      Tabs,
      TabPane,
      // SmartModuleRelateTable,
      BasicTable,
      TableAction,
      Button,
      Row,
      Spin,
    },
    emits: ['success', 'register', 'close'],
    setup(_, { emit }) {
      let dataRef = ref<any>({});
      const activeKey = ref(0);
      const activeRecord = ref({});
      const dataRecord = ref<any>({});
      const srcId = ref('-src');
      const formCode = ref('');
      const spinning = ref(false);
      const curFormUtil: any = inject('curFormUtil');

      const [registerDrawer, { setDrawerProps, closeDrawer, getVisible }] = useDrawerInner(
        async (data) => {
          setDrawerProps({ confirmLoading: false });

          removeScript(unref(srcId));
          activeKey.value = 0;
          dataRecord.value = data.record || {};
          dataRef.value = {};
          setTimeout(() => {
            getFirstTabInfo();
          }, 10);
        },
      );

      async function getFirstTabInfo() {
        var liveOp = new LiveValidation('cwsHelper');
        LiveValidation.destroyValidate(liveOp.formObj.fields);
        $('.LV_presence').remove();

        spinning.value = true;

        initWindowFunc();

        const res = await getVisualShowPage({
          moduleCode: unref(dataRecord).moduleCode,
          id: unref(dataRecord).id,
          visitKey: unref(dataRecord).visitKey,
        });

        spinning.value = false;

        dataRef.value = res;
        formCode.value = res.formCode;

        console.log('smartModuleShowDrawer getVisible', unref(getVisible));
        await nextTick();
        if (!unref(getVisible)) {
          console.log('smartModuleShowDrawer getVisible is false, now return.');
          return;
        }

        // 异步获取显示规则脚本
        getViewJsScript({
          moduleCode: unref(dataRecord).moduleCode,
          pageType: 'show',
          cwsFormName: getFormName.value,
        }).then((res) => {
          console.log('getViewJsScript filterJS');
          filterJS(res.script, '-src', o(getFormName.value), () => {
            return unref(getVisible);
          });
        });

        await filterJS(dataRef.value['rend'], '-src', o(getFormName.value), () => {
          return unref(getVisible);
        });

        setTimeout(() => {
          // 初始化日期控件
          initDatePicker();
          // 因为有可能在打开抽屉后即刻关闭，此时visualForm已被销毁，此处可能应改为用nextTick
          if (!o(getFormName.value)) {
            return;
          }
          // 替换按钮的样式
          var btns = o(getFormName.value).getElementsByTagName('button');
          for (var i = 0; i < btns.length; i++) {
            btns[i].className = 'ant-btn ant-btn-primary ant-btn-sm';
          }
          loadImg('visualForm');
        }, 100);

        // isHasAttachment 是否上传文件
        if (unref(dataRef).isHasAttachment && unref(dataRef).id) {
          setTimeout(() => {
            setProps({
              searchInfo: {
                moduleCode: unref(dataRef).moduleCode,
                id: unref(dataRef).id,
                isShowPage: true,
                visitKey: unref(dataRecord).visitKey,
                flowId: unref(dataRecord).flowId,
              },
            });
            reloadAttachment();
          }, 10);
        }
      }

      const getTitle = '查看';

      function getCurFormId() {
        return curFormUtil.get();
      }

      const getFormName = computed(() => 'visualFormShow' + curFormUtil?.getFormNo());

      function initWindowFunc() {
        setTimeout(() => {
          curFormUtil?.set(getFormName.value);
        }, 100);

        let newWindow = window as any;
        newWindow.getCurFormId = getCurFormId;
        newWindow.ajaxPost = ajaxPost;
        newWindow.ajaxGet = ajaxGet;
        newWindow.submitMyFile = submitMyFile;
        newWindow.ajaxGetJS = ajaxGetJS;
        newWindow.filterJS = filterJS;
      }

      async function handleSubmit() {
        try {
          setDrawerProps({ confirmLoading: true });
          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      //切换tab
      function getActiveKey(key: number) {
        activeKey.value = key;
        activeRecord.value = {};
        if (key != 0) {
          let record = dataRef.value.aryTab[key];
          activeRecord.value = record;
          // clearTimeoutAll();
        } else {
          // setTimeout(() => {
          //   getFirstTabInfo();
          // }, 10);
        }
      }

      function onClose() {
        o(getFormName.value).setAttribute('isclosed', 'true');
        removeScript(unref(srcId));
        curFormUtil.close(getFormName.value);
        emit('close');
      }

      // -----------------------------------------------------文件列表开始-------------------------------------------------------------
      const columns: BasicColumn[] = [
        {
          title: '标题',
          dataIndex: 'name',
          align: 'left',
        },
        {
          title: '创建者',
          dataIndex: 'creatorRealName',
        },
        {
          title: '创建时间',
          dataIndex: 'createDate',
          customRender: ({ text }) => {
            return dayjs(text).format('YYYY-MM-DD');
          },
        },
        {
          title: '大小(M)',
          dataIndex: 'fileSizeMb',
        },
      ];
      const [registerTable, { reload: reloadAttachment, setProps }] = useTable({
        title: '', // '附件列表',
        api: getVisualListAtt,
        columns,
        formConfig: {},
        searchInfo: {}, //额外的参数
        useSearchForm: false,
        showTableSetting: false,
        bordered: true,
        showIndexColumn: true,
        immediate: false,
        pagination: false,
        canResize: false,
        actionColumn: {
          width: 100,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      function handleDownload(record: any) {
        record.isDownloadAtt = true;
        const params = {
          visitKey: record.visitKey,
          attachId: record.id,
        };
        getVisualDownload(params)
          .then((data) => {
            if (data) {
              downloadByData(data, `${record.name}`);
            }
          })
          .finally(() => {
            record.isDownloadAtt = false;
          });
      }

      function printForm() {
        htmlPrint('visualForm');
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

      onMounted(() => {});

      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        dataRef,
        activeKey,
        getActiveKey,
        activeRecord,
        onClose,
        registerTable,
        handleDownload,
        printForm,
        formCode,
        spinning,
        getFormName,
      };
    },
  });
</script>
