<template>
  <div class="bg-white" :class="{ 'pt-2': !!typeCode, 'pt-1': true, 'pl-2': true, 'pr-2': true }">
    <Spin :spinning="isSpinning">
      <Row>
        <Button type="primary" size="small" @click="saveDraft">
          <template #icon><SaveOutlined /></template>
          保存
        </Button>
        <Button
          type="primary"
          size="small"
          class="ml-2"
          @click="toolbarSubmit"
          v-if="formRowData.aryButton.some((item) => item.name === 'commit')"
        >
          <template #icon><Icon icon="clarity:success-standard-line" /></template>
          {{ btnAgreeName }}
        </Button>
        <Button
          type="primary"
          size="small"
          class="ml-2"
          @click="returnFlow"
          v-if="formRowData.aryButton.some((item) => item.name === 'return')"
        >
          <template #icon><RollbackOutlined /></template>
          {{ btnReturnName }}
        </Button>

        <Popconfirm
          placement="top"
          title="确定拒绝吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="disagreeFlow"
        >
          <Button
            type="primary"
            size="small"
            class="ml-2"
            v-if="formRowData.aryButton.some((item) => item.name === 'disagree')"
          >
            <template #icon><CloseCircleOutlined /></template>
            {{ btnDisagreeName }}
          </Button>
        </Popconfirm>

        <Button
          type="primary"
          size="small"
          class="ml-2"
          @click="openFlowChartInfoDrawer"
          v-if="formRowData.aryButton.some((item) => item.name === 'chart')"
        >
          <template #icon><DeliveredProcedureOutlined /></template>
          流程图
        </Button>
        <!-- <Button type="primary" size="small" class="ml-2">
          <template #icon><HeartOutlined /></template>
          关注
        </Button>
         -->

        <Dropdown>
          <Button type="primary" size="small" class="ml-2"
            ><template #icon><PrinterOutlined /></template> 打印
          </Button>
          <template #overlay>
            <Menu @click="onClickMenu">
              <MenuItem key="form"> 打印表单 </MenuItem>
              <MenuItem key="all"> 打印全部 </MenuItem>
              <MenuItem v-for="(item, index) in formRowData.aryView" :key="item.id">
                {{ item.name }}
              </MenuItem>
            </Menu>
          </template>
        </Dropdown>

        <Button
          type="primary"
          size="small"
          class="ml-2"
          @click="handleManager"
          v-if="formRowData.aryButton.some((item) => item.name === 'manage')"
        >
          <template #icon><SettingOutlined /></template>
          管理
        </Button>
        <Button
          type="primary"
          size="small"
          class="ml-2"
          @click="handleRefresh"
          v-if="formRowData.aryButton.some((item) => item.name === 'debug')"
        >
          <template #icon><Icon icon="clarity:refresh-line" /></template>
          刷新
        </Button>
      </Row>
      <Row class="h-8 flex justify-items-center items-center">
        <Col :span="2"> <span>提交给 →</span>：</Col>
        <Col :span="22">
          <div class="flex">
            <Button type="primary" size="small" class="ml-2" @click="selectUser()">
              选择用户
            </Button>
          </div>
        </Col>
      </Row>
      <Row
        class="h-8 flex justify-items-start items-center"
        v-for="(user, index) in nextUsers"
        :key="index"
      >
        <Form layout="horizontal" class="w-full"
          ><Row class="flex items-center">
            <Col :span="3"
              ><FormItem label="用户">{{ user.realName }}</FormItem></Col
            >
            <Col :span="4"
              ><FormItem label="顺序"><Input v-model:value="user.order" /></FormItem
            ></Col>
            <Col :span="5"
              ><FormItem label="到期时间" :labelCol="{ span: 12 }"
                ><Input v-model:value="user.expireHour" /></FormItem
            ></Col>
            <Col :span="2"><span class="ml-2">小时</span></Col>
            <Col :span="2"
              ><Icon
                icon="clarity:times-line"
                class="cursor-pointer"
                @click="delUser(user.name)"
                color="red"
                title="删除"
            /></Col>
          </Row> </Form
      ></Row>
      <Row class="flex items-center pt-2">
        <Col :span="6" v-if="formRowData.isFlowLevelDisplay && !formRowData.isFlowStarted">
          <FormItem label="">
            <RadioGroup name="radioGroup" v-model:value="formRowData.cwsWorkflowLevel">
              <Radio :value="0">普通</Radio>
              <Radio :value="1">重要</Radio>
              <Radio :value="2">紧急</Radio>
              <!-- <Radio :value="-1">其他</Radio> -->
            </RadioGroup>
          </FormItem>
        </Col>
        <Col :span="2" v-else class="ml-2">
          <span title="普通" v-if="formRowData.cwsWorkflowLevel == 0"
            ><Icon icon="clarity:control-lun-line"
          /></span>
          <span title="一般" v-if="formRowData.cwsWorkflowLevel == 1" style="color: orange"
            ><Icon icon="clarity:control-lun-outline-badged"
          /></span>
          <span title="紧急" v-if="formRowData.cwsWorkflowLevel == 2" style="color: red"
            ><Icon icon="clarity:control-lun-outline-badged"
          /></span>
          {{ formRowData.levelDesc }}
        </Col>
        <Col :span="2">
          <FormItem label="流程号" :labelCol="{ style: '100px' }">
            {{ formRowData.flowId }}
          </FormItem>
        </Col>
        <Col :span="16">
          <Form layout="horizontal" :label-col="{ span: 8 }">
            <Row>
              <Col :span="10">
                <FormItem label="标题" :labelCol="{ style: '100px' }">
                  <Input
                    v-model:value="formRowData.cwsWorkflowTitle"
                    :readonly="formRowData.isFlowStarted || formRowData.isFlowTitleReadonly"
                    placeholder="请输入标题"
                  />
                </FormItem>
              </Col>
              <Col :span="5">
                <FormItem label="发起人">
                  <Input
                    v-model:value="formRowData.starterRealName"
                    placeholder="请输入发起人"
                    readonly
                  />
                </FormItem>
              </Col>
              <Col :span="9" v-if="flowIsRemarkShow">
                <FormItem :label="t('flow.leaveword')" :labelCol="{ style: '100px' }">
                  <Input
                    v-model:value="formRowData.cwsWorkflowResult"
                    :placeholder="t('flow.leavewordPlaceholder')"
                    :name="'cwsWorkflowResult'"
                  />
                </FormItem>
              </Col>
            </Row>
          </Form>
        </Col>
      </Row>
      <div class="border-1 border-solid border-gray-80 min-h-full" id="flowFormBox">
        <form id="flowForm" :name="getFormName" :formCode="formRowData ? formRowData.formCode : ''">
          <input type="hidden" id="op" name="op" value="saveformvalue" />
          <input type="hidden" name="cws_id" :value="cwsId" />
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div v-html="content"></div>
        </form>

        <!--意见输入框常用语提示框-->
        <div id="phraseBox">
          <div class="phraseCnt">
            <div class="phraseClose" onclick="closeTipPhrase()">×</div>
            <div class="phraseAdd" @click="openTipPhraseModal">+</div>
            <div class="phraseIcon" style="clear: both"> </div>
          </div>
        </div>
      </div>
      <Row>
        <template v-if="!isObjStoreEnabled">
          <Upload
            :file-list="fileList"
            :before-upload="beforeUpload"
            @remove="handleRemove"
            v-show="formRowData.isHasAttachment"
          >
            <Button>
              <UploadOutlined />
              上传文件
            </Button>
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
                  <span :style="file.status === 'error' ? 'color: red' : ''">{{ file.name }}</span>
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
      <BasicTable
        @register="registerTable"
        v-show="formRowData.isHasAttachment && fileDataSource && fileDataSource.length > 0"
      >
        <template #attTitle="{ record }">
          <div v-if="record.isPreview"
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
                loading: record.isDownloadAtt ? true : false,
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

      <BasicTable @register="registerHandleTable" />

      <Modal v-model:visible="returnFlowVisible" title="选择" @ok="handleReturnFlow">
        <div v-if="isReturnStyleFree" style="padding: 20px">
          <RadioGroup name="radioGroup" v-model:value="returnIdValueRadio">
            <Radio
              :value="item.returnId"
              v-for="(item, index) in returnResult"
              :key="index"
              style="display: flex"
              >{{ item.actionTitle }} {{ item.userRealName }}</Radio
            >
          </RadioGroup>
        </div>
        <div v-else style="padding: 20px">
          <CheckboxGroup
            name="CheckboxGroup"
            :options="returnResult"
            v-model:value="returnIdValueCheckBox"
            style="display: flex; flex-direction: column"
          />
        </div>
      </Modal>

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
      />
      <SmartModuleDrawer
        @register="registerSmartModuleDrawer"
        @success="handleSmartModuleCallBack"
        @close="initWindowFunc"
      />
      <FlowChartDrawer @register="registerFlowChartDrawer" />
      <ProcessShowDrawer
        @register="registerProcessShowDrawer"
        @success="handleProcessShowCallBack"
      />
      <ProcessViewDrawer
        @register="registerProcessViewDrawer"
        @success="handleProcessViewCallBack"
      />
      <InputPwdModal @register="registerInputPwdModal" @success="handleInputPwdCallBack" />
      <WritePadModal @register="registerWritePadModal" @success="handleWritePadCallBack" />
      <SelStampModal @register="registerSelStampModal" @success="handleSelStampCallBack" />
      <TipPhraseModal @register="registerTipPhraseModal" @success="handleTipPhraseCallBack" />
    </Spin>
  </div>
