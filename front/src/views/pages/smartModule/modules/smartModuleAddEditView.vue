<template>
  <div :class="{ 'pl-2 pr-2 pb-2 bg-white': isTab }" class="w-full">
    <Spin :spinning="confirmLoading">
      <Tabs
        v-model:activeKey="activeKey"
        centered
        @change="getActiveKey"
        v-if="dataRef.hasTab && dataRef.isNav && isUpdate != 1"
      >
        <TabPane
          :tab="item.tabName ? item.tabName : item.name"
          force-render
          v-for="(item, index) in dataRef.aryTab"
          :key="index"
        />
      </Tabs>
      <div id="visualFormBox" v-show="activeKey == 0" style="display: inline-block" class="w-full">
        <Spin :spinning="spinning">
          <form
            id="visualForm"
            :name="getFormName"
            :formCode="formCode"
            :style="[{ overflowX: isTab ? 'auto' : 'hidden' }]"
          >
            <!--  eslint-disable-next-line vue/no-v-html -->
            <div v-html="dataRef['rend']"> </div>
            <div style="page-break-after: always"></div>
            <input id="cwsHelper" name="cwsHelper" value="1" type="hidden" />
            <span id="spanTempCwsIds"></span>
          </form>
        </Spin>
        <Row v-if="dataRef.isHasAttachment && pageType != 'show'">
          <template v-if="!isObjStoreEnabled">
            <Upload :file-list="fileList" :before-upload="beforeUpload" @remove="handleRemove">
              <a-button class="mt-1.5">
                <UploadOutlined />
                上传文件
              </a-button>
            </Upload>
          </template>
          <template v-else>
            <Upload
              multiple
              :file-list="fileList"
              :before-upload="beforeUpload"
              @remove="handleRemove"
              @change="handleFilesChange"
            >
              <a-button class="mt-1.5">
                <UploadOutlined />
                上传文件
              </a-button>
              <template #itemRender="{ file, actions }">
                <template v-if="getAttVisible(file)">
                  <div class="pt-2 w-350px flex justify-between">
                    <span :style="file.status === 'error' ? 'color: red' : ''">{{
                      file.name
                    }}</span>
                    <span>
                      <Tooltip title="预览">
                        <ProfileOutlined
                          class="ml-2"
                          style="color: #0960bd"
                          v-if="getAttUploadFinishedCanPreview(file)"
                          @click="handlePreviewAtt(file)"
                        />
                      </Tooltip>
                      <Tooltip title="查看">
                        <PictureOutlined
                          class="ml-2"
                          style="color: #0960bd"
                          v-if="getAttUploadFinishedIsImg(file)"
                          @click="handleShowImgAtt(file)"
                        />
                      </Tooltip>
                      <Tooltip title="下载">
                        <DownloadOutlined
                          class="ml-2"
                          style="color: #0960bd"
                          v-if="getAttUploadFinished(file)"
                          @click="handleDownloadAtt(file)"
                        />
                      </Tooltip>
                      <Tooltip title="删除">
                        <DeleteOutlined
                          class="ml-2 mr-4"
                          @click="handleRemoveFile(file, actions)"
                          style="color: #ed6f6f"
                        />
                      </Tooltip>
                    </span>
                  </div>
                  <Progress :percent="getProgress(file)" />
                </template>
              </template>
            </Upload>
          </template>
        </Row>
        <BasicTable @register="registerTable" v-show="isAttachmentShow">
          <!-- <template #emptyText></template> -->
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
                  icon: 'ant-design:download-outlined',
                  tooltip: '下载',
                  onClick: handleDownload.bind(null, record),
                  loading: record.isDownloadAtt ? true : false,
                },
                {
                  icon: 'ant-design:delete-outlined',
                  tooltip: '删除',
                  color: 'error',
                  popConfirm: {
                    title: '是否确认删除',
                    confirm: handleDelete.bind(null, record, index),
                  },
                  ifShow: () => pageType != 'show',
                },
              ]"
            />
          </template>
        </BasicTable>
        <Row v-if="pageType == 'show'" align="middle" style="clear: both">
          <div
            v-if="pageType == 'show' && dataRef.buttons && dataRef.buttons.length > 0"
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
            <Button
              v-if="dataRef.btn_edit_display"
              type="primary"
              size="middle"
              class="ml-2"
              @click="handleOpenEditDrawer"
            >
              编辑
            </Button>
          </div>
          <div
            v-else
            style="
              text-align: center;
              margin-top: 10px;
              display: flex;
              justify-content: center;
              width: 100%;
            "
          >
            <template v-for="item in dataRef.buttons" :key="item.id">
              <Button
                v-if="item.id === 'btnPrint'"
                id="btnPrintForm"
                type="primary"
                size="middle"
                class="ml-2"
                @click="printForm"
              >
                {{ item.name }}
              </Button>
              <Button
                v-if="item.id === 'btnEdit'"
                type="primary"
                size="middle"
                class="ml-2"
                @click="handleOpenEditDrawer"
              >
                {{ item.name }}
              </Button>
            </template>
          </div>
        </Row>
        <Row v-if="pageType == 'edit'" align="middle" style="clear: both">
          <div
            style="
              text-align: center;
              margin-top: 10px;
              display: flex;
              justify-content: center;
              width: 100%;
            "
          >
            <template v-for="item in dataRef.buttons" :key="item.id">
              <Button type="primary" size="middle" class="ml-2" @click="handleBtnClick(item)">
                {{ item.name }}
              </Button>
            </template>
          </div>
        </Row>
      </div>
      <div v-show="activeKey != 0"> <SmartModuleRelateTable :activeRecord="activeRecord" /> </div>
      <SelectUser @register="registerModal" @success="handleCallBack" />
      <SmartModuleRelateTableDrawer
        @register="registerSmartModuleRelateTableDrawer"
        @success="smartModuleRelateTableDrawerhandleSuccess"
        @close="initWindowFunc"
      />
      <SmartModuleSelDrawer
        @register="registerSmartModuleSelDrawer"
        @success="handleSmartModuleSelCallBack"
        @clear="clearSmartModuleSel"
        @close="initWindowFunc"
      />
      <SmartModuleShowDrawer @register="registerSmartModuleShowDrawer" @close="initWindowFunc" />
      <WritePadModal @register="registerWritePadModal" @success="handleWritePadCallBack" />
      <LocationMarkModal
        @register="registerLocationMarkModal"
        @success="handleLocationMarkCallBack"
      />
      <SelDeptModal @register="registerSelDeptModal" @success="handleSelDeptCallBack" />
      <ProcessShowDrawer
        @register="registerProcessShowDrawer"
        @success="handleProcessShowCallBack"
      />

      <Modal v-model:visible="importVisible" title="导入" :closable="true">
        <template #footer>
          <Button @click="handleCancel">关闭</Button>
        </template>
        <div style="padding: 20px">
          <Row>
            <Col :span="8">
              <Button type="primary" @click="handleDownloadExcelTempl" :loading="isDownload"
                >下载模板</Button
              >
            </Col>
            <Col :span="8">
              <ImpExcel
                :isFiles="false"
                @raw-file="rawFileFn"
                dateFormat="YYYY-MM-DD"
                :loading="isImport"
              >
                <a-button type="primary" class="mr-1"> 上传文件 </a-button>
              </ImpExcel>
            </Col>
          </Row>
        </div>
      </Modal>
      <ProcessDrawer @register="registerProcessDrawer" @success="handleSuccess" />
      <div class="flex justify-center w-full" v-if="isTab && !spinning">
        <a-button @click="handleBack">关闭</a-button>
        <a-button
          type="primary"
          class="ml-2"
          :loading="confirmLoading"
          v-if="isUpdate == 1 || isUpdate == 2"
          @click="handleSubmit"
          >确定</a-button
        >
      </div>
    </Spin>
  </div>
