<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :minHeight="100"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, reactive, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { FormSchema } from '/@/components/Table';
  import { BasicForm, useForm } from '/@/components/Form/index';
  // import { useModal } from '/@/components/Modal';
  import { Row, Col, Button, TreeSelect } from 'ant-design-vue';
  import {
    getDepartment,
    getPortalCreate,
    getPortalUpdate,
    getListRole,
  } from '/@/api/system/system';

  export default defineComponent({
    components: { BasicModal, BasicForm, Row, Col, Button, TreeSelect },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const title = ref('');
      let isUpdate = ref(true);
      let dataRef = ref<Recordable>({});

      const [registerModal, { setModalProps, closeModal, updateSchema }] = useModalInner(
        async (data) => {
          resetFields();
          setModalProps({ confirmLoading: false, width: '30%' });
          isUpdate.value = !!data?.isUpdate;
          console.log('data', data);
          if (unref(isUpdate)) {
            dataRef.value = data.record;
            const model = {
              name: data.record.name,
              icon: data.record.icon,
              depts: data.record?.depts ? data.record.depts.split(',') : [],
              roles: data.record?.roles ? data.record.roles.split(',') : [],
              status: data.record?.status ? data.record.status == 1 : false,
              kind: data.record.kind,
            };
            setFieldsValue({
              ...model,
            });
          } else {
            dataRef.value = {
              depts: [],
              roles: [],
            };
            setFieldsValue({
              ...dataRef.value,
            });
          }
        },
      );
      const filterOption = (inputValue: string, option) => {
        return option.label.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0;
      };
      const FormSchema: FormSchema[] = [
        {
          field: 'name',
          label: '标题',
          component: 'Input',
          required: true,
          colProps: {
            span: 24,
          },
        },
        {
          field: 'status',
          label: '启用',
          component: 'Switch',
          colProps: { span: 24 },
          defaultValue: true,
          componentProps: {
            checkedChildren: '是',
            unCheckedChildren: '否',
            checkedValue: true,
          },
        },
        {
          field: 'icon',
          label: '图标',
          component: 'IconPicker',
          required: true,
          colProps: {
            span: 24,
          },
        },
        {
          field: 'depts',
          label: '部门',
          component: 'ApiTreeSelect',
          defaultValue: '',
          required: false,
          colProps: {
            span: 24,
          },
          componentProps: {
            api: getDepartment,
            multiple: true,
            // showCheckedStrategy: TreeSelect.SHOW_ALL,
            maxTagCount: 3,
            resultField: 'list',
            fieldNames: {
              label: 'name',
              key: 'code',
              value: 'code',
            },
            treeNodeFilterProp: 'name',
            getPopupContainer: () => document.body,
          },
        },
        {
          field: 'roles',
          label: '角色',
          component: 'ApiSelect',
          defaultValue: '',
          required: false,
          colProps: {
            span: 24,
          },
          componentProps: {
            api: getListRole,
            mode: 'multiple',
            maxTagCount: 3,
            resultField: 'list',
            // use name as label
            labelField: 'description',
            // use id as value
            valueField: 'code',
            // optionFilterProp: 'description',
            filterOption: filterOption,
            getPopupContainer: () => document.body,
          },
        },
        {
          field: 'kind',
          label: '类型',
          component: 'RadioButtonGroup',
          defaultValue: 0,
          componentProps: {
            options: [
              { label: '桌面型', value: 0 },
              { label: '菜单型', value: 1 },
            ],
          },
          colProps: { lg: 24, md: 24 },
        },
      ];
      const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
        labelWidth: 100,
        schemas: FormSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 23,
        },
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '添加门户' : '修改门户'));

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });
          const formData = Object.assign({}, dataRef.value, values);
          formData.depts = formData.depts && formData.depts.length ? formData.depts.join(',') : '';
          formData.roles = formData.roles && formData.roles.length ? formData.roles.join(',') : '';

          console.log('formData', formData);
          if (!unref(isUpdate)) {
            // isUpdate false
            await getPortalCreate(formData);
          } else {
            //isUpdate true
            await getPortalUpdate(formData);
          }
          closeModal();
          emit('success');
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
