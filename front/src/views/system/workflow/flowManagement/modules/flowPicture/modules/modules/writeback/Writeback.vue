<template>
  <ScrollContainer class="pr-4" v-loading="loading" :loading-tip="t('common.loadingText')">
    <div class="flex justify-around w-2/3 m-auto">
      <a-button @click="handleResetFields">重置</a-button>
      <a-button>外部</a-button>
      <a-button type="primary" @click="customSubmitFunc">保存</a-button>
    </div>
    <BasicForm @register="registerForm" />
    <BasicForm @register="registerForm2" />
  </ScrollContainer>
</template>

<script lang="ts" setup>
  import { ref, onMounted, h } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { ScrollContainer } from '/@/components/Container';
  import { Select, Input } from 'ant-design-vue';
  import { PlusOutlined, MinusOutlined } from '@ant-design/icons-vue';
  import { formSchema } from './writeback.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const loading = ref(false);
  const InputTextArea = Input.TextArea;
  const [
    registerForm,
    {
      setFieldsValue,
      updateSchema,
      resetFields,
      validate,
      appendSchemaByField,
      removeSchemaByFiled,
    },
  ] = useForm({
    labelWidth: 120,
    schemas: formSchema,
    showActionButtonGroup: false,
    actionColOptions: {
      span: 23,
    },
    // submitFunc: customSubmitFunc,
  });

  const formSchema2 = ref([]);
  formSchema2.value = formSchema;
  const [registerForm2, { validate: validate2 }] = useForm({
    labelWidth: 120,
    schemas: formSchema2,
    showActionButtonGroup: false,
    actionColOptions: {
      span: 23,
    },
  });
  const initForm = () => {
    updateSchema({
      field: 'divider-basic2',
      render: ({ model, field }) => {
        return h('span', [
          h(PlusOutlined, {
            style: 'margin-right:10px',
            onClick: (e) => {
              console.log('PlusOutlined', e, model);
              addField();
            },
          }),
          h(MinusOutlined, {
            onClick: (e) => {
              console.log('eMinusOutlinedee', e, model);
              delField();
            },
          }),
        ]);
      },
    });
    updateSchema({
      field: 'desc1',
      render: ({ model, field }) => {
        return h('span', [
          h(
            'div',
            {
              class: 'flex items-center mb-1',
            },
            [
              h(Select, {
                value: model[field],
                onChange: () => {},
              }),
              h(PlusOutlined, {
                style: 'margin-left:10px',
                onClick: (e) => {
                  console.log('PlusOutlined', e, model);
                  addField();
                },
              }),
              ,
            ],
          ),
          h(InputTextArea, {
            value: model['123'],
          }),
        ]);
      },
    });
  };

  const addField = () => {
    appendSchemaByField(
      {
        field: `369`,
        component: 'Input',
        label: '设置字段值为',
        colProps: {
          span: 24,
        },
        render: ({ model, field }) => {
          return h('span', [
            h(
              'div',
              {
                class: 'flex items-center mb-1',
              },
              [
                h(Select, {
                  value: model[field],
                  onChange: () => {},
                }),
                h(MinusOutlined, {
                  style: 'margin-left:10px',
                  onClick: (e) => {
                    console.log('eMinusOutlinedee', e, model);
                    delField(field);
                  },
                }),
                ,
              ],
            ),
            h(InputTextArea, {
              value: model['123'],
            }),
          ]);
        },
      },
      '',
    );
  };

  const delField = (field) => {
    removeSchemaByFiled([`${field}`]);
  };

  const customSubmitFunc = async () => {
    try {
      const values = await validate();
      const values2 = await validate2();
      console.log('values2', values2);
      // TODO custom api
      console.log(values);
      // emit('success', { isUpdate: unref(isUpdate), values: { ...values, id: rowId.value } });
    } finally {
    }
  };
  //重置
  const handleResetFields = () => {
    resetFields();
    formSchema2.value = [];
  };
  onMounted(() => {
    initForm();
  });
</script>
