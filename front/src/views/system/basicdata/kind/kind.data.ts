import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { InputNumber } from 'ant-design-vue';
import { getChangeBasicKindOrder } from '/@/api/system/system';

//1：true 0:false
export const columns: BasicColumn[] = [
  {
    title: '排序号',
    dataIndex: 'orders',
    width: 50,
    customRender: ({ record }) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      let newValue = record.orders;
      return h(InputNumber, {
        value: record.orders,
        loading: record.pendingStatus,
        style: { width: '60px' },
        onChange: (e) => {
          newValue = e;
        },
        onBlur: () => {
          if (record.orders === newValue) {
            return;
          }
          record.pendingStatus = true;
          getChangeBasicKindOrder({ id: record.id, order: newValue })
            .then(() => {
              record.orders = newValue;
              handleSuccess();
            })
            .catch(() => {})
            .finally(() => {
              record.pendingStatus = false;
            });
        },
      });
    },
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 150,
    ellipsis: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'what',
    label: '名称',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: '名称',
    required: true,
    component: 'Input',
  },
  {
    field: 'orders',
    label: '排序号',
    component: 'InputNumber',
    // helpMessage: [''],
  },
];
