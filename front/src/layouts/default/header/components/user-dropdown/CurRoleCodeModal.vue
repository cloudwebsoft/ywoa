<template>
  <BasicModal
    :footer="null"
    title="切换角色"
    v-bind="$attrs"
    :class="prefixCls"
    @register="register"
  >
    <div class="h-200px flex flex-col justify-center">
      <BasicForm @register="registerForm" />

      <div :class="`${prefixCls}__footer`">
        <a-button type="primary" block class="mt-2" @click="handleLock"> 确定 </a-button>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { BasicModal, useModalInner } from '/@/components/Modal/index';
  import { BasicForm, useForm } from '/@/components/Form/index';

  import { useUserStore } from '/@/store/modules/user';

  import { getSwitchDept } from '/@/api/system/system';
  export default defineComponent({
    name: 'LockModal',
    components: { BasicModal, BasicForm },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { prefixCls } = useDesign('header-lock-modal');
      const userStore = useUserStore();

      const getUserInfoPlus = computed(() => userStore.getUserInfoPlus);

      const [register, { closeModal }] = useModalInner(async () => {
        // console.log('getUserInfoPlus', getUserInfoPlus);
        if (!getUserInfoPlus.value) return;
        await getSwitchDept({ curDeptCode: getUserInfoPlus.value.curDeptCode }).then((res) => {
          console.log('res', res);
        });
      });

      const [registerForm, { validateFields, resetFields }] = useForm({
        showActionButtonGroup: false,
        schemas: [
          {
            field: 'curDeptCode',
            label: '角色',
            colProps: {
              span: 24,
            },
            component: 'Select',
            required: true,
            componentProps: {
              options: getUserInfoPlus.value?.roleList || [],
              fieldNames: { label: 'description', value: 'code' },
              getPopupContainer: () => document.body,
            },
          },
        ],
      });

      async function handleLock() {
        const values = (await validateFields()) as any;
        await getSwitchDept(values);
        closeModal();
        await resetFields();
        emit('success');
      }

      return {
        t,
        prefixCls,
        register,
        registerForm,
        handleLock,
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
