import type { AppRouteModule } from '/@/router/types';

import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';
const newSystem: AppRouteModule = {
  path: '/Isystem',
  name: 'ISystem',
  component: LAYOUT,
  redirect: '/system/account',
  meta: {
    orderNo: 2000,
    icon: 'ion:settings-outline',
    title: t('routes.demo.system.moduleName'),
  },
  children: [
    {
      path: 'organization',
      name: 'organization',
      meta: {
        title: '组织机构',
        ignoreKeepAlive: false,
      },
      children: [
        {
          path: 'jobNumberManagement',
          name: 'jobNumberManagement',
          meta: {
            title: '工号管理',
            ignoreKeepAlive: false,
          },
          component: () =>
            import('/@/views/system/organization/jobNumberManagement/jobNumberManagement.vue'),
        },
        {
          path: 'organizationManagement',
          name: 'organizationManagement',
          meta: {
            title: '组织管理',
            ignoreKeepAlive: false,
          },
          component: () =>
            import(
              '/@/views/system/organization/organizationManagement/organizationManagement.vue'
            ),
        },
      ],
    },
    {
      path: 'account',
      name: 'AccountManagement',
      meta: {
        title: '账号管理',
        ignoreKeepAlive: false,
      },
      component: () => import('/@/views/system/account/index.vue'),
    },
    {
      path: 'account_detail/:id',
      name: 'AccountDetail',
      meta: {
        hideMenu: true,
        title: t('routes.demo.system.account_detail'),
        ignoreKeepAlive: true,
        showMenu: false,
        currentActiveMenu: '/system/account',
      },
      component: () => import('/@/views/system/account/AccountDetail.vue'),
    },
    {
      path: 'Irole',
      name: 'IRoleManagement',
      meta: {
        title: t('routes.demo.system.role'),
        ignoreKeepAlive: true,
      },
      component: () => import('/@/views/system/role/index.vue'),
    },
    {
      path: 'UserGroup',
      name: 'UserGroup',
      meta: {
        title: '用户组管理',
        ignoreKeepAlive: true,
      },
      component: () => import('/@/views/system/role/userGroup/userGroup.vue'),
    },

    {
      path: 'PermissionName',
      name: 'PermissionName',
      meta: {
        title: '权限名称',
        ignoreKeepAlive: true,
      },
      component: () => import('/@/views/system/role/permissionName/permissionName.vue'),
    },
    {
      path: 'Imenu',
      name: 'IMenuManagement',
      meta: {
        title: t('routes.demo.system.menu'),
        ignoreKeepAlive: true,
      },
      component: () => import('/@/views/system/menu/index.vue'),
    },
    {
      path: 'Idept',
      name: 'IDeptManagement',
      meta: {
        title: t('routes.demo.system.dept'),
        ignoreKeepAlive: true,
      },
      component: () => import('/@/views/system/dept/index.vue'),
    },
    {
      path: 'IbasicdataKind',
      name: 'IBasicDataKindManagement',
      meta: {
        title: '基础数据类型',
        ignoreKeepAlive: false,
      },
      component: () => import('/@/views/system/basicdata/kind/index.vue'),
    },
    {
      path: 'Ibasicdata',
      name: 'IBasicDataManagement',
      meta: {
        title: '基础数据',
        ignoreKeepAlive: true,
      },
      component: () => import('/@/views/system/basicdata/index.vue'),
    },
    {
      path: 'IchangePassword',
      name: 'IChangePassword',
      meta: {
        title: t('routes.demo.system.password'),
        ignoreKeepAlive: true,
      },
      component: () => import('/@/views/system/password/index.vue'),
    },
  ],
};
// const processManagements: AppRouteModule = {
//   path: '/processManagement',
//   name: 'processManagement',
//   component: LAYOUT,
//   redirect: 'pages/processManagement/toDoProcess',
//   meta: {
//     orderNo: 2000,
//     icon: 'ion:settings-outline',
//     title: '流程',
//   },
//   children: [
//     {
//       path: 'myProcess',
//       name: 'myProcess',
//       meta: {
//         title: '我的流程',
//         ignoreKeepAlive: false,
//       },
//       component: () => import(''),
//     },
//     {
//       path: 'launch',
//       name: 'launch',
//       meta: {
//         title: '发起流程',
//         ignoreKeepAlive: false,
//       },
//       component: () => import('/@/views/pages/processManagement/launch.vue'),
//     },
//     {
//       path: 'followProcess',
//       name: 'followProcess',
//       meta: {
//         title: '关注流程',
//         ignoreKeepAlive: false,
//       },
//       component: () => import('/@/views/pages/processManagement/followProcess.vue'),
//     },
//   ],
// };
// const notices: AppRouteModule = {
//   path: '/administrativeManagement',
//   name: 'administrativeManagement',
//   component: LAYOUT,
//   redirect: 'pages/administrativeManagement/notice/notice',
//   meta: {
//     orderNo: 2000,
//     icon: 'ion:settings-outline',
//     title: '行政',
//   },
//   children: [
//     {
//       path: 'notice',
//       name: 'notice',
//       meta: {
//         title: '通知公告',
//         ignoreKeepAlive: false,
//       },
//       component: () => import('/@/views/pages/administrativeManagement/notice/notice.vue'),
//     },
//   ],
// };
// const personnelManagements: AppRouteModule = {
//   path: '/personnelManagement',
//   name: 'personnelManagement',
//   component: LAYOUT,
//   redirect: 'pages/personnelManagement/personnelInformation/personnelInformation',
//   meta: {
//     orderNo: 2000,
//     icon: 'ion:settings-outline',
//     title: '人事',
//   },
//   children: [
//     {
//       path: 'personnelInformation',
//       name: 'personnelInformation',
//       meta: {
//         title: '人事信息',
//         ignoreKeepAlive: false,
//       },
//       component: () =>
//         import('/@/views/pages/personnelManagement/personnelInformation/personnelInformation.vue'),
//     },
//   ],
// };
export default newSystem;
