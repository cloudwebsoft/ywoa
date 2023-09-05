<template>
  <span :class="`${prefixCls}__extra-redo`" @click="handleRedo">
    <RedoOutlined :spin="loading" />
  </span>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { RedoOutlined } from '@ant-design/icons-vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { useMultipleTabWithOutStore } from '/@/store/modules/multipleTab';

  export default defineComponent({
    name: 'TabRedo',
    components: { RedoOutlined },

    setup() {
      const loading = ref(false);

      const { prefixCls } = useDesign('multiple-tabs-content');
      const { refreshPage } = useTabs();

      const multipleTabStore = useMultipleTabWithOutStore();
      async function handleRedo() {
        loading.value = true;
        await refreshPage();
        multipleTabStore.updateIsRefreshPage(true);
        setTimeout(() => {
          loading.value = false;
          multipleTabStore.updateIsRefreshPage(false);
          // Animation execution time
        }, 1200);
      }
      return { prefixCls, handleRedo, loading };
    },
  });
</script>
