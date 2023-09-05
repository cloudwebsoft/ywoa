<template>
  <PageWrapper title="" contentBackground content="" contentClass="p-4">
    <div class="w-1/3 mx-auto mt-25 border-gray-300">
      <Card>
        <template #title>
          <div class="w-full text-center"> 重置密码 </div>
        </template>
        <BasicForm @register="register">
          <!-- <template #advanceAfter>
          </template> -->
        </BasicForm>
        <div class="flex justify-center">
          <a-button @click="resetFields">重置</a-button>
          <a-button type="primary" class="ml-2" @click="customSubmitFunc">确定</a-button>
          <a-button type="primary" class="ml-2" @click="setBack">返回</a-button>
        </div>
      </Card>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { BasicForm, useForm } from '/@/components/Form';
  import { defineComponent } from 'vue';
  import { FormSchema } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';

  import { getResetPwdSendLink } from '/@/api/system/system';

  import { useUserStore } from '/@/store/modules/user';
  import { Card } from 'ant-design-vue';
  export default defineComponent({
    name: 'FormBasicPage',
    components: { BasicForm, PageWrapper, Card },
    setup() {
      const { createMessage } = useMessage();
      const schemas: FormSchema[] = [
        {
          field: 'userName',
          component: 'Input',
          label: '用户名',
          colProps: { span: 24 },
          required: true,
          componentProps: {
            placeholder: '请输入用户名',
          },
          required: true,
        },
        {
          field: 'email',
          component: 'Input',
          label: '邮箱',
          colProps: { span: 24 },
          required: true,
          componentProps: {
            placeholder: '请输入邮箱',
          },
          rules: [
            {
              required: true,
              type: 'email',
            },
          ],
        },
      ];
      const [register, { validate, setProps, resetFields }] = useForm({
        labelCol: {
          span: 6,
        },
        wrapperCol: {
          span: 15,
        },
        schemas: schemas,
        actionColOptions: {
          offset: 4,
          span: 24,
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
          const data = await getResetPwdSendLink(formData);
          if (data.code != 200) {
            createMessage.error(data.msg);
            setProps({
              submitButtonOptions: {
                loading: false,
              },
            });
            return;
          } else {
            createMessage.success(data.msg);
          }
          setProps({
            submitButtonOptions: {
              loading: false,
            },
          });
          setBack();
        } catch (error) {}
      }

      const setBack = () => {
        userStore.logout(true);
      };

      return { register, resetFields, customSubmitFunc, setBack };
    },
  });
</script>
<style lang="less" scoped>
  .form-wrap {
    padding: 24px;
    background-color: @component-background;
  }
</style>
