<template>
  <div class="bg-white">
    <Spin :spinning="isSpinning">
      <Tabs v-model:activeKey="activeKey" centered @change="getActiveKey" v-if="isTab">
        <TabPane key="1" tab="流转过程" force-render />
        <TabPane key="2" tab="流程图" force-render v-if="flowProcessData.isFlowChartShow" />
        <TabPane key="3" tab="修改标题" force-render v-if="flowProcessData.canModifyTitle" />
      </Tabs>
      <Row v-show="activeKey == '1'">
        <div class="flex justify-end w-full mt-2">
          <Dropdown>
            <Button type="primary" size="small" class="ml-2"
              ><template #icon>
                <PrinterOutlined />
              </template>
              打印
            </Button>
            <template #overlay>
              <Menu @click="onClickMenu">
                <MenuItem key="form"> 打印表单 </MenuItem>
                <MenuItem key="all"> 打印全部 </MenuItem>
                <MenuItem v-for="(item, index) in flowProcessData.aryView" :key="item.id">
                  {{ item.name }}
                </MenuItem>
              </Menu>
            </template>
          </Dropdown>
          <Button
            type="primary"
            size="small"
            class="ml-2"
            @click="handleBut"
            v-if="currentFlows.length > 0"
          >
            <template #icon>
              <SaveOutlined />
            </template>
            处理
          </Button>
          <Popconfirm
            placement="top"
            title="确定撤回吗？"
            ok-text="确定"
            cancel-text="取消"
            v-if="isRecallBtnShow"
            @confirm="handleRecall"
          >
            <Button type="primary" size="small" class="ml-2">
              <template #icon>
                <IssuesCloseOutlined />
              </template>
              撤回
            </Button>
          </Popconfirm>
          <Popconfirm
            placement="top"
            title="确定放弃吗？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="discardFlow"
          >
            <Button type="primary" size="small" class="ml-2" v-if="isDiscardBtnShow">
              <template #icon>
                <ClearOutlined />
              </template>
              放弃
            </Button>
          </Popconfirm>
        </div>
        <div class="border-1 border-solid border-gray-80 min-h-full w-full" id="flowFormBox">
          <div class="flex justify-center w-full text-xl font-bold mb-1"> {{ flowTitle }} </div>
          <div class="flex justify-center w-full mb-2"
            >流程号：{{ flowId }} &nbsp;&nbsp;发起人：{{ starter }} &nbsp;&nbsp;状态：{{
              flowStatusDesc
            }}
          </div>
          <form
            id="flowForm"
            :name="getFormName"
            :formCode="formCode"
            :style="[{ overflowX: isTab ? 'auto' : 'hidden' }]"
          >
            <input type="hidden" id="op" name="op" value="saveformvalue" />
            <input type="hidden" name="isAfterSaveformvalueBeforeXorCondSelect" />
            <div class="p-2" v-html="content"></div>
          </form>

          <div>
            <BasicTable @register="registerHandleTable">
              <template #action="{ record, index }">
                <TableAction
                  :actions="[
                    {
                      icon: 'clarity:note-edit-line',
                      tooltip: '处理',
                      ifShow: record.canHandle && record.isHandleBtnShow,
                      popConfirm: {
                        title: '是否处理',
                        confirm: handleEdit.bind(null, record, index),
                      },
                    },
                    {
                      icon: 'clarity:email-outline-badged',
                      ifShow: record.isRemindBtnShow,
                      tooltip: '催办',
                      popConfirm: {
                        title: '是否催办',
                        confirm: handleRemind.bind(null, record, index),
                      },
                    },
                  ]"
                />
              </template>
            </BasicTable>
          </div>
        </div>

        <BasicTable @register="registerTable" v-show="isAttachmentShow" style="width: 100%">
          <template #attTitle="{ record, index }">
            <div v-if="record.previewUrl && record.previewUrl.length > 0"
              ><a :href="record.previewUrl" target="_blank">{{ record.name }}</a></div
            >
            <div v-else>{{ record.name }}</div>
          </template>
          <template #action="{ record, index }">
            <TableAction
              :actions="[
                {
                  icon: 'ion:download-outline',
                  tooltip: '下载',
                  onClick: handleDownload.bind(null, record),
                  loading: isDownloadAtt,
                },
                {
                  icon: 'ant-design:delete-outlined',
                  color: 'error',
                  popConfirm: {
                    title: '是否确认删除',
                    confirm: handleDelete.bind(null, record, index),
                  },
                },
              ]"
            />
          </template>
        </BasicTable>

        <!-- 回复 -->
        <div class="w-full mt-2 mb-2">
          <ReplyView
            v-model:flowInfo="flowProcessData"
            v-if="flowProcessData.isReply && flowProcessData.isFlowStarted"
          />
        </div>
      </Row>
      <Row v-show="activeKey == '2'">
        <FlowChart :flowId="flowProcessData.flowId" :activeKey="activeKey" />
      </Row>
      <Row v-show="activeKey == '3'">
        <UpdateTitle
          :flowId="flowProcessData.flowId"
          :activeKey="activeKey"
          @success="getFlowProcessContent"
        />
      </Row>
      <ProcessViewDrawer
        @register="registerProcessViewDrawer"
        @success="handleProcessViewCallBack"
        @close="initWindowFunc"
      />
      <SelectUser @register="registerModal" @success="handleCallBack" />

      <!-- 循环引用，因为SmartModuleDrawer中引用了ProcessShowDrawer -->
      <SmartModuleDrawer @register="registerSmartModuleDrawer" @close="initWindowFunc" />
      <!-- <SmartModuleShowDrawer @register="registerSmartModuleDrawer" @close="initWindowFunc" /> -->

      <LocationMarkModal
        @register="registerLocationMarkModal"
        @success="handleLocationMarkCallBack"
      />
    </Spin>
  </div>
