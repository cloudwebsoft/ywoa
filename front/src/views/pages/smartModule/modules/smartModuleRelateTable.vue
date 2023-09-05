<template>
  <div class="w-full">
    <div class="ml-3 mr-3 mt-3 p-2 bg-white" v-show="isShowSearch">
      <Form
        id="customFormRelate"
        :Name="getFormName"
        :label-col="labelCol"
        :wrapper-col="wrapperCol"
      >
        <Row id="ctlHtml">
          <Col
            v-for="(item, index) in listPage.aryCond"
            :key="index"
            :span="item.width ? item.width : 6"
          >
            <FormItem :label="item['title']">
              <!-- eslint-disable-next-line vue/no-v-html -->
              <div v-html="item['ctlHtml']" class="w-full flex"> </div>
            </FormItem>
          </Col>
        </Row>
        <Row class="flex justify-center">
          <Button @click="resetData">重置</Button>
          <Button type="primary" class="ml-1" @click="serchData" htmlType="submit">查询</Button>
        </Row>
        <input name="op" value="search" type="hidden" />
      </Form>
    </div>
    <BasicTable
      @register="registerTable"
      class="m-3"
      @edit-end="handleEditEnd"
      @edit-cancel="handleEditCancel"
      :beforeEditSubmit="beforeEditSubmit"
      :columns="flowColumns"
      @columns-change="columnsChange"
      :can-resize="listPage.isAutoHeight"
      @resize-column="handleResizeColumn"
    >
      <template #toolbar>
        <Button type="primary" class="mr-1" @click="handleAdd" v-if="listPage.isBtnAddShow">
          <template #icon><EditOutlined /></template>
          新增
        </Button>
        <Popconfirm
          placement="top"
          title="确定删除吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="deleteAll"
        >
          <Button type="primary" class="mr-1" v-if="listPage.isBtnDelShow">
            <template #icon><DeleteOutlined /></template> 删除
          </Button>
        </Popconfirm>

        <Button
          type="primary"
          class="mr-1"
          v-if="listPage.isBtnImportShow"
          :loading="isImport"
          @click="handleImport"
        >
          <template #icon><DownSquareOutlined /></template>
          导入
        </Button>

        <Button
          type="primary"
          class="mr-1"
          @click="handleExport"
          v-if="listPage.isBtnExportShow"
          :loading="isExport"
        >
          <template #icon><UpSquareOutlined /></template>
          导出
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
            @click="handleFlow(item)"
          >
            <template #icon
              ><i v-if="item['icon'] != 'none'" :class="'fa ' + item['icon']"></i
            ></template>
            {{ item.name }}
          </Button>
        </template>
        <Button type="primary" class="mr-1" v-if="listPage.isBtnExportWordShow">
          <template #icon><FileWordOutlined /></template>生成
        </Button>
        <Button type="primary" class="mr-1" v-if="listPage.isAdmin" @click="handleManager">
          <template #icon><SettingOutlined /></template>管理
        </Button>
      </template>
      <template #imgPrompt="{ record }">
        <div style="text-align: center">
          <img
            :style="[{ width: '26px', height: '26px' }, { display: record['url'] ? '' : 'none' }]"
            :src="record['url'] ? record['url'] : ''"
          />
        </div>
      </template>
      <template #iconCtl="{ record, column }">
        <div style="text-align: center" v-if="record['icon'] != ''">
          <img
            :style="[
              { width: '26px', height: '26px' },
              { display: record[column.dataIndex + '_meta_data'] ? 'inline' : 'none' },
            ]"
            :src="record['buf']"
          />
          <!-- <img
            :style="[
              { width: '26px', height: '26px' },
              { display: record[column.dataIndex + '_meta_data'] ? '' : 'none' },
            ]"
            :src="
              record[column.dataIndex + '_meta_data']
                ? record[column.dataIndex + '_meta_data'].url
                : ''
            "
          /> -->
        </div>
      </template>
      <template #action="{ record }">
        <div class="flex justify-between cursor-pointer">
          <template v-for="item in record['colOperate']" :key="item.id">
            <span v-if="item.type == 'DEL'" :title="item.name">
              <Popconfirm
                placement="left"
                :title="`确定${item.name}吗？`"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <Tooltip :title="item['name']">
                  <Icon
                    icon="ant-design:delete-outlined"
                    v-if="item.type == 'DEL'"
                    :title="item.name"
                    style="color: #f00"
                  />
                </Tooltip>
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
              <Tooltip :title="item['name']">
                <Icon
                  icon="ant-design:edit-outlined"
                  v-if="item.type == 'EDIT'"
                  :title="item.name"
                  style="color: #0960bd"
              /></Tooltip>
              <Tooltip :title="item['name']">
                <Icon
                  icon="clarity:info-standard-line"
                  v-if="item.type == 'SHOW'"
                  :title="item.name"
                  style="color: #0960bd"
              /></Tooltip>
              <Tooltip :title="item['name']">
                <i
                  :class="item.icon ? 'fa ' + item.icon : ''"
                  :style="[{ color: item.color ? item.color : '' }]"
                  :title="item.name"
                ></i>
              </Tooltip>
            </span>
          </template>
        </div>
      </template>
    </BasicTable>
    <smartModuleRelateTableDrawer
      @register="registerDrawer"
      @success="handleSuccess"
      @close="initWindowFunc"
    />
    <SmartModuleSelDrawer
      @register="registerSmartModuleSelDrawer"
      @success="handleSmartModuleSelCallBack"
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
  </div>
