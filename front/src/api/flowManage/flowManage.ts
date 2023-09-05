import { defHttp } from '/@/utils/http/axios';
import setting from '/@/settings/projectSetting';
import { ContentTypeEnum } from '/@/enums/httpEnum';

const Api = {
  flowGetDirTreeAll: `${setting.interfacePrefix}/flow/getDirTreeAll`, //流程管理左侧树
  formGetFields: `${setting.interfacePrefix}/form/getFields`, //取得表单中的所有字段
  visualGetCondField: `${setting.interfacePrefix}/visual/getCondField`, //取得查询中的条件字段
  adminFlowCreateNode: `${setting.interfacePrefix}/admin/flow/createNode`, //创建节点
  adminFlowEditNode: `${setting.interfacePrefix}/admin/flow/editNode`, //编辑时获取节点属性
  adminFlowUpdateNode: `${setting.interfacePrefix}/admin/flow/updateNode`, //编辑保存
  adminFlowDelNode: `${setting.interfacePrefix}/admin/flow/delNode`, //删除节点
  adminFlowMoveNode: `${setting.interfacePrefix}/admin/flow/moveNode`, //上下移动
  flowListTemplate: `${setting.interfacePrefix}/flow/listTemplate`, //公文模板
  visualListQuery: `${setting.interfacePrefix}/visual/listQuery`, //关联查询
  formListByFlowType: `${setting.interfacePrefix}/form/listByFlowType`, //流程类型下的表单
  adminFlowListPriv: `${setting.interfacePrefix}/admin/flow/listPriv`, //流程管理权限列表
  adminFlowCreatePriv: `${setting.interfacePrefix}/admin/flow/createPriv`, //流程管理权限列表添加
  adminFlowUpdatePriv: `${setting.interfacePrefix}/admin/flow/updatePriv`, //流程管理权限列表编辑
  adminFlowDelPriv: `${setting.interfacePrefix}/admin/flow/delPriv`, //流程管理权限列表删除

  adminFlowGetFlowJson: `${setting.interfacePrefix}/admin/flow/getFlowJson`, //获取流程图

  basicdataGetTree: `${setting.interfacePrefix}/basicdata/getTree?code=fileark_dir`, //取存档目录的接口
  flowGetActionsForProcessByFlowJson: `${setting.interfacePrefix}/flow/getActionsForProcessByFlowJson`, // 所选节点上的人员
  adminFlowListTaskStrategy: `${setting.interfacePrefix}/admin/flow/listTaskStrategy`, // 分配策略
  adminFlowListView: `${setting.interfacePrefix}/admin/flow/listView`, // 表单视图
};

export const getFlowGetDirTreeAll = (params?: any) =>
  defHttp.post<any>({ url: Api.flowGetDirTreeAll, params });
export const getFormGetFields = (params?: any) =>
  defHttp.post<any>({ url: Api.formGetFields, params });
export const getVisualGetCondField = (params?: any) =>
  defHttp.post<any>({ url: Api.visualGetCondField, params });
export const getAdminFlowCreateNode = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowCreateNode, params });
export const getAdminFlowEditNode = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowEditNode, params });
export const getAdminFlowDelNode = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowDelNode, params });
export const getAdminFlowMoveNode = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowMoveNode, params });
export const getAdminFlowUpdateNode = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowUpdateNode, params });
export const getFlowListTemplate = (params?: any) =>
  defHttp.post<any>({ url: Api.flowListTemplate, params });
export const getVisualListQuery = (params?: any) =>
  defHttp.post<any>({ url: Api.visualListQuery, params });
export const getFormListByFlowType = (params?: any) =>
  defHttp.post<any>({ url: Api.formListByFlowType, params });
export const getAdminFlowListPriv = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowListPriv, params });
export const getAdminFlowCreatePriv = (params?: any) =>
  defHttp.post<any>({
    url: Api.adminFlowCreatePriv,
    params,
    headers: { 'Content-Type': ContentTypeEnum.JSON },
  });
export const getAdminFlowUpdatePriv = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowUpdatePriv, params });
export const getAdminFlowDelPriv = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowDelPriv, params });

export const getAdminFlowGetFlowJson = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowGetFlowJson, params });

export const getBasicdataGetTree = (params?: any) =>
  defHttp.get<any>({ url: Api.basicdataGetTree, params });
export const getFlowGetActionsForProcessByFlowJson = (params?: any) =>
  defHttp.post<any>({ url: Api.flowGetActionsForProcessByFlowJson, params });

export const getAdminFlowListTaskStrategy = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowListTaskStrategy, params });
export const getAdminFlowListView = (params?: any) =>
  defHttp.post<any>({ url: Api.adminFlowListView, params });
