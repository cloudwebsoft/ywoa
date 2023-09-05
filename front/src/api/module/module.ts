import { defHttp } from '/@/utils/http/axios';
import { ContentTypeEnum } from '/@/enums/httpEnum';
import setting from '/@/settings/projectSetting';
const Api = {
  visualList: `${setting.interfacePrefix}/visual/list`, //智能模块列表
  visualListPage: `${setting.interfacePrefix}/visual/listPage`, //智能模块配置项
  visualAddPage: `${setting.interfacePrefix}/visual/addPage`, //智能模块新增初始化
  visualCreate: `${setting.interfacePrefix}/visual/create`, //智能模块新增保存
  visualEditPage: `${setting.interfacePrefix}/visual/editPage`, //智能模块编辑初始化
  getViewJsScript: `${setting.interfacePrefix}/visual/getViewJsScript`, //取显示规则脚本
  visualUpdate: `${setting.interfacePrefix}/visual/update`, //智能模块编辑保存
  visualShowPage: `${setting.interfacePrefix}/visual/showPage`, //智能模块查看
  visualDel: `${setting.interfacePrefix}/visual/del`, //智能模块查看
  visualBatchOp: `${setting.interfacePrefix}/visual/batchOp`, //智能模块自定义批处理
  visualModuleEditInPlace: `${setting.interfacePrefix}/visual/editInPlace`, //智能模块单元格编辑

  visualListRelatePage: `${setting.interfacePrefix}/visual/listRelatePage`, //智能模块关联模块初始化配置
  visualListRelate: `${setting.interfacePrefix}/visual/listRelate`, //智能模块关联模块列表
  visualAddRelatePage: `${setting.interfacePrefix}/visual/addRelatePage`, //智能模块关联模块新增配置
  visualCreateRelate: `${setting.interfacePrefix}/visual/createRelate`, //智能模块关联模块新增保存
  visualEditRelatePage: `${setting.interfacePrefix}/visual/editRelatePage`, //智能模块关联模块列表编辑配置
  visualUpdateRelate: `${setting.interfacePrefix}/visual/updateRelate`, //智能模块关联模块列表编辑保存
  visualShowRelatePage: `${setting.interfacePrefix}/visual/showRelatePage`, //智能模块关联模块列表查看
  visualDelRelate: `${setting.interfacePrefix}/visual/delRelate`, //智能模块关联模块列表删除
  copy: `${setting.interfacePrefix}/visual/copy`, //智能模块复制
  visualListSelPage: `${setting.interfacePrefix}/visual/listSelPage`, //表单域选择模块列表配置
  visualListNestSelPage: `${setting.interfacePrefix}/visual/listNestSel`, //嵌套表格选择模块列表配置

  visualExportExcel: `${setting.interfacePrefix}/visual/exportExcel`, //智能模块导出Excel

  visualExportExcelAsync: `${setting.interfacePrefix}/visual/exportExcelAsync`, //异步导出Excel

  getExportExcelProgress: `${setting.interfacePrefix}/visual/getExportExcelProgress`, //异步导出Excel的进度
  downloadExportExcelAsync: `${setting.interfacePrefix}/visual/downloadExportExcelAsync`, //下载异步导出的Excel文件

  visualImportExcel: `${setting.interfacePrefix}/visual/importExcel`, //智能模块导入
  visualExportWord: `${setting.interfacePrefix}/visual/exportWord`, //智能模块导出word
  visualDownloadZip: `${setting.interfacePrefix}/visual/downloadZipFile`, //智能模块压缩下载
  visualExportExcelRelate: `${setting.interfacePrefix}/visual/exportExcelRelate`, //智能模块关联模块导出

  visualImportExcelNest: `${setting.interfacePrefix}/visual/importExcelNest`, //嵌套表格导入

  visualDownloadExcelTemplForNest: `${setting.interfacePrefix}/visual/downloadExcelTemplForNest`, //下载嵌套表格Excel模板

  visualDownloadExcelTempl: `${setting.interfacePrefix}/visual/downloadExcelTempl`, //下载Excel模板

  itemsForListModuleSel: `${setting.interfacePrefix}/visual/getItemsForListModuleSel`, //当表单域选择宏控件选择后获取相关项

  selBatchForNest: `${setting.interfacePrefix}/visual/selBatchForNest`, //嵌套表格宏控件拉单

  visualListAtt: `${setting.interfacePrefix}/visual/listAtt`, //附件列表
  visualDelAttach: `${setting.interfacePrefix}/visual/delAttach`, //附件列表删除
  visualDownload: `${setting.interfacePrefix}/visual/download`, //附件列表下载

  visualListCalendar: `${setting.interfacePrefix}/visual/listCalendar`, //智能模块日历

  getModuleTree: `${setting.interfacePrefix}/visual/getModuleTree`, //智能模块树

  getModuleTreeNodePriv: `${setting.interfacePrefix}/visual/tree/priv/getModuleTreeNodePriv`, //用户在智能模块树节点上的权限
  getPrivList: `${setting.interfacePrefix}/visual/tree/priv/list`, //基础数据树形的权限控制器列表
  getPrivListCreate: `${setting.interfacePrefix}/visual/tree/priv/create`, //基础数据树形的权限控制器列表创建
  getPrivListUpdate: `${setting.interfacePrefix}/visual/tree/priv/update`, //基础数据树形的权限控制器列表修改
  getPrivListDel: `${setting.interfacePrefix}/visual/tree/priv/del`, //基础数据树形的权限控制器列表
  isManagerOfNode: `${setting.interfacePrefix}/visual/tree/priv/isManagerOfNode`, //基础数据树形节点上是否有管理权限
  getFieldsWithNest: `${setting.interfacePrefix}/visual/getFieldsWithNest`, //取模块表单及其嵌套表的字段
  getFieldsWithNestForExport: `${setting.interfacePrefix}/visual/getFieldsWithNestForExport`, //取选择列导出时模块表单及其嵌套表的字段

  getExportFields: `${setting.interfacePrefix}/visual/getExportFields`, //取模块中的导出字段配置
  getFrontColProps: `${setting.interfacePrefix}/visual/getFrontColProps`, //取模块中前端定义的列属性

  getBackColProps: `${setting.interfacePrefix}/visual/getBackColProps`, //取模块中后端定义的列属性
  updateFrontColProps: `${setting.interfacePrefix}/visual/updateFrontColProps`, //保存模块前台定义的列属性
};

