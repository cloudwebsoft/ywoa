<template>
  <div :class="prefixCls">
    <a-button type="primary" block @click="handleSave" class="mb-3" :loading="isUiSetup">
      <SaveOutlined class="mr-2" />
      {{ t('layout.setting.saveBtn') }}
    </a-button>

    <a-button type="primary" block @click="handleCopy">
      <CopyOutlined class="mr-2" />
      {{ t('layout.setting.copyBtn') }}
    </a-button>

    <a-button color="warning" block @click="handleResetSetting" class="my-3">
      <RedoOutlined class="mr-2" />
      {{ t('common.resetText') }}
    </a-button>

    <a-button color="error" block @click="handleClearAndRedo">
      <RedoOutlined class="mr-2" />
      {{ t('layout.setting.clearBtn') }}
    </a-button>
  </div>
</template>
<script lang="ts">
  import { defineComponent, unref, ref } from 'vue';

  import { CopyOutlined, RedoOutlined, SaveOutlined } from '@ant-design/icons-vue';

  import { useAppStore } from '/@/store/modules/app';
  import { usePermissionStore } from '/@/store/modules/permission';
  import { useMultipleTabStore } from '/@/store/modules/multipleTab';
  import { useUserStore } from '/@/store/modules/user';

  import { useDesign } from '/@/hooks/web/useDesign';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';

  import { updateColorWeak } from '/@/logics/theme/updateColorWeak';
  import { updateGrayMode } from '/@/logics/theme/updateGrayMode';
  import defaultSetting from '/@/settings/projectSetting';

  import { getUpdateUiSetup } from '/@/api/system/system';
  import { useGlobSetting } from '/@/hooks/setting';

  export default defineComponent({
    name: 'SettingFooter',
    components: { CopyOutlined, RedoOutlined, SaveOutlined },
    setup() {
      const permissionStore = usePermissionStore();
      const { prefixCls } = useDesign('setting-footer');
      const { t } = useI18n();
      const { createSuccessModal, createMessage } = useMessage();
      const tabStore = useMultipleTabStore();
      const userStore = useUserStore();
      const appStore = useAppStore();
      const isUiSetup = ref(false);
      //保存配置
      function handleSave() {
        const uiSetup = JSON.stringify(unref(appStore.getProjectConfig), null, 2);
        isUiSetup.value = true;
        const { applicationCode } = useGlobSetting();
        getUpdateUiSetup({ uiSetup, applicationCode }).then((res) => {
          isUiSetup.value = false;
          createMessage.info(res.msg);
        });
      }
      function handleCopy() {
        const { isSuccessRef } = useCopyToClipboard(
          JSON.stringify(unref(appStore.getProjectConfig), null, 2),
        );
        unref(isSuccessRef) &&
          createSuccessModal({
            title: t('layout.setting.operatingTitle'),
            content: t('layout.setting.operatingContent'),
          });
      }
      function handleResetSetting() {
        try {
          appStore.setProjectConfig(defaultSetting);
          const { colorWeak, grayMode } = defaultSetting;
          // updateTheme(themeColor);
          updateColorWeak(colorWeak);
          updateGrayMode(grayMode);
          createMessage.success(t('layout.setting.resetSuccess'));
        } catch (error: any) {
          createMessage.error(error);
        }
      }

      function handleClearAndRedo() {
        localStorage.clear();
        appStore.resetAllState();
        permissionStore.resetState();
        tabStore.resetState();
        userStore.resetState();
        userStore.setToken(undefined);
        location.reload();
      }
      return {
        prefixCls,
        t,
        handleSave,
        handleCopy,
        handleResetSetting,
        handleClearAndRedo,
        isUiSetup,
      };
    },
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-setting-footer';

  .@{prefix-cls} {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
</style>