</template>
<script lang="ts">
  import {
    defineComponent,
    ref,
    unref,
    onMounted,
    onUnmounted,
    h,
    watch,
    inject,
    onActivated,
    computed,
  } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ImpExcel } from '/@/components/Excel';
  import { useDrawer } from '/@/components/Drawer';
  import SmartModuleSelDrawer from '../smartModule/modules/smartModuleSelDrawer.vue';
  import SmartModuleDrawer from '../smartModule/modules/smartModuleDrawer.vue';
  import ProcessShowDrawer from './processShowDrawer.vue';
  import ProcessViewDrawer from './processViewDrawer.vue';
  import { getIsDebug, getToken } from '/@/utils/auth';
  import InputPwdModal from './modules/InputPwdModal.vue';
  import WritePadModal from './modules/WritePadModal.vue';
  import SelStampModal from './modules/SelStampModal.vue';
  import TipPhraseModal from './modules/TipPhraseModal.vue';
  import {
    getFlowProcessFree,
    getFinishActionFree,
    getListAttachment,
    getListAttachmentDelAtt,
    submitMyFile,
    getReturnAction,
    getDownload,
    downloadFile,
    getPhrases,
    getDelAttach,
  } from '/@/api/process/process';
  import { getShowImg } from '/@/api/system/system';
  import {
    SaveOutlined,
    SettingOutlined,
    // HeartOutlined,
    DeliveredProcedureOutlined,
    PrinterOutlined,
    UploadOutlined,
    RollbackOutlined,
    CloseCircleOutlined,
    ProfileOutlined,
    PictureOutlined,
    DownloadOutlined,
    DeleteOutlined,
  } from '@ant-design/icons-vue';
  import {
    Button,
    CheckboxGroup,
    Row,
    Col,
    Upload,
    UploadProps,
    Radio,
    Input,
    Form,
    Modal,
    Spin,
    Popconfirm,
    Dropdown,
    Menu,
    MenuItem,
    MenuProps,
    Progress,
    Tooltip,
  } from 'ant-design-vue';
  import Icon from '/@/components/Icon/index';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { BasicColumn } from '/@/components/Table';
  import { SelectUser } from '/@/components/CustomComp';
  import { useModal } from '/@/components/Modal';
  import SmartModuleRelateTableDrawer from '../smartModule/modules/smartModuleRelateTableDrawer.vue'; //选择模块
  import FlowChartDrawer from './flowChartDrawer.vue';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { useGo } from '/@/hooks/web/usePage';
  import printJS from 'print-js';
  import {
    removeScript,
    removeLink,
    filterJS,
    ajaxGet,
    ajaxPost,
    ajaxGetJS,
    myConfirm,
    myMsg,
    ajaxPostJson,
    loadImg,
    initFormCtl,
    getServerInfo,
    isImage,
  } from '/@/utils/utils';
  import { useRoute } from 'vue-router';
  import { useUserStore } from '/@/store/modules/user';
  import {
    getVisualExportExcelRelate,
    getVisualImportExcelNest,
    getVisualDownloadExcelTemplForNest,
    getItemsForListModuleSel,
    getSelBatchForNest,
  } from '/@/api/module/module';
  import { downloadByData } from '/@/utils/file/download';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { useRouter } from 'vue-router';

  import {
    selectFiles,
    deleteByField,
    isUploadFinished,
    getObjectByKey,
    uploadFileObjectFunc,
    deleteByUid,
  } from '/@/utils/uploadFile';
  import { useUploadFileStore } from '/@/store/modules/uploadFile';

  interface formRowData {
    cwsWorkflowTitle: string;
    cwsWorkflowResult?: string;
    cwsWorkflowLevel?: number; // 等级
    levelDesc?: string; // 等级描述
    flowId: string;
    starterRealName: string;
    actionId: string;
    myActionId: number;
    aryButton: Array<any>;
    isFlowLevelDisplay: boolean; // 是否显示流程等级
    hasView: boolean; // 是否有视图
    aryView: Array<any>; // 视图数组
    isFlowStarted: boolean; // 流程是否已启动
    isFlowTitleReadonly: boolean; // 流程标题是否只读
    formCode: string; // 表单编码
    isHasAttachment: boolean; // 表单是否带有附件
    isMyPlus: boolean; // 是否我加的加签
    isPlusBefore: boolean; // 是否为前加签
    plusDesc: string; // 加签的描述
    isPlus: boolean; // 是否为加签处理
    flowTitleDefault: string; // 默认标题
  }

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'processHandleFreeView',
    components: {
      Button,
      SaveOutlined,
      SettingOutlined,
      // HeartOutlined,
      DeliveredProcedureOutlined,
      PrinterOutlined,
      RollbackOutlined,
      CloseCircleOutlined,
      CheckboxGroup,
      Row,
      Col,
      Upload,
      BasicTable,
      TableAction,
      RadioGroup: Radio.Group,
      Radio,
      Input,
      Form,
      FormItem: Form.Item,
      SelectUser,
      Icon,
      UploadOutlined,
      Modal,
      SmartModuleRelateTableDrawer,
      SmartModuleSelDrawer,
      FlowChartDrawer,
      Spin,
      SmartModuleDrawer,
      ProcessShowDrawer,
      ImpExcel,
      Popconfirm,
      Dropdown,
      Menu,
      MenuItem,
      ProcessViewDrawer,
      InputPwdModal,
      WritePadModal,
      SelStampModal,
      TipPhraseModal,
      ProfileOutlined,
      PictureOutlined,
      DownloadOutlined,
      Progress,
      Tooltip,
      DeleteOutlined,
    },
    props: {
      myActionId: {
        type: Number,
        default: 0,
      },
    },
    emits: ['success', 'register', 'showView', 'closeDrawer'],
    setup(props, { emit }) {
      const { createMessage, createConfirm } = useMessage();
      const isSpinning = ref(false);
      const content = ref('');
      const common = ref('');
      const srcId = ref('-src');
      const selectDeptCode = ref('');
      const { t } = useI18n();
      const route = useRoute();
      const go = useGo();
      const userStore = useUserStore();
      const isDownloadAtt = ref(false);
      const cwsId = ref(0);
      const flowIsRemarkShow = ref(true);
      const isPlusDescShow = ref(false);
      const fileDataSource = ref<Recordable>([]);
      const fileList = ref<UploadProps['fileList']>([]);
      const { closeCurrent } = useTabs();
      const formRowData = ref<formRowData>({
        cwsWorkflowTitle: '',
        cwsWorkflowResult: '',
        cwsWorkflowLevel: 0,
        levelDesc: '',
        flowId: '',
        starterRealName: '',
        actionId: '',
        myActionId: -1,
        aryButton: [],
        isFlowLevelDisplay: true,
        hasView: false,
        aryView: [],
        isFlowStarted: false,
        isFlowTitleReadonly: true,
        formCode: '',
        isHasAttachment: true,
        isPlusBefore: true,
        isMyPlus: false,
        plusDesc: '',
        isPlus: false,
        flowTitleDefault: '',
      });

      const curFormUtil: any = inject('curFormUtil');
      const { currentRoute } = useRouter();
      const serverInfo = userStore.getServerInfo;
      const isObjStoreEnabled = ref(serverInfo.isObjStoreEnabled);

      // 导入
      const isImport = ref(false);
      const isDownload = ref(false);
      const btnAgreeName = ref('提交');
      const btnReturnName = ref('退回');
      const btnDisagreeName = ref('拒绝');
      const nextUsers = ref([]);
      // -----------------------------------------------------获取当前流程信息开始-------------------------------------------------------------
      //获取当前流程信息
      let flowProcessData = {} as any;
      async function getFlowProcessContent(myActionId: number) {
        console.log('myActionId', myActionId);
        initWindowFunc();
        let data = await getFlowProcessFree({ myActionId: myActionId });
        flowProcessData = data;
        // common.value = data.script;
        selectDeptCode.value = '';
        content.value = data.content;
        cwsId.value = data.cws_id;
        flowIsRemarkShow.value = data.flowIsRemarkShow;

        if (flowProcessData.aryMyAction && flowProcessData.aryMyAction.length > 0) {
          setTableData(flowProcessData.aryMyAction);
        } else {
          setTableData([]);
        }

        formRowData.value = {
          cwsWorkflowTitle: data.flowTitle,
          cwsWorkflowResult: '',
          cwsWorkflowLevel: data.level,
          levelDesc: data.levelDesc,
          flowId: data.flowId,
          actionId: data.actionId,
          myActionId: myActionId,
          starterRealName: data.starterRealName,
          aryButton: data.aryButton,
          isFlowLevelDisplay: data.isFlowLevelDisplay,
          hasView: data.hasView,
          aryView: data.aryView,
          isFlowStarted: data.isFlowStarted,
          isFlowTitleReadonly: data.isFlowTitleReadonly,
          formCode: data.formCode,
          isHasAttachment: data.isHasAttachment,
          isMyPlus: data.isMyPlus,
          isPlusBefore: data.isPlusBefore,
          plusDesc: data.plusDesc,
          isPlus: data.isPlus,
          flowTitleDefault: data.flowTitleDefault,
        };
        console.log('formRowData', unref(formRowData));

        isPlusDescShow.value =
          formRowData.value.isMyPlus &&
          !formRowData.value.isPlusBefore &&
          formRowData.value.plusDesc.length > 0;

        await filterJS(data.content, srcId.value, o('flowForm'));

        console.log('aryButton', data.aryButton);
        // 取出同意、退回及拒绝按钮的名称
        for (let k in data.aryButton) {
          let btn = data.aryButton[k];
          if (btn.name === 'commit') {
            btnAgreeName.value = btn.text;
          } else if (btn.name === 'disagree') {
            btnDisagreeName.value = btn.text;
          } else if (btn.name === 'return') {
            btnReturnName.value = btn.text;
          }
        }

        // 如果附件列表大于0，则置props及刷新附件列表
        setProps({ searchInfo: { flowId: unref(formRowData).flowId } });
        reloadAttachment();

        let rootPath = import.meta.env.VITE_PUBLIC_PATH;
        if (rootPath.endsWith('/')) {
          rootPath = rootPath.substring(0, rootPath.lastIndexOf('/'));
        }

        const url = `${rootPath}/resource/js/form/form_js_${data.formCode}.js?pageType=flow&flowId=${data.flowId}&myActionId=${data.myActionId}`;
        console.log('form_js url', url);
        let script1 = document.createElement('script');
        script1.type = 'text/javascript';
        script1.src = url;
        script1.id = `${100}${srcId.value}`;
        document.getElementsByTagName('head')[0].appendChild(script1);

        // 加载后台事件中配置的前台脚本
        if (data.formJs && data.formJs.length > 0) {
          let scriptFormJs = document.createElement('script');
          scriptFormJs.type = 'text/javascript';
          scriptFormJs.text = data.formJs;
          scriptFormJs.id = `${101}${srcId.value}`;
          document.getElementsByTagName('head')[0].appendChild(scriptFormJs);
        }

        setTimeout(() => {
          // 初始化日期控件
          initDatePicker();

          // 初始化计算控件
          initCalculator();
        }, 100);

        initFormCtl('flowForm');
        loadImg('flowForm');

        fetchPhrases();
      }

      // 取得常用语
      const fetchPhrases = () => {
        getPhrases({}).then((data) => {
          $('.phraseIcon').html(data.tipHtml);
        });
      };
      // -----------------------------------------------------获取当前流程信息结束-------------------------------------------------------------

      // -----------------------------------------------------选择人员开始-------------------------------------------------------------
      // 是否在表单中选择用户标志位
      let isSelUserInForm = false;
      function selectUser() {
        isSelUserInForm = false;
        let users = [];
        // if (record.checkers && record.checkers.length > 0) {
        //   users = record.checkers.map((item) => {
        //     return {
        //       ...item,
        //       name: item.userName,
        //     };
        //   });
        // }
        openModal(true, {
          isUpdate: false,
          users: users,
        });
      }
      // 在表单中选择用户
      let curObjName = '';
      function selectUserInForm(objName: string, users: any, type = 0) {
        isSelUserInForm = true;
        curObjName = objName;
        openModal(true, { users, type });
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
          nextUsers.value = data;
          nextUsers.value.forEach((item, index) => {
            item.order = 1;
            item.expireHour = 0;
          });
        }
      }
      // -----------------------------------------------------选择人员结束-------------------------------------------------------------

      // -----------------------------------------------------表单保存开始-------------------------------------------------------------
      //保存
      async function saveDraft() {
        if (
          !isUploadFinished(currentRoute.value?.query?.titleName || currentRoute.value?.meta?.title)
        ) {
          createMessage.warn('文件正在上传中，请在文件上传结束后再操作');
          return;
        }

        o('op').value = 'saveformvalue';
        const form = getFormData();
        isSpinning.value = true;
        await getFinishActionFree(form).then(async (res) => {
          await getFlowProcessContent(unref(formRowData).myActionId);
          fileList.value = [];
          isSpinning.value = false;
          createMessage.success(res.msg);
        });
      }
      // -----------------------------------------------------表单保存结束-------------------------------------------------------------

      // -----------------------------------------------------表单提交开始-------------------------------------------------------------
      //提交
      function toolbarSubmit() {
        try {
          ctlOnBeforeSerialize();
        } catch (e) {}

        // 重新生成LiveValidation校验规则
        if (flowProcessData.checkJsSub) {
          try {
            eval(flowProcessData.checkJsSub);
          } catch (e) {}
        }

        // 进行LiveValidation校验
        var liveOp = new LiveValidation('op');
        if (!LiveValidation.massValidate(liveOp.formObj.fields)) {
          LiveValidation.liveErrMsg = LiveValidation.liveErrMsg.replaceAll('<br/>', '\n');
          createMessage.warn({
            content: () => h('pre', LiveValidation.liveErrMsg),
          });
          return;
        }

        // 检验规则
        if (flowProcessData.checkJsSub) {
          try {
            eval(flowProcessData.checkJsSub);
          } catch (e) {
            console.log(e);
          }
        }

        if (nextUsers.value.length > 0) {
          let hasOrderEqOne = nextUsers.value.some((item) => item.order == 1);
          if (!hasOrderEqOne) {
            createMessage.error('顺序须从1开始');
            return;
          }
        }

        let canUserStartFlow = flowProcessData.canUserStartFlow;
        let msg = canUserStartFlow ? '您确定要开始流程么' : '您确定要提交么';

        if (nextUsers.value.length == 0) {
          msg = '您还没选择下一步用户，确定办理完毕了么';
        } else if (formRowData.value.cwsWorkflowTitle == formRowData.value.flowTitleDefault) {
          msg = '流程标题为默认标题，您确定不修改就提交么';
        }

        createConfirm({
          iconType: 'warning',
          title: () => h('span', t('common.prompt')),
          content: () => h('span', msg),
          onOk: async () => {
            // 摧毁LiveValidation校验，如不摧毁，在massValidate后，会形成重复校验
            var liveOp = new LiveValidation('op');
            LiveValidation.destroyValidate(liveOp.formObj.fields);
            // 清除不允许为空的红色*标
            $('.LV_presence').remove();

            isSpinning.value = true;
            await submitResult();
            isSpinning.value = false;
          },
        });
      }

      async function submitResult() {
        if (
          !isUploadFinished(currentRoute.value?.query?.titleName || currentRoute.value?.meta?.title)
        ) {
          createMessage.warn('文件正在上传中，请在文件上传结束后再操作');
          return;
        }

        o('op').value = 'finish';

        isSpinning.value = true;

        await getFinishActionFree(getFormData())
          .then((res) => {
            isSpinning.value = false;
            if (res.code == 500) {
              createMessage.error(res.msg);
              return;
            }
            // 此处不需要显示操作成功信息，否则可能会与对话框中的信息重复
            // createMessage.success(res.msg);
            showResponse(res);
          })
          .catch((e) => createMessage.warning(e.msg));
      }

      function showResponse(data) {
        if (data == null) {
          createMessage.warning('返回数据为空');
          return;
        }

        if (data.ret == '0') {
          createMessage.error(data.msg);
          return;
        }

        let isDebug = flowProcessData.isDebug;
        var op = data.op;
        if (op === 'read') {
          if (isDebug) {
            createConfirm({
              iconType: 'success',
              title: () => h('span', t('common.prompt')),
              content: () => h('span', data.msg),
              maskClosable: false,
              closable: false,
              cancelText: '',
              onOk: () => {
                go({
                  path: '/flowDebug',
                  query: {
                    myActionId: flowProcessData.myActionId,
                    flowId: flowProcessData.flowId,
                  },
                });
              },
            });
          } else {
            done(data.msg, true, _);
          }
          return;
        } else if (op == 'saveformvalue') {
          createMessage.info(data.msg);
          fileList.value = [];
          reloadAttachment();
          return;
        } else if (op == 'manualFinish' || op == 'manualFinishAgree') {
          done(data.msg, true, _);
        } else if (op == 'finish') {
          // 当流程处理后在form_js_表单编码.jsp文件中调用
          if (typeof onFinishAction === 'function') {
            onFinishAction(op);
          }

          var isCustomRedirectUrl = flowProcessData.isCustomRedirectUrl;
          if (isCustomRedirectUrl) {
            // jAlert_Redirect(data.msg, '[(#{prompt})]', "[(${redirectUrl})]myActionId=[(${myActionId})]");
          } else {
            var nextMyActionId = data.nextMyActionId;
            if (nextMyActionId != '') {
              createConfirm({
                iconType: 'success',
                title: () => h('span', t('common.prompt')),
                content: () => h('span', data.msg),
                maskClosable: false,
                closable: false,
                cancelText: '',
                onOk: async () => {
                  await getFlowProcessContent(nextMyActionId);
                },
              });
            } else {
              console.log('isDebug', isDebug);
              done(data.msg, false, op);
            }
          }
        } else if (op == 'return') {
          done(data.msg, true, '');
        }
      }

      function done(msg, isClose, op) {
        console.log('op', op, 'isClose', isClose);
        if (isClose) {
          createConfirm({
            iconType: 'success',
            title: () => h('span', t('common.prompt')),
            content: () => h('span', msg),
            maskClosable: false,
            closable: false,
            cancelText: '',
            onOk: async () => {
              handleClose();
            },
          });
        } else {
          // 当操作为“提交”时
          if ('finish' == op) {
            var isRecall = flowProcessData.isRecall;
            console.log('isRecall', isRecall);
            // 如果能够撤回
            if (isRecall) {
              msg += ' 如果数据填写有误，请及时撤回';
              createMessage.success(msg);
              // 进入到详情页
              emit('showView', { 'f.id': flowProcessData.flowId });
            } else {
              createMessage.success(msg);
            }
            handleClose();
          }
        }
      }

      //封装基本FormData 可基于返回FormData xx.append()附加参数
      function getFormData() {
        let form = new FormData($('#flowForm')[0]);

        var dropFieldName = null;
        // FormData中只能取到第一个文件，需删除掉，得取dropFiles
        for (var key of form.keys()) {
          // console.log('key:', key, 'value:', typeof form.get(key));
          // 如果是多文件上传控件则删除键值
          if (o(key).type == 'file' && o(key).getAttribute('multiple') != null) {
            form.delete(key);
            dropFieldName = key;
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
          // 取可视化上传控件的图片顺序
          form.append('uploaderImgOrders', getUploaderImgOrders(formRowData.value.formCode));
        } catch (e) {}

        // 加入流程等级、标题等
        for (let v in formRowData.value) {
          form.append(`${v}`, unref(formRowData)[v]);
        }

        // 如果启用了obs上传，则此处不需要再加入上传按钮所选的文件
        if (!isObjStoreEnabled.value) {
          // 加入附件
          if (fileList.value.length > 0) {
            fileList.value.forEach((file: UploadProps['fileList'][number], index) => {
              form.append(`att${index + 1}`, file as any);
            });
          }
        }

        // 加入选择的用户
        for (let index in nextUsers.value) {
          let user = nextUsers.value[index];
          console.log('getFormData user', user);
          form.append('orders', user.order);
          form.append('nextUsers', user.name);
          form.append('expireHours', user.expireHour);
        }

        return form;
      }
      // -----------------------------------------------------表单提交结束-------------------------------------------------------------

      // -----------------------------------------------------退回开始-------------------------------------------------------------
      // 退回
      function returnFlow() {
        returnFlowConfirm();
      }

      // 拒绝流程
      async function disagreeFlow() {
        o('op').value = 'manualFinish';
        const form = getFormData();
        isSpinning.value = true;
        await getFinishActionFree(form).then(async (res) => {
          isSpinning.value = false;
          createMessage.success(res.msg);
          handleClose();
        });
      }

      //退回需选择的用户或节点数据
      const returnFlowVisible = ref(false);
      const importVisible = ref(false);
      const returnResult = ref<any>([]);
      const aryReturnAction = ref([]);
      const returnIdValueRadio = ref('');
      const returnIdValueCheckBox = ref<any>([]);
      let isReturnStyleFree = ref(false);
      async function returnFlowConfirm() {
        let isFlowReturnWithRemark = flowProcessData.isFlowReturnWithRemark;
        if (isFlowReturnWithRemark) {
          if (!formRowData.value.cwsWorkflowResult) {
            createMessage.warning('请输入留言');
            o('cwsWorkflowResult').focus();
            return;
          }
        }

        // 退回时验证数据合法性
        try {
          // 在form_js_formCode.jsp中写此方法
          var r = checkOnReturnBack();
          if (r != '') {
            createMessage.warning(r);
            return;
          }
        } catch (e) {}

        isReturnStyleFree.value = flowProcessData.isReturnStyleFree;
        if (isReturnStyleFree.value) {
          await getReturnAction({
            actionId: unref(formRowData).actionId,
            flowId: unref(formRowData).flowId,
          }).then(async (data) => {
            returnResult.value = data.result || [];

            if (returnResult.value.length === 1) {
              returnIdValueRadio.value = returnResult.value[0].returnId;
            } else {
              returnIdValueRadio.value = '';
            }
          });
        } else {
          returnIdValueCheckBox.value = [];
          aryReturnAction.value = flowProcessData.aryReturnAction || [];
          aryReturnAction.value.forEach((item: any) => {
            returnIdValueCheckBox.value.push(item.actionId);
            item.label = item.actionTitle + '  ' + item.realName;
            item.value = item.actionId;
          });
        }
        returnFlowVisible.value = true;
      }

      //返回最终确认 Modal
      async function handleReturnFlow() {
        o('op').value = 'return';
        if (isReturnStyleFree.value) {
          if (!returnIdValueRadio.value) {
            createMessage.warning('请选择需返回的用户');
            return;
          }
        } else {
          if (returnIdValueCheckBox.value.length == 0) {
            createMessage.warning('请选择需返回的节点');
            return;
          }
        }

        let formData = getFormData();
        if (isReturnStyleFree.value) {
          formData.append('returnId', returnIdValueRadio.value);
        } else {
          for (let v = 0; v < returnIdValueCheckBox.value.length; v++) {
            formData.append('returnId', returnIdValueCheckBox.value[v]);
          }
        }
        isSpinning.value = true;
        await getFinishActionFree(formData)
          .then((res) => {
            isSpinning.value = false;
            createMessage.success(res.msg);
            returnFlowVisible.value = false;
            showResponse(res);
          })
          .catch((e) => createMessage.warning(e.msg));
      }
      // -----------------------------------------------------退回结束-------------------------------------------------------------

      // -----------------------------------------------------文件上传开始-------------------------------------------------------------
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
          slots: { customRender: 'attTitle' },
        },
        {
          title: '创建者',
          dataIndex: 'creatorRealName',
        },
        {
          title: '创建时间',
          dataIndex: 'createDate',
        },
        {
          title: '大小',
          dataIndex: 'size',
        },
      ];
      const [registerTable, { reload: reloadAttachment, setProps }] = useTable({
        title: '', // '附件列表',
        api: getListAttachment,
        columns,
        formConfig: {},
        searchInfo: {}, //额外的参数
        useSearchForm: false,
        showTableSetting: false,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        immediate: false,
        pagination: false,
        canResize: false,
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
      };
      //文件列表删除
      function handleDelete(record) {
        console.log('handleDelete record', record);
        let params = {
          flowId: unref(formRowData).flowId,
          attId: record.id,
        };
        getListAttachmentDelAtt(params).then(() => {
          if (record.fieldName && record.fieldName.length > 0) {
            handleRefresh();
          } else {
            reloadAttachment();
          }
        });
      }

      function handleDownload(record: any) {
        record.isDownloadAtt = true;
        const params = {
          flowId: formRowData.value.flowId,
          attachId: record.id,
        };
        getDownload(params)
          .then((data) => {
            if (data) {
              downloadByData(data, `${record.name}`);
            }
          })
          .finally(() => {
            record.isDownloadAtt = false;
          });
      }

      // -----------------------------------------------------文件列表结束-------------------------------------------------------------
      const onClickMenu: MenuProps['onClick'] = ({ key }) => {
        console.log(`Click on item ${key}`);
        if (key === 'form') {
          htmlPrint('flowForm');
        } else if (key === 'all') {
          htmlPrint('flowFormBox');
        } else {
          openProcessViewDrawerForPrint(unref(formRowData).flowId, key);
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
      // -----------------------------------------------------关联模块处理抽屉开始-------------------------------------------------------------

      const [registerSmartModuleRelateTableDrawer, { openDrawer: openSmartModuleRelateDrawer }] =
        useDrawer();

      let moduleCodeRelated = '';
      function openSmartModuleRelateTableDrawer(type = 1, params: Recordable) {
        console.log('openSmartModuleRelateTableDrawer params', params);
        if (type == 1) {
          console.log('openSmartModuleRelateTableDrawer params', params);
          moduleCodeRelated = params.moduleCodeRelated;
          openSmartModuleDrawer(true, {
            isUpdate: type, // 1 新增，2 编辑，3 详情
            record: {
              ...params,
              actionId: unref(formRowData).actionId,
            },
          });
        } else {
          moduleCodeRelated = params.moduleCodeRelated;
          openSmartModuleDrawer(true, {
            isUpdate: type,
            record: {
              moduleCode: params.moduleCodeRelated,
              id: params.id,

              flowId: unref(formRowData).flowId,
              actionId: unref(formRowData).actionId,
              pageType: params.pageType,
            },
          });
        }
      }

      function smartModuleRelateTableDrawerhandleSuccess({ moduleCodeRelated }) {
        // 刷新嵌套表
        eval('reloadNestSheetCtl' + moduleCodeRelated + '()');
      }

      // -----------------------------------------------------关联模块处理抽屉结束-------------------------------------------------------------

      // -----------------------------------------------------选择模块开始-------------------------------------------------------------

      const [registerSmartModuleSelDrawer, { openDrawer: openSmartModuleSelDrawer }] = useDrawer();

      let myStart = 1;
      function openSmartModuleSelTableDrawer(selMode = 1, start = 1, params: object) {
        console.log('process openModuleSelDrawer');
        myStart = start;
        openSmartModuleSelDrawer(true, {
          selMode, // 1 单选，2 多选
          start, // 1 表单域选择 2 嵌套表格拉单
          record: {
            ...params,
          },
        });
      }

      // 处理表单域选择窗口关闭事件
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
            await filterJS(res.data, 'items', o('flowForm'));

            $('.helper-module-list-sel').remove();
            removeScript('items');
            console.log('params', params);
            try {
              onModuleSelForField(params);
            } catch (e) {}
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
          params.flowId = unref(formRowData).flowId;
          getSelBatchForNest(params).then(async (res) => {
            console.log('handleSmartModuleSelCallBack res', res);
            await filterJS(res.data.script, 'items', o('flowForm'));
            // 刷新嵌套表
            if (params.nestType == 'nest_sheet') {
              eval('reloadNestSheetCtl' + params.moduleCode + '()');
            } else {
              eval('refreshNestTableCtl' + params.nestFieldName + '()');
            }

            removeScript('items');

            console.log('params', params);
            try {
              onModuleSelForNest(params);
            } catch (e) {}
          });
        }
      }

      function clearSmartModuleSel() {
        setInputObjValue('', '');
      }
      // -----------------------------------------------------选择模块结束-------------------------------------------------------------

      // ----------------------------------------------------嵌套表中查看开始-------------------------------------
      const [registerSmartModuleDrawer, { openDrawer: openSmartModuleDrawer }] = useDrawer();
      function openSmartModuleDrawerForShow(moduleCode, id, visitKey) {
        let params = {
          moduleCode,
          id,
          visitKey,
        };
        openSmartModuleDrawer(true, {
          isUpdate: 3,
          record: {
            ...params,
          },
        });
      }

      function openSmartModuleDrawerForEdit(moduleCode, id, visitKey) {
        let params = {
          moduleCode,
          id,
          visitKey,
        };
        openSmartModuleDrawer(true, {
          isUpdate: 2,
          record: {
            ...params,
          },
        });
      }

      function handleSmartModuleCallBack() {
        // 刷新嵌套表
        eval('reloadNestSheetCtl' + moduleCodeRelated + '()');
      }
      // ----------------------------------------------------嵌套表中查看开始结束-------------------------------------

      // ----------------------------------------------------嵌套表中流程查看开始-------------------------------------
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
      // ----------------------------------------------------嵌套表中流程查看结束-------------------------------------

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
      // ----------------------------------------------------视图查看结束-------------------------------------

      // -----------------------------------------------------查看流程图开始-------------------------------------------------------------

      const [registerFlowChartDrawer, { openDrawer: openFlowChartDrawer }] = useDrawer();

      function openFlowChartInfoDrawer() {
        openFlowChartDrawer(true, {
          flowId: unref(formRowData).flowId,
        });
      }
      // -----------------------------------------------------查看流程图结束-------------------------------------------------------------
      // -----------------------------------------------------管理开始-------------------------------------------------------------
      function handleManager() {
        go({
          path: '/managerPage',
          query: {
            urlParams:
              '/admin/flow_predefine_init_myflow.jsp?flowTypeCode=' + flowProcessData.flowTypeCode,
          },
        });
      }
      // -----------------------------------------------------管理结束-------------------------------------------------------------

      function handlePrintBtnClick(viewId) {
        console.log('btn params', viewId);
      }

      // -----------------------------------------------------debug开始-------------------------------------------------------------

      const [registerDebugDrawer, { openDrawer: openDebugDrawer }] = useDrawer();
      function handleDebug() {
        openDebugDrawer(true, {
          isUpdate: false,
          myActionId: formRowData.value.myActionId,
        });
      }

      async function handleRefresh() {
        // 如果不清除content，就会使得不可写字段及可视化上传控件的值被刷新后因脚本再次赋值出现有重复
        // 可能getFlowProcessContent取得的content与之前的一样，所以vue就不去清除content再重新赋予值了
        // 也就是说原来的内容还在，并未被清除
        content.value = '';
        // 删除原来的js脚本，以免重复运行
        removeScript('-src');
        await getFlowProcessContent(unref(formRowData).myActionId);
      }

      // -----------------------------------------------------debug结束-------------------------------------------------------------

      // ---------------------------------------------------------------------------嵌套表导出导入开始-----------------------------------------------------------
      //导出
      async function exportExcelRelate(
        nestType,
        parentId,
        formCode,
        formCodeRelated,
        title = '导出',
      ) {
        let params = {
          nestType,
          parentId,
          moduleCode: formCode,
          moduleCodeRelated: formCodeRelated,
        };
        getVisualExportExcelRelate(params).then((data) => {
          if (data) {
            downloadByData(data, `${title}.xls`);
          }
        });
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
          isImport.value = false;
          createMessage.success('操作成功');
          importVisible.value = false;
          // 刷新嵌套表
          if (excelParams.nestType == 'nest_sheet') {
            // 刷新嵌套表
            eval('reloadNestSheetCtl' + excelParams.moduleCode + '()');
          } else {
            eval('refreshNestTableCtl' + excelParams.nestFieldName + '()');
          }
        });
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

      // ---------------------------------------------------------------------------嵌套表导出导入结束-----------------------------------------------------------

      //关闭抽屉
      function closeCurrentDrawer() {
        emit('success');
        removeLink();
        removeScript(srcId.value);
      }

      //初始化
      watch(
        () => props.myActionId,
        async () => {
          console.log('processHandleFreeView props.myActionId', props.myActionId);
          isSpinning.value = false;
          if (!!props.myActionId) {
            removeScript(srcId.value);
            setTimeout(() => {
              getFlowProcessContent(props.myActionId);
            }, 100);
          }
        },
        {
          // 区别于processHandleView，此处须为true，但却不会两次重复调用getFlowProcessContent
          immediate: true,
        },
      );

      const query = ref<any>({});
      const typeCode = ref<any>(null);
      let isTab = false; // 是否在新选项卡中打开，当在launch.vue中打开时，isTab为true
      onMounted(() => {
        removeScript(srcId.value);
        //debug模式入口
        if (route.query) {
          query.value = route.query;
          isTab = query.value.isTab ? query.value.isTab : false;
        }
        if (query.value.myActionId) {
          getFlowProcessContent(query.value.myActionId);
        }
        if (query.value.isDebug == 'true') {
          userStore.setIsDebug(query.value.isDebug);
        }

        //流程模块入口
        console.log('onmounted route', route);
        console.log('onmounted props.myActionId', props.myActionId);
      });

      onActivated(() => {
        console.log('process free onActivated');
        initWindowFunc();
      });

      function getServerUrl() {
        return userStore.getServerUrl;
      }

      function getPublicPath() {
        const publicPath = import.meta.env.VITE_PUBLIC_PATH || '/';
        return publicPath;
      }

      function getCurFormId() {
        return curFormUtil.get();
      }

      const getFormName = computed(() => 'flowFormFree' + curFormUtil?.getFormNo());

      onUnmounted(() => {
        console.log('Unmounted in processHandleView!');
        curFormUtil?.close(getFormName.value);
      });

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
        selectFiles(
          {
            formName: getFormName.value,
            route: currentRoute.value,
            multiple: multiple,
            filePath,
            fieldName,
            fieldTitle,
            formCode,
            pageType,
            mainId,
            accept,
            maxFileSize: flowProcessData.maxFileSize,            
          },
          (files) => {
            console.log('回调文件', files);
          },
        );
      };

      function initWindowFunc() {
        setTimeout(() => {
          curFormUtil?.set(getFormName.value);
        }, 100);

        let newWindow = window as any;
        newWindow.getCurFormId = getCurFormId;
        newWindow.ajaxPost = ajaxPost;
        newWindow.ajaxGet = ajaxGet;
        newWindow.ajaxGetJS = ajaxGetJS;
        newWindow.filterJS = filterJS;
        newWindow.selectUserInForm = selectUserInForm;
        newWindow.openSmartModuleRelateTableDrawer = openSmartModuleRelateTableDrawer;
        newWindow.myConfirm = myConfirm;
        newWindow.myMsg = myMsg;
        console.log('init process openSmartModuleSelTableDrawer');
        newWindow.openSmartModuleSelTableDrawer = openSmartModuleSelTableDrawer;
        newWindow.submitMyFile = submitMyFile;
        newWindow.openSmartModuleDrawerForShow = openSmartModuleDrawerForShow;
        newWindow.openProcessShowDrawerForShow = openProcessShowDrawerForShow;
        newWindow.exportExcelRelate = exportExcelRelate;
        newWindow.openSmartModuleDrawerForEdit = openSmartModuleDrawerForEdit;
        newWindow.openImportExcelModal = openImportExcelModal;
        newWindow.getToken = getToken;
        newWindow.getServerUrl = getServerUrl;
        newWindow.ajaxPostJson = ajaxPostJson;
        newWindow.downloadFile = downloadFile;
        newWindow.reloadAttachment = reloadAttachment;
        newWindow.getPublicPath = getPublicPath;
        newWindow.openInputPwdModal = openInputPwdModal;
        newWindow.openWritePadModal = openWritePadModal;
        newWindow.openSelStampModal = openSelStampModal;
        newWindow.initFormCtl = initFormCtl;

        newWindow.selectFiles = handleSelectFiles;
        newWindow.getServerInfo = getServerInfo;
        newWindow.deleteByField = deleteByField;
        newWindow.downloadObsFile = handleDownloadObsFile;
        newWindow.setUploadFileTreeListAttInfo = setUploadFileTreeListAttInfo;
      }

      const [registerModal, { openModal }] = useModal();

      //debug回调
      function handleDeBugCallBack() {
        getFlowProcessContent(formRowData.value.myActionId);
      }

      function delUser(name) {
        nextUsers.value = nextUsers.value.filter((item) => item['name'] != name);
      }

      // -----------------------------------------------------处理过程开始-------------------------------------------------------------
      const handleColumns: BasicColumn[] = [
        {
          title: '处理人',
          dataIndex: 'userRealName',
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
        {
          title: '发起时间',
          dataIndex: 'receiveDate',
          align: 'center',
        },
        {
          title: '处理时间',
          dataIndex: 'checkDate',
          align: 'center',
        },
        {
          title: '用时(小时)',
          dataIndex: 'workDuration',
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
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        immediate: false,
        pagination: false,
        canResize: false,
        /* actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: 'right',
        }, */
      });

      //导入modal取消
      function handleCancel() {
        importVisible.value = false;
      }

      const [registerInputPwdModal, { openModal: openInPwdModal }] = useModal();
      const openInputPwdModal = () => {
        openInPwdModal(true, {});
      };

      async function handleInputPwdCallBack(record) {
        setInputObjValue(record.realName + '  ' + record.date);
      }

      const [registerWritePadModal, { openModal: openWPadModal }] = useModal();
      const openWritePadModal = (fieldName, w, h) => {
        openWPadModal(true, { fieldName, w, h });
      };

      async function handleWritePadCallBack() {}

      const [registerSelStampModal, { openModal: openSelectStampModal }] = useModal();
      const handleSelStamp = (record) => {
        openSelStampModal(record);
      };

      const openSelStampModal = (record) => {
        if (record) {
          openSelectStampModal(true, {
            op: 'getStampForAtt',
            ...record,
          });
        } else {
          openSelectStampModal(true, {
            op: 'getStampForForm',
          });
        }
      };

      async function handleSelStampCallBack(record) {
        console.log('handleSelStampCallBack', record);
        if (record.op == 'getStampForAtt') {
          isSpinning.value = true;
          sealDocument({
            flowId: formRowData.value.flowId,
            stampId: record.stampId,
            attachId: record.attachId,
          })
            .then((data) => {
              if (data.res == 0) {
                createMessage.success('操作成功');
              }
            })
            .finally(() => {
              isSpinning.value = false;
            });
        } else {
          await getShowImg({ path: record['imageUrl'] }).then(async (res) => {
            let imageUrl = bufToUrl(res);
            insertSignImg(record.stampId, imageUrl);
          });
        }
      }

      const [registerTipPhraseModal, { openModal: openTPhraseModal }] = useModal();
      const openTipPhraseModal = () => {
        openTPhraseModal(true, {});
      };

      async function handleTipPhraseCallBack() {
        $('.phraseIcon').html('');
        fetchPhrases();
      }

      function removePhrase(id) {
        createConfirm({
          iconType: 'info',
          title: () => h('span', t('common.prompt')),
          content: () => h('span', '您确定要删除么'),
          onOk: async () => {
            await delPhrase({ id: id }).then((data) => {
              if (data.res == 0) {
                createMessage.success(t('common.opSuccess'));
                handleTipPhraseCallBack();
              }
            });
          },
        });
      }

      function handleClose() {
        emit('closeDrawer');
        emit('success');
        if (isTab) {
          closeCurrent();
        }
      }

      const handleDownloadObsFile = (key) => {
        // getObjectByKey('upfile/visual/personbasic/2023/8/4dbc0ea68968427cb.doc');
        getObjectByKey(key);
      };

      let fileNo = 0;
      //获取文件change事件
      const handleFilesChange = (info) => {
        console.log('handleFilesChange info', info);
        console.log('handleFilesChange flowProcessData.validExt', flowProcessData.validExt);
        const { file } = info;

        let fileName = file.name;
        let ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (flowProcessData.validExt && flowProcessData.validExt.length > 0) {
          let isExtValid = flowProcessData.validExt.some((item) => item == ext);
          if (!isExtValid) {
            createMessage.warn('文件 ' + file.name + ' 类型非法');
            return;
          }
        }

        let maxFileSize = flowProcessData.maxFileSize;
        if (file.size > maxFileSize * 1024) {
          createMessage.warn('文件: ' + file.name + ' 大小超过了 ' + (maxFileSize / 1024) + 'M');
          return;
        }

        uploadFileObjectFunc({
          files: [file],
          formName: getFormName.value,
          route: currentRoute.value,
          fieldName: 'att' + fileNo,
          filePath: flowProcessData.visualPath,
          formCode: formRowData.value.formCode,
          pageType: 'flow',
          mainId: formRowData.value.flowId,
          fieldTitle: '附件',
        });

        fileNo++;
      };

      const handleRemoveFile = (file, actions) => {
        console.log('handleRemoveFile', file);
        let treeList = uploadFileStore.getUploadFileTreeList;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('handleRemoveFile it', it);
            if (it.file.uid === file.uid) {
              if (it.progress == 100) {
                let params = {
                  flowId: formRowData.value.flowId,
                  attachId: it.attId,
                };
                getDelAttach(params).then(() => {
                  deleteByUid(file.uid);

                  actions.remove();
                  reloadAttachment();
                });
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
        console.log('getProgress file', file);
        let treeList = uploadFileStore.getUploadFileTreeList;
        let progress = 100;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('getProgress it', it);
            if (it.file.uid === file.uid) {
              progress = it.progress;
              if (progress == 100) {
                // 此处不能调用reloadAttachment，因为progress有可能会多次被computed检测到变化，即便已经为100，而且在putUploadProgress中已经调用过了
                // reloadAttachment();
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
            console.log('getAttVisible it', it);
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
            console.log('getAttUploadFinishedIsImg it', it);
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
            console.log('handleShowImgAtt it', it);
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
            console.log('getAttUploadFinishedCanPreview it', it);
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
            console.log('getAttUploadFinished it', it);
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
            console.log('handlePreviewAtt it', it);
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
            console.log('handleDownloadAtt it', it);
            if (it.file.uid === file.uid) {
              key = it.fieldValue;
              handleDownloadObsFile(key);
            }
          });
        });
      });

      return {
        isSpinning,
        selectDeptCode,
        content,
        common,
        cwsId,
        saveDraft,
        fileList,
        beforeUpload,
        handleRemove,
        registerTable,
        handleDelete,
        handleDownload,
        formRowData,
        registerModal,
        selectUser,
        handleCallBack,
        selectUserInForm,
        toolbarSubmit,
        returnFlow,
        returnFlowVisible,
        handleReturnFlow,
        returnResult,
        returnIdValueRadio,
        aryReturnAction,
        returnIdValueCheckBox,
        isReturnStyleFree,
        closeCurrentDrawer,
        registerSmartModuleRelateTableDrawer,
        smartModuleRelateTableDrawerhandleSuccess,
        registerSmartModuleSelDrawer,
        handleSmartModuleSelCallBack,
        clearSmartModuleSel,
        registerFlowChartDrawer,
        openFlowChartInfoDrawer,
        handleManager,
        handleDebug,
        registerDebugDrawer,
        registerSmartModuleDrawer,
        handleSmartModuleCallBack,
        openSmartModuleDrawerForShow,
        registerProcessShowDrawer,
        handleProcessShowCallBack,
        handleProcessViewCallBack,
        handleDeBugCallBack,
        registerHandleTable,
        t,
        rawFileFn,
        importVisible,
        isImport,
        isDownload,
        handleDownloadExcelTempl,
        handleCancel,
        onClickMenu,
        handlePrintBtnClick,
        registerProcessViewDrawer,
        btnAgreeName,
        btnReturnName,
        btnDisagreeName,
        disagreeFlow,
        typeCode,
        isDownloadAtt,
        initWindowFunc,
        handleRefresh,
        flowIsRemarkShow,
        nextUsers,
        delUser,
        fileDataSource,
        registerInputPwdModal,
        handleInputPwdCallBack,
        registerWritePadModal,
        handleWritePadCallBack,
        registerSelStampModal,
        handleSelStampCallBack,
        registerTipPhraseModal,
        handleTipPhraseCallBack,
        openTipPhraseModal,
        getFormName,
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
<style scoped>
  @import '../../../assets/css/css.css';
  /* @import '@mdi/font/css/materialdesignicons.css'; 无效*/
  :deep(.ant-form-item) {
    margin-bottom: 5px;
  }
  :deep(.vben-basic-table .ant-table-wrapper) {
    padding-left: 0;
  }
</style>
