<template>
  <Dropdown placement="bottomLeft" :overlayClassName="`${prefixCls}-dropdown-overlay`">
    <span :class="[prefixCls, `${prefixCls}--${theme}`]" class="flex">
      <img :class="`${prefixCls}__header`" :src="getUserInfo.avatar" />
      <span :class="`${prefixCls}__info hidden md:block`">
        <span :class="`${prefixCls}__name  `" class="truncate">
          {{ getUserInfo.realName }}
        </span>
      </span>
    </span>
    <template #overlay>
      <Menu @click="handleMenuClick">
        <MenuItem
          key="doc"
          :text="t('layout.header.dropdownItemDoc')"
          icon="ion:document-text-outline"
          v-if="getShowDoc"
        />
        <MenuDivider v-if="getShowDoc" />
        <MenuItem key="person" text="个人信息" icon="ant-design:user-outlined" />
        <MenuItem key="pwd" text="修改密码" icon="ant-design:key-outlined" />
        <MenuItem
          key="role"
          text="切换角色"
          v-if="isRoleSwitchable"
          icon="ant-design:user-switch-outlined"
        />
        <MenuItem
          key="dept"
          text="切换部门"
          v-if="isDeptSwitchable"
          icon="ant-design:partition-outlined"
        />
        <MenuItem
          v-if="getUseLockPage"
          key="lock"
          :text="t('layout.header.tooltipLock')"
          icon="ion:lock-closed-outline"
        />
        <MenuItem
          key="logout"
          :text="t('layout.header.dropdownItemLoginOut')"
          icon="ion:power-outline"
        />
      </Menu>
    </template>
  </Dropdown>
  <LockAction @register="register" />
  <PersonDrawer @register="registerPersonDrawer" @success="handlePersonCallback" />
  <ChangeCurDeptCodeModal @register="registerCurDeptCode" @success="handleCurDeptCodeCallback" />
  <CurRoleCodeModal @register="registerCurRoleCode" @success="handleCurRoleCodeCallback" />
  <ChangePwdModal l @register="registerChangePwd" />
