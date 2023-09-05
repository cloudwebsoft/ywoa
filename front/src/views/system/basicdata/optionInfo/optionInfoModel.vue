<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, reactive } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getCreateBasicOption, getUpdateBasicOption } from '/@/api/system/system';
  import { formSchema } from './optionInfo.data';

  export default defineComponent({
    name: 'PositionInfoModel',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive({});
      let code;

      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 90,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '30%' });
        resetFields();

        isUpdate.value = !!data?.isUpdate;
        dataRef = data.record;
        code = data.code;

        if (unref(isUpdate)) {
          dataRef = data.record;
          dataRef['default'] = dataRef['default'] ? 1 : 0;
          dataRef['open'] = dataRef['open'] ? 1 : 0;
          setFieldsValue({
            ...dataRef,
          });
        } else {
          dataRef = {};
        }
      });

      const getTitle = '新增';

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });
          const values = await validate();
          let formData = Object.assign({}, dataRef, values);
          if (!formData.code) {
            formData.code = code;
          }
          if (!unref(isUpdate)) {
            await getCreateBasicOption(formData).then(() => {
              closeModal();
              emit('success');
            });
          } else {
            formData['isOpen'] = formData['open'];
            formData['isDefault'] = formData['default'] == 1 ? true : false;
            await getUpdateBasicOption(formData).then(() => {
              closeModal();
              emit('success');
            });
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
      };
    },
  });
</script>
