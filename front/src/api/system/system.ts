import {
  BasicDataKindPageParams,
  MenuListGetResultModel,
  BasicDataKindPageListGetResultModel,
  BasicDataPageListGetResultModel,
  BasicDataPageParams,
} from './model/systemModel';
import { defHttp } from '/@/utils/http/axios';

import { ContentTypeEnum } from '/@/enums/httpEnum';
import setting from '/@/settings/projectSetting';

const Api = {
  menuItem: `${setting.interfacePrefix}/home`, //侧边栏目录
  myInfo: `${setting.interfacePrefix}/getMyInfo`, //取用户信息
  getCode: `${setting.interfacePrefix}/admin/getCode`, //自定义编码
  menuList: `${setting.interfacePrefix}/admin/getMenuTree`, //菜单列表
  menuListAdd: `${setting.interfacePrefix}/admin/createMenuItem.do`, //菜单新增
  menuListEdit: `${setting.interfacePrefix}/admin/updateMenuItem`, //菜单编辑
  menuListDelete: `${setting.interfacePrefix}/admin/delMenuItem.do`, //菜单删除
  moveMenu: `${setting.interfacePrefix}/admin/moveMenuItem`, //菜单移动
  editMenu: `${setting.interfacePrefix}/admin/getMenuItem`, //菜单信息

  flowDirTree: `${setting.interfacePrefix}/flow/getDirTree`, //获取目录树
  modulesAll: `${setting.interfacePrefix}/visual/listAll`, //获取全部模块
  getFieldsByModuleCode: `${setting.interfacePrefix}/form/getFieldsByModuleCode`, //获取全部模块
  basicList: `${setting.interfacePrefix}/basicdata/listKind`, //获取基础数据

  listRole: `${setting.interfacePrefix}/admin/listRole`, //角色list
  getOptions: `${setting.interfacePrefix}/basicdata/getOptions`, //角色类别
  getRoleUnit: `${setting.interfacePrefix}/admin/getUnit`, //角色单位
  createRole: `${setting.interfacePrefix}/admin/createRole`, //角色新增
  updateRole: `${setting.interfacePrefix}/admin/updateRole`, //角色编辑
  delRole: `${setting.interfacePrefix}/admin/delRole`, //角色删除
  listUserOfRole: `${setting.interfacePrefix}/admin/listUserOfRole`, //获取角色用户
  roleDepartmentListPage: `${setting.interfacePrefix}//admin/roleDepartmentList`, //角色组织
  createRoleDepartmentList: `${setting.interfacePrefix}/admin/create`, //角色组织新增
  delRoleDepartmentList: `${setting.interfacePrefix}/admin/del`, //角色组织删除
  changeRoleDepartmentStatus: `${setting.interfacePrefix}/admin/changeRoleDepartmentStatus`, //角色组织包含与不包含

  setUserRoleOfDept: `${setting.interfacePrefix}//admin/setUserRoleOfDept`, //设置角色用户部门
  rolePostList: `${setting.interfacePrefix}/admin/rolePostList`, //设置角色职位
  rolePostUpdate: `${setting.interfacePrefix}/admin/rolePostUpdate`, //设置角色职位新增
  rolePostDel: `${setting.interfacePrefix}/admin/rolePostDel`, //设置角色职位删除
  rolePostTransferPage: `${setting.interfacePrefix}/admin/rolePostTransferPage`, //设置角色职位新增穿梭框

  changeRoleStatus: `${setting.interfacePrefix}/admin/changeRoleStatus`, //改变状态
  changeRoleOrder: `${setting.interfacePrefix}/admin/changeRoleOrder`, //改变角色序号
  addRoleUser: `${setting.interfacePrefix}/admin/addRoleUser`, //角色新增用户
  delRoleUserBatch: `${setting.interfacePrefix}/admin/delRoleUserBatch`, //角色删除用户 批量
  rolePriv: `${setting.interfacePrefix}/admin/rolePriv`, //角色权限
  setRolePrivs: `${setting.interfacePrefix}/admin/setRolePrivs`, //设置角色权限
  roleMenu: `${setting.interfacePrefix}/admin/roleMenu`, //角色菜单
  setMenuPriv: `${setting.interfacePrefix}/admin/setMenuPriv`, //角色菜单设置
  roleAdminDept: `${setting.interfacePrefix}/admin/roleAdminDept`, //角色部门查询
  setRoleAdminDept: `${setting.interfacePrefix}/admin/setRoleAdminDept`, //角色部门设置

  userMultiSel: `${setting.interfacePrefix}/userMultiSel`, //获取用户组件数据
  initUsers: `${setting.interfacePrefix}/user/initUsers`, //初始化用户
  getDeptUsers: `${setting.interfacePrefix}/user/getDeptUsers`, //新增用户页点击部门查询用户
  getRoleUsers: `${setting.interfacePrefix}/user/getRoleUsers`, //新增用户页点击角色查询用户
  getGroupUsers: `${setting.interfacePrefix}/user/getGroupUsers`, //新增用户页点击用户组查询用户
  getDepartments: `${setting.interfacePrefix}/department/getDepartments`, //部门
  getNewCode: `${setting.interfacePrefix}/department/getNewCode`, //部门新增时取得编码
  departmentCreate: `${setting.interfacePrefix}/department/create`, //部门新增
  departmentEdit: `${setting.interfacePrefix}/department/edit`, //部门编辑
  departmentSave: `${setting.interfacePrefix}/department/save`, //部门编辑
  departmentDel: `${setting.interfacePrefix}/department/del`, //部门删除
  departmentMove: `${setting.interfacePrefix}/department/move`, //部门删除

  unitTree: `${setting.interfacePrefix}/department/getUnitTree`, //单位树

  listGroup: `${setting.interfacePrefix}/admin/listGroup`, //用户组列表
  delGroup: `${setting.interfacePrefix}/admin/delGroup`, //用户组删除 groupCode
  createGroup: `${setting.interfacePrefix}/admin/createGroup`, //用户组列表新增
  updateGroup: `${setting.interfacePrefix}/admin/updateGroup`, //用户组列表编辑
  delGroupUserBatch: `${setting.interfacePrefix}/admin/delGroupUserBatch`, //用户组列表编辑用户表删除
  groupPriv: `${setting.interfacePrefix}/admin/groupPriv`, //用户组列表编辑权限表设置权限
  setGroupPrivs: `${setting.interfacePrefix}/admin/setGroupPrivs`, //用户组列表编辑用户表设置权限
  addGroupUser: `${setting.interfacePrefix}/admin/addGroupUser`, //用户组列表编辑用户表新增
  listUserOfGroup: `${setting.interfacePrefix}/admin/listUserOfGroup`, //用户组列表编辑用户表

  listBasicDataKind: `${setting.interfacePrefix}/basicdata/listKind`, //基础数据类型列表
  createBasicDataKind: `${setting.interfacePrefix}/basicdata/createKind`, //创建基础数据类型
  updateBasicDataKind: `${setting.interfacePrefix}/basicdata/updateKind`, // 修改基础数据类型
  delBasicDataKind: `${setting.interfacePrefix}/basicdata/delKind`, // 删除基础数据类型
  changeBasicKindOrder: `${setting.interfacePrefix}/basicdata/changeKindOrder`, // 修改基础数据类型序号

  listBasicData: `${setting.interfacePrefix}/basicdata/list`, // 基础数据列表
  changeBasicOrder: `${setting.interfacePrefix}/basicdata/changeOrder`, // 修改基础数据序号
  createBasicData: `${setting.interfacePrefix}/basicdata/create`, // 修改基础数据
  updateBasicData: `${setting.interfacePrefix}/basicdata/update`, // 修改基础数据
  delBasicData: `${setting.interfacePrefix}/basicdata/delete`, //   删除基础数据
  getBasicOptions: `${setting.interfacePrefix}/basicdata/getOptions`, // 取得基础数据的选项
  createBasicOption: `${setting.interfacePrefix}/basicdata/createOption`, // 创建基础数据的选项
  updateBasicOption: `${setting.interfacePrefix}/basicdata/updateOption`, // 修改基础数据的选项
  delBasicOption: `${setting.interfacePrefix}/basicdata/delOption`, //  删除基础数据的选项
  getBasicTree: `${setting.interfacePrefix}/basicdata/getTree`, // 取得基础数据的树形数据
  getBasicCreateNode: `${setting.interfacePrefix}/basicdata/createNode`, // 基础数据的树形数据新增
  getBasicUpdateNode: `${setting.interfacePrefix}/basicdata/updateNode`, // 基础数据的树形数据编辑
  getBasicDelNode: `${setting.interfacePrefix}/basicdata/delNode`, // 基础数据的树形数据删除
  getBasicMoveNode: `${setting.interfacePrefix}/basicdata/moveNode`, // 基础数据的树形数据拖动
  getBasicOpenNode: `${setting.interfacePrefix}/basicdata/openNode`, // 基础数据的树形数据启用
  getBasicCloseNode: `${setting.interfacePrefix}/basicdata/closeNode`, // 基础数据的树形数据停用

  listPriv: `${setting.interfacePrefix}/admin/listPriv`, //管理权限列表
  createPriv: `${setting.interfacePrefix}/admin/createPriv`, //管理权限新增
  setPrivs: `${setting.interfacePrefix}/admin/setPrivsList`, //管理权限编辑

  getAccountList: `${setting.interfacePrefix}/admin/getAccountList`, //工号管理列表
  createAccount: `${setting.interfacePrefix}/admin/createAccount`, //管理权限新增
  updateAccount: `${setting.interfacePrefix}/admin/updateAccount`, //管理权限编辑
  delAccount: `${setting.interfacePrefix}/admin/delAccount`, //管理权限删除

  userList: `${setting.interfacePrefix}/user/list`, //组织管理人员列表
  userCreate: `${setting.interfacePrefix}/user/create`, //组织管理人员新增
  userUpdate: `${setting.interfacePrefix}/user/update`, //组织管理人员列表编辑
  userDelUsers: `${setting.interfacePrefix}/user/delUsers`, //组织管理人员列表删除 params:ids
  editUser: `${setting.interfacePrefix}/admin/organize/editUser`, //组织管理人员列表编辑调用明细
  changeDepts: `${setting.interfacePrefix}//user/changeDepts`, //组织管理人员列表调出
  transferUsers: `${setting.interfacePrefix}//user/transferUsers`, //组织管理人员列表调入
  enableBatch: `${setting.interfacePrefix}//user/enableBatch`, //组织管理人员列表启用
  leaveOffBatch: `${setting.interfacePrefix}/user/leaveOffBatch`, //组织管理人员列表停用
  stopStartIsValid: `${setting.interfacePrefix}/user/stopStartIsValid`, //组织管理人员列表状态改变
  getMmaRaw: `${setting.interfacePrefix}/user/getMmaRaw`, // 取得经Aes加密的用户密码
  userCheckMobile: `${setting.interfacePrefix}/user/checkMobileRepeat`, //组织管理新增检验手机号唯一性 params: mobile
  userCheckPersonNo: `${setting.interfacePrefix}/user/checkPersonNoRepeat`, //组织管理新增检验人员编号唯一性 params: personNo
  userCheckPwd: `${setting.interfacePrefix}/user/checkPwd`, //组织管理新增检验密码唯一性 params:  op: "checkPwd",pwd: pwd
  userCheckUserName: `${setting.interfacePrefix}/user/checkUserName`, //组织管理新增检验账号 params: userName

  roleMultilSel: `${setting.interfacePrefix}/roleMultilSel`, //组织管理人员列表角色选择表
  userSetRole: `${setting.interfacePrefix}/admin/organize/userSetRole`, //组织管理人员列表角色查询
  setRoleOfUser: `${setting.interfacePrefix}/user/setRoleOfUser`, //组织管理人员列表角色设置

  userPriv: `${setting.interfacePrefix}/admin/organize/userPriv`, //组织管理人员列表权限列表
  setUserPrivs: `${setting.interfacePrefix}/user/setPrivs`, //组织管理人员列表权限列表保存

  userAdminDept: `${setting.interfacePrefix}/admin/organize/userAdminDept`, //组织管理人员列表权部门
  setUserAdminDept: `${setting.interfacePrefix}/user/setUserAdminDept`, //组织管理人员列表权部门保存
  exportUser: `${setting.interfacePrefix}/admin/exportUser`, //组织管理人员列表导出
  userImportFinish: `${setting.interfacePrefix}/admin/organize/userImportFinish`, //组织管理人员列表导入
  userImportConfirm: `${setting.interfacePrefix}/admin/organize/userImportConfirm`, //组织管理人员列表导入确认

  postList: `${setting.interfacePrefix}/admin/organize/postList`, //组织管理职位列表
  postCreate: `${setting.interfacePrefix}/admin/organize/postCreate`, //组织管理职位列表新增
  postEditPage: `${setting.interfacePrefix}/admin/organize/postEditPage`, //组织管理职位列表编辑
  postUpdate: `${setting.interfacePrefix}/admin/organize/postUpdate`, //组织管理职位列表保存
  postDel: `${setting.interfacePrefix}/admin/organize/postDel`, //组织管理职位列表删除
  postUserTransferPage: `${setting.interfacePrefix}/admin/organize/postUserTransferPage`, //组织管理职位成员管理列表
  postUserUpdate: `${setting.interfacePrefix}/admin/organize/postUserUpdate`, //组织管理职位列表成员管理列表保存

  showImg: `${setting.interfacePrefix}/showImg`, //图片请求
  showImgInJar: `${setting.interfacePrefix}/showImgInJar`, //jar包中的图片请求

  upeditUser: `${setting.interfacePrefix}/user/editUser`, //个人信息修改初始化
  updateMyInfo: `${setting.interfacePrefix}/user/updateMyInfo`, //个人信息修改保存
  resetPortrait: `${setting.interfacePrefix}/user/resetPortrait`, //重置头像

  getConfigInfo: `${setting.interfacePrefix}/config/getConfigInfo`, //通用配置

  getUiSetup: `${setting.interfacePrefix}/setup/getUiSetup`, //获取ui风格配置

  getUpdateUiSetup: `${setting.interfacePrefix}/setup/updateUiSetup`, //更新ui风格配置

  getUpdateInitPwd: `${setting.interfacePrefix}/user/updateInitPwd`, //修改初始密码
  getResetPwdSendLink: `${setting.interfacePrefix}/public/resetPwdSendLink`, //重置密码
  changePwd: `${setting.interfacePrefix}/user/changePwd`, //修改密码

  getSwitchRole: `${setting.interfacePrefix}/admin/switchRole`, //获取切换角色列表
  getSwitchDept: `${setting.interfacePrefix}/admin/switchDept`, //获取切换部门列表

  getQrCodeForLogin: `${setting.interfacePrefix}/public/getQrCodeForLogin`, //获取扫码登录二维码
  getQrCodeLoginCheck: `${setting.interfacePrefix}/public/qrCodeLoginCheck`, //检查二维码登录

  getPortalList: `${setting.interfacePrefix}/portal/list`, //门户列表
  getPortalCreate: `${setting.interfacePrefix}/portal/create`, //门户新增
  getPortalUpdate: `${setting.interfacePrefix}/portal/update`, //门户编辑
  getPortalCopy: `${setting.interfacePrefix}/portal/copy`, //门户编辑
  getPortalSort: `${setting.interfacePrefix}/portal/sort`, //门户排序
  getPortalDel: `${setting.interfacePrefix}/portal/del`, //门户删除
  getPortalListByUser: `${setting.interfacePrefix}/portal/listPortal`, //用户可见的门户列表

  getPortalListForMenu: `${setting.interfacePrefix}/portal/listPortalForMenu`, //用于菜单的门户列表
  getPortalNames: `${setting.interfacePrefix}/portal/listPortalNames`, //用于菜单的门户列表
  getApplications: `${setting.interfacePrefix}/admin/getApplications`, //用于菜单的应用列表

  getSetup: `${setting.interfacePrefix}/portal/getSetup`, //门户设计列表
  getUpdateSetup: `${setting.interfacePrefix}/portal/updateSetup`, //门户设计编辑

  getCard: `${setting.interfacePrefix}/portal/getCard`, //取得卡片
  getListAllCard: `${setting.interfacePrefix}/portal/listAllCard`, //取得全部卡片
  getListCard: `${setting.interfacePrefix}/portal/listCard`, //首页顶部四个块
  getListCardByModule: `${setting.interfacePrefix}/portal/listCardByModule`, //首页顶部卡片
  getListCardByApplication: `${setting.interfacePrefix}/portal/listCardByApplication`, //应用型卡片

  getListCarouselPicture: `${setting.interfacePrefix}/portal/listCarouselPicture`, //图片轮播列表
  getCarouselPictureInfo: `${setting.interfacePrefix}/portal/getCarouselPictureInfo`, //图片轮播内容

  getChartTypes: `${setting.interfacePrefix}/portal/getChartTypes`, //获取图形数据

  getBar: `${setting.interfacePrefix}/chart/getBar`, //获取柱状图
  getPie: `${setting.interfacePrefix}/chart/getPie`, //获取饼图
  getGauge: `${setting.interfacePrefix}/chart/getGauge`, //获取仪表盘
  getLine: `${setting.interfacePrefix}/chart/getLine`, //获取折线图
  getFunnel: `${setting.interfacePrefix}/chart/getFunnel`, //获取漏斗图
  getRadar: `${setting.interfacePrefix}/chart/getRadar`, //获取雷达图

  getDirNames: `${setting.interfacePrefix}/doc/getDirNames`, //获取文件柜tab名称
  getListDoc: `${setting.interfacePrefix}/doc/listDoc`, //获取文件柜列表
  listImageByDirCode: `${setting.interfacePrefix}/doc/listImageByDirCode`, //获取文件柜某目录下的图片列表
  getCheckPwd: `${setting.interfacePrefix}/flow/macro/checkPwd`, //验证密码
  jump: `${setting.interfacePrefix}/public/jump`, //跳转
  synAll: `${setting.interfacePrefix}/user/synAll`, //跳转
};

