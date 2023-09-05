import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { InputNumber, Select, TreeSelect } from 'ant-design-vue';
import { getChangeBasicOrder, getListBasicDataKind } from '/@/api/system/system';

export const columns: BasicColumn[] = [
  {
    title: '排序号',
    dataIndex: 'orders',
    width: 60,
    customRender: ({ record }: any) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      const newValue = record.orders;
      return h(InputNumber, {
        size: 'small',
        value: record.orders,
        loading: record.pendingStatus,
        style: { width: '60px' },
        onChange: (e) => {
          record.orders = e;
        },
        onBlur: () => {
          if (record.orders === record.oldOrders) {
            return;
          }
          record.pendingStatus = true;
          getChangeBasicOrder({ code: record.code, order: newValue })
            .then(() => {
              record.orders = newValue;
              handleSuccess();
            })
            .catch(() => {
              record.orders = record.oldOrders;
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        },
      });
    },
  },
  {
    title: '编码',
    dataIndex: 'code',
    width: 100,
    ellipsis: true,
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 100,
    ellipsis: true,
  },
  {
    title: '选项',
    dataIndex: 'options',
    width: 200,
    ellipsis: true,
    customRender: ({ record }: any) => {
      if (record['type'] === 0) {
        return h(Select, {
          size: 'small',
          style: 'width: 100%',
          fieldNames: { value: 'value', label: 'name' },
          options: record['options'],
          value: record['defaultValue'],
          onChange: (e) => {
            console.log(e);
            record['defaultValue'] = e;
          },
        });
      } else {
        return h(TreeSelect, {
          size: 'small',
          style: 'width: 100%',
          fieldNames: { value: 'code', label: 'name' },
          treeData: record['treeData'],
          treeDefaultExpandAll: true,
          value: record['defaultValue'],
          onChange: (e) => {
            console.log(e);
            record['defaultValue'] = e;
          },
        });
      }
    },
  },
  {
    title: '类别',
    dataIndex: 'typeName',
    width: 50,
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'kindName',
    width: 150,
    ellipsis: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'code',
    label: '编码/名称',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'kind',
    label: '类型',
    component: 'ApiSelect',
    // defaultValue: '',
    componentProps: {
      api: getListBasicDataKind,
      labelField: 'name',
      valueField: 'id',
      params: {
        type: 'kind',
      },
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'code',
    label: '编码',
    required: true,
    component: 'Input',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'name',
    label: '名称',
    required: true,
    component: 'Input',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'orders',
    label: '序号',
    component: 'InputNumber',
    colProps: {
      span: 24,
    },
    // helpMessage: [''],
  },
  {
    field: 'type',
    label: '类别',
    component: 'Select',
    defaultValue: '0',
    required: true,
    colProps: {
      span: 24,
    },
    // helpMessage: [],
    componentProps: {
      options: [
        { label: '列表', value: 0 },
        { label: '树形', value: 1 },
      ],
    },
  },
  {
    field: 'kind',
    label: '类型',
    component: 'ApiSelect',
    required: true,
    colProps: {
      span: 24,
    },
    defaultValue: '',
    componentProps: {
      api: getListBasicDataKind,
      labelField: 'name',
      valueField: 'id',
      params: {
        type: 'kind',
      },
    },
  },
];
