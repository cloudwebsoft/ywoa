import { FormSchema } from '/@/components/Table';
import { getUnitTree, getDepartment, getListRole } from '/@/api/system/system';
import { TreeSelect } from 'ant-design-vue';
import { isArray } from '/@/utils/is';
import { getFlowListTemplate } from '/@/api/flowManage/flowManage';
import componentSetting from '/@/settings/componentSetting';

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    colProps: {
      span: 24,
    },
    component: 'Input',
    show: false,
  },
  {
    field: 'layer',
    label: 'layer',
    colProps: {
      span: 24,
    },
    component: 'Input',
    show: false,
  },
  {
    field: 'parentCode',
    label: 'parentCode',
    colProps: {
      span: 24,
    },
    component: 'Input',
    show: false,
  },
  {
    field: 'name',
    label: '名称',
    component: 'Input',
    colProps: {
      span: 24,
    },
    required: true,
  },
  {
    field: 'description',
    label: '自动生成标题',
    colProps: {
      span: 24,
    },
    component: 'Input',
    slot: 'description',
    ifShow: ({ values }) => values.layer == 3,
  },
  {
    field: 'seltype',
    label: '类型',
    component: 'Select',
    colProps: {
      span: 24,
    },
    componentProps: {
      showSearch: false,
      options: [
        {
          value: 0, // op是AddChild或者op是modify if (("AddChild".equals(op) && "root".equals(parent_code)) || (isModify && leaf.getType() == Leaf.TYPE_NONE))
          label: '分类',
        },
        {
          value: 1, //AddChild".equals(op) 禁用这个
          label: '自由流程',
        },
        {
          value: 2,
          label: '固定流程',
        },
      ],
    },
    ifShow: ({ values }) => values.layer == 3,
  },
  {
    field: 'type',
    label: 'type',
    colProps: {
      span: 24,
    },
    component: 'Input',
    show: false,
  },
  {
    field: 'root_code',
    label: 'rootCode',
    colProps: {
      span: 24,
    },
    component: 'Input',
    show: false,
  },
  {
    field: 'icon',
    label: '图标',
    component: 'TreeSelect',
    colProps: {
      span: 24,
    },
    componentProps: {
      fieldNames: {
        label: 'deptName',
        key: 'id',
        value: 'id',
      },
      getPopupContainer: () => document.body,
    },
    slot: 'icon',
  },
  {
    field: 'formCode',
    label: '表单',
    component: 'Select',
    required: true,
    colProps: {
      span: 24,
    },
    componentProps: {
      options: [],
    },
    ifShow: ({ values }) => values.layer == 3,
  },

  {
    label: '手机客户端',
    field: 'isMobileStart',
    component: 'Checkbox',
    colProps: {
      span: 24,
    },
    renderComponentContent: '发起',
    ifShow: ({ values }) => values.layer == 3,
  },

  {
    label: '启用',
    field: 'isOpen',
    component: 'RadioButtonGroup',
    colProps: {
      span: 24,
    },
    defaultValue: '1',
    componentProps: {
      options: [
        {
          label: '是',
          value: '1',
        },
        {
          label: '否',
          value: '0',
        },
      ],
    },
  },
  {
    label: '编码',
    field: 'code', //op.equals("modify")?"readonly":""
    component: 'Input',
    colProps: {
      span: 24,
    },
    // show: false,
  },
  {
    label: '参数',
    field: 'params',
    component: 'Input',
    helpMessage: '可用参数：$userName，表示当前用户名，格式：user_name=$userName',
    colProps: {
      span: 24,
    },
    ifShow: ({ values }) => values.layer == 3,
  },
  {
    label: '公文模板',
    field: 'templateId',
    component: 'Select', //"modify".equals(op) 有一步赋值
    helpMessage: '用于模板套红',
    colProps: {
      span: 24,
    },
    componentProps: async ({ formActionType }) => {
      let options = (await getFlowListTemplate()) || [];
      if (!isArray(options)) {
        options = [];
      }
      options.unshift({
        id: -1,
        title: '无',
      });
      const obj = {
        options,
        fieldNames: { label: 'title', value: 'id', key: 'id' },
        getPopupContainer: () => document.body,
      };
      const { updateSchema } = formActionType;

      updateSchema([
        {
          field: 'templateId',
          componentProps: obj,
        },
      ]);

      return obj;
    },
    ifShow: ({ values }) => values.layer == 3,
  },
  {
    label: '运行模式',
    field: 'isDebug', //"modify".equals(op) 条件满足 赋值
    helpMessage:
      '运行于调试模式时，可以直接切换用户，表单的修改立即生效，流转时不会发消息通知，调试完毕后应恢复为正常模式，以便于显示历史表单记录',
    component: 'RadioButtonGroup',
    colProps: {
      span: 24,
    },
    componentProps: {
      options: [
        {
          label: '正常',
          value: '0',
        },
        {
          label: '调试',
          value: '1',
        },
      ],
    },
    ifShow: ({ values }) => values.layer == 3,
  },

  {
    field: 'depts',
    label: '能发起流程的部门',
    component: 'ApiTreeSelect',
    defaultValue: undefined,
    colProps: {
      span: 24,
    },
    helpMessage: '空表示所有部门都可以发起流程',
    componentProps: {
      api: getDepartment,
      // showSearch: true,
      multiple: true,
      showCheckedStrategy: TreeSelect.SHOW_ALL,
      maxTagCount: 10,
      // resultField: 'list',
      fieldNames: {
        label: 'name',
        key: 'code',
        value: 'code',
      },
      treeNodeFilterProp: 'name',
      getPopupContainer: () => document.body,
    },
  },
  {
    label: '单位',
    field: 'unitCode',
    component: 'TreeSelect',
    helpMessage: '公共流程为所有子单位可见',
    colProps: {
      span: 24,
    },
    componentProps: async ({ schema, tableAction, formActionType, formModel }) => {
      console.log({ schema, tableAction, formActionType, formModel });
      let treeData = (await getUnitTree()) || [];
      if (!isArray(treeData)) {
        treeData = [];
      }
      treeData.unshift({
        code: -1,
        name: '公共流程',
      });
      const obj = {
        treeData,
        fieldNames: { label: 'name', value: 'code', key: 'code' },
        getPopupContainer: () => document.body,
      };
      const { updateSchema } = formActionType;

      updateSchema([
        {
          field: 'unitCode',
          componentProps: obj,
        },
      ]);

      return obj;
    },
  },
  {
    label: '关联查询',
    field: 'queryId',
    component: 'Select',
    colProps: {
      span: 24,
    },
    slot: 'queryId',
  },
  {
    label: '条件映射',
    field: 'queryCondMap',
    component: 'Input',
    colProps: {
      span: 24,
    },
    slot: 'queryCondMap',
  },
  {
    label: '能看见查询结果的角色',
    field: 'queryRole', //roleDescs
    helpMessage: '空表示所有人员都能看见',
    component: 'Select',
    colProps: {
      span: 24,
    },
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
          field: 'queryRole',
          componentProps: obj,
        },
      ]);

      return obj;
    },
  },
];
