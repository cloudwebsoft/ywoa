<template>
  <BasicModal
    :footer="null"
    title="回滚"
    v-bind="$attrs"
    :class="prefixCls"
    @register="register"
    :loading="isLoading"
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
  import { getActionsFinished, rollBack } from '/@/api/process/process';
  export default defineComponent({
    name: 'RollBackModal',
    components: { BasicModal, BasicForm },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { prefixCls } = useDesign('header-lock-modal');

      const isLoading = ref(false);
      const flowId = ref(-1);
      const isLog = ref(true);
      const [register, { closeModal }] = useModalInner(async (data) => {
        console.log('data', data);
        flowId.value = data.flowId;
        isLog.value = data.isLog;
        console.log('flowId.value', flowId.value);
      });

      const [registerForm, { validateFields }] = useForm({
        showActionButtonGroup: false,
        schemas: [
          {
            field: 'actionId',
            label: '已处理的节点',
            colProps: {
              span: 24,
            },
            component: 'ApiSelect',
            required: true,
            componentProps: {
              showSearch: true,
              api: getFlowActionsFinished,
              labelField: 'name',
              valueField: 'id',
            },
          },
          {
            field: 'isRollBackData',
            label: '回滚数据',
            colProps: {
              span: 24,
            },
            component: 'RadioButtonGroup',
            required: false,
            defaultValue: '1',
            componentProps: {
              options: [
                { label: '是', value: '1' },
                { label: '否', value: '0' },
              ],
            },
            helpMessage: ['将数据也回滚至所选节点的处理时间点'],
            ifShow: () => isLog.value,
          },
        ],
      });

      async function getFlowActionsFinished() {
        if (flowId.value != -1) {
          const data = await getActionsFinished({ flowId: flowId.value });
          return data;
        } else {
          return [];
        }
      }

      async function handleOk() {
        const values = (await validateFields()) as any;
        isLoading.value = true;
        try {
          await rollBack({ ...values });
          emit('success', { flowId: flowId.value, ...values });
        } finally {
          isLoading.value = false;
        }
        closeModal();
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
        isLoading,
        isLog,
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
