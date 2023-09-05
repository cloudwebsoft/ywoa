import type { UserInfo } from '/#/store';
import type { ErrorMessageMode } from '/#/axios';
import { defineStore } from 'pinia';
import { store } from '/@/store';
import { RoleEnum } from '/@/enums/roleEnum';
import { PageEnum } from '/@/enums/pageEnum';
import {
  ROLES_KEY,
  TOKEN_KEY,
  USER_INFO_KEY,
  USER_DEBUG_KEY,
  SERVER_URL_KEY,
  USER_INFO_PLUS_KEY,
  SERVER_INFO_KEY,
} from '/@/enums/cacheEnum';
import { getAuthCache, setAuthCache } from '/@/utils/auth';
import { GetUserInfoModel, LoginParams } from '/@/api/sys/model/userModel';
import { doLogout, getUserInfo, loginApi } from '/@/api/sys/user';
import { getMyInfo, getShowImg, getUiSetup } from '/@/api/system/system';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { router } from '/@/router';
import { usePermissionStore } from '/@/store/modules/permission';
import { RouteRecordRaw } from 'vue-router';
import { PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';
import { isArray } from '/@/utils/is';
import { useAppStore } from '/@/store/modules/app';
import { h } from 'vue';
import { bufToUrl } from '/@/utils/file/base64Conver';
import setting from '/@/settings/projectSetting';
import { createMessageGuard } from '/@/router/guard';
import { useGlobSetting } from '/@/hooks/setting';
import { updateHeaderBgColor, updateSidebarBgColor } from '/@/logics/theme/updateBackground';

interface UserState {
  userInfo: Nullable<UserInfo>;
  token?: string;
  roleList: RoleEnum[];
  sessionTimeout?: boolean;
  lastUpdateTime: number;
  isDebug: string;
  serverUrl: string;
  userInfoPlus: Recordable;
  serverInfo: Recordable;
  bigFullScreen: boolean;
}

export const useUserStore = defineStore({
  id: 'app-user',
  state: (): UserState => ({
    // user info
    userInfo: null,
    // token
    token: undefined,
    // roleList
    roleList: [],
    // Whether the login expired
    sessionTimeout: false,
    // Last fetch time
    lastUpdateTime: 0,
    //whether the mode is isDebug
    isDebug: 'false',
    serverUrl: '',
    userInfoPlus: {},
    serverInfo: {},
    bigFullScreen: false,
  }),
  getters: {
    getUserInfo(): UserInfo {
      return this.userInfo || getAuthCache<UserInfo>(USER_INFO_KEY) || {};
    },
    getToken(): string {
      return this.token || getAuthCache<string>(TOKEN_KEY);
    },
    getUserInfoPlus(): Recordable {
      return this.userInfoPlus || getAuthCache<Recordable>(USER_INFO_PLUS_KEY) || {};
    },

    getRoleList(): RoleEnum[] {
      return this.roleList.length > 0 ? this.roleList : getAuthCache<RoleEnum[]>(ROLES_KEY);
    },
    getSessionTimeout(): boolean {
      return !!this.sessionTimeout;
    },
    getLastUpdateTime(): number {
      return this.lastUpdateTime;
    },
    getIsDebug(): string {
      return this.isDebug || getAuthCache<string>(USER_DEBUG_KEY);
    },
    getServerUrl(): string {
      return this.serverUrl || getAuthCache<string>(SERVER_URL_KEY);
    },
    getServerInfo(): Recordable {
      return this.serverInfo || getAuthCache<Recordable>(SERVER_INFO_KEY) || {};
    },
  },
  actions: {
    setToken(info: string | undefined) {
      this.token = info ? info : ''; // for null or undefined value
      setAuthCache(TOKEN_KEY, info);
    },
    setRoleList(roleList: RoleEnum[]) {
      this.roleList = roleList;
      setAuthCache(ROLES_KEY, roleList);
    },
    setUserInfo(info: UserInfo | null) {
      this.userInfo = info;
      this.lastUpdateTime = new Date().getTime();
      setAuthCache(USER_INFO_KEY, info);
    },
    setUserInfoPlus(info: Recordable | {}) {
      this.userInfoPlus = info;
      setAuthCache(USER_INFO_PLUS_KEY, info);
    },
    setSessionTimeout(flag: boolean) {
      this.sessionTimeout = flag;
    },
    resetState() {
      this.userInfo = null;
      this.token = '';
      this.roleList = [];
      this.sessionTimeout = false;
      this.userInfoPlus = {};
    },

    setIsDebug(info: string) {
      this.isDebug = info;
      setAuthCache(USER_DEBUG_KEY, info);
    },

    setServerUrl(url: string) {
      this.serverUrl = url;
      setAuthCache(SERVER_URL_KEY, url);
    },

    setServerInfo(serverInfo: Recordable) {
      this.serverInfo = serverInfo;
      setAuthCache(SERVER_INFO_KEY, serverInfo);
    },
    setBigFullScreen(info: boolean) {
      this.bigFullScreen = info;
    },

    /**
     * @description: login
     */
    async login(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      },
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const data = await loginApi(loginParams, mode);
        console.log('data', data);
        if (data['code'] != 200) {
          const { createMessage } = useMessage();
          createMessage.error(data['msg']);
          Promise.reject(data['msg']);
        } else {
          const { Authorization, serverInfo } = data;
          this.setServerUrl(serverInfo.url);
          this.setServerInfo(serverInfo);
          // window.sessionStorage.setItem('serverInfoUrl', serverInfo.url);
          // save token
          this.setToken(Authorization);
          // router.replace(PageEnum.BASE_HOME);
          return this.afterLoginAction(goHome, data);
        }
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async afterLoginAction(goHome?: boolean, data?: object): Promise<GetUserInfoModel | null> {
      if (!this.getToken) return null;

      const appStore = useAppStore();
      // 从后台取得ui设置
      const { applicationCode } = useGlobSetting();
      const configSetting = await getUiSetup({ applicationCode });
      if (configSetting) {
        const newConfigSetting = JSON.parse(configSetting);
        // 保留系统所设置的interfacePrefix，以免被服务端所存储的覆盖
        newConfigSetting.interfacePrefix = setting.interfacePrefix;
        appStore.setProjectConfig(newConfigSetting);
        // 刷新界面，否则在浏览器中首次登录时，界面上菜单底色会显示为白色，其它地方也会不正常
        updateHeaderBgColor();
        updateSidebarBgColor();
      }
      // get user info
      const userInfo = await this.getUserInfoAction();

      const sessionTimeout = this.sessionTimeout;
      if (sessionTimeout) {
        this.setSessionTimeout(false);
      } else {
        const permissionStore = usePermissionStore();
        if (!permissionStore.isDynamicAddedRoute) {
          const routes = await permissionStore.buildRoutesAction();
          routes.forEach((route) => {
            router.addRoute(route as unknown as RouteRecordRaw);
          });
          router.addRoute(PAGE_NOT_FOUND_ROUTE as unknown as RouteRecordRaw);
          permissionStore.setDynamicAddedRoute(true);
        }
        // goHome && (await router.replace(userInfo?.homePath || PageEnum.BASE_HOME));
        if (data && data['isForceChangePwd']) {
          await router.replace(PageEnum.CHANGE_PASSWORD);
        } else if (data && data['ret'] == 0) {
          // 许可证过期
          const { createMessage } = useMessage();
          createMessage.error(data['msg']);
          return null;
        } else {
          goHome && (await router.replace(PageEnum.BASE_HOME));
          // 跳转至系统门户
          // await router.replace(PageEnum.SYSTEM_PORTAL);
        }
      }
      return userInfo;
    },
    async getUserInfoAction(): Promise<UserInfo | null> {
      if (!this.getToken) return null;
      const info = await getMyInfo();
      console.log('info==>', info);
      this.setUserInfoPlus(info);
      const userInfo = info['user'];
      if (info['portrait'])
        await getShowImg({ path: info['portrait'] }).then((res: any) => {
          userInfo['avatar'] = bufToUrl(res);
        });

      // const { roles = [] } = userInfo;
      // if (isArray(roles)) {
      //   const roleList = roles.map((item) => item.value) as RoleEnum[];
      //   this.setRoleList(roleList);
      // } else {
      //   userInfo.roles = [];
      //   this.setRoleList([]);
      // }
      this.setUserInfo(userInfo);
      return userInfo;
    },
    /**
     * @description: logout
     */
    async logout(goLogin = false) {
      console.log('logout getToken', this.getToken);
      if (this.getToken) {
        try {
          await doLogout();
        } catch (e) {
          console.warn('注销Token失败');
          console.warn(e);
        }
      }
      this.setToken(undefined);
      this.setSessionTimeout(false);
      this.setUserInfo(null);
      goLogin && router.push(PageEnum.BASE_LOGIN);
    },

    /**
     * @description: Confirm before logging out
     */
    confirmLoginOut() {
      const { createConfirm } = useMessage();
      const { t } = useI18n();
      createConfirm({
        iconType: 'warning',
        title: () => h('span', t('sys.app.logoutTip')),
        content: () => h('span', t('sys.app.logoutMessage')),
        onOk: async () => {
          await this.logout(true);
        },
      });
    },
  },
});

// Need to be used outside the setup
export function useUserStoreWithOut() {
  return useUserStore(store);
}
