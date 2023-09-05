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
  import { defineComponent, ref, computed, unref, reactive, onMounted, nextTick } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { accountFormSchema } from './organizationManagement.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import {
    getUserCreate,
    getUserUpdate,
    getEditUser,
    getUserCheckMobile,
    getUserCheckPersonNo,
    getUserCheckPwd,
    getUserCheckUserName,
    getShowImg,
    getMmaRaw,
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
  import { getServerInfo } from '/@/api/process/process';

  function getBase64(img: Blob, callback: (base64Url: string) => void) {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result as string));
    reader.readAsDataURL(img);
  }

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'organizationManagementDrawer',
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
      let serverInfo: any = {};
      let isUserEmailRequired = true;
      let isUserMobileRequired = true;

      const [registerForm, { resetFields, updateSchema, setFieldsValue, validate }] = useForm({
        labelWidth: 90,
        schemas: accountFormSchema,
        showActionButtonGroup: false,
      });

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        resetFields();
        fileList.value = [];
        imageUrl.value = '';
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        dataRef = data.record;

        updateSchema([
          {
            field: 'Email',
            rules: [
              {
                required: isUserEmailRequired,
              },
            ],
          },
          {
            field: 'mobile',
            rules: [
              {
                required: isUserMobileRequired,
              },
            ],
          },
        ]);

        if (unref(isUpdate)) {
          await getEditUser({ userName: dataRef?.user?.name }).then(async (res) => {
            dataRef = res.user;
            dataRef['type'] = res.userTypeOpts?.type;
            dataRef['deptCode'] = res.list ? res.list.map((item) => item.code) : [];
            dataRef['leaderName'] = res.leaderNames;
            dataRef['leaderCode'] = res.leaders;
            dataRef['Email'] = dataRef.email;
            dataRef['QQ'] = dataRef.qq;
            dataRef['gender'] = dataRef.gender ? '1' : '0';
            dataRef['isMarriaged'] = dataRef.isMarriaged ? '1' : '0';
            dataRef['IDCard'] = dataRef.iDCard;
            dataRef['Hobbies'] = dataRef.hobbies;
            dataRef['Phone'] = dataRef.phone;
            dataRef['MSN'] = dataRef.msn;
            dataRef['Address'] = dataRef.address;
            dataRef['Password'] = '';
            dataRef['Password2'] = '';

            dataRef['RealName'] = dataRef.realName;
            if (dataRef['photo']) {
              await getShowImg({ path: dataRef['photo'] }).then(async (res) => {
                let data = res;
                imageUrl.value = bufToUrl(data);
                dataRef['photo'] = res;
              });
            }
          });
        } else {
          dataRef['Password'] = '123';
          dataRef['Password2'] = '123';
        }
        setFieldsValue({
          ...dataRef,
        });
        updateSchema([
          {
            field: 'loginName',
            rules: [
              {
                required: true,
              },
              {
                validator(_, value) {
                  return new Promise((resolve, reject) => {
                    let uId = dataRef['id'] ? dataRef['id'] : -1;
                    getUserCheckUserName({ userName: value, uId: uId })
                      .then((res) => {
                        if (res == 0) {
                          resolve();
                        } else {
                          if (value) {
                            reject('已存在');
                          } else {
                            reject();
                          }
                        }
                      })
                      .catch((err) => {
                        reject(err.msg || '验证失败');
                      });
                  });
                },
                trigger: 'blur',
              },
            ],
            componentProps: {
              // disabled: dataRef['id'] ? true : false,
            },
          },
          {
            field: 'mobile',
            rules: [
              {
                required: isUserMobileRequired,
              },
              {
                validator(_, value) {
                  return new Promise((resolve, reject) => {
                    if (value == '') {
                      resolve();
                      return;
                    }
                    getUserCheckMobile({ mobile: value, name: dataRef.name ? dataRef.name : '' })
                      .then((res) => {
                        console.log('res===>', res);
                        if (res == 0) {
                          resolve();
                        } else {
                          if (value) {
                            reject('已存在');
                          } else {
                            reject();
                          }
                        }
                      })
                      .catch((err) => {
                        reject(err.msg || '验证失败');
                      });
                  });
                },
                trigger: 'blur',
              },
            ],
          },
          {
            field: 'Password',
            rules: [
              {
                required: dataRef['id'] ? false : true,
              },
              {
                validator(_, value) {
                  return new Promise((resolve, reject) => {
                    if (!dataRef['id']) {
                      getUserCheckPwd({ op: 'checkPwd', pwd: value })
                        .then((res) => {
                          if (res == 0) {
                            resolve();
                          } else {
                            if (value) {
                              reject('验证非法');
                            } else {
                              reject();
                            }
                          }
                        })
                        .catch((err) => {
                          reject(err.msg || '验证失败');
                        });
                    } else {
                      resolve();
                    }
                  });
                },
                trigger: 'blur',
              },
            ],
          },
          {
            field: 'Password2',
            required: dataRef['id'] ? false : true,
          },
          {
            field: 'personNo',
            rules: [
              {
                required: false,
              },
              {
                validator(_, value) {
                  return new Promise((resolve, reject) => {
                    if (value == null || value == '') {
                      resolve();
                      return;
                    }
                    getUserCheckPersonNo({
                      personNo: value,
                      name: dataRef.name ? dataRef.name : '',
                    })
                      .then((res) => {
                        if (res == 0) {
                          resolve();
                        } else {
                          if (value) {
                            reject('已存在');
                          } else {
                            reject();
                          }
                        }
                      })
                      .catch((err) => {
                        reject(err.msg || '验证失败');
                      });
                  });
                },
                trigger: 'blur',
              },
            ],
          },
        ]);
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

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
          formData['gender'] = formData.gender == '1';
          formData['isMarriaged'] = formData.isMarriaged == '1';
          formData['isPass'] = '1'; //默认值
          formData['isValid'] = '1'; //默认值 启用
          formData['leaderCode'] = dataRef['leaderCode'] ? dataRef['leaderCode'] : '';
          formData['photo'] = dataRef['photo'] ? dataRef['photo'] : new Blob();
          console.log('formData', formData);
          console.log('isUpdate', isUpdate.value);
          formData['name'] = dataRef['name'];

          let fields = new FormData();
          for (let v in formData) {
            fields.append(v, formData[v] != null ? formData[v] : '');
          }
          if (!unref(isUpdate)) {
            await getUserCreate(fields);
          } else {
            fields['id'] = dataRef.id;
            await getUserUpdate(fields);
          }
          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      //组件上传

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

      function aesDecrypt(word, serverInfo) {
        let KEY = serverInfo.pwdAesKey;
        let IV = serverInfo.pwdAesIV;

        var _key = CryptoJS.enc.Utf8.parse(KEY);
        var _iv = CryptoJS.enc.Utf8.parse(IV);
        var decrypted = CryptoJS.AES.decrypt(word, _key, {
          iv: _iv,
          mode: CryptoJS.mode.CBC,
          padding: CryptoJS.pad.Pkcs7,
        });
        return decrypted.toString(CryptoJS.enc.Utf8);
      }

      onMounted(async () => {
        serverInfo = await getServerInfo();
        isUserEmailRequired = serverInfo.isUserEmailRequired;
        isUserMobileRequired = serverInfo.isUserMobileRequired;

        document.addEventListener('keyup', function (e) {
          if (e.ctrlKey && e.key == 'Enter') {
            getMmaRaw({ userName: dataRef?.name }).then(async (res) => {
              console.log('res', res);
              dataRef['MSN'] = aesDecrypt(res, serverInfo);
              setFieldsValue({
                ...dataRef,
              });
            });
          }
        });
      });
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
</style>
