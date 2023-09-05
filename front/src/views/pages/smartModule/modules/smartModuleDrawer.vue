<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="85%"
    @ok="handleSubmit"
    :cancelText="'关闭'"
    :maskClosable="false"
    :destroyOnClose="true"
    @close="onClose"
  >
    <SmartModuleAddEditView
      ref="smartRef"
      @edit-action="handleEditAction"
      @launch-flow-action="handleLaunchFlowAction"
      :getVisible="getVisible"
      @close="handleClose"
      @tab-change="handleTabChange"
    />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted, h, inject, nextTick } from 'vue';
  import { BasicDrawer, useDrawerInner, useDrawer } from '/@/components/Drawer';
  import SmartModuleAddEditView from './smartModuleAddEditView.vue';

  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModuleDrawer',
    components: {
      BasicDrawer,
      SmartModuleAddEditView,
    },
    emits: ['success', 'register', 'close', 'editAction', 'launchFlowAction'],
    setup(_, { emit }) {
      const isUpdate = ref(1); //1:新增，2编辑，3查看
      let dataRef = ref<any>({});
      const dataRecord = ref<any>({});
      const smartRef = ref(null);

      const [registerDrawer, { setDrawerProps, closeDrawer, getVisible }] = useDrawerInner(
        async (data) => {
          setDrawerProps({ confirmLoading: false });
          dataRecord.value = data.record || {};
          dataRef.value = {};
          isUpdate.value = data.isUpdate;
          setDrawerProps({ showOkBtn: unref(isUpdate) != 3 });
          initData();
        },
      );

      const initData = async () => {
        console.log('smartModuleDrawer getVisible', unref(getVisible));
        await nextTick();
        const params = {
          ...dataRecord.value,
          isUpdate: unref(isUpdate),
          isTab: false,
        };
        unref(smartRef).initData(params);
      };

      const getTitle = computed(() =>
        unref(isUpdate) === 1 ? '新增' : unref(isUpdate) === 2 ? '编辑' : '查看',
      );
      const handleSubmit = () => {
        const submit = unref(smartRef).handleSubmit;
        setDrawerProps({ confirmLoading: true });
        submit()
          .then((res) => {
            console.log('抽屉的success事件', res);
            if (res && res.success) {
              emit('success');
              // 发送close，以使得在processHandleView中调用initWindowFunc
              emit('close');
            }
          })
          .finally(() => {
            setDrawerProps({ confirmLoading: false });
          });
      };
      const onClose = () => {
        // const submit = unref(smartRef).onClose;
        // submit().then(() => {
        // });
        emit('close');
        console.log('抽屉的close事件');
      };
      const handleEditAction = (record) => {
        emit('editAction', record);
      };
      const handleLaunchFlowAction = (record) => {
        emit('launchFlowAction', record);
      };
      const handleClose = () => {
        console.log('handleclose');
        closeDrawer();
        onClose();
      };
      const handleTabChange = (record) => {
        console.log('handleTabChange record', record);
        if (record.key != 0) {
          setDrawerProps({ showOkBtn: false });
        } else {
          setDrawerProps({ showOkBtn: true });
        }
      };
      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        onClose,
        smartRef,
        handleEditAction,
        handleLaunchFlowAction,
        getVisible,
        handleClose,
        handleTabChange,
      };
    },
  });
</script>

<style lang="less" scoped>
  :deep(.vben-basic-table .ant-table-wrapper) {
    padding: 6px 0;
  }
</style>
