<template>
  <ScrollContainer class="pr-4" v-loading="loading" :loading-tip="t('common.loadingText')">
    <div class="flex justify-around w-2/3 m-auto">
      <Select :options="selectOptions" class="w-40" />
      <a-button type="primary" @click="customSubmitFunc">保存</a-button>
      <a-button type="primary" @click="handleDesign">设计器</a-button>
    </div>
    <!-- <BasicForm @register="registerForm" /> -->
  </ScrollContainer>
  <DesignIframe @register="registerDesignIframeView" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  // import { BasicForm, useForm } from '/@/components/Form/index';
  import { ScrollContainer } from '/@/components/Container';
  import { Select } from 'ant-design-vue';
  import DesignIframe from './moduels/DesignIframe.vue';
  import { useModal } from '/@/components/Modal';
  // import { formSchema } from './flowEdit.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const loading = ref(false);
  const selectOptions = ref([
    {
      value: '1',
      label: '事件1',
    },
    {
      value: '2',
      label: '事件2',
    },
  ]);
  const [registerDesignIframeView, { openModal }] = useModal();
  const customSubmitFunc = async () => {
    try {
      const values = await validate();
      // TODO custom api
      console.log(values);
      // emit('success', { isUpdate: unref(isUpdate), values: { ...values, id: rowId.value } });
    } finally {
    }
  };
  // const [registerForm, { setFieldsValue, updateSchema, resetFields, validate }] = useForm({
  //   labelWidth: 120,
  //   schemas: formSchema,
  //   showActionButtonGroup: true,
  //   actionColOptions: {
  //     span: 23,
  //   },
  //   submitFunc: customSubmitFunc,
  // });

  //设计器
  const handleDesign = () => {
    openModal(true, {});
  };
</script>
