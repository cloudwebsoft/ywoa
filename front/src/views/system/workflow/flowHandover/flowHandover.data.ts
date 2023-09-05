import { FormSchema } from '/@/components/Table';
export const formSchema: FormSchema[] = [
  {
    field: 'typeCode',
    label: '选择流程',
    component: 'Select',
    colProps: {
      span: 24,
    },
    componentProps: {
      mode: 'multiple',
      options: [],
      optionFilterProp: 'label',
      fieldNames: {
        label: 'deptName',
        key: 'id',
        value: 'id',
      },
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'oldUserRealName', //oldUserName
    label: '原办理人',
    colProps: {
      span: 24,
    },
    required: true,
    component: 'SelectUserInput',
    componentProps: ({ schema, tableAction, formActionType, formModel }) => {
      return {
        type: 0,
        onChange: (value, record) => {
          console.log('value,e', value, record);
          formModel['oldUserName'] = record.dataRef.userNames || '';
          console.log({ schema, tableAction, formActionType, formModel });
        },
      };
    },
    // componentProps: {
    //   type: 0,
    //   onChange: (value, e) => {
    //     console.log('value,e', value, e);
    //   },
    // },
  },
  {
    field: 'oldUserName',
    label: 'oldUserName',
    colProps: {
      span: 24,
    },
    component: 'Input',
    show: false,
  },
  {
    field: 'newUserRealName',
    label: '移交给',
    component: 'SelectUserInput',
    required: true,
    componentProps: ({ schema, tableAction, formActionType, formModel }) => {
      return {
        type: 0,
        onChange: (value, record) => {
          console.log('value,e', value, record);
          formModel['newUserName'] = record.dataRef.userNames || '';
          console.log({ schema, tableAction, formActionType, formModel });
        },
      };
    },
    colProps: {
      span: 24,
    },
  },
  {
    field: 'newUserName',
    label: 'newUserName',
    colProps: {
      span: 24,
    },
    component: 'Input',
    show: false,
  },
  {
    field: 'dates',
    label: '日期',
    component: 'RangePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      format: 'YYYY-MM-DD',
      placeholder: ['开始时间', '结束时间'],
      showTime: false,
      getPopupContainer: () => document.body,
    },
    colProps: {
      span: 24,
    },
  },
];
