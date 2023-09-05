<template>
  <BasicModal :footer="null" title="修改密码" v-bind="$attrs" @register="register">
    <div class="w-2/3 mx-auto mt-10 border-gray-300">
      <BasicForm @register="registerForm" />
      <div class="flex justify-center">
        <a-button type="primary" class="ml-2" @click="customSubmitFunc">确定</a-button>
        <a-button type="primary" class="ml-2" @click="handleCancel">取消</a-button>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { BasicForm, useForm } from '/@/components/Form';
  import { defineComponent } from 'vue';
  import { FormSchema } from '/@/components/Form';
  import { getChangePwd } from '/@/api/system/system';
  import { useUserStore } from '/@/store/modules/user';
  import { BasicModal, useModalInner } from '/@/components/Modal/index';

  export default defineComponent({
    name: 'ChangePwdModal',
    components: { BasicForm, BasicModal },
    setup() {
      const [register, { closeModal }] = useModalInner(async () => {});

      const schemas: FormSchema[] = [
        {
          field: 'pwd3',
          component: 'InputPassword',
          label: '旧密码',
          colProps: { span: 24 },
          required: true,
          componentProps: {
            placeholder: '请输入旧密码',
          },
        },
        {
          field: 'pwd',
          component: 'InputPassword',
          label: '新密码',
          colProps: { span: 24 },
          required: true,
          componentProps: {
            placeholder: '请输入新密码',
          },
        },
        {
          field: 'pwd2',
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
                    reject('与新密码不一致');
                  }
                });
              },
              trigger: 'change',
            },
          ],
        },
      ];
      const [registerForm, { validate, setProps, getFieldsValue }] = useForm({
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
          await getChangePwd(formData);
          setProps({
            submitButtonOptions: {
              loading: false,
            },
          });
          userStore.logout(true);
        } catch (error) {}
      }

      function handleCancel() {
        closeModal();
      }
      return { register, customSubmitFunc, handleCancel, registerForm };
    },
  });
</script>
<style lang="less" scoped></style>
