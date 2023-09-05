<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="90%"
    :maskClosable="false"
    :showOkBtn="false"
    :cancelText="'关闭'"
    :destroyOnClose="true"
    @close="closeCurrentDrawer"
    :contentWrapperStyle="{ overflowX: 'auto' }"
    class="overflow-x-auto"
  >
    <ProcessHandleFreeView
      v-if="type == 1"
      v-model:myActionId="myActionId"
      @close-drawer="closeDrawer"
      @success="handleSuccess"
      @show-view="showView"
    />
    <ProcessHandleView
      v-else
      :isInDrawer="true"
      v-model:myActionId="myActionId"
      @close-drawer="closeDrawer"
      @success="handleSuccess"
      @show-view="showView"
    />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, inject } from 'vue';
  import ProcessHandleView from './processHandleView.vue';
  import ProcessHandleFreeView from './processHandleFreeView.vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { removeScript, removeLink } from '/@/utils/utils';

  export default defineComponent({
    name: 'ProcessDrawer',
    components: {
      BasicDrawer,
      ProcessHandleView,
      ProcessHandleFreeView,
    },
    emits: ['register', 'success', 'showView'],
    setup(_, { emit }) {
      const getTitle = '处理流程';
      const myActionId = ref<number>(0);
      const type = ref<number>(2);
      const curFormUtil: any = inject('curFormUtil');

      //初始化抽屉
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner((data) => {
        setDrawerProps({ confirmLoading: false });
        myActionId.value = data.myActionId;
        type.value = data.type;
        console.log('processDrawer type', type.value);
      });

      //成功回调
      function handleSuccess() {
        emit('success');
      }

      //显示详情抽屉
      function showView(record: object) {
        emit('showView', record);
      }

      //关闭抽屉
      let srcId = ref('-src');
      function closeCurrentDrawer() {
        // 因在菜单项设缓存后，在processHandleView中的onmounted中作curFormUtil.close处理会无效
        // curFormUtil.close();

        // emit('success');
        removeLink();
        removeScript(srcId.value);
        myActionId.value = 0;
      }

      return {
        registerDrawer,
        closeDrawer,
        closeCurrentDrawer,
        handleSuccess,
        showView,
        getTitle,
        myActionId,
        type,
      };
    },
  });
</script>
<style lang="less">
  .scrollbar > .scrollbar__bar.is-horizontal {
    display: block !important;
  }
</style>