export const getMyInfo = (params?: any) =>
  defHttp.get<MenuListGetResultModel>({ url: Api.myInfo, params });
export const getMenuItem = (params?: any) =>
  defHttp.get<MenuListGetResultModel>({ url: Api.menuItem, params });
export const getCode = () => defHttp.get<any>({ url: Api.getCode });
export const getMenuList = (params?: any) =>
  defHttp.get<MenuListGetResultModel>({ url: Api.menuList, params });

export const setMenuListAdd = (params?: any) => {
  return defHttp.get<MenuListGetResultModel>({ url: Api.menuListAdd, params });
};

export const setMenuListEdit = (params?: any) => {
  return defHttp.post<MenuListGetResultModel>({ url: Api.menuListEdit, params });
};

export const setMenuListDelete = (params?: any) => {
  return defHttp.post({ url: Api.menuListDelete, params });
};
export const setMoveMenu = (params?: any) => {
  return defHttp.post({ url: Api.moveMenu, params });
};
export const setEditMenu = (params?: any) => {
  return defHttp.get<MenuListGetResultModel>({ url: Api.editMenu, params });
};

export const getFlowDirTree = (params?: any) => defHttp.post({ url: Api.flowDirTree, params });

export const getModulesAll = (params?: any) => defHttp.post({ url: Api.modulesAll, params });

