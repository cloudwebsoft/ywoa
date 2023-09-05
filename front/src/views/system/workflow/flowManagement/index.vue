<template>
  <PageWrapper dense contentFullHeight fixedHeight contentClass="flex">
    <div class="w-1/4 xl:w-1/5 m-2 overflow-hidden bg-white">
      <div>
        <Select :options="typeTree" class="w-full" />
      </div>
      <div class="h-full overflow-auto pb-2">
        <FlowTree
          @select="handleSelect"
          @del-action="delAction"
          @ok="onClose"
          ref="treeRef"
          @add-action="addAction"
          @tree-context-menu="treeContextMenu"
          @edit-action="handleEditAction"
          @root-record="handleRootRecord"
        />
      </div>
    </div>
    <div class="mt-2 mb-2 w-3/4 xl:w-4/5 bg-white">
      <Tabs v-model:activeKey="activeKey" centered class="h-full">
        <TabPane
          v-for="(item, index) in tabList"
          :key="item.key"
          :tab="item.title"
          class="h-full"
          forceRender
        >
          <component
            :is="item.component"
            :current-record="dataRef"
            :current-key="item.key"
            :active-key="activeKey"
            @success="handleSuccess"
            :ref="setRefs(item.key)"
          />
        </TabPane>
      </Tabs>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref, computed, unref } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { Tabs, TabPane, Select } from 'ant-design-vue';
  import { useRefs } from '/@/hooks/core/useRefs';
  import FlowTree from './modules/flowTree/FlowTree.vue';
  import Attribute from './modules/attribute/Attribute.vue';
  import AttributeAdd from './modules/attribute/Attribute.vue';
  import Authority from './modules/authority/Authority.vue';
  import DisplayRules from './modules/displayRules/DisplayRules.vue';
  import EfficiencyAnalysis from './modules/efficiencyAnalysis/EfficiencyAnalysis.vue';
  import FlowPicture from './modules/flowPicture/FlowPicture.vue';
  import MonthlyStatistics from './modules/monthlyStatistics/MonthlyStatistics.vue';
  import RecordList from './modules/recordList/RecordList.vue';
  import YearStatistics from './modules/yearStatistics/YearStatistics.vue';

  export default defineComponent({
    name: 'FlowManagement',
    components: {
      PageWrapper,
      FlowTree,
      Tabs,
      TabPane,
      Select,
      Attribute,
      AttributeAdd,
      Authority,
      DisplayRules,
      EfficiencyAnalysis,
      FlowPicture,
      MonthlyStatistics,
      RecordList,
      YearStatistics,
    },
    setup() {
      const [refs, setRefs] = useRefs();
      const treeRef: any = ref(null);
      const rootRecord = ref({}); //顶级对象
      let dataRef = ref({});

      function onClose(e?: number) {
        if (e != 1) {
          //1是新增，不用清空
          dataRef.value = {};
        }
      }
      const activeKey = ref('Attribute');

      const typeTree = ref([
        {
          value: '1',
          label: '一',
        },
        {
          value: '2',
          label: '二',
        },
        {
          value: '3',
          label: '三',
        },
      ]);

      const tabList = ref([]);
      //layer3tab
      const layer1 = ref([
        {
          title: '属性',
          key: 'Attribute',
          component: 'Attribute',
        },
        {
          title: '权限',
          key: 'Authority',
          component: 'Authority',
        },
        {
          title: '添加',
          key: 'AttributeAdd',
          component: 'AttributeAdd',
        },
        {
          title: '记录',
          key: 'RecordList',
          component: 'RecordList',
        },
      ]);
      //layer2tab
      const layer2 = ref([
        {
          title: '属性',
          key: 'Attribute',
          component: 'Attribute',
        },
        {
          title: '权限',
          key: 'Authority',
          component: 'Authority',
        },
        {
          title: '添加',
          key: 'AttributeAdd',
          component: 'AttributeAdd',
        },
      ]);
      // layer3tab
      const layer3 = ref([
        {
          title: '流程图',
          key: 'FlowPicture',
          component: 'FlowPicture',
        },
        {
          title: '属性',
          key: 'Attribute',
          component: 'Attribute',
        },
        {
          title: '权限',
          key: 'Authority',
          component: 'Authority',
        },
        {
          title: '显示规则',
          key: 'DisplayRules',
          component: 'DisplayRules',
        },
        {
          title: '记录',
          key: 'RecordList',
          component: 'RecordList',
        },
        {
          title: '月统计',
          key: 'MonthlyStatistics',
          component: 'MonthlyStatistics',
        },
        {
          title: '年统计',
          key: 'YearStatistics',
          component: 'YearStatistics',
        },
        {
          title: '效率分析',
          key: 'EfficiencyAnalysis',
          component: 'EfficiencyAnalysis',
        },
      ]);

      tabList.value = layer1.value;
      //树新增事件
      const addAction = (key: string) => {
        activeKey.value = 'AttributeAdd';
        refs.value['AttributeAdd'].initData();
      };

      //树删除事件
      const delAction = () => {
        if (!dataRef.value.code) {
          createMessage.warning('请选择节点');
          return;
        }
        unref(treeRef)?.setMenuDelete(dataRef.value.code);
        onClose();
      };

      const handleSelect = (code = '', trc) => {
        const data = trc.node.dataRef;
        setTabs(data);
      };
      //右击选择
      const treeContextMenu = (record: Record) => {
        setTabs(record);
      };

      const setTabs = (record: Record) => {
        dataRef.value = record;
        const { layer } = record;
        if (layer == 1) {
          tabList.value = layer1.value;
        } else if (layer == 2) {
          tabList.value = layer2.value;
        } else if (layer == 3) {
          tabList.value = layer3.value;
        }
        activeKey.value = tabList.value[0].key;
        setTimeout(() => {
          refs.value['Attribute'].initData();
        }, 10);
      };
      //成功回调
      const handleSuccess = (type) => {
        treeRef.value.fetch();
        if (type == 'add') {
          activeKey.value = tabList.value[0].key;
        }
      };
      //编辑节点
      const handleEditAction = (code) => {
        activeKey.value = 'Attribute';
      };
      //获取顶级code
      const handleRootRecord = (record) => {
        rootRecord.value = record;
      };
      return {
        handleSelect,
        treeRef,
        addAction,
        delAction,
        onClose,
        treeContextMenu,
        tabList,
        activeKey,
        typeTree,
        dataRef,
        handleSuccess,
        setRefs,
        handleEditAction,
        handleRootRecord,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.ant-card-head) {
    min-height: 37px;
  }
  :deep(.ant-card-head-title) {
    padding: 0;
    line-height: 35px;
  }

  :deep(.ant-tabs-content) {
    height: 100%;
  }
</style>
