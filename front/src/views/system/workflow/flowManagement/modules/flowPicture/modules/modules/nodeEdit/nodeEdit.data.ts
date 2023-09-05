import { h } from 'vue';
import { FormSchema } from '/@/components/Table';
import { Checkbox } from 'ant-design-vue';
import { isArray } from '/@/utils/is';
import componentSetting from '/@/settings/componentSetting';
import { getAdminFlowListTaskStrategy, getAdminFlowListView } from '/@/api/flowManage/flowManage';
export const formSchema: FormSchema[] = [
  {
    field: 'handlePerson',
    label: '处理人员',
    component: 'Input',
    colProps: {
      span: 24,
    },
    slot: 'handlePerson',
  },
  {
    field: 'title',
    label: '标题',
    component: 'Input',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'relateRoleToOrganization',
    label: '角色关联',
    helpMessage: '角色与组织机构(行文方向)、职级、部门相关联',
    component: 'Input',
    colProps: {
      span: 24,
    },
    render: ({ model, field }) => {
      return model[field];
    },
  },
  {
    field: 'userName',
    label: '行文方向',
    helpMessage: '上一节点至本节点的行文方向',
    component: 'Input',
    colProps: {
      span: 24,
    },
    render: ({ model, field }) => {
      return model[field];
    },
  },
  {
    label: '关联节点',
    field: 'relateToAction',
    helpMessage: '上一节点至本节点的行文方向',
    component: 'Select',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      // optionFilterProp: 'label',
      options: [
        {
          label: '关联上一节点',
          value: 'default',
        },
        {
          label: '关联开始节点',
          value: 'starter',
        },
        {
          label: '关联表单中的部门字段',
          value: 'dept',
        },
      ],
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'relateDeptManager',
    label: '分管部门',
    helpMessage: '用户能否分管所关联节点上的人员',
    colProps: {
      span: 24,
    },
    component: 'Checkbox',
  },
  {
    field: 'userRealName',
    label: '角色/用户',
    component: 'InputTextArea',
    colProps: {
      span: 24,
    },
    componentProps: {
      disabled: true,
    },
  },
  {
    field: 'spanMode',
    label: '当前为',
    component: 'Input',
    colProps: {
      span: 24,
    },
    render: ({ model, field }) => {
      return model[field];
    },
  },
  {
    field: 'rank',
    label: '用户职级',
    component: 'Select',
    colProps: {
      span: 24,
    },
    show: false,
    componentProps: {
      options: [
        {
          value: '',
          label: '不限定',
        },
      ],
    },
  },
  {
    field: 'checkState',
    label: 'checkState',
    component: 'Input',
    colProps: {
      span: 24,
    },
    show: false,
  },
  {
    field: 'fieldWriteText', //fieldWrite
    label: '可写字段',
    component: 'InputTextArea',
    colProps: {
      span: 24,
    },
    componentProps: {
      disabled: true,
    },
    slot: 'fieldWriteText',
  },
  {
    field: 'fieldHideText', //fieldHide
    label: '隐藏字段',
    component: 'InputTextArea',
    helpMessage: '字段被隐藏后，在处理流程时将不可见',
    colProps: {
      span: 24,
    },
    componentProps: {
      disabled: true,
    },
    slot: 'fieldHideText',
  },
  // {
  //   field: 'fieldHideText', //fieldHide
  //   label: '流程抄送',
  //   component: 'InputTextArea',
  //   colProps: {
  //     span: 24,
  //   },
  //   slot: 'fieldHideText',
  // },
  {
    field: 'item1',
    label: '结束节点',
    component: 'RadioButtonGroup',
    helpMessage: '如果是结束节点，则该节点处理后流程变为结束状态',
    colProps: {
      span: 24,
    },
    componentProps: {
      options: [
        {
          value: '1',
          label: '是',
        },
        {
          value: '0',
          label: '否',
        },
      ],
    },
  },
  {
    field: 'trFlag',
    label: '标志位',
    component: 'Checkbox',
    colProps: {
      span: 24,
    },
    slot: 'trFlag',
  },
  {
    field: 'gjxz',
    label: '高级选择',
    component: 'Divider',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'strategy',
    label: '分配策略',
    component: 'Select',
    colProps: {
      span: 24,
    },

    componentProps: async ({ formActionType }) => {
      let options = (await getAdminFlowListTaskStrategy()) || [];
      if (!isArray(options)) {
        options = [];
      }
      const obj = {
        options,
        // mode: 'multiple',
        fieldNames: { label: 'name', value: 'code', key: 'code' },
        getPopupContainer: () => document.body,
      };
      const { updateSchema } = formActionType;

      updateSchema([
        {
          field: 'strategy',
          componentProps: obj,
        },
      ]);

      return obj;
    },
    // componentProps: {
    //   showSearch: false,
    //   options: [
    //     {
    //       value: '',
    //       label: '处理者指定',
    //     },
    //     {
    //       value: 'freefirst',
    //       label: '最闲者优先',
    //     },
    //     {
    //       value: 'OnlyOne',
    //       label: '只需其中一人处理',
    //     },
    //     {
    //       value: 'all',
    //       label: '角色中全部人员均需处理',
    //     },
    //     {
    //       value: 'x',
    //       label: '角色中x人处理',
    //     },
    //     {
    //       value: 'OnlySelectOne',
    //       label: '只能选择一个用户',
    //     },
    //     {
    //       value: 'AllSelected',
    //       label: '角色中全部人员默认被选中',
    //     },
    //     {
    //       value: 'GoDown',
    //       label: '自选用户时采用下达模式',
    //     },
    //     {
    //       value: 'AllSelected',
    //       label: '角色中全部人员默认被选中',
    //     },
    //     {
    //       value: 'AtLeastSelectOne',
    //       label: '至少选择一个用户',
    //     },
    //   ],
    // },
  },
  {
    field: 'ignoreType',
    label: '跳过方式',
    component: 'Select',
    helpMessage: '如设为无用户时跳过，则未选择用户时，也将被跳过',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: '0',
          label: '无用户时跳过',
        },
        {
          value: '1',
          label: '无用户时不允许跳过',
        },
        {
          value: '2',
          label: '无用户或用户之前处理过则跳过',
        },
        {
          value: '3',
          label: '角色比较大小时不允许跳过',
        },
      ],
    },
  },
  {
    field: 'kind',
    label: '动作',
    component: 'Select',
    colProps: {
      span: 24,
    },
  },

  {
    label: '退回方式',
    field: 'isMobileStart',
    component: 'Select',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: '0',
          label: '处理',
        },
        {
          value: '1',
          label: '审阅',
        },
        {
          value: '2',
          label: '子流程',
        },
      ],
    },
  },

  {
    label: '分支模式',
    field: 'branchMode',
    component: 'Select',
    helpMessage: '当分支线上无条件时，分支模式才生效',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: '1',
          label: '多选',
        },
        {
          value: '0',
          label: '单选',
        },
      ],
    },
  },
  {
    label: '延迟退休',
    field: 'isDelayTr',
    component: 'Select',
    colProps: {
      span: 24,
    },
    slot: 'isDelayTr',
  },
  {
    label: '限定部门',
    field: 'deptName', //dept
    component: 'Select',
    colProps: {
      span: 24,
    },
  },
  {
    label: '限定部门表单域',
    field: 'deptField',
    component: 'Select',
    helpMessage: '只有限定部门控件内的人员才能处理，当角色关联行文方向时无效',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [],
    },
  },
  {
    label: '被退回可重选用户',
    field: 'canSelUserWhenReturned', //1
    component: 'Checkbox',
    helpMessage: '被退回时是否可重选用户，不勾选表示只能提交给退回者',
    colProps: {
      span: 24,
    },
  },
  {
    label: '限定部门表单域',
    field: 'deptField',
    component: 'Select',
    helpMessage: '只有限定部门控件内的人员才能处理，当角色关联行文方向时无效',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [],
    },
  },
  {
    label: '归档',
    field: 'flagSaveArchive', //trArchive
    component: 'Select',
    helpMessage: '只有限定部门控件内的人员才能处理，当角色关联行文方向时无效',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: '0',
          label: '不归档',
        },
        {
          value: '2',
          label: '自动归档',
        },
        // {
        //   value:'3',
        //   label:'公文归档'
        // },
      ],
    },
  },
  {
    label: '表单视图',
    field: 'formView',
    component: 'Select',
    helpMessage: '只有限定部门控件内的人员才能处理，当角色关联行文方向时无效',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: '0',
          label: '默认',
        },
      ],
    },
  },
  {
    label: '是否提醒',
    field: 'isMsg', //1
    component: 'Checkbox',
    helpMessage: '在本节点提交时是否发送消息提醒下一步处理人员',
    colProps: {
      span: 24,
    },
  },
  {
    label: '提醒人员',
    field: 'deptField',
    component: 'Input',
    helpMessage: '如果不设置，默认仅提醒下一步处理人员',
    colProps: {
      span: 24,
    },
    slot: 'imgComb',
  },
  {
    label: '模块过滤',
    field: 'isModuleFilter',
    component: 'Checkbox',
    helpMessage: '如果存在用嵌套表格2，是否启用其模块中配置的过滤条件',
    colProps: {
      span: 24,
    },
    show: false,
  },
  {
    label: '保存按钮',
    field: 'isBtnSaveShow', //1
    component: 'Checkbox',
    helpMessage: '保存按钮是否显示',
    colProps: {
      span: 24,
    },
    componentProps: {
      suffix: '显示',
    },
  },
  {
    label: '提交按钮',
    field: 'btnAgreeName',
    component: 'Input',
    helpMessage: '提交按钮的名称，空则默认为同意',
    colProps: {
      span: 24,
    },
  },
  {
    label: '拒绝按钮',
    field: 'btnRefuseName',
    component: 'Input',
    helpMessage: '拒绝按钮的名称，空则默认为拒绝',
    colProps: {
      span: 24,
    },
  },
  {
    label: '审核人',
    field: 'isShowNextUsers', //1
    component: 'Checkbox',
    helpMessage: '是否显示下一节点上的审核人',
    colProps: {
      span: 24,
    },
    componentProps: {
      suffix: '显示下一节点上的审核人',
    },
  },
  {
    label: '流转页面',
    field: 'redirectUrl',
    component: 'Input',
    helpMessage: '交办至下一节点后重定向的页面',
    colProps: {
      span: 24,
    },
  },
  {
    label: '内部名称',
    field: 'internalName',
    component: 'Input',
    helpMessage: '节点的内部名称，用于二次开发',
    colProps: {
      span: 24,
    },
    componentProps: {
      disabled: true,
    },
  },
  {
    label: '子流程类型',
    field: 'subFlowTypeCode',
    component: 'Input',
    colProps: {
      span: 24,
    },
    componentProps: {
      options: [],
    },
    show: false,
  },
  {
    label: '字段',
    field: 'trMapField',
    component: 'Input',
    colProps: {
      span: 24,
    },
    componentProps: {
      options: [],
    },
    show: false,
  },
  {
    label: '父 -> 子',
    field: 'p2s',
    component: 'Input',
    colProps: {
      span: 24,
    },
    show: false,
  },
  {
    label: '子 -> 父',
    field: 's2P',
    component: 'Input',
    colProps: {
      span: 24,
    },
    show: false,
  },
];