export const getFieldsByModuleCode = (params?: any) =>
  defHttp.post({ url: Api.getFieldsByModuleCode, params });

export const getBasicList = (params?: any) => defHttp.post({ url: Api.basicList, params });

export const getListRole = (params?) => defHttp.get<any>({ url: Api.listRole, params });

export const getOptions = (params?: any) => defHttp.get<any>({ url: Api.getOptions, params });

export const getRoleUnit = (params?: any) => defHttp.get<any>({ url: Api.getRoleUnit, params });

export const getCreateRole = (params?: any) => defHttp.get<any>({ url: Api.createRole, params });

export const getUpdateRole = (params?: any) => defHttp.post<any>({ url: Api.updateRole, params });

export const getDelRole = (params?: any) => defHttp.post<any>({ url: Api.delRole, params });

export const getChangeRoleStatus = (params?: any) =>
  defHttp.post<any>({ url: Api.changeRoleStatus, params });

export const getChangeRoleOrder = (params?: any) =>
  defHttp.post<any>({ url: Api.changeRoleOrder, params });

export const getListUserOfRole = (params?: any) =>
  defHttp.get<any>({ url: Api.listUserOfRole, params });

export const getRoleDepartmentListPage = (params?: any) =>
  defHttp.get<any>({ url: Api.roleDepartmentListPage, params });

