import { defHttp } from '/@/utils/http/axios';
import setting from '/@/settings/projectSetting';

const Api = {
  noticeListPage: `${setting.interfacePrefix}/notice/listPage`, //通知公告列表页
  noticeList: `${setting.interfacePrefix}/notice/list`, //通知公告列表
  noticeShow: `${setting.interfacePrefix}/notice/show`, //通知公告查看列表
  noticeAdd: `${setting.interfacePrefix}/notice/add`, //通知公告列表新增查询
  noticeCreate: `${setting.interfacePrefix}/notice/create`, //通知公告列表新增
  noticeEdit: `${setting.interfacePrefix}/notice/edit`, //通知公告列表查询修改内容
  noticeDel: `${setting.interfacePrefix}/notice/del`, //通知公告列表删除
  noticeSave: `${setting.interfacePrefix}/notice/save`, //通知公告列表编辑保存
  noticeDelAtt: `${setting.interfacePrefix}/notice/delAtt`, //通知公告文件删除
  noticeAttDownload: `${setting.interfacePrefix}/notice/getFile`, //通知公告文件下载

  noticeReplyList: `${setting.interfacePrefix}/notice/reply/list`, //通知公告回复列表
  noticeReplyAdd: `${setting.interfacePrefix}/notice/reply/add`, //通知公告回复新增

  noticeListImportant: `${setting.interfacePrefix}/notice/listImportant`, //重要通知公告列表
};

export const getNoticeList = (params?: any) => defHttp.get<any>({ url: Api.noticeList, params });
export const getNoticeListPage = (params?: any) =>
  defHttp.post<any>({ url: Api.noticeListPage, params });
export const getNoticeListImportant = (params?: any) =>
  defHttp.get<any>({ url: Api.noticeListImportant, params });
export const getNoticeCreate = (params?: any) =>
  defHttp.post<any>({
    url: Api.noticeCreate,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });
export const getNoticeShow = (params?: any) => defHttp.get<any>({ url: Api.noticeShow, params });
export const getNoticeAdd = (params?: any) =>
  defHttp.post<any>({
    url: Api.noticeAdd,
    params,
  });
export const getNoticeEdit = (params?: any) => defHttp.post<any>({ url: Api.noticeEdit, params });
export const getNoticeSave = (params?: any) =>
  defHttp.post<any>({
    url: Api.noticeSave,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getNoticeDel = (params?: any) => defHttp.post<any>({ url: Api.noticeDel, params });
export const getNoticeDelAtt = (params?: any) =>
  defHttp.post<any>({ url: Api.noticeDelAtt, params });
export const getNoticeAttDownload = (params?: any) =>
  defHttp.post<any>(
    { url: Api.noticeAttDownload, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );

export const getNoticeReplyList = (params?: any) =>
  defHttp.post<any>({ url: Api.noticeReplyList, params });
export const getNoticeReplyAdd = (params?: any) =>
  defHttp.post<any>({ url: Api.noticeReplyAdd, params });
