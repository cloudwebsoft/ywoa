<template>
  <PageWrapper dense :contentFullHeight="true" :fixedHeight="false" contentClass="flex">
    <div class="w-full">
      <div class="w-full h-full">
        <div class="ml-2 mr-2" v-show="showSearch || (hasTab && aryTab.length > 1) || cardLen > 0">
          <Tabs
            class="bg-white"
            v-model:activeKey="activeKey.id"
            centered
            @change="getActiveKey"
            v-if="hasTab && aryTab.length > 1"
          >
            <TabPane :tab="item.name" force-render v-for="item in aryTab" :key="item.id" />
          </Tabs>
          <div
            class="p-2 bg-white"
            v-show="showSearch && isShowSearch && aryCond && aryCond.length > 0"
          >
            <Form
              id="customForm"
              :Name="getFormName"
              :label-col="labelCol"
              :wrapper-col="wrapperCol"
              @keyup.enter="searchData"
            >
              <Row id="ctlHtml">
                <Col
                  v-for="(item, index) in aryCond"
                  :key="index"
                  :span="item.width ? item.width : 6"
                >
                  <FormItem
                    :label="item['title']"
                    v-show="index <= 3 || (isShowAllCond && index > 3)"
                  >
                    <!-- eslint-disable-next-line vue/no-v-html -->
                    <div v-html="item['ctlHtml']" class="w-full flex"> </div>
                    <span v-show="item.expand" @click="expandSearchForm">
                      <Tooltip :title="isShowAllCond ? '收缩' : '展开'">
                        <Icon
                          :class="[
                            isShowAllCond ? 'normal' : 'rotate180',
                            'cursor-pointer',
                            'ml-1',
                          ]"
                          icon="clarity:collapse-line"
                        />
                      </Tooltip>
                    </span>
                  </FormItem>
                </Col>
              </Row>
              <Row class="flex justify-center">
                <Button @click="resetData">重置</Button>
                <Button type="primary" class="ml-1" @click="searchData" htmlType="submit"
                  >查询</Button
                >
              </Row>
              <input name="op" value="search" type="hidden" />
              <input id="moduleCode" name="moduleCode" value="" type="hidden" />
            </Form>
          </div>
          <div class="p-2 bg-white mt-2" v-show="cardLen > 0">
            <CardView :kind="1" :moduleCode="getModuleCode" @loaded="handleLoaded" />
          </div>
        </div>
        <div class="h-full" v-if="activeKey.type == 'calendar'">
          <smartCalendar v-model:listPage="listPage" v-model:searchParams="formParams" />
        </div>
        <div class="h-full" v-else>
          <BasicTable
            @register="registerTable"
            class="m-2"
            @edit-end="handleEditEnd"
            @edit-cancel="handleEditCancel"
            :beforeEditSubmit="beforeEditSubmit"
            :columns="flowColumns"
            @columns-change="columnsChange"
            :can-resize="listPage.isAutoHeight"
            @resize-column="handleResizeColumn"
            :pagination="pagination"
          >
            <template v-if="listPage.isExpand" #expandedRowRender="{ record }">
              <template v-for="(columns, index) in nestColumns" :key="index">
                <BasicTable
                  :columns="columns"
                  :data-source="record.nestTableAry[index]"
                  :show-index-column="false"
                  :pagination="false"
                />
              </template>
            </template>
            <template #toolbar v-if="showToolbar">
              <Button type="primary" class="mr-1" @click="handleAdd" v-if="canAdd">
                <template #icon><EditOutlined /></template>
                {{ btnAddName }}
              </Button>
              <Popconfirm
                placement="top"
                title="确定删除吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="deleteBatch"
              >
                <Button type="primary" class="mr-1" v-if="canDel">
                  <template #icon><DeleteOutlined /></template>
                  {{ btnDelName }}
                </Button>
              </Popconfirm>

              <!-- <ImpExcel :isFiles="false" @raw-file="rawFileFn" dateFormat="YYYY-MM-DD"> -->
              <Button
                type="primary"
                class="mr-1"
                v-if="listPage.isBtnImportShow"
                @click="handleImport"
              >
                <template #icon><DownSquareOutlined /></template>
                导入
              </Button>
              <!-- </ImpExcel> -->
              <!-- <Button
                type="primary"
                class="mr-1"
                @click="handleExport"
                v-if="listPage.isBtnExportShow"
                :loading="isExport"
              >
                <template #icon><UpSquareOutlined /></template>
                导出
              </Button> -->
              <Dropdown v-if="listPage.isBtnExportShow || listPage.isBtnExportXlsColShow">
                <Button type="primary" class="mr-1" :loading="isExport"
                  ><template #icon><UpSquareOutlined /></template> 导出
                </Button>
                <template #overlay>
                  <Menu @click="onClickExportMenu">
                    <MenuItem key="expBySel" v-if="listPage.isBtnExportXlsColShow">
                      选择列导出
                    </MenuItem>
                    <MenuItem key="expByCol" v-if="listPage.isBtnExportShow"> 按列表导出 </MenuItem>
                  </Menu>
                </template>
              </Dropdown>
              <Button
                type="primary"
                class="mr-1"
                @click="copyRecords"
                v-if="listPage.isBtnCopyShow"
                :loading="isCopy"
              >
                <template #icon><CopyOutlined /></template>
                复制
              </Button>
              <!-- 工具栏自定义按钮 -->
              <template v-for="item in listPage.aryBtnEvent" :key="item.name">
                <Popconfirm
                  placement="top"
                  :title="`确定${item.name}吗？`"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="batchOp(item)"
                  v-if="item.type == 'batchBtn'"
                >
                  <Button type="primary" class="mr-1" v-if="item.type == 'batchBtn'">
                    <template #icon
                      ><i v-if="item['icon'] != 'none'" :class="'fa ' + item['icon']"></i
                    ></template>
                    {{ item.name }}
                  </Button>
                </Popconfirm>
                <Button
                  type="primary"
                  class="mr-1"
                  v-if="item.type == 'script'"
                  @click="handleScript(item)"
                >
                  <template #icon
                    ><i v-if="item['icon'] != 'none'" :class="'fa ' + item['icon']"></i
                  ></template>
                  {{ item.name || '事件' }}
                </Button>
                <Button
                  type="primary"
                  class="mr-1"
                  v-if="item.type == 'flowBtn'"
                  :loading="isLaunching"
                  @click="handleFlow(item)"
                >
                  <template #icon
                    ><i v-if="item['icon'] != 'none'" :class="'fa ' + item['icon']"></i
                  ></template>
                  {{ item.name }}
                </Button>
                <Button
                  type="primary"
                  class="mr-1"
                  v-if="item.type == 'moduleBtn'"
                  @click="handleAddModule(item)"
                >
                  <template #icon
                    ><i v-if="item['icon'] != 'none'" :class="'fa ' + item['icon']"></i
                  ></template>
                  {{ item.name }}
                </Button>
              </template>
              <Button
                type="primary"
                class="mr-1"
                :loading="isExportWord"
                @click="handleExportWord"
                v-if="listPage.isBtnExportWordShow"
              >
                <template #icon><FileWordOutlined /></template>
                生成
              </Button>
              <Button
                type="primary"
                class="mr-1"
                :loading="isDownloadZip"
                @click="handleDownloadZip"
                v-if="listPage.isBtnZipShow"
              >
                <template #icon>
                  <FileZipOutlined />
                </template>
                压缩
              </Button>
              <Button type="primary" class="mr-1" v-if="listPage.isAdmin" @click="handleManager">
                <template #icon><SettingOutlined /></template>
                管理
              </Button>
              <Tooltip title="设置表头">
                <Icon
                  icon="ant-design:setting-outlined"
                  style="margin-right: 0px !important"
                  class="cursor-pointer"
                  :size="20"
                  v-if="listPage.isBtnSetListShow"
                  @click="handleSetupColProps"
                />
              </Tooltip>
            </template>
            <template #imgPrompt="{ record }">
              <div style="text-align: center">
                <img
                  :style="[
                    { width: '26px', height: '26px' },
                    { display: record['url'] ? '' : 'none' },
                  ]"
                  :src="record['url'] ? record['url'] : ''"
                />
              </div>
            </template>
            <template #iconCtl="{ record, column }">
              <div style="text-align: center" v-if="record['icon_meta_data'].url != undefined">
                <img
                  :style="[
                    { width: '26px', height: '26px' },
                    { display: record[column.dataIndex + '_meta_data'] ? 'inline' : 'none' },
                  ]"
                  :src="record['buf']"
                />
                <!-- :src="
                    record[column.dataIndex + '_meta_data']
                      ? record[column.dataIndex + '_meta_data'].url
                      : ''
                  " -->
              </div>
            </template>
            <template #title="{ record }">
              <div style="text-align: left">
                <span
                  :style="[
                    { color: record['color'] },
                    { fontWeight: record['is_bold'] == 1 ? 'bold' : 'normal' },
                  ]"
                >
                  {{ record['title'] }}
                </span>
              </div>
            </template>
            <template #iconFont="{ record }">
              <div style="text-align: center">
                <i :class="record['icon_font'] ? 'fa ' + record['icon_font'] : ''"></i>
              </div>
            </template>
            <template #attachmentCtl="{ record, column }">
              <span v-if="record[column.dataIndex + '_meta_data'] != undefined">
                <a
                  v-if="record[column.dataIndex + '_meta_data'].previewUrl"
                  @click="handlePreview(record[column.dataIndex + '_meta_data'])"
                  >{{ record[column.dataIndex + '_meta_data'].attachName }}
                </a>
                <a v-else @click="handleDownload(record[column.dataIndex + '_meta_data'])">
                  {{ record[column.dataIndex + '_meta_data'].attachName }}
                </a>
              </span>
              <span v-else>{{ record[column.dataIndex] }}</span>
            </template>
            <template #colorPicker="{ record }">
              <div
                :style="[
                  { display: 'inline-block', width: '16px', height: '16px' },
                  { backgroundColor: record['bg_color'] },
                ]"
              ></div>
            </template>
            <template #action="{ record }">
              <div
                class="cursor-pointer pl-2 pr-2"
                :class="{
                  'flex justify-between': record['colOperate'] && record['colOperate'].length > 1,
                }"
              >
                <template v-for="(item, index) in record['colOperate']" :key="item.id">
                  <span v-if="item.type == 'DEL'">
                    <Tooltip :title="item['name']">
                      <Popconfirm
                        placement="left"
                        :title="`确定${item.name}吗？`"
                        ok-text="确定"
                        cancel-text="取消"
                        @confirm="handleDelete(record)"
                      >
                        <Icon
                          icon="ant-design:delete-outlined"
                          v-if="item.type == 'DEL'"
                          style="color: #f00; margin-right: 10px"
                        />
                      </Popconfirm>
                    </Tooltip>
                  </span>
                  <span
                    v-else
                    @click="
                      item.type == 'EDIT'
                        ? handleEdit(record)
                        : item.type == 'SHOW'
                        ? handleView(record)
                        : item.type == 'HISTORY'
                        ? handleHistory(record)
                        : item.type == 'LOG_EDIT'
                        ? handleLogEdit(record)
                        : item.type == 'LOG_SHOW'
                        ? handleLogShow(record)
                        : item.type == 'ROLL_BACK'
                        ? handleRollBack(record)
                        : handleColOperate(item, record)
                    "
                  >
                    <Tooltip :title="item['name']">
                      <Icon
                        icon="ant-design:edit-outlined"
                        v-if="item.type == 'EDIT'"
                        style="color: #0960bd"
                      />
                      <Icon
                        icon="clarity:info-standard-line"
                        v-if="item.type == 'SHOW'"
                        style="color: #0960bd"
                      />
                      <Icon
                        icon="clarity:check-circle-line"
                        v-if="item.type == 'FLOW_SHOW'"
                        style="color: #0960bd"
                      />
                      <Icon
                        icon="clarity:replay-all-line"
                        v-if="item.type == 'ROLL_BACK'"
                        style="color: #0960bd"
                      />
                      <i
                        :class="item.icon ? 'fa ' + item.icon : ''"
                        :style="[
                          {
                            color: item.color ? item.color : '',
                          },
                        ]"
                      ></i>
                    </Tooltip>
                  </span>
                </template>
              </div>
            </template>
            <template #summary v-if="sumData && Object.keys(sumData).length">
              <TableSummary fixed>
                <TableSummaryRow>
                  <TableSummaryCell :index="0">合计</TableSummaryCell>
                  <TableSummaryCell
                    :index="1"
                    v-for="(item, index) in tableSummaryColumns"
                    :key="`TableSummaryCell${index}`"
                    class="text-center"
                  >
                    <template v-if="getSumField(item.dataIndex)">
                      {{ sumData[item.dataIndex] }}
                    </template>
                  </TableSummaryCell>
                </TableSummaryRow>
              </TableSummary>
            </template>
          </BasicTable>
        </div>
      </div>
    </div>
    <SmartModuleDrawer
      @register="registerDrawer"
      @success="handleSuccess"
      @close="initWindowFunc"
      @edit-action="handleOpenEditDrawer"
      @launch-flow-action="handleLaunchFlowInDrawer"
    />
    <ProcessDrawer @register="registerProcessDrawer" @success="handleSuccess" />
    <ProcessShowDrawer @register="registerProcessShowDrawer" @handle-current="handleEditFlow" />
    <DistributeModal @register="registerDistributeModal" @success="handleDistributeCallBack" />
    <RollBackModal @register="registerRollBackModal" @success="handleRollBackCallBack" />
    <SelFormViewModal @register="registerSelFormViewModal" @success="handleSelFormViewCallBack" />
    <SmartModuleModal
      @register="registerSmartModuleModal"
      @success="handleSmartModuleCallBack"
      @close="initWindowFunc"
    />
    <SmartModuleSelDrawer
      @register="registerSmartModuleSelDrawer"
      @success="handleSmartModuleSelCallBack"
    />
    <ExportSelFieldModal @register="registerModalExport" @async-download="handlePollDownload" />
    <ExportExcelProgressModal @register="registerModalExportExcelPgogress" />
    <SetModuleColumnModal @register="registerModalSetColumn" @success="handleSuccessSetColumn" />

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
  </PageWrapper>
