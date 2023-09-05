import { defHttp } from '/@/utils/http/axios';
import setting from '/@/settings/projectSetting';
import { useMessage } from '/@/hooks/web/useMessage';
import { h } from 'vue';

import { filterJS } from '/@/utils/utils';
import { downloadByData } from '/@/utils/file/download';

const { createConfirm, createMessage } = useMessage();

const Api = {
  flowProcess: `${setting.interfacePrefix}/flow/flowProcess`, //固定流程处理
  flowProcessScript: `${setting.interfacePrefix}/flow/flowProcessScript`, //固定流程的显示规则等脚本
  flowProcessFree: `${setting.interfacePrefix}/flow/flowProcessFree`, //自由流程处理
  finishAction: `${setting.interfacePrefix}/flow/finishAction`, //流程提交处理
  finishActionFree: `${setting.interfacePrefix}/flow/finishActionFree`, //流程提交处理
  finishBatch: `${setting.interfacePrefix}/flow/finishBatch`, //流程提交处理
  listAttachment: `${setting.interfacePrefix}/flow/listAttachment`, //附件列表
  listAttachmentDelAtt: `${setting.interfacePrefix}/flow/delAtt`, //附件列表删除
  delAttach: `${setting.interfacePrefix}/flow/delAttach`, //删除文件宏控件中的附件
  uploadMedia: `${setting.interfacePrefix}/uploadMedia`, //上传图片、视频

  getDirTree: `${setting.interfacePrefix}/flow/getDirTree`, //发起流程页面树数据
  getDirTreeForQuery: `${setting.interfacePrefix}/flow/getDirTreeForQuery`, //查询流程页面树数据
  getDirTreeOpened: `${setting.interfacePrefix}/flow/getDirTreeOpened`, //待办流程页面树数据

  flowInit: `${setting.interfacePrefix}/flow/init`, //传参数typeCode，即流程类型编码，可以发起流程，然后在返回中会有myActionId，然后调用流程抽屉
  flowListPage: `${setting.interfacePrefix}/flow/listPage`, //流程通用列表配置项，例如表头等等
  flowList: `${setting.interfacePrefix}/flow/list`, //流程通用列表

  flowShow: `${setting.interfacePrefix}/flowShow`, //流程详情
  flowShowScript: `${setting.interfacePrefix}/flowShowScript`, //流程详情中的显示规则脚本
  flowView: `${setting.interfacePrefix}/flowView`, //流程视图
  flowShowChart: `${setting.interfacePrefix}/flowShowChart`, //流程图
  flowModifyTitle: `${setting.interfacePrefix}/flowModifyTitle`, //流程修改标题信息
  modifyTitle: `${setting.interfacePrefix}/flow/modifyTitle`, //流程修改标题信息

  getConds: `${setting.interfacePrefix}/flow/getConds`, //查询流程扩增条件
  getFields: `${setting.interfacePrefix}/flow/getFields`, //取得表单中的字段
  getFieldsWithNest: `${setting.interfacePrefix}/flow/getFieldsWithNest`, //流程查询中扩增导出字段
  setConds: `${setting.interfacePrefix}/flow/setConds`, //流程扩增条件字段保存
  getExportFields: `${setting.interfacePrefix}/flow/getExportFields`, //导出表头所有已设字段
  getServerInfo: `${setting.interfacePrefix}/public/getServerInfo`, //获取url
  getColProps: `${setting.interfacePrefix}/flow/getColProps`, //取出表头设置
  saveColProps: `${setting.interfacePrefix}/flow/saveColProps`, //保存表头设置
  saveColWidth: `${setting.interfacePrefix}/flow/saveColWidth`, //保存列宽设置
  getFlowListFields: `${setting.interfacePrefix}/flow/getFlowListFields`, // 取出所有的字段，含表单及流程中的系统字段
  getExportExcel: `${setting.interfacePrefix}/flow/exportExcel`, //导出

  getExportExcelAsync: `${setting.interfacePrefix}/flow/exportExcelAsync`, //异步导出Excel

  getExportExcelProgress: `${setting.interfacePrefix}/visual/getExportExcelProgress`, //异步导出进度

  applyProps: `${setting.interfacePrefix}/flow/applyProps`, //debug调试面板确定
  runValidateScript: `${setting.interfacePrefix}/flow/runValidateScript`, //debug调试面板验证脚本
  runFinishScript: `${setting.interfacePrefix}/flow/runFinishScript`, //debug调试面板结束脚本
  runDeliverScript: `${setting.interfacePrefix}/flow/runDeliverScript`, //debug调试面板流转脚本

  getTestInfo: `${setting.interfacePrefix}/flow/getTestInfo`, //debug页面获取调试信息

  getTokenByUser: `${setting.interfacePrefix}/flow/getTokenByUser`, //获取新的用户token

  flowCreateNestSheetRelated: `${setting.interfacePrefix}/flow/createNestSheetRelated`, //创建嵌套表格记录

  flowUpdateNestSheetRelated: `${setting.interfacePrefix}/flow/updateNestSheetRelated`, //修改嵌套表格记录

  discardFlow: `${setting.interfacePrefix}/flow/discard`, //放弃流程
  suspend: `${setting.interfacePrefix}/flow/suspend`, //挂起流程
  resume: `${setting.interfacePrefix}/flow/resume`, //挂起流程

  delFlow: `${setting.interfacePrefix}/flow/del`, //删除流程

  returnAction: `${setting.interfacePrefix}/flow/returnAction`, //退回流程

  matchBranchAndUser: `${setting.interfacePrefix}/flow/matchBranchAndUser`, //匹配分支及用户

  favorite: `${setting.interfacePrefix}/flow/favorite`, //关注流程
  unfavorite: `${setting.interfacePrefix}/flow/unfavorite`, //取关流程
  download: `${setting.interfacePrefix}/flow/download`, //取关流程

  resetColProps: `${setting.interfacePrefix}/flow/resetColProps`, //重置流程列表的表头
  remind: `${setting.interfacePrefix}/flow/remind`, //催办

  plus: `${setting.interfacePrefix}/flow/plus`, //加签
  delPlus: `${setting.interfacePrefix}/flow/delPlus`, //删除加签

  listAnnex: `${setting.interfacePrefix}/flow/listAnnex`, //流程回复列表
  addReply: `${setting.interfacePrefix}/flow/addReply`, //流程回复新增
  delAnnex: `${setting.interfacePrefix}/flow/delAnnex`, //流程回复删除
  downloadAnnexAttachment: `${setting.interfacePrefix}/flow/downloadAnnexAttachment`, //流程回复下载附件
  recall: `${setting.interfacePrefix}/flow/recall`, //撤回

  distribute: `${setting.interfacePrefix}/flow/distribute`, //抄送
  listDistributeToMe: `${setting.interfacePrefix}/flow/listDistributeToMe`, // 抄送给我的
  listMyDistribute: `${setting.interfacePrefix}/flow/listMyDistribute`, // 我抄送的
  delDistribute: `${setting.interfacePrefix}/flow/delDistribute`, // 删除抄送

  getTemplates: `${setting.interfacePrefix}/flow/getTemplates`, // 获得用户有权限的公文模板
  convertToRedDocument: `${setting.interfacePrefix}/flow/convertToRedDocument`, // 套红
  getStamps: `${setting.interfacePrefix}/flow/getStamps`, // 获得用户有权限的印章
  sealDocument: `${setting.interfacePrefix}/flow/sealDocument`, // 盖章
  getPhrases: `${setting.interfacePrefix}/tipPhrase/getPhrases`, // 取得常用语
  addPhrase: `${setting.interfacePrefix}/tipPhrase/add`, // 添加常用语
  addFrequency: `${setting.interfacePrefix}/tipPhrase/addFrequency`, // 添加常用语使用频次
  delPhrase: `${setting.interfacePrefix}/tipPhrase/del`, // 添加常用语使用频次
  getFormViews: `${setting.interfacePrefix}/form/getViews`, // 取得表单视图
  getActionsFinished: `${setting.interfacePrefix}/flow/getActionsFinished`, // 取得已处理的节点
  rollBack: `${setting.interfacePrefix}/flow/rollBack`, // 回滚
  getLsdList: `${setting.interfacePrefix}/lsd/list`, // 大屏资源
};

