<template>
  <BasicModal
    :footer="null"
    title="选择视图"
    v-bind="$attrs"
    :class="prefixCls"
    @register="register"
  >
    <div class="h-200px flex flex-col justify-center">
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
  import { getFormViews } from '/@/api/process/process';
  import { getCheckPwd } from '/@/api/system/system';
  const { createMessage } = useMessage();

  export default defineComponent({
    name: 'SelTemplateModal',
    components: { BasicModal, BasicForm },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { prefixCls } = useDesign('header-lock-modal');

      const [registerForm, { updateSchema, validateFields }] = useForm({
        showActionButtonGroup: false,
        schemas: [
          {
            field: 'formViewId',
            label: '视图',
            colProps: {
              span: 24,
            },
            component: 'Select',
            required: true,
            defaultValue: -1,
            componentProps: {
              fieldNames: {
                label: 'name',
                key: 'id',
                value: 'id',
              },
            },
          },
        ],
      });

      let result: any = [];
      const [register, { closeModal }] = useModalInner(async (data) => {
        result = await getFormViews({ formCode: data.formCode });
        result.unshift({ id: -1, name: '默认' });
        console.log('result', result);
        updateSchema([
          {
            field: 'formViewId',
            componentProps: {
              options: result,
            },
          },
        ]);
      });

      async function handleOk() {
        const values = (await validateFields()) as any;
        closeModal();
        emit('success', { ...values });
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
  @prefix-cls: ~'@{namespace}-header-lock-modal';

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
