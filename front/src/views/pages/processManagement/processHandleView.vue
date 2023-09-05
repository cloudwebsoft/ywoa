<template>
  <div class="bg-white pt-1 pl-2 pr-2 h-full" :class="{ 'pt-2': !!typeCode }">
    <Spin :spinning="isSpinning" style="margin-top: 50px">
      <div class="toolbar-wrap" v-show="isLoaded || isLaunched">
        <div class="toolbar" :style="[{ marginTop: isInDrawer ? '-20px' : '-8px' }]">
          <Row>
            <Button type="primary" size="small" @click="saveDraft" :disabled="isDoLoading">
              <template #icon><SaveOutlined /></template>
              保存
            </Button>
            <Button
              type="primary"
              size="small"
              class="ml-2"
              :disabled="isDoLoading"
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
              :disabled="isDoLoading"
              @click="handleCountersign"
              v-if="formRowData.aryButton.some((item) => item.name === 'plus') && !isPlusDescShow"
            >
              <template #icon><Icon icon="ant-design:user-add-outlined" /></template>
              加签
            </Button>
            <Button
              type="primary"
              size="small"
              class="ml-2"
              :disabled="isDoLoading"
              @click="returnFlow"
              v-if="formRowData.aryButton.some((item) => item.name === 'return')"
            >
              <template #icon><RollbackOutlined /></template>
              {{ btnReturnName }}
            </Button>
            <Popconfirm
              placement="top"
              title="确定删除吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="delFlow"
            >
              <Button
                type="primary"
                size="small"
                class="ml-2"
                :disabled="isDoLoading"
                v-if="formRowData.aryButton.some((item) => item.name === 'del')"
              >
                <template #icon><DeleteOutlined /></template>
                删除
              </Button>
            </Popconfirm>

            <Button
              type="primary"
              size="small"
              class="ml-2"
              :disabled="isDoLoading"
              @click="handleDistribute"
              v-if="formRowData.aryButton.some((item) => item.name === 'distribute')"
            >
              <template #icon><Icon icon="clarity:deploy-line" /></template>
              抄送
            </Button>

            <Popconfirm
              placement="top"
              title="确定要直送给返回者吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleToRetuner"
            >
              <Button
                type="primary"
                size="small"
                class="ml-2"
                :disabled="isDoLoading"
                v-if="formRowData.aryButton.some((item) => item.name === 'toRetuner')"
              >
                <template #icon><SelectOutlined /></template>
                直送
              </Button>
            </Popconfirm>

            <Popconfirm
              placement="top"
              title="确定要挂起吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleSuspend"
            >
              <Button
                type="primary"
                size="small"
                class="ml-2"
                :disabled="isDoLoading"
                v-if="formRowData.aryButton.some((item) => item.name === 'suspend')"
              >
                <template #icon><PauseCircleOutlined /></template>
                挂起
              </Button>
            </Popconfirm>

            <Popconfirm
              placement="top"
              title="确定要恢复吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleResume"
            >
              <Button
                type="primary"
                size="small"
                class="ml-2"
                :disabled="isDoLoading"
                v-if="formRowData.aryButton.some((item) => item.name === 'resume')"
              >
                <template #icon><RightCircleOutlined /></template>
                恢复
              </Button>
            </Popconfirm>

            <Popconfirm
              placement="top"
              title="确定放弃吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="discardFlow"
            >
              <Button
                type="primary"
                size="small"
                class="ml-2"
                :disabled="isDoLoading"
                v-if="formRowData.aryButton.some((item) => item.name === 'discard')"
              >
                <template #icon><ClearOutlined /></template>
                放弃
              </Button>
            </Popconfirm>
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
                :disabled="isDoLoading"
                v-if="formRowData.aryButton.some((item) => item.name === 'disagree')"
              >
                <template #icon><CloseCircleOutlined /></template>
                {{ btnDisagreeName }}
              </Button>
            </Popconfirm>

            <Popconfirm
              placement="top"
              title="确定同意并结束吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="finishAgreeFlow"
            >
              <Button
                type="primary"
                size="small"
                class="ml-2"
                title="同意并结束"
                :disabled="isDoLoading"
                v-if="formRowData.aryButton.some((item) => item.name === 'finishAgree')"
              >
                <template #icon><CheckSquareOutlined /></template>
                结束
              </Button>
            </Popconfirm>

            <Button
              type="primary"
              size="small"
              class="ml-2"
              :disabled="isDoLoading"
              @click="openFlowChartInfoDrawer"
              v-if="formRowData.aryButton.some((item) => item.name === 'chart')"
            >
              <template #icon><PartitionOutlined /></template>
              流程图
            </Button>
            <!-- <Button type="primary" size="small" class="ml-2">
          <template #icon><HeartOutlined /></template>
          关注
        </Button>
         -->

            <Dropdown>
              <Button
                type="primary"
                size="small"
                class="ml-2"
                v-show="false"
                :disabled="isDoLoading"
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
              @click="handleDebug"
              v-if="formRowData.aryButton.some((item) => item.name === 'debug')"
            >
              <template #icon><Icon icon="clarity:bug-line" /></template>
              调试
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
          <Row
            class="h-8 flex justify-items-center items-center"
            v-for="(action, index) in toActions"
            :key="index"
            v-show="!formRowData.isNotShowNextUsers"
            style="background-color: white"
          >
            <Col :span="2">
              <span style="color:{{ action.color }}">{{ action.title }}</span
              >：</Col
            >
            <Col :span="22">
              <div class="flex">
                <Checkbox
                  :id="`XOR${action.id}`"
                  name="XorActionSelected"
                  :value="action.internalName"
                  style="display: none"
                  :checked="action.xorChecked"
                  v-if="action.hasOwnProperty('XorActionSelected')"
                />
                <span v-for="(checker, indexChecker) in action.checkers" :key="indexChecker">
                  <Checkbox
                    :name="`WorkflowAction_${action.id}`"
                    v-model:checked="checker.checked"
                    :disabled="checker.disabled"
                    @click="checkXOR(action.id, checker, index)"
                    :value="checker.value"
                  >
                    {{ checker.realName }}
                  </Checkbox>
                </span>
                <!-- :disabled="checker.disabled" -->
                <!-- <CheckboxGroup
            :name="`WorkflowAction_${action.id}`"
            :options="action.checkers"
            v-model:value="action.selectUserNames"
          /> -->
                <!-- <Checkbox v-model:checked="checked">Checkbox</Checkbox> -->
                <Button
                  type="primary"
                  size="small"
                  class="ml-2"
                  @click="selectUser(action, index, action.isBtnXor)"
                  v-if="action.isBtnSelUser"
                >
                  选择
                </Button>
                <span v-if="action.hasOwnProperty('expireHour')" class="pl-2"
                  >完成时间：{{ action.expireHour }}{{ action.expireUnit }}
                </span>
              </div>
            </Col>
          </Row>
          <Row
            class="flex items-center"
            v-if="matchJson.hasOwnProperty('isMatchUserException') && matchJson.isMultiDept"
            style="background-color: white"
          >
            <Col :span="2"> 请选择部门 </Col>
            <Col :span="22">
              <RadioGroup
                v-model:value="selectDeptCode"
                name="deptOfUserWithMultiDept"
                @change="onSelDept"
              >
                <Radio
                  :value="dept.deptCode"
                  v-for="(dept, indexDept) in matchJson.multiDepts"
                  :key="indexDept"
                >
                  {{ dept.deptName }}
                </Radio>
              </RadioGroup>
              <!-- <div class="flex">
          <span v-for="(dept, indexDept) in matchJson.multiDepts" :key="indexDept">
            <Radio
              name="deptOfUserWithMultiDept"
              :value="dept.deptCode"
              @click="onSelDept(dept.deptCode)"
            >
              {{ dept.deptName }}
            </Radio>
          </span>
        </div> -->
            </Col>
          </Row>
          <Row v-if="formRowData.isPlus" class="m-2" style="background-color: white"
            ><Col :span="24"> 您正在进行加签</Col></Row
          >
          <Row v-show="isPlusDescShow" class="m-2" style="background-color: white"
            ><Col :span="24"
              >{{ formRowData.plusDesc }}
              <Popconfirm
                placement="top"
                title="确定取消加签么？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="delPlus"
              >
                <Icon
                  icon="clarity:times-line"
                  class="cursor-pointer"
                  color="red"
                  title="删除加签" /></Popconfirm></Col
          ></Row>
          <Row class="flex items-center" style="background-color: white">
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
                  <Col :span="6">
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
        </div>
      </div>
      <div
        v-show="isLoaded"
        class="border-1 border-solid border-gray-80 min-h-full"
        id="flowFormBox"
      >
        <!-- :style="[{ overflowX: formRowData.isDebug ? 'auto' : 'hidden' }]" -->
        <form
          id="flowForm"
          :name="getFormName"
          :formCode="formRowData ? formRowData.formCode : ''"
          :style="[{ overflowX: isTab ? 'auto' : 'hidden' }]"
        >
          <div id="formQueryBox" class="mt-2" v-show="formRowData.canQuery"></div>
          <input type="hidden" id="op" name="op" value="saveformvalue" />
          <input type="hidden" name="isAfterSaveformvalueBeforeXorCondSelect" />
          <input type="hidden" name="cws_id" :value="cwsId" />
          <input type="hidden" name="flowAction" />
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div id="contentBox" v-html="content"></div>
        </form>
        <!--意见输入框常用语提示框-->
        <div id="phraseBox">
          <div class="phraseCnt">
            <div class="phraseClose" onclick="closeTipPhrase()">×</div>
            <div class="phraseAdd" @click="openTipPhraseModal">+</div>
            <div class="phraseIcon" style="clear: both"> </div>
          </div>
        </div>
        <Row>
          <template v-if="!isObjStoreEnabled">
            <Upload
              :file-list="fileList"
              :before-upload="beforeUpload"
              @remove="handleRemove"
              v-show="formRowData.isHasAttachment && isLoaded"
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
              v-show="formRowData.isHasAttachment && isLoaded"
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
                          v-if="getAttUploadFinished(file)"
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
        <BasicTable @register="registerTable" v-show="aryAttSize > 0">
          <template #attTitle="{ record }">
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
                },
                {
                  icon: 'ant-design:pic-center-outlined',
                  tooltip: '套红',
                  color: 'error',
                  onClick: handleSelTemplate.bind(null, record),
                  ifShow: record.canDocInRed,
                  loading: record.isDoDocInRed,
                },
                {
                  icon: 'ant-design:file-done-outlined',
                  tooltip: '盖章',
                  color: 'error',
                  onClick: handleSelStamp.bind(null, record),
                  ifShow: record.canSeal,
                  loading: record.isSeal ? true : false,
                },
              ]"
            />
          </template>
        </BasicTable>
        <BasicTable @register="registerHandleTable" v-show="isLoaded" />
      </div>
      <!-- 回复 -->
      <div class="mt-2 mb-2">
        <ReplyView
          v-model:flowInfo="formRowData"
          v-if="formRowData.isReply && formRowData.isFlowStarted"
        />
      </div>

      <Modal
        v-model:visible="returnFlowVisible"
        title="选择"
        @ok="handleReturnFlow"
        :confirmLoading="isReturning"
      >
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
          <RadioGroup
            name="CheckboxGroup"
            :options="aryReturnAction"
            v-model:value="returnIdValueRadio"
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
              <ImpExcel :isFiles="false" @raw-file="rawFileFn" dateFormat="YYYY-MM-DD">
                <a-button type="primary" class="mr-1" :loading="isImport"> 上传文件 </a-button>
              </ImpExcel>
            </Col>
          </Row>
        </div>
      </Modal>

      <SelectUser @register="registerModal" @success="handleCallBack" />
      <MatchBranchAndUserModal
        @register="matchJsonRegisterModal"
        @success="handleMatchJsonCallBack"
      />
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
      <DebugDrawer @register="registerDebugDrawer" @success="handleDeBugCallBack" />
      <CountersignModal @register="registerCountersignModal" @success="handleCountersignCallBack" />
      <DistributeModal @register="registerDistributeModal" @success="handleDistributeCallBack" />
      <SelTemplateModal @register="registerSelTemplateModal" @success="handleSelTemplateCallBack" />
      <SelStampModal @register="registerSelStampModal" @success="handleSelStampCallBack" />
      <InputPwdModal @register="registerInputPwdModal" @success="handleInputPwdCallBack" />
      <WritePadModal @register="registerWritePadModal" @success="handleWritePadCallBack" />
      <TipPhraseModal @register="registerTipPhraseModal" @success="handleTipPhraseCallBack" />
      <SelDeptModal @register="registerSelDeptModal" @success="handleSelDeptCallBack" />
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
    unref,
    onMounted,
    h,
    watch,
    inject,
    nextTick,
    onActivated,
    onDeactivated,
    onUnmounted,
    computed,
    defineAsyncComponent,
  } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ImpExcel } from '/@/components/Excel';
  import { useDrawer } from '/@/components/Drawer';
  import SmartModuleSelDrawer from '../smartModule/modules/smartModuleSelDrawer.vue';
  // import SmartModuleDrawer from '../smartModule/modules/smartModuleDrawer.vue';
  import ProcessShowDrawer from './processShowDrawer.vue';
  import ProcessViewDrawer from './processViewDrawer.vue';
  import ReplyView from './modules/ReplyView.vue';
  import { getIsDebug, getToken } from '/@/utils/auth';
  import { getShowImg } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { getVisualDownload } from '/@/api/module/module';
  import {
    selectFiles,
    deleteByField,
    isUploadFinished,
    getObjectByKey,
    uploadFileObjectFunc,
    deleteByUid,
  } from '/@/utils/uploadFile';
  import { useUploadFileStore } from '/@/store/modules/uploadFile';

  import {
    getFlowProcess,
    getFlowProcessScript,
    getFinishAction,
    getListAttachment,
    getListAttachmentDelAtt,
    submitMyFile,
    getDiscardFlow,
    getSuspendFlow,
    getResumeFlow,
    getDelFlow,
    getReturnAction,
    getMatchBranchAndUser,
    getFlowInit,
    getDownload,
    downloadFile,
    getDelPlus,
    convertToRedDocument,
    sealDocument,
    getPhrases,
    delPhrase,
    getDelAttach,
  } from '/@/api/process/process';
  import { useMultipleTabWithOutStore } from '/@/store/modules/multipleTab';

  import {
    SaveOutlined,
    SettingOutlined,
    // HeartOutlined,
    PartitionOutlined,
    PrinterOutlined,
    DeleteOutlined,
    UploadOutlined,
    SelectOutlined,
    RightCircleOutlined,
    PauseCircleOutlined,
    RollbackOutlined,
    ClearOutlined,
    CloseCircleOutlined,
    CheckSquareOutlined,
    ProfileOutlined,
    PictureOutlined,
    DownloadOutlined,
  } from '@ant-design/icons-vue';
  import {
    Button,
    CheckboxGroup,
    Row,
    Col,
    Checkbox,
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
  import MatchBranchAndUserModal from './modules/MatchBranchAndUserModal.vue';
  import SmartModuleRelateTableDrawer from '../smartModule/modules/smartModuleRelateTableDrawer.vue'; //选择模块
  import FlowChartDrawer from './flowChartDrawer.vue';
  import CountersignModal from './modules/CountersignModal.vue';
  import DistributeModal from './modules/DistributeModal.vue';
  import SelTemplateModal from './modules/SelTemplateModal.vue';
  import SelStampModal from './modules/SelStampModal.vue';
  import InputPwdModal from './modules/InputPwdModal.vue';
  import WritePadModal from './modules/WritePadModal.vue';
  import TipPhraseModal from './modules/TipPhraseModal.vue';
  import SelDeptModal from './modules/SelDeptModal.vue';
  import LocationMarkModal from './modules/LocationMarkModal.vue';
  import { useGo } from '/@/hooks/web/usePage';
  import CurFormUtil from '/@/utils/form';
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
    o,
    getServerInfo,
    isImage,
  } from '/@/utils/utils';
  import DebugDrawer from './modules/debugDrawer.vue';
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
  import { useTabs } from '/@/hooks/web/useTabs';
  import { useRouter } from 'vue-router';

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
    isNotShowNextUsers: boolean; // 是否不显示下一节点上的审核人
    isFlowManager: boolean; // 是否流程管理员
    isReply: boolean; // 能否回复
    flowStatus: number; // 流程状态
    canQuery: boolean; // 能否查询
    isDebug: boolean; // 是否为调试模式
  }

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'processHandleView',
    components: {
      Button,
      SaveOutlined,
      SettingOutlined,
      // HeartOutlined,
      PartitionOutlined,
      PrinterOutlined,
      DeleteOutlined,
      SelectOutlined,
      RightCircleOutlined,
      PauseCircleOutlined,
      RollbackOutlined,
      ClearOutlined,
      CloseCircleOutlined,
      CheckSquareOutlined,
      CheckboxGroup,
      Row,
      Col,
      Checkbox,
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
      MatchBranchAndUserModal,
      SmartModuleRelateTableDrawer,
      SmartModuleSelDrawer,
      FlowChartDrawer,
      Spin,
      DebugDrawer,
      SmartModuleDrawer: defineAsyncComponent(
        () => import('../smartModule/modules/SmartModuleDrawer.vue'),
      ),
      ProcessShowDrawer,
      ImpExcel,
      Popconfirm,
      Dropdown,
      Menu,
      MenuItem,
      ProcessViewDrawer,
      CountersignModal,
      ReplyView,
      DistributeModal,
      SelTemplateModal,
      SelStampModal,
      InputPwdModal,
      WritePadModal,
      TipPhraseModal,
      SelDeptModal,
      LocationMarkModal,
      Progress,
      ProfileOutlined,
      PictureOutlined,
      DownloadOutlined,
      Tooltip,
    },
    props: {
      myActionId: {
        type: Number,
        default: 0,
      },
      isInDrawer: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['success', 'register', 'showView', 'closeDrawer'],
    setup(props, { emit }) {
      const { createMessage, createConfirm, createSuccessModal, createErrorModal } = useMessage();
      const isSpinning = ref(false);
      const content = ref('');
      const common = ref('');
      const srcId = ref('-src');
      const toActions = ref<any>([]);
      const matchJson = ref<any>({});
      const selectDeptCode = ref('');
      const { t } = useI18n();
      const route = useRoute();
      const go = useGo();
      const userStore = useUserStore();
      const cwsId = ref(0);
      const flowIsRemarkShow = ref(false);
      const isPlusDescShow = ref(false);
      const { setTitle, closeCurrent } = useTabs();
      const fileList = ref<UploadProps['fileList']>([]);

      const curFormUtil: CurFormUtil | undefined = inject('curFormUtil');
      const { currentRoute, replace, push } = useRouter();
      const multipleTabStore = useMultipleTabWithOutStore();

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
        isNotShowNextUsers: false,
        isFlowManager: false,
        isReply: false,
        flowStatus: 1, // 流程状态，默认为处理中
        canQuery: false,
        isDebug: false,
      });

      // 导入
      const isImport = ref(false);
      const isDownload = ref(false);
      const btnAgreeName = ref('提交');
      const btnReturnName = ref('退回');
      const btnDisagreeName = ref('拒绝');
      const aryAttSize = ref(0);

      // 发起流程步骤是否正在加载中，为true后使工具条显示，用以加快显示速度，提升用户体验
      const isLaunched = ref(false);
      // 处理流程步骤是否正在加载中，为true后使工具条按钮由灰色变为蓝色，可点击
      const isDoLoading = ref(false);
      // 处理流程步骤的获取流程信息部分（除去filterJS等外）是否已加载，为true后显示全部内容
      const isLoaded = ref(false);
      const serverInfo = userStore.getServerInfo;
      const isObjStoreEnabled = ref(serverInfo.isObjStoreEnabled);

      // -----------------------------------------------------获取当前流程信息开始-------------------------------------------------------------
      //获取当前流程信息
      let flowProcessData = {} as any;
      async function getFlowProcessContent(myActionId: number) {
        isDoLoading.value = true;
        console.log('myActionId', myActionId);
        initWindowFunc();

        isSpinning.value = true;
        let data = await getFlowProcess({ myActionId: myActionId, cwsFormName: getFormName.value });
        isSpinning.value = false;

        flowProcessData = data;
        aryAttSize.value = data.aryAttSize;
        // common.value = data.script;
        selectDeptCode.value = '';

        // 页面风格
        let cont = data.content;
        let isPageStyleLight = data.isPageStyleLight;
        if (isPageStyleLight) {
          cont = cont.replace('tabStyle_8', 'tabStyle_1');
        }
        content.value = cont;

        cwsId.value = data.cws_id;
        flowIsRemarkShow.value = data.flowIsRemarkShow;

        if (flowProcessData.aryMyAction && flowProcessData.aryMyAction.length > 0) {
          setTableData(flowProcessData.aryMyAction);
        } else {
          setTableData([]);
        }

        const newMatchJson = data.matchJson || {};
        console.log('getFlowProcessContent data', data);
        handleMatchJson(newMatchJson);

        // 当actions有多行时，自适应toolbar-wrap高度
        console.log('toActions.value.length', toActions.value.length);
        if (toActions.value.length > 1) {
          $('.toolbar-wrap').css('height', 70 + toActions.value.length * 40 + 'px');
        } else {
          $('.toolbar-wrap').css('height', '100px');
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
          isNotShowNextUsers: data.isNotShowNextUsers,
          isFlowManager: data.isFlowManager,
          isReply: data.isReply,
          flowStatus: data.flowStatus,
          canQuery: data.canQuery,
          isDebug: data.isDebug,
        };
        console.log('formRowData', unref(formRowData));

        if (isTab.value) {
          let title = data.flowId + '-' + data.flowTitle;
          if (title.length > 18) {
            title = title.substring(0, 18) + '...';
          }
          setTitle(title);
        }

        // 如果是我做的加签，且不是前加签，且加签描述不为空
        isPlusDescShow.value =
          formRowData.value.isMyPlus &&
          !formRowData.value.isPlusBefore &&
          formRowData.value.plusDesc.length > 0;

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

        if (unref(formRowData).isHasAttachment) {
          // 如果附件列表大于0，则置props及刷新附件列表
          setProps({
            searchInfo: {
              flowId: unref(formRowData).flowId,
              actionId: unref(formRowData).actionId,
            },
          });
          reloadAttachment();
        }

        await nextTick();

        if (!isCached) {
          console.log('processHandleView isCached', isCached);
          return;
        }

        // 加快显示
        isLoaded.value = true;

        initContent();
      }

      async function initContent() {
        console.log('initContent start');

        // 防止切换太快，有时内容不能显示
        let cont = flowProcessData.content;
        let isPageStyleLight = flowProcessData.isPageStyleLight;
        if (isPageStyleLight) {
          cont = cont.replace('tabStyle_8', 'tabStyle_1');
        }
        content.value = cont;

        // 加快显示，故把isLoaded置为true，原来在filterJS之后提前到这里
        isLoaded.value = true;

        let rootPath = import.meta.env.VITE_PUBLIC_PATH;
        if (rootPath.endsWith('/')) {
          rootPath = rootPath.substring(0, rootPath.lastIndexOf('/'));
        }

        const url = `${rootPath}/resource/js/form/form_js_${flowProcessData.formCode}.js?pageType=flow&flowId=${flowProcessData.flowId}&myActionId=${flowProcessData.myActionId}&cwsFormName=${getFormName.value}`;
        console.log('form_js url', url);
        let script1 = document.createElement('script');
        script1.type = 'text/javascript';
        script1.src = url;
        script1.id = `${100}${srcId.value}`;
        document.getElementsByTagName('head')[0].appendChild(script1);

        // 加载后台事件中配置的前台脚本
        if (flowProcessData.formJs && flowProcessData.formJs.length > 0) {
          let scriptFormJs = document.createElement('script');
          scriptFormJs.type = 'text/javascript';
          scriptFormJs.text = flowProcessData.formJs;
          scriptFormJs.id = `${101}${srcId.value}`;
          document.getElementsByTagName('head')[0].appendChild(scriptFormJs);
        }

        // 初始化查询侦听
        putParamInFormMap(getFormName.value, 'flowTypeCode', flowProcessData.flowTypeCode);
        putParamInFormMap(getFormName.value, 'queryId', flowProcessData.queryId);
        putParamInFormMap(getFormName.value, 'queryAjaxUrl', flowProcessData.queryAjaxUrl);
        putParamInFormMap(getFormName.value, 'queryCond', flowProcessData.queryCond);
        onQueryRelateFieldChange(getFormName.value);
        initQueryCondListener(getFormName.value);

        await nextTick();

        initFormCtl(getFormName.value);

        // 为向下兼容，引入服务端form_js
        await ajaxGetJS(
          `/flow/form_js/form_js_${flowProcessData.formCode}.jsp?pageType=flow&flowId=${flowProcessData.flowId}&myActionId=${flowProcessData.myActionId}&cwsFormName=${getFormName.value}`,
        );

        // 异步获取显示规则等脚本
        getFlowProcessScript({
          actionId: formRowData.value.actionId,
          cwsFormName: getFormName.value,
        }).then((res) => {
          console.log('getFlowProcessScript filterJS');
          filterJS(res.script, srcId.value, o(getFormName.value));
        });

        console.log('initContent filterJS');
        await filterJS(flowProcessData.content, srcId.value, o(getFormName.value));

        setTimeout(() => {
          // 初始化日期控件
          initDatePicker();

          // 初始化计算控件
          initCalculator();

          loadImg('flowForm');

          // 初始化表单中指定的人员和限定部门字段的侦听，不能用change事件，因为可能为表单域选择宏控件映射的字段
          let bindFieldList: string[] = flowProcessData.bindFieldList;
          let bindFieldListOldVal: Array<string> = new Array<string>();
          for (let k in bindFieldList) {
            let fieldName = bindFieldList[k];
            bindFieldListOldVal[k] = '';
            curFormUtil?.setInterval(function () {
              // console.log('fo(' + fieldName + ')', fo(fieldName));
              if (
                fo(fieldName, getFormName.value) &&
                bindFieldListOldVal[k] != fo(fieldName, getFormName.value).value
              ) {
                reMatchUser(fieldName, fo(fieldName, getFormName.value).value);
                bindFieldListOldVal[k] = fo(fieldName, getFormName.value).value;
              }
            }, 500);
          }
        }, 100);

        fetchPhrases();

        // isDoLoading.value = false;

        // 使最长3秒，顶部的提交按钮可用，以免网络不佳的时候使得onRendEnd延迟
        setTimeout(() => {
          isDoLoading.value = false;
        }, 3000);
      }

      // 强制在渲染的脚本执行完后，使工具条按钮可用，以免中间有脚本错误，显示不全，但仍能提交
      function onRendEnd(flowId, formCode) {
        if (formRowData.value.flowId == flowId) {
          isDoLoading.value = false;
          console.log(
            'onRendEnd flowId=' +
              flowId +
              ' formCode=' +
              formCode +
              ', isDoLoading is setted to false.',
          );
        } else {
          console.log('onRendEnd ' + formCode);
        }
      }

      // 当表单中的用户有变化时，重新匹配用户
      function reMatchUser(fieldName, fieldValue) {
        var params = {
          op: 'reMatchUser',
          actionId: flowProcessData.actionId,
          myActionId: flowProcessData.myActionId,
          fieldName: fieldName,
          fieldValue: fieldValue,
        };

        getMatchBranchAndUser(params).then((res) => {
          if (res.code === 200) {
            handleMatchJson(res.data || {});
          } else {
            createMessage.warn(res.msg);
          }
        });
      }

      // 取得常用语
      const fetchPhrases = () => {
        getPhrases({}).then((data) => {
          $('.phraseIcon').html(data.tipHtml);
        });
      };

      // ---------------------------------获取当前流程信息结束-----------------------------------------

      // -------------------------------------处理支线开始-----------------------------
      //处理人员匹配结果
      function handleMatchJson(obj) {
        matchJson.value = obj || {};
        if (obj.errCode === -1) {
          createMessage.error(obj.info);
        }
        toActions.value = [];
        if (matchJson.value.toActions && matchJson.value.toActions.length > 0) {
          matchJson.value.toActions.forEach((item) => {
            toActions.value.push({
              ...item,
              deptOfUserWithMultiDept: unref(matchJson).deptOfUserWithMultiDept,
              errCode: unref(matchJson).errCode,
              flagXorRadiate: unref(matchJson).flagXorRadiate,
              hasCond: unref(matchJson).hasCond,
              info: unref(matchJson).info,
              /* isBtnSelUserShow: matchJson.isBtnSelUserShow, */
              isMatchUserException: unref(matchJson).isMatchUserException,
              op: unref(matchJson).op,
              checkers:
                (item.checkers && item.checkers.length > 0
                  ? item.checkers.map((el) => {
                      let isDisabled = false;
                      if (el.hasOwnProperty('disabled')) {
                        isDisabled = el.disabled;
                      }
                      if (!el.hasOwnProperty('checked')) {
                        el.checked = false;
                      } else {
                        el.checked = !!el.checked; // el.checked可能为''
                      }
                      return {
                        ...el,
                        disabled: isDisabled,
                        value: el.userName,
                      };
                    })
                  : []) || [],
              xorChecked:
                item.checkers && item.checkers.length > 0
                  ? item.checkers.some((v) => (v.checked == true ? true : false))
                  : false,
            });
          });
        }
      }

      //设置支线是否勾选人员，如果选中则默认选中当前支线
      function checkXOR(actionId, checker, itemIndex) {
        if (!checker.hasOwnProperty('clickXor')) {
          return;
        }
        var xorObj = document.getElementById('XOR' + actionId);
        if (xorObj == null) return;

        // 如果用户原来未被勾选上，点击后则被勾选上了，置xor控件标志为checked
        if (!checker.checked) {
          // $(xorObj).prop('checked', true); // 无效
          toActions.value[itemIndex].xorChecked = true;
          return;
        }

        // 判断如果该action中所有的用户都未选，则置xor控件标志为checked=false
        var isAllUnchecked = true;
        $("input[name='" + 'WorkflowAction_' + actionId + "']").each(function () {
          var chked = $(this).prop('checked');
          if (chked) {
            isAllUnchecked = false;
            return;
          }
        });

        if (isAllUnchecked) {
          toActions.value[itemIndex].xorChecked = false;
        }
      }
      // 当存在兼职的情况，选择部门时
      function onSelDept() {
        var params = {
          op: 'matchAfterSelDept',
          actionId: unref(formRowData).actionId,
          myActionId: unref(formRowData).myActionId,
          deptOfUserWithMultiDept: unref(selectDeptCode),
        };
        getMatchBranchAndUser(params).then((res) => {
          if (res.code === 200) {
            handleMatchJson(res.data || {});
          } else {
            createMessage.warn(res.msg);
          }
        });
      }
      // -----------------------------------------------------处理支线结束-------------------------------------------------------------

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
        let users = [];
        if (record.checkers && record.checkers.length > 0) {
          users = record.checkers.map((item) => {
            return {
              ...item,
              name: item.userName,
            };
          });
        }
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
          let datas = data;
          datas.forEach((item) => {
            item.value = item.name;
            item.checked = item.checked || true;
            item.id = currentActionId.value;
            item.type = 'checkbox';
            item.disabled = item.disabled || false;
            item.clickXor = item.clickXor || true;
          });

          if (currentIsBtnXor) {
            toActions.value[currentIndex.value].xorChecked = true;
          }
          // let newDats = [] as any;
          // let values = toActions.value[currentIndex.value].checkers.map((item) => item.realName);
          // datas.forEach((item: any) => {
          //   if (!values.includes(item.realName)) {
          //     newDats.push(item);
          //   }
          // });

          toActions.value[currentIndex.value].checkers = [
            // ...toActions.value[currentIndex.value].checkers,
            ...datas,
          ];
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
        await getFinishAction(form).then(async (res) => {
          isSpinning.value = false;
          if (res.code == 500) {
            createMessage.warn(res.msg);
          } else {
            fileList.value = [];
            await getFlowProcessContent(unref(formRowData).myActionId);
            createMessage.success(res.msg);
          }
        });
      }
      // -----------------------------------------------------表单保存结束-------------------------------------------------------------

      // -----------------------------------------------------表单提交开始-------------------------------------------------------------
      //提交
      function toolbarSubmit() {
        try {
          ctlOnBeforeSerialize();
        } catch (e) {}

        // 检查文件是否已上传完毕

        // 摧毁LiveValidation校验，如不摧毁，在massValidate后，会形成重复校验
        var liveOp = new LiveValidation('op');
        LiveValidation.destroyValidate(liveOp.formObj.fields);
        // 清除不允许为空的红色*标
        $('.LV_presence').remove();

        // 重新生成LiveValidation校验规则
        if (flowProcessData.checkJsSub) {
          try {
            eval(flowProcessData.checkJsSub);
          } catch (e) {}
        }

        // 进行LiveValidation校验
        liveOp = new LiveValidation('op');
        if (!LiveValidation.massValidate(liveOp.formObj.fields)) {
          let liveErrMsg = LiveValidation.liveErrMsg.replaceAll('<br/>', '\n');
          if (liveErrMsg.indexOf('\n') >= 0 && liveErrMsg.indexOf('\n') != liveErrMsg.length - 1) {
            createMessage.warn({
              content: () => h('pre', liveErrMsg),
              duration: 5,
            });
          } else {
            createMessage.warn(liveErrMsg, 5);
          }
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

        var canUserStartFlow = flowProcessData.canUserStartFlow;
        createConfirm({
          iconType: 'warning',
          title: () => h('span', t('common.prompt')),
          content: () => h('span', canUserStartFlow ? '您确定要开始流程么？' : '您确定要提交吗？'),
          onOk: async () => {
            isSpinning.value = true;
            await submitResult();
            isSpinning.value = false;
          },
        });
      }

      async function submitResult(
        isAfterSaveformvalueBeforeXorCondSelect?,
        actions = [],
        isToReturner?,
      ) {
        if (
          !isUploadFinished(currentRoute.value?.query?.titleName || currentRoute.value?.meta?.title)
        ) {
          createMessage.warn('文件正在上传中，请在文件上传结束后再操作');
          return;
        }

        console.log(
          'submitResult isAfterSaveformvalueBeforeXorCondSelect',
          isAfterSaveformvalueBeforeXorCondSelect,
        );
        let hasCond = flowProcessData?.matchJson?.hasCond;
        console.log(
          'submitResult hasCond',
          hasCond,
          'flowProcessData?.matchJson',
          flowProcessData?.matchJson,
        );
        let isAutoSaveArchive = flowProcessData.isAutoSaveArchive;
        if (isToReturner == null || !isToReturner) {
          if (hasCond && !isAfterSaveformvalueBeforeXorCondSelect) {
            // 先ajax保存表单，然后再ajax弹出对话框选择用户，然后才交办
            o('op').value = 'saveformvalueBeforeXorCondSelect';

            isSpinning.value = true;
            await getFinishAction(getFormData(actions))
              .then((res) => {
                isSpinning.value = false;
                if (res.code == 500) {
                  createErrorModal({
                    title: t('common.tip'),
                    content: () => h('pre', res.msg),
                    okText: '关闭',
                  });

                  // createMessage.error(res.msg);
                  return;
                }

                // createMessage.success(res.msg);
                showResponse(res);
              })
              .catch((error) => {
                createMessage.error(error.msg);
              });

            return;
          }
        }

        // 如果是自动存档节点，则先保存表单，然后回到此页面，在onload的时候再FinishActoin
        if (isAutoSaveArchive) {
          o('op').value = 'AutoSaveArchiveNodeCommit';
        } else {
          o('op').value = 'finish';
        }

        isSpinning.value = true;
        if (isAfterSaveformvalueBeforeXorCondSelect) {
          o('isAfterSaveformvalueBeforeXorCondSelect').value =
            '' + isAfterSaveformvalueBeforeXorCondSelect;
        }

        await getFinishAction(getFormData(actions))
          .then((res) => {
            isSpinning.value = false;
            if (res.code == 500) {
              createErrorModal({
                title: t('common.tip'),
                content: () => h('pre', res.msg),
                okText: '关闭',
              });

              // if (res.msg.indexOf('\n') >= 0 && res.msg.indexOf('\n') != res.msg.length - 1) {
              //   // 使支持通过\n换行
              //   createMessage.warn({
              //     content: () => h('pre', res.msg),
              //   });
              // } else {
              //   createMessage.warn(res.msg);
              // }
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

        // // 过滤掉其它字符，只保留JSON字符串
        // var m = data.match(/\{.*?\}/gi);
        // if (m != null) {
        //   if (m.length == 1) {
        //     data = m[0];
        //   }
        // }

        // try {
        //   data = jQuery.parseJSON(data);
        // } catch (e) {
        //   createMessage.warning(data);
        //   return;
        // }

        if (data.ret == '0') {
          createErrorModal({
            title: t('common.tip'),
            content: () => h('pre', data.msg),
            okText: '关闭',
          });
          // createMessage.error(data.msg);
          return;
        }

        let isDebug = flowProcessData.isDebug;
        var op = data.op;
        if (op === 'read') {
          if (isDebug) {
            // layer.alert(data.msg, {
            //     btn: ['确定'],
            //     yes: function() {
            //         window.location.href = "flow/flow_list_debugger.jsp?myActionId=[(${myActionId})]";
            //     }
            // });
            createConfirm({
              iconType: 'success',
              title: () => h('span', t('common.prompt')),
              content: () => h('span', data.msg),
              maskClosable: false,
              closable: false,
              cancelText: '',
              onOk: () => {
                handleClose();
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
        } else if (op == 'AutoSaveArchiveNodeManualFinish') {
          // 20200428自动存档已经不需要再回到flow_dispose.jsp页面处理了
          done(data.msg, true, _);
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
              if (isDebug) {
                let isDebugCache = getIsDebug();
                if (isDebugCache == 'true') {
                  go({
                    path: '/flowDebugPage',
                    query: {
                      myActionId: flowProcessData.myActionId,
                      flowId: flowProcessData.flowId,
                    },
                  });
                } else {
                  userStore.setIsDebug('false');
                  handleClose();
                  go({
                    path: '/flowDebug',
                    query: {
                      myActionId: flowProcessData.myActionId,
                      flowId: flowProcessData.flowId,
                    },
                  });
                }
              } else {
                done(data.msg, false, op);
              }
            }
          }
        } else if (op == 'return') {
          if (isDebug) {
            let isDebugCache = getIsDebug();
            if (isDebugCache == 'true') {
              go({
                path: '/flowDebugPage',
                query: {
                  myActionId: flowProcessData.myActionId,
                  flowId: flowProcessData.flowId,
                },
              });
            } else {
              userStore.setIsDebug('false');
              handleClose();
              go({
                path: '/flowDebug',
                query: {
                  myActionId: flowProcessData.myActionId,
                  flowId: flowProcessData.flowId,
                },
              });
            }

            // 置刷新标志
            multipleTabStore.updateIsRefreshProcessPage(true);
          } else {
            done(data.msg, true, '');
          }
        } else if (op == 'saveformvalueBeforeXorCondSelect') {
          const params = {
            op: 'matchNextBranch',
            actionId: flowProcessData.actionId,
            deptOfUserWithMultiDept: selectDeptCode.value, // 当前所选的部门
            myActionId: flowProcessData.myActionId,
            askType: 1,
          };
          getMatchBranchAndUser(params).then((res) => {
            console.log('getMatchBranchAndUser res', res);
            if (res.data.errCode == -2) {
              // 不需要显示info
              return;
            } else if (res.data.errCode == -1) {
              // 如果条件不满足，在默认条件分支中匹配用户matchActionUser报错
              createMessage.error(res.data.info);
              return;
            }

            var rendResult = ref('');
            // 判断有没有匹配到人员
            let hasUserCheckbox = res.data.toActions
              ? res.data.toActions.some((item) => item.checkers && item.checkers.length > 0)
              : false;

            var isMatchUserException = false;
            if (res.data.hasOwnProperty('isMatchUserException')) {
              isMatchUserException = res.data.isMatchUserException;
            }
            console.log(
              'hasUserCheckbox',
              hasUserCheckbox,
              'isBtnSelUserShow',
              res.data.isBtnSelUserShow,
              'isMatchUserException',
              isMatchUserException,
            );
            // 如果没有匹配到用户，且不存在自选用户，且没有匹配异常，且条件匹配，说明可能是用了辅助角色，直接提交
            if (
              !hasUserCheckbox &&
              !res.data.isBtnSelUserShow &&
              !isMatchUserException &&
              res.data.errCode != -3 // 条件不匹配
            ) {
              console.log('res.data', res.data);
              // 对分支匹配结果进行处理，存入toActions
              handleMatchJson(res.data);
              // 如果toActions中仅匹配到一条分支（且只应匹配到一条分支），需置xorChecked为true，以便于在submitResult中getFormData时，将xorChecked的分支作为XorNextActionInternalNames提交
              if (toActions.value.length == 1) {
                toActions.value[0].xorChecked = true;
              }
              submitResult(true, toActions);
              return;
            }
            console.log('hasUserCheckbox', hasUserCheckbox);
            // 如果匹配到人员、没有“选择用户”按钮且条件匹配
            if (
              hasUserCheckbox &&
              !res.data.isBtnSelUserShow &&
              !isMatchUserException &&
              res.data.errCode != -3 // 条件不匹配
            ) {
              rendResult.value = '如果不需要选择用户，请直接点击确定按钮！';
              // 判断是否存在未被disabed的用户，即需手动勾选的用户
              let hasCheckerNotDisabled = res.data.toActions.some((item) => {
                if (item.checkers && item.checkers.length > 0) {
                  for (let k in item.checkers) {
                    if (!item.checkers[k].hasOwnProperty('disabled')) {
                      return true;
                    }
                  }
                } else {
                  // 如果是自选用户
                  if (item.isBtnSelUser) {
                    return true;
                  }
                }
                return false;
              });

              console.log('hasCheckerNotDisabled', hasCheckerNotDisabled);

              // 如果所有的用户均是disabled则直接提交
              if (!hasCheckerNotDisabled) {
                handleMatchJson(res.data);
                submitResult(true);
                return;
              }
            }

            var isNotShowNextUsers = flowProcessData.isNotShowNextUsers;
            if (isNotShowNextUsers) {
              if (!isMatchUserException) {
                // 直接提交
                rendResult.value = '请点击确定按钮';
                submitResult(true);
                return;
              }
            }

            openMatchJsonModal(true, {
              record: res,
              rendResult: unref(rendResult),
              formRowData: unref(formRowData),
            });
          });
        }
      }
      //打开分支线人员弹窗
      const [matchJsonRegisterModal, { openModal: openMatchJsonModal }] = useModal();
      //分支线人员弹窗回调
      function handleMatchJsonCallBack(actions) {
        submitResult(true, actions);
      }
      function done(msg, isClose, op) {
        // 置刷新标志
        multipleTabStore.updateIsRefreshProcessPage(true);

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

              // createMessage.success(msg);
              // // 进入到详情页
              // emit('showView', { 'f.id': flowProcessData.flowId });

              createSuccessModal({
                title: t('common.tip'),
                content: msg,
                onOk: () => {
                  // 进入到详情页（抽屉中才能进入到详情页）
                  emit('showView', {
                    'f.id': flowProcessData.flowId,
                    'f.title': formRowData.value.cwsWorkflowTitle,
                  });
                  handleClose();
                },
              });
            } else {
              // createMessage.success(msg);

              createSuccessModal({
                title: t('common.tip'),
                content: msg,
                onOk: () => {
                  handleClose();
                },
              });
            }

            // handleClose();
          }
        }
      }

      //封装基本FormData 可基于返回FormData xx.append()附加参数
      function getFormData(matcBranchAndUsertoActions = []) {
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
        let XorNextActionInternalNames = ref<any>([]);
        // 加入流程中选定的人员、分支
        if (matcBranchAndUsertoActions.length > 0) {
          for (let index in matcBranchAndUsertoActions) {
            let item = matcBranchAndUsertoActions[index] as any;
            if (item.xorChecked) {
              XorNextActionInternalNames.value.push(item.internalName);
            }
            for (let checkerIndex in item.checkers) {
              let checker = item.checkers[checkerIndex];
              if (checker.checked) {
                form.append('WorkflowAction_' + item.id, checker.value);
              }
            }
          }
        } else {
          for (let index in toActions.value) {
            let item = toActions.value[index];
            if (item.xorChecked) {
              XorNextActionInternalNames.value.push(item.internalName);
            }
            for (let checkerIndex in item.checkers) {
              let checker = item.checkers[checkerIndex];
              if (checker.checked) {
                form.append('WorkflowAction_' + item.id, checker.value);
              }
            }
          }
        }
        // xorChecked
        form.append('XorNextActionInternalNames', unref(XorNextActionInternalNames).join(','));

        // 如果启用了obs上传，则此处不需要再加入上传按钮所选的文件
        if (!isObjStoreEnabled.value) {
          if (fileList.value.length > 0) {
            fileList.value.forEach((file: UploadProps['fileList'][number], index) => {
              form.append(`att${index + 1}`, file as any);
            });
          }
        }
        return form;
      }
      // -----------------------------------------------------表单提交结束-------------------------------------------------------------

      // -----------------------------------------------------退回开始-------------------------------------------------------------
      // 退回
      function returnFlow() {
        returnFlowConfirm();
      }

      // 删除流程
      async function delFlow() {
        await getDelFlow({
          flowId: unref(formRowData).flowId,
        }).then(async (res) => {
          if (res.data.res === 0) {
            createMessage.success(res.data.msg);

            // 置刷新标志
            multipleTabStore.updateIsRefreshProcessPage(true);

            handleClose();
          } else {
            createMessage.error(res.data.msg);
          }
        });
      }

      // 放弃流程
      async function discardFlow() {
        await getDiscardFlow({
          flowId: unref(formRowData).flowId,
        }).then(async (res) => {
          if (res.data.res === 0) {
            createMessage.success(res.data.msg);

            // 置刷新标志
            multipleTabStore.updateIsRefreshProcessPage(true);

            handleClose();
          } else {
            createMessage.error(res.data.msg);
          }
        });
      }

      // 拒绝流程
      async function disagreeFlow() {
        o('op').value = 'manualFinish';
        const form = getFormData();
        isSpinning.value = true;
        await getFinishAction(form).then(async (res) => {
          isSpinning.value = false;
          createMessage.success(res.msg);

          // 置刷新标志
          multipleTabStore.updateIsRefreshProcessPage(true);

          handleClose();
        });
      }

      // 同意并结束流程
      async function finishAgreeFlow() {
        o('op').value = 'manualFinishAgree';
        const form = getFormData();
        isSpinning.value = true;
        await getFinishAction(form).then(async (res) => {
          isSpinning.value = false;
          createMessage.success(res.msg);

          // 置刷新标志
          multipleTabStore.updateIsRefreshProcessPage(true);

          handleClose();
        });
      }

      // 退回需选择的用户或节点数据
      const returnFlowVisible = ref(false);
      // 是否正退回
      const isReturning = ref(false);
      const importVisible = ref(false);
      const returnResult = ref<any>([]);
      const aryReturnAction = ref<any>([]);
      const returnIdValueRadio = ref('');
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
          // 在form_js_formCode.js中写此方法
          var r = checkOnReturnBack();
          if (r != '') {
            createMessage.warning(r);
            return;
          }
        } catch (e) {}

        isReturnStyleFree.value = flowProcessData.isReturnStyleFree;
        if (isReturnStyleFree.value) {
          isSpinning.value = true;
          await getReturnAction({
            actionId: unref(formRowData).actionId,
            flowId: unref(formRowData).flowId,
          })
            .then(async (data) => {
              returnResult.value = data.result || [];

              if (returnResult.value.length === 1) {
                returnIdValueRadio.value = returnResult.value[0].returnId;
              } else {
                returnIdValueRadio.value = '';
              }
            })
            .finally(() => {
              isSpinning.value = false;
            });
        } else {
          aryReturnAction.value = flowProcessData.aryReturnAction || [];
          if (aryReturnAction.value.length === 1) {
            returnIdValueRadio.value = aryReturnAction.value[0].actionId;
          } else {
            returnIdValueRadio.value = '';
          }
          aryReturnAction.value.forEach((item: any) => {
            item.label = item.actionTitle + '  ' + item.realName;
            item.value = item.actionId;
          });
        }
        returnFlowVisible.value = true;
      }

      //返回最终确认 Modal
      async function handleReturnFlow() {
        o('op').value = 'return';
        if (!returnIdValueRadio.value) {
          createMessage.warning('请选择需返回的用户');
          return;
        }

        let formData = getFormData();
        formData.append('returnId', returnIdValueRadio.value);

        isReturning.value = true;
        isSpinning.value = true;
        await getFinishAction(formData)
          .then((res) => {
            createMessage.success(res.msg);
            showResponse(res);
          })
          .catch((e) => createMessage.warning(e.msg))
          .finally(() => {
            isReturning.value = false;
            returnFlowVisible.value = false;
            isSpinning.value = false;
          });
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
          width: '350px',
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
          dataIndex: 'fileSizeMb',
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
          if (data && data.length > 0) {
            aryAttSize.value = data.length;
          }
        },
        actionColumn: {
          width: 150,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });
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
        if (serverInfo.isObjStoreEnabled) {
          getObjectByKey(record.visualPath + '/' + record.diskName);
        } else {
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
          /* // 注意添加时用的是relate抽屉，而编辑时不是
          openSmartModuleRelateDrawer(true, {
            isUpdate: type, // 1 新增，2 编辑，3 详情
            record: {
              ...params,
              actionId: unref(formRowData).actionId,
              // moduleCode: '',
              // moduleCodeRelated: '',
              // parentId: '',
            },
          }); */
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
        console.log('流程的抽屉', rows, 'myStart', myStart);
        let params: any = getCurModuleFieldListParams();
        console.log('params', params);
        if (myStart == 1) {
          params.id = rows[0].ID;
          params.cwsFormName = getFormName.value;
          let thisBody: Object = document.body;
          // 给表单域控赋值，并带入其映射字段
          getItemsForListModuleSel(params).then(async (res) => {
            // console.log(res.data);
            $(thisBody).append(res.data);

            isSpinning.value = true;
            await filterJS(res.data, 'items', o('flowForm'));
            isSpinning.value = false;

            $('.helper-module-list-sel').remove();
            removeScript('items');
            console.log('handleSmartModuleSelCallBack params', params);
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
      function openSmartModuleDrawerForShow(moduleCode, id, visitKey, params) {
        console.log('openSmartModuleDrawerForShow params', params);
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

      function handleProcessShowCallBack(rows) {
        initWindowFunc();
      }
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
            console.log('exportExcelRelate 导出 ' + title);
            downloadByData(data, `${title}.xls`);
          } else {
            console.error('exportExcelRelate 导出失败');
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
        getVisualImportExcelNest(formData, query)
          .then(() => {
            createMessage.success('操作成功');
            if (excelParams.nestType == 'nest_sheet') {
              // 刷新嵌套表
              eval('reloadNestSheetCtl' + excelParams.moduleCode + '()');
            } else {
              eval('refreshNestTableCtl' + excelParams.nestFieldName + '()');
            }
          })
          .finally(() => {
            isImport.value = false;
            importVisible.value = false;
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

      //初始化
      watch(
        () => props.myActionId,
        async (newVal, oldVal) => {
          if (newVal !== oldVal) {
            isSpinning.value = false;
            if (!!props.myActionId) {
              removeScript(srcId.value);
              setTimeout(() => {
                getFlowProcessContent(props.myActionId);
              }, 100);
            }
          }
        },
        {
          // 0611 如果为true，可能会调两次getFlowProcessContent，在上面加上了判断 newVal !== oldVal也没用
          // immediate: true,
        },
      );
      //发起流程
      const beginLaunch = (typeCode: string) => {
        return new Promise((resolve) => {
          isSpinning.value = true;
          getFlowInit({ typeCode: typeCode })
            .then((res) => {
              isLaunched.value = true;
              let myActionId = res.myActionId || '';
              if (myActionId) {
                getFlowProcessContent(myActionId);
              }
            })
            .finally(() => {
              resolve(true);
              //isSpinning.value = false;
            });
        });
      };
      const query = ref<any>({});
      const typeCode = ref<any>(null);
      const isTab = ref(false); // 是否在新选项卡中打开，当在launch.vue中打开时，isTab为true
      let isFirst = false; // 是否第一次进入

      onMounted(async () => {
        isFirst = true;

        removeScript(srcId.value);
        // debug模式入口或从发起流程页面launch.vue进入
        if (route.query) {
          query.value = route.query;
          isTab.value = query.value.isTab ? query.value.isTab : false;
        }
        // 从菜单进入
        if (route.meta.formCode) {
          isTab.value = true;
        }
        // 从processShowView进入
        if (query.value.isFromShowView || query.value.isFromProcess) {
          isTab.value = true;
          if (route.query.title) {
            setTitle(route.query.title);
          }
        }
        // 从待办列表进入或来自于processShowView中的处理按钮
        if (query.value.myActionId) {
          await getFlowProcessContent(query.value.myActionId);
        }
        if (query.value.isDebug == 'true') {
          userStore.setIsDebug(query.value.isDebug);
        }

        //流程模块入口
        console.log('onmounted route', route);
        console.log('props.myActionId', props.myActionId);
        // 从操作列发起流程时，route.meta.formCode中也可能是有值的，但是此时myActionId已经有了，故可以根据此来判断是否beginLaunch
        if (!!props.myActionId || props.myActionId === 0) {
          // 当在模块列表页时，route.meta.formCode的值为当前的模块编码，当在模块列表的工具条按钮中发起流程时，需通过type来判断
          // 当菜单类型为发起流程时
          if (route.meta.formCode && route.meta.type === 3) {
            typeCode.value = route.meta?.formCode;
            // console.log('typeCode', typeCode.value);
            beginLaunch(typeCode.value);
          }
        }
      });

      let isCached = true;

      onActivated(() => {
        isCached = true;
        console.log('process onActivated isDoLoading', isDoLoading.value);
        initWindowFunc();
        console.log('onActivated initWindowFunc');

        console.log('onActivated isFirst', isFirst);

        setTimeout(() => {
          // 如果是第一次进入页面，不能调用initContent
          if (!isFirst) {
            // 如果加载未完成，则继续
            if (isDoLoading.value) {
              console.log('process onActivated initContent');
              initContent();
            }
          }

          // if (multipleTabStore.isRefreshPage) {
          //   console.log('formRowData.value.myActionId', formRowData.value.myActionId);
          //   if (formRowData.value.myActionId && formRowData.value.myActionId != -1) {
          //     // handleRefresh();
          //     isSpinning.value = true;

          //     // 清空数据
          //     content.value = '';
          //     setTableData([]);

          //     typeCode.value = route.meta?.formCode;
          //     // console.log('typeCode', typeCode.value);
          //     beginLaunch(typeCode.value);
          //   }
          //   multipleTabStore.updateIsRefreshPage(false);
          // }
          try {
            // 用于form_js中的定时器检测是否可继续执行
            eval('onActivated_' + formRowData.value.formCode + '(1, typeCode.value)');
          } catch (e) {
            // console.error(e);
            console.warn('form_js_' + formRowData.value.formCode + ' 中无onActivated方法');
          }

          isFirst = false;
        }, 100);
      });

      onDeactivated(() => {
        isCached = false;
        console.log('processHandle onDeactivated');
        try {
          // 用于form_js中的定时器检测是否可继续执行
          eval('onDeactivated_' + formRowData.value.formCode + '(1, typeCode.value)');
        } catch (e) {
          // console.error(e);
          console.warn('form_js_' + formRowData.value.formCode + ' 中无onDeactivated方法');
        }
      });

      // 如果菜单项被缓存，则关闭选项卡时会调用到此事件
      onUnmounted(() => {
        console.log('Unmounted in processHandleView!');
        curFormUtil?.close(getFormName.value);
      });

      function getServerUrl() {
        return userStore.getServerUrl;
      }

      function getPublicPath() {
        const publicPath = import.meta.env.VITE_PUBLIC_PATH || '/';
        return publicPath;
      }

      function getCurFormId() {
        // 注意可能会造成有的页面中忘了设置getCurFormId，导致fo调用getCurFormId时将本页的formName认为是当前form
        // return getFormName.value;
        return curFormUtil?.get();
      }

      function getCurFormUtil() {
        return curFormUtil;
      }

      function showSpinning(isShow) {
        isSpinning.value = isShow;
      }

      const getFormName = computed(() => 'flowForm' + curFormUtil?.getFormNo());

      function initWindowFunc() {
        console.log('initWindowFunc getFormName.value=' + getFormName.value);
        // initWindowFunc中需延时置curFormUtil.set(...)，否则当切换选项卡时，如从列表页切至流程处理页，列表页onunmounted可能会滞后，导致其中curFormUtil.close()处理的是本页面的formId
        setTimeout(() => {
          curFormUtil?.set(getFormName.value);
        }, 100);

        let newWindow = window as any;
        newWindow.onRendEnd = onRendEnd;
        newWindow.getCurFormId = getCurFormId;
        newWindow.getCurFormUtil = getCurFormUtil;
        newWindow.ajaxPost = ajaxPost;
        newWindow.ajaxGet = ajaxGet;
        newWindow.ajaxGetJS = ajaxGetJS;
        newWindow.filterJS = filterJS;
        newWindow.selectUserInForm = selectUserInForm;
        newWindow.openSmartModuleRelateTableDrawer = openSmartModuleRelateTableDrawer;
        newWindow.myConfirm = myConfirm;
        newWindow.myMsg = myMsg;
        newWindow.openSmartModuleSelTableDrawer = openSmartModuleSelTableDrawer;
        newWindow.submitMyFile = submitMyFile;
        // 表单域选择宏控件在查看时会用到
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
        newWindow.openSelStampModal = openSelStampModal;
        newWindow.showImage = showImage;
        newWindow.openWritePadModal = openWritePadModal;
        newWindow.removePhrase = removePhrase;
        newWindow.openSelDeptModal = openSelDeptModal;
        newWindow.openLocationMarkModal = openLocationMarkModal;
        newWindow.initFormCtl = initFormCtl;
        newWindow.showSpinning = showSpinning;
        newWindow.downloadFileVisual = downloadFileVisual; // 映射的文件宏控件下载

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

      // ------------------------------------加签开始----------------------------------------------

      const [registerCountersignModal, { openModal: openCountersignModal }] = useModal();
      //加签modal
      const handleCountersign = () => {
        openCountersignModal(true, {
          isUpdate: true,
          isFlowStarted: formRowData.value.isFlowStarted,
          myActionId: formRowData.value.myActionId,
          record: {},
        });
      };
      //加签modal回调
      const handleCountersignCallBack = (plusType, desc) => {
        formRowData.value.plusDesc = desc;
        isPlusDescShow.value = true;
        console.log('handleCountersignCallBack', plusType, desc);
        // 如果为前加签，则关闭抽屉
        if (plusType == 0) {
          createMessage.success('前加签成功');
          let isDebug = flowProcessData.isDebug;
          if (isDebug) {
            let isDebugCache = getIsDebug();
            if (isDebugCache == 'true') {
              go({
                path: '/flowDebugPage',
                query: {
                  myActionId: flowProcessData.myActionId,
                  flowId: flowProcessData.flowId,
                },
              });
            } else {
              userStore.setIsDebug('false');
              handleClose();
              go({
                path: '/flowDebug',
                query: {
                  myActionId: flowProcessData.myActionId,
                  flowId: flowProcessData.flowId,
                },
              });
            }
          } else {
            handleClose();
          }
        }

        // 置刷新标志
        multipleTabStore.updateIsRefreshProcessPage(true);
      };

      async function delPlus() {
        let params = {
          actionId: formRowData.value.actionId,
        };
        await getDelPlus(params);
        isPlusDescShow.value = false;
        formRowData.value.plusDesc = '';
      }

      // ------------------------------------加签结束----------------------------------------------

      // ------------------------------------抄送开始----------------------------------------------
      const [registerDistributeModal, { openModal: openDistributeModal }] = useModal();
      //展示加签modal
      const handleDistribute = () => {
        openDistributeModal(true, {
          isUpdate: true,
          record: formRowData,
        });
      };
      //加签modal回调
      const handleDistributeCallBack = () => {};
      // ------------------------------------抄送结束----------------------------------------------

      const [registerSelTemplateModal, { openModal: openSelTemplateModal }] = useModal();
      const handleSelTemplate = (record) => {
        openSelTemplateModal(true, {
          ...record,
        });
      };

      async function handleSelTemplateCallBack(record) {
        console.log(record);
        record.isDoDocInRed = true;
        convertToRedDocument({
          flowId: formRowData.value.flowId,
          templateId: record.templateId,
          attachId: record.attachId,
        })
          .then((data) => {
            if (data.res == 0) {
              createMessage.success('操作成功');
              reloadAttachment();
            }
          })
          .finally(() => {
            record.isDoDocInRed = false;
          });
      }

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

      // 原用于图片签名框SignImgCtl，现暂已无用
      async function showImage(imgId, imgPath) {
        await getShowImg({ path: imgPath }).then(async (res) => {
          let imageUrl = bufToUrl(res);
          console.log('showImage imgId', imgId, 'imgPath', imgPath);
          findObj(imgId).src = imageUrl;
        });
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

      const [registerSelDeptModal, { openModal: openSDeptModal }] = useModal();
      const openSelDeptModal = (record) => {
        openSDeptModal(true, {
          ...record,
        });
      };

      async function handleSelDeptCallBack(record) {
        let codes = '',
          names = '';
        if (record && record.length && record.length > 0) {
          for (let i in record) {
            if (codes == '') {
              codes = record[i].value;
              names = record[i].label;
            } else {
              codes += ',' + record[i].value;
              names += ',' + record[i].label;
            }
          }
        }
        console.log('handleSelDeptCallBack', record);
        setInputObjValue(codes, names);
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

      // 直送给返回者
      async function handleToRetuner() {
        isSpinning.value = true;
        let isToReturner = true;
        await submitResult(false, [], isToReturner);
        isSpinning.value = false;
      }

      async function handleSuspend() {
        isSpinning.value = true;
        let res = await getSuspendFlow({ myActionId: formRowData.value.myActionId });
        done(res.msg, true, '');
        isSpinning.value = false;
      }

      async function handleResume() {
        isSpinning.value = true;
        let res = await getResumeFlow({ myActionId: formRowData.value.myActionId });
        createMessage.success(res.msg);
        handleRefresh();
        isSpinning.value = false;
      }

      function closeView() {
        removeLink();
        removeScript(srcId.value);
        closeCurrent();
      }

      function handleClose() {
        emit('success');
        emit('closeDrawer');
        if (isTab.value) {
          closeView();
        }
      }

      // 映射的文件宏控件下载
      function downloadFileVisual(fileName, params) {
        getVisualDownload(params).then((data) => {
          if (data) {
            downloadByData(data, fileName);
          }
        });
      }

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
          createMessage.warn('文件: ' + file.name + ' 大小超过了 ' + maxFileSize / 1024 + 'M');
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
            console.log('it', it);
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
        console.log('file', file);
        let treeList = uploadFileStore.getUploadFileTreeList;
        let progress = 100;
        treeList.forEach((item) => {
          item.children.forEach((it) => {
            console.log('it', it);
            if (it.file.uid === file.uid) {
              progress = it.progress;
              if (progress == 100) {
                console.log('getProgress', progress);
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
        isSpinning,
        matchJson,
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
        toActions,
        registerModal,
        selectUser,
        handleCallBack,
        selectUserInForm,
        onSelDept,
        checkXOR,
        toolbarSubmit,
        matchJsonRegisterModal,
        handleMatchJsonCallBack,
        returnFlow,
        returnFlowVisible,
        handleReturnFlow,
        returnResult,
        returnIdValueRadio,
        aryReturnAction,
        isReturnStyleFree,
        registerSmartModuleRelateTableDrawer,
        smartModuleRelateTableDrawerhandleSuccess,
        registerSmartModuleSelDrawer,
        handleSmartModuleSelCallBack,
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
        delFlow,
        onClickMenu,
        handlePrintBtnClick,
        registerProcessViewDrawer,
        discardFlow,
        btnAgreeName,
        btnReturnName,
        btnDisagreeName,
        disagreeFlow,
        finishAgreeFlow,
        typeCode,
        aryAttSize,
        initWindowFunc,
        handleRefresh,
        flowIsRemarkShow,
        handleCountersign,
        registerCountersignModal,
        handleCountersignCallBack,
        handleDistribute,
        registerDistributeModal,
        handleDistributeCallBack,
        isPlusDescShow,
        delPlus,
        registerSelTemplateModal,
        handleSelTemplate,
        handleSelTemplateCallBack,
        registerSelStampModal,
        handleSelStamp,
        handleSelStampCallBack,
        registerInputPwdModal,
        handleInputPwdCallBack,
        registerWritePadModal,
        handleWritePadCallBack,
        registerTipPhraseModal,
        handleTipPhraseCallBack,
        openTipPhraseModal,
        handleToRetuner,
        handleSuspend,
        handleResume,
        registerSelDeptModal,
        handleSelDeptCallBack,
        registerLocationMarkModal,
        handleLocationMarkCallBack,
        isLoaded,
        clearSmartModuleSel,
        isDoLoading,
        getFormName,
        isReturning,
        isLaunched,
        isTab,
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
  .toolbar-wrap {
    height: 100px;
    background-color: white;
    z-index: 100;
  }
  .toolbar {
    position: fixed;
    padding-top: 8px;
    z-index: 10;
    background-color: white;
    width: 90%;
    height: 90px;
    z-index: 100;
  }
</style>
