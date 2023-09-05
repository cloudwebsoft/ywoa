import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getMessageType } from '/@/api/workOffice/workOffice';

export const columns: BasicColumn[] = [
  {
    title: '标题',
    dataIndex: 'title',
    align: 'left',
    slots: { customRender: 'title' },
  },
  {
    title: '发布者',
    dataIndex: 'sender',
    width: 100,
  },
  {
    title: '类型',
    dataIndex: 'kind',
    width: 120,
  },
  {
    title: '状态',
    dataIndex: 'isReaded',
    width: 100,
    customRender: ({ text }) => {
      return text ? '已读' : '未读';
    },
  },
  {
    title: '日期',
    dataIndex: 'sendTime',
    width: 130,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'isRecycle',
    label: '分类',
    component: 'Select',
    colProps: { span: 4 },
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '收件箱', value: '0' },
        { label: '垃圾箱', value: '1' },
      ],
    },
  },
  {
    field: 'actionType',
    label: '类型',
    component: 'ApiSelect',
    colProps: { span: 5 },
    componentProps: {
      api: getMessageType,
      labelField: 'name',
      valueField: 'type',
      params: {},
    },
  },
  {
    field: 'kind',
    label: '    ',
    component: 'Select',
    colProps: { span: 4 },
    defaultValue: 'title',
    componentProps: {
      options: [
        { value: 'title', label: '标题' },
        { value: 'content', label: '内容' },
        { value: 'notreaded', label: '未读信息' },
      ],
    },
  },
  {
    field: 'what',
    label: '',
    component: 'Input',
    colProps: { span: 3 },
  },
];
