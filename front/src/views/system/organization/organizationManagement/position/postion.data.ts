import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getDepartment, getPostList } from '/@/api/system/system';

export const columns: BasicColumn[] = [
  {
    title: '排序号',
    dataIndex: 'orders',
    width: 70,
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 180,
  },
  {
    title: '部门',
    dataIndex: 'fullDeptName',
    width: 100,
  },
  {
    title: '限制人数',
    dataIndex: 'numLimited',
    width: 100,
  },
  {
    title: '成员',
    dataIndex: 'member',
    width: 150,
  },
  {
    title: '描述',
    dataIndex: 'description',
    width: 200,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: '名称',
    component: 'Input',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'deptCode',
    label: '部门',
    component: 'ApiTreeSelect',
    required: true,
    componentProps: {
      api: getDepartment,
      // multiple: true,
      // maxTagCount: 3,
      resultField: 'list',
      fieldNames: {
        label: 'name',
        key: 'code',
        value: 'code',
      },
      getPopupContainer: () => document.body,
    },
    colProps: { span: 24 },
  },
  {
    field: 'orders',
    label: '排序号',
    helpMessage: '排序号越大，排名越高',
    colProps: { span: 24 },
    required: true,
    component: 'InputNumber',
  },
  {
    field: 'name',
    label: '名称',
    required: true,
    component: 'Input',
    colProps: { span: 24 },
  },
  {
    field: 'description',
    label: '描述',
    component: 'Input',
    colProps: { span: 24 },
  },
  {
    field: 'status',
    label: '启用',
    component: 'Switch',
    colProps: { span: 24 },
    componentProps: {
      checkedChildren: '是',
      unCheckedChildren: '否',
      checkedValue: true,
    },
  },
  {
    field: 'limited',
    label: '是否限制人数',
    component: 'Switch',
    colProps: { span: 24 },
    componentProps: {
      checkedChildren: '是',
      unCheckedChildren: '否',
      checkedValue: true,
    },
  },
  {
    field: 'numLimited',
    label: '限制人数',
    colProps: { span: 24 },
    component: 'InputNumber',
    helpMessage: '0或负值表示不限制',
    ifShow: ({ values }) => !!values.limited,
  },
  {
    field: 'excluded',
    label: '是否职位互斥',
    component: 'Switch',
    colProps: { span: 24 },
    componentProps: {
      checkedChildren: '是',
      unCheckedChildren: '否',
    },
  },
  {
    field: 'postsExcluded',
    label: '职位',
    component: 'ApiTreeSelect',
    slot: 'postsExcluded',
    // componentProps: {
    //   api: getPostList,
    //   multiple: true,
    //   maxTagCount: 3,
    //   resultField: 'list',
    //   fieldNames: {
    //     label: 'name',
    //     key: 'id',
    //     value: 'id',
    //   },
    //   getPopupContainer: () => document.body,
    // },
    colProps: { span: 24 },
    ifShow: ({ values }) => !!values.excluded,
  },
];