export const getCreateRoleDepartmentList = (params?: any) =>
  defHttp.post<any>({ url: Api.createRoleDepartmentList, params });

export const getDelRoleDepartmentList = (params?: any) =>
  defHttp.post<any>({ url: Api.delRoleDepartmentList, params });

export const getChangeRoleDepartmentStatus = (params?: any) =>
  defHttp.get<any>({ url: Api.changeRoleDepartmentStatus, params });

export const getRolePriv = (params?: any) => defHttp.get<any>({ url: Api.rolePriv, params });

export const getSetRolePrivs = (params?: any) =>
  defHttp.post<any>({ url: Api.setRolePrivs, params });

export const getRoleMenu = (params?: any) => defHttp.get<any>({ url: Api.roleMenu, params });
export const getSetMenuPriv = (params?: any) => defHttp.post<any>({ url: Api.setMenuPriv, params });

export const getRoleAdminDept = (params?: any) =>
  defHttp.get<any>({ url: Api.roleAdminDept, params });

export const getSetRoleAdminDept = (params?: any) =>
  defHttp.post<any>({ url: Api.setRoleAdminDept, params });

export const getSetUserRoleOfDept = (params?: any) =>
  defHttp.post<any>({ url: Api.setUserRoleOfDept, params });

export const getAddRoleUser = (params?: any) => defHttp.post<any>({ url: Api.addRoleUser, params });

