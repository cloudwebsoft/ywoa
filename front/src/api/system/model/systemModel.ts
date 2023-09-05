import { BasicPageParams, BasicFetchResult } from '/@/api/model/baseModel';

export type AccountParams = BasicPageParams & {
  account?: string;
  nickname?: string;
};

export type RoleParams = {
  roleName?: string;
  status?: string;
  code?: string;
};

export type BasicDataKindParams = {
  id?: number;
  name?: string;
};

export type BasicDataParams = {
  code?: string;
  name?: string;
};

export type RolePageParams = BasicPageParams & RoleParams;

export type BasicDataKindPageParams = BasicPageParams & BasicDataKindParams;

export type BasicDataPageParams = BasicPageParams & BasicDataParams;

export type DeptParams = {
  deptName?: string;
  status?: string;
};

export type MenuParams = {
  menuName?: string;
  status?: string;
};

export interface AccountListItem {
  id: string;
  account: string;
  email: string;
  nickname: string;
  role: number;
  createTime: string;
  remark: string;
  status: number;
}

export interface DeptListItem {
  id: string;
  orderNo: string;
  createTime: string;
  remark: string;
  status: number;
}

export interface MenuListItem {
  id: string;
  orderNo: string;
  createTime: string;
  status: number;
  icon: string;
  component: string;
  permission: string;
}

export interface RoleListItem {
  id: string;
  roleName: string;
  roleValue: string;
  status: number;
  orderNo: string;
  createTime: string;
}

export interface BasicDataKindListItem {
  id: number;
  name: string;
  orderNo: number;
}

export interface BasicDataListItem {
  code: string;
  name: string;
  typeName: string;
  kindName: string;
  orderNo: number;
}

/**
 * @description: Request list return value
 */
export type AccountListGetResultModel = BasicFetchResult<AccountListItem>;

export type DeptListGetResultModel = BasicFetchResult<DeptListItem>;

export type MenuListGetResultModel = BasicFetchResult<MenuListItem>;

export type RolePageListGetResultModel = BasicFetchResult<RoleListItem>;

export type RoleListGetResultModel = RoleListItem[];

export type BasicDataKindPageListGetResultModel = BasicFetchResult<BasicDataKindListItem>;

export type BasicDataPageListGetResultModel = BasicFetchResult<BasicDataListItem>;
