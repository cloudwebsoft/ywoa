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
  import { formSchema } from './kind.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getCreateBasicKind, getUpdateBasicKind } from '/@/api/system/system';

  export default defineComponent({
    name: 'KindDrawer',
    components: { BasicDrawer, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive({});
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
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
        } else {
          dataRef = {};
        }
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      async function handleSubmit() {
        try {
          const values = await validate();
          let formData = Object.assign({}, dataRef, values);
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          if (!unref(isUpdate)) {
            await getCreateBasicKind(formData);
          } else {
            await getUpdateBasicKind(formData);
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
