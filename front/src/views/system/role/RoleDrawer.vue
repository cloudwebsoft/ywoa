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
  import { formSchema, DataRef } from './role.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';

  import { getCreateRole, getUpdateRole } from '/@/api/system/system';
  export default defineComponent({
    name: 'RoleDrawer',
    components: { BasicDrawer, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive<DataRef>({});
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 90,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        resetFields();
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        console.log('isUpdate', isUpdate);

        if (unref(isUpdate)) {
          dataRef = data.record;
          dataRef.isSystem = dataRef.isSystem ? true : false;
          setFieldsValue({
            ...dataRef,
          });
        } else {
          dataRef = {};
        }
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增角色' : '编辑角色'));

      async function handleSubmit() {
        try {
          const values = await validate();
          let formData = Object.assign({}, dataRef, values);
          formData.isSystem = formData.isSystem ? 1 : 0;
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          if (!unref(isUpdate)) {
            await getCreateRole(formData);
          } else {
            await getUpdateRole(formData);
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