export const getItemsForListModuleSel = (params?: any) =>
  defHttp.post<any>(
    { url: Api.itemsForListModuleSel, params },
    {
      isTransformResponse: false,
    },
  );

export const getSelBatchForNest = (params?: any) =>
  defHttp.post<any>(
    { url: Api.selBatchForNest, params },
    {
      isTransformResponse: false,
    },
  );

export const getVisualList = (params?: any) => defHttp.post<any>({ url: Api.visualList, params });

export const getVisualListPage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualListPage, params });

export const getVisualAddPage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualAddPage, params });

export const getVisualCreate = (params?: any, query?: string) =>
  defHttp.post<any>({
    url: Api.visualCreate + query,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getVisualEditPage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualEditPage, params });
export const getViewJsScript = (params?: any) =>
  defHttp.get<any>({ url: Api.getViewJsScript, params });

export const getVisualUpdate = (params?: any, query?: string) =>
  defHttp.post<any>({
    url: Api.visualUpdate + query,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getVisualShowPage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualShowPage, params });

export const getVisualDel = (params?: any) => defHttp.post<any>({ url: Api.visualDel, params });
export const getVisualBatchOp = (params?: any) =>
  defHttp.post<any>({ url: Api.visualBatchOp, params });

export const getVisualModuleEditInPlace = (params?: any) =>
  defHttp.post<any>({ url: Api.visualModuleEditInPlace, params });

export const getVisualListRelatePage = (params?: any) =>
  defHttp.post<any>({ url: Api.visualListRelatePage, params });

export const getVisualListRelate = (params?: any) =>
  defHttp.post<any>({ url: Api.visualListRelate, params });

export const getVisualAddRelatePage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualAddRelatePage, params });

export const getVisualCreateRelate = (params?: any, query?: string) =>
  defHttp.post<any>({
    url: Api.visualCreateRelate + query,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getVisualEditRelatePage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualEditRelatePage, params });

export const getVisualUpdateRelate = (params?: any, query?: string) =>
  defHttp.post<any>({
    url: Api.visualUpdateRelate + query,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getVisualShowRelatePage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualShowRelatePage, params });