export const getDelRoleUserBatch = (params?: any) =>
  defHttp.post<any>({ url: Api.delRoleUserBatch, params });

export const getRolePostList = (params?: any) =>
  defHttp.get<any>({ url: Api.rolePostList, params });

export const getRolePostUpdate = (params?: any) =>
  defHttp.post<any>({ url: Api.rolePostUpdate, params });

export const getRolePostDel = (params?: any) => defHttp.post<any>({ url: Api.rolePostDel, params });

export const getRolePostTransferPage = (params?: any) =>
  defHttp.get<any>({ url: Api.rolePostTransferPage, params });

export const getUserMultiSel = (params?: any) =>
  defHttp.get<any>({ url: Api.userMultiSel, params });
export const getInitUsers = (params?: any) => defHttp.get<any>({ url: Api.initUsers, params });

export const getDeptUsers = (params?: any) => defHttp.get<any>({ url: Api.getDeptUsers, params });
export const getRoleUsers = (params?: any) => defHttp.get<any>({ url: Api.getRoleUsers, params });
export const getGroupUsers = (params?: any) => defHttp.get<any>({ url: Api.getGroupUsers, params });

export const getDepartment = (params?: any) =>
  defHttp.get<any>({ url: Api.getDepartments, params });
export const getDepartmentCreate = (params?: any) =>
  defHttp.post<any>({ url: Api.departmentCreate, params });
export const getDepartmentEdit = (params?: any) =>
  defHttp.post<any>({ url: Api.departmentEdit, params });
export const getNewCode = (params?: any) => defHttp.post<any>({ url: Api.getNewCode, params });
export const getDepartmentSave = (params?: any) =>
  defHttp.post<any>({
    url: Api.departmentSave,
    params,
    headers: { 'Content-Type': ContentTypeEnum.JSON },
  });

