<template>
  <div class="bg-white h-full overflow-hidden">
    <Tabs v-model:activeKey="activeKey" centered>
      <TabPane key="1" tab="我的日程" />
      <TabPane key="2" tab="全部日程" />
    </Tabs>
    <template v-if="activeKey === '1'">
      <MySchedule />
    </template>
    <template v-if="activeKey === '2'">
      <AllSchedule />
    </template>
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  // import { useMessage } from '/@/hooks/web/useMessage';
  import { Tabs, TabPane } from 'ant-design-vue';
  import MySchedule from './modules/mySchedule/index.vue';
  import AllSchedule from './modules/allSchedule/index.vue';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'schedule',
    components: {
      Tabs,
      TabPane,
      MySchedule,
      AllSchedule,
    },
    setup() {
      // const { createMessage } = useMessage();
      const activeKey = ref('1');
      onMounted(() => {
        init();
      });
      const init = () => {};

      return {
        activeKey,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.ant-tabs-top > .ant-tabs-nav, .ant-tabs-bottom > .ant-tabs-nav, .ant-tabs-top
      > div
      > .ant-tabs-nav, .ant-tabs-bottom > div > .ant-tabs-nav) {
    margin: 0 !important;
  }
</style>
