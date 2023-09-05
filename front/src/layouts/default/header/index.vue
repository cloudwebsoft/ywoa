<template>
  <Header :class="getHeaderClass">
    <!-- left start -->
    <div :class="`${prefixCls}-left`">
      <!-- logo -->
      <AppLogo
        v-if="getShowHeaderLogo || getIsMobile"
        :class="`${prefixCls}-logo`"
        :theme="getHeaderTheme"
        :style="getLogoWidth"
        :isLogo="3"
      />
      <LayoutTrigger
        v-if="
          (getShowContent && getShowHeaderTrigger && !getSplit && !getIsMixSidebar) || getIsMobile
        "
        :theme="getHeaderTheme"
        :sider="false"
      />
      <LayoutBreadcrumb v-if="getShowContent && getShowBread" :theme="getHeaderTheme" />
    </div>
    <!-- left end -->

    <!-- menu start -->
    <div :class="`${prefixCls}-menu`" v-if="getShowTopMenu && !getIsMobile">
      <LayoutMenu
        :isHorizontal="true"
        :theme="getHeaderTheme"
        :splitType="getSplitType"
        :menuMode="getMenuMode"
      />
    </div>
    <!-- menu-end -->

    <!-- action  -->
    <div :class="`${prefixCls}-action`">
      <template v-if="aryPortal?.length <= 3">
        <span
          v-for="item in aryPortal"
          :key="item.id"
          :class="`${prefixCls}-action__item`"
          style="font-size: 14px"
          @click="openPortal(item)"
          ><Icon :icon="item.icon" :size="26" v-if="item.icon" :color="'rgb(33, 119, 243)'" />{{
            item.name
          }}
        </span>
      </template>
      <template v-else>
        <span :class="`${prefixCls}-action__item`" style="font-size: 14px">
          <Select
            ref="select"
            v-model:value="selectPortal"
            style="width: 240px"
            placeholder="请选择门户"
          >
            <SelectOption
              :value="item.id"
              v-for="item in aryPortal"
              :key="item.id"
              @click="openPortal(item)"
              ><Icon :icon="item.icon" :size="26" class="mr-2" v-if="item.icon" />{{
                item.name
              }}</SelectOption
            >
          </Select>
        </span>
      </template>
      <AppSearch class="ml-5" :class="`${prefixCls}-action__item `" v-if="getShowSearch" />
      <BigScreen :class="`${prefixCls}-action__item fullscreen-item`" />
      <ErrorAction v-if="getUseErrorHandle" :class="`${prefixCls}-action__item error-action`" />

      <UploadFileView
        :class="`${prefixCls}-action__item fullscreen-item`"
        type="drawer"
        v-if="uploadFileViewType == 'drawer'"
      />
      <!--悬浮-->
      <Teleport to="body" v-if="uploadFileViewType == 'tab'">
        <UploadFileSuspension />
      </Teleport>

      <Notify v-if="getShowNotice" :class="`${prefixCls}-action__item notify-item`" />

      <FullScreen v-if="getShowFullScreen" :class="`${prefixCls}-action__item fullscreen-item`" />

      <AppLocalePicker
        v-if="getShowLocalePicker"
        :reload="true"
        :showText="false"
        :class="`${prefixCls}-action__item`"
      />

      <UserDropDown :theme="getHeaderTheme" />

      <SettingDrawer
        v-if="getShowSetting && name === 'admin'"
        :class="`${prefixCls}-action__item`"
      />

      <span v-if="showId" class="mr-1" style="color: rgb(125, 185, 220)" title="Server ID">{{
        id
      }}</span>
    </div>
  </Header>
