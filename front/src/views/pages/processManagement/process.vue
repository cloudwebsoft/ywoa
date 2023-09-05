<template>
  <PageWrapper dense contentFullHeight fixedHeight contentClass="flex">
    <TypeCodeTree class="w-1/4 xl:w-1/5 treeList" @select="handleSelect" v-if="isSearch" />
    <div v-show="!isShowEmpty" :class="getType == 'search' ? 'mb-4 ml-1 w-3/4 xl:w-4/5' : 'w-full'">
      <Tabs
        v-model:activeKey="activeKey"
        centered
        v-if="getType == 'attend'"
        @change="getActiveKey"
      >
        <TabPane key="1" tab="我参与的流程" force-render />
        <TabPane key="2" tab="我发起的流程" force-render />
      </Tabs>
      <div class="ml-3 mr-3 mt-3 p-2 bg-white">
        <Form
          id="customForm"
          :label-col="labelCol"
          :wrapper-col="wrapperCol"
          v-show="isShowSearch"
          :Name="getFormName"
        >
          <Row>
            <Col :span="6" v-if="!isSearch">
              <FormItem label="类型">
                <select
                  id="typeCode"
                  name="typeCode"
                  show-search
                  style="width: 200px"
                  placeholder="不限"
                  :value="typeCode"
                  @change="handleChangeTypeCode"
                  ref="typeCodeRef"
                >
                  <option value="">&nbsp;&nbsp;&nbsp;请选择</option>
                  <option v-for="(tp, tpin) in newDirTreeList" :key="tpin" :value="tp['code']">
                    <span v-for="(_, nbin) in tp['layer']" :key="nbin">&nbsp;&nbsp;&nbsp;</span
                    >{{ tp.name }}
                  </option>
                </select>
              </FormItem>
            </Col>
            <Col :span="0" v-else>
              <input type="hidden" id="typeCode" name="typeCode" value="" />
            </Col>
            <Col :span="6">
              <FormItem label="按">
                <select id="by" name="by" show-search style="width: 80px" value="title">
                  <option value="title">标题</option>
                  <option value="flowId">流程号</option>
                </select>
                <input id="title" name="title" value="" style="margin-left: 2px" />
              </FormItem>
            </Col>
            <Col :span="6" v-if="getType != 'mine' || activeKey != '1'">
              <FormItem label="发起人">
                <input id="starter" name="starter" value="" style="margin-left: 2px" />
              </FormItem>
            </Col>
            <Col :span="6">
              <FormItem label="时间">
                <input id="fromDate" name="fromDate" title="发起时间开始" />
                &nbsp;-&nbsp;
                <input id="toDate" name="toDate" title="发起时间结束" />
                <span @click="expandSearchForm" v-if="getType != 'search'">
                  <Tooltip :title="isShowAllCond ? '收缩' : '展开'">
                    <Icon
                      :class="[isShowAllCond ? 'normal' : 'rotate180', 'cursor-pointer', 'ml-1']"
                      icon="clarity:collapse-line"
                    />
                  </Tooltip>
                </span>
              </FormItem>
            </Col>
            <Col :span="6" v-if="(getType != 'doing' && isShowAllCond) || getType == 'search'">
              <FormItem label="状态">
                <select
                  id="status"
                  name="status"
                  show-search
                  style="width: 80px"
                  placeholder="不限"
                  value=""
                  class="ant-input-affix-wrapper"
                >
                  <option value="">请选择</option>
                  <option value="0">未开始</option>
                  <option value="1">处理中</option>
                  <option value="2">已结束</option>
                  <option value="-1">已放弃</option>
                  <option value="-2">已拒绝</option>
                </select>
                <span @click="expandSearchForm" v-if="getType == 'search'">
                  <Tooltip :title="isShowAllCond ? '收缩' : '展开'">
                    <Icon
                      :class="[isShowAllCond ? 'normal' : 'rotate180', 'cursor-pointer', 'ml-1']"
                      icon="clarity:collapse-line"
                    />
                  </Tooltip>
                </span>
              </FormItem>
            </Col>
            <Col
              :span="item['typeOfField'] == FormField.TYPE_DATE && item['condType'] == '0' ? 6 : 6"
              v-for="(item, index) in forms"
              :key="index"
              v-show="isShowAllCond"
              class="cond-span"
            >
              <FormItem :label="item['fieldTitle']">
                <template v-if="item['typeOfField'] == FormField.TYPE_DATE">
                  <input
                    :name="item['fieldName'] + '_cond'"
                    :value="item['condType']"
                    type="hidden"
                  />
                  <template v-if="item['condType'] == '0'">
                    <input
                      :id="item['fieldName'] + 'FromDate'"
                      :name="item['fieldName'] + 'FromDate'"
                      size="15"
                      :value="item['formDate']"
                    />
                    &nbsp;-&nbsp;
                    <input
                      :id="item['fieldName'] + 'ToDate'"
                      :name="item['fieldName'] + 'ToDate'"
                      size="15"
                      :value="item['toDate']"
                    />
                  </template>
                  <template v-if="item['condType'] != '0'">
                    <input
                      :id="item['fieldName']"
                      :name="item['fieldName']"
                      :value="item['queryValue']"
                      class="cond-date"
                    />
                  </template>
                </template>
                <template v-else-if="item['typeOfField'] == FormField.TYPE_MACRO">
                  <input
                    :name="item['fieldName'] + '_cond'"
                    :value="item['condType']"
                    type="hidden"
                  />
                  <!-- eslint-disable-next-line vue/no-v-html -->
                  <div style="width: 100%" v-html="item['ctlForQuery']"> </div>
                </template>
                <template
                  v-else-if="
                    item['typeOfField'] ==
                    ('numberic' ||
                      FormField.FIELD_TYPE_INT ||
                      FormField.FIELD_TYPE_DOUBLE ||
                      FormField.FIELD_TYPE_FLOAT ||
                      FormField.FIELD_TYPE_LONG ||
                      FormField.FIELD_TYPE_PRICE)
                  "
                >
                  <select :name="item['fieldName'] + '_cond'" :value="item.nameCond">
                    <option value="=">=</option>
                    <option value=">">></option>
                    <option value="&lt;">&lt;</option>
                    <option value=">=">>=</option>
                    <option value="&lt;=">&lt;=</option>
                  </select>
                  <input :name="item['fieldName']" style="width: 60px" :value="item.queryValue" />
                </template>
                <template v-else>
                  <template v-if="item['condType'] == FormField.COND_TYPE_NORMAL">
                    <template v-if="item['type'] == FormField.TYPE_SELECT">
                      <input
                        :name="item['fieldName'] + '_cond'"
                        :value="item['condType']"
                        type="hidden"
                      />
                      <!-- eslint-disable-next-line vue/no-v-html -->
                      <select
                        :id="item['fieldName']"
                        :name="item['fieldName']"
                        :value="item['queryValue']"
                        v-html="item['options']"
                      >
                      </select>
                    </template>
                    <template v-if="item['type'] == FormField.TYPE_RADIO">
                      <input
                        :name="item['fieldName'] + '_cond'"
                        :value="item['condType']"
                        type="hidden"
                      />
                      <span v-for="(jsonR, jsoIn) in item['aryRadio']" :key="jsoIn">
                        <input
                          type="radio"
                          :id="item['fieldName']"
                          :name="item['fieldName']"
                          :value="jsonR['val']"
                        />{{ jsonR['text'] }}
                      </span>
                    </template>
                    <template v-if="item['type'] == FormField.TYPE_CHECKBOX">
                      <span v-for="(jsonChk, statChk) in item['aryChk']" :key="statChk">
                        <input
                          :name="jsonChk['fieldName'] + '_cond'"
                          :value="item['condType']"
                          type="hidden"
                        />
                        <input
                          type="checkbox"
                          :id="jsonChk['fieldName']"
                          :name="jsonChk['fieldName']"
                          :value="jsonChk['val']"
                          :style="[{ width: statChk > 1 ? '200px' : '' }]"
                        />
                        <span v-if="statChk > 1">{{ jsonChk['text'] }}</span>
                        <span v-else></span>
                      </span>
                    </template>
                  </template>

                  <template v-if="!item['isSpecial']">
                    <input
                      :name="item['fieldName'] + '_cond'"
                      :value="item['condType']"
                      type="hidden"
                    />
                    <input
                      :id="'field' + index"
                      :name="item['fieldName']"
                      style="width: 60px"
                      :value="item['queryValue']"
                    />
                    <a :id="'arrow' + index" href="javascript:;">
                      <Dropdown>
                        <a class="ant-dropdown-link" @click.prevent>
                          <CaretDownOutlined />
                        </a>
                        <template #overlay>
                          <Menu>
                            <MenuItem @click="setInputVal('#field' + index, IS_EMPTY)">
                              <a href="javascript:;">等于空</a>
                            </MenuItem>
                            <MenuItem @click="setInputVal('#field' + index, IS_NOT_EMPTY)">
                              <a href="javascript:;">不等于空</a>
                            </MenuItem>
                          </Menu>
                        </template>
                      </Dropdown>
                    </a>
                  </template>
                </template>
              </FormItem>
            </Col>
          </Row>
          <Row class="flex justify-center">
            <Button @click="resetData">重置</Button>
            <Button type="primary" class="ml-1" @click="searchData" htmlType="submit">查询</Button>
          </Row>
          <input name="op" value="search" type="hidden" />
          <input id="action" name="action" value="" type="hidden" />
          <input id="myUserName" name="myUserName" value="" type="hidden" />
        </Form>
      </div>
      <BasicTable
        @register="registerTable"
        :rowSelection="Object.keys(rowSelection).length > 0 ? rowSelection : undefined"
        class="m-3"
        :columns="flowColumns"
        @resize-column="handleResizeColumn"
      >
        <template #toolbar>
          <a-button
            type="primary"
            class="mr-1"
            @click="exportModal"
            v-if="isExport"
            :loading="isExporting"
          >
            导出
          </a-button>
          <a-button
            type="primary"
            class="mr-1"
            @click="getFieldList"
            :loading="isCondition"
            v-if="listPage.nodeType != 0 && listPage.isFlowManager"
          >
            条件
          </a-button>
          <a-button
            type="primary"
            v-show="true"
            @click="handleBatch"
            v-if="listPage.canDisposeBatch"
          >
            批量提交
          </a-button>

          <Tooltip title="设置表头">
            <Icon
              icon="ant-design:setting-outlined"
              style="margin-right: 0px !important"
              class="cursor-pointer"
              :size="20"
              v-if="listPage.isFlowManager"
              @click="handleSetupColProps"
            />
          </Tooltip>
          <!-- 
            调整列宽及恢复统一在后端处理
            <Popconfirm
            placement="top"
            title="确定恢复列的默认设置吗？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="resetColProps"
          >
            <a-button type="primary" v-show="true"> 恢复 </a-button>
          </Popconfirm> -->
        </template>
        <template #action="{ record }">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: '处理',
                ifShow: () => getType == 'doing',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'clarity:info-standard-line',
                tooltip: '查看',
                ifShow: () => getType != 'doing',
                onClick: handleView.bind(null, record),
              },
              {
                icon: 'ant-design:heart-outlined',
                tooltip: '关注',
                ifShow: () => getType != 'favorite' && listPage.isBtnAttentionShow,
                popConfirm: {
                  title: '是否确认关注',
                  confirm: handleFavorite.bind(null, record),
                },
              },
              {
                icon: 'ant-design:heart-outlined',
                tooltip: '取关',
                ifShow: () => getType === 'favorite',
                popConfirm: {
                  title: '是否确认取关',
                  confirm: handleUnFavorite.bind(null, record),
                },
              },
            ]"
          />
        </template>
        <template #flow_level="{ text }">
          <span title="普通" v-if="text == 0"><Icon icon="clarity:control-lun-line" /></span>
          <span title="一般" v-if="text == 1" style="color: orange"
            ><Icon icon="clarity:control-lun-outline-badged"
          /></span>
          <span title="紧急" v-if="text == 2" style="color: red"
            ><Icon icon="clarity:control-lun-outline-badged"
          /></span>
        </template>
        <template #title="{ record, text }"
          ><span
            @click="handleClickTitle(record)"
            :style="[
              { cursor: 'pointer' },
              { fontWeight: getType == 'doing' && !record.isReaded ? 'bold' : 'normal' },
            ]"
            >{{ text }}</span
          >
        </template>
      </BasicTable>
    </div>
    <ProcessDrawer @register="registerDrawer" @success="handleSuccess" @show-view="handleView" />
    <ProcessShowDrawer
      @register="registerViewDrawer"
      @handle-current="handleEdit"
    /><!-- @success="handleSuccess" -->
    <SelectField @register="registerModalSelectField" @success="handleSuccessSelectField" />
    <ExportTableModal @register="registerModalExport" @async-download="handlePollDownload" />
    <SetColumnModal @register="registerModalSetColumn" @success="handleSuccessSetColumn" />
    <ExportExcelProgressModal @register="registerModalExportExcelPgogress" />
    <div class="w-3/4 xl:w-4/5 h-full" v-if="isShowEmpty">
      <div class="flex flex-col justify-center bg-white m-4 h-48/50">
        <Empty>
          <template #description>
            <span> {{ emptyDesc }} </span>
          </template>
        </Empty>
      </div></div
    >
  </PageWrapper>
