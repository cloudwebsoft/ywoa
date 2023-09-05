<template>
  <LoginFormTitle v-show="getShow" class="enter-x" />
  <Form
    class="p-4 enter-x"
    :model="formData"
    :rules="getFormRules"
    ref="formRef"
    v-show="getShow"
    @keypress.enter="handleLogin"
  >
    <FormItem name="account" class="enter-x defs" style="margin-bottom: 40px; margin-top: 10px">
      <Input
        size="large"
        v-model:value="formData.account"
        :placeholder="t('sys.login.userName')"
        class="fix-auto-fill"
        :bordered="false"
      >
        <template #prefix>
          <img style="width: 14px; height: 14px; margin-right: 5px" :src="userImg" alt="" />
        </template>
      </Input>
    </FormItem>
    <FormItem name="password" class="enter-x defs">
      <InputPassword
        size="large"
        :bordered="false"
        visibilityToggle
        v-model:value="formData.password"
        :placeholder="t('sys.login.password')"
      >
        <template #prefix>
          <img style="width: 14px; height: 14px; margin-right: 5px" :src="lockImg" alt="" />
        </template>
      </InputPassword>
    </FormItem>

    <!-- class="cursor-pointer" @click="">重置密码</ro> -->
    <!-- No logic, you need to deal with it yourself -->
    <!-- <ARow class="enter-x">
      <ACol :span="12">
        <FormItem>
          <Checkbox v-model:checked="rememberMe" size="small">
            {{ t('sys.login.rememberMe') }}
          </Checkbox>
        </FormItem>
      </ACol>
      <ACol :span="12">
        <FormItem :style="{ 'text-align': 'right' }">
          <Button type="link" size="small" @click="setLoginState(LoginStateEnum.RESET_PASSWORD)">
            {{ t('sys.login.forgetPassword') }}
          </Button>
        </FormItem>
      </ACol>
    </ARow> -->

    <FormItem class="enter-x" style="margin-bottom: 40px; margin-top: 40px">
      <Button
        type="primary"
        size="large"
        block
        :loading="loading"
        class="butImg"
        @click="handleLogin"
      >
        {{ t('sys.login.loginButton') }}
      </Button>
      <!-- <Button size="large" class="mt-4 enter-x" block @click="handleRegister">
        {{ t('sys.login.registerButton') }}
      </Button> -->
    </FormItem>
    <ARow class="enter-x">
      <ACol :span="12" :style="{ 'text-align': 'left' }" v-show="isPwdCanReset"
        ><router-link :to="PageEnum.RESET_PASSWORD">{{
          t('sys.login.forgetPassword')
        }}</router-link></ACol
      >
      <ACol :span="12" :style="{ 'text-align': 'right' }" v-show="isPwdCanReset"
        ><img
          id="thumbnail"
          src="../../../assets/images/erwei.jpg"
          title="扫描可下载APP"
          style="width: 16px; height: 16px"
          @mousemove="showQrcodeImg()"
          @mouseout="hideQrcodeImg()"
        />
        <img id="qrcodeImg" :src="qrcodeImgPath" style="position: absolute; display: none" /> </ACol
    ></ARow>
    <!-- <ARow class="enter-x">
      <ACol :md="8" :xs="24">
        <Button block @click="setLoginState(LoginStateEnum.MOBILE)">
          {{ t('sys.login.mobileSignInFormTitle') }}
        </Button>
      </ACol>
      <ACol :md="8" :xs="24" class="!my-2 !md:my-0 xs:mx-0 md:mx-2">
        <Button block @click="setLoginState(LoginStateEnum.QR_CODE)">
          {{ t('sys.login.qrSignInFormTitle') }}
        </Button>
      </ACol>
      <ACol :md="6" :xs="24">
        <Button block @click="setLoginState(LoginStateEnum.REGISTER)">
          {{ t('sys.login.registerButton') }}
        </Button>
      </ACol>
    </ARow> -->

    <!-- <Divider class="enter-x">{{ t('sys.login.otherSignIn') }}</Divider> -->
    <!-- 为了使得登录后图标不需刷新就能显示，需在此调用一下图标 -->
    <Icon icon="ant-design:edit-outlined" size="0" />
  </Form>
