import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tinymce } from '/@/components/Tinymce/index';
import { dateUtil, formatToDate } from '/@/utils/dateUtil';
export const columns: BasicColumn[] = [
  {
    title: '标题',
    dataIndex: 'title',
    width: 300,
    ellipsis: true,
    slots: { customRender: 'title' },
    resizable: true,
    // customCell: () => {
    //   return {
    //     style: {
    //       color: 'red', // 有效
    //       'text-align': 'left', // 无效
    //     },
    //   };
    // },
  },
  {
    title: '发布者',
    dataIndex: 'userName',
    width: 50,
    ellipsis: true,
    resizable: true,
  },
  {
    title: '类别',
    dataIndex: 'kind',
    width: 80,
    ellipsis: true,
    resizable: true,
  },
  {
    title: '有效期',
    dataIndex: 'endDate',
    width: 100,
    ellipsis: true,
    resizable: true,
  },
  {
    title: '发布日期',
    dataIndex: 'createDate',
    width: 100,
    ellipsis: true,
    resizable: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'cond',
    label: '',
    component: 'Select',
    required: true,
    colProps: { span: 2 },
    defaultValue: 'title',
    componentProps: {
      options: [
        { value: 'title', label: '标题' },
        { value: 'content', label: '内容' },
      ],
    },
  },
  {
    field: 'what',
    label: '',
    component: 'Input',
    colProps: { span: 3 },
  },
  {
    field: 'dates',
    label: '日期',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'title',
    label: '标题',
    required: true,
    component: 'Input',
    colProps: { span: 24 },
  },
  {
    field: 'color',
    label: '颜色',
    component: 'ColorPicker',
    // slot: 'color',
    colProps: { span: 4 },
  },
  {
    field: 'isBold',
    label: '',
    component: 'Checkbox',
    renderComponentContent: '加粗',
    colProps: { span: 4 },
  },
  {
    field: 'level',
    label: '',
    component: 'Checkbox',
    renderComponentContent: '弹窗通知',
    colProps: { span: 4 },
  },
  // {
  //   field: 'isShow',
  //   label: '',
  //   component: 'Checkbox',
  //   defaultValue: true,
  //   renderComponentContent: '显示已查看通知人员',
  //   colProps: { span: 4 },
  // },
  {
    field: 'isReply',
    label: '',
    component: 'Checkbox',
    defaultValue: true,
    renderComponentContent: '允许回复',
    colProps: { span: 4 },
  },
  {
    field: 'isForcedResponse',
    label: '',
    component: 'Checkbox',
    renderComponentContent: '强制回复',
    colProps: { span: 4 },
  },
  {
    field: 'isToMobile',
    label: '',
    component: 'Checkbox',
    renderComponentContent: '短信提醒',
    defaultValue: true,
    colProps: { span: 4 },
    ifShow: ({ model, field }) => {
      return model[field];
    },
  },
  {
    field: 'content',
    label: '内容',
    required: true,
    component: 'Input',
    colProps: { span: 24 },
    render: ({ model, field }) => {
      return h(Tinymce, {
        value: model[field],
        showImageUpload: false,
        onChange: (value: string) => {
          model[field] = value;
        },
      });
    },
  },
  {
    field: 'beginDate',
    label: '开始日期',
    required: true,
    defaultValue: formatToDate(dateUtil()),
    component: 'DatePicker',
    colProps: { span: 8 },
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      format: 'YYYY-MM-DD',
    },
  },
  {
    field: 'endDate',
    label: '结束日期',
    component: 'DatePicker',
    colProps: { span: 8 },
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      format: 'YYYY-MM-DD',
    },
  },
  {
    field: 'realNames',
    label: '人员范围',
    required: true,
    component: 'InputTextArea',
    defaultValue: '全部用户',
    slot: 'realNames',
    colProps: { span: 24 },
  },
  {
    field: 'file',
    label: '文件',
    component: 'Input',
    slot: 'file',
    colProps: { span: 8 },
  },
];