</template>
<script lang="ts">
  import {
    defineComponent,
    onMounted,
    onUnmounted,
    ref,
    unref,
    watch,
    reactive,
    h,
    nextTick,
    onActivated,
    computed,
    inject,
  } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getDelToDustbin } from '/@/api/workOffice/workOffice';
  import {
    getFlowList,
    getFlowListPage,
    getConds,
    getDirTreeOpened,
    getFavorite,
    getUnfavorite,
    getResetColProps,
    getFinishBatch,
    saveColWidth,
    getExportExcel,
    getExportExcelAsync,
    getExportExcelProgress,
  } from '/@/api/process/process';
  import { downloadByData } from '/@/utils/file/download';
  import { filterJS, ajaxGetJS, ajaxPost, removeScript } from '/@/utils/utils';
  import SelectField from './modules/SelectField.vue';
  import SetColumnModal from './modules/SetColumnModal.vue';
  import TypeCodeTree from './modules/TypeCodeTree.vue';

  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import ProcessDrawer from './processDrawer.vue';
  import ProcessShowDrawer from './processShowDrawer.vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';

  import {
    Tabs,
    TabPane,
    Button,
    Form,
    Row,
    Col,
    Dropdown,
    Menu,
    Popconfirm,
    Empty,
    Tooltip,
  } from 'ant-design-vue';

  import { CaretDownOutlined } from '@ant-design/icons-vue';

  import { useModal } from '/@/components/Modal';
  import { Icon } from '/@/components/Icon';
  import { columns } from './process.data';
  import { FormField } from '/@/enums/formField';
  import { PageWrapper } from '/@/components/Page';
  import ExportTableModal from './modules/ExportTableModal.vue';
  import { useMultipleTabWithOutStore } from '/@/store/modules/multipleTab';
  import { useGo } from '/@/hooks/web/usePage';
  import { useUserStore } from '/@/store/modules/user';
  import ExportExcelProgressModal from '../smartModule/modules/ExportExcelProgressModal.vue';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'process',
    components: {
      BasicTable,
      TableAction,
      ProcessDrawer,
      Icon,
      Tabs,
      TabPane,
      ProcessShowDrawer,
      SelectField,
      TypeCodeTree,
      PageWrapper,
      Button,
      Form,
      FormItem: Form.Item,
      Row,
      Col,
      CaretDownOutlined,
      Dropdown,
      Menu,
      MenuItem: Menu.Item,
      ExportTableModal,
      Popconfirm,
      Empty,
      SetColumnModal,
      Tooltip,
      ExportExcelProgressModal,
    },
    setup() {
      const { createMessage, createConfirm } = useMessage();
      const [registerDrawer, { openDrawer }] = useDrawer();
      const [registerViewDrawer, { openDrawer: openViewDrawer }] = useDrawer();
      const activeKey = ref('1');
      const isCheckbox = ref(false);
      const activeType = ref('');
      const isShowEmpty = ref(false);
      const emptyDesc = ref('');
      const { t } = useI18n();
      const typeCode = ref<any>('');
      const typeCodeRef = ref<null | HTMLElement>(null);
      const isShowSearch = ref(false);
      const isShowAllCond = ref(false); // 是否显示全部的条件
      const multipleTabStore = useMultipleTabWithOutStore();
      const { currentRoute } = useRouter();
      const flowList = ['doing', 'mine', 'attend', 'favorite', 'search'];
      const curFormUtil: any = inject('curFormUtil');
      const getFormName = computed(() => 'customForm' + curFormUtil?.getFormNo());
      console.log('getFormName', getFormName.value);
      const srcId = ref('-src');
      const isDoLoading = ref(false); // 是否加载完成
      let isFirst = false; // 是否第一次进入
      const isSearch = ref(false);
      const userStore = useUserStore();
      let serverInfo = userStore.getServerInfo;

      onActivated(() => {
        if (getType.value == 'doing') {
          console.log('onActivated isRefreshProcessPage', multipleTabStore.isRefreshProcessPage);
          if (multipleTabStore.isRefreshProcessPage) {
            reload();
            multipleTabStore.updateIsRefreshProcessPage(false);
          }
        }

        initWindowFunc();

        console.log('onActivated isFirst', isFirst, 'isDoLoading.value', isDoLoading.value);

        if (!isFirst) {
          // 如果条件加载未完成，则重新加载页面，因为如：条件中的部门选择宏控件需要异步加载
          if (isDoLoading.value) {
            console.log('process onActivated initPage');
            initPage();
          }
        }

        isFirst = false;
      });

      function initWindowFunc() {
        setTimeout(() => {
          curFormUtil?.set(getFormName.value);
        }, 100);
        let newWindow = window as any;
        newWindow.ajaxGetJS = ajaxGetJS;
        newWindow.ajaxPost = ajaxPost;
      }

      onUnmounted(() => {
        console.log('Unmounted in process!');
        removeScript(srcId.value);
        curFormUtil.close(getFormName.value);
      });

      // const getType = () => {
      //   if (Object.keys(currentRoute.value.query).length > 0) {
      //     // 来自于门户，我的上报
      //     if (currentRoute.value.query.type == 'mine') {
      //       return 'mine';
      //     }
      //   }
      //   let paths = currentRoute.value.path.split('/');
      //   console.log('getType paths', paths);
      //   // console.log(new Error());
      //   let type = flowList.includes(paths[paths.length - 1]) ? paths[paths.length - 1] : 'doing';
      //   return type;
      // };

      const getType = computed(() => {
        if (Object.keys(currentRoute.value.query).length > 0) {
          // 来自于门户，我的上报
          if (currentRoute.value.query.type == 'mine') {
            return 'mine';
          }
        }
        let paths = currentRoute.value.path.split('/');
        console.log('getType paths', paths);
        // console.log(new Error());
        let type = flowList.includes(paths[paths.length - 1]) ? paths[paths.length - 1] : 'doing';
        return type;
      });

      isSearch.value = getType.value == 'search';

      function getTypeDetailed() {
        let type = getType.value;
        if (type == 'attend') {
          console.log('activeType.value', activeType.value);
          if (activeType.value) {
            return activeType.value;
          } else {
            return type;
          }
        } else {
          return type;
        }
      }

      const [registerTable, { reload, setSelectedRowKeys, getSelectRows, setProps }] = useTable({
        title: '流程列表',
        api: getFlowList,
        rowKey: getType.value == 'doing' ? 'id' : 'f.id',
        // columns,
        beforeFetch: (info) => {
          let newInfo = info;
          let type = unref(activeType) || getType.value;
          newInfo.type = type;
          // 当刚进入页面时，onmounted中，$('#typeCode').val()的值为空，所以用typeCode.value替换
          // 而当点击查询按钮时，$('#typeCode').val()能取到值
          console.log("$('#typeCode').val()", $('#typeCode').val());
          // if (!!$('#typeCode').val()) {
          //   newInfo.typeCode = $('#typeCode').val();
          // }
          if (typeCode.value) {
            newInfo.typeCode = typeCode.value;
          }
          console.log('beforeFetch newInfo', newInfo);
          newInfo = Object.assign({}, newInfo, formParams.value);
          return newInfo;
        },
        searchInfo: { op: 'search' }, //额外的参数
        // useSearchForm: false,
        bordered: true,
        formConfig: {
          autoSubmitOnEnter: true,
        },
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        immediate: false,
        showTableSetting: true,
        tableSetting: {
          setting: false,
        },
        // actionColumn: {
        //   width: 100,
        //   title: '操作',
        //   dataIndex: 'action',
        //   slots: { customRender: 'action' },
        //   fixed: 'right',
        // },
      });
      const rowSelection = ref({});
      watch(
        () => isCheckbox.value,
        (newVal) => {
          if (newVal) rowSelection.value = { type: 'checkbox' };
        },
        { deep: true, immediate: true },
      );

      onMounted(async () => {
        isDoLoading.value = true;
        isFirst = true;

        // 当从门户中进入待办流程时会传入参数typeCode
        console.log('onMounted currentRoute.value.query', currentRoute.value.query);
        if (Object.keys(currentRoute.value.query).length > 0) {
          if (currentRoute.value.query.typeCode) {
            console.log('currentRoute.value.query.typeCode', currentRoute.value.query.typeCode);
            typeCode.value = currentRoute.value.query.typeCode;
            if (typeCodeRef.value) {
              typeCodeRef.value.setAttribute('value', currentRoute.value.query.typeCode);
              console.log('typeCode输入框的值', typeCodeRef.value.getAttribute('value'));
            }
          }
        }

        initWindowFunc();

        await initPage();
      });

      async function initPage() {
        await getListPage();

        await getCondsList();

        getDirTreeList();

        $(fo('fromDate')).datetimepicker({
          lang: 'ch',
          timepicker: false,
          format: 'Y-m-d',
        });
        $(fo('toDate')).datetimepicker({
          lang: 'ch',
          timepicker: false,
          format: 'Y-m-d',
        });

        // await nextTick();
        // setSearchFromClass();
        isShowSearch.value = true;
      }

      async function handleChangeTypeCode(e: ChangeEvent) {
        typeCode.value = $('#typeCode').val();
        await getListPage();
        getCondsList();
      }

      // 用reactive，会导致v-if="listPage.nodeType != 0"始终为true
      // let listPage = reactive({ nodeType: 2 });
      const listPage = ref<any>({});
      let isExport = ref(false);
      let isExporting = ref(false);
      let IS_EMPTY = ref('=空');
      let IS_NOT_EMPTY = ref('<>空');
      let flowColumns = ref<any>([]);

      const getListPage = async (params = {}) => {
        await getFlowListPage({ type: getType.value, typeCode: typeCode.value }).then((res) => {
          console.log('res', res);
          if (res.code == 500) {
            createMessage.warn(res.msg);
            isShowEmpty.value = true;
            emptyDesc.value = res.msg;
            return;
          }

          isShowEmpty.value = false;
          listPage.value = res.data;
          console.log('listPage', listPage.value);
          console.log('getFlowListPage res.nodeType', res.data.nodeType);
          console.log('getFlowListPage listPage.value["nodeType"]', listPage.value['nodeType']);

          isExport.value = listPage.value['isExport'];
          IS_EMPTY.value = listPage.value['IS_EMPTY'];
          IS_NOT_EMPTY.value = listPage.value['IS_NOT_EMPTY'];
          $('#myUserName').empty();
          $('#action').empty();

          $('#myUserName').val(listPage['myUserName']);
          $('#action').val(listPage['action']);
          // 因为可以勾选导出，故此处注释掉
          // if (listPage.value['isCheckbox'])
          isCheckbox.value = true;
          flowColumns.value = [];
          if (listPage.value['colProps'] && listPage.value['colProps'].length > 0) {
            listPage.value['colProps'].forEach((item) => {
              flowColumns.value.push({
                title: item.title ? item.title : item.display,
                dataIndex: item.field ? item.field : item.name,
                align: item.align || 'center',
                // fixed: item.fixed,
                width: item.width,
                sorter: true,
                ellipsis: true,
                resizable: true,
                slots: {
                  customRender:
                    item.field == 'f.flow_level'
                      ? 'flow_level'
                      : item.field == 'f.title'
                      ? 'title'
                      : item.field,
                },
              });
            });
            flowColumns.value.push({
              width: 100,
              title: '操作',
              dataIndex: 'action',
              slots: { customRender: 'action' },
              fixed: 'right',
              align: 'center',
            });
          }

          searchData(params);
        });
      };

      //切换tab
      function getActiveKey(key: string) {
        activeType.value = key == '1' ? 'attend' : 'mine';
        reload();
      }

      function handleClickTitle(record: any) {
        if (getType.value == 'doing') {
          handleEdit(record);
        } else {
          handleView(record);
        }
      }

      const go = useGo();
      function handleEdit(record: any) {
        let title = record['f.id'] + '-' + record['f.title'];
        if (title.length > 18) {
          title = title.substring(0, 18) + '...';
        }
        console.log('handleEdit record', record);
        if (listPage.value['flowOpStyle'] == 1) {
          if (record.type == 2) {
            go({
              path: '/processHandle',
              query: {
                myActionId: record.id,
                isFromProcess: true,
                title: title,
                cacheName: `processHandle${record['f.id']}`,
              },
            });
          } else {
            go({
              path: '/processHandleFree',
              query: {
                myActionId: record.id,
                isFromProcess: true,
                title: title,
                cacheName: `processHandle${record['f.id']}`,
              },
            });
          }
        } else {
          openDrawer(true, {
            myActionId: record.id,
            type: record.type,
          });
        }
      }

      async function handleDelete(record: Recordable) {
        await getDelToDustbin({ ids: record.id }).then(() => {
          handleSuccess();
        });
      }

      //查看详情
      function handleView(record: object) {
        if (listPage.value['flowOpStyle'] == 1) {
          let title = record['f.id'] + '-' + record['f.title'];
          if (title.length > 18) {
            title = title.substring(0, 18) + '...';
          }
          go({
            path: '/processShow',
            query: {
              flowId: record['f.id'],
              title: title,
              isFromProcess: true,
              cacheName: `processShow${record['f.id']}`,
            },
          });
        } else {
          openViewDrawer(true, {
            flowId: record['f.id'],
          });
        }
      }

      // ---------------------------------------------------------------------------扩容条件开始-----------------------------------------------------------
      const [registerModalSelectField, { openModal }] = useModal();
      const isCondition = ref(false);
      //获取条件
      function getFieldList() {
        let typeCode = $('#typeCode').val();
        if (!typeCode) {
          createMessage.warning('请选择类型');
          return;
        }
        openModal(true, {
          typeCode,
          forms,
        });
      }

      function getColumnList() {
        let typeCode = $('#typeCode').val();
        if (!typeCode) {
          createMessage.warning('请选择类型');
          return;
        }

        if (!$('#typeCode').val()) {
          createMessage.warning('请选择流程类型');
          return;
        }
        ExportOpenModal(true, {
          typeCode: $('#typeCode').val(),
          op: 'getColumnList',
        });
      }

      function getDisplayMode() {
        let displayMode = 0;
        switch (getTypeDetailed()) {
          case 'doing':
            displayMode = 1;
            break;
          case 'attend':
            displayMode = 2;
            break;
          case 'mine':
            displayMode = 3;
            break;
          case 'favorite':
            displayMode = 4;
            break;
        }
        return displayMode;
      }

      function resetColProps() {
        let displayMode = getDisplayMode();
        getResetColProps({ typeCode: $('#typeCode').val(), displayMode: displayMode }).then(
          (res) => {
            reload();
          },
        );
      }

      function handleSuccessSelectField() {
        getCondsList();
      }
      const aryCondList = ref([]);
      async function getCondsList() {
        aryCondList.value = [];
        if (!typeCode.value) {
          console.log('getCondsList typeCode is empty.');
          nextTick().then(() => {
            setSearchFromClass();
          });
          isDoLoading.value = false;
          return;
        }
        const res = await getConds({ typeCode: typeCode.value });
        console.log('getCondsList res', res);
        aryCondList.value = res.aryCond || [];
        setSearchForm(unref(aryCondList));

        nextTick(() => {
          setSearchFromClass();
        });
      }
      const forms = ref<any>([]);
      async function setSearchForm(data) {
        forms.value = [];
        if (data.length == 0) return;
        data.forEach((item) => {
          forms.value.push(item);
        });

        await nextTick();

        data.forEach((item) => {
          filterJS(item.ctlForQuery, srcId.value, o(getFormName.value)).then(() => {
            if (o(getFormName.value)) {
              console.log('filterJS end');
              isDoLoading.value = false;
            }
          });
        });

        setTimeout(() => {
          $("[name$='FromDate']").datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
          });
          $("[name$='ToDate']").datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
          });
          $('.cond-date').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
          });
        }, 200);
      }
      // ---------------------------------------------------------------------------扩容条件结束-----------------------------------------------------------

      // ---------------------------------------------------------------------------流程查询页面开始-----------------------------------------------------------

      //流程查询选择树形目录节点
      function handleSelect(key: string) {
        $(fo('typeCode')).empty();
        $(fo('typeCode')).val(key);
        console.log('handleSelect key', key);
        if (key) {
          isDoLoading.value = true;

          typeCode.value = key;
          getListPage({ sortInfo: { field: '', order: '' } });
          getCondsList();
        }
        // reload();
      }

      // ---------------------------------------------------------------------------流程查询页面结束-----------------------------------------------------------

      //页面回调
      function handleSuccess() {
        searchData();
      }

      // ---------------------------------顶部查询栏开始-----------------------
      const dirTreeList = ref([]);
      const newDirTreeList = ref<any>([]);
      async function getDirTreeList() {
        dirTreeList.value = await getDirTreeOpened();
        newDirTreeList.value = [];
        if (dirTreeList.value && dirTreeList.value.length > 0) {
          dirTreeList.value = getChildren(dirTreeList.value);
        }
      }

      function getChildren(data) {
        data.forEach((item) => {
          newDirTreeList.value.push(item);
          if (item.children && item.children.length > 0) {
            getChildren(item.children);
          }
        });
        return data;
      }
      const formParams = ref({});
      //查询
      function searchData(params = {}) {
        let data = $(o(getFormName.value)).serializeArray();
        formParams.value = {};
        if (data.length > 0) {
          data.forEach((item) => {
            formParams.value[item['name']] = item['value'];
          });
        }
        reload(params);

        // 清空所选的记录，以免在导出时仍然导出上次所选的记录
        setSelectedRowKeys([]);
      }

      //重置
      function resetData() {
        $(o(getFormName.value))[0].reset();
        formParams.value = {};
        reload();
      }
      //设置是否为空
      function setInputVal(dom, val) {
        $(dom).val(val);
      }
      // ---------------------------------------------------------------------------顶部查询栏结束-----------------------------------------------------------

      const [registerModalSetColumn, { openModal: OpenModalSetColumn }] = useModal();
      const handleSetupColProps = () => {
        if (!$('#typeCode').val()) {
          createMessage.warning('请选择流程类型');
          return;
        }
        OpenModalSetColumn(true, {
          typeCode: $('#typeCode').val(),
          displayMode: getDisplayMode(),
        });
      };

      const handleSuccessSetColumn = () => {
        getListPage();
      };

      const [registerModalExport, { openModal: ExportOpenModal }] = useModal();
      //导出
      async function exportModal() {
        let checkes = getSelectRows();
        let ids;
        if (getType.value == 'doing') {
          ids = checkes.map((item) => item['id']).join(',');
        } else {
          ids = checkes.map((item) => item['f.id']).join(',');
        }
        if (getType.value == 'search') {
          if (!$('#typeCode').val()) {
            createMessage.warning('请选择流程类型');
            return;
          }

          ExportOpenModal(true, {
            typeCode: $('#typeCode').val(),
            formParams: formParams.value,
            displayMode: getDisplayMode(),
            ids,
          });
        } else {
          try {
            isExporting.value = true;
            console.log('formParams', formParams.value);
            let params = {
              typeCode: typeCode.value,
              displayMode: getDisplayMode(),
              ...formParams.value,
              ids: ids,
            };

            let fileName;
            switch (getTypeDetailed()) {
              case 'doing':
                fileName = '待办流程导出.xls';
                break;
              case 'attend':
                fileName = '我参与的流程导出.xls';
                break;
              case 'mine':
                fileName = '我发起的流程导出.xls';
                break;
              case 'favorite':
                fileName = '我的收藏导出.xls';
                break;
            }

            if (!serverInfo.isExportExcelAsync) {
              let res = await getExportExcel(params);
              await downloadByData(res, fileName);
            } else {
              await getExportExcelAsync(params).then((res) => {
                let data: any = {};
                data.moduleName = fileName;
                data.uid = res;
                handlePollDownload(data);
              });
            }
          } finally {
            isExporting.value = false;
          }
        }
      }

      const [registerModalExportExcelPgogress, { openModal: ExportExcelProgressOpenModal }] =
        useModal();

      const handlePollDownload = (data) => {
        console.log('handlePollDownload', data);
        ExportExcelProgressOpenModal(true, {
          ...data,
        });
      };

      // 关注
      function handleFavorite(record: any) {
        getFavorite({ flowId: record['f.id'] }).then(async (res) => {
          if (res.data.res === 0) {
            createMessage.success(res.data.msg);
          } else {
            createMessage.error(res.data.msg);
          }
        });
      }

      // 取关
      function handleUnFavorite(record: any) {
        getUnfavorite({ flowId: record['f.id'] }).then(async (res) => {
          if (res.data.res === 0) {
            reload();
            createMessage.success(res.data.msg);
          } else {
            createMessage.error(res.data.msg);
          }
        });
      }

      function setSearchFromClass() {
        // 设置搜索条件框中的tabIndex及使focus时高亮
        let searchForm = $('#customForm')[0];
        if (!searchForm) {
          console.warn('setSearchFromClass search form is not exist.');
          return;
        }
        let elements = searchForm.elements;
        let k = 1;
        for (let i = 0; i < elements.length; i++) {
          let element = elements[i];
          // 判断是否为隐藏元素
          let $obj = $(element);
          if (!$obj.attr('hidden') && (element.tagName == 'INPUT' || element.tagName == 'SELECT')) {
            $obj.attr('autocomplete', 'off');
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

      async function handleBatch() {
        let checkes = getSelectRows();
        if (checkes.length == 0) {
          createMessage.warn('请选择待办记录');
          return;
        }

        createConfirm({
          iconType: 'warning',
          title: () => h('span', '您确定要批量处理么？'),
          onOk: async () => {
            let ids = checkes.map((item) => item['id']).join(',');
            let res = await getFinishBatch({ ids: ids });
            if (res.code === 200) {
              createMessage.success(t('common.opSuccess'));
              reload();
            } else {
              createMessage.warn({
                content: () => h('pre', res.msg),
              });
            }
          },
        });
      }

      function expandSearchForm() {
        isShowAllCond.value = !isShowAllCond.value;
        console.log('isShowAllCond', isShowAllCond.value);
      }

      return {
        registerTable,
        registerDrawer,
        handleEdit,
        handleDelete,
        handleSuccess,
        rowSelection,
        activeKey,
        getType,
        getActiveKey,
        handleView,
        registerViewDrawer,
        getFieldList,
        isCondition,
        registerModalSelectField,
        handleSuccessSelectField,
        handleSelect,
        forms,
        FormField,
        dirTreeList,
        resetData,
        searchData,
        newDirTreeList,
        getCondsList,
        labelCol: { style: { width: '90px', whiteSpace: 'normal', height: '40px' } },
        wrapperCol: { span: 24 },
        setInputVal,
        registerModalExport,
        exportModal,
        isExport,
        IS_EMPTY,
        IS_NOT_EMPTY,
        flowColumns,
        handleFavorite,
        handleUnFavorite,
        listPage,
        resetColProps,
        getListPage,
        handleClickTitle,
        isShowEmpty,
        emptyDesc,
        handleChangeTypeCode,
        handleBatch,
        handleResizeColumn: (w, col) => {
          col.width = w;
          saveColWidth({
            typeCode: typeCode.value,
            field: col.dataIndex,
            width: w,
            displayMode: getDisplayMode(),
          });
          console.log('saveColWidth');
        },
        typeCode,
        typeCodeRef,
        handleSetupColProps,
        getColumnList,
        registerModalSetColumn,
        handleSuccessSetColumn,
        isShowSearch,
        expandSearchForm,
        isShowAllCond,
        isExporting,
        getFormName,
        isSearch,
        registerModalExportExcelPgogress,
        handlePollDownload,
      };
    },
  });
</script>
<style scoped>
  #customForm input:not([type='radio'], [type='button'], [type='checkbox']) {
    height: 29px !important;
    width: 100% !important;
  }

  #customForm select {
    width: 100% !important;
  }

  :deep(
      .ant-tabs-top > .ant-tabs-nav,
      .ant-tabs-bottom > .ant-tabs-nav,
      .ant-tabs-top > div > .ant-tabs-nav,
      .ant-tabs-bottom > div > .ant-tabs-nav
    ) {
    margin: 0 !important;
  }

  :deep(.ant-input-affix-wrapper) {
    padding: 4px 6px;
  }
  .ant-col {
    height: 40px;
  }

  input:not([type='radio'], [type='button'], [type='checkbox']) {
    height: 29px !important;
    width: 100% !important;
  }
  select {
    width: 100% !important;
  }
  :deep(.ant-form-item-control-input-content) {
    display: flex;
    align-items: center;
  }
  .vben-basic-table-action {
    justify-content: center;
  }

  /* 使展开条件按钮可旋转 */
  .normal {
    transition: all 0.5s;
  }
  .rotate180 {
    transform: rotate(-180deg);
    transition: all 0.5s;
  }
</style>
