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
    :destroyOnClose="true"
    @close="closeCurrentDrawer"
  >
    <ProcessShowView
      v-model:flowId="flowId"
      :visitKey="visitKey"
      :isTab="isTab"
      @handle-current="handleEdit"
    />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, inject } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { removeScript, removeLink } from '/@/utils/utils';
  import ProcessShowView from './processShowView.vue';

  export default defineComponent({
    name: 'ProcessShowDrawer',
    components: {
      BasicDrawer,
      ProcessShowView,
    },
    emits: ['success', 'register', 'handleCurrent'],
    setup(_, { emit }) {
      const flowId = ref('-1');
      const visitKey = ref('');
      const isTab = ref(true);
      const getTitle = ref('查看流程');

      //初始化抽屉
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        console.log('data', data);
        setDrawerProps({ confirmLoading: false });
        flowId.value = data.flowId;
        visitKey.value = data.visitKey;
        if (data.isTab === false) {
          isTab.value = data.isTab;
        } else {
          isTab.value = true;
        }
      });

      function handleEdit(record) {
        closeDrawer();
        emit('handleCurrent', record);
      }

      // 关闭抽屉
      function closeCurrentDrawer() {
        emit('success');
        removeScript('-srcShow');
        removeLink();
        flowId.value = '-1';
      }

      return {
        registerDrawer,
        handleEdit,
        isTab,
        flowId,
        visitKey,
        closeCurrentDrawer,
        getTitle,
      };
    },
  });
</script>
<style scoped>
  @import '../../../assets/css/css.css';
  ::v-deep .ant-tabs-top > .ant-tabs-nav,
  .ant-tabs-bottom > .ant-tabs-nav,
  .ant-tabs-top > div > .ant-tabs-nav,
  .ant-tabs-bottom > div > .ant-tabs-nav {
    margin: 0 !important;
  }
</style>
