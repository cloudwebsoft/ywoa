/**
The routing of this file will not show the layout.
It is an independent new page.
the contents of the file still need to log in to access
 */
import type { AppRouteModule } from '/@/router/types';

// test
// http:ip:port/main-out
export const mainOutRoutes: AppRouteModule[] = [
  {
    path: '/main-out',
    name: 'MainOut',
    component: () => import('/@/views/demo/main-out/index.vue'),
    meta: {
      title: 'MainOut',
      ignoreAuth: true,
    },
  },
  {
    path: '/processHandleView',
    name: 'processHandleView',
    component: () => import('/@/views/pages/processManagement/processHandleView.vue'),
    meta: {
      orderNo: 10,
      title: '流程处理',
    },
  },
  {
    path: '/flowDebugPage',
    name: 'flowDebugPage',
    component: () => import('/@/views/pages/processManagement/flowDebug.vue'),
    meta: {
      orderNo: 10,
      title: '流程调试',
    },
  },
  {
    path: '/changePassword',
    name: 'changePassword',
    component: () => import('/@/views/pages/common/ChangePassword.vue'),
    meta: {
      orderNo: 10,
      title: '修改密码',
    },
  },
  {
    path: '/resetPassword',
    name: 'resetPassword',
    component: () => import('/@/views/pages/common/ResetPassword.vue'),
    meta: {
      orderNo: 10,
      title: '重置密码',
    },
  },
  {
    path: '/qrcodeLogin',
    name: 'QrcodeLogin',
    component: () => import('/@/views/pages/common/QrcodeLogin.vue'),
    meta: {
      orderNo: 10,
      title: '扫码登录',
    },
  },
  // {
  //   path: '/testDemo',
  //   name: 'testDemo',
  //   component: () => import('/@/views/demo/test/Test.vue'),
  //   meta: {
  //     orderNo: 10,
  //     title: 'test',
  //   },
  // },
  {
    path: '/tabsForm',
    name: 'tabsForm',
    component: () => import('/@/views/demo/form/tabsForm.vue'),
    meta: {
      orderNo: 10,
      title: 'test',
    },
  },
  {
    path: '/jump',
    name: 'Jump',
    component: () => import('/@/views/pages/common/Jump.vue'),
    meta: {
      orderNo: 10,
      title: '跳转',
    },
  },
];

export const mainOutRouteNames = mainOutRoutes.map((item) => item.name);