</template>
<script lang="ts">
  import {
    defineComponent,
    ref,
    computed,
    unref,
    onMounted,
    onUnmounted,
    h,
    inject,
    nextTick,
    defineAsyncComponent,
    onActivated,
    onDeactivated,
  } from 'vue';
  import { BasicDrawer, useDrawerInner, useDrawer } from '/@/components/Drawer';
  import {
    Tabs,
    TabPane,
    Row,
    UploadProps,
    Upload,
    Button,
    Spin,
    Modal,
    Col,
    Progress,
    Tooltip,
  } from 'ant-design-vue';
  import { ImpExcel } from '/@/components/Excel';
  import { getToken } from '/@/utils/auth';
  import {
    getVisualAddPage,
    getVisualAddRelatePage,
    getVisualCreate,
    getVisualCreateRelate,
    getVisualEditPage,
    getViewJsScript,
    getVisualUpdate,
    getVisualShowPage,
    getItemsForListModuleSel,
    getSelBatchForNest,
    getVisualListAtt,
    getVisualDelAttach,
    getVisualDownload,
    getVisualDownloadExcelTemplForNest,
    getVisualImportExcelNest,
  } from '/@/api/module/module';
  import {
    UploadOutlined,
    DeleteOutlined,
    DownloadOutlined,
    PictureOutlined,
    ProfileOutlined,
  } from '@ant-design/icons-vue';
  import { submitMyFile, downloadFile, getFlowInit, getDelAttach } from '/@/api/process/process';
  import { getShowImg } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { useModal } from '/@/components/Modal';
  import { SelectUser } from '/@/components/CustomComp';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { BasicColumn } from '/@/components/Table';
  import { downloadByData } from '/@/utils/file/download';
  import WritePadModal from '../../processManagement/modules/WritePadModal.vue';
  import LocationMarkModal from '../../processManagement/modules/LocationMarkModal.vue';
  import SelDeptModal from '../../processManagement/modules/SelDeptModal.vue';
  import {
    removeScript,
    createCss,
    removeLink,
    clearTimeoutAll,
    filterJS,
    ajaxPost,
    ajaxGet,
    ajaxGetJS,
    myConfirm,
    myMsg,
    ajaxPostJson,
    loadImg,
    initFormCtl,
    o,
    getServerInfo,
    isImage,
  } from '/@/utils/utils';
  import SmartModuleSelDrawer from './smartModuleSelDrawer.vue';
  import SmartModuleRelateTable from './smartModuleRelateTable.vue';
  import SmartModuleRelateTableDrawer from './smartModuleRelateTableDrawer.vue';
  import { dateUtil as dayjs } from '/@/utils/dateUtil';
  import printJS from 'print-js';
  import { useUserStore } from '/@/store/modules/user';
  import SmartModuleShowDrawer from './smartModuleShowDrawer.vue';
  import { useGo } from '/@/hooks/web/usePage';
  import { useRouter } from 'vue-router';
  import { useMultipleTabStore } from '/@/store/modules/multipleTab';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { useI18n } from '/@/hooks/web/useI18n';

  import ProcessShowDrawer from '../../processManagement/processShowDrawer.vue';
  // import ProcessDrawer from '../../processManagement/processDrawer.vue';

  import { useUploadFileStore } from '/@/store/modules/uploadFile';

  // import { useFileDialog } from '/@/hooks/web/useFileModal';
  import {
    selectFiles,
    deleteByField,
    isUploadFinished,
    getObjectByKey,
    uploadFileObjectFunc,
    deleteByUid,
  } from '/@/utils/uploadFile';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModuleAddEditView',
    components: {
      Tabs,
      TabPane,
      SmartModuleRelateTable,
      SelectUser,
      SmartModuleSelDrawer,
      SmartModuleRelateTableDrawer,
      Row,
      BasicTable,
      TableAction,
      UploadOutlined,
      Upload,
      Button,
      SmartModuleShowDrawer,
      WritePadModal,
      LocationMarkModal,
      SelDeptModal,
      ProcessShowDrawer,
      ImpExcel,
      Modal,
      Col,
      Spin,
      ProcessDrawer: defineAsyncComponent(
        () => import('../../processManagement/processDrawer.vue'),
      ),
      DeleteOutlined,
      DownloadOutlined,
      PictureOutlined,
      ProfileOutlined,
      Progress,
      Tooltip,
    },
    props: {
      getVisible: {
        type: Boolean,
        default: true,
      },
    },
    emits: ['success', 'register', 'close', 'editAction', 'launchFlowAction', 'tabChange'],
    setup(props, { emit }) {
      const { createConfirm } = useMessage();
      const { currentRoute, replace, push } = useRouter();
      const isUpdate = ref(1); //1:新增，2编辑，3查看
      let dataRef = ref<any>({});
      const activeKey = ref(0);
      const activeRecord = ref({});
      const dataRecord = ref<any>({});
      const srcId = ref('-src');

      const pageType = ref('add');
      const formCode = ref('');
      const go = useGo();
      const isAttachmentShow = ref(true);
      const curFormUtil: any = inject('curFormUtil');
      const spinning = ref(false);
      let isQuote = false;
      const importVisible = ref(false);
      const isImport = ref(false);
      const isDownload = ref(false);
      //页面loading
      const confirmLoading = ref(false);
      const isTab = ref(false);
      const { t } = useI18n();

      const userStore = useUserStore();
      const serverInfo = userStore.getServerInfo;
      const isObjStoreEnabled = ref(serverInfo.isObjStoreEnabled);

      const [registerSmartModuleShowDrawer, { openDrawer: openSmartModuleShowDrawer }] =
        useDrawer();

      // const [registerDrawer, { setDrawerProps, closeDrawer, getVisible }] = useDrawerInner();

      const { closeCurrent, refreshPage, setTitle } = useTabs();
      const tabStore = useMultipleTabStore();
      let isFirst = false;
      let isDeactivated = false;
      const isDoLoading = ref(false);
      let isViewJsScriptLoaded = false;

      onMounted(() => {
        console.log('当前路由', currentRoute.value);
        console.log('tabStore', tabStore);
        isFirst = true;
        const query = unref(currentRoute).query;
        if (query && query.moduleCode) {
          initData(query);
          if (query.titleName) {
            setTitle(query.titleName);
          }
        }
      });

      onUnmounted(() => {
        console.log('smartModuleAddEditView onUnmounted onClose');
        onClose();
      });

      onActivated(() => {
        isDeactivated = false;
        console.log('process onActivated isDoLoading', isDoLoading.value);
        initWindowFunc();
        console.log('onActivated initWindowFunc');

        console.log('onActivated isFirst', isFirst, 'isDoLoading', isDoLoading.value);

        setTimeout(() => {
          // 如果是第一次进入页面，不能调用 initData
          if (!isFirst) {
            // 如果加载未完成，则继续
            if (isDoLoading.value || !isViewJsScriptLoaded) {
              console.log('process onActivated initData');
              initData(dataRecord.value);
            }
          }

          isFirst = false;
        }, 100);
      });

      onDeactivated(() => {
        isDeactivated = true;
        console.log('smartModuleAddEditView onDeactivated');
        if (isDoLoading.value || !isViewJsScriptLoaded) {
          createMessage.warn('请等待加载完成后，再切换页面');
        }
      });

      //初始化页面
      const initData = async (data) => {
        confirmLoading.value = false;
        removeScript(unref(srcId));
        removeLink();
        activeKey.value = 0;
        dataRecord.value = data || {};
        console.log('initData dataRecord.value', dataRecord.value);
        dataRef.value = {};
        isUpdate.value = data.isUpdate;
        isTab.value = data.isTab ? true : false; //是否是tab页面
        // setDrawerProps({ showOkBtn: unref(isUpdate) != 3 });
        setTimeout(() => {
          getFirstTabInfo();
        }, 10);
      };

      //返回
      const handleBack = () => {
        closeCurrent();
      };

      //查看详情
      function openSmartModuleDrawerForShow(moduleCode, id, visitKey) {
        if (unref(isTab)) {
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
            },
          });
        } else {
          openSmartModuleShowDrawer(true, {
            isUpdate: 3,
            record: {
              moduleCode: moduleCode,
              id: id,
              visitKey: visitKey,
            },
          });
        }
      }

      async function getFirstTabInfo() {
        isDoLoading.value = true;
        isViewJsScriptLoaded = false;

        initWindowFunc();

        var liveOp = new LiveValidation('cwsHelper');
        LiveValidation.destroyValidate(liveOp.formObj.fields);
        $('.LV_presence').remove();

        // 在flow_js.js中通过openWinModuleShow传过来的参数
        let params = dataRecord.value.params ? dataRecord.value.params : {};
        console.log('getFirstTabInfo dataRecord.value', dataRecord.value);
        params['cwsFormName'] = getFormName.value;

        spinning.value = true;
        console.log('getFirstTabInfo params', params, 'spinning', spinning.value);

        isAttachmentShow.value = false;

        try {
          if (unref(isUpdate) == 1) {
            // 流程中嵌套表格添加
            if (dataRecord.value.pageType === 'add_relate') {
              let res = await getVisualAddRelatePage({ ...dataRecord.value, ...params });
              dataRef.value = res;
              formCode.value = dataRef.value.formCode;

              setTimeout(() => {
                // 初始化计算控件
                initCalculator();
              }, 100);
            } else {
              console.log('getFirstTabInfo getVisualAddPage dataRecord', dataRecord.value);
              let res = await getVisualAddPage({ ...dataRecord.value, ...params });
              dataRef.value = res;
              formCode.value = dataRef.value.formCode;

              setTimeout(() => {
                // 初始化计算控件
                initCalculator();
              }, 100);
            }
          } else if (unref(isUpdate) == 2) {
            console.log('getFirstTabInfo dataRecord', dataRecord.value);
            // 流程中嵌套表格编辑
            if (dataRecord.value.pageType === 'edit_relate') {
              let res = await getVisualEditPage({
                moduleCode: unref(dataRecord).moduleCode,
                id: unref(dataRecord).id,
                flowId: unref(dataRecord).flowId,
                actionId: unref(dataRecord).actionId,
                pageType: unref(dataRecord).pageType,
                ...params,
              });
              dataRef.value = res;
              isQuote = res.cwsQuoteId != 0;
              formCode.value = dataRef.value.formCode;
              isAttachmentShow.value = dataRef.value.isHasAttachment;

              console.log('初始化计算控件');
              setTimeout(() => {
                // 初始化计算控件
                initCalculator();
              }, 100);
            } else {
              let res = await getVisualEditPage({
                moduleCode: unref(dataRecord).moduleCode,
                id: unref(dataRecord).id,
                isTreeView: unref(dataRecord).isTreeView, // 是否树形视图
                treeNodeCode: unref(dataRecord).treeNodeCode, // 树形视图中所选中的节点
                pageType: 'edit',
                ...params,
              });
              dataRef.value = res;
              formCode.value = dataRef.value.formCode;

              console.log('初始化计算控件');
              setTimeout(() => {
                // 初始化计算控件
                initCalculator();
              }, 100);
            }
          } else {
            let res = await getVisualShowPage({
              moduleCode: unref(dataRecord).moduleCode,
              id: unref(dataRecord).id,
              visitKey: unref(dataRecord).visitKey,
              isTreeView: unref(dataRecord).isTreeView, // 是否树形视图
              treeNodeCode: unref(dataRecord).treeNodeCode, // 树形视图中所选中的节点
              ...params,
            });
            dataRef.value = res;
          }

          if (dataRef.value.pageType) {
            pageType.value = dataRef.value.pageType;
          } else {
            pageType.value = dataRecord.value.pageType ? dataRecord.value.pageType : 'show';
          }

          loadAtt();

          // 获取显示规则脚本
          getViewJsScript({
            moduleCode: unref(dataRecord).moduleCode,
            pageType: pageType.value,
            cwsFormName: getFormName.value,
            id: unref(dataRecord).id,
          }).then((res) => {
            console.log('getViewJsScript filterJS');
            filterJS(res.script, '-src', o(getFormName.value), () => {
              return unref(props.getVisible);
            });
            isViewJsScriptLoaded = true;
          });

          // 动态加载css
          if (dataRef.value.pageCss) {
            console.log('pageCss', dataRef.value.pageCss);
            createCss(dataRef.value.pageCss);
          }

          // 页面风格
          let cont = dataRef.value['rend'];
          let isPageStyleLight = dataRef.value.isPageStyleLight;
          if (isPageStyleLight) {
            cont = cont.replace('tabStyle_8', 'tabStyle_1');
          }
          dataRef.value['rend'] = cont;
          console.log('props', props);
          console.log('smartModuleDrawer getVisible tab', unref(props.getVisible));

          await nextTick();

          if (!unref(props.getVisible)) {
            console.log('smartModuleAddEditView getVisible tab is false, now return.');
            return;
          }

          if (isDeactivated) {
            console.log('processHandleView isDeactivated', isDeactivated);
            return;
          }

          if (unref(isUpdate) == 1 || unref(isUpdate) == 2) {
            // 设置控件的只读状态
            setNotReadOnly();

            initFormCtl(getFormName.value);
          }

          spinning.value = false;

          await filterJS(dataRef.value['rend'], '-src', o(getFormName.value), () => {
            return unref(props.getVisible);
          });

          console.log('pageType', pageType.value);
          console.log('dataReocrd', dataRecord.value);
          console.log('dataRef', dataRef.value);
          let thisFormCode = dataRef.value.formCode;
          let thisModuleCode = dataRef.value.moduleCode;
          // 只判断add_relate的情况，因为edit_relate的时候请求的还是editPage接口
          if (dataRecord.value.pageType && dataRecord.value.pageType === 'add_relate') {
            thisFormCode = dataRef.value.formCodeRelated;
            thisModuleCode = dataRef.value.moduleCodeRelated;
          }

          formCode.value = thisFormCode;

          let rootPath = import.meta.env.VITE_PUBLIC_PATH;
          if (rootPath.endsWith('/')) {
            rootPath = rootPath.substring(0, rootPath.lastIndexOf('/'));
          }

          console.log('dataRef', dataRef.value);

          const url = `${rootPath}/resource/js/form/form_js_${thisFormCode}.js?pageType=${
            pageType.value
          }&cwsFormName=${getFormName.value}&moduleCode=${thisModuleCode}&id=${
            dataRecord.value.id ? dataRecord.value.id : ''
          }`;
          console.log('form_js url', pageType.value + ' ' + url);
          let script1 = document.createElement('script');
          script1.type = 'text/javascript';
          script1.src = url;
          script1.id = `${100}${srcId.value}`;
          document.getElementsByTagName('head')[0].appendChild(script1);

          // 为向下兼容，引入服务端form_js
          try {
            await ajaxGetJS(
              `/flow/form_js/form_js_${thisFormCode}.jsp?pageType=${pageType.value}&cwsFormName=${
                getFormName.value
              }&moduleCode=${thisModuleCode}&id=${dataRecord.value.id ? dataRecord.value.id : ''}`,
            );
          } catch (e) {
            console.log(e);
          }

          // 加载后台事件中配置的前台脚本
          if (dataRef.value.formJs && dataRef.value.formJs.length > 0) {
            let scriptFormJs = document.createElement('script');
            scriptFormJs.type = 'text/javascript';
            scriptFormJs.text = dataRef.value.formJs;
            scriptFormJs.id = `${101}${srcId.value}`;
            document.getElementsByTagName('head')[0].appendChild(scriptFormJs);
          }

          loadImg(getFormName.value);

          if (dataRef.value.canUserData) {
            $('#visualForm input, #visualForm select, #visualForm textarea').each(function () {
              $(this).removeAttr('readonly');
              $(this).attr('onchange', '');
            });
          }

          // 初始化日期控件
          initDatePicker();

          // 如果在加载到此处，找不着表单了，说明被缓存了，那么因为加载不完整，再次进入时需重新加
          // 通过 isDeactivated 应该也能判断
          if (o(getFormName.value)) {
            isDoLoading.value = false;
          } else {
            console.error('Form: ' + getFormName.value + ' is not exists.');
          }
        } finally {
          spinning.value = false;
        }
      }

      function loadAtt() {
        // isHasAttachment 是否上传文件
        // 因为有可能表单中不带有附件，但是带有可视化上传控件（上传以后会reloadAttachment)，所以searchInfo中参数仍需初始化
        // setTimeout(() => {
        console.log('loadAtt dataRecord', dataRecord.value);
        setProps({
          api: getVisualListAtt,
          searchInfo: {
            moduleCode: unref(dataRef).moduleCode,
            id: unref(dataRef).id,
            isShowPage: isUpdate.value != 1 && isUpdate.value != 2,
            visitKey: unref(dataRecord).visitKey,
            flowId: unref(dataRecord).flowId,
            pageType: pageType.value,
          },
        });

        if (unref(dataRef).isHasAttachment && unref(dataRef).id) {
          reloadAttachment();
        }
        // }, 10);
      }

      // 设置控件的只读状态
      function setNotReadOnly() {
        let obj = o(getFormName.value);
        if (!obj) {
          console.log(getFormName.value + ' is not found');
          return;
        }
        for (var i = 0; i < obj.elements.length; i++) {
          let el = obj.elements[i];
          let $el = $(el);

          if ($el.attr('readonly') != null) {
            let isUseReadOnly = true;
            let readOnlyType = $el.attr('readOnlyType');
            if (unref(isUpdate) == 1) {
              if (readOnlyType == '1' || readOnlyType == '2') {
                isUseReadOnly = false;
              }
            } else if (unref(isUpdate) == 2) {
              // 如果是编辑页面
              if (isQuote) {
                // 注意js中存在隐式转换，0=='' 为true
                if (readOnlyType === '0') {
                  isUseReadOnly = false;
                }
              } else {
                // 注意js中存在隐式转换，0=='' 为true
                if (readOnlyType === '0' || readOnlyType === '2') {
                  isUseReadOnly = false;
                }
              }
            }

            if (!isUseReadOnly) {
              $el.removeAttr('readonly');
              // console.log($el.attr('name') + ' ' + $el.attr('title') + ' ' + obj.elements[i].tagName);
              if (el.type == 'radio') {
                // 删除其父节点span的readonly属性
                $el.parent().removeAttr('readonly');
                $el.removeAttr('onchange');
                $el.removeAttr('onfocus');
                $el.click(function () {
                  $(this).attr('checked', true);
                });
              } else if (el.tagName == 'SELECT') {
                $el.removeAttr('onchange');
                $el.removeAttr('onfocus');
              } else if (el.type == 'checkbox') {
                $el.removeAttr('onclick');
              }
            }
          }
        }
      }

      // -----------------------------------------------------选择模块开始-------------------------------------------------------------
      const [registerSmartModuleSelDrawer, { openDrawer: openSmartModuleSelDrawer }] = useDrawer();

      let myStart = 1;
      function openSmartModuleSelTableDrawer(selMode = 1, start = 1, params: object) {
        console.log('smart openModuleSelDrawer');
        myStart = start;
        openSmartModuleSelDrawer(true, {
          selMode, // 1 单选，2 多选
          start, // 1 表单域选择 2 嵌套表格拉单
          record: {
            ...params,
          },
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

      function handleSmartModuleSelCallBack(rows) {
        // if (rows.size() > 1) {
        //    createMessage.warning('请选择一条记录');
        // }
        console.log('流程的抽屉', rows);
        let params: any = getCurModuleFieldListParams();
        console.log('params', params);
        if (myStart == 1) {
          params.id = rows[0].ID;
          params.cwsFormName = getFormName.value;
          let thisBody: Object = document.body;
          // 给表单域控赋值，并带入其映射字段
          getItemsForListModuleSel(params).then(async (res) => {
            console.log(res.data);
            $(thisBody).append(res.data);
            await filterJS(res.data, 'items', o('visualForm'), () => {
              return unref(props.getVisible);
            });

            $('.helper-module-list-sel').remove();
            removeScript('items');
          });
        } else if (myStart == 2) {
          let ids = '';
          for (let k in rows) {
            if (ids == '') {
              ids = rows[k].ID;
            } else {
              ids += ',' + rows[k].ID;
            }
          }
          params.ids = ids;
          // params.flowId = unref(formRowData).flowId;
          getSelBatchForNest(params).then(async (res) => {
            console.log(res);
            await filterJS(
              '<script>' + res.data.script + '<\/script>',
              'items',
              o(getFormName.value),
              () => {
                return unref(props.getVisible);
              },
            );
            if (params.nestType == 'nest_sheet') {
              // 如果编辑时可刷新嵌套表，而添加时不能刷新，因为是通过insertRow_***插入的
              if (unref(isUpdate) == 2) {
                eval('reloadNestSheetCtl' + params.moduleCode + '()');
              }
            } else {
              eval('refreshNestTableCtl' + params.nestFieldName + '()');
            }

            removeScript('items');
          });
        }
      }

      function clearSmartModuleSel() {
        setInputObjValue('', '');
      }
      // -----------------------------------------------------选择模块结束-------------------------------------------------------------

      // -----------------------------------------------------嵌套关联模块开始-------------------------------------------------------------
      const [registerSmartModuleRelateTableDrawer, { openDrawer: openSmartModuleRelateDrawer }] =
        useDrawer();
      function openSmartModuleRelateTableDrawer(type = 1, params: object) {
        console.log('openSmartModuleRelateTableDrawer params', params);
        openSmartModuleRelateDrawer(true, {
          isUpdate: type, // 1 新增，2 编辑，3 详情
          record: {
            ...params,
            // moduleCode: '',
            // moduleCodeRelated: '',
            // parentId: '',
            // 在params中实际已有pageType，与判断type得到的结果一样，但前端还是以type为准
            pageType: type == 1 ? 'add_relate' : type == 2 ? 'edit_relate' : 'show_relate',
          },
        });
      }

      function smartModuleRelateTableDrawerhandleSuccess({ moduleCodeRelated, flowId }) {
        console.log('flowId', flowId);
        console.log('isUpdate', isUpdate.value);
        // 如果不是在流程中且不为添加时(即在智能模块中添加嵌套表)，则刷新嵌套表
        if (!(flowId == -1 && isUpdate.value == 1)) {
          eval('reloadNestSheetCtl' + moduleCodeRelated + '()');
        }
      }

      // -----------------------------------------------------嵌套关联模块结束-------------------------------------------------------------

      // 在表单中选择用户
      let curObjName = '';
      function selectUserInForm(objName: string, users: any, type = 0) {
        curObjName = objName;
        openModal(true, { users, type });
      }

      //人员组件选择后回调
      function handleCallBack(data) {
        let realNames = data.map((item) => item.realName).join();
        let names = data.map((item) => item.name).join();
        if (o(curObjName)) {
          o(curObjName).value = names;
          o(curObjName + '_realshow').value = realNames;
        } else {
          console.warn(curObjName + '不存在');
        }
      }

      const getTitle = computed(() =>
        unref(isUpdate) == 1 ? '新增' : unref(isUpdate) == 2 ? '编辑' : '查看',
      );

      // -----------------------------------------------------文件上传开始-------------------------------------------------------------

      const fileList = ref<UploadProps['fileList']>([]);
      //上传前校验
      const beforeUpload: UploadProps['beforeUpload'] = (file) => {
        fileList.value = [...fileList.value, file];
        return false;
      };
      // 上传文件删除
      const handleRemove: UploadProps['onRemove'] = (file) => {
        const index = fileList.value.indexOf(file);
        const newFileList = fileList.value.slice();
        newFileList.splice(index, 1);
        fileList.value = newFileList;
      };

      // -----------------------------------------------------文件上传结束-------------------------------------------------------------
      // -----------------------------------------------------文件列表开始-------------------------------------------------------------
      const columns: BasicColumn[] = [
        {
          title: '标题',
          dataIndex: 'name',
          align: 'left',
          width: '400px',
          slots: { customRender: 'attTitle' },
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
          title: '大小',
          dataIndex: 'fileSizeMb',
        },
      ];
      const fileDataSource = ref<Recordable>([]);
      const [registerTable, { reload: reloadAttachment, setProps }] = useTable({
        title: '', // '附件列表',
        // api: getVisualListAtt, // 需注释掉，否则当查看详情，点击顶部选项卡切换过快时，就会报moduleCode不存在的参数验证错误
        columns,
        formConfig: {},
        searchInfo: {}, //额外的参数
        beforeFetch: (info) => {
          let newInfo = info;
          newInfo.isTreeView = unref(dataRecord).isTreeView;
          newInfo.treeNodeCode = unref(dataRecord).treeNodeCode;
          return newInfo;
        },
        useSearchForm: false,
        showTableSetting: false,
        bordered: true,
        showIndexColumn: true,
        immediate: false,
        pagination: false,
        canResize: false,
        afterFetch: (data) => {
          getFileDataSource(data);
        },
        actionColumn: {
          width: 120,
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
          moduleCode: unref(dataRef).moduleCode,
          attachId: record.id,
          docId: record.docId,
          isTreeView: unref(dataRecord).isTreeView, // 是否树形视图
          treeNodeCode: unref(dataRecord).treeNodeCode, // 树形视图中所选中的节点
        };
        getVisualDelAttach(params).then(() => {
          reloadAttachment();
        });
      }

      function handleDownload(record: any) {
        if (serverInfo.isObjStoreEnabled) {
          getObjectByKey(record.visualPath + '/' + record.diskName);
        } else {
          record.isDownloadAtt = true;
          const params = {
            visitKey: record.visitKey,
            attachId: record.id,
            docId: record.docId, // 流程中的附件带有docId
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
      }

      function downloadFileVisual(fileName, params) {
        getVisualDownload(params).then((data) => {
          if (data) {
            downloadByData(data, fileName);
          }
        });
      }

      // -----------------------------------------------------文件列表结束-------------------------------------------------------------
      function getSelectMultipleVal(obj) {
        let values = [];
        for (let i = 0; i < obj.options.length; i++) {
          if (obj.options[i].selected) {
            values.push(obj.options[i].value);
          }
        }
        return values.join(',');
      }

      const { createMessage, createWarningModal } = useMessage();
      //保存
      async function handleSubmit() {
        if (
          !isUploadFinished(currentRoute.value?.query?.titleName || currentRoute.value?.meta?.title)
        ) {
          createMessage.warn('文件正在上传中，请在文件上传结束后再操作');
          return;
        }

        try {
          const backEntity = ref({});
          backEntity.value = {
            success: false,
          };
          // 要摧毁校验，包括他不允许为空的*以及错误提示都需要摧毁，再实例化lv_cwsWorkflowResult，再调用FormUtil.doGetCheckJS

          if (o(getFormName.value)) {
            var liveOp = new LiveValidation(findObjInForm(getFormName.value, 'cwsHelper'));
            // LiveValidation.destroyValidate(liveOp.formObj.fields);
            // $('.LV_presence').remove();

            if (!LiveValidation.massValidate(liveOp.formObj.fields)) {
              LiveValidation.liveErrMsg = LiveValidation.liveErrMsg.replaceAll('<br/>', '\n');
              // createMessage.warn({
              //   content: () => h('pre', LiveValidation.liveErrMsg),
              // });

              createWarningModal({
                title: t('common.tip'),
                content: () => h('pre', LiveValidation.liveErrMsg),
                okText: '关闭',
              });
              return backEntity.value;
            }

            confirmLoading.value = true;
            let form = new FormData(o(getFormName.value) as any);
            // 如果启用了obs上传，则此处不需要再加入上传按钮所选的文件
            if (!isObjStoreEnabled.value) {
              if (fileList.value.length > 0) {
                fileList.value.forEach((file: UploadProps['fileList'][number], index) => {
                  form.append(`att${index + 1}`, file as any);
                });
              }
            }

            var dropFieldName = null;
            // FormData中只能取到第一个文件，需删除掉，得取dropFiles
            for (var key of form.keys()) {
              // console.log('key:', key, 'value:', form.get(key), 'type:', typeof form.get(key));
              // 如果是可视化多文件上传控件则删除键值，注意要先判断是否存在o(key)，因为当用抽屉中表单自身的上传功能上传时，在上面fileList经append转换后，key会为att1、att2...，但o(key)不存在
              let keyObj = findObj(key, getFormName.value);
              if (keyObj && keyObj.type == 'file' && keyObj.getAttribute('multiple') != null) {
                form.delete(key);
                dropFieldName = key;
              }

              // FormData中，多选型select如果有多个值，则只能记录一个，而如果没有值，则不会记录
              if (keyObj && keyObj.tagName == 'SELECT') {
                if ($(keyObj).attr('multiple') != null) {
                  let val = getSelectMultipleVal(keyObj);
                  console.log('select multiple name=', key, ' val=', val);
                  form.set(key, val);
                }
              }
            }

            if (dropFieldName) {
              // 可视化上传文件宏控件中可能含有拖放的图片
              let dpFiles = getDropFiles(dropFieldName);
              console.log('getFormData dpFiles', dpFiles);
              if (dpFiles && dpFiles.length > 0) {
                for (let i = 0; i < dpFiles.length; i++) {
                  form.append(dropFieldName, dpFiles[i].file);
                }
              }
            }
            try {
              form.append('uploaderImgOrders', getUploaderImgOrders(formCode.value));
            } catch (e) {}

            console.log('unref(isUpdate)', unref(isUpdate));
            if (unref(isUpdate) == 1) {
              // rowIds为工具条“添加模块”按钮点击前选择的记录的ID
              if (dataRecord.value.rowIds) {
                form.append('rowIds', dataRecord.value.rowIds);
              }
              // 如果是嵌套表中的添加
              if (dataRecord.value.pageType && dataRecord.value.pageType === 'add_relate') {
                let query = `?formCode=${dataRef.value.formCode}&moduleCodeRelated=${dataRecord.value.moduleCodeRelated}&pageType=add_relate&parentId=${dataRecord.value.parentId}&flowId=${dataRecord.value.flowId}&actionId=${dataRecord.value.actionId}`;
                form.append('cws_id', dataRecord.value.parentId);
                await getVisualCreateRelate(form, query);
                backEntity.value.success = true;
              } else {
                let query = `?moduleCode=${dataRecord.value.moduleCode}&isTreeView=${dataRecord.value.isTreeView}&treeNodeCode=${dataRecord.value.treeNodeCode}`;
                await getVisualCreate(form, query);
                backEntity.value.success = true;
                tabStore.addCacheModuleCode(dataRecord.value.moduleCode, true);

                if (unref(isTab)) {
                  createConfirm({
                    iconType: 'info',
                    title: () => h('span', t('common.prompt')),
                    content: () => h('span', t('common.opSuccess')),
                    onOk: async () => {
                      handleBack();
                    },
                  });
                }
              }
            } else if (unref(isUpdate) == 2) {
              let query = `?id=${dataRecord.value.id}&moduleCode=${dataRecord.value.moduleCode}&flowId=${dataRecord.value.flowId}&isTreeView=${dataRecord.value.isTreeView}&treeNodeCode=${dataRecord.value.treeNodeCode}`;
              await getVisualUpdate(form, query);
              // 使列表页刷新
              tabStore.addCacheModuleCode(dataRecord.value.moduleCode, true);
              backEntity.value.success = true;
              if (unref(isTab)) {
                createConfirm({
                  iconType: 'info',
                  title: () => h('span', t('common.prompt')),
                  content: () => h('span', t('common.opSuccess')),
                  onOk: async () => {
                    initData(unref(dataRecord));
                  },
                });
              }
            }
          } else {
            console.warn('handleSubmit: ' + getFormName.value + ' is not exist');
          }

          emit('success');
          // 发送close，以使得在processHandleView中调用initWindowFunc
          emit('close');
          fileList.value = [];
          console.log('backEntity.value', backEntity.value);
          return backEntity.value;
        } finally {
          confirmLoading.value = false;
        }
      }

      let activeKeyOld = 0;

      //切换tab
      function getActiveKey(key: number) {
        activeKey.value = key;
        activeRecord.value = {};
        if (key != 0) {
          if (activeKeyOld == 0) {
            // 原来第一个选项卡是用的v-if，所以切换选项卡后，当前抽屉中的visualFormMain在dom中就找不到了
            // curFormUtil.close();
          }
          let record = dataRef.value.aryTab[key];
          console.log('getActiveKey record', record);
          activeRecord.value = record;
          // 清除所有的timeout，包括interval都会被清除
          // clearTimeoutAll();
        } else {
          setTimeout(() => {
            console.log('getActiveKey smartModuleDrawer initWindowFunc');
            initWindowFunc();
          }, 100);
        }
        activeKeyOld = key;
        emit('tabChange', { key: key, isUpdate: isUpdate.value });
      }

      const onClose = async () => {
        curFormUtil.close(getFormName.value);
        removeScript(unref(srcId));
        removeLink();
      };

      function getServerUrl() {
        return userStore.getServerUrl;
      }

      function getPublicPath() {
        const publicPath = import.meta.env.VITE_PUBLIC_PATH || '/';
        return publicPath;
      }

      function reloadAtt() {
        // 可视化上传控件删除后会调用
        if (unref(dataRef).isHasAttachment && unref(dataRef).id) {
          reloadAttachment();
        }
      }

      function getCurFormId() {
        // 注意可能会造成有的页面中忘了设置getCurFormId，导致fo调用getCurFormId时将本页的formName认为是当前form
        // return getFormName.value;
        return curFormUtil?.get();
      }

      function getCurFormUtil() {
        return curFormUtil;
      }

      let excelParams: any = {};
      function openImportExcelModal(
        parentId,
        moduleCode,
        parentFormCode,
        flowId,
        nestFieldName,
        nestType = 'nest_sheet',
      ) {
        importVisible.value = true;
        excelParams = {
          parentId,
          moduleCode,
          parentFormCode,
          flowId,
          nestFieldName,
          nestType,
        };
      }

      //获取files
      function rawFileFn(files) {
        let formData = new FormData();
        formData.append('att1', files);
        formData.append('parentId', excelParams.parentId);
        formData.append('moduleCode', excelParams.moduleCode);
        formData.append('parentFormCode', excelParams.parentFormCode);
        formData.append('flowId', excelParams.flowId);
        let query = '?parentId=' + excelParams.parentId;
        isImport.value = true;
        getVisualImportExcelNest(formData, query).then(() => {
          createMessage.success('操作成功');
          if (excelParams.nestType == 'nest_sheet') {
            // 刷新嵌套表
            eval('reloadNestSheetCtl' + excelParams.moduleCode + '()');
          } else {
            eval('refreshNestTableCtl' + excelParams.nestFieldName + '()');
          }
          isImport.value = false;
          importVisible.value = false;
        });
      }

      //导入modal取消
      function handleCancel() {
        importVisible.value = false;
      }

      function handleDownloadExcelTempl() {
        isDownload.value = true;
        getVisualDownloadExcelTemplForNest(excelParams).then((data) => {
          isDownload.value = false;
          let title = '模板';
          if (data) {
            downloadByData(data, `${title}.xls`);
          }
        });
      }

      const getFormName = computed(() => 'visualFormMain' + curFormUtil?.getFormNo());

      const handleSelectFiles = (
        filePath,
        fieldName,
        fieldTitle,
        formCode,
        pageType,
        mainId,
        accept = '',
        multiple = false,
      ) => {
        let validExt = dataRef.value.validExt;
        let maxFileSize = dataRef.value.maxFileSize;

        selectFiles(
          {
            formName: getFormName.value,
            route: currentRoute.value,
            filePath,
            fieldName,
            fieldTitle,
            formCode,
            pageType,
            mainId,
            accept,
            multiple,
            validExt,
            maxFileSize,
          },
          (files) => {
            console.log('回调文件', files);
          },
        );
      };
      function initWindowFunc() {
        setTimeout(() => {
          curFormUtil?.set(getFormName.value);
          console.log('smartModuleAddEditView initWindowFunc formName', getFormName.value);
        }, 100);

        let newWindow = window as any;
        newWindow.getCurFormUtil = getCurFormUtil;

        newWindow.getCurFormId = getCurFormId;
        newWindow.ajaxPost = ajaxPost;
        newWindow.ajaxGet = ajaxGet;
        newWindow.submitMyFile = submitMyFile;
        newWindow.ajaxGetJS = ajaxGetJS;
        newWindow.filterJS = filterJS;
        newWindow.selectUserInForm = selectUserInForm;
        console.log('init smart openSmartModuleSelTableDrawer');
        newWindow.openSmartModuleSelTableDrawer = openSmartModuleSelTableDrawer;
        newWindow.openSmartModuleRelateTableDrawer = openSmartModuleRelateTableDrawer;
        newWindow.myConfirm = myConfirm;
        newWindow.myMsg = myMsg;
        newWindow.ajaxPostJson = ajaxPostJson;
        newWindow.downloadFile = downloadFile; // 流程附件下载
        newWindow.reloadAttachment = reloadAtt;
        newWindow.downloadFileVisual = downloadFileVisual; // 模块附件下载
        newWindow.getServerUrl = getServerUrl;
        newWindow.getPublicPath = getPublicPath;
        newWindow.openSmartModuleDrawerForShow = openSmartModuleDrawerForShow;
        newWindow.getToken = getToken;
        newWindow.openWritePadModal = openWritePadModal;
        newWindow.openLocationMarkModal = openLocationMarkModal;
        newWindow.openSelDeptModal = openSelDeptModal;
        newWindow.initFormCtl = initFormCtl;
        newWindow.openProcessShowDrawerForShow = openProcessShowDrawerForShow;
        newWindow.openImportExcelModal = openImportExcelModal;
        newWindow.selectFiles = handleSelectFiles;
        newWindow.getServerInfo = getServerInfo;
        newWindow.deleteByField = deleteByField;
        newWindow.downloadObsFile = handleDownloadObsFile;
        newWindow.setUploadFileTreeListAttInfo = setUploadFileTreeListAttInfo;
      }

      const [registerModal, { openModal }] = useModal();

      function handleOpenEditDrawer() {
        onClose();
        if (unref(isTab)) {
          dataRecord.value.isUpdate = 2;
          initData(dataRecord.value);
        } else {
          emit('editAction', dataRecord.value);
        }
      }

      function handleBtnClick(item) {
        console.log('handleBtnClick item', item);
        if (item.event === 'link') {
          item['moduleId'] = dataRecord.value['id'];
          item['btnId'] = item.id;
          go({
            path: item.link,
            query: {
              ...item,
              ...dataRecord,
            },
          });
        } else if (item.event === 'flow') {
          onClose();
          item['moduleId'] = dataRecord.value['id'];
          item['btnId'] = item.id;
          if (unref(isTab)) {
            handleLaunchFlowInDrawer(item);
          } else {
            emit('launchFlowAction', item);
          }
        } else {
          // click事件
          try {
            eval(item['link']);
          } catch (e) {
            console.log('handleBtnClick', e);
          }
        }
      }

      const [registerProcessDrawer, { openDrawer: openProcessDrawer }] = useDrawer();

      // 在编辑或查看详情抽屉中发起流程
      function handleLaunchFlowInDrawer(item) {
        // 发起流程
        let params = {
          op: 'opBtnFlow',
          typeCode: item.flowTypeCode,
          ...item,
        };
        getFlowInit(params).then((res) => {
          let myActionId = res.myActionId || '';
          if (myActionId) {
            openProcessDrawer(true, {
              myActionId: myActionId,
              type: res.type,
            });
          }
        });
      }

      const [registerWritePadModal, { openModal: openWPadModal }] = useModal();
      const openWritePadModal = (fieldName, w, h) => {
        openWPadModal(true, { fieldName, w, h });
      };

      async function handleWritePadCallBack(record) {}

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

      const [registerSelDeptModal, { openModal: openSDeptModal }] = useModal();
      const openSelDeptModal = (record) => {
        console.log('openSelDeptModal record', record);
        openSDeptModal(true, {
          ...record,
        });
      };

      async function handleSelDeptCallBack(record) {
        let codes = '',
          names = '';
        if (record) {
          if (record.length) {
            for (let i in record) {
              if (codes == '') {
                codes = record[i].value;
                names = record[i].label;
              } else {
                codes += ',' + record[i].value;
                names += ',' + record[i].label;
              }
            }
          } else {
            codes = record.value;
            names = record.label;
          }
        }
        console.log('handleSelDeptCallBack', record, 'record.length', record.length);
        setInputObjValue(codes, names);
      }

      const [registerProcessShowDrawer, { openDrawer: openProcessShowDrawer }] = useDrawer();
      function openProcessShowDrawerForShow(flowId, visitKey) {
        let params = {
          flowId,
          visitKey,
          isTab: false,
        };
        openProcessShowDrawer(true, params);
      }
      function handleProcessShowCallBack(rows) {}
      function handleSuccess() {}

      const handleDownloadObsFile = (key) => {
        // getObjectByKey('upfile/visual/personbasic/2023/8/4dbc0ea68968427cb.doc');
        getObjectByKey(key);
      };

      let fileNo = 0;
      //获取文件change事件
      const handleFilesChange = (info) => {
        console.log('info', info);
        const { file } = info;

        let fileName = file.name;
        let ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (dataRef.value.validExt && dataRef.value.validExt.length > 0) {
          let isExtValid = dataRef.value.validExt.some((item) => item == ext);
          if (!isExtValid) {
            createMessage.warn('文件: ' + file.name + ' 类型非法');
            return;
          }
        }
        let maxFileSize = dataRef.value.maxFileSize;
        if (file.size > maxFileSize * 1024) {
          createMessage.warn('文件:' + file.name + ' 大小不能超过 ' + maxFileSize + 'K');
          return;
        }

        uploadFileObjectFunc({
          files: [file],
          formName: getFormName.value,
          route: currentRoute.value,
          fieldName: 'att' + fileNo,
          filePath: dataRef.value.visualPath,
          formCode: formCode.value,
          pageType: pageType.value,
          mainId: dataRecord.value?.id,
          fieldTitle: '附件',
        });

        fileNo++;
      };

      const handleRemoveFile = (file, actions) => {
        console.log('handleRemoveFile', file);
        let treeList = uploadFileStore.getUploadFileTreeList;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              if (it.progress == 100) {
                if (pageType.value == 'edit') {
                  let params = {
                    flowId: -1,
                    attachId: it.attId,
                  };
                  getDelAttach(params).then(() => {
                    deleteByUid(file.uid);

                    actions.remove();
                    reloadAttachment();
                  });
                } else {
                  // 添加页面通过diskName删除
                  var diskName = it.fieldValue;
                  var p = diskName.lastIndexOf('/');
                  diskName = diskName.substring(p + 1);
                  let params = {
                    flowId: -1,
                    diskName: diskName,
                  };

                  getDelAttach(params).then(() => {
                    // 删除在macro_attachment_ctl.jsp中的putUploadProgress中插入的隐藏域
                    console.log('o(getFormName.value)', o(getFormName.value));
                    $(o(getFormName.value))
                      .find('input[name=att]')
                      .each(function () {
                        console.log('handleRemoveFile this', this);
                        if ($(this).val() == diskName) {
                          $(this).remove();
                        }
                      });

                    deleteByUid(file.uid);
                    actions.remove();
                  });
                }
              } else {
                deleteByUid(file.uid);
                actions.remove();
              }
            }
          });
        });
      };

      const uploadFileStore = useUploadFileStore();
      const getProgress = computed(() => (file: any) => {
        console.log('file', file);
        let treeList = uploadFileStore.getUploadFileTreeList;
        let progress = 100;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              progress = it.progress;
              if (progress == 100) {
                if (pageType.value == 'edit') {
                  reloadAttachment();
                }
              }
            }
          });
        });
        return progress;
      });

      const getAttVisible = computed(() => (file: any) => {
        let treeList = uploadFileStore.getUploadFileTreeList;
        let isFound = false;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              isFound = true;
            }
          });
        });
        // 未找到，说明是从树形列表上删除了该节点
        return isFound;
      });

      const getAttUploadFinishedIsImg = computed(() => (file: any) => {
        let treeList = uploadFileStore.getUploadFileTreeList;
        let progress = 0;
        let isFound = false;
        let isImg = false;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              progress = it.progress;
              isImg = isImage(it.fieldValue);
              isFound = true;
            }
          });
        });
        // 未找到，说明是从树形列表上删除了该节点
        if (!isFound) {
          return false;
        }
        if (progress == 100) {
          return isImg;
        }
      });

      const handleShowImgAtt = (file: any) => {
        let treeList = uploadFileStore.getUploadFileTreeList;
        let progress = 0;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              progress = it.progress;
              if (progress == 100) {
                window.open(
                  serverInfo.rootPath +
                    '/showImg.do?visitKey=' +
                    it.visitKey +
                    '&path=' +
                    it.fieldValue,
                );
              }
            }
          });
        });
      };

      const getAttUploadFinishedCanPreview = computed(() => (file: any) => {
        let treeList = uploadFileStore.getUploadFileTreeList;
        let progress = 0;
        let isFound = false;
        let canPreview = false;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              progress = it.progress;
              canPreview = it.canPreview;
              isFound = true;
            }
          });
        });
        // 未找到，说明是从树形列表上删除了该节点
        if (!isFound) {
          return true;
        }
        if (progress == 100) {
          return canPreview;
        }
      });

      const getAttUploadFinished = computed(() => (file: any) => {
        let treeList = uploadFileStore.getUploadFileTreeList;
        let progress = 0;
        let isFound = false;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              progress = it.progress;
              isFound = true;
            }
          });
        });
        // 未找到，说明是从树形列表上删除了该节点
        if (!isFound) {
          return true;
        }
        if (progress == 100) {
          return true;
        }
      });

      const handlePreviewAtt = computed(() => (file: any) => {
        let previewUrl = '';
        let treeList = uploadFileStore.getUploadFileTreeList;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              previewUrl = it.previewUrl;
            }
          });
        });

        window.open(previewUrl);
      });

      const setUploadFileTreeListAttInfo = (file: any, attId, canPreview, previewUrl, visitKey) => {
        let treeList = uploadFileStore.getUploadFileTreeList;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('setUploadFileTreeListAttInfo it', it);
            console.log('setUploadFileTreeListAttInfo file.uid', file.uid);
            if (it.file.uid === file.uid) {
              it.attId = attId;
              it.canPreview = canPreview;
              it.previewUrl = previewUrl;
              it.visitKey = visitKey;

              console.log('setUploadFileTreeListAttInfo it2', it);
              uploadFileStore.setUploadFileTreeList(treeList);
            }
          });
        });
      };

      const handleDownloadAtt = computed(() => (file: any) => {
        let key = '';
        let treeList = uploadFileStore.getUploadFileTreeList;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              key = it.fieldValue;
              handleDownloadObsFile(key);
            }
          });
        });
      });

      return {
        getTitle,
        handleSubmit,
        dataRef,
        activeKey,
        getActiveKey,
        activeRecord,
        onClose,
        registerModal,
        handleCallBack,
        registerSmartModuleSelDrawer,
        handleSmartModuleSelCallBack,
        clearSmartModuleSel,
        registerSmartModuleRelateTableDrawer,
        smartModuleRelateTableDrawerhandleSuccess,
        isUpdate,
        registerTable,
        handleDelete,
        handleDownload,
        beforeUpload,
        handleRemove,
        fileList,
        pageType,
        printForm,
        registerSmartModuleShowDrawer,
        fileDataSource,
        formCode,
        handleOpenEditDrawer,
        handleBtnClick,
        registerWritePadModal,
        handleWritePadCallBack,
        isAttachmentShow,
        registerLocationMarkModal,
        handleLocationMarkCallBack,
        registerSelDeptModal,
        handleSelDeptCallBack,
        initWindowFunc,
        spinning,
        registerProcessShowDrawer,
        handleProcessShowCallBack,
        importVisible,
        rawFileFn,
        isImport,
        isDownload,
        handleCancel,
        handleDownloadExcelTempl,
        getFormName,
        confirmLoading,
        isTab,
        handleBack,
        initData,
        registerProcessDrawer,
        handleSuccess,
        handleSelectFiles,
        handleDownloadObsFile,
        handleFilesChange,
        handleRemoveFile,
        getProgress,
        getAttUploadFinished,
        handleDownloadAtt,
        isObjStoreEnabled,
        handlePreviewAtt,
        getAttUploadFinishedCanPreview,
        getAttUploadFinishedIsImg,
        handleShowImgAtt,
        getAttVisible,
      };
    },
  });
</script>

<style lang="less" scoped>
  :deep(.vben-basic-table .ant-table-wrapper) {
    padding: 6px 0;
  }
</style>
