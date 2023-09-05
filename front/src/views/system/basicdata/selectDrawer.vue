<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="500px"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './select.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getCreateBasic, getUpdateBasic } from '/@/api/system/system';

  export default defineComponent({
    name: 'SelectDrawer',
    components: { BasicDrawer, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive({});
      const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
        labelWidth: 90,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        resetFields();
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          dataRef = data.record;
          setFieldsValue({
            ...dataRef,
          });
          updateSchema([
            {
              field: 'code',
              componentProps: { readOnly: true },
            },
          ]);
        } else {
          dataRef = {};
          updateSchema([
            {
              field: 'code',
              componentProps: { readOnly: false },
            },
          ]);
        }
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      async function handleSubmit() {
        try {
          const values = await validate();
          let formData = Object.assign({}, dataRef, values);
          setDrawerProps({ confirmLoading: true });
          if (!unref(isUpdate)) {
            await getCreateBasic(formData);
          } else {
            await getUpdateBasic(formData);
          }
          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      return {
        registerDrawer,
        registerForm,
        getTitle,
        handleSubmit,
        dataRef,
      };
    },
  });
</script>
