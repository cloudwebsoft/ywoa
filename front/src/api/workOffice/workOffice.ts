import { defHttp } from '/@/utils/http/axios';

import setting from '/@/settings/projectSetting';
const Api = {
  sysMessageList: `${setting.interfacePrefix}/message_oa/sysMessageList`, //消息列表
  sysMessageShowPage: `${setting.interfacePrefix}/message_oa/sysMessageShowPage`, //消息列表查看
  setReaded: `${setting.interfacePrefix}/message_oa/setReaded`, //消息列表已读 未读
  delToDustbin: `${setting.interfacePrefix}/message_oa/delToDustbin`, //消息列表删除
  restore: `${setting.interfacePrefix}/message_oa/restore`, //消息列表恢复
  getMessageType: `${setting.interfacePrefix}/message_oa/getMessageType`, //消息类型
  getNewMsgsOfUser: `${setting.interfacePrefix}/message_oa/getNewMsgsOfUser`, //新消息

  getPlanList: `${setting.interfacePrefix}/plan/list`, //日程安排全部列表
  getPlanCreate: `${setting.interfacePrefix}/plan/create`, //日程安排全部列表新增
  getPlanUpdate: `${setting.interfacePrefix}/plan/update`, //日程安排全部列表编辑
  getPlanDel: `${setting.interfacePrefix}/plan/del`, //日程安排全部列表删除

  getPlan: `${setting.interfacePrefix}/plan/getPlan`, //日程取编辑数据
  getListPhase: `${setting.interfacePrefix}/plan/listPhase`, //我的日程

  getOacalenderList: `${setting.interfacePrefix}/oacalender/list`, //工作日历列表
  getOacalenderInit: `${setting.interfacePrefix}/oacalender/init`, //工作日历初始化
  getOacalenderModifyDays: `${setting.interfacePrefix}/oacalender/modifyDays`, //批量修改日期类型
  getOacalenderModifyDay: `${setting.interfacePrefix}/oacalender/modifyDay`, //工作日历修改某天
  getDayInfo: `${setting.interfacePrefix}/oacalender/getDayInfo`, //取得某天的工作日历
  getConfig: `${setting.interfacePrefix}/oacalender/getConfig`, //取得配置的工作日历
};

export const getSysMessageList = (params?: any) =>
  defHttp.post<any>({ url: Api.sysMessageList, params });
export const getSysMessageShowPage = (params?: any) =>
  defHttp.post<any>({ url: Api.sysMessageShowPage, params });
export const getSetReaded = (params?: any) => defHttp.post<any>({ url: Api.setReaded, params });
export const getDelToDustbin = (params?: any) =>
  defHttp.post<any>({ url: Api.delToDustbin, params });
export const getRestore = (params?: any) => defHttp.post<any>({ url: Api.restore, params });

export const getMessageType = (params?: any) =>
  defHttp.get<any>({ url: Api.getMessageType, params });

export const getNewMsgsOfUser = (params?: any) =>
  defHttp.get<any>({ url: Api.getNewMsgsOfUser, params });

export const getPlanList = (params?: any) => defHttp.post<any>({ url: Api.getPlanList, params });
export const getPlanCreate = (params?: any) =>
  defHttp.post<any>({ url: Api.getPlanCreate, params });
export const getPlanUpdate = (params?: any) =>
  defHttp.post<any>({ url: Api.getPlanUpdate, params });
export const getPlanDel = (params?: any) => defHttp.post<any>({ url: Api.getPlanDel, params });
export const getPlan = (params?: any) => defHttp.post<any>({ url: Api.getPlan, params });
export const getListPhase = (params?: any) => defHttp.post<any>({ url: Api.getListPhase, params });

export const getOacalenderList = (params?: any) =>
  defHttp.post<any>({ url: Api.getOacalenderList, params });

export const getOacalenderInit = (params?: any) =>
  defHttp.post<any>({ url: Api.getOacalenderInit, params });

export const getOacalenderModifyDays = (params?: any) =>
  defHttp.post<any>({ url: Api.getOacalenderModifyDays, params });

export const getOacalenderModifyDay = (params?: any) =>
  defHttp.post<any>({ url: Api.getOacalenderModifyDay, params });

export const getDayInfo = (params?: any) => defHttp.post<any>({ url: Api.getDayInfo, params });

export const getConfig = (params?: any) => defHttp.post<any>({ url: Api.getConfig, params });