</template>
<script lang="ts">
  import {
    defineComponent,
    ref,
    unref,
    watch,
    nextTick,
    h,
    onMounted,
    onActivated,
    inject,
    onUnmounted,
    computed,
  } from 'vue';

  import { BasicTable, useTable } from '/@/components/Table';
  import {
    getVisualListRelate,
    getVisualListRelatePage,
    getVisualBatchOp,
    getVisualModuleEditInPlace,
    getVisualDelRelate,
    getVisualExportExcelRelate,
    getVisualImportExcel,
    getVisualDownloadExcelTempl,
  } from '/@/api/module/module';

  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Button, Form, Row, Col, Popconfirm, Tooltip, Modal } from 'ant-design-vue';
  import {
    DeleteOutlined,
    EditOutlined,
    SettingOutlined,
    DownSquareOutlined,
    UpSquareOutlined,
    FileWordOutlined,
  } from '@ant-design/icons-vue';
  import SmartModuleRelateTableDrawer from './smartModuleRelateTableDrawer.vue';
  import SmartModuleSelDrawer from './smartModuleSelDrawer.vue';
  import { ImpExcel } from '/@/components/Excel';
  import { filterJS, myMsg, ajaxGetJS } from '/@/utils/utils';
  import { downloadByData } from '/@/utils/file/download';
  import Icon from '/@/components/Icon/index';
  import { useGo } from '/@/hooks/web/usePage';
  import { useRouter } from 'vue-router';
  import { getShowImgInJar } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModuleRelateTable',
    components: {
      BasicTable,
      Button,
      Form,
      FormItem: Form.Item,
      Row,
      Col,
      SmartModuleRelateTableDrawer,
      Popconfirm,
      ImpExcel,
      Icon,
      DeleteOutlined,
      EditOutlined,
      SettingOutlined,
      DownSquareOutlined,
      UpSquareOutlined,
      FileWordOutlined,
      SmartModuleSelDrawer,
      Tooltip,
      Modal,
    },
    props: {
      activeRecord: {
        type: Object,
        default: () => {},
      },
    },
    setup(props) {
      const { createMessage, createConfirm } = useMessage();
      const [registerDrawer, { openDrawer }] = useDrawer();
      const isShowSearch = ref(false);
      const searchInfo = ref<any>({});
      const srcId = ref('-src');
      const dataSource = ref();
      const importVisible = ref(false);
      const isDownload = ref(false);
      const curFormUtil: any = inject('curFormUtil');
      const { currentRoute } = useRouter();

      const [
        registerTable,
        { reload, setSelectedRowKeys, setProps, getSelectRowKeys, getColumns },
      ] = useTable({
        title: '',
        api: getVisualListRelate,
        immediate: false,
        rowKey: 'id',
        rowSelection: {
          type: 'checkbox',
          columnWidth: 40,
        },
        beforeFetch: (info) => {
          let newInfo = info;
          newInfo = Object.assign({}, newInfo, formParams.value);
          searchInfo.value = newInfo;
          return newInfo;
        },
        afterFetch: (data) => {
          dataSource.value = data || [];
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
        },
        showTableSetting: true,
        tableSetting: {
          setting: false,
        },
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
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
            // 这里就是让数据位置互换，让视图更新 你们可以看record，index的输出，看是什么
            [dataSource.value[source], dataSource.value[target]] = [
              dataSource.value[target],
              dataSource.value[source],
            ];
            // console.log(record, index, 'target', source, target);
          },
          onDblclick: () => {
            // handleDbClick(record);
          },
        };
      }

      function handleDbClick(record) {
        console.log('handleDbClick record', record);
        if (listPage.value.canView) {
          handleView(record);
        } else {
          createMessage.warning('没有详情可以查看');
        }
      }

      onMounted(async () => {});

      const getFormName = computed(() => 'customForm' + curFormUtil?.getFormNo());

      // 这儿不能调用 initWindowFunc，因为smartModuleAddEditView会被缓存，当其onActivated时，会调用此处的initWindowFunc，导致当前curFormUtil指向的是本组件中的form
      // onActivated(() => {
      //   console.log('smartModuleRelateTable onActivated');
      //   setTimeout(() => {
      //     initWindowFunc();
      //   }, 100);
      // });

      onUnmounted(() => {
        console.log('Unmounted in smartModuleRelateTable!');
        curFormUtil.close(getFormName.value);
      });

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

      const searchData = ref<any>({});
      const listPageParams = ref({});
      watch(
        () => props.activeRecord,
        (record: object) => {
          record &&
            Object.keys(record).length > 0 &&
            nextTick(() => {
              searchData.value = record;
              listPageParams.value = {
                moduleCode: unref(searchData).params.moduleCode || '',
                menuItem: unref(searchData).params.menuItem || '',
                moduleCodeRelated: unref(searchData).params.moduleCodeRelated || '',
                parentId: unref(searchData).params.parentId || '',
                parentPageType: unref(searchData).params.parentPageType || '',
                formCode: unref(searchData).params.formCode || '',
                tagName: unref(searchData).tagName || '',
                mode: unref(searchData).mode || '',
              };
              setTimeout(() => {
                getListPage();
              }, 100);
            });
        },
      );

      let listPage = ref<any>({});
      let flowColumns = ref<any>([]);
      const getListPage = async () => {
        await initWindowFunc();

        await getVisualListRelatePage(unref(listPageParams)).then((res) => {
          listPage.value = res;
          setProps({ title: listPage.value.moduleName });
          listPage.value.aryCond =
            listPage.value.aryCond && listPage.value.aryCond.length > 0
              ? listPage.value.aryCond
              : [];
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
          if (res.colProps && res.colProps.length > 0) {
            res.colProps.forEach((item) => {
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
              if (item.title == '操作') {
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
              } else if (item.macroType == 'macro_icon_ctl') {
                flowColumns.value.push({
                  title: item.title,
                  dataIndex: item.field,
                  align: 'center',
                  fixed: item.fixed,
                  width: item.width,
                  ellipsis: true,
                  resizable: true,
                  slots: { customRender: 'iconCtl' },
                });
              } else if (item.type != 'checkbox') {
                flowColumns.value.push({
                  title: item.title,
                  dataIndex: item.field,
                  align: item.align || 'center',
                  // fixed: item.fixed,
                  width: item.width + 100,
                  ellipsis: true,
                  sorter: item.sort,
                  editComponent:
                    type === 'select' ? 'Select' : type === 'DATE' ? 'DatePicker' : 'Input',
                  edit: listPage.value.isEditInplace && item.field === fieldName,
                  editComponentProps: {
                    showSearch: true,
                    fieldNames: { label: 'name' },
                    options: options,
                  },
                  resizable: true,
                });
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
              listPage.value.aryCond.forEach((item) => {
                filterJS(item.ctlHtml);
                if (item.script) {
                  eval(item.script);
                }

                // 设置搜索条件框中的tabIndex及使focus时高亮
                let searchForm = $('#customFormRelate')[0];
                $(searchForm)
                  .find('input')
                  .each(function () {
                    if ($(this).attr('kind') == 'DATE' || $(this).attr('kind') == 'DATE_TIME') {
                      $(this).attr('autocomplete', 'off');
                    }
                  });

                let elements = searchForm.elements;
                let k = 50000; // 从50000开始，以免与列表页上的控件冲突
                for (let i = 0; i < elements.length; i++) {
                  let element = elements[i];
                  // 判断是否为隐藏元素
                  let $obj = $(element);
                  if (
                    !$obj.is(':hidden') &&
                    (element.tagName == 'INPUT' || element.tagName == 'SELECT')
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
              });

              // 初始化条件字段中的日期控件
              initDatePicker();
            }, 300);
          }

          setProps({
            searchInfo: {
              op: unref(listPage).op,
              mode: unref(listPage).mode,
              tagName: unref(listPage).tagName,
              moduleCode: unref(listPage).moduleCode,
              moduleCodeRelated: unref(listPage).moduleCodeRelated,
              parentId: unref(listPage).parentId,
              sort: unref(listPage).sort,
              orderBy: unref(listPage).orderBy,
            },
          });

          serchData();
        });

        // 列表页上的form_js注意得加上_list
        let rootPath = import.meta.env.VITE_PUBLIC_PATH;
        if (rootPath.endsWith('/')) {
          rootPath = rootPath.substring(0, rootPath.lastIndexOf('/'));
        }
        // 列表页上的form_js注意得加上_list
        const url = `${rootPath}/resource/js/form/form_js_${listPage.value.formCodeRelated}_list.js?pageType=moduleList&moduleCode=${listPage.value.formCodeRelated}&parentId=${listPage.value.parentId}`;
        console.log('list form_js url', url);

        // 此方法相对于直接appendChild，能知道js是否加载成功
        loadJs(url, `${100}${srcId.value}`).catch((e) => {
          // console.log('e', e);
          console.warn(`Form: ${listPage.value.formCodeRelated}'s js is not exist.`);
        });

        // 为向下兼容，引入服务端form_js
        await ajaxGetJS(
          `/flow/form_js/form_js_${listPage.value.formCodeRelated}.jsp?pageType=moduleListRelate&moduleCode=${listPage.value.formCodeRelated}&parentId=${listPage.value.parentId}`,
        );

        // 加载后台事件中配置的前台脚本
        if (listPage.value.formJs && listPage.value.formJs.length > 0) {
          let scriptFormJs = document.createElement('script');
          scriptFormJs.type = 'text/javascript';
          scriptFormJs.text = listPage.value.formJs;
          scriptFormJs.id = `${101}${srcId.value}`;
          document.getElementsByTagName('head')[0].appendChild(scriptFormJs);
        }
      };

      // ---------------------------------------------------------------------------表格工具栏开始-----------------------------------------------------------

      //新增列表
      function handleAdd() {
        openDrawer(true, {
          isUpdate: 1,
          record: {
            moduleCode: listPage.value.moduleCode,
            moduleCodeRelated: listPage.value.moduleCodeRelated,
            parentId: listPage.value.parentId,
          },
        });
      }
      //修改列表
      function handleEdit(record: object) {
        openDrawer(true, {
          isUpdate: 2,
          record: {
            moduleCode: listPage.value.moduleCode,
            moduleCodeRelated: listPage.value.moduleCodeRelated,
            parentId: listPage.value.parentId,
            id: record['id'],
          },
        });
      }
      //单个删除
      async function handleDelete(record: object) {
        await getVisualDelRelate({
          ids: record['id'],
          moduleCode: listPage.value.moduleCode,
          moduleCodeRelated: listPage.value.moduleCodeRelated,
          parentId: listPage.value.parentId,
        }).then(() => {
          handleSuccess();
        });
      }

      //查看详情
      function handleView(record: object) {
        if (listPage.value['opStyle'] == 1) {
          go({
            path: '/smartModuleAddEditView',
            query: {
              isTab: true,
              isUpdate: 3,
              parentPath: unref(currentRoute).path,
              moduleCode: listPage.value.moduleCodeRelated,
              id: record['id'],
              formCode: listPage.value.formCodeRelated,
              treeNodeCode: '',
              isTreeView: false,
              titleName: `${listPage.value.moduleName}-详情`,
              cacheName: `smartModuleAddEditView${record.id}`,
            },
          });
        } else {
          openDrawer(true, {
            isUpdate: 3,
            record: {
              moduleCode: listPage.value.moduleCode,
              moduleCodeRelated: listPage.value.moduleCodeRelated,
              parentId: listPage.value.parentId,
              id: record['id'],
            },
          });
        }
      }

      //批量删除
      function deleteAll() {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('请选择需要删除的数据');
          return;
        }
        getVisualDelRelate({
          ids: keys.join(','),
          moduleCode: listPage.value.moduleCode,
          moduleCodeRelated: listPage.value.moduleCodeRelated,
          parentId: listPage.value.parentId,
        }).then(() => {
          handleSuccess();
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
        serchData();
        setSelectedRowKeys([]);
        initWindowFunc();
      }

      // ---------------------------------------------------------------------------顶部查询栏开始-----------------------------------------------------------

      const formParams = ref({});
      //查询
      function serchData() {
        $('#op').empty();
        $('#op').val(listPage.value.op);
        let data = $('#customFormRelate').serializeArray();
        formParams.value = {};
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
                          listPage.value.aryCond.forEach((cond) => {
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

            formParams.value[item['name']] = item['value'];
          });
        }
        reload();

        // 清除已选择的记录，否则在查询得到新结果，导出时，仍会导出之前选择的记录
        setSelectedRowKeys([]);
      }

      //重置
      function resetData() {
        $('#customFormRelate')[0].reset();
        formParams.value = {};
        serchData();
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
              listPage.value.formCodeRelated +
              '&moduleCode=' +
              listPage.value.moduleCodeRelated,
          },
        });
      }
      // -----------------------------------------------------管理结束-------------------------------------------------------------
      // ---------------------------------------------------------------------------导出导入开始-----------------------------------------------------------
      //获取改变后的头部数据
      const afterChangecolumns = ref([]);
      function columnsChange(data) {
        afterChangecolumns.value = data;
      }
      //导出
      const isImport = ref(false);
      //导入
      const isExport = ref(false);
      //获取files
      function rawFileFn(files) {
        let formData = new FormData();
        formData.append('att1', files);
        formData.append('moduleCode', listPage.value.moduleCode);
        formData.append('moduleCodeRelated', listPage.value.moduleCodeRelated);
        formData.append('parentId', listPage.value.parentId);
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
            isImport.value = false;
            importVisible.value = false;
          })
          .catch((e) => {
            createMessage.error(e.msg);
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
        let keys = getSelectRowKeys();
        let ids = keys.join(',');
        let params = {
          moduleCodeRelated: listPage.value.moduleCodeRelated,
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
              isExport.value = true;
              getVisualExportExcelRelate(params).then((data) => {
                isExport.value = false;
                if (data) {
                  downloadByData(data, `${title}.xls`);
                }
              });
            },
          });
        } else {
          isExport.value = true;
          getVisualExportExcelRelate(params).then((data) => {
            isExport.value = false;
            if (data) {
              downloadByData(data, `${title}.xls`);
            }
          });
        }
      }
      // ---------------------------------------------------------------------------导出导入结束-----------------------------------------------------------

      // ---------------------------------------------------------------------------操作列开始-----------------------------------------------------------
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
      // 脚本
      function handleScript(record: any) {
        console.log('record', record);
        eval('(' + record.script + ')');
      }
      // 发起流程按钮
      function handleFlow(record: any) {
        // getFlowInit({ op: 'opBtnFlow', typeCode: record.flowTypeCode }).then((res) => {
        //   let myActionId = res.myActionId || '';
        //   if (myActionId) {
        //     openProcessDrawer(true, {
        //       myActionId: myActionId,
        //     });
        //   }
        // });
      }
      // 自定义操作列按钮
      function handleColOperate(item, record) {
        console.log('item', item);
        console.log('record', record);
        if (item.type == 'FLOW') {
          let params = {
            op: 'opLinkFlow',
            typeCode: item.flowTypeCode,
            ...item,
            // ...item.params,
          };
          // getFlowInit(params).then((res) => {
          //   let myActionId = res.myActionId || '';
          //   if (myActionId) {
          //     openProcessDrawer(true, {
          //       myActionId: myActionId,
          //     });
          //   }
          // });
        } else if (item.type == 'CLICK') {
          eval(item.script);
        }
      }
      // ---------------------------------------------------------------------------操作列结束-----------------------------------------------------------

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

      function getIdsSelected() {
        return getSelectRowKeys().join(',');
      }

      function getCurFormId() {
        return curFormUtil.get();
      }

      async function initWindowFunc() {
        setTimeout(async () => {
          await curFormUtil?.set(getFormName.value);
          console.log('smartModuleRelateTable initWindowFunc formName', getFormName.value);
        }, 100);

        let newWindow = window as any;
        newWindow.getCurFormId = getCurFormId;
        newWindow.goTo = goTo;
        newWindow.reload = reload;
        newWindow.myMsg = myMsg;
        newWindow.openSmartModuleSelTableDrawer = openSmartModuleSelTableDrawer;
        newWindow.getIdsSelected = getIdsSelected;
      }

      // -----------------------------------------------------选择模块开始-------------------------------------------------------------
      const [registerSmartModuleSelDrawer, { openDrawer: openSmartModuleSelDrawer }] = useDrawer();

      let myStart = 3;
      function openSmartModuleSelTableDrawer(selMode = 1, start = 1, params: object) {
        console.log('openSmartModuleSelTableDrawer params', params);
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
            console.warn(
              'form_js_' + listPage.value.formCodeRelated + ' 中未设置 handleModuleListSel',
            );
            console.warn(e);
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
        getVisualDownloadExcelTempl({ moduleCode: listPage.value.moduleCodeRelated }).then(
          (data) => {
            isDownload.value = false;
            let title = '模板';
            if (data) {
              downloadByData(data, `${title}.xls`);
            }
          },
        );
      }

      function handleImport() {
        importVisible.value = true;
      }

      return {
        registerTable,
        registerDrawer,
        handleEdit,
        handleView,
        handleDelete,
        handleSuccess,
        resetData,
        serchData,
        labelCol: { style: { width: '100px', whiteSpace: 'normal' } },
        wrapperCol: { span: 24 },
        setInputVal,
        listPage,
        handleAdd,
        deleteAll,
        batchOp,
        handleEditEnd,
        handleEditCancel,
        beforeEditSubmit,
        isShowSearch,
        rawFileFn,
        handleExport,
        handleHistory,
        handleLogEdit,
        handleLogShow,
        handleScript,
        handleFlow,
        handleColOperate,
        flowColumns,
        columnsChange,
        isImport,
        isExport,
        handleManager,
        registerSmartModuleSelDrawer,
        handleSmartModuleSelCallBack,
        initWindowFunc,
        handleResizeColumn: (w, col) => {
          col.width = w;
        },
        importVisible,
        handleCancel,
        handleDownloadExcelTempl,
        isDownload,
        handleImport,
        getFormName,
      };
    },
  });
</script>
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

  :deep(.ant-form-item) {
    margin-bottom: 10px;
  }

  :deep(#customFormRelate input:not([type='checkbox'], [type='radio'], [type='button'])) {
    height: 29px !important;
    width: 100% !important;
  }
  :deep(#customFormRelate select) {
    width: 100% !important;
  }
  :deep(#customFormRelate input[type='checkbox']) {
    margin-top: 5px !important;
  }
  :deep(#customFormRelate input[type='radio']) {
    vertical-align: middle !important;
    margin-top: 2px !important;
    margin-left: 5px;
    margin-right: 5px;
    height: 18px !important;
  }
</style>
