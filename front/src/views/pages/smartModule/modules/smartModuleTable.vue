<template>
  <!-- 用于流程中的表单域选择或拉单 -->
  <PageWrapper dense contentFullHeight contentClass="flex">
    <div class="w-full">
      <Tabs v-model:activeKey="activeKey" centered @change="getActiveKey" v-if="listPage.hasTab">
        <TabPane
          :tab="item.name"
          force-render
          v-for="item in listPage.aryTab"
          :key="item.params.moduleCode"
        />
      </Tabs>
      <div class="ml-3 mr-3 mt-3 p-2 bg-white">
        <Form
          :id="getFormName"
          :Name="getFormName"
          class="custom-form"
          :label-col="labelCol"
          :wrapper-col="wrapperCol"
        >
          <Row id="ctlHtml">
            <Col
              v-for="(item, index) in listPage.aryCond"
              :key="index"
              :span="item.width ? item.width : 6"
            >
              <FormItem :label="item['title']" v-show="index <= 3 || (isShowAllCond && index > 3)">
                <!-- eslint-disable-next-line vue/no-v-html -->
                <div v-html="item['ctlHtml']" class="w-full flex"> </div>
                <span v-show="index == 3 && listPage.aryCond.length > 3" @click="expandSearchForm">
                  <Tooltip :title="isShowAllCond ? '收缩' : '展开'">
                    <Icon
                      :class="[isShowAllCond ? 'normal' : 'rotate180', 'cursor-pointer', 'ml-1']"
                      icon="clarity:collapse-line"
                    />
                  </Tooltip>
                </span>
              </FormItem>
            </Col>
          </Row>
          <Row class="flex justify-center" v-if="isShowSearch">
            <Button @click="resetData">重置</Button>
            <Button type="primary" class="ml-1" @click="searchData">查询</Button>
          </Row>
          <input name="op" value="search" type="hidden" />
          <input id="moduleCode" name="moduleCode" value="" type="hidden" />
        </Form>
      </div>
      <BasicTable
        @register="registerTable"
        class="m-3"
        @edit-end="handleEditEnd"
        @edit-cancel="handleEditCancel"
        :columns="flowColumns"
        :beforeEditSubmit="beforeEditSubmit"
        @columns-change="columnsChange"
      >
        <template #toolbar>
          <a-button type="primary" class="mr-1" @click="handleAdd" v-if="listPage.isBtnAddShow">
            新增
          </a-button>
          <Popconfirm
            placement="top"
            title="确定删除吗？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="deleteAll"
          >
            <a-button type="primary" class="mr-1" v-if="listPage.isBtnDelShow"> 删除 </a-button>
          </Popconfirm>
          <ImpExcel :isFiles="false" @raw-file="rawFileFn" dateFormat="YYYY-MM-DD">
            <a-button
              type="primary"
              class="mr-1"
              v-if="listPage.isBtnImportShow"
              :loading="isImport"
            >
              导入
            </a-button>
          </ImpExcel>
          <a-button
            type="primary"
            class="mr-1"
            @click="handleExport"
            v-if="listPage.isBtnExportShow"
            :loading="isExport"
          >
            导出
          </a-button>
          <template v-for="item in listPage.aryBtnEvent" :key="item.name">
            <Popconfirm
              placement="top"
              :title="`确定${item.name}吗？`"
              ok-text="确定"
              cancel-text="取消"
              @confirm="batchOp(item)"
              v-if="item.type == 'batchBtn'"
            >
              <a-button type="primary" class="mr-1" v-if="item.type == 'batchBtn'">
                {{ item.name }}
              </a-button>
            </Popconfirm>
            <!-- 工具栏自定义按钮 -->
            <a-button
              type="primary"
              class="mr-1"
              v-if="item.type == 'script'"
              @click="handleScript(item)"
            >
              {{ item.name || '事件' }}
            </a-button>
            <a-button
              type="primary"
              class="mr-1"
              v-if="item.type == 'link'"
              @click="handleLink(item)"
            >
              {{ item.name }}
            </a-button>
          </template>
          <a-button
            type="primary"
            class="mr-1"
            :loading="isExportWord"
            @click="handleExportWord"
            v-if="listPage.isBtnExportWordShow"
          >
            生成
          </a-button>
          <a-button type="primary" class="mr-1" v-if="listPage.isAdmin" @click="handleManager">
            管理
          </a-button>
        </template>
        <template #action="{ record }">
          <div
            class="cursor-pointer"
            :class="{
              'flex justify-center': record['colOperate'] && record['colOperate'].length > 1,
            }"
          >
            <template v-for="item in record['colOperate']" :key="item.id">
              <span v-if="item.type == 'DEL'" :title="item.name">
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
                    :title="item.name"
                    style="color: #f00"
                  />
                </Popconfirm>
              </span>
              <span
                v-else
                :title="item.name"
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
                    : handleColOperate(item, record)
                "
              >
                <!-- <Icon
                  icon="ant-design:edit-outlined"
                  v-if="item.type == 'EDIT'"
                  :title="item.name"
                  style="color: #0960bd"
                /> -->
                <Icon
                  icon="clarity:info-standard-line"
                  v-if="item.type == 'SHOW'"
                  :title="item.name"
                  style="color: #0960bd"
                />
                <i
                  :class="item.icon ? 'fa ' + item.icon : ''"
                  :style="[{ color: item.color ? item.color : '' }]"
                  :title="item.name"
                ></i>
              </span>
            </template>
          </div>
        </template>
      </BasicTable>
    </div>
    <SmartModuleShowDrawer @register="registerDrawer" />
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
    computed,
    inject,
  } from 'vue';

  import { BasicTable, useTable } from '/@/components/Table';
  import {
    getVisualList,
    getVisualListPage,
    getVisualDel,
    getVisualBatchOp,
    getVisualModuleEditInPlace,
    getVisualListSelPage, //表单域选择模块配置
    getVisualListNestSelPage, //嵌套表格模块选择配置
    getVisualExportExcel,
    getVisualImportExcel,
    getVisualExportWord,
  } from '/@/api/module/module';
  import { getFlowInit } from '/@/api/process/process';

  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { Tabs, TabPane, Button, Form, Row, Col, Popconfirm, Tooltip } from 'ant-design-vue';
  import Icon from '/@/components/Icon/index';

  import { PageWrapper } from '/@/components/Page';

  import SmartModuleShowDrawer from './smartModuleShowDrawer.vue';

  import { useGo } from '/@/hooks/web/usePage';

  import { ImpExcel } from '/@/components/Excel';

  import { downloadByData } from '/@/utils/file/download';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModuleTable',
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
      SmartModuleShowDrawer,
      Popconfirm,
      Icon,
      ImpExcel,
      Tooltip,
    },
    props: {
      start: {
        type: [String, Number],
        default: 0, //默认为0是请求, 1表单域选择模块
      },
      smartModuleSel: {
        type: Object,
        default: () => {
          return {};
        },
      },
      selMode: {
        type: Number,
        default: 2, //1是单选，2多选
      },
    },
    setup(props) {
      const { createMessage } = useMessage();
      const [registerDrawer, { openDrawer }] = useDrawer();

      const flowColumns = ref<any>([]);
      const activeKey = ref('');
      const moduleCode = ref('');
      const isShowSearch = ref(false);
      const searchInfo = ref<any>({});
      const isShowAllCond = ref(false); // 是否显示全部的条件
      const curFormUtil: any = inject('curFormUtil');

      const getFormName = computed(() => 'customFormRelate' + curFormUtil?.getFormNo());

      const [
        registerTable,
        {
          reload,
          setSelectedRowKeys,
          setProps,
          getSelectRowKeys,
          getSelectRows,
          getColumns,
          redoHeight,
        },
      ] = useTable({
        title: '',
        api: getVisualList,
        rowKey: 'id',
        // 不在此处传表头参数，而是写在模板中，以免表头设置改变后，列不出来，在注册table时不能设置columns:[],否则表头为空，无效
        // columns: [],
        // maxHeight: 180,
        rowSelection: {
          type: 'checkbox',
        },
        beforeFetch: (info) => {
          let newInfo = info;
          newInfo = Object.assign({}, newInfo, formParams.value);
          if (selParams.value && Object.keys(selParams.value).length > 0) {
            newInfo = Object.assign({}, newInfo, selParams.value);
          }
          console.log('newInfo', newInfo);
          searchInfo.value = { ...newInfo };
          return newInfo;
        },
        showTableSetting: true,
        tableSetting: {
          setting: false,
        },
        bordered: true,
        showIndexColumn: true,
        indexColumnProps: { width: 50 },
        immediate: false,
      });

      const { currentRoute } = useRouter();

      const getSelMode = ref(2);
      onMounted(async () => {
        watch(
          () => props.selMode,
          () => {
            getSelMode.value = props.selMode;
            // console.log('getSelMode', getSelMode.value);
            setProps({
              rowSelection: { type: getSelMode.value == 1 ? 'radio' : 'checkbox', columnWidth: 40 },
            });
          },
          {
            immediate: true,
          },
        );

        watch(
          () => props.smartModuleSel,
          () => {
            if (props.start == 3) {
              if (props.smartModuleSel && Object.keys(props.smartModuleSel).length > 0) {
                console.log('props.smartModuleSel start=3', props.smartModuleSel);
                console.log('props.smartModuleSel.moduleCode', props.smartModuleSel.moduleCode);
                moduleCode.value = props.smartModuleSel.moduleCode;
                getListPage();
              }
            } else if (props.start == 1 || props.start == 2) {
              if (props.smartModuleSel && Object.keys(props.smartModuleSel).length > 0) {
                console.log('props start', props.start, 'smartModuleSel', props.smartModuleSel);
                getListPage();
              }
            } else if (props.start == 0) {
              let { formCode } = currentRoute.value.meta;
              moduleCode.value = formCode as string;
              getListPage();
            }
          },
          {
            immediate: false,
          },
        );
      });

      onUnmounted(() => {
        console.log('Unmounted in smartModuleTable');
        curFormUtil.close(getFormName.value);
      });

      let listPage = ref<any>({});
      const selParams = ref<any>({});
      const getListPage = async () => {
        setTimeout(() => {
          curFormUtil?.set(getFormName.value);
        }, 100);

        console.log('moduleCode.value', moduleCode.value);
        //智能模块初始化配置
        if (props.start == 0 || props.start == 3) {
          await getVisualListPage({ moduleCode: moduleCode.value }).then((res) => {
            setResult(res);
          });
        }
        //表单域选择模块初始化配置
        else if (props.start == 1) {
          await getVisualListSelPage({ ...props.smartModuleSel.listPageParams }).then((res) => {
            console.log('getVisualListSelPage res', res);
            setResult(res);
          });
        } else if (props.start == 2) {
          // 嵌套表格选择
          console.log('props.smartModuleSel.listPageParams', props.smartModuleSel.listPageParams);
          await getVisualListNestSelPage({ ...props.smartModuleSel.listPageParams }).then((res) => {
            console.log('getVisualListNestSelPage res', res);
            setResult(res);
          });
        }
      };

      function setResult(res: any) {
        listPage.value = res;
        if (listPage.value.aryTab && listPage.value.aryTab.length > 0) {
          activeKey.value = activeKey.value
            ? activeKey.value
            : listPage.value.aryTab[0].params.moduleCode;
        }
        setProps({ title: listPage.value.moduleName });

        isShowAllCond.value = listPage.value.isAutoExpand;

        listPage.value.aryCond =
          listPage.value.aryCond && listPage.value.aryCond.length > 0 ? listPage.value.aryCond : [];
        isShowSearch.value = listPage.value.aryCond.length > 0 ? true : false;

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
            } else if (item.type != 'checkbox') {
              if (type === 'select') {
                flowColumns.value.push({
                  title: item.title,
                  dataIndex: item.field,
                  align: item.align || 'center',
                  width: item.width + 100,
                  ellipsis: true,
                  editComponent: 'Select',
                  edit: listPage.value.isEditInplace && fieldName === item.field,
                  editComponentProps: {
                    showSearch: true,
                    filterOption: filterOption,
                    fieldNames: { label: 'name' },
                    options: options,
                  },
                });
              } else {
                flowColumns.value.push({
                  title: item.title,
                  dataIndex: item.field,
                  align: item.align || 'center',
                  width: item.width + 100,
                  ellipsis: true,
                  editComponent: type === 'DATE' ? 'DatePicker' : 'Input',
                  edit: listPage.value.isEditInplace && fieldName === item.field,
                  editComponentProps: {},
                });
              }
            }
          });
          flowColumns.value.push({
            title: 'id',
            dataIndex: 'id',
            width: 0,
            // ifShow: false,
          });
        }

        if (listPage.value.aryCond && listPage.value.aryCond.length > 0) {
          setTimeout(() => {
            setSearchFromClass();

            listPage.value.aryCond.forEach((item) => {
              if (item.script) {
                eval(item.script);
              }
            });
          }, 100);
        }
        moduleCode.value = listPage.value.moduleCode;
        // 表单域选择
        if (props.start == 1) {
          selParams.value.pageType = listPage.value.pageType;
          selParams.value.op = listPage.value.op;
          selParams.value.moduleCode = listPage.value.moduleCode;
          selParams.value.byFieldName = listPage.value.byFieldName;
          selParams.value.showFieldName = listPage.value.showFieldName;
          selParams.value.openerFormCode = listPage.value.openerFormCode;
          selParams.value.openerFieldName = listPage.value.openerFieldName;
          selParams.value = {
            ...selParams.value,
            ...props.smartModuleSel.condsOfFilter,
          };
        } else if (props.start == 2) {
          selParams.value.pageType = listPage.value.pageType;
          selParams.value.op = listPage.value.op;
          selParams.value.action = listPage.value.action;
          selParams.value.moduleCode = listPage.value.moduleCode;
          selParams.value.formCode = listPage.value.formCode;
          selParams.value.parentFormCode = listPage.value.parentFormCode;
          selParams.value.nestFieldName = listPage.value.nestFieldName;
          selParams.value.nestType = listPage.value.nestType;
          selParams.value.parentId = listPage.value.parentId;
          selParams.value.mainId = listPage.value.mainId;
          selParams.value = {
            ...selParams.value,
            ...props.smartModuleSel.condsOfFilter,
          };
        } else if (props.start == 3) {
          selParams.value.moduleCode = listPage.value.moduleCode;
          selParams.value.formCode = listPage.value.formCode;
        } else {
          selParams.value = {};
        }

        // 不在此处传表头参数，而是写在模板中，以免表头设置改变后，列不出来
        // console.log('setResult flowColumns value', flowColumns.value);
        // setColumns(flowColumns.value);
        searchData();
      }
      // const newFlowColumns = computed(() => flowColumns.value);

      //切换顶部tab
      function getActiveKey(key: string) {
        moduleCode.value = key;
        getListPage();
      }

      // ---------------------------------------------------------------------------表格工具栏开始-----------------------------------------------------------
      //新增列表
      function handleAdd() {
        openDrawer(true, {
          isUpdate: 1,
          record: {
            moduleCode: listPage.value.moduleCode,
          },
        });
      }
      //修改列表
      function handleEdit(record: object) {
        openDrawer(true, {
          isUpdate: 2,
          record: {
            moduleCode: listPage.value.moduleCode,
            id: record['id'],
          },
        });
      }

      function handleDelete(record: object) {
        getVisualDel({ ids: record['id'], moduleCode: listPage.value.moduleCode }).then(() => {
          handleSuccess();
        });
      }

      //查看详情
      function handleView(record: object) {
        openDrawer(true, {
          isUpdate: 3,
          record: {
            moduleCode: listPage.value.moduleCode,
            id: record['id'],
          },
        });
      }

      //批量删除
      async function deleteAll() {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('请选择需要删除的数据');
          return;
        }
        await getVisualDel({ ids: keys.join(','), moduleCode: listPage.value.moduleCode }).then(
          () => {
            handleSuccess();
          },
        );
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
      }

      // ---------------------------------------------------------------------------顶部查询栏开始-----------------------------------------------------------

      const formParams = ref({});
      //查询
      function searchData() {
        if (!!moduleCode.value) {
          $('#moduleCode').empty();
          $('#moduleCode').val(unref(moduleCode) as string);
        }

        $('#op').empty();
        $('#op').val(listPage.value.op);
        let data = $('#' + getFormName.value).serializeArray();
        formParams.value = {};
        if (data.length > 0) {
          data.forEach((item) => {
            formParams.value[item['name']] = item['value'];
          });
        }
        reload();
      }

      //重置
      function resetData() {
        $('#' + getFormName.value)[0].reset();
        formParams.value = {};
        searchData();
      }
      //设置是否为空
      function setInputVal(dom, val) {
        $(dom).val(val);
      }
      // ---------------------------------------------------------------------------顶部查询栏结束-----------------------------------------------------------

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
        return await feakSave({ id: record.id, key, value });
      }

      function handleEditCancel() {
        console.log('cancel');
      }
      const filterOption = (input: string, option: any) => {
        return option.name.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      };
      // -----------------------------------------------------------单元格编辑结束----------------------------------------------------

      // -----------------------------------------------------管理开始-------------------------------------------------------------
      const go = useGo();
      function handleManager() {
        // visual/module_field_list.jsp?formCode=personbasic&code=personbasic
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
      //脚本
      function handleScript(record: any) {
        console.log('record', record);
        eval('(' + record.script + ')');
      }
      //链接
      function handleLink(record: any) {
        console.log('record', record);
      }
      // 自定义操作列按钮
      function handleColOperate(item, record) {
        console.log('item', item);
        console.log('record', record);
        /* getFlowInit({ typeCode: '16432767899234409135' }).then((res) => {
          let myActionId = res.myActionId || '';
          if (myActionId) {
            openProcessDrawer(true, {
              myActionId: myActionId,
            });
          }
        }); */
      }
      // -----------------------------------------------------操作列结束-------------------------------------------------------------

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
      //获取files
      function rawFileFn(files) {
        let formData = new FormData();
        formData.append('att1', files);
        formData.append('moduleCode', listPage.value.moduleCode);
        // formData.append('moduleCodeRelated', listPage.value.moduleCodeRelated);
        // formData.append('parentId', listPage.value.parentId);
        isImport.value = true;
        getVisualImportExcel(formData).then(() => {
          isImport.value = false;
          handleSuccess();
        });
      }

      //导出
      async function handleExport() {
        const columns = getColumns();
        const cols = ref<any>([]);
        if (afterChangecolumns.value.length > 0) {
          afterChangecolumns.value.forEach((item: any) => {
            if (item.visible && item.dataIndex != 'id' && item.dataIndex != 'colOperate')
              cols.value.push(item.dataIndex);
          });
        } else {
          if (columns.length > 0) {
            columns.forEach((item: any) => {
              if (item.dataIndex != 'id' && item.dataIndex != 'colOperate') {
                cols.value.push(item.dataIndex);
              }
            });
          }
        }

        let title = listPage.value.moduleName;
        let params = {
          moduleCode: listPage.value.moduleCode,
          cols: cols.value.join(','),
          ...searchInfo.value,
        };
        isExport.value = true;
        getVisualExportExcel(params).then((data) => {
          isExport.value = false;
          if (data) {
            downloadByData(data, `${title}.xls`);
          }
        });
      }
      // ---------------------------------------------------------------------------导出导入结束-----------------------------------------------------------

      // ---------------------------------------------------------------------------导出word开始---------------------------------------------------------
      async function handleExportWord() {
        let keys = getSelectRowKeys();
        let title = listPage.value.moduleName;
        let params = {
          moduleCode: listPage.value.moduleCode,
          ids: keys.join(','),
        };
        isExportWord.value = true;
        getVisualExportWord(params).then((data) => {
          isExportWord.value = false;
          if (data) {
            downloadByData(data, `${title}.doc`);
          }
        });
      }
      // ---------------------------------------------------------------------------导出word结束---------------------------------------------------------

      function setSearchFromClass() {
        // 设置搜索条件框中的tabIndex及使focus时高亮
        let searchForm = $('#' + getFormName.value)[0];
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

      function expandSearchForm() {
        isShowAllCond.value = !isShowAllCond.value;
        // 如果表格高度设为自适应，则需重新调整高度
        if (listPage.value.isAutoHeight) {
          redoHeight();
        }
      }

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
        labelCol: { style: { width: '100px' } },
        wrapperCol: { span: 14 },
        setInputVal,
        listPage,
        handleAdd,
        deleteAll,
        batchOp,
        handleEditEnd,
        handleEditCancel,
        beforeEditSubmit,
        filterOption,
        isShowSearch,
        getSelMode,
        getSelectRowKeys,
        setSelectedRowKeys,
        getSelectRows,
        handleManager,
        handleHistory,
        handleLogEdit,
        handleLogShow,
        handleScript,
        handleLink,
        handleColOperate,
        rawFileFn,
        isImport,
        isExport,
        isExportWord,
        handleExport,
        handleExportWord,
        columnsChange,
        flowColumns,
        expandSearchForm,
        isShowAllCond,
        getFormName,
      };
    },
  });
</script>
<style scoped>
  :deep(.ant-form-item-label) {
    overflow: inherit;
  }
  .custom-form input:not([type='radio'], [type='button'], [type='checkbox']) {
    height: 29px !important;
    width: 100% !important;
  }
  :deep(.custom-form input[type='checkbox']) {
    margin-top: 5px !important;
  }
  .custom-form select {
    width: 100% !important;
  }

  :deep(.custom-form .cond-text) {
    padding-top: 5px;
    padding-left: 2px;
    padding-right: 2px;
  }

  :deep(.custom-form input[type='radio']) {
    vertical-align: middle !important;
    margin-top: 5px !important;
    margin-left: 5px;
    margin-right: 5px;
    height: 18px !important;
  }
  :deep(.custom-form .cond-arrow) {
    margin-left: 2px;
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

  input {
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
</style>
