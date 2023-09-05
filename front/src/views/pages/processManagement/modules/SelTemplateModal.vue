<template>
  <BasicModal
    :footer="null"
    title="选择模板"
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
  import { BasicModal, useModalInner } from '/@/components/Modal/index';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { getTemplates } from '/@/api/process/process';
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
            field: 'templateId',
            label: '模板',
            colProps: {
              span: 24,
            },
            component: 'ApiSelect',
            required: true,
            componentProps: {
              api: getTemplates,
              labelField: 'name',
              valueField: 'id',
            },
          },
        ],
      });

      async function handleOk() {
        const values = (await validateFields()) as any;
        closeModal();
        emit('success', { attachId: attRecord.value['id'], ...values });
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
