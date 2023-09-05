import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getListRole } from '/@/api/system/system';
import componentSetting from '/@/settings/componentSetting';
import { isArray } from '/@/utils/is';
import { h } from 'vue';
import { Checkbox } from 'ant-design-vue';
export const columns: BasicColumn[] = [
  {
    title: '用户',
    dataIndex: 'title',
    width: 100,
    ellipsis: true,
    resizable: true,
  },
  {
    title: '发起',
    dataIndex: 'see',
    width: 50,
    ellipsis: true,
    resizable: true,
    customRender: ({ record, column }) => {
      return h(Checkbox, {
        checked: record['see'] == 1 ? true : false,
        onChange: (e) => {
          record['see'] = e.target.checked ? 1 : 0;
        },
      });
    },
  },
  {
    title: '查询',
    dataIndex: 'modify',
    width: 80,
    ellipsis: true,
    resizable: true,
    customRender: ({ record }) => {
      return h(Checkbox, {
        checked: record['modify'] == 1 ? true : false,
        onChange: (e) => {
          record['modify'] = e.target.checked ? 1 : 0;
        },
      });
    },
  },
  {
    title: '管理',
    dataIndex: 'examine',
    width: 100,
    ellipsis: true,
    resizable: true,
    customRender: ({ record }) => {
      return h(Checkbox, {
        checked: record['examine'] == 1 ? true : false,
        onChange: (e) => {
          record['examine'] = e.target.checked ? 1 : 0;
        },
      });
    },
  },
  {
    title: '类型',
    dataIndex: 'typeDesc',
    width: 100,
    ellipsis: true,
    resizable: true,
  },
  {
    title: '流程类别',
    dataIndex: 'dirName',
    width: 100,
    ellipsis: true,
    resizable: true,
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
    field: 'title1',
    label: '角色',
    required: true,
    component: 'Select',
    colProps: { span: 24 },
    componentProps: async ({ formActionType }) => {
      let options =
        (await getListRole({ pageSize: 99999 }))[componentSetting.table.fetchSetting.listField] ||
        [];
      if (!isArray(options)) {
        options = [];
      }
      const obj = {
        options,
        mode: 'multiple',
        fieldNames: { label: 'description', value: 'code', key: 'code' },
        getPopupContainer: () => document.body,
      };
      const { updateSchema } = formActionType;

      updateSchema([
        {
          field: 'title1',
          componentProps: obj,
        },
      ]);

      return obj;
    },
  },
  {
    field: 'title3',
    label: '用户名',
    required: true,
    component: 'SelectUserInput',
    colProps: { span: 24 },
    componentProps: ({ schema, tableAction, formActionType, formModel }) => {
      return {
        type: 1,
        onChange: (value, record) => {
          console.log('value,e', value, record);
          formModel['newUserName'] = record.dataRef.userNames || '';
          console.log({ schema, tableAction, formActionType, formModel });
        },
      };
    },
  },
  {
    field: 'newUserName',
    label: 'newUserName',
    required: true,
    component: 'Input',
    colProps: { span: 24 },
    show: false,
  },
];
