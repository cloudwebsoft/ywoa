<template>
  <PageWrapper title="" contentBackground content="" contentClass="p-4">
    <div class="w-1/3 mx-auto mt-25 border-gray-300">
      <Card>
        <template #title>
          <div class="w-full text-center"> 扫码登录 </div>
        </template>
        <div id="qrCodeBox" class="flex justify-center"> </div>
        <div class="flex justify-center" :style="{ color: 'red' }" v-if="errMsg.length > 0">{{
          errMsg
        }}</div>
        <div class="flex justify-center" v-if="loginMode === 2">
          <a-button type="primary" class="ml-2" @click="setBack">账号登录</a-button>
        </div>
      </Card>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { useUserStore } from '/@/store/modules/user';
  import { Card } from 'ant-design-vue';
  import { getQrCodeForLogin, getQrCodeLoginCheck } from '/@/api/system/system';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useGo } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'QrcodeLogin',
    components: { PageWrapper, Card },
    setup() {
      const { createMessage } = useMessage();
      const userStore = useUserStore();
      const setBack = () => {
        userStore.logout(true);
      };

      const loginMode = ref(1);
      const go = useGo();
      const errMsg = ref('');

      getQrCodeForLogin().then((data) => {
        $('#qrCodeBox').html('<img src="' + data.QRCodeImg + '"/>');
        let uuid = data.uuid;
        loginMode.value = data.loginMode;
        // 如果只允许帐户密码登录，则转至登录页
        if (loginMode.value === 0) {
          setBack();
          return;
        }
        check(uuid);
      });

      function check(uuid) {
        let params = {
          uuid: uuid,
        };
        getQrCodeLoginCheck(params)
          .then((data) => {
            userStore.setServerUrl(data.serverInfo.url);
            userStore.setServerInfo(data.serverInfo);
            userStore.setToken(data.Authorization);
            go(PageEnum.BASE_HOME);
          })
          .catch((e) => {
            console.log(e.message);
            errMsg.value = e.message;
            // 退回登录页
            // setBack();
          });
      }

      return { setBack, loginMode, errMsg };
    },
  });
</script>
<style lang="less" scoped>
  #qrCodeBox img {
    width: 150px;
    height: 150px;
  }
</style>
