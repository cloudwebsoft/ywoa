<!--
 * @Author: Vben
 * @Description: logo component
-->
<template>
  <div class="anticon" :class="getAppLogoClass" @click="goHome">
    <img
      :style="[
        {
          width: isLogo == 1 ? '25px' : isLogo == 4 ? '32px' : isLogo == 5 ? '32px' : '60px',
          height: isLogo == 1 ? '22px' : isLogo == 4 ? '30px' : isLogo == 5 ? '32px' : '50px',
          marginTop: isLogo == 1 ? '0px' : isLogo == 4 ? '0px' : isLogo == 5 ? '5px' : '30px',
        },
      ]"
      src="../../../assets/images/logo.png"
    />
    <img
      v-if="isLogo != 5"
      :style="[
        {
          width: '1px',
          height: isLogo == 1 ? '20px' : '30px',
          marginTop: isLogo == 1 ? '0px' : '30px',
        },
      ]"
      class="ml-3 mr-3"
      src="../../../assets/images/login-h.png"
      v-show="showTitle"
    />
    <div
      class="truncate md:opacity-100"
      :style="[
        {
          marginTop: isLogo == 1 ? '0px' : isLogo == 5 ? '5px' : '30px',
          marginLeft: isLogo == 5 ? '10px' : '',
        },
      ]"
      :class="getTitleClass"
      v-show="showTitle"
    >
      <!-- {{ title }} -->
      <img
        v-if="isLogo == 1"
        style="width: 100%; height: 25px"
        src="../../../assets/images/login-title.png"
      />
      <img
        v-if="isLogo == 2"
        style="width: 100%; height: 55px"
        src="../../../assets/images/login-title.png"
      />
      <img v-if="isLogo == 3" style="width: 50%" src="../../../assets/images/login-title.png" />
      <img
        v-if="isLogo == 5"
        style="width: 100%; height: 36px"
        src="../../../assets/images/login-title.png"
      />
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed, unref, ref } from 'vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useGo } from '/@/hooks/web/usePage';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useUserStore } from '/@/store/modules/user';
  import { number } from 'vue-types';
  import { useAppStore } from '/@/store/modules/app';
  import { MenuTypeEnum } from '/@/enums/menuEnum';
  import { getToken } from '/@/utils/auth';

  const props = defineProps({
    /**
     * The theme of the current parent component
     */
    theme: { type: String, validator: (v: string) => ['light', 'dark'].includes(v) },
    /**
     * Whether to show title
     */
    showTitle: { type: Boolean, default: true },
    /**
     * The title is also displayed when the menu is collapsed
     */
    alwaysShowTitle: { type: Boolean },
    // 1 左侧菜单 2 登录页（默认） 3 顶部菜单logo 4 混合菜单 5 顶部菜单
    isLogo: {
      type: [Number, String],
      default: 2,
    },
  });

  const isLogo = ref(props.isLogo);
  const { prefixCls } = useDesign('app-logo');
  const { getCollapsedShowTitle } = useMenuSetting();
  const userStore = useUserStore();
  const { title } = useGlobSetting();
  const go = useGo();

  const appStore = useAppStore();
  if (getToken() != undefined) {
    if (appStore.getMenuSetting.type === MenuTypeEnum.MIX_SIDEBAR) {
      isLogo.value = 4;
    } else if (appStore.getMenuSetting.type === MenuTypeEnum.TOP_MENU) {
      isLogo.value = 5;
    }
  }

  console.log('isLogo', isLogo.value);

  const getAppLogoClass = computed(() => [
    prefixCls,
    props.theme,
    { 'collapsed-show-title': unref(getCollapsedShowTitle) },
  ]);

  const getTitleClass = computed(() => [
    `${prefixCls}__title`,
    {
      'xs:opacity-0': !props.alwaysShowTitle,
    },
  ]);

  function goHome() {
    go(userStore.getUserInfo.homePath || PageEnum.BASE_HOME);
  }
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-app-logo';

  .@{prefix-cls} {
    display: flex;
    align-items: center;
    padding-left: 7px;
    cursor: pointer;
    transition: all 0.2s ease;

    &.light {
      border-bottom: 1px solid @border-color-base;
    }

    &.collapsed-show-title {
      padding-left: 20px;
    }

    &.light &__title {
      color: @primary-color;
    }

    &.dark &__title {
      color: @white;
    }

    &__title {
      font-size: 16px;
      font-weight: 700;
      transition: all 0.5s;
      line-height: normal;
    }
  }
</style>
