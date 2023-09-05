import { h } from 'vue';
import { FormSchema } from '/@/components/Table';
export const formSchema: FormSchema[] = [
  {
    field: 'divider-basic',
    component: 'Divider',
    label: '回写模块设置',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'name',
    label: '回写表单',
    component: 'Select',
    colProps: {
      span: 24,
    },
    componentProps: {
      optionFilterProp: 'label',
      options: [],
      getPopupContainer: () => document.body,
    },
  },
  {
    label: '回写方式',
    field: 'depts',
    component: 'Select',
    colProps: {
      span: 24,
    },
    componentProps: {
      optionFilterProp: 'label',
      options: [],
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'description',
    label: '回写时机',
    colProps: {
      span: 24,
    },
    component: 'RadioGroup',
    componentProps: {
      options: [],
    },
  },
  {
    field: 'divider-basic2',
    component: 'Input',
    label: '配置条件',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'desc1',
    label: '设置字段值为',
    colProps: {
      span: 24,
    },
    component: 'Input',
  },
];
