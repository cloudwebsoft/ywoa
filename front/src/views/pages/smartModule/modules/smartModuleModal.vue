<template>
  <BasicModal
    :footer="null"
    destroyOnClose
    :title="moduleName"
    v-bind="$attrs"
    :class="prefixCls"
    :icon="getIcon"
    @register="register"
  >
    <template #title
      ><Icon :icon="getIcon" :color="'rgb(250, 172, 4)'" />
      <span class="vben-basic-title">{{ moduleName }}</span>
    </template>
    <div id="visualFormBox" style="display: inline-block" class="w-full">
      <form id="visualForm" name="visualFormModal" :formCode="formCode">
        <!--  eslint-disable-next-line vue/no-v-html -->
        <div v-html="dataRef['rend']"> </div>
        <div style="page-break-after: always"></div>
        <input id="cwsHelper" name="cwsHelper" value="1" type="hidden" />
        <span id="spanTempCwsIds"></span>
      </form>
      <Row v-if="!isDialog && dataRef.isHasAttachment && pageType != 'show'">
        <Upload :file-list="fileList" :before-upload="beforeUpload" @remove="handleRemove">
          <a-button class="mt-1.5">
            <UploadOutlined />
            上传文件
          </a-button>
        </Upload>
      </Row>
      <BasicTable @register="registerTable" v-show="dataRef.isHasAttachment && isAttachmentShow">
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
                ifShow: () => pageType != 'show',
              },
            ]"
          />
        </template>
      </BasicTable>
      <Row v-if="false && pageType == 'show'" align="middle" style="clear: both">
        <div
          v-if="pageType == 'show' && (!dataRef.buttons || dataRef.buttons.length == 0)"
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
      <div :class="`${prefixCls}__footer`">
        <a-button type="primary" size="middle" @click="handleOk" :loading="loading">
          确定
        </a-button>
        <a-button type="primary" size="middle" class="ml-2" @click="handleCancel"> 取消 </a-button>
      </div>
    </div>
    <SelectUser @register="registerModal" @success="handleCallBack" />
    <WritePadModal @register="registerWritePadModal" @success="handleWritePadCallBack" />
    <LocationMarkModal
      @register="registerLocationMarkModal"
      @success="handleLocationMarkCallBack"
    />
    <SelDeptModal @register="registerSelDeptModal" @success="handleSelDeptCallBack" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted, h, inject } from 'vue';
  import { Row, UploadProps, Upload, Button } from 'ant-design-vue';
  import { Icon } from '/@/components/Icon';
  import { getToken } from '/@/utils/auth';
  import {
    getVisualAddPage,
    getVisualCreate,
    getVisualEditPage,
    getVisualUpdate,
    getVisualShowPage,
    getVisualListAtt,
    getVisualDelAttach,
    getVisualDownload,
  } from '/@/api/module/module';
  import { UploadOutlined } from '@ant-design/icons-vue';
  import { submitMyFile, downloadFile } from '/@/api/process/process';
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
    filterJS,
    ajaxPost,
    ajaxGet,
    ajaxGetJS,
    myConfirm,
    myMsg,
    ajaxPostJson,
    loadImg,
    initFormCtl,
  } from '/@/utils/utils';
  import SmartModuleSelDrawer from './smartModuleSelDrawer.vue';
  import { dateUtil as dayjs } from '/@/utils/dateUtil';
  import printJS from 'print-js';
  import { useUserStore } from '/@/store/modules/user';
  import { useGo } from '/@/hooks/web/usePage';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { BasicModal, useModalInner } from '/@/components/Modal/index';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModuleDrawer',
    components: {
      SelectUser,
      Row,
      BasicTable,
      TableAction,
      UploadOutlined,
      Upload,
      Button,
      WritePadModal,
      LocationMarkModal,
      SelDeptModal,
      BasicModal,
      Icon,
    },
    emits: ['success', 'register', 'close', 'editAction', 'launchFlowAction'],
    setup(_, { emit }) {
      const isUpdate = ref(1); // 1:新增，2编辑，3查看
      let dataRef = ref<any>({});
      const dataRecord = ref<any>({});
      const srcId = ref('-src');

      const pageType = ref('show');
      const formCode = ref('');
      const go = useGo();
      const isAttachmentShow = ref(true);
      const { prefixCls } = useDesign('header-lock-modal');
      const loading = ref(false);
      const moduleName = ref('');
      const { createMessage } = useMessage();
      const curFormUtil: any = inject('curFormUtil');
      const isDialog = ref(false);
      const html = ref(''); // 用于对话框模式，替换原表单中的Html

      const [register, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '45%' });
        initWindowFunc();
        removeScript(unref(srcId));
        removeLink();
        dataRecord.value = data.record || {};
        dataRef.value = {};
        isUpdate.value = data.isUpdate;
        isDialog.value = data.isDialog == undefined ? false : true;
        html.value = data.record.html ? data.record.html : '';
        console.log('data.record.html', data.record.html);
        if (html.value != '') {
          moduleName.value = data.title;
          $(o('visualFormModal')).append(html.value);
          let rules = data.record.rules;
          console.log('rules', rules);
          if (rules) {
            for (let i in rules) {
              let rule = rules[i];
              console.log('rule', rule);
              if (rule.required) {
                new LiveValidation(rule.field).add(Validate.Presence);
              }
            }
          }
          initFormCtl('visualForm');
        } else {
          setTimeout(() => {
            getFirstTabInfo();
          }, 10);
        }
      });

      const userStore = useUserStore();

      async function getFirstTabInfo() {
        var liveOp = new LiveValidation('cwsHelper');
        LiveValidation.destroyValidate(liveOp.formObj.fields);
        $('.LV_presence').remove();

        // 在flow_js.js中通过openWinModuleShow传过来的参数
        let params = dataRecord.value.params ? dataRecord.value.params : {};
        console.log('getFirstTabInfo params', params, 'isUpdate', isUpdate.value);

        if (unref(isUpdate) === 1) {
          pageType.value = 'add';
          isAttachmentShow.value = false;
          console.log('getFirstTabInfo getVisualAddPage dataRecord', dataRecord.value);
          await getVisualAddPage({ ...dataRecord.value, ...params }).then((res) => {
            dataRef.value = res;
            formCode.value = dataRef.value.formCode;
            moduleName.value = dataRef.value.moduleName;

            filterJS(dataRef.value['rend'], '-src', o('visualFormModal'));

            setTimeout(() => {
              // 初始化计算控件
              initCalculator();
            }, 100);
          });
        } else if (unref(isUpdate) === 2) {
          pageType.value = 'edit';
          console.log('getFirstTabInfo dataRecord', dataRecord.value);
          await getVisualEditPage({
            moduleCode: unref(dataRecord).moduleCode,
            id: unref(dataRecord).id,
            isTreeView: unref(dataRecord).isTreeView, // 是否树形视图
            treeNodeCode: unref(dataRecord).treeNodeCode, // 树形视图中所选中的节点
            ...params,
          }).then((res) => {
            dataRef.value = res;
            formCode.value = dataRef.value.formCode;

            filterJS(dataRef.value['rend'], '-src', o('visualFormModal'));

            console.log('初始化计算控件');
            setTimeout(() => {
              // 初始化计算控件
              initCalculator();
            }, 100);
          });
        } else {
          pageType.value = 'show';
          await getVisualShowPage({
            moduleCode: unref(dataRecord).moduleCode,
            id: unref(dataRecord).id,
            visitKey: unref(dataRecord).visitKey,
            isTreeView: unref(dataRecord).isTreeView, // 是否树形视图
            treeNodeCode: unref(dataRecord).treeNodeCode, // 树形视图中所选中的节点
            ...params,
          }).then((res) => {
            dataRef.value = res;
            filterJS(dataRef.value['rend'], '-src', o('visualFormModal'));
          });
        }
        // isHasAttachment 是否上传文件
        // 因为有可能表单中不带有附件，但是带有可视化上传控件（上传以后会reloadAttachment)，所以searchInfo中参数仍需初始化
        setTimeout(() => {
          setProps({
            searchInfo: {
              moduleCode: unref(dataRef).moduleCode,
              id: unref(dataRef).id,
              isShowPage: isUpdate.value != 1 && isUpdate.value != 2,
              visitKey: unref(dataRecord).visitKey,
              flowId: unref(dataRecord).flowId,
            },
          });
          if (unref(dataRef).isHasAttachment && unref(dataRef).id) {
            reloadAttachment();
          }
        }, 10);

        if (dataRef.value.pageType) {
          pageType.value = dataRef.value.pageType;
        } else {
          pageType.value = dataRecord.value.pageType ? dataRecord.value.pageType : 'show';
        }

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
        }&moduleCode=${thisModuleCode}&id=${dataRecord.value.id ? dataRecord.value.id : ''}`;
        console.log('form_js url', pageType.value + ' ' + url);
        let script1 = document.createElement('script');
        script1.type = 'text/javascript';
        script1.src = url;
        script1.id = `${100}${srcId.value}`;
        document.getElementsByTagName('head')[0].appendChild(script1);

        // 加载后台事件中配置的前台脚本
        if (dataRef.value.formJs && dataRef.value.formJs.length > 0) {
          let scriptFormJs = document.createElement('script');
          scriptFormJs.type = 'text/javascript';
          scriptFormJs.text = dataRef.value.formJs;
          scriptFormJs.id = `${101}${srcId.value}`;
          document.getElementsByTagName('head')[0].appendChild(scriptFormJs);
        }

        loadImg('visualForm');

        setTimeout(() => {
          $('#visualForm input[type=radio]').each(function (i) {
            if ($(this).attr('readonly') == null) {
              $(this).addClass('radio-menu');
            }
          });

          // 不能用BootstrapMenu，因为chrome上会导致radio无法点击
          // $.contextMenu({
          //   selector: '.radio-menu',
          //   trigger: 'hover',
          //   delay: 1000,
          //   callback: function (key, options) {
          //     if (key == 'cancel') {
          //       var $obj = options.$trigger;
          //       var name = $obj.attr('name');
          //       $('input[type=radio][name="' + name + '"]:checked').attr('checked', false);
          //     }
          //   },
          //   items: {
          //     cancel: {
          //       name: '取消选择',
          //       icon: function ($element, key, item) {
          //         return 'context-menu-icon context-menu-icon-quit';
          //       },
          //     },
          //   },
          // });

          if (unref(isUpdate) === 1 || unref(isUpdate) === 2) {
            // 设置控件的只读状态
            setNotReadOnly();

            initFormCtl('visualForm');
          }

          // 初始化日期控件
          initDatePicker();
        }, 100);
      }

      // 设置控件的只读状态
      function setNotReadOnly() {
        let obj = o('visualForm');
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
              if (readOnlyType === '0' || readOnlyType === '2') {
                isUseReadOnly = false;
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
        unref(isUpdate) === 1 ? '新增' : unref(isUpdate) === 2 ? '编辑' : '查看',
      );

      // -------------------------文件上传开始-------------------------------

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
      const [registerTable, { reload: reloadAttachment, setProps, getDataSource }] = useTable({
        title: '', // '附件列表',
        api: getVisualListAtt,
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

      function downloadFileVisual(fileName, params) {
        getVisualDownload(params).then((data) => {
          if (data) {
            downloadByData(data, fileName);
          }
        });
      }

      // -------------------------文件列表结束-------------------------------------
      function getSelectMultipleVal(obj) {
        let values = [];
        for (let i = 0; i < obj.options.length; i++) {
          if (obj.options[i].selected) {
            values.push(obj.options[i].value);
          }
        }
        return values.join(',');
      }

      //保存
      async function handleSubmit() {
        loading.value = true;
        try {
          // 要摧毁校验，包括不允许为空的*以及错误提示都需要摧毁，再实例化lv_cwsWorkflowResult，再调用FormUtil.doGetCheckJS
          if (o('visualFormModal')) {
            var liveOp = new LiveValidation(findObjInForm('visualFormModal', 'cwsHelper'));
            // LiveValidation.destroyValidate(liveOp.formObj.fields);
            // $('.LV_presence').remove();

            if (!LiveValidation.massValidate(liveOp.formObj.fields)) {
              LiveValidation.liveErrMsg = LiveValidation.liveErrMsg.replaceAll('<br/>', '\n');
              createMessage.warn({
                content: () => h('pre', LiveValidation.liveErrMsg),
              });
              return;
            }

            let form = new FormData(o('visualFormModal') as any);
            if (fileList.value.length > 0) {
              fileList.value.forEach((file: UploadProps['fileList'][number], index) => {
                form.append(`att${index + 1}`, file as any);
              });
            }

            var dropFieldName = null;
            // FormData中只能取到第一个文件，需删除掉，得取dropFiles
            for (var key of form.keys()) {
              console.log('key:', key, 'value:', form.get(key), 'type:', typeof form.get(key));
              // 如果是可视化多文件上传控件则删除键值，注意要先判断是否存在o(key)，因为当用抽屉中表单自身的上传功能上传时，在上面fileList经append转换后，key会为att1、att2...，但o(key)不存在
              let keyObj = findObj(key);
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

            if (unref(isUpdate) === 1) {
              // rowIds为工具条“添加模块”按钮点击前选择的记录的ID
              if (dataRecord.value.rowIds) {
                form.append('rowIds', dataRecord.value.rowIds);
              }
              // moduleAction为按钮在后台配置中所传的参数
              if (dataRecord.value.moduleAction) {
                form.append('moduleAction', dataRecord.value.moduleAction);
                // 加上op，以简化书写
                form.append('op', dataRecord.value.moduleAction);
              }

              form.append('curModuleCode', dataRecord.value.params.curModuleCode);

              let query = `?moduleCode=${dataRecord.value.moduleCode}&isTreeView=${dataRecord.value.isTreeView}&treeNodeCode=${dataRecord.value.treeNodeCode}`;
              let data = await getVisualCreate(form, query);
              if (data.res === 0) {
                createMessage.success('操作成功');
              }
            } else if (unref(isUpdate) === 2) {
              let query = `?id=${dataRecord.value.id}&moduleCode=${dataRecord.value.moduleCode}&flowId=${dataRecord.value.flowId}&isTreeView=${dataRecord.value.isTreeView}&treeNodeCode=${dataRecord.value.treeNodeCode}`;
              await getVisualUpdate(form, query);
            }
          } else {
            console.warn('handleSubmit: visualFormModal is not exist');
          }

          onClose();
          console.log('closeModal');
          closeModal();

          emit('success');
          // 发送close，以使得在父组件中调用initWindowFunc
          emit('close');
          fileList.value = [];
        } finally {
          loading.value = false;
        }
      }

      function onClose() {
        removeScript(unref(srcId));
        removeLink();
        curFormUtil.close('visualFormModal');
        emit('close');
      }

      onMounted(() => {});

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
        return curFormUtil.get();
      }

      function initWindowFunc() {
        curFormUtil.set('visualFormModal');

        let newWindow = window as any;
        newWindow.getCurFormId = getCurFormId;
        newWindow.ajaxPost = ajaxPost;
        newWindow.ajaxGet = ajaxGet;
        newWindow.submitMyFile = submitMyFile;
        newWindow.ajaxGetJS = ajaxGetJS;
        newWindow.filterJS = filterJS;
        newWindow.selectUserInForm = selectUserInForm;
        newWindow.myConfirm = myConfirm;
        newWindow.myMsg = myMsg;
        newWindow.ajaxPostJson = ajaxPostJson;
        newWindow.downloadFile = downloadFile; // 流程附件下载
        newWindow.reloadAttachment = reloadAtt;
        newWindow.downloadFileVisual = downloadFileVisual; // 模块附件下载
        newWindow.getServerUrl = getServerUrl;
        newWindow.getPublicPath = getPublicPath;
        newWindow.getToken = getToken;
        newWindow.openWritePadModal = openWritePadModal;
        newWindow.openLocationMarkModal = openLocationMarkModal;
        newWindow.openSelDeptModal = openSelDeptModal;
        newWindow.initFormCtl = initFormCtl;
      }

      const [registerModal, { openModal }] = useModal();

      function handleOpenEditDrawer() {
        removeScript(unref(srcId));
        removeLink();
        emit('editAction', dataRecord.value);
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
          removeScript(unref(srcId));
          removeLink();
          item['moduleId'] = dataRecord.value['id'];
          item['btnId'] = item.id;
          emit('launchFlowAction', item);
        } else {
          // click事件
          try {
            eval(item['link']);
          } catch (e) {
            console.log('handleBtnClick', e);
          }
        }
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

      // 当用于dialog模式时，返回表单中的数据
      function handleReturnFormData() {
        let form = new FormData(o('visualFormModal') as any);
        if (o('visualFormModal')) {
          var liveOp = new LiveValidation(findObjInForm('visualFormModal', 'cwsHelper'));
          // LiveValidation.destroyValidate(liveOp.formObj.fields);
          // $('.LV_presence').remove();

          if (!LiveValidation.massValidate(liveOp.formObj.fields)) {
            LiveValidation.liveErrMsg = LiveValidation.liveErrMsg.replaceAll('<br/>', '\n');
            createMessage.warn({
              content: () => h('pre', LiveValidation.liveErrMsg),
            });
            return;
          }

          console.log('smartModuleModal form1', form);

          // FormData中只能取到第一个文件，需删除掉，得取dropFiles
          for (var key of form.keys()) {
            console.log('key:', key, 'value:', form.get(key), 'type:', typeof form.get(key));
            let keyObj = fo(key);

            // FormData中，多选型select如果有多个值，则只能记录一个，而如果没有值，则不会记录
            if (keyObj && keyObj.tagName == 'SELECT') {
              if ($(keyObj).attr('multiple') != null) {
                let val = getSelectMultipleVal(keyObj);
                console.log('select multiple name=', key, ' val=', val);
                form.set(key, val);
              }
            }
          }

          // moduleAction为按钮在后台配置中所传的参数
          if (dataRecord.value.op) {
            form.append('op', dataRecord.value.op);
          }

          if (unref(isUpdate) === 1) {
            // rowIds为工具条“添加模块”按钮点击前选择的记录的ID
            if (dataRecord.value.rowIds) {
              form.append('rowIds', dataRecord.value.rowIds);
            }

            form.append('curModuleCode', dataRecord.value.params.curModuleCode);
          }
        } else {
          console.warn('handleReturnFormData: visualFormModal is not exist');
        }

        onClose();
        console.log('Close dialog');
        closeModal();
        console.log('smartModuleModal form2', form);
        emit('success', form);
        // 发送close，以使得在父组件中调用initWindowFunc
        emit('close');
      }

      async function handleOk() {
        if (!isDialog.value) {
          handleSubmit();
        } else {
          handleReturnFormData();
        }
      }

      async function handleCancel() {
        onClose();
        closeModal();
      }

      const getIcon = computed(() => 'clarity:error-standard-line');

      return {
        getTitle,
        handleSubmit,
        dataRef,
        registerModal,
        handleCallBack,
        SmartModuleSelDrawer,
        isUpdate,
        registerTable,
        handleDelete,
        handleDownload,
        beforeUpload,
        handleRemove,
        fileList,
        pageType,
        printForm,
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
        prefixCls,
        register,
        handleOk,
        handleCancel,
        loading,
        moduleName,
        getIcon,
        isDialog,
      };
    },
  });
</script>

<style lang="less" scoped>
  :deep(.vben-basic-table .ant-table-wrapper) {
    padding: 6px 0;
  }

  @prefix-cls: ~'@{namespace}-header-lock-modal';

  .@{prefix-cls} {
    &__entry {
      position: relative;
      //height: 240px;
      padding: 130px 30px 30px;
      border-radius: 10px;
    }

    &__header {
      position: absolute;
      top: 0;
      left: calc(50% - 45px);
      width: auto;
      text-align: center;

      &-img {
        width: 70px;
        border-radius: 50%;
      }

      &-name {
        margin-top: 5px;
      }
    }

    &__footer {
      text-align: center;
      margin-top: 10px;
    }
  }

  .vben-basic-title {
    padding-left: 7px;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    color: rgba(0, 0, 0, 0.85);
    cursor: pointer;
    -webkit-user-select: none;
    -moz-user-select: none;
    user-select: none;
  }
</style>
