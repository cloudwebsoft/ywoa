<template>
  <RouterView>
    <template #default="{ Component, route }">
      <keep-alive v-if="openCache" :include="getCaches">
        <component :is="wrap(route, Component)" :key="route.fullPath" />
      </keep-alive>
      <component v-else :is="Component" :key="route.fullPath" />
      <!-- <transition name="fade-slide" mode="out-in" appear>  && route.path != '/managerPage'
        <component
          :is="Component"
          :key="route.fullPath"
          v-if="(!openCache || !route.meta.ignoreKeepAlive) && route.path != '/managerPage'"
        />
      </transition> -->
      <!-- v-if="
            openCache &&
            route.meta.ignoreKeepAlive &&
            route.path != '/managerPage' &&
            route.meta.preCode != 'iframe'
          " -->
      <!-- route.meta.ignoreKeepAlive -->
      <!-- <transition
        :name="
          getTransitionName({
            route,
            openCache,
            enableTransition: getEnableTransition,
            cacheTabs: getCaches,
            def: getBasicTransition,
          })
        "
        mode="out-in"
        appear
      >
        <keep-alive v-if="openCache" :include="getCaches">
          <component :is="Component" :key="route.fullPath" />
        </keep-alive>
        <div v-else :key="route.name">
          <component :is="Component" :key="route.fullPath" />
        </div>
      </transition> -->
    </template>
  </RouterView>

  <IManagerPage v-show="curRoute.path == '/managerPage'" />
  <!-- v-show="openCache && curRoute.meta.ignoreKeepAlive && curRoute.meta.preCode == 'iframe'" -->
  <FrameLayout v-if="getCanEmbedIFramePage" />
</template>

<script lang="ts">
  import { computed, defineComponent, unref, h } from 'vue';

  import FrameLayout from '/@/layouts/iframe/index.vue';
  import IManagerPage from '/@/layouts/iframe/IManagerPage.vue';

  import { useRootSetting } from '/@/hooks/setting/useRootSetting';

  import { useTransitionSetting } from '/@/hooks/setting/useTransitionSetting';
  import { useMultipleTabSetting } from '/@/hooks/setting/useMultipleTabSetting';
  import { getTransitionName } from './transition';

  import { useMultipleTabStore } from '/@/store/modules/multipleTab';

  import { useRoute, useRouter } from 'vue-router';

  export default defineComponent({
    name: 'PageLayout',
    components: { FrameLayout, IManagerPage },
    setup() {
      const { getShowMultipleTab } = useMultipleTabSetting();
      const tabStore = useMultipleTabStore();

      const { getOpenKeepAlive, getCanEmbedIFramePage } = useRootSetting();

      const { getBasicTransition, getEnableTransition } = useTransitionSetting();

      const openCache = computed(() => unref(getOpenKeepAlive) && unref(getShowMultipleTab));

      const getCaches = computed((): string[] => {
        console.log('tabStore.getCachedTabList', tabStore.getCachedTabList);

        // if (!unref(getOpenKeepAlive)) {
        //   return [];
        // }
        return tabStore.getCachedTabList;
      });

      const curRoute = useRoute();
      const router = useRouter();
      console.log('route', { curRoute, router, getRoutes: router.getRoutes() });

      const wrapperMap = new Map();
      const wrap = ({ name, query }, component) => {
        let wrapper;
        console.log('wrapperMap', wrapperMap);
        let wrapperName = name;
        if (query?.cacheName) {
          wrapperName = query.cacheName;
        }

        if (wrapperMap.has(wrapperName)) {
          wrapper = wrapperMap.get(wrapperName);
        } else {
          wrapper = {
            name: wrapperName,

            render() {
              return h('div', { className: 'cloud-page-wrapper' }, component);
            },
          };

          wrapperMap.set(wrapperName, wrapper);
        }

        return h(wrapper);
      };
      return {
        getTransitionName,
        openCache,
        getEnableTransition,
        getBasicTransition,
        getCaches,
        getCanEmbedIFramePage,
        curRoute,
        wrap,
      };
    },
  });
</script>