</template>
<script lang="ts">
  // 列表取数据：visual/list
  // 字段名 + "_link"表示 钻取的地址，如果为show，则直接指向详情页

  // 操作列按钮：
  // type为SHOW，打开详情页，如果link不为空，则打开link中的路由
  // type为EDIT，打开编辑页
  // type为DEL，删除记录
  // type为FLOW，打开流程详情页，flowId表示流程ID
  // type为HISTORY，打开历史记录页
  // type为LOG_EDIT，打开修改日志页面
  // type为LOG_SHOW，打开浏览日志页面

  // 自定义按钮：
  // type为script，表示点击型按钮，script中为javascript脚本
  // type为batchBtn，表示批处理按钮
  // type为flowBtn，表示流程发起按钮，flowTypeCode中为流程类型
  import {
    defineComponent,
    onMounted,
    onActivated,
    onDeactivated,
    onUnmounted,
    ref,
    unref,
    watch,
    computed,
    h,
    inject,
    nextTick,
  } from 'vue';
  import CardView from '../../dashboard/analysis/comps/CardView.vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import {
    getVisualList,
    getVisualListPage,
    getVisualDel,
    getVisualBatchOp,
    getVisualModuleEditInPlace,
    getVisualExportExcel,
    getVisualImportExcel,
    getVisualExportWord,
    getVisualDownloadZip,
    getVisualCopy,
    getVisualDownloadExcelTempl,
    getVisualDownload,
    getVisualExportExcelAsync,
  } from '/@/api/module/module';
  import { getFlowInit, getDownload } from '/@/api/process/process';
  import ExportSelFieldModal from './modules/ExportSelFieldModal.vue';
  import ExportExcelProgressModal from './modules/ExportExcelProgressModal.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import {
    Tabs,
    TabPane,
    Button,
    Form,
    Row,
    Col,
    Popconfirm,
    Tooltip,
    Modal,
    Dropdown,
    Menu,
    MenuItem,
    MenuProps,
    Spin,
    Table,
  } from 'ant-design-vue';
  import Icon from '/@/components/Icon/index';
  import SetModuleColumnModal from './modules/SetModuleColumnModal.vue';

  import {
    DeleteOutlined,
    EditOutlined,
    SettingOutlined,
    DownSquareOutlined,
    UpSquareOutlined,
    CopyOutlined,
    FileZipOutlined,
    FileWordOutlined,
  } from '@ant-design/icons-vue';

  import { PageWrapper } from '/@/components/Page';

  import SmartModuleDrawer from './modules/smartModuleDrawer.vue';
  import SmartModuleModal from './modules/smartModuleModal.vue';
  import RollBackModal from './modules/RollBackModal.vue';
  import SmartModuleSelDrawer from './modules/smartModuleSelDrawer.vue';
  import { useMultipleTabWithOutStore, useMultipleTabStore } from '/@/store/modules/multipleTab';

  import { useGo } from '/@/hooks/web/usePage';
  import ProcessDrawer from '../processManagement/processDrawer.vue';
  import ProcessShowDrawer from '../processManagement/processShowDrawer.vue';
  import { downloadByData } from '/@/utils/file/download';
  import { ImpExcel } from '/@/components/Excel';
  import { useTabs } from '/@/hooks/web/useTabs';
  import {
    myMsg,
    filterJS,
    createCss,
    removeLink,
    myConfirm,
    removeScript,
    ajaxGetJS,
    ajaxPost,
    ajaxGet,
    initFormCtl,
    loadImg,
    loadImgInJar,
    o,
  } from '/@/utils/utils';
  import smartCalendar from './smartCalendar.vue';
  import DistributeModal from '../processManagement/modules/DistributeModal.vue';
  import SelFormViewModal from '../processManagement/modules/SelFormViewModal.vue';
  import { useModal } from '/@/components/Modal';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getShowImgInJar } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';

  interface ActiveKey {
    id: string;
    type: string;
  }

  // interface NodePriv {
  //   priv: 'see' | 'add' | 'edit' | 'del';
  //   have: boolean;
  // }

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModule',
    components: {
      BasicTable,
      Tabs,
      TabPane,
      PageWrapper,
      Button,
      Form,
      FormItem: Form.Item,
      Row,
      Col,
      SmartModuleDrawer,
      Popconfirm,
      Icon,
      ProcessDrawer,
      ImpExcel,
      ProcessShowDrawer,
      smartCalendar,
      CardView,
      Tooltip,
      DistributeModal,
      DeleteOutlined,
      EditOutlined,
      DownSquareOutlined,
      UpSquareOutlined,
      CopyOutlined,
      SettingOutlined,
      SelFormViewModal,
      SmartModuleModal,
      FileWordOutlined,
      FileZipOutlined,
      RollBackModal,
      SmartModuleSelDrawer,
      Modal,
      Menu,
      Dropdown,
      MenuItem,
      ExportSelFieldModal,
      SetModuleColumnModal,
      Spin,
      TableSummary: Table.Summary,
      TableSummaryRow: Table.Summary.Row,
      TableSummaryCell: Table.Summary.Cell,
      ExportExcelProgressModal,
    },
    props: {
      start: {
        type: [String, Number],
        default: 0, //默认为0是请求, 5是树形右边智能模块, 6是首页智能模块
      },
      moduleCode: {
        type: String,
        default: '',
      },
      isTreeView: {
        type: Boolean,
        default: false,
      },
      treeNodeCode: {
        type: String,
        default: '',
      },
      showSearch: {
        type: Boolean,
        default: true,
      },
      pagination: {
        type: Boolean,
        default: true,
      },
      showToolbar: {
        type: Boolean,
        default: true,
      },
      showOpCol: {
        type: Boolean,
        default: true,
      },
      // treeNodePriv: {
      //   type: Array as PropType<NodePriv[]>,
      //   default: function (): Array<NodePriv> {
      //     return [];
      //   },
      // },
    },
    setup(props) {
      const { createMessage, createConfirm } = useMessage();
      const [registerDrawer, { openDrawer }] = useDrawer();
      const [registerProcessDrawer, { openDrawer: openProcessDrawer }] = useDrawer();
      const [registerProcessShowDrawer, { openDrawer: openProcessShowDrawer }] = useDrawer();
      const { setTitle, closeCurrent } = useTabs();
      const userStore = useUserStore();

      const multipleTabStore = useMultipleTabWithOutStore();
      const tabStore = useMultipleTabStore();

      const aryTab = ref([]);
      const hasTab = ref(false);
      const activeKey = ref<ActiveKey>({
        id: '',
        type: '',
      });
      const moduleCode = ref('');
      const isShowSearch = ref(false);
      const searchInfo = ref<any>({});
      let flowColumns = ref<any>([]);
      let nestColumns = ref<any>([]);
      const srcId = ref('-src');
      const aryCond = ref([]);
      const isSort = ref(false);
      const treeNodeCode = ref('');
      const dataSource = ref();
      const isShowAllCond = ref(false); // 是否显示全部的条件
      let moduleCodeForLog = '';
      const { t } = useI18n();
      const importVisible = ref(false);
      const isDownload = ref(false);

      const curFormUtil: any = inject('curFormUtil');
      const getFormName = computed(() => 'customForm' + curFormUtil?.getFormNo());
      console.log('getFormName', getFormName.value);

      const isLaunching = ref(false);
      const sumData: any = ref({});

      let serverInfo = userStore.getServerInfo;

      const showIndexColumn = true;
      const [
        registerTable,
        {
          reload,
          redoHeight,
          setSelectedRowKeys,
          setProps,
          getSelectRowKeys,
          getSelectRows,
          getColumns,
          getRowSelection,
        },
      ] = useTable({
        title: '',
        api: getVisualList,
        immediate: false,
        striped: true,
        rowKey: 'id',
        rowClassName: getRowClsName, //设置每行样式
        beforeFetch: (info) => {
          let newInfo = info;
          console.log('beforeFetch newInfo1', newInfo);
          newInfo = Object.assign({}, newInfo, formParams.value);
          searchInfo.value = { ...newInfo };
          if (unref(isSort)) {
            newInfo.field = null;
            newInfo.order = null;
            isSort.value = false;
          }
          console.log('beforeFetch newInfo2', newInfo);
          return newInfo;
        },
        afterFetch: async (data) => {
          data = data || [];
          let sumRow: Recordable = data[data.length - 1];
          console.log('sumRow', sumRow);
          if (Reflect.has(sumRow, '_row')) {
            sumData.value = sumRow;
            data.splice(data.length - 1);
          }
          console.log('sumData', sumData);

          dataSource.value = data;
          console.log('dataSource.value', dataSource.value);
          dataSource.value.forEach((item) => {
            if (
              item.icon_meta_data &&
              item.icon_meta_data.url &&
              item.icon_meta_data.url.length > 0
            ) {
              let showImgStr = 'showImgInJar';
              let p = item.icon_meta_data.url.indexOf(showImgStr);
              if (p != -1) {
                p = item.icon_meta_data.url.indexOf('path=');
                let path = item.icon_meta_data.url.substring(p + 'path='.length);
                console.log('path', path);
                getShowImgInJar({ path: path }).then((res2) => {
                  item.buf = bufToUrl(res2)!;
                });
              }
            }
          });
          console.log('afterFetch', data);
        },
        showTableSetting: true,
        tableSetting: {
          setting: false,
        },
        // scroll: { y: 300 },
        bordered: true,
        showIndexColumn,
        indexColumnProps: { width: 50 },
        clickToRowSelect: false,
        customRow: customRow,
      });
      let source = 0; // 源目标数据序号
      let target = 0; // 目标数据序号
      // Table行拖拽
      function customRow(record, index) {
        // 只有当具有管理权限时才启用
        // if (!listPage.value.canUserManage) {
        //   return;
        // }
        return {
          props: {
            // draggable: 'true'
          },
          style: {
            // cursor: 'pointer',
          },
          // 鼠标移入
          onMouseenter: (event) => {
            // event.srcElement.tagName始终为TR，无法区分是点在了哪一列
          },
          onMousedown: (event) => {
            if (event.srcElement.tagName === 'TD') {
              let cellIndex = event.srcElement.cellIndex;
              // 如果点击的是第1列（第0列为复选框列），则使可以拖动
              // 因拖拽会影响拷贝粘贴，故限制只能在第1列可拖拽
              if (cellIndex == 1) {
                event.target.draggable = true; // 使可以拖动
              }
            }
          },
          // 开始拖拽
          onDragstart: (event) => {
            // 兼容IE
            let ev = event || window.event;
            // 阻止冒泡
            ev.stopPropagation();
            // 得到源目标数据序号
            source = index;
            console.log(record, index, 'source');
          },
          // 拖动元素经过的元素
          onDragover: (event) => {
            // 兼容 IE
            let ev = event || window.event;
            // 阻止默认行为
            ev.preventDefault();
          },
          // 鼠标松开
          onDrop: (event) => {
            // 兼容IE
            let ev = event || window.event;
            // 阻止冒泡
            ev.stopPropagation();
            // 得到目标数据序号
            target = index;
            // 数据位置互换，让视图更新 可以看record，index的输出
            [dataSource.value[source], dataSource.value[target]] = [
              dataSource.value[target],
              dataSource.value[source],
            ];
            // console.log(record, index, 'target', source, target);
          },
          onDblclick: () => {
            handleDbClick(record);
          },
        };
      }

      function customCell(record, index, column) {
        console.log('customCell column', column);
        return {
          style: {
            color: 'blue', //这里将名称变了下色
          },
          onClick: () => {
            //点击事件，也可以加其他事件
            console.log('here');
          },
        };
      }

      const { currentRoute, replace, push } = useRouter();
      let listPage = ref<any>({});

      onMounted(async () => {
        console.log('props.treeNodePriv', props.treeNodePriv);

        if (props.start == 0) {
          console.log('currentRoute.value', currentRoute.value);
          // 如果是从菜单进入
          if (JSON.stringify(currentRoute.value.query) === '{}') {
            let { formCode } = currentRoute.value.meta;
            moduleCode.value = formCode as string;
            await getListPage();
          } else {
            // 如果是调用了goTo方法，从静态路由进入
            await getListPage(currentRoute.value.query);
            // 置选项卡的标题，注意直接通过currentRoute.meta.title修改无效
            if (currentRoute.value.query.title) {
              // 置表格的标题
              setTitle(currentRoute.value.query.title);
              // 置第一个选项卡的标题
              if (hasTab.value && aryTab.value.length > 1) {
                aryTab.value[0].name = currentRoute.value.query.title;
              }
            } else {
              setTitle(listPage.value.moduleName);
            }
          }
        }

        initWindowFunc();
      });

      let isInited = false;
      onActivated(async () => {
        console.log('smartModule onActivated', listPage);
        setTimeout(async () => {
          if (unref(listPage).moduleCode) {
            const cacheModuleCodeList = multipleTabStore.getCacheModuleCodeList;
            if (cacheModuleCodeList.get(unref(listPage).moduleCode)) {
              // await getListPage();
              await searchData();
              multipleTabStore.addCacheModuleCode(unref(listPage).moduleCode, false);
            }
          }
          initWindowFunc();

          // 当点击菜单时，使刷新list
          // if (multipleTabStore.isRefreshPage) {
          //   if (isInited) {
          //     reload();
          //   }
          //   multipleTabStore.updateIsRefreshPage(false);
          // }

          try {
            // 用于form_js中的定时器检测是否可继续执行
            eval('onActivated_' + listPage.value.formCode + '(2, moduleCode.value)');
          } catch (e) {
            // console.error(e);
            console.warn('form_js_' + listPage.value.formCode + ' 中无onActivated方法');
          }
        }, 100);
      });

      onDeactivated(() => {
        console.log('smartModule onDeactivated');
        try {
          // 用于form_js中的定时器检测是否可继续执行
          eval('onDeactivated_' + listPage.value.formCode + '(2, moduleCode.value)');
        } catch (e) {
          // console.error(e);
          console.warn('form_js_' + listPage.value.formCode + ' 中无onDeactivated方法');
        }
      });

      onUnmounted(() => {
        console.log('Unmounted in smartModule!');
        removeScript(srcId.value);
        removeLink();
        curFormUtil.close(getFormName.value);
      });

      function getServerUrl() {
        return userStore.getServerUrl;
      }

      function expandSearchForm() {
        isShowAllCond.value = !isShowAllCond.value;
        // 如果表格高度设为自适应，则需重新调整高度
        if (listPage.value.isAutoHeight) {
          redoHeight();
        }
      }

      function setSearchFromClass() {
        // 设置搜索条件框中的tabIndex及使focus时高亮
        let searchForm = $('[name=' + getFormName.value + ']')[0];
        if (!searchForm) {
          console.warn('setSearchFromClass search form: ' + getFormName.value + ' is not exist.');
          return;
        }

        $(searchForm)
          .find('input')
          .each(function () {
            if ($(this).attr('kind') == 'DATE' || $(this).attr('kind') == 'DATE_TIME') {
              $(this).attr('autocomplete', 'off');
            }
          });

        let elements = searchForm.elements;
        let k = 1;
        for (let i = 0; i < elements.length; i++) {
          let element = elements[i];
          // 判断是否为隐藏元素
          let $obj = $(element);
          if (
            (element.type != 'hidden' && element.tagName == 'INPUT') ||
            element.tagName == 'SELECT'
          ) {
            $obj.attr('tabindex', k);
            $obj.addClass('ant-input-affix-wrapper');
            $obj
              .focus(function () {
                $(this).addClass('ant-input-affix-wrapper-focus');
              })
              .blur(function () {
                $(this).removeClass('ant-input-affix-wrapper-focus');
              });
            k++;
          }
        }
      }

      function getRowClsName(record, index) {
        try {
          return getRowClassName(record, index, listPage.value.moduleCode);
        } catch (e) {
          console.log('form_js文件中未定义方法 getRowClassName');
        }
      }

      function getChkboxProps(record) {
        try {
          return getCheckboxProps(record);
        } catch (e) {
          console.log('form_js文件中未定义方法 getChkboxProps');
        }
        // return {
        //   disabled: record.ID == 96094,
        //   // 默认选中
        //   defaultChecked: record.state == 1,
        // };
      }

      function getCurFormId() {
        // 注意可能会造成有的页面中忘了设置getCurFormId，导致fo调用getCurFormId时将本页的formName认为是当前form
        // return getFormName.value;
        return curFormUtil?.get();
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

        newWindow.goTo = goTo;
        newWindow.reload = reload;
        newWindow.myMsg = myMsg;
        newWindow.openSmartModuleDrawerForShow = openSmartModuleDrawerForShow;
        newWindow.openDistributeModal = openDistributeModal;
        newWindow.myConfirm = myConfirm;
        newWindow.copyRecords = copyRecords;
        newWindow.initFormCtl = initFormCtl;
        newWindow.getIdsSelected = getIdsSelected;
        newWindow.openDialog = openDialog;
        newWindow.searchData = searchData;
        newWindow.openSmartModuleSelTableDrawerOnListPage = openSmartModuleSelTableDrawer;
        newWindow.launchFlow = launchFlow;
        newWindow.ajaxPost = ajaxPost;
        newWindow.ajaxGet = ajaxGet;
        newWindow.ajaxGetJS = ajaxGetJS;

        newWindow.loadImgInJar = loadImgInJar;
      }

      function getIdsSelected() {
        return getSelectRowKeys().join(',');
      }

      watch(
        () => props.moduleCode,
        (newVal) => {
          console.log('props.moduleCode', newVal, 'props.start', props.start);
          if ((props.start == 5 || props.start == 6) && newVal) {
            console.log('props.moduleCode===>', props.moduleCode);
            moduleCode.value = newVal;
            console.log('moduleCode.value newVal===>', moduleCode.value);
            // 防止重复请求服务器端
            if (!props.treeNodeCode) {
              getListPage();
            }
          }
        },
        {
          immediate: true,
        },
      );

      watch(
        () => props.treeNodeCode,
        (newVal) => {
          console.log('props.start', props.start, 'props.treeNodeCode', props.treeNodeCode);
          // 如果是树形视图
          if (props.start == 5 && newVal) {
            console.log('treeNodeCode.value===>', treeNodeCode.value);
            treeNodeCode.value = newVal;
            console.log('treeNodeCode.value newVal===>', treeNodeCode.value);
            console.log('moduleCode.value===>', moduleCode.value);
            if (moduleCode.value) {
              getListPage();
            } else {
              console.log(
                'watch: The moduleCode is empty. Waitting for moduleCode prop to change.',
              );
            }
          }
        },
      );

      function loadJs(src, scriptId) {
        return new Promise((resolve, reject) => {
          let script = document.createElement('script');
          script.type = 'text/javascript';
          script.id = scriptId;
          script.onload = () => {
            resolve(true);
          };
          script.onerror = () => {
            reject('js load error');
          };
          script.src = src;
          document.getElementsByTagName('body')[0].appendChild(script);
        });
      }

      async function getListPage(params = {}) {
        isInited = false;
        console.log('getListPage params', params);
        await getVisualListPage({
          moduleCode: moduleCode.value,
          treeNodeCode: treeNodeCode.value,
          isTreeView: props.isTreeView,
          ...params,
        }).then((res) => {
          setResult(res);
        });
      }

      const go = useGo();
      async function setResult(res: any) {
        listPage.value = res;

        isShowAllCond.value = listPage.value.isAutoExpand;

        // 如果配置了树形视图，且属性中的moduleCode为空，则说明需转至smartModuleTreeViewPage
        // 如果属性中的moduleCode不为空，说明已经在smartModuleTreeView组件中被赋予了该属性，那就不能再replace路由了，会导致循环调用
        if (listPage.value.view === 'tree' && !props.moduleCode) {
          // 关闭选项卡
          closeCurrent();

          // 效果看起来最好
          replace({
            path: '/smartModuleTreeViewPage',
            query: { treeBasicCode: listPage.value.treeBasicCode, moduleCode: moduleCode.value },
          });

          // 会开启一个新的选项卡
          // go(
          //   {
          //     path: '/smartModuleTreeViewPage',
          //     query: {
          //       code: listPage.value.treeBasicCode,
          //     },
          //   },
          //   true,
          // );

          // 与go效果一样
          // push({
          //   name: 'Redirect',
          //   params: { path: '/smartModuleTreeViewPage' },
          //   query: { code: listPage.value.treeBasicCode },
          // });
          return;
        }

        aryCond.value = res.aryCond && res.aryCond.length > 0 ? res.aryCond : [];

        if (!isInited) {
          aryTab.value = res.aryTab;
          hasTab.value = res.hasTab;
          activeKey.value.id = res.aryTab && res.aryTab.length > 0 ? res.aryTab[0].id : '';
          activeKey.value.type = res.aryTab && res.aryTab.length > 0 ? res.aryTab[0].type : '';

          if (aryCond.value.length > 0) {
            let isFound = false;
            let wSum = 0;
            let k = 0;
            aryCond.value.forEach((item) => {
              item.expand = false;
              if (item.width == '') {
                item.width = '6';
              }
              if (!isFound) {
                wSum += parseInt(item.width);
                if (wSum == 24) {
                  item.expand = true;
                  isFound = true;
                } else if (wSum > 24) {
                  aryCond.value[k - 1].expand = true;
                  isFound = true;
                }
                k++;
              }
            });

            nextTick(() => {
              setSearchFromClass();
              isShowSearch.value = true;
            });
          }
        }
        console.log('activeKey.value', activeKey.value);
        console.log('aryCond.value', aryCond.value);

        // if (listPage.value.view === 'calendar') return;

        if (activeKey.value.type == 'calendar') {
          isInited = true;
          return;
        }

        // let script1 = document.createElement('script');
        // script1.type = 'text/javascript';
        // script1.src = url;
        // script1.id = `${100}${srcId.value}`;
        // document.getElementsByTagName('head')[0].appendChild(script1);

        // 加载后台事件中配置的前台脚本
        if (listPage.value.formJs && listPage.value.formJs.length > 0) {
          let scriptFormJs = document.createElement('script');
          scriptFormJs.type = 'text/javascript';
          scriptFormJs.text = listPage.value.formJs;
          scriptFormJs.id = `${101}${srcId.value}`;
          document.getElementsByTagName('head')[0].appendChild(scriptFormJs);
        }

        if (listPage.value.aryTab && listPage.value.aryTab.length > 0) {
          activeKey.value.type = activeKey.value.type
            ? activeKey.value.type
            : listPage.value.aryTab[0].type;
          activeKey.value.id = activeKey.value.id
            ? activeKey.value.id
            : listPage.value.aryTab[0].id;
        }

        if (listPage.value.isColCheckboxShow) {
          setProps({
            rowSelection: {
              type: 'checkbox',
              getCheckboxProps: getChkboxProps,
            },
          });
        }

        // 表格左上方显示名称
        setProps({ title: listPage.value.moduleName });

        listPage.value.aryEditable =
          listPage.value.aryEditable && listPage.value.aryEditable.length > 0
            ? listPage.value.aryEditable
            : [];
        listPage.value.aryEditableOpt =
          listPage.value.aryEditableOpt && listPage.value.aryEditableOpt.length > 0
            ? listPage.value.aryEditableOpt
            : [];

        flowColumns.value = [];
        if (listPage.value.colProps && listPage.value.colProps.length > 0) {
          listPage.value.colProps.forEach((item) => {
            let type =
              listPage.value.aryEditable.filter((el) => el.fieldName === item.field).length > 0
                ? listPage.value.aryEditable.filter((el) => el.fieldName === item.field)[0].type
                : 'text';
            let fieldName =
              listPage.value.aryEditable.filter((el) => el.fieldName === item.field).length > 0
                ? listPage.value.aryEditable.filter((el) => el.fieldName === item.field)[0]
                    .fieldName
                : '';
            let options =
              listPage.value.aryEditableOpt.filter((el) => el.fieldName === item.field).length > 0
                ? listPage.value.aryEditableOpt.filter((el) => el.fieldName === item.field)[0]
                    .options
                : [];
            // if (item.title == '操作') {
            if (item.field == 'colOperate') {
              flowColumns.value.push({
                title: item.title,
                dataIndex: item.field,
                align: item.align || 'center',
                fixed: item.fixed,
                width: item.width,
                ellipsis: true,
                slots: { customRender: 'action' },
              });
            } else if (item.field == 'colPrompt') {
              flowColumns.value.push({
                title: item.title,
                dataIndex: item.field,
                align: 'center',
                fixed: item.fixed,
                width: 42,
                ellipsis: true,
                resizable: true,
                slots: { customRender: 'imgPrompt' },
              });
            } else if (item.field == 'title') {
              flowColumns.value.push({
                title: item.title,
                dataIndex: item.field,
                align: 'center',
                fixed: item.fixed,
                width: item.width,
                ellipsis: true,
                slots: { customRender: 'title' },
                resizable: true,
              });
            } else if (item.macroType == 'macro_icon_font_select_ctl') {
              flowColumns.value.push({
                title: item.title,
                dataIndex: item.field,
                align: 'center',
                fixed: item.fixed,
                width: item.width,
                ellipsis: true,
                slots: { customRender: 'iconFont' },
              });
            } else if (item.macroType == 'macro_color_picker_ctl') {
              flowColumns.value.push({
                title: item.title,
                dataIndex: item.field,
                align: 'center',
                fixed: item.fixed,
                width: item.width,
                ellipsis: true,
                slots: { customRender: 'colorPicker' },
              });
            } else if (item.macroType == 'macro_icon_ctl') {
              flowColumns.value.push({
                title: item.title,
                dataIndex: item.field,
                align: 'center',
                fixed: item.fixed,
                width: item.width,
                ellipsis: true,
                slots: { customRender: 'iconCtl' },
              });
            } else if (item.macroType == 'macro_attachment') {
              flowColumns.value.push({
                title: item.title,
                dataIndex: item.field,
                align: 'center',
                fixed: item.fixed,
                width: item.width,
                ellipsis: true,
                slots: { customRender: 'attachmentCtl' },
              });
            } else if (item.type != 'checkbox') {
              if (type === 'select') {
                flowColumns.value.push({
                  title: item.title,
                  dataIndex: item.field,
                  align: item.align || 'center',
                  width: item.width,
                  ellipsis: true,
                  sorter: item.sort,
                  editComponent: 'Select',
                  edit: listPage.value.isEditInplace && fieldName === item.field,
                  editComponentProps: {
                    showSearch: true,
                    filterOption: filterOption,
                    fieldNames: { label: 'name' },
                    options: options,
                  },
                  resizable: true,
                });
              } else {
                flowColumns.value.push({
                  title: item.title,
                  dataIndex: item.field,
                  align: item.align || 'center',
                  width: item.width,
                  ellipsis: true,
                  sorter: item.sort,
                  editComponent: type === 'DATE' ? 'DatePicker' : 'Input',
                  edit: listPage.value.isEditInplace && fieldName === item.field,
                  editComponentProps: {},
                  resizable: true,
                  // slots: { customRender: item.field },
                  customRender: function ({ text, record }) {
                    if (record[item.field + '_link']) {
                      let type = record[item.field + '_link'].type;
                      if (type) {
                        return h(
                          'span',
                          {
                            class: 'cursor-pointer hover:underline',
                            onClick: () => {
                              if (type === 'SHOW') {
                                handleDbClick(record);
                              } else if (type === 'FLOW') {
                                // 查看流程详情
                                openProcessShowDrawer(true, {
                                  flowId: record.flowId,
                                  visitKey: record[item.field + '_link'].visitKey,
                                });
                              } else if (type === 'LINK') {
                                go({
                                  path: record[item.field + '_link'].route,
                                  query: record[item.field + '_link'].query,
                                });
                              }
                            },
                          },
                          text,
                        );
                      }
                    } else {
                      return text;
                    }
                  },
                });
              }
            }
          });
          // flowColumns.value.push({
          //   title: 'id',
          //   dataIndex: 'id',
          //   width: 0,
          //   // ifShow: false,
          // });
          console.log('flowColumns', flowColumns);
        }

        nestColumns.value = [];
        if (listPage.value.nestColPropsAry && listPage.value.nestColPropsAry.length > 0) {
          listPage.value.nestColPropsAry.forEach((itemAry) => {
            let ary: any = [];
            itemAry.forEach((item) => {
              if (item.type != 'checkbox') {
                ary.push({
                  title: item.title,
                  dataIndex: item.field,
                  align: item.align || 'center',
                  width: item.width,
                  ellipsis: true,
                  sorter: item.sort,
                  edit: false,
                });
              }
            });
            nestColumns.value.push(ary);
          });
        }

        moduleCode.value = listPage.value.moduleCode;

        // 如果是从树形视图跳转进来的，因为走了静态路由，所以需改选项卡标题
        // 如果是直接在菜单上面配了树形组件，则不需要在模块中再配成树形视图，以保持选项卡标题为菜单名称
        if (props.start == 5 && listPage.value.view === 'tree') {
          setTitle(listPage.value.moduleName);
        }

        console.log('setResult isInited', isInited);

        if (!isInited) {
          // 动态加载css
          if (listPage.value.pageCss) {
            console.log('pageCss', listPage.value.pageCss);
            createCss(listPage.value.pageCss);
          }

          if (listPage.value.aryCond && listPage.value.aryCond.length > 0) {
            setTimeout(() => {
              listPage.value.aryCond.forEach((item) => {
                filterJS(item.ctlHtml, '-src', o(getFormName.value)).then(() => {
                  // loadImg('.select2-results');
                });
                if (item.script) {
                  eval(item.script);
                }
                if (item.defaultValue != '') {
                  $(o(getFormName.value))
                    .find('[name="' + item.fieldName + '"]')
                    .val(item.defaultValue);
                }
              });

              // 初始化条件字段中的日期控件
              initDatePicker();

              // 放在setTimeout中（否则aryCond中的条件还没有在dom中生成），使searchData中能够包含aryCond中的默认条件，如：年份默认为当年
              searchData();
            }, 300);
          } else {
            searchData();
          }
        } else {
          searchData();
        }

        // 列表页上的form_js注意得加上_list
        let rootPath = import.meta.env.VITE_PUBLIC_PATH;
        if (rootPath.endsWith('/')) {
          rootPath = rootPath.substring(0, rootPath.lastIndexOf('/'));
        }
        // 列表页上的form_js注意得加上_list
        const url = `${rootPath}/resource/js/form/form_js_${listPage.value.formCode}_list.js?pageType=moduleList&moduleCode=${moduleCode.value}`;
        console.log('list form_js url', url);

        // 此方法相对于直接appendChild，能知道js是否加载成功
        loadJs(url, `${100}${srcId.value}`).catch((e) => {
          // console.log('e', e);
          console.warn(`Form: ${listPage.value.formCode}'s js is not exist.`);
        });

        // 为向下兼容，引入服务端form_js
        await ajaxGetJS(
          `/flow/form_js/form_js_${listPage.value.formCode}.jsp?pageType=moduleList&moduleCode=${moduleCode.value}`,
        );

        console.log('setResult after ajaxGetJS.');
        // 如果在form_js中对表格的行设置了getRowClassName函数，需将striped属性置为false，否则会被vben自定义的-row__striped样式覆盖
        if (typeof getRowClassName == 'function') {
          console.log('form_js has getRowClassName function.');
          setProps({ striped: false });
        }

        isInited = true;
      }

      //调整合计头部
      const tableSummaryColumns = computed(() => {
        let columns = [];
        // 在需要合计的表头，加summary为true
        if (listPage.value.isExpand) {
          columns.push({
            title: '+',
            dataIndex: 'zk',
          });
        }
        if (getRowSelection()) {
          columns.push({
            title: '选择',
            dataIndex: 'xz',
          });
        }
        if (showIndexColumn) {
          columns.push({
            title: '序号',
            dataIndex: 'xh',
          });
        }
        // 使TableSummaryCell中的“合计”能够放在+、选择或序号列
        if (columns.length > 0) {
          columns.splice(0, 1);
        }
        const oflowColumns = flowColumns.value.filter((item) => item.title != 'id');
        columns = [...columns, ...oflowColumns];
        return columns;
      });

      //切换顶部tab
      function getActiveKey(key: string) {
        let record: any = aryTab.value.find((item) => item.id === key);
        activeKey.value.type = record.type;
        if (activeKey.value.type == 'logList' || activeKey.value.type == 'logListRead') {
          moduleCode.value = record?.params.moduleCodeLog;
          moduleCodeForLog = record?.params.moduleCode;
        } else {
          moduleCode.value = record?.params.moduleCode;
        }
        // 使清空原来的排序，以免带到新点击的tab对应的模块中
        isSort.value = true;
        getListPage();
      }

      // ---------------------------------------------------------------------------表格工具栏开始-----------------------------------------------------------
      //新增列表
      function handleAdd() {
        if (listPage.value['opStyle'] == 1) {
          go({
            path: '/smartModuleAddEditView',
            query: {
              isTab: true,
              isUpdate: 1,
              parentPath: unref(currentRoute).path,
              moduleCode: listPage.value.moduleCode,
              formCode: listPage.value.formCode,
              treeNodeCode: treeNodeCode.value,
              isTreeView: props.isTreeView,
              titleName: `新增-${currentRoute.value.meta.title}`,
              cacheName: `smartModuleAddEditViewAdd`,
            },
          });
        } else {
          openDrawer(true, {
            isUpdate: 1,
            record: {
              moduleCode: listPage.value.moduleCode,
              formCode: listPage.value.formCode,
              treeNodeCode: treeNodeCode.value,
              isTreeView: props.isTreeView,
            },
          });
        }
      }

      //修改列表
      function handleEdit(record: object) {
        if (listPage.value['opStyle'] == 1) {
          let tabTitle = record['tabTitle'] ? record['tabTitle'] : currentRoute.value.meta.title;
          go({
            path: '/smartModuleAddEditView',
            query: {
              isTab: true,
              isUpdate: 2,
              parentPath: unref(currentRoute).path,
              moduleCode: listPage.value.moduleCode,
              id: record['id'],
              formCode: listPage.value.formCode,
              treeNodeCode: treeNodeCode.value,
              isTreeView: props.isTreeView,
              titleName: `编辑-${tabTitle}`,
              cacheName: `smartModuleAddEditViewEdit${record.id}`,
            },
          });
        } else {
          openDrawer(true, {
            isUpdate: 2,
            record: {
              moduleCode: listPage.value.moduleCode,
              id: record['id'],
              formCode: listPage.value.formCode,
              treeNodeCode: treeNodeCode.value,
              isTreeView: props.isTreeView,
            },
          });
        }
      }

      //单个删除
      function handleDelete(record: object) {
        getVisualDel({
          ids: record['id'],
          moduleCode: listPage.value.moduleCode,
          treeNodeCode: treeNodeCode.value,
          isTreeView: props.isTreeView,
        }).then(() => {
          handleSuccess();
        });
      }

      //查看详情
      function handleView(record: object) {
        if (listPage.value['opStyle'] == 1) {
          let tabTitle = record['tabTitle'] ? record['tabTitle'] : currentRoute.value.meta.title;
          go({
            path: '/smartModuleAddEditView',
            query: {
              isTab: true,
              isUpdate: 3,
              parentPath: unref(currentRoute).path,
              moduleCode: listPage.value.moduleCode,
              id: record['id'],
              formCode: listPage.value.formCode,
              treeNodeCode: treeNodeCode.value,
              isTreeView: props.isTreeView,
              titleName: `详情-${tabTitle}`,
              cacheName: `smartModuleAddEditViewDetail${record.id}`,
            },
          });
        } else {
          openDrawer(true, {
            isUpdate: 3,
            record: {
              moduleCode: listPage.value.moduleCode,
              id: record['id'],
              formCode: listPage.value.formCode,
              isTreeView: props.isTreeView,
              treeNodeCode: treeNodeCode.value,
            },
          });
        }
      }

      //批量删除
      async function deleteBatch() {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('请选择需要删除的数据');
          return;
        }
        await getVisualDel({
          ids: keys.join(','),
          moduleCode: listPage.value.moduleCode,
          treeNodeCode: treeNodeCode.value,
          isTreeView: props.isTreeView,
        }).then(() => {
          handleSuccess();
        });
      }

      async function copyRecords() {
        isCopy.value = true;
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('请选择数据');
          isCopy.value = false;
          return;
        }

        createConfirm({
          iconType: 'info',
          title: () => h('span', '提示'),
          content: () => h('span', '您确定要复制么'),
          maskClosable: false,
          onOk: async () => {
            await getVisualCopy({
              moduleCode: listPage.value.moduleCode,
              ids: keys.join(','),
            }).then(() => {
              isCopy.value = false;
              handleSuccess();
            });
          },
        });
      }

      //自定义按钮
      function batchOp(record: any) {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('请选择需要修改的数据');
          return;
        }
        let params = {
          moduleCode: listPage.value.moduleCode,
          ids: keys.join(','),
          batchField: record.batchField,
          batchValue: record.batchValue,
        };
        getVisualBatchOp(params).then(() => {
          handleSuccess();
        });
      }
      // ---------------------------------------------------------------------------表格工具栏结束-----------------------------------------------------------

      //页面回调
      function handleSuccess() {
        searchData();
        setSelectedRowKeys([]);
        createMessage.success(t('common.opSuccess'));
      }

      // ---------------------------------------------------------------------------顶部查询栏开始-----------------------------------------------------------

      const formParams = ref({});
      //查询
      async function searchData() {
        await nextTick();
        console.log('moduleCode.value=>', moduleCode.value);
        if (!!moduleCode.value) {
          $(fo('moduleCode')).empty();
          $(fo('moduleCode')).val(unref(moduleCode) as string);
        }

        $('#op').empty();
        $('#op').val(listPage.value.op);

        let data = $(o(getFormName.value)).serializeArray();
        console.log('searchData data', data);

        formParams.value = {};
        // 传入的参数，如在form_js_***打开，则从route.query中获取
        if (Object.keys(currentRoute.value.query).length > 0) {
          formParams.value = { ...currentRoute.value.query };
        }

        // 树形视图中选中的节点
        formParams.value['treeNodeCode'] = treeNodeCode.value;
        formParams.value['isTreeView'] = props.isTreeView;

        // 用于页签“修改日志”，传当前的模块编码
        formParams.value['moduleCodeForLog'] = moduleCodeForLog;

        if (data.length > 0) {
          data.forEach((item) => {
            // 检查时间段的开始时间是否小于结束时间
            let p = item['name'].indexOf('FromDate');
            if (p != -1) {
              let fieldName = item['name'].substring(0, p);
              let bd = o(item['name']).value;
              if (bd != '') {
                // 找到对应的结束时间
                data.forEach((item2) => {
                  let q = item2['name'].indexOf('ToDate');
                  if (q != -1) {
                    let fieldName2 = item2['name'].substring(0, q);
                    if (fieldName2 == fieldName) {
                      let ed = o(item2['name']).value;
                      if (ed != '') {
                        let bDate = new Date(bd).getTime();
                        let eDate = new Date(ed).getTime();
                        if (bDate > eDate) {
                          let title = '';
                          // 找到字段名
                          aryCond.value.forEach((cond) => {
                            if (cond.ctlHtml.indexOf(item2['name']) != -1) {
                              title = cond.title;
                              return;
                            }
                          });
                          createMessage.error(title + '的开始时间不能大于结束时间');
                        }
                      }
                      return;
                    }
                  }
                });
              }
            }

            // 当为select类型时，如果可多选，则可能会有多个值，需以#,#号分隔
            console.log('formParams.value[' + item['name'] + ']', formParams.value[item['name']]);
            // 路由中可能已传过来moduleCode（来自桌面轻模块标题点击），此处不能再加上，否则会导致该moduleCode出问题，如变为：16741686620151565305#,#16741686620151565305
            if (formParams.value[item['name']] && item['name'] != 'moduleCode') {
              formParams.value[item['name']] =
                formParams.value[item['name']] + '#,#' + item['value'];
            } else {
              formParams.value[item['name']] = item['value'];
            }
          });
        }

        console.log('searchData() formParams', formParams.value);
        // 如果到此处，moduleCode没有值，则说明快速刷新，form表单的dom没有了，因此无需再往下执行
        // 如果继续执行，则可能list会报缺少moduleCode错误
        if (!formParams.value['moduleCode']) {
          return;
        }
        if (activeKey.value.type != 'calendar') {
          reload();
        }

        // 清除已选择的记录，否则在查询得到新结果，导出时，仍会导出之前选择的记录
        setSelectedRowKeys([]);

        try {
          // 用于二次开发扩展search事件
          onSearch(formParams);
        } catch (e) {
          console.info('未在form_js文件中找到onSearch方法');
        }
      }

      //重置
      function resetData() {
        $(o(getFormName.value))[0].reset();
        formParams.value = {};
        searchData();

        try {
          onReset();
        } catch (e) {
          console.info('未在form_js文件中找到onReset方法');
        }
      }

      //设置是否为空
      function setInputVal(dom, val) {
        $(dom).val(val);
      }
      // ---------------------------------------------------------------------------顶部查询栏结束-----------------------------------------------------------

      // ---------------------------------------------------------------------------导出导入开始-----------------------------------------------------------
      //获取改变后的头部数据
      const afterChangecolumns = ref([]);
      function columnsChange(data) {
        afterChangecolumns.value = data;
      }
      // 导入
      const isImport = ref(false);
      // 导出Excel
      const isExport = ref(false);
      // 导出word
      const isExportWord = ref(false);
      const isDownloadZip = ref(false);
      const isCopy = ref(false);
      //获取files
      function rawFileFn(files) {
        let formData = new FormData();
        formData.append('att1', files);
        formData.append('moduleCode', listPage.value.moduleCode);
        formData.append('isTreeView', '' + props.isTreeView);
        formData.append('treeNodeCode', props.treeNodeCode);

        // formData.append('moduleCodeRelated', listPage.value.moduleCodeRelated);
        // formData.append('parentId', listPage.value.parentId);
        isImport.value = true;
        getVisualImportExcel(formData)
          .then(async (res) => {
            if (res.data.res === 0) {
              handleSuccess();
            } else {
              if (res.msg.indexOf('\n') >= 0 && res.msg.indexOf('\n') != res.msg.length - 1) {
                // 使支持通过\n换行
                createMessage.error({
                  content: () => h('pre', res.msg),
                });
              } else {
                createMessage.error(res.msg);
              }
            }
          })
          .finally(() => {
            isImport.value = false;
            importVisible.value = false;
          });
      }

      //导出
      async function handleExport() {
        const columns = getColumns();
        const cols = ref<any>([]);
        console.log('handleExport afterChangecolumns', afterChangecolumns.value);
        console.log('handleExport columns', columns);
        if (afterChangecolumns.value.length > 0) {
          afterChangecolumns.value.forEach((item: any) => {
            if (item.visible && item.dataIndex != 'id' && item.dataIndex != 'colOperate')
              cols.value.push(item.dataIndex);
          });
        } else {
          if (columns.length > 0) {
            columns.forEach((item: any) => {
              // 序号列没有dataIndex，{flag: 'INDEX', width: 50, title: '序号'...}
              if (item.dataIndex && item.dataIndex != 'id' && item.dataIndex != 'colOperate') {
                cols.value.push(item.dataIndex);
              }
            });
          }
        }

        let title = listPage.value.moduleName;
        let keys = getSelectRowKeys();
        let ids = keys.join(',');
        let params = {
          moduleCode: listPage.value.moduleCode,
          cols: cols.value.join(','),
          rowIds: ids,
          ...searchInfo.value,
        };

        if (ids == '') {
          createConfirm({
            iconType: 'info',
            title: () => h('span', '提示'),
            content: () => h('span', '您还没有选择记录，确定要导出全部记录么'),
            maskClosable: false,
            onOk: async () => {
              if (!serverInfo.isExportExcelAsync) {
                isExport.value = true;
                getVisualExportExcel(params).then((data) => {
                  isExport.value = false;
                  if (data) {
                    downloadByData(data, `${title}.xls`);
                  }
                });
              } else {
                await getVisualExportExcelAsync(params).then((res) => {
                  let data: any = {};
                  data.moduleName = listPage.value.moduleName;
                  data.uid = res;
                  handlePollDownload(data);
                });
              }
            },
          });
        } else {
          if (!serverInfo.isExportExcelAsync) {
            isExport.value = true;
            getVisualExportExcel(params).then((data) => {
              isExport.value = false;
              if (data) {
                downloadByData(data, `${title}.xls`);
              }
            });
          } else {
            isExport.value = true;
            await getVisualExportExcelAsync(params).then((res) => {
              let data: any = {};
              data.moduleName = listPage.value.moduleName;
              data.uid = res;
              handlePollDownload(data);
              isExport.value = false;
            });
          }
        }
      }
      // ---------------------------------------------------------------------------导出导入结束-----------------------------------------------------------

      // ---------------------------------------------------------------------------导出word开始---------------------------------------------------------
      async function handleExportWord() {
        openSelViewModal(true, { formCode: listPage.value.formCode });
      }

      // ---------------------------------------------------------------------------导出word结束---------------------------------------------------------

      // -----------------------------------------------------------单元格编辑开始----------------------------------------------------
      function handleEditEnd({ record, index, key, value }: Recordable) {
        console.log('handleEditEnd', record, index, key, value);
        return false;
      }

      // 模拟将指定数据保存
      function feakSave({ value, key, id }) {
        // createMessage.loading({
        //   content: `正在保存${key}`,
        //   key: '_save_fake_data',
        //   duration: 0,
        // });
        let params = {
          colName: key,
          id: id,
          update_value: value,
          moduleCode: listPage.value.moduleCode,
          // original_value:'',
          // original_html:''
        };

        return new Promise((resolve) => {
          getVisualModuleEditInPlace(params).then(() => {
            handleSuccess();
            resolve(true);
          });
        });
      }

      async function beforeEditSubmit({ record, index, key, value }) {
        console.log('单元格数据正在准备提交', { record, index, key, value });
        feakSave({ id: record.id, key, value });
      }

      function handleEditCancel() {
        console.log('cancel');
      }
      const filterOption = (input: string, option: any) => {
        return option.name.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      };
      // -----------------------------------------------------------单元格编辑结束----------------------------------------------------

      function handleDbClick(record) {
        console.log('handleDbClick record', record);
        if (listPage.value.canView) {
          handleView(record);
        } else {
          createMessage.warning('没有详情可以查看');
        }
      }

      function handleEditFlow(record: any) {
        openProcessDrawer(true, {
          myActionId: record.id,
          type: record.type,
        });
      }

      // -----------------------------------------------------管理开始-------------------------------------------------------------
      function handleManager() {
        go({
          path: '/managerPage',
          query: {
            urlParams:
              '/visual/module_field_list.jsp?formCode=' +
              listPage.value.formCode +
              '&moduleCode=' +
              listPage.value.moduleCode,
          },
        });
      }
      // -----------------------------------------------------管理结束-------------------------------------------------------------

      // -----------------------------------------------------操作列开始-------------------------------------------------------------
      //打开历史记录页
      function handleHistory(record: any) {
        console.log('打开历史记录页', record);
      }
      //打开修改日志页面
      function handleLogEdit(record: any) {
        console.log('打开修改日志页面', record);
      }
      //打开浏览日志页面
      function handleLogShow(record: any) {
        console.log('打开浏览日志页面', record);
      }

      const [registerRollBackModal, { openModal: openRollBackModal }] = useModal();
      function handleRollBack(record: any) {
        openRollBackModal(true, { ...record, isLog: listPage.value.isLog });
      }

      function handleRollBackCallBack() {
        reload();
      }

      // 脚本
      function handleScript(record: any) {
        console.log('record', record);
        eval('(' + record.script + ')');
      }

      // 工具条发起流程按钮
      function handleFlow(record: any) {
        console.log('handleFlow record', record);
        let keys = getSelectRowKeys();
        if (record.isSelRow) {
          if (keys.length == 0) {
            createMessage.warning('请选择记录');
            return;
          }
        }

        isLaunching.value = true;
        let ids = keys.join(',');
        getFlowInit({ typeCode: record.flowTypeCode, rowIds: ids }).then((res) => {
          let myActionId = res.myActionId || '';
          let flowId = res.flowId || '';
          let type = res.type || 2;
          isLaunching.value = false;
          if (myActionId) {
            if (listPage.value['flowOpStyle'] == 1) {
              let title = flowId + '-' + res.flowTitle;
              if (title.length > 18) {
                title = title.substring(0, 18) + '...';
              }
              if (type == 2) {
                go({
                  path: '/processHandle',
                  query: {
                    myActionId: myActionId,
                    isFromProcess: true,
                    title: title,
                    cacheName: `processHandle${flowId}`,
                  },
                });
              } else {
                go({
                  path: '/processHandleFree',
                  query: {
                    myActionId: myActionId,
                    isFromProcess: true,
                    title: title,
                    cacheName: `processHandle${flowId}`,
                  },
                });
              }
            } else {
              openProcessDrawer(true, {
                myActionId: myActionId,
                type,
              });
            }
          }
        });
      }

      function launchFlow(flowTypeCode) {
        getFlowInit({ typeCode: flowTypeCode }).then((res) => {
          let myActionId = res.myActionId || '';
          let type = res.type || 2;
          if (myActionId) {
            openProcessDrawer(true, {
              myActionId: myActionId,
              type,
            });
          }
        });
      }

      // ------------------------------------模块添加对话框开始----------------------------------------------
      const [registerSmartModuleModal, { openModal: openSmartModal }] = useModal();
      //模块添加modal回调
      const handleSmartModuleCallBack = (formData) => {
        console.log('handleSmartModuleCallBack formData', formData);
        if (formData) {
          try {
            // 不同模块有些form_js内的方法有冲突
            eval('onDialogClose_' + listPage.value.formCode + '(formData, moduleCode.value)');
          } catch (e) {
            console.error('form_js中可能未写onDialogClose方法');
            // console.error(e);
          }
        } else {
          searchData();
        }
      };
      // ------------------------------------模块添加对话框开始---------------------------------------------
      // 工具条模块添加按钮
      function handleAddModule(record: any) {
        let keys = getSelectRowKeys();
        if (record.isSelRow) {
          if (keys.length == 0) {
            createMessage.warning('请选择记录');
            return;
          }
        }

        let selRowMax = record.selRowMax == undefined ? '-1' : record.selRowMax;
        if (selRowMax != '-1' && selRowMax != '') {
          if (selRowMax == 1) {
            if (keys.length > 1) {
              createMessage.warning('请选择一条记录');
              return;
            }
          } else if (keys.length > selRowMax) {
            createMessage.warning('请选择最多不超过' + selRowMax + '条记录');
            return;
          }
        }

        let ids = keys.join(',');
        if (record.mode == 0) {
          openDrawer(true, {
            isUpdate: 1,
            record: {
              moduleCode: record.moduleCode,
              rowIds: ids,
            },
            params: {
              curModuleCode: listPage.value.moduleCode,
            },
            moduleAction: record.moduleAction,
          });
        } else {
          // 需选择记录
          openSmartModal(true, {
            isUpdate: 1,
            record: {
              moduleCode: record.moduleCode,
              rowIds: ids,
              params: {
                curModuleCode: listPage.value.moduleCode,
              },
              moduleAction: record.moduleAction,
            },
          });
        }
      }

      // 打开对话框
      function openDialog(options: Recordable) {
        let keys = getSelectRowKeys();
        if (options.isSelRow) {
          if (keys.length == 0) {
            createMessage.warning('请选择记录');
            return;
          }
        }

        let ids = keys.join(',');
        if (options.mode == 0) {
          // 抽屉
          openDrawer(true, {
            isUpdate: 1,
            isDialog: true,
            record: {
              moduleCode: options.moduleCode, // 对话框中的模块编码
              rowIds: ids,
              op: options.op,
              title: options.title,
              html: options.html,
              rules: options.rules,
              params: {
                curModuleCode: listPage.value.moduleCode,
              },
            },
          });
        } else {
          // 对话框
          openSmartModal(true, {
            isUpdate: 1,
            isDialog: true,
            title: options.title,
            record: {
              moduleCode: options.moduleCode, // 对话框中的模块编码
              rowIds: ids,
              op: options.op,
              html: options.html,
              rules: options.rules,
              params: {
                curModuleCode: listPage.value.moduleCode,
              },
            },
          });
        }
      }

      // 自定义操作列按钮
      function handleColOperate(item, record) {
        if (item.type == 'FLOW_SHOW') {
          // 查看流程详情
          openProcessShowDrawer(true, {
            flowId: item.flowId,
            visitKey: item.visitKey,
          });
        } else if (item.type == 'FLOW') {
          // 发起流程
          let params = {
            op: 'opLinkFlow',
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
        } else if (item.type == 'MODULE') {
          goTo('/smartModulePage', {
            moduleCode: item.moduleCode,
            moduleId: record.id,
          });
        } else if (item.type == 'CLICK') {
          eval(item.script);
        }
      }
      // -----------------------------------------------------操作列结束-------------------------------------------------------------

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

      //此处处理树形视图模块
      // ---------------------------------------------------------------------------树形视图页面开始-----------------------------------------------------------

      //树形选择回调
      function handleSelect(key: string) {}

      // ---------------------------------------------------------------------------树形视图页面结束-----------------------------------------------------------

      /**
       * 跳转到其它模块，用于二次开发
       * goTo('/smartModulePage', {
       *  moduleCode: 'xmxxgl',
       *  xm: xmId,
       * })
       */
      function goTo(path: string, query: any) {
        go({
          path,
          query,
        });
      }

      //查看详情
      function openSmartModuleDrawerForShow(moduleCode, id, visitKey, params) {
        params = params ? params : {};
        console.log('openSmartModuleDrawerForShow params', params);
        openDrawer(true, {
          isUpdate: 3,
          record: {
            moduleCode: moduleCode,
            id: id,
            visitKey: visitKey,
            params: params,
            isTreeView: props.isTreeView,
            treeNodeCode: treeNodeCode.value,
          },
        });
      }

      const getModuleCode = computed(() => moduleCode.value);

      const canAdd = computed(() => {
        return listPage.value.isBtnAddShow;
      });

      const canDel = computed(() => {
        return listPage.value.isBtnDelShow;
      });

      const btnAddName = computed(() => {
        return listPage.value.btnAddName ? listPage.value.btnAddName : '新增';
      });

      const btnDelName = computed(() => {
        return listPage.value.btnDelName ? listPage.value.btnDelName : '删除';
      });

      const cardLen = ref(0);
      function handleLoaded(len) {
        console.log('handleLoaded len', len);
        cardLen.value = len;
      }

      function handleOpenEditDrawer(record) {
        console.log('handleOpenEditDrawer record', record);
        handleEdit(record);
      }

      // ------------------------------------抄送开始----------------------------------------------
      const [registerDistributeModal, { openModal: openDisModal }] = useModal();
      //展示加签modal
      const openDistributeModal = (flowId) => {
        openDisModal(true, {
          isUpdate: true,
          record: { flowId: flowId },
        });
      };
      //加签modal回调
      const handleDistributeCallBack = () => {};
      // ------------------------------------抄送结束---------------------------------------------

      const [registerSelFormViewModal, { openModal: openSelViewModal }] = useModal();
      const handleSelFormViewCallBack = (record) => {
        let keys = getSelectRowKeys();
        let title = listPage.value.moduleName;
        let params = {
          moduleCode: listPage.value.moduleCode,
          ids: keys.join(','),
          isTreeView: props.isTreeView,
          treeNodeCode: treeNodeCode.value,
          formViewId: record.formViewId,
        };
        isExportWord.value = true;
        getVisualExportWord(params).then((data) => {
          isExportWord.value = false;
          if (data) {
            downloadByData(data, `${title}.doc`);
          }
        });
      };

      const handleDownloadZip = (record) => {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('请选择记录');
          return;
        }
        let params = {
          moduleCode: listPage.value.moduleCode,
          ids: keys.join(','),
          isTreeView: props.isTreeView,
          treeNodeCode: treeNodeCode.value,
          formViewId: record.formViewId,
          visitKey: listPage.value.visitKey,
        };
        isDownloadZip.value = true;
        getVisualDownloadZip(params).then((data) => {
          if (data) {
            downloadByData(data, `${listPage.value.moduleName}.zip`);
            isDownloadZip.value = false;
          }
        });
      };

      function getPublicPath() {
        const publicPath = import.meta.env.VITE_PUBLIC_PATH || '/';
        return publicPath;
      }

      // -----------------------------------------------------选择模块开始-------------------------------------------------------------
      const [registerSmartModuleSelDrawer, { openDrawer: openSmartModuleSelDrawer }] = useDrawer();

      let myStart = 3;
      function openSmartModuleSelTableDrawer(selMode = 1, start = 1, params: object) {
        console.log('openSmartModuleSelTableDrawer');
        myStart = start;
        openSmartModuleSelDrawer(true, {
          selMode: selMode, // 1 单选，2 多选
          start: myStart, // 1 表单域选择 2 嵌套表格拉单 3 form_js选择记录
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
        console.log('handleSmartModuleSelCallBack', rows, 'myStart', myStart);
        if (myStart == 3) {
          let ids = '';
          for (let k in rows) {
            if (ids == '') {
              ids = rows[k].ID;
            } else {
              ids += ',' + rows[k].ID;
            }
          }

          try {
            handleModuleListSel(ids);
          } catch (e) {
            console.warn('form_js 中未设置 handleModuleListSel, ids:' + ids);
            console.log(e);
          }
        }
      }
      // -----------------------------------------------------选择模块结束-------------------------------------------------------------

      //导入modal取消
      function handleCancel() {
        importVisible.value = false;
      }

      function handleDownloadExcelTempl() {
        isDownload.value = true;
        getVisualDownloadExcelTempl({ moduleCode: moduleCode.value }).then((data) => {
          isDownload.value = false;
          let title = '模板';
          if (data) {
            downloadByData(data, `${title}.xls`);
          }
        });
      }

      function handleImport() {
        importVisible.value = true;
      }

      function handlePreview(record) {
        window.open(record.previewUrl);
      }

      function handleDownload(record: any) {
        console.log('handleDownload', record);
        if (record.flowId) {
          const params = {
            flowId: record.flowId,
            attachId: record.attachId,
          };
          getDownload(params).then((data) => {
            if (data) {
              downloadByData(data, `${record.attachName}`);
            }
          });
        } else {
          const params = {
            visitKey: record.visitKey,
            attachId: record.attachId,
            docId: record.docId, // 流程中的附件带有docId
          };
          getVisualDownload(params).then((data) => {
            if (data) {
              downloadByData(data, `${record.attachName}`);
            }
          });
        }
      }

      const onClickExportMenu: MenuProps['onClick'] = ({ key }) => {
        console.log(`Click on item ${key}`);
        if (key === 'expByCol') {
          handleExport();
        } else {
          exportModal();
        }
      };

      const [registerModalExport, { openModal: ExportOpenModal }] = useModal();
      const [registerModalExportExcelPgogress, { openModal: ExportExcelProgressOpenModal }] =
        useModal();

      //导出
      function exportModal() {
        console.log('exportModal listPage.value.moduleName', listPage.value.moduleName);
        let keys = getSelectRowKeys();
        let ids = keys.join(',');
        ExportOpenModal(true, {
          formCode: listPage.value.formCode,
          moduleCode: moduleCode.value,
          moduleName: listPage.value.moduleName,
          formParams: { ...formParams.value },
          rowIds: ids,
        });
      }

      const handleSuccessSetColumn = () => {
        getListPage();
      };

      const [registerModalSetColumn, { openModal: OpenModalSetColumn }] = useModal();
      const handleSetupColProps = () => {
        OpenModalSetColumn(true, {
          moduleCode: moduleCode.value,
        });
      };

      const getSumField = (dataIndex) => {
        return Reflect.has(sumData.value, dataIndex);
      };

      const handlePollDownload = (data) => {
        console.log('handlePollDownload', data);
        ExportExcelProgressOpenModal(true, {
          ...data,
        });
      };

      return {
        registerTable,
        registerDrawer,
        handleEdit,
        handleView,
        handleDelete,
        handleSuccess,
        activeKey,
        getActiveKey,
        resetData,
        searchData,
        labelCol: { style: { width: '100px', whiteSpace: 'normal' } },
        wrapperCol: { span: 24 },
        setInputVal,
        rawFileFn,
        isImport,
        isExport,
        isExportWord,
        handleExport,
        listPage,
        handleAdd,
        deleteBatch,
        batchOp,
        handleEditEnd,
        handleEditCancel,
        beforeEditSubmit,
        filterOption,
        isShowSearch,
        getSelectRowKeys,
        setSelectedRowKeys,
        getSelectRows,
        handleManager,
        handleHistory,
        handleLogEdit,
        handleLogShow,
        handleScript,
        handleFlow,
        handleColOperate,
        registerProcessDrawer,
        flowColumns,
        columnsChange,
        aryTab,
        hasTab,
        handleExportWord,
        handleSelect,
        registerProcessShowDrawer,
        handleDbClick,
        handleEditFlow,
        formParams,
        aryCond,
        initWindowFunc,
        getModuleCode,
        cardLen,
        handleLoaded,
        canAdd,
        canDel,
        handleOpenEditDrawer,
        handleLaunchFlowInDrawer,
        nestColumns,
        registerDistributeModal,
        handleDistributeCallBack,
        isShowAllCond,
        expandSearchForm,
        handleResizeColumn: (w, col) => {
          col.width = w;
        },
        isCopy,
        copyRecords,
        getPublicPath,
        registerSelFormViewModal,
        handleSelFormViewCallBack,
        handleAddModule,
        registerSmartModuleModal,
        handleSmartModuleCallBack,
        handleRollBack,
        registerRollBackModal,
        handleRollBackCallBack,
        registerSmartModuleSelDrawer,
        handleSmartModuleSelCallBack,
        handleDownloadZip,
        isDownloadZip,
        importVisible,
        handleCancel,
        handleDownloadExcelTempl,
        isDownload,
        handleImport,
        getFormName,
        handleDownload,
        handlePreview,
        onClickExportMenu,
        registerModalExport,
        registerModalExportExcelPgogress,
        btnAddName,
        btnDelName,
        isLaunching,
        handleSetupColProps,
        registerModalSetColumn,
        handleSuccessSetColumn,
        tableSummaryColumns,
        getSumField,
        sumData,
        handlePollDownload,
      };
    },
  });
