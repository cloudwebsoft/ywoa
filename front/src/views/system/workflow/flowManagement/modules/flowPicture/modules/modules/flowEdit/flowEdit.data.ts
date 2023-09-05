import { h } from 'vue';
import { FormSchema } from '/@/components/Table';
import { isArray } from '/@/utils/is';
import componentSetting from '/@/settings/componentSetting';
import { getBasicdataGetTree } from '/@/api/flowManage/flowManage';
export const formSchema: FormSchema[] = [
  {
    field: 'title',
    label: '名称',
    component: 'Input',
    colProps: {
      span: 24,
    },
    render: ({ model, field }) => {
      return model[field];
    },
  },
  {
    label: '存档目录',
    field: 'dirCode',
    component: 'TreeSelect',
    helpMessage: '自动存档时保存于文件柜的目录，需在节点属性上配置为“自动存档',
    colProps: {
      span: 24,
    },
    componentProps: async ({ formActionType }) => {
      let treeData =
        (await getBasicdataGetTree())[componentSetting.table.fetchSetting.listField] || [];
      console.log('optionsoptions', treeData);
      if (!isArray(treeData)) {
        treeData = [];
      }
      const obj = {
        treeData,
        // mode: 'multiple',
        fieldNames: { label: 'name', value: 'code', key: 'code' },
        getPopupContainer: () => document.body,
      };
      const { updateSchema } = formActionType;

      updateSchema([
        {
          field: 'dirCode',
          componentProps: obj,
        },
      ]);

      return obj;
    },
  },
  {
    field: 'examine',
    label: '保存状态',
    helpMessage: '自动存档时保存状态',
    colProps: {
      span: 24,
    },
    component: 'Select',
    componentProps: {
      showSearch: false,
      optionFilterProp: 'label',
      options: [
        {
          value: '0',
          label: '未审核',
        },
        {
          value: '2',
          label: '已通过',
        },
      ],
    },
  },
  {
    field: 'isReactive', //1
    label: '能否变更',
    component: 'Checkbox',
    helpMessage: '能否在流程已转交下一步后再次变更',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'isRecall', //1
    label: '能否撤回',
    component: 'Checkbox',
    helpMessage: '流程在转交后能否撤回',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'isDistribute', //1
    label: '流程抄送',
    component: 'Checkbox',
    helpMessage: '在每个节点上都可以将流程表单分发给相关人员',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'isPlus', //1
    label: '能否加签',
    component: 'Checkbox',
    helpMessage: '流程中能否加签',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'isTransfer', //1
    label: '能否转办',
    component: 'Checkbox',
    helpMessage: '流程中能否转办',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'isReply', //1
    label: '能否回复',
    component: 'Checkbox',
    helpMessage: '流程中能否回复',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'isModuleFilter', //1
    label: '模块过滤',
    component: 'Checkbox',
    helpMessage: '如果存在用嵌套表格2，则启用其模块中配置的过滤条件',
    colProps: {
      span: 24,
    },
  },
  {
    field: 'icon',
    label: '最大下载',
    component: 'InputNumber',
    helpMessage: '每个人可下载每个附件的最大次数',
    defaultValue: -1,
    colProps: {
      span: 24,
    },
    componentProps: {
      suffix: '-1表示不限',
    },
  },
  {
    field: 'canDelOnReturn', //1
    label: '退回时可删除',
    component: 'Checkbox',
    helpMessage: '当节点上设置了“删除流程”标志位时，被退回时能否删除',
    colProps: {
      span: 24,
    },
  },

  {
    label: '退回方式',
    field: 'returnMode',
    component: 'Select',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: '0',
          label: '退回后按流程图流转',
        },
        {
          value: '1',
          label: '退回后可直送给返回者',
        },
      ],
    },
  },

  {
    label: '退回人员',
    field: 'returnStyle',
    component: 'Select',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: '0',
          label: '按流程图退回至设定的人员',
        },
        {
          value: '1',
          label: '可退回至任一已处理过的人员',
        },
      ],
    },
  },
  {
    label: '角色比较',
    field: 'roleRankMode',
    component: 'Select',
    helpMessage: '特定角色节点不会被跳过',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: '0',
          label: '无',
        },
        {
          value: '2',
          label: '跳过比当前角色小的节点',
        },
      ],
    },
  },
  {
    label: '注',
    field: 'params',
    component: 'Input',
    render: () => {
      const div2 = h('div', '选中节点或连接线可以编辑属性');
      return h('div', ["修改属性后请点击'保存'按钮", div2]);
    },

    colProps: {
      span: 24,
    },
  },
];