export const downloadFile = (fileName, params) => {
  getDownload(params).then((data) => {
    if (data) {
      downloadByData(data, fileName);
    }
  });
};

export const getRemind = (params?: any) => defHttp.post<any>({ url: Api.remind, params });
export const getRecall = (params?: any) => defHttp.post<any>({ url: Api.recall, params });
export const getDistribute = (params?: any) => defHttp.post<any>({ url: Api.distribute, params });
export const getListDistributeToMe = (params?: any) =>
  defHttp.post<any>({ url: Api.listDistributeToMe, params });
export const getListMyDistribute = (params?: any) =>
  defHttp.post<any>({ url: Api.listMyDistribute, params });
export const getDelDistribute = (params?: any) =>
  defHttp.post<any>({ url: Api.delDistribute, params });

export const getResetColProps = (params?: any) =>
  defHttp.get<any>({ url: Api.resetColProps, params });

export const getFlowProcess = (params?: any) => defHttp.get<any>({ url: Api.flowProcess, params });
export const getFlowProcessScript = (params?: any) =>
  defHttp.get<any>({ url: Api.flowProcessScript, params });
export const getFlowProcessFree = (params?: any) =>
  defHttp.get<any>({ url: Api.flowProcessFree, params });

export const getDownload = (params?: any) =>
  defHttp.get<any>(
    { url: Api.download, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getFavorite = (params?: any) =>
  defHttp.post<any>(
    { url: Api.favorite, params },
    {
      isTransformResponse: false,
    },
  );

export const getUnfavorite = (params?: any) =>
  defHttp.post<any>(
    { url: Api.unfavorite, params },
    {
      isTransformResponse: false,
    },
  );
export const getDiscardFlow = (params?: any) =>
  defHttp.post<any>(
    { url: Api.discardFlow, params },
    {
      isTransformResponse: false,
    },
  );

export const getSuspendFlow = (params?: any) =>
  defHttp.post<any>(
    { url: Api.suspend, params },
    {
      isTransformResponse: false,
    },
  );
export const getResumeFlow = (params?: any) =>
  defHttp.post<any>(
    { url: Api.resume, params },
    {
      isTransformResponse: false,
    },
  );

export const getDelFlow = (params?: any) =>
  defHttp.post<any>(
    { url: Api.delFlow, params },
    {
      isTransformResponse: false,
    },
  );

export const getReturnAction = (params?: any) =>
  defHttp.get<any>(
    { url: Api.returnAction, params },
    {
      isTransformResponse: false,
    },
  );

export const getMatchBranchAndUser = (params?: any) =>
  defHttp.post<any>(
    { url: Api.matchBranchAndUser, params },
    {
      isTransformResponse: false,
    },
  );

export const submitMyFile = (params?: any) =>
  defHttp.post<any>(
    {
      url: Api.uploadMedia,
      params,
      headers: {
        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
      },
    },
    // {
    //   isTransformResponse: false,
    // },
  );

export const getFinishAction = (params?: any) =>
  defHttp.post<any>(
    {
      url: Api.finishAction,
      params,
      headers: {
        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
      },
    },
    {
      isTransformResponse: false,
    },
  );

export const getFinishActionFree = (params?: any) =>
  defHttp.post<any>(
    {
      url: Api.finishActionFree,
      params,
      headers: {
        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
      },
    },
    {
      isTransformResponse: false,
    },
  );

export const getFinishBatch = (params?: any) =>
  defHttp.post<any>(
    { url: Api.finishBatch, params },
    {
      isTransformResponse: false,
    },
  );

export const getListAttachment = (params?: any) =>
  defHttp.post<any>({ url: Api.listAttachment, params });

export const getListAttachmentDelAtt = (params?: any) =>
  defHttp.post<any>({ url: Api.listAttachmentDelAtt, params });

export const getDelAttach = (params?: any) => defHttp.post<any>({ url: Api.delAttach, params });

export const getDirTree = (params?: any) => defHttp.post<any>({ url: Api.getDirTree, params });
export const getDirTreeForQuery = (params?: any) =>
  defHttp.post<any>({ url: Api.getDirTreeForQuery, params });
export const getDirTreeOpened = (params?: any) =>
  defHttp.post<any>({ url: Api.getDirTreeOpened, params });

export const getFlowInit = (params?: any) => defHttp.post<any>({ url: Api.flowInit, params });
export const getFlowListPage = (params?: any) =>
  defHttp.post<any>(
    {
      url: Api.flowListPage,
      params,
    },
    {
      isTransformResponse: false,
    },
  );

export const getFlowList = (params?: any) =>
  defHttp.post<any>({
    url: Api.flowList,
    params,
  });

export const getFlowShow = (params?: any) =>
  defHttp.post<any>({
    url: Api.flowShow,
    params,
  });

export const getFlowShowScript = (params?: any) =>
  defHttp.post<any>({
    url: Api.flowShowScript,
    params,
  });

export const getViewShow = (params?: any) =>
  defHttp.post<any>({
    url: Api.flowView,
    params,
  });
export const getFlowShowChart = (params?: any) =>
  defHttp.get<any>({
    url: Api.flowShowChart,
    params,
  });
export const getFlowModifyTitle = (params?: any) =>
  defHttp.get<any>({
    url: Api.flowModifyTitle,
    params,
  });
export const getModifyTitle = (params?: any) =>
  defHttp.post<any>(
    {
      url: Api.modifyTitle,
      params,
    },
    {
      isTransformResponse: false,
    },
  );

export const getConds = (params?: any) =>
  defHttp.get<any>({
    url: Api.getConds,
    params,
  });
export const getFields = (params?: any) =>
  defHttp.post<any>({
    url: Api.getFields,
    params,
  });
export const getFieldsWithNest = (params?: any) =>
  defHttp.post<any>({
    url: Api.getFieldsWithNest,
    params,
  });

export const getSetConds = (params?: any) =>
  defHttp.post<any>({
    url: Api.setConds,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getColProps = (params?: any) =>
  defHttp.post<any>({
    url: Api.getColProps,
    params,
  });
export const getFlowListFields = (params?: any) =>
  defHttp.post<any>({
    url: Api.getFlowListFields,
    params,
  });
export const saveColProps = (params?: any) =>
  defHttp.post<any>({
    url: Api.saveColProps,
    params,
  });
export const saveColWidth = (params?: any) =>
  defHttp.post<any>({
    url: Api.saveColWidth,
    params,
  });
export const getExportFields = (params?: any) =>
  defHttp.post<any>({
    url: Api.getExportFields,
    params,
  });
export const getExportExcel = (params?: any) =>
  defHttp.post<any>(
    { url: Api.getExportExcel, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getExportExcelAsync = (params?: any) =>
  defHttp.post<any>({ url: Api.getExportExcelAsync, params });

export const getExportExcelProgress = (params?: any) =>
  defHttp.post<any>({ url: Api.getExportExcelProgress, params });

export const getServerInfo = (params?: any) =>
  defHttp.get<any>({
    url: Api.getServerInfo,
    params,
  });

export const getApplyProps = (params?: any) =>
  defHttp.post<any>(
    { url: Api.applyProps, params },
    {
      isTransformResponse: false,
    },
  );

export const getRunValidateScript = (params?: any) =>
  defHttp.post<any>(
    { url: Api.runValidateScript, params },
    {
      isTransformResponse: false,
    },
  );
export const getRunFinishScript = (params?: any) =>
  defHttp.post<any>(
    { url: Api.runFinishScript, params },
    {
      isTransformResponse: false,
    },
  );
export const getRunDeliverScript = (params?: any) =>
  defHttp.post<any>(
    { url: Api.runDeliverScript, params },
    {
      isTransformResponse: false,
    },
  );

export const getTestInfo = (params?: any) =>
  defHttp.post<any>({
    url: Api.getTestInfo,
    params,
  });

export const getTokenByUser = (params?: any) =>
  defHttp.post<any>({
    url: Api.getTokenByUser,
    params,
  });

export const getFlowCreateNestSheetRelated = (params?: any, query?: string) =>
  defHttp.post<any>(
    {
      url: Api.flowCreateNestSheetRelated + query,
      params,
      headers: {
        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
      },
    },
    {
      isTransformResponse: false,
    },
  );

export const getFlowUpdateNestSheetRelated = (params?: any, query?: string) =>
  defHttp.post<any>(
    {
      url: Api.flowUpdateNestSheetRelated + query,
      params,
      headers: {
        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
      },
    },
    {
      isTransformResponse: false,
    },
  );

export const getPlus = (params?: any) => defHttp.post<any>({ url: Api.plus, params });
export const getDelPlus = (params?: any) => defHttp.post<any>({ url: Api.delPlus, params });

export const getListAnnex = (params?: any) => defHttp.post<any>({ url: Api.listAnnex, params });
export const getAddReply = (params?: any) => defHttp.post<any>({ url: Api.addReply, params });
export const getDelAnnex = (params?: any) => defHttp.post<any>({ url: Api.delAnnex, params });
export const getDownloadAnnexAttachment = (params?: any) =>
  defHttp.post<any>(
    { url: Api.downloadAnnexAttachment, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getTemplates = (params?: any) => defHttp.post<any>({ url: Api.getTemplates, params });
export const convertToRedDocument = (params?: any) =>
  defHttp.post<any>({ url: Api.convertToRedDocument, params });
export const getStamps = (params?: any) => defHttp.post<any>({ url: Api.getStamps, params });
export const sealDocument = (params?: any) => defHttp.post<any>({ url: Api.sealDocument, params });
export const getPhrases = (params?: any) => defHttp.post<any>({ url: Api.getPhrases, params });
export const addPhrase = (params?: any) => defHttp.post<any>({ url: Api.addPhrase, params });
export const addFrequency = (params?: any) => defHttp.post<any>({ url: Api.addFrequency, params });
export const delPhrase = (params?: any) => defHttp.post<any>({ url: Api.delPhrase, params });
export const getFormViews = (params?: any) => defHttp.post<any>({ url: Api.getFormViews, params });
export const getActionsFinished = (params?: any) =>
  defHttp.post<any>({ url: Api.getActionsFinished, params });
export const rollBack = (params?: any) => defHttp.post<any>({ url: Api.rollBack, params });

export const getLsdList = (params?: any) => defHttp.post<any>({ url: Api.getLsdList, params });