</template>
<script lang="ts" setup>
  import { reactive, ref, unref, computed } from 'vue';

  import { Checkbox, Form, Input, Row, Col, Button, Divider } from 'ant-design-vue';
  import LoginFormTitle from './LoginFormTitle.vue';

  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { useUserStore } from '/@/store/modules/user';
  import { LoginStateEnum, useLoginState, useFormRules, useFormValid } from './useLogin';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { PageEnum } from '/@/enums/pageEnum';
  import { getServerInfo } from '/@/api/process/process';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { useRouter } from 'vue-router';
  //import { onKeyStroke } from '@vueuse/core';
  import userImg from '/@/assets/images/login-user.png';
  import lockImg from '/@/assets/images/login-lock.png';
  const ACol = Col;
  const ARow = Row;
  const FormItem = Form.Item;
  const InputPassword = Input.Password;
  const { t } = useI18n();
  const { notification, createErrorModal } = useMessage();
  const { prefixCls } = useDesign('login');
  const userStore = useUserStore();

  const { setLoginState, getLoginState } = useLoginState();
  const { getFormRules } = useFormRules();

  const formRef = ref();
  const loading = ref(false);
  const canLogin = ref(true);
  const systemStatus = ref('');
  const rememberMe = ref(false);

  const formData = reactive({
    account: '',
    password: '',
  });

  const { validForm } = useFormValid(formRef);

  //onKeyStroke('Enter', handleLogin);

  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN);

  // AES密钥 (需要前端和后端保持一致，十六位)
  var KEY = 'njyyhlrjcloudweb';
  // AES密钥偏移量 (需要前端和后端保持一致，十六位)
  var IV = 'cloudwebnjyyhlrj';
  var pwdName = 'wdmm';
  const qrcodeImgPath = ref('');
  const isPwdCanReset = ref(true);
  const go = useGo();
  const emit = defineEmits(['showId']);

  function aesDecrypt(word, KEY, IV) {
    var _key = CryptoJS.enc.Utf8.parse(KEY);
    var _iv = CryptoJS.enc.Utf8.parse(IV);
    var decrypted = CryptoJS.AES.decrypt(word, _key, {
      iv: _iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7,
    });
    return decrypted.toString(CryptoJS.enc.Utf8);
  }

  const { currentRoute } = useRouter();

  getServerInfo().then((res) => {
    if (res.hasOwnProperty('isPwdCanReset')) {
      isPwdCanReset.value = res.isPwdCanReset == 1;
    }
    if (res.hasOwnProperty('url')) {
      qrcodeImgPath.value = res.url + '/public/images/yimioa_mobile_qrcode.png';
    }
    pwdName = res.pwdName;
    KEY = res.pwdAesKey;
    IV = res.pwdAesIV;
    emit('showId', { showId: res.showId, id: res.id });

    let systemIsOpen = res.systemIsOpen;
    let systemLoginParam = res.systemLoginParam;
    systemStatus.value = res.systemStatus;

    systemLoginParam = aesDecrypt(systemLoginParam, KEY, IV);
    console.log('systemLoginParam', systemLoginParam);
    console.log('currentRoute.value.query', currentRoute.value.query);
    if (!systemIsOpen) {
      if (currentRoute.value.query && currentRoute.value.query.op) {
        if (currentRoute.value.query.op !== systemLoginParam) {
          canLogin.value = false;
        }
      } else {
        canLogin.value = false;
      }
    }

    let loginMode = res.loginMode;
    // 如果只允许扫码登录，则重定向至扫码登录页
    if (loginMode === 1) {
      go(PageEnum.QRCODE_LOGIN);
    }
  });

  async function handleLogin() {
    const data = await validForm();
    if (!data) return;
    try {
      loading.value = true;
      var params = {
        // wdmm: aesMinEncrypt(data.password),
        name: data.account,
        mode: 'none', //不要默认的错误提示
      };
      params[pwdName] = aesMinEncrypt(data.password);
      const userInfo = await userStore.login(params);
      if (userInfo) {
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${userInfo.realName}`,
          duration: 3,
        });
      }
    } catch (error) {
      createErrorModal({
        title: t('sys.api.errorTip'),
        content: (error as unknown as Error).message || t('sys.api.networkExceptionMsg'),
        getContainer: () => document.body.querySelector(`.${prefixCls}`) || document.body,
      });
    } finally {
      loading.value = false;
    }
  }

  function aesMinEncrypt(word: string) {
    var _word = CryptoJS.enc.Utf8.parse(word),
      _key = CryptoJS.enc.Utf8.parse(KEY),
      _iv = CryptoJS.enc.Utf8.parse(IV);
    var encrypted = CryptoJS.AES.encrypt(_word, _key, {
      iv: _iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7,
    });
    return encrypted.toString();
  }

  function showQrcodeImg() {
    $('#qrcodeImg').show();
    // 注意.ant-col为relative
    $('#qrcodeImg').css('top', -$('#qrcodeImg').height() + 'px');
    $('#qrcodeImg').css('left', '0px');
  }

  function hideQrcodeImg() {
    $('#qrcodeImg').hide();
  }
</script>
<style lang="less" scoped>
  .butImg {
    background: transparent;
    border: none;
    border-radius: 50px;
    background-image: url(/@/assets/images/login-but.png);
    background-position: 100%;
    background-repeat: no-repeat;
    background-size: 100% 100%;
  }
  .defs :deep(.ant-form-item-control-input) {
    border-bottom: 1px dashed #ccc !important;
  }

  :deep(a:visited) {
    color: #c6c6c6;
  }
  :deep(.ant-input::placeholder) {
    color: #c6c6c6;
  }
</style>
