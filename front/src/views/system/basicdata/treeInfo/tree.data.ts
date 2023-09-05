import { FormSchema } from '/@/components/Table';
import { getFlowDirTree, getModulesAll } from '/@/api/system/system';
import { ref } from 'vue';
export const preCode = ref(1);
export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: '名称',
    component: 'Input',
    required: true,
    colProps: {
      span: 24,
    },
  },
  {
    field: 'preCode',
    label: '类型',
    component: 'RadioButtonGroup',
    defaultValue: '',
    colProps: {
      span: 24,
    },
    componentProps: {
      options: [
        { label: '链接', value: '' },
        { label: '流程', value: 'flow' },
        { label: '模块', value: 'module' },
      ],
      getPopupContainer: () => document.body,
      onchange: () => {
        preCode.value++;
        // setFieldsValue({ formCode: '' });
      },
    },
  },
  {
    field: 'formCode', //'flowTypeCode',
    label: '流程',
    component: 'ApiTreeSelect',
    required: true,
    componentProps: {
      api: getFlowDirTree,
      fieldNames: { label: 'name', value: 'code' },
      showSearch: true,
      treeNodeFilterProp: 'name',
      getPopupContainer: () => document.body,
    },
    colProps: {
      span: 24,
    },
    ifShow: ({ values }) => values.preCode === 'flow',
  },
  {
    field: 'formCode2',
    label: '模块',
    component: 'ApiSelect',
    required: true,
    componentProps: {
      api: getModulesAll,
      labelField: 'name',
      valueField: 'code',
      showSearch: true,
      optionFilterProp: 'label',
      getPopupContainer: () => document.body,
    },
    colProps: {
      span: 24,
    },
    ifShow: ({ values }) => values.preCode === 'module',
  },
  {
    field: 'link',
    label: '链接',
    component: 'Input',
    required: false,
    colProps: {
      span: 24,
    },
    ifShow: ({ values }) => values.preCode === '',
  },
  {
    field: 'description',
    label: '描述',
    component: 'Input',
    required: false,
    colProps: {
      span: 24,
    },
  },
  {
    field: 'metaData',
    label: '数据',
    component: 'Input',
    required: false,
    colProps: {
      span: 24,
    },
  },
  {
    field: 'isOpen',
    label: '是否启用',
    component: 'RadioButtonGroup',
    defaultValue: 1,
    colProps: {
      span: 24,
    },
    componentProps: {
      options: [
        { label: '启用', value: 1 },
        { label: '停用', value: 0 },
      ],
    },
  },
];