</template>
<script lang="ts">
  import { defineComponent, unref, computed, onMounted, ref } from 'vue';

  import { propTypes } from '/@/utils/propTypes';

  import { Layout, Select } from 'ant-design-vue';
  import { AppLogo } from '/@/components/Application';
  import LayoutMenu from '../menu/index.vue';
  import LayoutTrigger from '../trigger/index.vue';

  import { AppSearch } from '/@/components/Application';
  import Icon from '/@/components/Icon/index';

  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useRootSetting } from '/@/hooks/setting/useRootSetting';

  import { MenuModeEnum, MenuSplitTyeEnum } from '/@/enums/menuEnum';
  import { SettingButtonPositionEnum } from '/@/enums/appEnum';
  import { AppLocalePicker } from '/@/components/Application';

  import {
    UserDropDown,
    LayoutBreadcrumb,
    FullScreen,
    Notify,
    ErrorAction,
    BigScreen,
    UploadFileView,
    UploadFileSuspension,
  } from './components';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useDesign } from '/@/hooks/web/useDesign';

  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useLocale } from '/@/locales/useLocale';

  import { useUserStore } from '/@/store/modules/user';
  import { getPortalListByUser } from '/@/api/system/system';
  import { useGo } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'LayoutHeader',
    components: {
      Header: Layout.Header,
      AppLogo,
      LayoutTrigger,
      LayoutBreadcrumb,
      LayoutMenu,
      UserDropDown,
      AppLocalePicker,
      FullScreen,
      Notify,
      AppSearch,
      ErrorAction,
      SettingDrawer: createAsyncComponent(() => import('/@/layouts/default/setting/index.vue'), {
        loading: true,
      }),
      Icon,
      Select,
      SelectOption: Select.Option,
      BigScreen,
      UploadFileView,
      UploadFileSuspension,
    },
    props: {
      fixed: propTypes.bool,
    },
    setup(props) {
      const { prefixCls } = useDesign('layout-header');

      const userStore = useUserStore();

      const { name = '' } = userStore.getUserInfo || {};

      const aryPortal = ref<Recordable>([]);
      const go = useGo();

      const {
        getShowTopMenu,
        getShowHeaderTrigger,
        getSplit,
        getIsMixMode,
        getMenuWidth,
        getIsMixSidebar,
      } = useMenuSetting();
      const { getUseErrorHandle, getShowSettingButton, getSettingButtonPosition } =
        useRootSetting();

      const {
        getHeaderTheme,
        getShowFullScreen,
        getShowNotice,
        getShowContent,
        getShowBread,
        getShowHeaderLogo,
        getShowHeader,
        getShowSearch,
      } = useHeaderSetting();

      const { getShowLocalePicker } = useLocale();

      const { getIsMobile } = useAppInject();

      const getHeaderClass = computed(() => {
        const theme = unref(getHeaderTheme);
        return [
          prefixCls,
          {
            [`${prefixCls}--fixed`]: props.fixed,
            [`${prefixCls}--mobile`]: unref(getIsMobile),
            [`${prefixCls}--${theme}`]: theme,
          },
        ];
      });

      const getShowSetting = computed(() => {
        if (!unref(getShowSettingButton)) {
          return false;
        }
        const settingButtonPosition = unref(getSettingButtonPosition);

        if (settingButtonPosition === SettingButtonPositionEnum.AUTO) {
          return unref(getShowHeader);
        }
        return settingButtonPosition === SettingButtonPositionEnum.HEADER;
      });

      const getLogoWidth = computed(() => {
        if (!unref(getIsMixMode) || unref(getIsMobile)) {
          return {};
        }
        const width = unref(getMenuWidth) < 180 ? 180 : unref(getMenuWidth);
        return { width: `${width}px` };
      });

      const getSplitType = computed(() => {
        return unref(getSplit) ? MenuSplitTyeEnum.TOP : MenuSplitTyeEnum.NONE;
      });

      const getMenuMode = computed(() => {
        return unref(getSplit) ? MenuModeEnum.HORIZONTAL : null;
      });

      const showId = ref(false);
      const id = ref('');
      const uploadFileViewType = ref('tab');

      onMounted(() => {
        getPortalListByUser().then((data) => {
          aryPortal.value = data;
          console.log('getPortalListByUser aryPortal.value', aryPortal.value);
        });

        let serverInfo = userStore.getServerInfo;
        console.log('serverInfo', serverInfo);
        showId.value = serverInfo.showId;
        id.value = serverInfo.id;
        if (serverInfo.isUploadPanelBtnSuspension) {
          uploadFileViewType.value = 'tab';
        } else {
          uploadFileViewType.value = 'drawer';
        }
      });
      const selectPortal = ref('请选择门户');
      function openPortal(item) {
        let path = 'portal';
        go({
          path: path,
          query: {
            id: item.id,
            name: item.name,
          },
        });
      }

      return {
        prefixCls,
        getHeaderClass,
        getShowHeaderLogo,
        getHeaderTheme,
        getShowHeaderTrigger,
        getIsMobile,
        getShowBread,
        getShowContent,
        getSplitType,
        getSplit,
        getMenuMode,
        getShowTopMenu,
        getShowLocalePicker,
        getShowFullScreen,
        getShowNotice,
        getUseErrorHandle,
        getLogoWidth,
        getIsMixSidebar,
        getShowSettingButton,
        getShowSetting,
        name,
        getShowSearch,
        aryPortal,
        openPortal,
        selectPortal,
        showId,
        id,
        uploadFileViewType,
      };
    },
  });
</script>
<style lang="less">
  @import './index.less';
</style>
