<template>
  <iframe
    :src="pageUrl"
    frameborder="0"
    style="width: 100%; height: 100%"
    v-if="pageUrl && pageUrl.length > 0"
  ></iframe>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { getServerInfo } from '/@/api/process/process';
  import { getToken } from '/@/utils/auth';
  import { cloneDeep } from 'lodash-es';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: 'ManagerPage',
    components: {},
    setup(_, {}) {
      const route = useRoute();
      const query = ref<any>({});
      const url = ref('');
      const pageUrl = ref('');
      const userStore = useUserStore();

      let serverInfo = userStore.getServerInfo;

      onMounted(() => {
        // 此时query.value.urlParams为undefined，所以不能search()
        // search();
      });
      //因为本组件会被多次引用，导致会有多个tab,如果缓存就不会刷新iframe，所以监听路由变化，再次刷新页面
      watch(
        () => route.query.urlParams,
        (newVal) => {
          if (newVal && query.value?.urlParams != newVal) {
            search();
          }
        },
      );
      function search() {
        if (route.query) {
          query.value = cloneDeep(route.query);
          getUrl();
        }
      }
      function getUrl() {
        // getServerInfo().then((res) => {
        //   url.value = res.url;
        //   pageUrl.value = url.value + query.value.urlParams + '&Authorization=' + token;
        // });
	// getServerInfo卡顿时延时较长
        pageUrl.value = serverInfo.url + query.value.urlParams + '&Authorization=' + token;
      }

      const token = getToken();
      return {
        pageUrl,
      };
    },
  });
</script>
