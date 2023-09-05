import { getDepartment, getStopStartIsValid } from '/@/api/system/system';
import { h, ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { Switch } from 'ant-design-vue';
import { useMessage } from '/@/hooks/web/useMessage';
export const isChange = ref(false);
export const columns: BasicColumn[] = [
  {
    title: '账号',
    dataIndex: ['user', 'loginName'],
    width: 120,
    resizable: true,
  },
  {
    title: '姓名',
    dataIndex: 'realName',
    width: 120,
    resizable: true,
  },
  {
    title: '性别',
    dataIndex: 'gender',
    width: 80,
    customRender: function ({ record }) {
      return record['gender'] ? '女' : '男';
    },
    resizable: true,
  },
  {
    title: '工号',
    dataIndex: 'account',
    width: 120,
  },
  {
    title: '所属部门',
    dataIndex: 'deptNames',
    width: 150,
    resizable: true,
  },
  {
    title: '角色',
    dataIndex: 'roleNames',
    width: 150,
    resizable: true,
  },
  {
    title: '手机号',
    dataIndex: 'mobile',
    resizable: true,
  },
  {
    title: '启用',
    dataIndex: ['user', 'isValid'],
    width: 120,
    ellipsis: true,
    customRender: ({ record }: any) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      const newStatus = record.user.isValid == 1 ? true : false;
      return h(Switch, {
        checked: record.user.isValid == 1 ? true : false,
        checkedChildren: '已启用',
        unCheckedChildren: '已禁用',
        loading: record.pendingStatus,
        onChange(checked: boolean) {
          record.pendingStatus = true;
          const { createMessage } = useMessage();
          isChange.value = false;
          getStopStartIsValid({ name: record.user.name, isValid: checked ? 1 : 0 })
            .then(() => {
              isChange.value = true;
              createMessage.success(`已成功修改启用状态`);
            })
            .catch(() => {
              record.user.isValid = newStatus ? 1 : 0;
              createMessage.error('修改启用状态失败');
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        },
      });
    },
    resizable: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'searchType',
    label: '',
    component: 'Select',
    defaultValue: 'realName',
    componentProps: {
      options: [
        { label: '姓名', value: 'realName' },
        { label: '帐号', value: 'userName' },
        { label: '工号', value: 'account' },
        { label: '手机', value: 'mobile' },
        { label: '邮箱', value: 'email' },
      ],
      allowClear: false,
      style: 'width:98%',
    },
    colProps: { span: 2 },
  },
  {
    field: 'condition',
    label: '',
    component: 'Input',
    colProps: { span: 6 },
    // ifShow: ({ values }) => !!values.searchType,
  },
  {
    field: 'isValid',
    label: '状态',
    defaultValue: '1',
    component: 'Select',
    componentProps: {
      options: [
        { label: '启用', value: '1' },
        { label: '停用', value: '0' },
      ],
    },
    colProps: { span: 4 },
  },
];

//部门
export const treeFormSchema: FormSchema[] = [
  {
    field: 'code',
    label: '编码',
    component: 'Input',
    required: true,
    componentProps: {
      readonly: true,
    },
  },
  {
    field: 'name',
    label: '名称',
    component: 'Input',
    required: true,
  },
  {
    field: 'parentNodeName',
    label: '归属',
    component: 'Input',
    componentProps: {
      readonly: true,
    },
  },
  {
    field: 'isHide',
    label: '隐藏',
    component: 'RadioButtonGroup',
    defaultValue: 0,
    componentProps: {
      options: [
        { label: '是', value: 1 },
        { label: '否', value: 0 },
      ],
    },
    colProps: { span: 24 },
  },
  {
    field: 'isGroup', //1：是   0:否
    label: '是否班组',
    component: 'RadioButtonGroup',
    defaultValue: 0,
    componentProps: {
      options: [
        { label: '是', value: 1 },
        { label: '否', value: 0 },
      ],
    },
    colProps: { span: 24 },
    ifShow: false,
  },
  {
    field: 'deptType', //1：部门   0:单位
    label: '是否单位',
    component: 'RadioButtonGroup',
    defaultValue: 1,
    componentProps: {
      options: [
        { label: '是', value: 0 },
        { label: '否', value: 1 },
      ],
    },
    colProps: { span: 24 },
  },
  {
    field: 'shortName',
    label: '简称',
    component: 'Input',
  },
  {
    field: 'description',
    label: '描述',
    component: 'Input',
  },
];

export const accountFormSchema: FormSchema[] = [
  {
    field: 'loginName',
    label: '账号',
    component: 'Input',
    colProps: {
      span: 12,
    },
    rules: [
      {
        required: true,
      },
    ],
  },
  {
    field: 'RealName',
    label: '姓名',
    component: 'Input',
    required: true,
    colProps: {
      span: 12,
    },
  },
  {
    field: 'photo',
    label: '头像',
    component: 'Input',
    slot: 'photo',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'mobile',
    label: '手机号',
    component: 'Input',
    colProps: {
      span: 12,
    },
    componentProps: { maxLength: 11 },
    rules: [
      {
        required: true,
      },
    ],
  },
  {
    field: 'Password',
    label: '密码',
    component: 'InputPassword',
    helpMessage: ['默认密码：123'],
    colProps: {
      span: 12,
    },
  },
  {
    field: 'isMarriaged',
    label: '婚否',
    component: 'RadioGroup',
    defaultValue: '0',
    colProps: {
      span: 12,
    },
    componentProps: {
      options: [
        {
          label: '已婚',
          value: '1',
        },
        {
          label: '未婚',
          value: '0',
        },
      ],
    },
  },
  {
    field: 'Password2',
    label: '确认密码',
    component: 'InputPassword',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'QQ',
    label: 'QQ',
    component: 'Input',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'deptCode',
    label: '部门',
    component: 'ApiTreeSelect',
    componentProps: {
      api: getDepartment,
      multiple: true,
      maxTagCount: 3,
      resultField: 'list',
      fieldNames: {
        label: 'name',
        key: 'code',
        value: 'code',
      },
      getPopupContainer: () => document.body,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'entryDate',
    label: '入职日期',
    component: 'DatePicker',
    colProps: {
      span: 12,
    },
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      style: { width: '100%' },
    },
  },
  {
    field: 'type',
    label: '类型',
    component: 'Select',
    colProps: {
      span: 12,
    },
    componentProps: {
      options: [
        {
          label: '类型1',
          value: 1,
        },
        {
          label: '类型2',
          value: 0,
        },
      ],
    },
  },
  {
    field: 'leaderName',
    label: '他的领导',
    component: 'Input',
    slot: 'leaderName',
    colProps: {
      span: 12,
    },
    componentProps: {
      disabled: true,
    },
  },
  {
    field: 'postCode',
    label: '邮政编码',
    component: 'Input',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'gender',
    label: '性别',
    component: 'RadioGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: '男', value: '0' },
        { label: '女', value: '1' },
      ],
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'birthday',
    label: '出生日期',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      style: { width: '100%' },
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'Email',
    label: 'E-mail',
    component: 'Input',
    required: true,
    colProps: {
      span: 12,
    },
  },
  {
    field: 'IDCard',
    label: '身份证号码',
    component: 'Input',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'Hobbies',
    label: '兴趣爱好',
    component: 'Input',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'Phone',
    label: '电话',
    component: 'Input',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'personNo',
    label: '员工编号',
    component: 'Input',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'MSN',
    label: '短号',
    component: 'Input',
    colProps: {
      span: 12,
    },
  },

  {
    label: '地址',
    field: 'Address',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
];

export const cardTabList = [
  {
    key: '1',
    tab: '人员资料',
  },
  {
    key: '2',
    tab: '岗位',
  },
];
