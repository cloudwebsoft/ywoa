<template>
  <div>
    <WorkCalendar />
    <!-- <Tabs v-model:activeKey="activeKey" centered>
      <TabPane :tab="item.label" force-render v-for="item in tabs" :key="item.value" />
    </Tabs>
    <div v-if="activeKey === 1">
      <WorkCalendar />
    </div>
    <div v-if="activeKey === 2">
      <SmartCalendar />
    </div> -->
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { Tabs, TabPane } from 'ant-design-vue';
  import SmartCalendar from '../../pages/smartModule/smartCalendar.vue';
  import WorkCalendar from './workCalendar.vue';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'workCalendarIndex',
    components: { Tabs, TabPane, SmartCalendar, WorkCalendar },
    setup() {
      const activeKey = ref(1);
      const tabs = [
        {
          value: 1,
          label: '工作日历',
        },
        {
          value: 2,
          label: '日历视图',
        },
      ];
      return {
        activeKey,
        tabs,
      };
    },
  });
</script>
