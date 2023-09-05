<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="50%"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm">
      <template #photo="{ model, field }">
        <Upload
          name="avatar"
          v-model="model[field]"
          :file-list="fileList"
          list-type="picture-card"
          class="avatar-uploader"
          :show-upload-list="false"
          :before-upload="beforeUpload"
          :customRequest="handleCustomRequest"
          @change="handleChange"
        >
          <img v-if="imageUrl" :src="imageUrl" alt="avatar" />
          <div v-else>
            <loading-outlined v-if="loading" />
            <plus-outlined v-else />
            <div class="ant-upload-text">Upload</div>
          </div>
        </Upload>
        <a v-if="imageUrl" @click="resetPortrait" class="reset-portrait">重置</a>
      </template>
      <template #leaderName="{ model, field }">
        <Row class="justify-between">
          <Col :span="19">
            <Input disabled v-model:value="model[field]" placeholder="请选择他的领导" />
          </Col>
          <Col :span="4">
            <Button type="primary" @click="handleCreate">选择</Button>
          </Col>
        </Row>
      </template>
    </BasicForm>
    <SelectUser @register="registerModal" @success="handleSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive, h } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import {
    getUserCreate,
    getUpdateMyInfo,
    getUpeditUser,
    getResetPortrait,
    // getUserCheckMobile,
    // getUserCheckPersonNo,
    // getUserCheckPwd,
    // getUserCheckUserName,
    getShowImg,
  } from '/@/api/system/system';
  import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue';
  import {
    message,
    Upload,
    Button,
    Input,
    Row,
    Col,
    UploadChangeParam,
    UploadProps,
  } from 'ant-design-vue';
  import { SelectUser } from '/@/components/CustomComp';
  import { useModal } from '/@/components/Modal';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { FormSchema } from '/@/components/Table';
  import { createMessageGuard } from '/@/router/guard';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';

  const { t } = useI18n();
  const { createMessage, createConfirm } = useMessage();

  function getBase64(img: Blob, callback: (base64Url: string) => void) {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result as string));
    reader.readAsDataURL(img);
  }
  export default defineComponent({
    name: 'PersonDrawer',
    components: {
      BasicDrawer,
      BasicForm,
      PlusOutlined,
      LoadingOutlined,
      Upload,
      Button,
      Input,
      Row,
      Col,
      SelectUser,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive<any>({});

      let fileList = ref([]);
      let loading = ref<boolean>(false);
      let imageUrl = ref<any>('');
      const accountFormSchema: FormSchema[] = [
        {
          field: 'roleName',
          label: '角色',
          component: 'Input',
          colProps: {
            span: 12,
          },
          componentProps: {
            placeholder: '  ',
            disabled: true,
          },
        },
        {
          field: 'name',
          label: '账号',
          component: 'Input',
          colProps: {
            span: 12,
          },
          componentProps: {
            disabled: true,
          },
        },
        {
          field: 'realName',
          label: '姓名',
          component: 'Input',
          colProps: {
            span: 12,
          },
          componentProps: {
            disabled: true,
          },
        },
        {
          field: 'personNo',
          label: '工号',
          component: 'Input',
          colProps: {
            span: 12,
          },
          componentProps: {
            disabled: true,
          },
        },
        {
          field: 'deptName',
          label: '部门',
          component: 'Input',
          componentProps: {
            disabled: true,
          },
          colProps: {
            span: 24,
          },
        },
        {
          field: 'photo',
          label: '头像',
          component: 'Input',
          slot: 'photo',
          colProps: {
            span: 24,
          },
        },
        {
          field: 'mobile',
          label: '手机号',
          component: 'Input',
          colProps: {
            span: 12,
          },
          componentProps: {
            // readonly: true,
          },
        },

        {
          field: 'Marriage',
          label: '婚否',
          component: 'RadioGroup',
          defaultValue: '0',
          colProps: {
            span: 12,
          },
          componentProps: {
            options: [
              {
                label: '已婚',
                value: '1',
              },
              {
                label: '未婚',
                value: '0',
              },
            ],
          },
        },
        {
          field: 'qq',
          label: 'QQ',
          component: 'Input',
          colProps: {
            span: 12,
          },
        },
        {
          field: 'entryDate',
          label: '入职日期',
          component: 'DatePicker',
          colProps: {
            span: 12,
          },
          componentProps: {
            valueFormat: 'YYYY-MM-DD',
            style: { width: '100%' },
          },
        },
        {
          field: 'type',
          label: '类型',
          component: 'Select',
          colProps: {
            span: 12,
          },
          componentProps: {
            options: [
              {
                label: '类型1',
                value: 1,
              },
              {
                label: '类型2',
                value: 0,
              },
            ],
          },
        },
        {
          field: 'leaderName',
          label: '我的领导',
          component: 'Input',
          slot: 'leaderName',
          colProps: {
            span: 12,
          },
          componentProps: {
            disabled: true,
          },
        },
        {
          field: 'postCode',
          label: '邮政编码',
          component: 'Input',
          colProps: {
            span: 12,
          },
        },
        {
          field: 'gender',
          label: '性别',
          component: 'RadioGroup',
          defaultValue: '0',
          componentProps: {
            options: [
              { label: '男', value: '0' },
              { label: '女', value: '1' },
            ],
          },
          colProps: {
            span: 12,
          },
        },
        {
          field: 'birthday',
          label: '出生日期',
          component: 'DatePicker',
          componentProps: {
            valueFormat: 'YYYY-MM-DD',
            style: { width: '100%' },
          },
          colProps: {
            span: 12,
          },
        },
        {
          field: 'Email',
          label: 'E-mail',
          component: 'Input',
          colProps: {
            span: 12,
          },
        },
        {
          field: 'IDCard',
          label: '身份证号码',
          component: 'Input',
          colProps: {
            span: 12,
          },
          componentProps: {
            readonly: true,
          },
        },
        {
          field: 'Hobbies',
          label: '兴趣爱好',
          component: 'Input',
          colProps: {
            span: 12,
          },
        },
        {
          field: 'Phone',
          label: '电话',
          component: 'Input',
          colProps: {
            span: 12,
          },
        },
        {
          field: 'MSN',
          label: '短号',
          component: 'Input',
          colProps: {
            span: 12,
          },
        },

        {
          label: '地址',
          field: 'Address',
          component: 'InputTextArea',
          colProps: { span: 24 },
        },
      ];
      const [registerForm, { resetFields, updateSchema, setFieldsValue, validate }] = useForm({
        labelWidth: 90,
        schemas: accountFormSchema,
        showActionButtonGroup: false,
      });

      async function reloadUserInfo(uName) {
        imageUrl.value = '';
        await getUpeditUser({ userName: uName }).then(async (res) => {
          dataRef = res.user;
          dataRef['type'] = res.userTypeOpts?.type;
          dataRef['deptName'] = res.deptName;
          dataRef['leaderName'] = res.leaderNames;
          dataRef['leaderCode'] = res.leaders;
          dataRef['roleName'] = res.roleName;
          dataRef['Email'] = dataRef.email;
          dataRef['qq'] = dataRef.qq;
          dataRef['gender'] = dataRef.gender ? '1' : '0';
          dataRef['Marriage'] = dataRef.isMarriaged ? '1' : '0';
          dataRef['IDCard'] = dataRef.iDCard;
          dataRef['Hobbies'] = dataRef.hobbies;
          dataRef['Phone'] = dataRef.phone;
          dataRef['MSN'] = dataRef.msn;
          dataRef['Address'] = dataRef.address;

          if (dataRef['photo']) {
            await getShowImg({ path: dataRef['photo'] }).then(async (res) => {
              let data = res;
              imageUrl.value = bufToUrl(data);
              dataRef['photo'] = res;
            });
          }
          setFieldsValue({
            ...dataRef,
          });
        });
      }

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        resetFields();
        fileList.value = [];
        imageUrl.value = '';
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        dataRef = data.record;
        if (unref(isUpdate)) {
          reloadUserInfo(dataRef?.name);
        }
        // updateSchema([
        //   {
        //     field: 'name',

        //     rules: [
        //       {
        //         required: dataRef['id'] ? false : true,
        //       },
        //       {
        //         validator(_, value) {
        //           return new Promise((resolve, reject) => {
        //             if (!dataRef['id']) {
        //               getUserCheckUserName({ userName: value })
        //                 .then((res) => {
        //                   console.log('resssss', res);
        //                   if (res == 0) {
        //                     resolve();
        //                   } else {
        //                     if (value) {
        //                       reject('已存在');
        //                     } else {
        //                       reject();
        //                     }
        //                   }
        //                 })
        //                 .catch((err) => {
        //                   reject(err.msg || '验证失败');
        //                 });
        //             } else {
        //               resolve();
        //             }
        //           });
        //         },
        //         trigger: 'blur',
        //       },
        //     ],
        //     componentProps: {
        //       disabled: dataRef['id'] ? true : false,
        //     },
        //   },
        //   {
        //     field: 'mobile',
        //     rules: [
        //       {
        //         required: true,
        //       },
        //       {
        //         validator(_, value) {
        //           return new Promise((resolve, reject) => {
        //             getUserCheckMobile({ mobile: value, name: dataRef.name ? dataRef.name : '' })
        //               .then((res) => {
        //                 console.log('res===>', res);
        //                 if (res == 0) {
        //                   resolve();
        //                 } else {
        //                   if (value) {
        //                     reject('已存在');
        //                   } else {
        //                     reject();
        //                   }
        //                 }
        //               })
        //               .catch((err) => {
        //                 reject(err.msg || '验证失败');
        //               });
        //           });
        //         },
        //         trigger: 'blur',
        //       },
        //     ],
        //   },
        //   {
        //     field: 'personNo',
        //     rules: [
        //       {
        //         required: true,
        //       },
        //       {
        //         validator(_, value) {
        //           return new Promise((resolve, reject) => {
        //             getUserCheckPersonNo({
        //               personNo: value,
        //               name: dataRef.name ? dataRef.name : '',
        //             })
        //               .then((res) => {
        //                 if (res == 0) {
        //                   resolve();
        //                 } else {
        //                   if (value) {
        //                     reject('已存在');
        //                   } else {
        //                     reject();
        //                   }
        //                 }
        //               })
        //               .catch((err) => {
        //                 reject(err.msg || '验证失败');
        //               });
        //           });
        //         },
        //         trigger: 'blur',
        //       },
        //     ],
        //   },
        // ]);
      });

      const getTitle = '个人信息';

      async function handleSubmit() {
        try {
          const values = await validate();
          setDrawerProps({ confirmLoading: true });
          let formData = Object.assign({}, values);
          // TODO custom api
          formData.deptCode =
            formData.deptCode && Array.isArray(formData.deptCode)
              ? formData.deptCode.join(',')
              : '';
          console.log('handleSubmit formData.gender', formData.gender);
          formData['gender'] = formData.gender == '1';
          formData['isMarriaged'] = formData.Marriage == '1';
          formData['isPass'] = '1'; //默认值
          formData['isValid'] = '1'; //默认值启用
          formData['leaderCode'] = dataRef['leaderCode'] ? dataRef['leaderCode'] : '';
          formData['photo'] = dataRef['photo'] ? dataRef['photo'] : new Blob();
          console.log('formData', formData);
          console.log('isUpdate', isUpdate.value);
          formData['id'] = dataRef.id;

          console.log('handleSubmit formData', formData);

          let form = new FormData();
          for (let v in formData) {
            // 将null转为''
            form.append(v, formData[v] != null ? formData[v] : '');
          }
          await getUpdateMyInfo(form);
          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      // 组件上传
      const handleChange = (info: UploadChangeParam) => {
        dataRef['photo'] = info.file.originFileObj;
        getBase64(info.file.originFileObj, (base64Url: string) => {
          imageUrl.value = base64Url;
          loading.value = false;
        });
      };

      function handleCustomRequest(info) {
        console.log('handleCustomRequest', info);
      }

      const beforeUpload = (file: UploadProps['fileList'][number]) => {
        console.log('beforeUpload', file);
        const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
        if (!isJpgOrPng) {
          message.error('只能上传jpg和png图片');
        }
        const isLt2M = file.size / 1024 / 1024 < 2;
        if (!isLt2M) {
          message.error('图片大小不能超过2MB');
        }
        if (isJpgOrPng) {
        } else {
          // dataRef['photo'] = '';
        }
        console.log("dataRef['photo']", dataRef['photo']);
        return isJpgOrPng && isLt2M;
      };

      const [registerModal, { openModal }] = useModal();
      function handleCreate() {
        openModal(true, {
          isUpdate: false,
          type: 'user',
        });
      }

      function handleEdit(record) {
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      function handleSuccess(data) {
        if (data && data.length > 0) {
          dataRef['leaderCode'] = data[0].name;
          dataRef['leaderName'] = data[0].realName;
          setFieldsValue({
            leaderName: dataRef['leaderName'],
          });
        }
      }

      async function resetPortrait() {
        createConfirm({
          iconType: 'info',
          title: () => h('span', t('common.prompt')),
          content: () => h('span', '您确定要重置头像么'),
          onOk: async () => {
            await getResetPortrait({ userName: dataRef?.name }).then((res) => {
              if (res.code == 200) {
                createMessage.success(t('common.opSuccess'));
                reloadUserInfo(dataRef?.name);
              }
            });
          },
        });
      }

      return {
        registerDrawer,
        registerForm,
        getTitle,
        handleSubmit,
        fileList,
        loading,
        imageUrl,
        handleChange,
        beforeUpload,
        registerModal,
        handleCreate,
        handleEdit,
        handleSuccess,
        handleCustomRequest,
        getShowImg,
        resetPortrait,
      };
    },
  });
</script>
<style scoped>
  .avatar-uploader > .ant-upload {
    width: 128px;
    height: 128px;
  }
  .ant-upload-select-picture-card i {
    font-size: 32px;
    color: #999;
  }

  .ant-upload-select-picture-card .ant-upload-text {
    margin-top: 8px;
    color: #666;
  }

  .reset-portrait {
    display: block;
  }
</style>