export const getDepartmentDel = (params?: any) =>
  defHttp.post<any>({ url: Api.departmentDel, params });

export const getDepartmentMove = (params?: any) =>
  defHttp.post<any>({ url: Api.departmentMove, params });

export const getUnitTree = (params?: any) => defHttp.post<any>({ url: Api.unitTree, params });

export const getListGroup = (params?: any) => defHttp.get<any>({ url: Api.listGroup, params });
export const getDelGroup = (params?: any) => defHttp.post<any>({ url: Api.delGroup, params });
export const getCreateGroup = (params?: any) => defHttp.post<any>({ url: Api.createGroup, params });
export const getUpdateGroup = (params?: any) => defHttp.post<any>({ url: Api.updateGroup, params });

export const getListUserOfGroup = (params?: any) =>
  defHttp.get<any>({ url: Api.listUserOfGroup, params });
export const getDelGroupUserBatch = (params?: any) =>
  defHttp.post<any>({ url: Api.delGroupUserBatch, params });

export const getGroupPriv = (params?: any) => defHttp.get<any>({ url: Api.groupPriv, params });
export const getSetGroupPrivs = (params?: any) =>
  defHttp.post<any>({ url: Api.setGroupPrivs, params });
export const getAddGroupUser = (params?: any) =>
  defHttp.post<any>({ url: Api.addGroupUser, params });

export const getListBasicDataKind = (params?: BasicDataKindPageParams) =>
  defHttp.get<BasicDataKindPageListGetResultModel>({ url: Api.listBasicDataKind, params });
export const getCreateBasicKind = (params?: BasicDataKindPageParams) =>
  defHttp.post<any>({ url: Api.createBasicDataKind, params });
export const getUpdateBasicKind = (params?: BasicDataKindPageParams) =>
  defHttp.post<any>({ url: Api.updateBasicDataKind, params });
export const getDelBasicKind = (params?: BasicDataKindPageParams) =>
  defHttp.post<any>({ url: Api.delBasicDataKind, params });
export const getChangeBasicKindOrder = (params?: BasicDataKindPageParams) =>
  defHttp.post<any>({ url: Api.changeBasicKindOrder, params });

export const getListBasicData = (params?: BasicDataPageParams) =>
  defHttp.get<BasicDataPageListGetResultModel>({ url: Api.listBasicData, params });
export const getChangeBasicOrder = (params?: BasicDataPageParams) =>
  defHttp.post<any>({ url: Api.changeBasicOrder, params });
export const getCreateBasic = (params?: BasicDataPageParams) =>
  defHttp.post<any>({ url: Api.createBasicData, params });
export const getUpdateBasic = (params?: BasicDataPageParams) =>
  defHttp.post<any>({ url: Api.updateBasicData, params });
export const getDelBasic = (params?: BasicDataPageParams) =>
  defHttp.post<any>({ url: Api.delBasicData, params });
export const getBasicOptions = (params?: BasicDataPageParams) =>
  defHttp.get<any>({ url: Api.getBasicOptions, params });
export const getCreateBasicOption = (params?: BasicDataPageParams) =>
  defHttp.post<any>({ url: Api.createBasicOption, params });
export const getUpdateBasicOption = (params?: BasicDataPageParams) =>
  defHttp.post<any>({ url: Api.updateBasicOption, params });
export const getDelBasicOption = (params?: any) =>
  defHttp.post<any>({ url: Api.delBasicOption, params });
export const getBasicTree = (params?: any) => defHttp.get<any>({ url: Api.getBasicTree, params });

export const getListPriv = (params?: any) => defHttp.get<any>({ url: Api.listPriv, params });
export const getCreatePriv = (params?: any) => defHttp.post<any>({ url: Api.createPriv, params });
export const getSetPrivs = (params?: any) => defHttp.post<any>({ url: Api.setPrivs, params });

export const getAccountList = (params?: any) =>
  defHttp.get<any>({ url: Api.getAccountList, params });
export const getCreateAccount = (params?: any) =>
  defHttp.post<any>({
    url: Api.createAccount,
    params,
  });
export const getUpdateAccount = (params?: any) =>
  defHttp.post<any>({
    url: Api.updateAccount,
    params,
    // headers: {
    //   'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    // },
  });
export const getDelAccount = (params?: any) => defHttp.post<any>({ url: Api.delAccount, params });

export const getUerList = (params?: any) => defHttp.post<any>({ url: Api.userList, params });
export const getUserCreate = (params?: any) =>
  defHttp.post<any>({
    url: Api.userCreate,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });
export const getUserUpdate = (params?: any) =>
  defHttp.post<any>({
    url: Api.userUpdate,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });
export const getEditUser = (params?: any) => defHttp.get<any>({ url: Api.editUser, params });

export const getUserDelUsers = (params?: any) =>
  defHttp.post<any>({ url: Api.userDelUsers, params });