</script>
<style>
  /* 选择是否为空bootstrap下拉菜单 */
  .dropdown {
    position: relative;
  }
  .dropdown-toggle:focus {
    outline: 0;
  }
  .dropdown-menu {
    position: absolute;
    top: 100%;
    left: 0;
    z-index: 1000;
    display: none;
    float: left;
    min-width: 160px;
    padding: 5px 0;
    margin: 2px 0 0;
    font-size: 14px;
    text-align: left;
    list-style: none;
    background-color: #fff;
    -webkit-background-clip: padding-box;
    background-clip: padding-box;
    border: 1px solid #ccc;
    border: 1px solid rgba(0, 0, 0, 0.15);
    border-radius: 4px;
    -webkit-box-shadow: 0 6px 12px rgba(0, 0, 0, 0.175);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.175);
  }
  .dropdown-menu.pull-right {
    right: 0;
    left: auto;
  }
  .dropdown-menu .divider {
    height: 1px;
    margin: 9px 0;
    overflow: hidden;
    background-color: #e5e5e5;
  }
  .dropdown-menu > li > a {
    display: block;
    padding: 3px 20px;
    clear: both;
    font-weight: normal;
    line-height: 1.42857143;
    color: #333;
    white-space: nowrap;
  }
  .dropdown-menu > li > a:hover,
  .dropdown-menu > li > a:focus {
    color: #262626;
    text-decoration: none;
    background-color: #f5f5f5;
  }
  .dropdown-menu > .active > a,
  .dropdown-menu > .active > a:hover,
  .dropdown-menu > .active > a:focus {
    color: #fff;
    text-decoration: none;
    background-color: #337ab7;
    outline: 0;
  }
  .dropdown-menu > .disabled > a,
  .dropdown-menu > .disabled > a:hover,
  .dropdown-menu > .disabled > a:focus {
    color: #777;
  }
  .dropdown-menu > .disabled > a:hover,
  .dropdown-menu > .disabled > a:focus {
    text-decoration: none;
    cursor: not-allowed;
    background-color: transparent;
    background-image: none;
    filter: progid:DXImageTransform.Microsoft.gradient(enabled = false);
  }
  .open > .dropdown-menu {
    display: block;
  }
  .open > a {
    outline: 0;
  }
  .dropdown-menu-right {
    right: 0;
    left: auto;
  }
  .dropdown-menu-left {
    right: auto;
    left: 0;
  }
  .dropdown-header {
    display: block;
    padding: 3px 20px;
    font-size: 12px;
    line-height: 1.42857143;
    color: #777;
    white-space: nowrap;
  }
  .dropdown-backdrop {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 990;
  }
  .pull-right > .dropdown-menu {
    right: 0;
    left: auto;
  }
  .dropup .caret,
  .navbar-fixed-bottom .dropdown .caret {
    content: '';
    border-top: 0;
    border-bottom: 4px dashed;
    border-bottom: 4px solid \9;
  }
  .dropup .dropdown-menu,
  .navbar-fixed-bottom .dropdown .dropdown-menu {
    top: auto;
    bottom: 100%;
    margin-bottom: 2px;
  }
  @media (min-width: 768px) {
    .navbar-right .dropdown-menu {
      right: 0;
      left: auto;
    }
    .navbar-right .dropdown-menu-left {
      right: auto;
      left: 0;
    }
  }
