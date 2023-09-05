import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { Input } from 'ant-design-vue';
import { h } from 'vue';
export const columns: BasicColumn[] = [
  {
    title: '序号',
    dataIndex: 'in',
    width: 50,
    ellipsis: true,
    customRender: function ({ index }) {
      return index + 1;
    },
  },
  {
    title: '类别',
    dataIndex: 'desc',
    width: 100,
    ellipsis: true,
    customRender: function ({ record }) {
      if (record['layer'] == 1) {
        return h(Input, {
          value: record['description'],
          onChange: (e) => (record['description'] = e.target.value),
        });
      }
    },
  },
  {
    title: '名称',
    dataIndex: 'desc2',
    width: 150,
    ellipsis: true,
    customRender: function ({ record }) {
      if (record['layer'] == 2) {
        return h(Input, {
          value: record['description'],
          onChange: (e) => (record['description'] = e.target.value),
        });
      }
    },
  },
  {
    title: '编码',
    dataIndex: 'priv',
    width: 100,
    ellipsis: true,
  },
  {
    title: '系统保留',
    dataIndex: 'isSystem',
    width: 100,
    ellipsis: true,
    customRender: function (t) {
      return t ? '是' : '否';
    },
  },
];

export const searchFormSchema: FormSchema[] = [];

export const formSchema: FormSchema[] = [
  {
    field: 'priv',
    label: '编码',
    required: true,
    component: 'Input',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'desc',
    label: '描述',
    required: true,
    component: 'Input',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'layer',
    label: '层级',
    component: 'RadioButtonGroup',
    required: true,
    colProps: {
      span: 24,
    },
    defaultValue: '1',
    componentProps: {
      options: [
        { value: '1', label: '大类' },
        { value: '2', label: '小类' },
      ],
    },
  },
];