export const getVisualDelRelate = (params?: any) =>
  defHttp.post<any>({ url: Api.visualDelRelate, params });

export const getVisualCopy = (params?: any) => defHttp.post<any>({ url: Api.copy, params });

export const getVisualListSelPage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualListSelPage, params });

export const getVisualListNestSelPage = (params?: any) =>
  defHttp.get<any>({ url: Api.visualListNestSelPage, params });

export const getVisualExportWord = (params?: any) =>
  defHttp.get<any>(
    { url: Api.visualExportWord, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getVisualDownloadZip = (params?: any) =>
  defHttp.get<any>(
    { url: Api.visualDownloadZip, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getVisualExportExcel = (params?: any) =>
  defHttp.get<any>(
    { url: Api.visualExportExcel, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const downloadExportExcelAsync = (params?: any) =>
  defHttp.post<any>(
    { url: Api.downloadExportExcelAsync, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getVisualExportExcelAsync = (params?: any) =>
  defHttp.post<any>({ url: Api.visualExportExcelAsync, params });

export const getExportExcelProgress = (params?: any) =>
  defHttp.post<any>({ url: Api.getExportExcelProgress, params });

export const getVisualImportExcel = (params?: any) =>
  defHttp.post<any>(
    {
      url: Api.visualImportExcel,
      params,
      headers: {
        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
      },
    },
    {
      isTransformResponse: false,
    },
  );

export const getVisualExportExcelRelate = (params?: any) =>
  defHttp.get<any>(
    { url: Api.visualExportExcelRelate, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getVisualImportExcelNest = (params?: any, query?: string) =>
  defHttp.post<any>({
    url: Api.visualImportExcelNest + query,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getVisualDownloadExcelTemplForNest = (params?: any) =>
  defHttp.get<any>(
    { url: Api.visualDownloadExcelTemplForNest, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getVisualDownloadExcelTempl = (params?: any) =>
  defHttp.get<any>(
    { url: Api.visualDownloadExcelTempl, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getVisualListAtt = (params?: any) =>
  defHttp.post<any>({ url: Api.visualListAtt, params });

export const getVisualDelAttach = (params?: any) =>
  defHttp.post<any>({ url: Api.visualDelAttach, params });

export const getVisualDownload = (params?: any) =>
  defHttp.post<any>(
    { url: Api.visualDownload, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getvisualListCalendar = (params?: any) =>
  defHttp.post<any>({ url: Api.visualListCalendar, params });

export const getModuleTree = (params?: any) => defHttp.get<any>({ url: Api.getModuleTree, params });
export const getModuleTreeNodePriv = (params?: any) =>
  defHttp.post<any>({ url: Api.getModuleTreeNodePriv, params });
// 树形节点上的权限列表
export const getPrivList = (params?: any) => defHttp.post<any>({ url: Api.getPrivList, params });
export const getPrivListCreate = (params?: any) =>
  defHttp.post<any>({
    url: Api.getPrivListCreate,
    params,
    headers: { 'Content-Type': ContentTypeEnum.JSON },
  });
export const getPrivListUpdate = (params?: any) =>
  defHttp.post<any>({
    url: Api.getPrivListUpdate,
    params,
    headers: { 'Content-Type': ContentTypeEnum.JSON },
  });
export const getPrivListDel = (params?: any) =>
  defHttp.post<any>({
    url: Api.getPrivListDel,
    params,
  });

export const getIsManagerOfNode = (params?: any) =>
  defHttp.post<any>({
    url: Api.isManagerOfNode,
    params,
  });

export const getFieldsWithNest = (params?: any) =>
  defHttp.post<any>({ url: Api.getFieldsWithNest, params });
export const getFieldsWithNestForExport = (params?: any) =>
  defHttp.post<any>({ url: Api.getFieldsWithNestForExport, params });

export const getExportFields = (params?: any) =>
  defHttp.post<any>({ url: Api.getExportFields, params });

export const getFrontColProps = (params?: any) =>
  defHttp.post<any>({ url: Api.getFrontColProps, params });

export const getBackColProps = (params?: any) =>
  defHttp.post<any>({ url: Api.getBackColProps, params });

export const updateFrontColProps = (params?: any) =>
  defHttp.post<any>({ url: Api.updateFrontColProps, params });