</style>
<style scoped>
  :deep(
      .ant-tabs-top > .ant-tabs-nav,
      .ant-tabs-bottom > .ant-tabs-nav,
      .ant-tabs-top > div > .ant-tabs-nav,
      .ant-tabs-bottom > div > .ant-tabs-nav
    ) {
    margin: 0 !important;
  }

  :deep(.ant-form-item-label) {
    overflow: inherit;
  }

  #customForm input:not([type='radio'], [type='button'], [type='checkbox']) {
    height: 29px !important;
    width: 100% !important;
  }

  :deep(#customForm input[type='checkbox']) {
    margin-top: 5px !important;
  }

  #customForm select {
    width: 100% !important;
  }

  :deep(#customForm .cond-text) {
    padding-top: 5px;
    padding-left: 2px;
    padding-right: 2px;
  }

  :deep(#customForm input[type='radio']) {
    vertical-align: middle !important;
    margin-top: 2px !important;
    margin-left: 5px;
    margin-right: 5px;
    height: 18px !important;
  }

  :deep(#customForm .cond-arrow) {
    margin-left: 2px;
  }

  :deep(.ant-form-item-control-input-content) {
    display: flex;
    align-items: center;
  }

  :deep(.ant-input-affix-wrapper) {
    padding: 4px 6px;
  }
  .fa {
    font-size: 16px;
  }

  :deep(.ant-form-item) {
    margin-bottom: 10px;
  }

  /* 使展开条件按钮可旋转 */
  .normal {
    transition: all 0.5s;
  }
  .rotate180 {
    transform: rotate(-180deg);
    transition: all 0.5s;
  }

  i {
    margin-right: 10px;
  }

  :deep(.row-color-gray) {
    background-color: #ddd !important;
  }
  :deep(.row-color-red) {
    background-color: #ff0000 !important;
  }
  :deep(.row-color-green) {
    background-color: #00ff00 !important;
  }
  :deep(.row-color-blue) {
    background-color: #0000ff !important;
  }
</style>
