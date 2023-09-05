<template>
  <div class="h-full">
    <Tabs
      class="bg-white"
      v-if="tabList.length > 1"
      v-model:activeKey="key"
      @change="onTabChange"
      centered
    >
      <TabPane :key="item.id" :tab="item.name" v-for="item in tabList" />
    </Tabs>
    <Portal :portalId="portalId" class="h-full" />
  </div>
</template>
<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { getPortalNames } from '/@/api/system/system';
  import { Tabs, TabPane } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import Portal from './index.vue';
  const { currentRoute } = useRouter();

  const defaultPortalId = ref(55);
  const portalId = ref(null);

  onMounted(() => {
    fetch();
  });
  const key = ref(55);
  const tabList = ref<Recordable>([]);

  const fetch = () => {
    // 如果是从菜单进入
    let { formCode } = currentRoute.value.meta;
    if (formCode) {
      getPortalNames({ portals: formCode }).then((res) => {
        tabList.value = res;
        key.value = tabList.value[0].id;
        if (tabList.value[0].id == null || tabList.value[0].id == undefined) {
          portalId.value = defaultPortalId.value;
        } else {
          portalId.value = tabList.value[0].id;
        }
      });
    }
  };

  const onTabChange = (tabKey) => {
    portalId.value = tabKey;
    key.value = tabKey;
  };
</script>
<style lang="less" scoped>
  :deep(.ant-card) {
    background-color: transparent;
  }
</style>
