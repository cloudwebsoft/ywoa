import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getRoleUnit } from '/@/api/system/system';

export const columns: BasicColumn[] = [
  {
    title: '工号',
    dataIndex: 'account',
  },
  {
    title: '姓名',
    dataIndex: 'realName',
  },
  {
    title: '部门',
    dataIndex: 'deptName',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'searchUnitCode',
    label: '单位',
    component: 'ApiSelect',
    colProps: { span: 8 },
    componentProps: {
      api: getRoleUnit,
      params: {},
      resultField: 'list',
    },
  },
  {
    field: 'by',
    label: '查询字段',
    component: 'Select',
    required: true,
    colProps: { span: 5 },
    defaultValue: 'userName',
    componentProps: {
      options: [
        { value: 'userName', label: '姓名' },
        { value: 'account', label: '工号' },
      ],
    },
  },
  {
    field: 'what',
    label: '',
    component: 'Input',
    colProps: { span: 5 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: '工号',
    required: true,
    component: 'Input',
  },
  {
    field: 'realName',
    label: '姓名',
    required: true,
    component: 'Input',
    slot: 'realName',
  },
];
