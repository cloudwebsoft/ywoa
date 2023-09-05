<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { treeFormSchema } from './organizationManagement.data';
  import {
    getDepartmentCreate,
    getDepartmentEdit,
    getNewCode,
    getDepartmentSave,
  } from '/@/api/system/system';

  export default defineComponent({
    name: 'DeptTreeListModel',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let isUpdate = ref(true);
      let dataRef = reactive({ code: '' });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false, width: '30%' });
        isUpdate.value = !!data?.isUpdate;
        console.log('data', data);
        if (unref(isUpdate)) {
          dataRef = data.record;
          await getDepartmentEdit({ code: dataRef.code }).then((res) => {
            dataRef = res;
            setFieldsValue({
              ...dataRef,
            });
          });
        } else {
          dataRef = {};
          dataRef['parentNodeName'] = data.record.name;
          dataRef['show'] = 1;

          // parentCode
          dataRef['parentCode'] = data.record.code ? data.record.code : 'root';
          await getNewCode({ parentCode: dataRef['parentCode'] }).then((res) => {
            dataRef['code'] = res.newNodeCode;
            setFieldsValue({
              ...dataRef,
            });
          });
        }
      });
      const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
        labelWidth: 100,
        schemas: treeFormSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 23,
        },
      });

      // const treeData = [];
      //   updateSchema([
      //     {
      //       field: 'pwd',
      //       show: !unref(isUpdate),
      //     },
      //     {
      //       field: 'dept',
      //       componentProps: { treeData },
      //     },
      //   ]);
      // });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });
          // TODO custom api
          const formData = Object.assign({}, dataRef, values);
          if (!unref(isUpdate)) {
            await getDepartmentCreate(formData);
          } else {
            let params = {
              code: formData.code,
              deptType: formData.deptType,
              description: formData.description,
              id: formData.id,
              isGroup: formData.isGroup,
              isHide: formData.isHide,
              layer: formData.layer,
              name: formData.name,
              parentCode: formData.parentCode,
              shortName: formData.shortName,
              show: '1',
            };
            await getDepartmentSave(params);
          }
          closeModal();
          emit('success');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return { registerModal, registerForm, getTitle, handleSubmit };
    },
  });
</script>