export const getUserCheckMobile = (params?: any) =>
  defHttp.post<any>({ url: Api.userCheckMobile, params });
export const getUserCheckPersonNo = (params?: any) =>
  defHttp.post<any>({ url: Api.userCheckPersonNo, params });
export const getUserCheckPwd = (params?: any) =>
  defHttp.post<any>({ url: Api.userCheckPwd, params });
export const getUserCheckUserName = (params?: any) =>
  defHttp.post<any>({ url: Api.userCheckUserName, params });

export const getChangeDepts = (params?: any) => defHttp.post<any>({ url: Api.changeDepts, params });
export const getTransferUsers = (params?: any) =>
  defHttp.post<any>({ url: Api.transferUsers, params });
export const getEnableBatch = (params?: any) => defHttp.get<any>({ url: Api.enableBatch, params });
export const getLeaveOffBatch = (params?: any) =>
  defHttp.get<any>({ url: Api.leaveOffBatch, params });
export const getStopStartIsValid = (params?: any) =>
  defHttp.post<any>({ url: Api.stopStartIsValid, params });

export const getRoleMultilSel = (params?: any) =>
  defHttp.get<any>({ url: Api.roleMultilSel, params });
export const getUserSetRole = (params?: any) => defHttp.get<any>({ url: Api.userSetRole, params });
export const getSetRoleOfUser = (params?: any) =>
  defHttp.post<any>({ url: Api.setRoleOfUser, params });

export const getUserPriv = (params?: any) => defHttp.get<any>({ url: Api.userPriv, params });
export const getSetUserPrivs = (params?: any) =>
  defHttp.post<any>({ url: Api.setUserPrivs, params });

export const getUserAdminDept = (params?: any) =>
  defHttp.get<any>({ url: Api.userAdminDept, params });
export const getSetUserAdminDept = (params?: any) =>
  defHttp.post<any>({ url: Api.setUserAdminDept, params });

export const getExportUser = (params?: any) =>
  defHttp.post<any>(
    { url: Api.exportUser, params, responseType: 'blob' },
    {
      isTransformResponse: false,
    },
  );
export const getUserImportFinish = (params?: any) =>
  defHttp.post<any>({
    url: Api.userImportFinish,
    params,
  });
