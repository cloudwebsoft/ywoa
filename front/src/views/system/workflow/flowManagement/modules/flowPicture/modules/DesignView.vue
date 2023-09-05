<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    wrap-class-name="full-modal"
    :canFullscreen="false"
    :destroyOnClose="true"
  >
    <div class="flex">
      <div class="w-3/4 overflow-auto view-style">
        <EditFlowChart :typeCode="typeCode" ref="editFlowChartRef" />
      </div>
      <div class="w-1/4 overflow-auto view-style pl-2">
        <Tabs v-model:activeKey="activeKey" type="card">
          <TabPane tab="流程" key="1">
            <FlowEdit />
          </TabPane>
          <TabPane tab="属性" key="2">
            <NodeEdit />
          </TabPane>
          <TabPane tab="回写" key="3">
            <Writeback />
          </TabPane>
          <TabPane tab="事件" key="4">
            <EventDesign />
          </TabPane>
        </Tabs>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Tabs, TabPane } from 'ant-design-vue';
  import { FlowEdit, Writeback, EventDesign } from './modules';
  import EditFlowChart from './modules/designViewFlowChart/index.vue';
  import NodeEdit from './modules/nodeEdit/NodeEdit.vue';
  export default defineComponent({
    name: 'DeptModal',
    components: {
      BasicModal,
      Tabs,
      TabPane,
      FlowEdit,
      Writeback,
      EventDesign,
      EditFlowChart,
      NodeEdit,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const editFlowChartRef = ref<HTMLElement>(null);
      const isUpdate = ref(true);
      const activeKey = ref('2');
      const typeCode = ref('');
      const { createMessage } = useMessage();
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '100%' });
        isUpdate.value = !!data?.isUpdate;
        typeCode.value = data?.typeCode;
        await nextTick();
        editFlowChartRef.value.fetch();
      });

      const getTitle = '设计';

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });
          // TODO custom api
          if (unref(selectUserList).length === 0) {
            createMessage.warning('请选择用户');
            return;
          }
          if (unref(type) === 1) {
            if (unref(selectUserList).length != 1) {
              createMessage.warning('只能选择一条数据');
              return;
            }
            closeModal();
            emit('success', unref(selectUserList));
          } else {
            closeModal();
            emit('success', unref(selectUserList));
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }
      onMounted(() => {});
      return {
        registerModal,
        getTitle,
        handleSubmit,
        editFlowChartRef,
        typeCode,
        activeKey,
      };
    },
  });
</script>
<style lang="less">
  .full-modal {
    .ant-modal {
      max-width: 100%;
      top: 0;
      padding-bottom: 0;
      margin: 0;
    }
    .ant-modal-content {
      display: flex;
      flex-direction: column;
      height: calc(100vh);
    }
    .ant-modal-body {
      flex: 1;
    }
  }

  .view-style {
    height: calc(100vh - 140px);
  }
</style>
