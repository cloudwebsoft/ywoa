<template>
  <BasicModal
    :footer="null"
    title="选择部门"
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
  import { getDepartment } from '/@/api/system/system';
  export default defineComponent({
    name: 'SelDeptModal',
    components: { BasicModal, BasicForm },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { prefixCls } = useDesign('header-lock-modal');

      const record = ref({});
      const [register, { closeModal }] = useModalInner(async (data) => {
        record.value = data;

        // 在此处updateSchema，使得可以控制multiple，另外，也是为了防止两次getDepartment，因为updateSchema的时候会调用一次
        updateSchema({
          field: 'deptCode',
          componentProps: {
            api: getDepartment,
            showSearch: true,
            treeNodeFilterProp: 'name',
            params: {
              parentFormCode: record.value.parentCode ? record.value.parentCode : '',
            },
            multiple: !record.value.isSingle,
            maxTagCount: 3,
            resultField: 'list',
            fieldNames: {
              label: 'name',
              key: 'code',
              value: 'code',
            },
            // expandedKeys: ['root'], // 无效
            labelInValue: true, // 是否把每个选项的 label 包装到 value 中
            getPopupContainer: () => document.body,
          },
        });

        setFieldsValue({
          deptCode: data.value,
        });
      });

      const [registerForm, { validateFields, updateSchema, setFieldsValue }] = useForm({
        showActionButtonGroup: false,
        schemas: [
          {
            field: 'deptCode',
            label: '部门',
            colProps: {
              span: 24,
            },
            component: 'ApiTreeSelect',
            required: true,
            defaultValue: '',
            componentProps: {},
          },
        ],
      });

      async function handleOk() {
        const values = (await validateFields()) as any;
        closeModal();
        console.log('handleOk', values.deptCode);
        emit('success', values.deptCode);
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