export const getUserImportConfirm = (params?: any) =>
  defHttp.post<any>({
    url: Api.userImportConfirm,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getPostList = (params?: any) => defHttp.get<any>({ url: Api.postList, params });
export const getPostCreate = (params?: any) => defHttp.post<any>({ url: Api.postCreate, params });
export const getPostEditPage = (params?: any) =>
  defHttp.post<any>({ url: Api.postEditPage, params });
export const getPostUpdate = (params?: any) => defHttp.post<any>({ url: Api.postUpdate, params });
export const getPostDel = (params?: any) => defHttp.post<any>({ url: Api.postDel, params });

export const getPostUserTransferPage = (params?: any) =>
  defHttp.get<any>({ url: Api.postUserTransferPage, params });
export const getPostUserUpdate = (params?: any) =>
  defHttp.post<any>({ url: Api.postUserUpdate, params });

export const getShowImg = (params?: any) =>
  defHttp.post<any>(
    {
      url: Api.showImg,
      params,
      responseType: 'blob',
    },
    {
      isTransformResponse: false,
    },
  );

export const getShowImgInJar = (params?: any) =>
  defHttp.post<any>(
    {
      url: Api.showImgInJar,
      params,
      responseType: 'blob',
    },
    {
      isTransformResponse: false,
    },
  );

export const getUpeditUser = (params?: any) => defHttp.post<any>({ url: Api.upeditUser, params });

export const getUpdateMyInfo = (params?: any) =>
  defHttp.post<any>({
    url: Api.updateMyInfo,
    params,
    headers: {
      'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVD0qp8RY7NId5ucK',
    },
  });

export const getResetPortrait = (params?: any) =>
  defHttp.post<any>(
    { url: Api.resetPortrait, params },
    {
      isTransformResponse: false,
    },
  );

export const getConfigInfo = (params?: any) =>
  defHttp.post<any>({ url: Api.getConfigInfo, params });

export const getUiSetup = (params?: any) => defHttp.post<any>({ url: Api.getUiSetup, params });
export const getUpdateUiSetup = (params?: any) =>
  defHttp.post<any>(
    { url: Api.getUpdateUiSetup, params },
    {
      isTransformResponse: false,
    },
  );

export const getBasicCreateNode = (params?: any) =>
  defHttp.post<any>({ url: Api.getBasicCreateNode, params });
export const getBasicUpdateNode = (params?: any) =>
  defHttp.post<any>({ url: Api.getBasicUpdateNode, params });
export const getBasicDelNode = (params?: any) =>
  defHttp.post<any>({ url: Api.getBasicDelNode, params });
export const getBasicMoveNode = (params?: any) =>
  defHttp.post<any>({ url: Api.getBasicMoveNode, params });
export const getBasicOpenNode = (params?: any) =>
  defHttp.post<any>({ url: Api.getBasicOpenNode, params });
export const getBasicCloseNode = (params?: any) =>
  defHttp.post<any>({ url: Api.getBasicCloseNode, params });

export const getUpdateInitPwd = (params?: any) =>
  defHttp.post<any>({ url: Api.getUpdateInitPwd, params });

export const getChangePwd = (params?: any) => defHttp.post<any>({ url: Api.changePwd, params });

export const getResetPwdSendLink = (params?: any) =>
  defHttp.post<any>(
    { url: Api.getResetPwdSendLink, params },
    {
      isTransformResponse: false,
    },
  );

export const getSwitchRole = (params?: any) =>
  defHttp.post<any>({ url: Api.getSwitchRole, params });
export const getSwitchDept = (params?: any) =>
  defHttp.post<any>({ url: Api.getSwitchDept, params });

export const getQrCodeForLogin = (params?: any) =>
  defHttp.post<any>({ url: Api.getQrCodeForLogin, params });

export const getQrCodeLoginCheck = (params?: any) =>
  defHttp.post<any>({ url: Api.getQrCodeLoginCheck, params });

export const getPortalList = (params?: any) =>
  defHttp.post<any>({ url: Api.getPortalList, params });
export const getPortalCreate = (params?: any) =>
  defHttp.post<any>({ url: Api.getPortalCreate, params });
export const getPortalUpdate = (params?: any) =>
  defHttp.post<any>({ url: Api.getPortalUpdate, params });
export const getPortalCopy = (params?: any) =>
  defHttp.post<any>({ url: Api.getPortalCopy, params });

export const getPortalSort = (params?: any) =>
  defHttp.post<any>({ url: Api.getPortalSort, params });
export const getPortalDel = (params?: any) => defHttp.post<any>({ url: Api.getPortalDel, params });
export const getPortalListByUser = (params?: any) =>
  defHttp.post<any>({ url: Api.getPortalListByUser, params });
export const getPortalListForMenu = (params?: any) =>
  defHttp.post<any>({ url: Api.getPortalListForMenu, params });
export const getPortalNames = (params?: any) =>
  defHttp.post<any>({ url: Api.getPortalNames, params });
export const getApplications = (params?: any) =>
  defHttp.post<any>({ url: Api.getApplications, params });

export const getSetup = (params?: any) =>
  defHttp.post<any>({
    url: Api.getSetup,
    params,
  });
export const getUpdateSetup = (params?: any) =>
  defHttp.post<any>({ url: Api.getUpdateSetup, params });

export const getCard = (params?: any) => defHttp.post<any>({ url: Api.getCard, params });
export const getListAllCard = (params?: any) =>
  defHttp.post<any>({ url: Api.getListAllCard, params });
export const getListCard = (params?: any) => defHttp.post<any>({ url: Api.getListCard, params });
export const getListCardByModule = (params?: any) =>
  defHttp.post<any>({ url: Api.getListCardByModule, params });
export const getListCardByApplication = (params?: any) =>
  defHttp.post<any>({ url: Api.getListCardByApplication, params });

export const getListCarouselPicture = (params?: any) =>
  defHttp.post<any>({ url: Api.getListCarouselPicture, params });
export const getCarouselPictureInfo = (params?: any) =>
  defHttp.post<any>({ url: Api.getCarouselPictureInfo, params });

export const getChartTypes = (params?: any) =>
  defHttp.post<any>({ url: Api.getChartTypes, params });

export const getBar = (params?: any) => defHttp.post<any>({ url: Api.getBar, params });
export const getPie = (params?: any) => defHttp.post<any>({ url: Api.getPie, params });
export const getGauge = (params?: any) => defHttp.post<any>({ url: Api.getGauge, params });
export const getLine = (params?: any) => defHttp.post<any>({ url: Api.getLine, params });
export const getFunnel = (params?: any) => defHttp.post<any>({ url: Api.getFunnel, params });
export const getRadar = (params?: any) => defHttp.post<any>({ url: Api.getRadar, params });

export const getDirNames = (params?: any) => defHttp.post<any>({ url: Api.getDirNames, params });
export const getListDoc = (params?: any) => defHttp.post<any>({ url: Api.getListDoc, params });
export const listImageByDirCode = (params?: any) =>
  defHttp.post<any>({ url: Api.listImageByDirCode, params });

export const getCheckPwd = (params?: any) => defHttp.post<any>({ url: Api.getCheckPwd, params });
export const getJump = (params?: any) => defHttp.post<any>({ url: Api.jump, params });
export const synAll = (params?: any) => defHttp.post<any>({ url: Api.synAll, params });
export const getMmaRaw = (params?: any) => defHttp.post<any>({ url: Api.getMmaRaw, params });
