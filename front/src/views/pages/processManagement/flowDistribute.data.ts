import { left } from 'inquirer/lib/utils/readline';
import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '标题',
    dataIndex: 'title',
    width: 400,
    align: 'left',
  },
  {
    title: '用户',
    dataIndex: 'realName',
    width: 100,
  },
  // {
  //   title: '单位',
  //   dataIndex: 'unitName',
  //   width: 100,
  // },
  {
    title: '日期',
    dataIndex: 'disDate',
    width: 100,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'title',
    label: '标题',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'dates',
    label: '日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];