</template>
<script lang="ts">
  import {
    defineComponent,
    ref,
    inject,
    watch,
    onMounted,
    onUnmounted,
    defineAsyncComponent,
    computed,
    nextTick,
    unref,
  } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { downloadByData } from '/@/utils/file/download';
  // import SmartModuleShowDrawer from '../smartModule/modules/SmartModuleShowDrawer.vue';
  // import SmartModuleDrawer from '../smartModule/modules/SmartModuleDrawer.vue';
  import ReplyView from './modules/ReplyView.vue';
  import { getShowImg } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { useDrawer } from '/@/components/Drawer';
  import { Spin } from 'ant-design-vue';
  import LocationMarkModal from './modules/LocationMarkModal.vue';
  import { getVisualDownload } from '/@/api/module/module';
  import { useGo } from '/@/hooks/web/usePage';
  import { getObjectByKey } from '/@/utils/uploadFile';
  import printJS from 'print-js';
  import {
    getListAttachment,
    getListAttachmentDelAtt,
    getFlowShow,
    getFlowShowScript,
    getDownload,
    getRemind,
    getRecall,
    downloadFile,
    getDiscardFlow,
  } from '/@/api/process/process';
  import {
    SaveOutlined,
    PrinterOutlined,
    IssuesCloseOutlined,
    ClearOutlined,
  } from '@ant-design/icons-vue';
  import {
    Button,
    Row,
    UploadProps,
    Tabs,
    TabPane,
    Dropdown,
    Menu,
    MenuItem,
    MenuProps,
    Popconfirm,
  } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { SelectUser } from '/@/components/CustomComp';
  import { useModal } from '/@/components/Modal';
  import FlowChart from './flowChart.vue';
  import UpdateTitle from './updateTitle.vue';
  import { BasicColumn } from '/@/components/Table';
  import { removeScript, removeLink, filterJS, ajaxGetJS, loadImg } from '/@/utils/utils';
  import ProcessViewDrawer from './processViewDrawer.vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { useUserStore } from '/@/store/modules/user';

  // const SmartModuleDrawer = defineAsyncComponent(
  //   () => import('../smartModule/modules/SmartModuleDrawer.vue'),
  // );

  export default defineComponent({
    name: 'ProcessShowView',
    components: {
      Button,
      SaveOutlined,
      Row,
      BasicTable,
      TableAction,
      SelectUser,
      Tabs,
      TabPane,
      FlowChart,
      UpdateTitle,
      Dropdown,
      Menu,
      MenuItem,
      ProcessViewDrawer,
      PrinterOutlined,
      // SmartModuleShowDrawer,
      // SmartModuleDrawer,
      SmartModuleDrawer: defineAsyncComponent(
        () => import('../smartModule/modules/SmartModuleDrawer.vue'),
      ),
      // SmartModuleDrawer: ()=> import('../smartModule/modules/SmartModuleDrawer.vue'),
      IssuesCloseOutlined,
      Popconfirm,
      ReplyView,
      ClearOutlined,
      Spin,
      LocationMarkModal,
    },
    props: {
      flowId: {
        type: String,
        default: '-1',
      },
      visitKey: {
        type: String,
        default: '',
      },
      isTab: {
        type: Boolean,
        default: true,
      },
    },
    emits: ['success', 'register', 'handleCurrent'],
    setup(props, { emit }) {
      const isSpinning = ref(false);
      const { createMessage, createConfirm } = useMessage();
      const content = ref('');
      const common = ref('');
      const getTitle = ref('查看流程');
      const flowTitle = ref('');
      const containerRef = ref();
      const toActions = ref([]);
      const matchJson = ref({});
      const selectDeptCode = ref('');
      const { t } = useI18n();
      const activeKey = ref('1');
      const currentFlows = ref([]);
      const flowId = ref('-1');
      const flowStatusDesc = ref('');
      const starter = ref('');
      const visitKey = ref('');
      const isTab = ref(true);
      // let srcNum = ref(1);
      const fileList = ref<UploadProps['fileList']>([]);
      const flowIsRemarkShow = ref(true);
      const aryAtt = ref([]);
      const isDownloadAtt = ref(false);
      const formCode = ref('');
      const isRecallBtnShow = ref(false);
      const recallMyActionId = ref(-1);
      const isHasAttachment = ref(true);
      const isDiscardBtnShow = ref(false);
      const isAttachmentShow = ref(false);
      const type = ref(2); // 流程类型，2为固定流程
      const route = useRoute();
      const curFormUtil: any = inject('curFormUtil');
      const getFormName = computed(() => 'flowFormShow' + curFormUtil?.getFormNo());
      const { setTitle, closeCurrent } = useTabs();
      const { currentRoute } = useRouter();
      const userStore = useUserStore();

      let isFromProcess = false;

      // -----------------------------------------------------获取当前流程信息开始-------------------------------------------------------------
      //获取当前流程信息
      let flowProcessData = ref<any>({});
      async function getFlowProcessContent() {
        initWindowFunc();

        isSpinning.value = true;
        let data = await getFlowShow({ flowId: flowId.value, visitKey: visitKey.value });
        isSpinning.value = false;

        flowProcessData.value = data;
        flowTitle.value = data.flowTitle;
        isHasAttachment.value = data.isHasAttachment;
        flowStatusDesc.value = data.flowStatusDesc;
        starter.value = data.starter;
        formCode.value = data.formCode;
        isDiscardBtnShow.value = data.isDiscardBtnShow;
        // // common.value = data.script;
        // selectDeptCode.value = '';
        type.value = data.type;

        // 页面风格
        let cont = data.content;
        let isPageStyleLight = data.isPageStyleLight;
        if (isPageStyleLight) {
          cont = cont.replace('tabStyle_8', 'tabStyle_1');
        }
        content.value = cont;

        flowIsRemarkShow.value = data.flowIsRemarkShow;
        aryAtt.value = data.aryAtt;

        // 异步获取显示规则脚本
        getFlowShowScript({
          flowId: flowId.value,
          cwsFormName: getFormName.value,
        }).then((res) => {
          console.log('getFlowShowScript filterJS');
          filterJS(res.script, '-srcShow', o(getFormName.value));
        });

        await filterJS(data.content, '-srcShow', o(getFormName.value));

        if (flowProcessData.value.aryMyAction && flowProcessData.value.aryMyAction.length > 0) {
          setTableData(flowProcessData.value.aryMyAction);
          currentFlows.value = flowProcessData.value.aryMyAction.filter(
            (item) => item.canHandle && item.isHandleBtnShow,
          );

          isRecallBtnShow.value = flowProcessData.value.aryMyAction.some((item) => {
            if (item.isRecallBtnShow) {
              recallMyActionId.value = item.id;
            }
            return item.isRecallBtnShow;
          });
        } else {
          setTableData([]);
        }
        // 需判断表格是否显示，如果不显示直接调用setProps，会报：The table instance has not been obtained yet, please make sure the table is presented when performing the table operation
        if (isHasAttachment.value) {
          setProps({ searchInfo: { flowId: flowProcessData.value.flowId } });
          reloadAttachment();
        }

        let rootPath = import.meta.env.VITE_PUBLIC_PATH;
        if (rootPath.endsWith('/')) {
          rootPath = rootPath.substring(0, rootPath.lastIndexOf('/'));
        }
        const url = `${rootPath}/resource/js/form/form_js_${data.formCode}.js?pageType=flowShow&flowId=${flowId.value}&cwsFormName=${getFormName.value}`;
        console.log('form_js url', url);
        let script1 = document.createElement('script');
        script1.type = 'text/javascript';
        script1.src = url;
        script1.id = `${100}-srcShow`;
        document.getElementsByTagName('head')[0].appendChild(script1);

        // 加载后台事件中配置的前台脚本
        if (data.formJs && data.formJs.length > 0) {
          let scriptFormJs = document.createElement('script');
          scriptFormJs.type = 'text/javascript';
          scriptFormJs.text = data.formJs;
          scriptFormJs.id = `${101}-srcShow`;
          document.getElementsByTagName('head')[0].appendChild(scriptFormJs);
        }

        loadImg('flowForm');

        // 为向下兼容，引入服务端form_js
        await ajaxGetJS(
          `/flow/form_js/form_js_${data.formCode}.jsp?pageType=flowShow&flowId=${flowId.value}&cwsFormName=${getFormName.value}`,
        );
      }
      // -----------------------------------------------------获取当前流程信息结束-------------------------------------------------------------

      // ----------------------------------------------------视图查看开始-------------------------------------
      const [registerProcessViewDrawer, { openDrawer: openProcessViewDrawer }] = useDrawer();
      function openProcessViewDrawerForPrint(flowId, viewId) {
        let params = {
          flowId,
          viewId,
        };
        openProcessViewDrawer(true, params);
      }

      function handleProcessViewCallBack() {}
      // ----------------------------------------------------视图查看结束

      const onClickMenu: MenuProps['onClick'] = ({ key }) => {
        console.log(`Click on item ${key}`);
        if (key === 'form') {
          htmlPrint('flowForm');
        } else if (key === 'all') {
          htmlPrint('flowFormBox');
        } else {
          openProcessViewDrawerForPrint(flowId.value, key);
        }
      };

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

      // -----------------------------------------------------选择人员开始-------------------------------------------------------------
      // 是否在表单中选择用户标志位
      let isSelUserInForm = false;
      let currentIndex = ref(0);
      let currentActionId = ref(0);
      let currentIsBtnXor = false;
      function selectUser(record, index, isBtnXor) {
        isSelUserInForm = false;
        currentActionId.value = record.id;
        currentIndex.value = index;
        currentIsBtnXor = isBtnXor;
        openModal(true, {
          isUpdate: false,
        });
      }

      //人员组件选择后回调
      function handleCallBack(data) {
        if (isSelUserInForm) {
          let realNames = data.map((item) => item.realName).join();
          let names = data.map((item) => item.name).join();
          if (o(curObjName)) {
            o(curObjName).value = names;
            o(curObjName + '_realshow').value = realNames;
          } else {
            console.warn(curObjName + '不存在');
          }
        } else {
          let datas = data;
          datas.forEach((item) => {
            item.value = item.name;
            item.checked = true;
            item.id = currentActionId.value;
            item.type = 'checkbox';
            item.disabled = false;
            item.clickXor = true;
          });

          if (currentIsBtnXor) {
            toActions.value[currentIndex.value].xorChecked = true;
          }
          let newDats = [];
          let values = toActions.value[currentIndex.value].checkers.map((item) => item.value);
          datas.forEach((item) => {
            if (!values.includes(item.value)) {
              newDats.push(item);
            }
          });

          toActions.value[currentIndex.value].checkers = [
            ...toActions.value[currentIndex.value].checkers,
            ...newDats,
          ];
        }
      }
      // -----------------------------------------------------选择人员结束-------------------------------------------------------------

      // -----------------------------------------------------文件列表开始-------------------------------------------------------------
      const columns: BasicColumn[] = [
        {
          title: '标题',
          dataIndex: 'name',
          align: 'left',
          slots: { customRender: 'attTitle' },
        },
        {
          title: '创建时间',
          dataIndex: 'createDate',
        },
      ];

      const fileDataSource = ref<Recordable>([]);
      const [registerTable, { reload: reloadAttachment, setProps }] = useTable({
        title: '附件列表',
        api: getListAttachment,
        columns,
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
        // beforeFetch: () => {
        //   const params = {
        //     flowId: flowProcessData.value.flowId,
        //   };
        //   return params;
        // },
        afterFetch: (data) => {
          getFileDataSource(data);
        },
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      const getFileDataSource = (data) => {
        fileDataSource.value = data || [];
        console.log('fileDataSource', data);
        if (fileDataSource.value.length == 0) {
          isAttachmentShow.value = false;
        } else {
          isAttachmentShow.value = true;
        }
      };

      //文件列表删除
      function handleDelete(record) {
        let params = {
          flowId: flowProcessData.value.flowId,
          attId: record.id,
        };
        getListAttachmentDelAtt(params).then(() => {
          reloadAttachment();
        });
      }
      // -----------------------------------------------------文件列表结束-------------------------------------------------------------

      const serverInfo = userStore.getServerInfo;

      function handleDownload(record: any) {
        if (serverInfo.isObjStoreEnabled) {
          getObjectByKey(record.visualPath + '/' + record.diskName);
        } else {
          isDownloadAtt.value = true;
          const params = {
            flowId: flowProcessData.value.flowId,
            attachId: record.id,
          };
          getDownload(params).then((data) => {
            isDownloadAtt.value = false;
            if (data) {
              downloadByData(data, `${record.name}`);
            }
          });
        }
      }

      // ----------------------------------------------------嵌套表中查看开始-------------------------------------
      const [registerSmartModuleDrawer, { openDrawer: openSmartModuleDrawer }] = useDrawer();
      function openSmartModuleDrawerForShow(moduleCode, id, visitKey, params) {
        console.log('openSmartModuleDrawerForShow params', params);
        if (unref(isTab.value)) {
          go({
            path: '/smartModuleAddEditView',
            query: {
              isTab: true,
              isUpdate: 3,
              parentPath: unref(currentRoute).path,
              moduleCode: moduleCode,
              id: id,
              visitKey: visitKey,
              titleName: `详情`,
              cacheName: `smartModuleAddEditViewDetail${id}`,
              ...params,
            },
          });
        } else {
          openSmartModuleDrawer(true, {
            isUpdate: 3,
            record: {
              moduleCode,
              id,
              visitKey,
              ...params,
            },
          });
        }
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
          title: '到达时间',
          dataIndex: 'receiveDate',
          align: 'center',
        },
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
          dataIndex: 'checker',
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
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: 'right',
        },
      });

      const go = useGo();
      function handleEdit(record) {
        console.log('handleEdit record', record);
        console.log('handleEdit isFromProcess', isFromProcess);
        if (isFromProcess) {
          if (isTab.value) {
            closeCurrent();
          }
          let myActionId = record.id;
          processFlow(myActionId);
        } else {
          emit('handleCurrent', record);
        }
      }

      function processFlow(myActionId) {
        let title = flowProcessData.value.flowId + '-' + flowProcessData.value.flowTitle;
        if (title.length > 18) {
          title = title.substring(0, 18) + '...';
        }
        if (type.value == 2) {
          go({
            path: '/processHandle',
            query: {
              myActionId: myActionId,
              isFromShowView: true,
              title: title,
              cacheName: `processHandle${flowProcessData.value.flowId + '-' + myActionId}`,
            },
          });
        } else {
          go({
            path: '/processHandleFree',
            query: {
              myActionId: myActionId,
              isFromShowView: true,
              title: title,
              // cacheName: `processHandle${flowProcessData.value.flowId}`,
              cacheName: `processHandle${flowProcessData.value.flowId + '-' + myActionId}`,
            },
          });
        }
      }

      async function handleRemind(record) {
        let data = await getRemind({ myActionId: record.id });
        console.log('handleRemind data', data);
        if (data.res === 0) {
          createMessage.success('催办成功');
        }
      }

      // 处理按钮
      function handleBut() {
        if (currentFlows.value.length > 0) {
          handleEdit(currentFlows.value[0]);
        }
      }

      async function handleRecall() {
        let data = await getRecall({ myActionId: recallMyActionId.value });
        if (data.res === 0) {
          createMessage.success(t('common.opSuccess'));
          content.value = '';
          removeScript('-srcShow');
          removeLink();
          if (isTab.value) {
            closeCurrent();
            processFlow(recallMyActionId.value);
          } else {
            await getFlowProcessContent();
          }
        }
      }
      // -----------------------------------------------------处理过程结束-------------------------------------------------------------
      //切换tab
      function getActiveKey(key: string) {}

      function getPublicPath() {
        const publicPath = import.meta.env.VITE_PUBLIC_PATH || '/';
        return publicPath;
      }

      function getCurFormId() {
        return curFormUtil.get();
      }

      // 映射的文件宏控件下载
      function downloadFileVisual(fileName, params) {
        getVisualDownload(params).then((data) => {
          if (data) {
            downloadByData(data, fileName);
          }
        });
      }

      function getCurFormUtil() {
        return curFormUtil;
      }

      function initWindowFunc() {
        setTimeout(() => {
          curFormUtil?.set(getFormName.value);
        }, 100);

        let newWindow = window as any;
        newWindow.getCurFormId = getCurFormId;
        newWindow.getCurFormUtil = getCurFormUtil;
        newWindow.ajaxGetJS = ajaxGetJS;
        newWindow.filterJS = filterJS;
        newWindow.downloadFile = downloadFile;
        newWindow.openSmartModuleDrawerForShow = openSmartModuleDrawerForShow;
        newWindow.getPublicPath = getPublicPath;
        newWindow.openLocationMarkModal = openLocationMarkModal;
        newWindow.downloadFileVisual = downloadFileVisual; // 映射的文件宏控件下载
      }

      const [registerModal, { openModal }] = useModal();

      // 放弃流程
      async function discardFlow() {
        await getDiscardFlow({
          flowId: flowProcessData.value.flowId,
        }).then(async (res) => {
          if (res.data.res === 0) {
            content.value = '';
            removeScript('-srcShow');
            removeLink();
            await getFlowProcessContent();
          } else {
            createMessage.error(res.data.msg);
          }
        });
      }

      const [registerLocationMarkModal, { openModal: openLMModal }] = useModal();
      const openLocationMarkModal = (record) => {
        console.log('openLocationMarkModal record', record);
        openLMModal(true, {
          ...record,
        });
      };

      async function handleLocationMarkCallBack(item) {
        setInputObjValue(item.point.lng + ',' + item.point.lat + ',' + item.address, item.address);
      }

      onMounted(async () => {
        console.log('route.query', route.query);
        if (route.query && route.query.flowId) {
          flowId.value = route.query['flowId'];
          if (flowId.value != '-1') {
            getFlowProcessContent();
            if (route.query.title) {
              setTitle(route.query.title);
            }
          }

          isFromProcess = route.query.isFromProcess ? route.query.isFromProcess == 'true' : false;
          console.log('route.query isFromProcess', isFromProcess);
        }
      });

      onUnmounted(async () => {
        curFormUtil.close(getFormName.value);
      });

      watch(
        () => props.flowId,
        (newVal) => {
          activeKey.value = '1';
          flowId.value = props.flowId;
          visitKey.value = props.visitKey;
          // 是否显示顶部的选项卡
          if (props.isTab === false) {
            isTab.value = props.isTab;
          } else {
            isTab.value = true;
          }
          if (flowId.value != '-1') {
            getFlowProcessContent();
          }
        },
        // {
        //   immediate: true,
        // },
      );

      return {
        matchJson,
        selectDeptCode,
        content,
        common,
        getTitle,
        fileList,
        registerTable,
        handleDelete,
        containerRef,
        toActions,
        registerModal,
        selectUser,
        handleCallBack,
        registerHandleTable,
        handleEdit,
        handleBut,
        activeKey,
        getActiveKey,
        currentFlows,
        flowProcessData,
        getFlowProcessContent,
        flowIsRemarkShow,
        onClickMenu,
        registerProcessViewDrawer,
        handleProcessViewCallBack,
        flowTitle,
        flowStatusDesc,
        starter,
        aryAtt,
        isDownloadAtt,
        handleDownload,
        handleRemind,
        registerSmartModuleDrawer,
        initWindowFunc,
        formCode,
        isRecallBtnShow,
        handleRecall,
        fileDataSource,
        isHasAttachment,
        isDiscardBtnShow,
        discardFlow,
        isAttachmentShow,
        isSpinning,
        registerLocationMarkModal,
        handleLocationMarkCallBack,
        getFormName,
        flowId,
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
