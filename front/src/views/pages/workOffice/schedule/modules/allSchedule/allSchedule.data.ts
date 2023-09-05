import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getPlanUpdate } from '/@/api/workOffice/workOffice';
import { h, ref } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { Switch } from 'ant-design-vue';
import { dateUtil as dayjs } from '/@/utils/dateUtil';

export const isChange = ref(false);

export const columns: BasicColumn[] = [
  {
    title: '状态',
    dataIndex: 'closed',
    customRender: ({ record }: any) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      const newStatus = record.closed;
      return h(Switch, {
        checked: record.closed,
        checkedChildren: '已完成',
        unCheckedChildren: '未完成',
        loading: record.pendingStatus,
        onChange(checked: boolean) {
          record.pendingStatus = true;
          isChange.value = false;
          const { createMessage } = useMessage();
          getPlanUpdate({
            ...record,
            shared: record.shared ? '1' : '0',
            isClosed: checked ? '1' : '0',
          })
            .then(() => {
              isChange.value = true;
              createMessage.success(`已成功修改状态`);
            })
            .catch(() => {
              record.closed = newStatus;
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        },
      });
    },
  },
  {
    title: '日期',
    dataIndex: 'myDate',
  },
  {
    title: '标题',
    dataIndex: 'title',
  },
  {
    title: '共享',
    dataIndex: 'shared',
    customRender: ({ record }: any) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      const newStatus = record.shared;
      return h(Switch, {
        checked: record.shared,
        checkedChildren: '已共享',
        unCheckedChildren: '未共享',
        loading: record.pendingStatus,
        onChange(checked: boolean) {
          record.pendingStatus = true;
          const { createMessage } = useMessage();
          getPlanUpdate({
            ...record,
            isClosed: record.closed ? '1' : '0',
            shared: checked ? '1' : '0',
          })
            .then(() => {
              isChange.value = true;
              createMessage.success(`已成功修改状态`);
            })
            .catch(() => {
              record.shared = newStatus;
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        },
      });
    },
  },
  {
    title: '制定者',
    dataIndex: 'userName',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'dates',
    label: '日期区间',
    component: 'RangePicker',
    colProps: { span: 6 },
    componentProps: {
      getPopupContainer: () => document.body,
      valueFormat: 'YYYY-MM-DD',
    },
  },
  {
    field: 'isClosed',
    label: '状态',
    component: 'Select',
    colProps: { span: 6 },
    componentProps: {
      options: [
        { value: '-1', label: '不限' },
        { value: '0', label: '未完成' },
        { value: '1', label: '已完成' },
      ],
    },
  },
  {
    field: 'what',
    label: '标题',
    component: 'Input',
    colProps: { span: 6 },
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
    field: 'myDate',
    label: '开始时间',
    required: true,
    component: 'DatePicker',
    colProps: { span: 24 },
    defaultValue: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    componentProps: {
      placeholder: '请选择',
      style: 'width:100%',
      showTime: true,
      valueForamt: 'YYYY-MM-DD HH:mm:ss',
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'endDate',
    label: '结束时间',
    component: 'DatePicker',
    colProps: { span: 24 },
    defaultValue: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    componentProps: {
      placeholder: '请选择',
      style: 'width:100%',
      showTime: true,
      valueForamt: 'YYYY-MM-DD HH:mm:ss',
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'closed',
    label: '是否完成',
    component: 'Switch',
    colProps: { span: 24 },
    componentProps: {
      checkedChildren: '已完成',
      unCheckedChildren: '未完成',
    },
    ifShow: false,
  },
  {
    field: 'shared',
    label: '是否共享',
    component: 'Switch',
    colProps: { span: 24 },
    componentProps: {
      checkedChildren: '是',
      unCheckedChildren: '否',
    },
  },
  {
    field: 'remind',
    label: '是否提醒',
    component: 'Switch',
    colProps: { span: 24 },
    componentProps: {
      checkedChildren: '是',
      unCheckedChildren: '否',
    },
  },
  {
    field: 'before',
    label: '提醒时间',
    component: 'Select',
    required: true,
    slot: 'before',
    colProps: { span: 24 },
    ifShow: ({ values }) => !!values.remind,
  },
  {
    field: 'content',
    label: '内容',
    required: true,
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
];
