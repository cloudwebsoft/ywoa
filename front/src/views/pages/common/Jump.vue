<template>
  <PageWrapper v-loading="loadingRef" title="" contentBackground content="" contentClass="p-4">
    <div class="w-1/3 mx-auto mt-25 border-gray-300" v-show="false">
      <Card>
        <template #title>
          <div class="w-full text-center"> 跳转页 </div>
        </template>
        <div class="flex justify-center" :style="{ color: 'red' }" v-if="errMsg.length > 0">{{
          errMsg
        }}</div>
        <div class="flex justify-center">
          <a-button type="primary" class="ml-2" @click="setBack">登录</a-button>
        </div>
      </Card>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { useUserStore } from '/@/store/modules/user';
  import { Card } from 'ant-design-vue';
  import { getJump } from '/@/api/system/system';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useGo } from '/@/hooks/web/usePage';
  import { useRouter } from 'vue-router';
  import { router } from '/@/router';

  export default defineComponent({
    name: 'Jump',
    components: { PageWrapper, Card },
    setup() {
      const { createMessage } = useMessage();
      const userStore = useUserStore();
      const setBack = () => {
        userStore.logout(true);
      };

      const go = useGo();
      const errMsg = ref('');
      const { currentRoute } = useRouter();
      const loadingRef = ref(true);

      onMounted(() => {
        let query = currentRoute.value.query;
        let op = query.op;
        let action = query.action;
        console.log('op', op);

        getJump({ op, action }).then((data) => {
          if (data.token) {
            // 跳转
            loadingRef.value = false;

            userStore.setToken(data.token);

            userStore.afterLoginAction(false).then(() => {
              if (op === 'flowProcess') {
                // 会出现两个首页选项卡
                // go(
                //   {
                //     path: PageEnum.BASE_HOME,
                //     query: { op: op, myActionId: data.myActionId },
                //   },
                //   true,
                // );
                // 只有一个首页选项卡，但未传参
                // router.replace(PageEnum.BASE_HOME);
                // 会出现两个首页选项卡
                // router.replace(PageEnum.BASE_HOME + '?op=' + op + '&myActionId=' + data.myActionId);
                // 也会出现两个首页选项卡
                // router.replace({
                //   path: PageEnum.BASE_HOME,
                //   query: { op: op, myActionId: data.myActionId },
                // });
                go({
                  path: '/processHandle',
                  query: {
                    myActionId: data.myActionId,
                    isTab: true,
                  },
                });
              } else if (op == 'flowShow') {
                go({
                  path: '/processShow',
                  query: {
                    flowId: data.flowId,
                  },
                });
              } else if (op == 'resetPwd') {
                // 不用go，因为query传参可以修改url，会使修改密码界面上的“进入系统”按钮显示
                // go({
                //   path: '/changePassword',
                //   query: { isResetPwd: true },
                // });
                router.push({ name: 'changePassword', params: { isResetPwd: 'true' } });
              }
            });
          }
        });
      });

      return { setBack, errMsg, loadingRef };
    },
  });
</script>
<style lang="less" scoped></style>
