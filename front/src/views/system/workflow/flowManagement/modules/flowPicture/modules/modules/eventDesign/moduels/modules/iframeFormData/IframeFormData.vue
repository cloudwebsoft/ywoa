<template>
  <ScrollContainer class="pr-4" v-loading="loading" :loading-tip="t('common.loadingText')">
    <div class="flex justify-center">
      <div class="flex items-center">
        <div class="mr-2">表单</div>
        <div><Select :options="formDataOptions" style="width: 260px" placeholder="请选择" /></div>
      </div>
    </div>
  </ScrollContainer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { ScrollContainer } from '/@/components/Container';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { Select } from 'ant-design-vue';
  const { t } = useI18n();
  const loading = ref(false);
  const formDataOptions = ref([
    {
      value: 1,
      label: '单1',
    },
    {
      value: 1,
      label: '单2',
    },
  ]);
  const customSubmitFunc = async () => {
    try {
      const values = await validate();
      // TODO custom api
      console.log(values);
      // emit('success', { isUpdate: unref(isUpdate), values: { ...values, id: rowId.value } });
    } finally {
    }
  };

  const handleResetFields = () => {
    resetFields();
  };
</script>
