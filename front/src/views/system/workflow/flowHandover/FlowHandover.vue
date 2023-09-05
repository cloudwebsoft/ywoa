<template>
  <div class="h-full bg-white">
    <div class="mx-auto w-1/3 pt-3">
      <ScrollContainer class="pr-4" v-loading="loading" :loading-tip="t('common.loadingText')">
        <BasicForm @register="registerForm" />
      </ScrollContainer>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './flowHandover.data';
  import {} from '/@/api/system/system';
  import { ScrollContainer } from '/@/components/Container';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  export default defineComponent({
    components: { BasicForm, ScrollContainer },
    setup() {
      const isUpdate = ref(true);
      const loading = ref(false);

      const [registerForm, { setFieldsValue, updateSchema, resetFields, validate }] = useForm({
        labelWidth: 180,
        schemas: formSchema,
        showActionButtonGroup: true,
        actionColOptions: {
          span: 23,
        },
        submitButtonOptions: {
          text: '确定',
        },
        submitFunc: customSubmitFunc,
      });
      let dataRef = ref([]);
      const initForm = async (data) => {
        resetFields();
        dataRef.value = {
          // ...data.record,
          root_code: '',
          type: '',
          isMobileStart: 1,
        };
        if (data?.record?.begin_date && data?.record?.end_date) {
          dataRef.value = [data.record.begin_date, data.record.end_date];
        }
        isUpdate.value = !!data?.isUpdate;

        // if (unref(isUpdate)) {
        //   setFieldsValue({
        //     ...dataRef,
        //   });
        // }
        setFieldsValue({
          ...dataRef.value,
        });
      };

      async function customSubmitFunc() {
        try {
          const values = await validate();
          loading.value = true;
          if (values.dates && values.dates.length > 0) {
            dataRef.value.begin_date = values.dates[0];
            dataRef.value.end_date = values.dates[1];
          }
          const formData = Object.assign({}, dataRef, values);
          delete formData.dates;
          // TODO custom api
          console.log(formData);
        } finally {
          loading.value = false;
        }
      }

      onMounted(() => {
        initForm();
      });

      return { registerForm, loading, t };
    },
  });
</script>
