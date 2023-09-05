<template>
  <ScrollContainer class="pr-4" v-loading="loading" :loading-tip="t('common.loadingText')">
    <div class="flex justify-around w-1/3 m-auto">
      <!-- <a-button @click="handleResetFields">重置</a-button> -->
      <a-button type="primary" @click="customSubmitFunc">保存</a-button>
    </div>
    <BasicForm @register="registerForm" />
  </ScrollContainer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { ScrollContainer } from '/@/components/Container';
  import { formSchema } from './flowEdit.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const loading = ref(false);

  const customSubmitFunc = async () => {
    try {
      const values = await validate();
      // TODO custom api
      console.log(values);
      // emit('success', { isUpdate: unref(isUpdate), values: { ...values, id: rowId.value } });
    } finally {
    }
  };
  const [registerForm, { setFieldsValue, updateSchema, resetFields, validate }] = useForm({
    labelWidth: 120,
    schemas: formSchema,
    showActionButtonGroup: false,
    actionColOptions: {
      span: 23,
    },
    submitFunc: customSubmitFunc,
  });

  const handleResetFields = () => {
    resetFields();
  };
</script>
