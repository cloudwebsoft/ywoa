<template>
  <div class="bg-white h-full flex justify-between">
    <Card title="流程图" :bordered="false" :style="[{ width: '80%' }]" class="pt-2">
      <!-- :style="[{ width: dataRef.canUserSeeFlowChart ? '80%' : '0' }]" -->
      <FlowChart v-model:flowId="query.flowId" activeKey="2" :isFlowDebug="true" />
    </Card>
    <Card
      title="请选择节点"
      :bordered="false"
      :style="[{ width: dataRef.canUserSeeFlowChart ? '100%' : '19%' }]"
    >
      <template #extra>
        <Popconfirm
          placement="top"
          title="确定重新登录吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="loginAgain"
        >
          <a-button type="small"> 重新登录 </a-button>
        </Popconfirm>
      </template>
      <div class="w-full overflow-auto p-2">
        <div class="text-black font-bold">下一节点：</div>
        <div v-for="(item, index) in dataRef.nextActions || []" :key="index" class="flex">
          <div> {{ item.actionTitle }}： </div>
          <div>
            <div
              v-for="el in item.checkers || []"
              :key="el.userName"
              class="cursor-pointer hover:text-red-500"
              @click="handleNextNode(el)"
            >
              {{ el.realName }}
            </div>
          </div>
        </div>
        <div class="text-black font-bold mt-2">全部待办节点：</div>
        <div v-for="item in dataRef.allActions || []" :key="item.realName">
          {{ item.actionTitle }}：<span
            class="cursor-pointer hover:text-red-500"
            @click="handleNextNode(item)"
            >{{ item.realName }}</span
          >
        </div>
      </div>
    </Card>
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  // import { useMessage } from '/@/hooks/web/useMessage';
  import { Card, Popconfirm } from 'ant-design-vue';
  import FlowChart from './flowChart.vue';
  import { useRoute } from 'vue-router';
  import { getTestInfo, getTokenByUser } from '/@/api/process/process';
  import { useUserStore } from '/@/store/modules/user';
  import { useGo } from '/@/hooks/web/usePage';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'flowDebug',
    components: { Card, FlowChart, Popconfirm },
    setup() {
      const route = useRoute();
      const query = ref<any>({});
      const dataRef = ref<any>({});
      // const { createMessage } = useMessage();
      onMounted(() => {
        console.log('route.query', route.query);
        if (route.query) {
          query.value = route.query;
        }
        console.log('query.value', query.value);
        query.value.flowId = query.value.flowId - 0;
        // query.value.flowId = 471;
        init();
      });
      const init = () => {
        fetch();
      };
      function fetch() {
        //455
        getTestInfo({ myActionId: query.value.myActionId }).then((res) => {
          dataRef.value = res;
        });
      }
      const userStore = useUserStore();
      //重新登录
      function loginAgain() {
        userStore.logout(true);
      }

      const go = useGo();
      //下一节点
      function handleNextNode(record: any) {
        getTokenByUser({ newUserName: record.userName }).then((res) => {
          userStore.setToken(res);
          go({
            path: '/processHandleView',
            query: {
              myActionId: record.nextMyActionId,
              isDebug: true,
            },
          });
        });
      }
      return {
        query,
        loginAgain,
        handleNextNode,
        dataRef,
      };
    },
  });
</script>
<style lang="less" scoped>
  ::v-deep .ant-card-body {
    padding: 0;
    height: calc(100vh - 150px);
    overflow: auto;
    padding-top: 1px;
  }
</style>
