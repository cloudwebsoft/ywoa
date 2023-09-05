<template>
  <PageWrapper title="" contentBackground content="" contentClass="p-4">
    <div class="w-1/3 mx-auto mt-25 border-gray-300">
      <Card>
        <template #title>
          <div class="w-full text-center"> 修改密码 </div>
        </template>
        <BasicForm @register="register" />
        <div class="flex justify-center">
          <a-button @click="resetFields">重置</a-button>
          <a-button type="primary" class="ml-2" @click="customSubmitFunc">确定</a-button>
          <!-- <a-button type="primary" class="ml-2" @click="goHome" v-if="!isResetPwd"
            >进入系统</a-button
          > -->
        </div>
      </Card>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { BasicForm, useForm } from '/@/components/Form';
  import { defineComponent, computed } from 'vue';
  import { FormSchema } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { useGo } from '/@/hooks/web/usePage';
  import { PageEnum } from '/@/enums/pageEnum';
  import { Card } from 'ant-design-vue';
  import { useRouter } from 'vue-router';

  import { getUpdateInitPwd } from '/@/api/system/system';

  import { useUserStore } from '/@/store/modules/user';
  export default defineComponent({
    name: 'FormBasicPage',
    components: { BasicForm, PageWrapper, Card },
    setup() {
      const { createMessage } = useMessage();
      const go = useGo();
      const { currentRoute } = useRouter();

      console.log('currentRoute', currentRoute.value);
      const isResetPwd = computed(() => currentRoute.value.params.isResetPwd == 'true');

      const schemas: FormSchema[] = [
        {
          field: 'pwd',
          component: 'InputPassword',
          label: '密码',
          colProps: { span: 24 },
          required: true,
          componentProps: {
            placeholder: '请输入密码',
          },
          required: true,
        },
        {
          field: 'confirmPwd',
          component: 'InputPassword',
          label: '确认密码',
          colProps: { span: 24 },
          required: true,
          componentProps: {
            placeholder: '请输入确认密码',
          },
          rules: [
            {
              required: true,
            },
            {
              validator(_, value) {
                return new Promise((resolve, reject) => {
                  console.log('record==>', getFieldsValue());
                  const pwd = getFieldsValue().pwd;
                  if (pwd === value) {
                    resolve();
                  } else {
                    reject('与密码不一致');
                  }
                });
              },
              trigger: 'change',
            },
          ],
        },
      ];
      const [register, { validate, setProps, getFieldsValue, resetFields }] = useForm({
        labelCol: {
          span: 8,
        },
        wrapperCol: {
          span: 15,
        },
        schemas: schemas,
        actionColOptions: {
          offset: 8,
          span: 15,
        },
        submitButtonOptions: {
          text: '确定',
        },
        showActionButtonGroup: false,
        submitFunc: customSubmitFunc,
      });

      const userStore = useUserStore();
      async function customSubmitFunc() {
        try {
          const formData = await validate();
          setProps({
            submitButtonOptions: {
              loading: true,
            },
          });
          await getUpdateInitPwd(formData);
          setProps({
            submitButtonOptions: {
              loading: false,
            },
          });
          userStore.logout(true);
        } catch (error) {}
      }

      function goHome() {
        go(userStore.getUserInfo.homePath || PageEnum.BASE_HOME);
      }
      return { register, resetFields, customSubmitFunc, goHome, isResetPwd };
    },
  });
</script>
<style lang="less" scoped>
  .form-wrap {
    padding: 24px;
    background-color: @component-background;
  }
</style>
