<template>
  <BasicModal
    :footer="null"
    title="请输入登录密码"
    v-bind="$attrs"
    :class="prefixCls"
    :bodyStyle="{ height: '170px' }"
    @register="register"
  >
    <div class="h-150px flex flex-col justify-center">
      <BasicForm @register="registerForm" />
      <div :class="`${prefixCls}__footer`">
        <a-button type="primary" size="middle" @click="handleOk"> 确定 </a-button>
        <a-button type="primary" size="middle" class="ml-2" @click="handleCancel"> 取消 </a-button>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicModal, useModalInner } from '/@/components/Modal/index';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { getCheckPwd } from '/@/api/system/system';
  const { createMessage } = useMessage();

  export default defineComponent({
    name: 'SelTemplateModal',
    components: { BasicModal, BasicForm },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { prefixCls } = useDesign('header-lock-modal');

      const attRecord = ref({});
      const [register, { closeModal }] = useModalInner(async (data) => {
        attRecord.value = data;
      });

      const [registerForm, { validateFields }] = useForm({
        showActionButtonGroup: false,
        schemas: [
          {
            field: 'pwd',
            component: 'InputPassword',
            label: '密码',
            colProps: {
              span: 12,
            },
            required: true,
            componentProps: {
              placeholder: '请输入登录密码',
            },
          },
        ],
      });

      async function handleOk() {
        const values = (await validateFields()) as any;

        // 检查密码是否正确
        const data = await getCheckPwd({ ...values });
        if (data.res != 0) {
          createMessage.warn('密码输入错误');
          return;
        }

        closeModal();
        console.log('handleOk data', data);
        emit('success', data);
      }

      async function handleCancel() {
        closeModal();
      }

      return {
        t,
        prefixCls,
        register,
        registerForm,
        handleOk,
        handleCancel,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-input-pwd-modal';

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
    }
  }
</style>
