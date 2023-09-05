import { defHttp } from '/@/utils/http/axios';
import { LoginParams, LoginResultModel, GetUserInfoModel } from './model/userModel';

import { ErrorMessageMode } from '/#/axios';

import setting from '/@/settings/projectSetting';
const Api = {
  Login: `${setting.interfacePrefix}/doLogin.do`,
  Logout: `${setting.interfacePrefix}/logout`,
  GetUserInfo: `${setting.interfacePrefix}/user`, //无效
  GetPermCode: '/getPermCode',
  TestRetry: '/testRetry',
};

/**
 * @description: user login api
 */
export function loginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.Login,
      params,
    },
    {
      errorMessageMode: mode,
      isTransformResponse: false,
    },
  );
}

/**
 * @description: getUserInfo
 */
export function getUserInfo() {
  return defHttp.get<GetUserInfoModel>({ url: Api.GetUserInfo }, { errorMessageMode: 'none' });
}

export function getPermCode() {
  return defHttp.get<string[]>({ url: Api.GetPermCode });
}

export function doLogout() {
  // from 表示来自于前端，区别于后端登录，因后端登录需在MyLogoutSuccessHandler中重定向至/index
  const params = { from: 'front' };
  return defHttp.get({ url: Api.Logout, params });
}

export function testRetry() {
  return defHttp.get(
    { url: Api.TestRetry },
    {
      retryRequest: {
        isOpenRetry: true,
        count: 5,
        waitTime: 1000,
      },
    },
  );
}
