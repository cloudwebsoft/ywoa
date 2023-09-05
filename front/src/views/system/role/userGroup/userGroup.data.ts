import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Switch, InputNumber } from 'ant-design-vue';
import { getOptions, getRoleUnit } from '/@/api/system/system';
import { useMessage } from '/@/hooks/web/useMessage';

export const records = {};

//1：true 0:false
export const columns: BasicColumn[] = [
  {
    title: '类别',
    dataIndex: 'kindName',
    width: 100,
    ellipsis: true,
  },
  {
    title: '名称',
    dataIndex: 'desc',
    width: 150,
    ellipsis: true,
  },
  {
    title: '单位',
    dataIndex: 'unitName',
    width: 100,
    ellipsis: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'searchUnitCode',
    label: '单位',
    component: 'ApiSelect',
    colProps: { span: 6 },
    componentProps: {
      api: getRoleUnit,
      params: {},
      resultField: 'list',
    },
  },
  {
    field: 'kind',
    label: '类别',
    component: 'ApiSelect',
    componentProps: {
      api: getOptions,
      labelField: 'name',
      valueField: 'value',
      params: {
        code: 'usergroup_kind',
      },
    },
    colProps: { span: 6 },
  },
  {
    field: 'what',
    label: '名称',
    component: 'Input',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  // {
  //   field: 'code',
  //   label: '编码',
  //   required: true,
  //   component: 'Input',
  // },
  {
    field: 'desc',
    label: '名称',
    required: true,
    component: 'Input',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'kind',
    label: '类别',
    component: 'ApiSelect',
    required: false,
    colProps: {
      span: 24,
    },
    defaultValue: '1',
    componentProps: {
      api: getOptions,
      labelField: 'name',
      valueField: 'value',
      params: {
        code: 'usergroup_kind',
      },
    },
  },

  {
    field: 'unitCode',
    label: '单位',
    component: 'ApiSelect',
    colProps: {
      span: 24,
    },
    componentProps: {
      api: getRoleUnit,
      params: {},
      resultField: 'list',
    },
  },
];
