import type { AppRouteRecordRaw, AppRouteModule } from '/@/router/types';

import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '/@/router/routes/basic';

import { LAYOUT } from '/@/router/constant';

import { mainOutRoutes } from './mainOut';
import { PageEnum } from '/@/enums/pageEnum';
import { t } from '/@/hooks/web/useI18n';

// import.meta.globEager() 直接引入所有的模块 Vite 独有的功能
const modules = import.meta.globEager('./modules/**/*.ts');
const routeModuleList: AppRouteModule[] = [];

// 加入到路由集合中
Object.keys(modules).forEach((key) => {
  const mod = modules[key].default || {};
  const modList = Array.isArray(mod) ? [...mod] : [mod];
  routeModuleList.push(...modList);
});

export const asyncRoutes = [PAGE_NOT_FOUND_ROUTE, ...routeModuleList];

// 根路由
export const RootRoute: AppRouteRecordRaw = {
  path: '/',
  name: 'Root',
  redirect: PageEnum.BASE_HOME,
  meta: {
    title: 'Root',
  },
};

export const LoginRoute: AppRouteRecordRaw = {
  path: '/login',
  name: 'Login',
  component: () => import('/@/views/sys/login/Login.vue'),
  meta: {
    title: t('routes.basic.login'),
  },
};

export const PortalRoute: AppRouteModule = {
  path: '/portal',
  name: 'MyPortal',
  component: LAYOUT,
  redirect: '/portal/portal',
  meta: {
    orderNo: 11,
    title: '门户',
  },
  children: [
    {
      path: '/portal',
      name: 'Portal',
      component: () => import('/@/views/dashboard/analysis/index.vue'),
      meta: {
        title: '门户',
      },
    },
  ],
};

export const SmartModulePage: AppRouteModule = {
  path: '/module',
  name: 'Module',
  component: LAYOUT,
  redirect: '/module/smartModulePage',
  meta: {
    orderNo: 10,
    title: '模块列表',
  },
  children: [
    {
      path: '/smartModulePage',
      name: 'SmartModulePage',
      component: () => import('/@/views/pages/smartModule/smartModule.vue'),
      meta: {
        title: '模块列表',
      },
    },
    {
      path: '/smartModuleTreeViewPage',
      name: 'SmartModuleTreeViewPage',
      component: () => import('/@/views/pages/smartModule/smartModuleTreeView.vue'),
      meta: {
        title: '树形视图',
      },
    },
    {
      path: '/smartModuleAddEditView',
      name: 'smartModuleAddEditView',
      component: () => import('/@/views/pages/smartModule/modules/smartModuleAddEditView.vue'),
      meta: {
        title: '模块',
        ignoreKeepAlive: true, // 缓存
      },
    },
  ],
};

export const Manager: AppRouteModule = {
  path: '/manager',
  name: 'Manager',
  component: LAYOUT,
  redirect: '/manager/managerPage',
  meta: {
    orderNo: 10,
    icon: 'ion:grid-outline',
    title: '管理页',
  },
  children: [
    {
      path: '/managerPage',
      name: 'ManagerPage',
      component: () => import('/@/views/pages/processManagement/managerPage.vue'),
      meta: {
        title: '管理',
        ignoreKeepAlive: true, // 缓存
      },
    },
    {
      path: '/processHandle',
      name: 'processHandle',
      component: () => import('/@/views/pages/processManagement/processHandleView.vue'),
      meta: {
        title: '流程处理',
        ignoreKeepAlive: true, // 缓存
      },
    },
    {
      path: '/processHandleFree',
      name: 'processHandleFree',
      component: () => import('/@/views/pages/processManagement/processHandleFreeView.vue'),
      meta: {
        title: '流程处理',
        ignoreKeepAlive: true, // 缓存
      },
    },
    {
      path: '/processShow',
      name: 'processShow',
      component: () => import('/@/views/pages/processManagement/processShowView.vue'),
      meta: {
        title: '流程查看',
        ignoreKeepAlive: true, // 缓存
      },
    },
    {
      path: '/flowDebug',
      name: 'FlowDebug',
      component: () => import('/@/views/pages/processManagement/flowDebug.vue'),
      meta: {
        title: '流程调试',
      },
    },
    {
      path: '/basicdataKind',
      name: 'BasicdataKind',
      component: () => import('/@/views/system/basicdata/kind/index.vue'),
      meta: {
        title: '基础数据类型',
      },
    },
  ],
};

export const SmartCalendar: AppRouteModule = {
  path: '/smartCalendar',
  name: 'smartCalendar',
  component: () => import('/@/views/pages/smartModule/smartCalendar.vue'),
  meta: {
    orderNo: 10,
    title: '测试',
  },
};

// Basic routing without permission
// 未经许可的基本路由
export const basicRoutes = [
  LoginRoute,
  Manager,
  RootRoute,
  PortalRoute,
  SmartCalendar,
  SmartModulePage,
  ...mainOutRoutes,
  REDIRECT_ROUTE,
  PAGE_NOT_FOUND_ROUTE,
  // Test,
];
