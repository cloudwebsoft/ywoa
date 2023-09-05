<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="30%"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './userGroup.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';

  import { getCreateGroup, getUpdateGroup, getCode } from '/@/api/system/system';

  export default defineComponent({
    name: 'UserGroupDrawer',
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
          console.log('dataRef', dataRef);
          setFieldsValue({
            ...dataRef,
          });
        } else {
          dataRef = {};
          await getCode().then((res) => {
            dataRef.code = res;
            setFieldsValue({
              ...dataRef,
            });
          });
        }
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      async function handleSubmit() {
        try {
          const values = await validate();
          let formData = Object.assign({}, dataRef, values);
          formData.isDept = formData.isDept ? '1' : '0';
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          console.log('formData', formData);

          if (!unref(isUpdate)) {
            await getCreateGroup(formData);
          } else {
            await getUpdateGroup(formData);
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
