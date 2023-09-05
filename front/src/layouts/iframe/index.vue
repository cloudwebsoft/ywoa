<template>
  <div v-if="showFrame">
    <template v-for="frame in frameList" :key="frame.path">
      <FramePage
        v-if="frame.meta.frameSrc && hasRenderFrame(frame.name) && frame.isReload"
        v-show="showIframe(frame)"
        :frameSrc="frame.meta.frameSrc"
      />
    </template>
  </div>
</template>
<script lang="ts">
  import { defineComponent, unref, computed, onActivated, watch, ref } from 'vue';
  import FramePage from '/@/views/sys/iframe/index.vue';

  import { useFrameKeepAlive } from './useFrameKeepAlive';
  import { useMultipleTabWithOutStore } from '/@/store/modules/multipleTab';

  import { useRouter } from 'vue-router';
  export default defineComponent({
    name: 'FrameLayout',
    components: { FramePage },
    setup() {
      const router = useRouter();
      const { getFramePages, hasRenderFrame, showIframe } = useFrameKeepAlive();
      const frameList = ref([]);
      frameList.value = JSON.parse(JSON.stringify(getFramePages.value));
      frameList.value.forEach((item) => {
        item.isReload = true;
      });
      const showFrame = computed(() => unref(getFramePages).length > 0);
      const isReload = ref(true);
      const multipleTabStore = useMultipleTabWithOutStore();
      multipleTabStore.updateIsRefreshPage(false);
      watch(
        () => multipleTabStore.isRefreshPage,
        (newVal) => {
          if (newVal) {
            // multipleTabStore.updateIsRefreshPage(false);
            const { currentRoute } = router;
            const itemIndex = unref(frameList).findIndex((item) => {
              return unref(currentRoute).fullPath.indexOf(item.path.split('/')[1]) != -1;
            });
            if (itemIndex == -1) return;
            frameList.value[itemIndex].isReload = false;
            setTimeout(() => {
              frameList.value[itemIndex].isReload = true;
            }, 100);
            frameList.value.forEach((item) => {
              if (item.name == unref(currentRoute).name) {
                item.isReload = false;
                setTimeout(() => {
                  item.isReload = true;
                }, 100);
              }
            });
          }
        },
      );
      return { frameList, hasRenderFrame, showIframe, showFrame, isReload };
    },
  });
</script>
