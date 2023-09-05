<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm">
      <!-- <template #color="{ model, field }"> -->
      <!-- <color-picker v-model:pureColor="model[field]" shape="square" useType="both" format="hex" /> -->
      <!-- <div id="colors">
          <Teleport to="#colors">
            <color-picker
              v-model:pureColor="model[field]"
              shape="circle"
              useType="both"
              format="hex"
            />
          </Teleport>
        </div> -->
      <!-- </template> -->
      <template #file>
        <Upload :file-list="fileList" :before-upload="beforeUpload" @remove="handleRemove">
          <Button>
            <upload-outlined />
            上传文件
          </Button>
        </Upload>
      </template>
      <!-- v-model:pureColor="pureColor" 
          v-model:gradientColor="gradientColor"-->
      <template #realNames="{ model, field }">
        <div>
          <FormItemRest>
            <div class="mb-2" v-show="!isUpdate">
              <RadioGroup v-model:value="radioValue" button-style="solid">
                <RadioButton value="radioAll" @click="getRadioValue('radioAll')"
                  >全部用户</RadioButton
                >
                <RadioButton value="radioSelect" @click="getRadioValue('radioSelect')"
                  >选择用户</RadioButton
                >
              </RadioGroup>
            </div>
          </FormItemRest>
          <InputTextArea :disabled="true" :row="3" v-model:value="model[field]" />
        </div>
      </template>
    </BasicForm>
    <BasicTable @register="registerTable" v-show="oaNoticeAttList.length > 0">
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
    <SelectUser @register="registerModal" @success="handleModelSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { UploadOutlined } from '@ant-design/icons-vue';
  import { Input, Radio, Form, Upload, Button, UploadProps } from 'ant-design-vue';
  import { formSchema } from './notice.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { SelectUser } from '/@/components/CustomComp';
  import { useModal } from '/@/components/Modal';
  import { dateUtil, formatToDate } from '/@/utils/dateUtil';
  import { BasicColumn } from '/@/components/Table';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { downloadByData } from '/@/utils/file/download';
  import {
    getNoticeAdd,
    getNoticeCreate,
    getNoticeEdit,
    getNoticeSave,
    getNoticeDelAtt,
    getNoticeAttDownload,
  } from '/@/api/administration/administration';
  export default defineComponent({
    name: 'NoticeDrawer',
    components: {
      BasicDrawer,
      BasicForm,
      SelectUser,
      RadioGroup: Radio.Group,
      RadioButton: Radio.Button,
      FormItemRest: Form.ItemRest,
      InputTextArea: Input.TextArea,
      Upload,
      Button,
      UploadOutlined,
      BasicTable,
      TableAction,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive({});
      let radioValue = ref('radioAll');
      const fileList = ref<UploadProps['fileList']>([]);
      const gradientColor = ref(
        'linear-gradient(0deg, rgba(0, 0, 0, 1) 0%, rgba(0, 0, 0, 1) 100%)',
      );
      const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
        labelWidth: 90,
        schemas: formSchema,
        showActionButtonGroup: false,
      });
      let oaNoticeAttList = ref([]);
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        resetFields();
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = data?.isUpdate;
        radioValue.value = 'radioAll';
        fileList.value = [];
        oaNoticeAttList.value = [];
        if (unref(isUpdate)) {
          await getNoticeEdit({ id: data.record.id }).then((res) => {
            let notice = res.notice;
            oaNoticeAttList.value = notice.oaNoticeAttList || [];
            if (unref(oaNoticeAttList).length > 0) {
              setTableData(unref(oaNoticeAttList));
            }
            if (!res.names) {
              radioValue.value = 'radioAll';
            } else {
              radioValue.value = 'radioSelect';
            }
            dataRef = {
              ...notice,
              realNames: res.realNames ? res.realNames : '全部用户',
              receiver: res.names,
              level: notice.noticeLevel == 1,
              isReply: notice.isReply == 1,
              isShow: notice.isShow,
              isForcedResponse: notice.isForcedResponse == 1,
              isToMobile: notice.isToMobile == 1,
              color: data.record.color ? data.record.color : '#000000',
              isBold: data.record.isBold == 1,
            };
            updateSchema({
              field: 'realNames',
              required: false,
            });
            setFieldsValue({
              ...dataRef,
            });
          });
        } else {
          await getNoticeAdd().then((res) => {
            dataRef = {
              realNames: '全部用户',
              color: '#000000',
              unitCode: res.myUnitCode,
              userName: res.userName,
              beginDate: formatToDate(dateUtil()),
            };
          });
          updateSchema({
            field: 'realNames',
            required: true,
          });
          setFieldsValue({
            ...dataRef,
          });
        }
      });
      const columns: BasicColumn[] = [
        {
          title: '标题',
          dataIndex: 'name',
          align: 'left',
        },
        {
          title: '创建时间',
          dataIndex: 'uploadDate',
        },
      ];
      const [registerTable, { setTableData }] = useTable({
        title: '文件列表',
        api: '' as any,
        columns,
        formConfig: {},
        searchInfo: {
          op: 'search',
        },
        useSearchForm: false,
        showTableSetting: false,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        immediate: false,
        pagination: false,
        canResize: false,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      function handleDelete(record, index) {
        console.log('record', record);
        let params = {
          noticeId: dataRef.id,
          attId: record.id,
        };
        getNoticeDelAtt(params).then((res) => {
          console.log('res==>', res);
          oaNoticeAttList.value.splice(index, 1);
          setTableData(unref(oaNoticeAttList));
        });
      }

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      function getRadioValue(e) {
        if (e === 'radioAll') {
          setFieldsValue({
            realNames: '全部用户',
          });
        } else {
          selectUser();
        }
      }

      const [registerModal, { openModal }] = useModal();
      function selectUser() {
        openModal(true, {
          type: 'notice', //公告入口判断
        });
      }

      async function handleModelSuccess(data) {
        //选择用户后的回调
        let realNames = data.map((item) => item.realName);
        let receivers = data.map((item) => item.name);
        dataRef.receiver = receivers;
        setFieldsValue({
          realNames: realNames,
        });
      }
      async function handleSubmit() {
        try {
          console.log('dataRef111', dataRef);
          const values = await validate();
          let formData = Object.assign({}, dataRef, values);

          console.log('formData', formData);
          console.log('dataRef', dataRef);
          formData.level = formData.level == true ? 1 : 0;
          formData.isShow = formData.isShow == true ? 1 : 0;
          formData.isReply = formData.isReply == true ? 1 : 0;
          formData.isForcedResponse = formData.isForcedResponse == true ? 1 : 0;
          let params = {
            title: formData.title,
            color: formData.color,
            isShow: formData.isShow,
            isReply: formData.isReply,
            isToMobile: formData.isToMobile,
            content: formData.content,
            beginDate: formData.beginDate,
            endDate: formData.endDate ? formData.endDate : '',
            isNoticeMgr: formData.isNoticeMgr,
            userName: formData.userName,
            unitCode: formData.unitCode,
            isAll: radioValue.value == 'radioAll' ? 2 : 0,
            receiver: formData.receiver,
            att1: formData.att1, //文件  多个就是 att2,att3...
            isForcedResponse: formData.isForcedResponse,
            level: formData.level,
            isBold: formData.isBold ? 1 : 0,
          };
          if (fileList.value.length > 0) {
            fileList.value.forEach((file: UploadProps['fileList'][number], index) => {
              params[`att${index + 1}`] = file as any;
            });
          }
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          let Files = new FormData();
          if (!unref(isUpdate)) {
            for (let v in params) {
              Files.append(v, params[v]);
            }
            await getNoticeCreate(Files);
          } else {
            params.id = formData.id;
            console.log('params', params);
            for (let v in params) {
              Files.append(v, params[v]);
            }
            await getNoticeSave(Files);
          }
          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      function getColor(e, node) {
        console.log('resssss', e, node);
      }

      const handleRemove: UploadProps['onRemove'] = (file) => {
        const index = fileList.value.indexOf(file);
        const newFileList = fileList.value.slice();
        newFileList.splice(index, 1);
        fileList.value = newFileList;
      };

      const beforeUpload: UploadProps['beforeUpload'] = (file) => {
        fileList.value = [...fileList.value, file];
        return false;
      };

      const handleUpload = () => {
        // const formData = new FormData();
        // fileList.value.forEach((file: UploadProps['fileList'][number]) => {
        //   formData.append('files[]', file as any);
        // });
      };

      function handleDownload(record: any) {
        record.isDownloadAtt = true;
        const params = {
          visitKey: record.visitKey,
          attachId: record.id,
        };
        console.log('reocrd.name', record.name);
        getNoticeAttDownload(params)
          .then((data) => {
            if (data) {
              downloadByData(data, `${record.name}`);
            }
          })
          .finally(() => {
            record.isDownloadAtt = false;
          });
      }

      return {
        registerDrawer,
        registerForm,
        getTitle,
        radioValue,
        handleSubmit,
        registerModal,
        selectUser,
        handleModelSuccess,
        getRadioValue,
        gradientColor,
        handleRemove,
        beforeUpload,
        handleUpload,
        fileList,
        getColor,
        registerTable,
        handleDelete,
        oaNoticeAttList,
        isUpdate,
        handleDownload,
      };
    },
  });
</script>
