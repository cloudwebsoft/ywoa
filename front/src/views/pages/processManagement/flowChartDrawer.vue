<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="90%"
    :showOkBtn="false"
    :cancelText="'关闭'"
    ref="containerRef"
  >
    <FlowChart :flowId="flowId" activeKey="2" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getFlowShow } from '/@/api/process/process';
  import FlowChart from './flowChart.vue';
  export default defineComponent({
    name: 'ProcessDrawer',
    components: {
      BasicDrawer,
      FlowChart,
    },
    emits: ['success', 'register'],
    setup(_, {}) {
      const content = ref('');
      let getTitle = '查看流程';
      const containerRef = ref();
      const flowId = ref('');
      //初始化抽屉
      const [registerDrawer, { setDrawerProps }] = useDrawerInner(async (data: any) => {
        setDrawerProps({ confirmLoading: false });
        flowId.value = data.flowId;
        // getFlowProcessContent();
      });
      //获取当前流程信息
      let flowProcessData = ref<any>({});
      async function getFlowProcessContent() {
        let data = await getFlowShow({ flowId: flowId.value });
        flowProcessData.value = data;
        getTitle = flowProcessData.value.flowTitle;
      }

      return {
        registerDrawer,
        content,
        getTitle,
        containerRef,
        flowProcessData,
        flowId,
      };
    },
  });
</script>
