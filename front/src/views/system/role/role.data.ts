import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Switch, InputNumber } from 'ant-design-vue';
import {
  getOptions,
  getChangeRoleStatus,
  getChangeRoleOrder,
  getRoleUnit,
} from '/@/api/system/system';
import { useMessage } from '/@/hooks/web/useMessage';

export interface DataRef {
  id?: string;
  isSystem?: boolean | string;
  code?: string;
}
//1：true 0:false
export const columns: BasicColumn[] = [
  {
    title: '排序号',
    dataIndex: 'orders',
    width: 50,
    customRender: ({ record }: any) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      return h(InputNumber, {
        value: record.orders,
        loading: record.pendingStatus,
        style: { width: '60px' },
        onChange: (e) => {
          record.orders = e;
        },
        onBlur: () => {
          if (record.orders == record.oldOrders) {
            return;
          }
          record.pendingStatus = true;
          getChangeRoleOrder({ code: record.code, order: record.orders })
            .then(() => {
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
    title: '类别',
    dataIndex: 'kindName',
    width: 100,
    ellipsis: true,
  },
  {
    title: '名称',
    dataIndex: 'description',
    width: 150,
    ellipsis: true,
  },
  {
    title: '单位',
    dataIndex: 'unitName',
    width: 150,
    ellipsis: true,
  },
  {
    title: '人员',
    dataIndex: 'userRealNames',
    width: 100,
    ellipsis: true,
  },
  {
    title: '系统',
    dataIndex: 'isSystem',
    width: 50,
    ellipsis: true,
    customRender: function (t) {
      return t ? '是' : '否';
    },
  },
  {
    title: '人数',
    dataIndex: 'userCount',
    width: 50,
    ellipsis: true,
  },
  {
    title: '启用',
    dataIndex: 'status',
    width: 120,
    ellipsis: true,
    customRender: ({ record }: any) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }

      if (record.code === 'member') {
        return '';
      }

      const newStatus = record.status;
      return h(Switch, {
        checked: record.status,
        checkedChildren: '已启用',
        unCheckedChildren: '已禁用',
        loading: record.pendingStatus,
        onChange(checked: boolean) {
          record.pendingStatus = true;
          const { createMessage } = useMessage();
          getChangeRoleStatus({ code: record.code, status: checked })
            .then((res) => {
              record.status = res.status;
              createMessage.success(`已成功修改角色启用状态`);
            })
            .catch(() => {
              record.status = newStatus;
              // createMessage.error('修改角色启用状态失败');
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        },
      });
    },
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
        code: 'role_kind',
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
    field: 'desc',
    label: '名称',
    required: true,
    component: 'Input',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'orders',
    label: '排序号',
    component: 'InputNumber',
    helpMessage: ['序号越大，表示角色级别越高，用于流程中比较角色大小'],
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
        code: 'role_kind',
      },
    },
  },
  {
    field: 'isDeptManager',
    label: '管理本部门',
    component: 'RadioButtonGroup',
    colProps: {
      span: 24,
    },
    defaultValue: '1',
    componentProps: {
      options: [
        { label: '是', value: '1' },
        { label: '否', value: '0' },
      ],
    },
  },

  {
    field: 'unitName',
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
  {
    field: 'isSystem',
    label: '系统',
    component: 'Checkbox',
    colProps: {
      span: 24,
    },
    helpMessage: ['系统角色对于集团中的子单位管理员可见'],
  },
];