</template>
<script lang="ts">
  // components
  import { Dropdown, Menu } from 'ant-design-vue';
  import type { MenuInfo } from 'ant-design-vue/lib/menu/src/interface';

  import { defineComponent, computed } from 'vue';

  import { DOC_URL } from '/@/settings/siteSetting';

  import { useUserStore } from '/@/store/modules/user';
  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useModal } from '/@/components/Modal';
  import { bufToUrl } from '/@/utils/file/base64Conver';

  // import headerImg from '/@/assets/images/header.jpg';
  import manImg from '/@/assets/images/man.png';
  import womanImg from '/@/assets/images/woman.png';
  import { propTypes } from '/@/utils/propTypes';
  import { openWindow } from '/@/utils';

  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

  import PersonDrawer from '/@/layouts/default/header/components/user-dropdown/PersonDrawer.vue';
  import ChangeCurDeptCodeModal from '/@/layouts/default/header/components/user-dropdown/ChangeCurDeptCodeModal.vue';
  import CurRoleCodeModal from '/@/layouts/default/header/components/user-dropdown/CurRoleCodeModal.vue';
  import ChangePwdModal from '/@/layouts/default/header/components/user-dropdown/ChangePwdModal.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { getMyInfo, getShowImg } from '/@/api/system/system';

  type MenuEvent = 'logout' | 'doc' | 'lock' | 'person' | 'dept' | 'role' | 'pwd';

  export default defineComponent({
    name: 'UserDropdown',
    components: {
      Dropdown,
      Menu,
      MenuItem: createAsyncComponent(() => import('./DropMenuItem.vue')),
      MenuDivider: Menu.Divider,
      LockAction: createAsyncComponent(() => import('../lock/LockModal.vue')),
      PersonDrawer,
      ChangeCurDeptCodeModal,
      CurRoleCodeModal,
      ChangePwdModal,
    },
    props: {
      theme: propTypes.oneOf(['dark', 'light']),
    },
    setup() {
      const { prefixCls } = useDesign('header-user-dropdown');
      const { t } = useI18n();
      const { getShowDoc, getUseLockPage } = useHeaderSetting();
      const userStore = useUserStore();

      const isDeptSwitchable = computed(() => userStore.getUserInfoPlus?.isDeptSwitchable);
      const isRoleSwitchable = computed(() => userStore.getUserInfoPlus?.isRoleSwitchable);
      const getUserInfo = computed(() => {
        const { realName = '', avatar, desc, gender } = userStore.getUserInfo || {};
        return { realName, avatar: avatar || gender ? womanImg : manImg, desc };
      });

      const [register, { openModal }] = useModal();

      function handleLock() {
        openModal(true);
      }

      //  login out
      function handleLoginOut() {
        userStore.confirmLoginOut();
      }

      // open doc
      function openDoc() {
        openWindow(DOC_URL);
      }

      const [registerPersonDrawer, { openDrawer: openPersonDrawer }] = useDrawer();
      // open person
      function handlePerson() {
        openPersonDrawer(true, {
          isUpdate: true,
          record: userStore.getUserInfo || {},
        });
      }
      //修改成功回调
      async function handlePersonCallback() {
        const info = await getMyInfo();
        const userInfo = info['user'];
        console.log('handlePersonCallback userInfo', userInfo);
        if (info['portrait']) {
          await getShowImg({ path: info['portrait'] }).then((res: any) => {
            userInfo['avatar'] = bufToUrl(res);
          });
        }
        userStore.setUserInfo(userInfo);
        userStore.setUserInfoPlus(info);
      }

      //更换部门
      const [registerCurDeptCode, { openModal: openCurDeptCodeModal }] = useModal();
      const handleCurDeptCodeCallback = () => {
        handlePersonCallback();
      };
      //更换角色
      const [registerCurRoleCode, { openModal: openCurRoleCodeModal }] = useModal();
      const handleCurRoleCodeCallback = () => {
        handlePersonCallback();
      };

      const [registerChangePwd, { openModal: openChangePwdModal }] = useModal();

      function handleMenuClick(e: { key: MenuEvent }) {
        switch (e.key) {
          case 'logout':
            handleLoginOut();
            break;
          case 'doc':
            openDoc();
            break;
          case 'lock':
            handleLock();
            break;
          case 'person':
            handlePerson();
            break;
          case 'dept':
            openCurDeptCodeModal(true);
            break;
          case 'role':
            openCurRoleCodeModal(true);
            break;
          case 'pwd':
            openChangePwdModal(true);
            break;
        }
      }

      return {
        prefixCls,
        t,
        getUserInfo,
        handleMenuClick,
        getShowDoc,
        register,
        getUseLockPage,
        registerPersonDrawer,
        handlePersonCallback,
        registerCurDeptCode,
        handleCurDeptCodeCallback,
        registerCurRoleCode,
        handleCurRoleCodeCallback,
        isDeptSwitchable,
        isRoleSwitchable,
        registerChangePwd,
      };
    },
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-header-user-dropdown';

  .@{prefix-cls} {
    height: @header-height;
    padding: 0 0 0 10px;
    padding-right: 10px;
    overflow: hidden;
    font-size: 12px;
    cursor: pointer;
    align-items: center;

    img {
      width: 24px;
      height: 24px;
      margin-right: 12px;
    }

    &__header {
      border-radius: 50%;
    }

    &__name {
      font-size: 14px;
    }

    &--dark {
      &:hover {
        background-color: @header-dark-bg-hover-color;
      }
    }

    &--light {
      &:hover {
        background-color: @header-light-bg-hover-color;
      }

      .@{prefix-cls}__name {
        color: @text-color-base;
      }

      .@{prefix-cls}__desc {
        color: @header-light-desc-color;
      }
    }

    &-dropdown-overlay {
      .ant-dropdown-menu-item {
        min-width: 160px;
      }
    }
  }
</style>
