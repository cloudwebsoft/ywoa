<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    :showOkBtn="false"
    :cancelText="'关闭'"
  >
    <div class="border-1 border-solid border-gray-200 min-h-full">
      <div class="flex justify-center items-center h-10 text-base font-bold bg-gray-100"
        >{{ contentInfo.title }} {{ contentInfo.senderName }} {{ contentInfo.rq }}</div
      >
      <div class="p-2" v-html="contentInfo.content"></div>
    </div>
    <div class="mt-4">
      <a
        href="javascript:void(0)"
        v-if="contentInfo.actionName && contentInfo.actionType.length > 0"
        @click="handleAction"
        >>> {{ contentInfo.actionName }}</a
      >
    </div>
    <ProcessDrawer @register="registerProcessDrawer" />
    <ProcessShowDrawer @register="registerProcessShowDrawer" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, reactive } from 'vue';
  import ProcessDrawer from '../../processManagement/processDrawer.vue';
  import { BasicDrawer, useDrawerInner, useDrawer } from '/@/components/Drawer';
  import { getSysMessageShowPage } from '/@/api/workOffice/workOffice';
  import ProcessShowDrawer from '../../processManagement/processShowDrawer.vue';

  interface contentInfo {
    title?: string;
    senderName?: string;
    rq?: string;
    content?: string;
    action?: number;
    actionType?: string;
    actionName?: string;
    flowId?: number;
    flowType?: number;
    visitKey?: string;
  }

  export default defineComponent({
    name: 'MessageCenterInfoDrawer',
    components: { BasicDrawer, ProcessDrawer, ProcessShowDrawer },
    emits: ['success', 'register'],
    setup(_, {}) {
      const [registerProcessDrawer, { openDrawer: openProcessDrawer }] = useDrawer();
      const [registerProcessShowDrawer, { openDrawer: openProcessShowDrawer }] = useDrawer();

      const isUpdate = ref(true);
      let dataRef = reactive({});
      let contentInfo = ref<contentInfo>({
        title: '',
        senderName: '',
        rq: '',
        content: '',
        action: -1,
        actionType: '',
        actionName: '',
        flowId: -1,
        flowType: 2,
        visitKey: '',
      });
      const [registerDrawer, { setDrawerProps }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          dataRef = {
            ...data.record,
          };
          await getSysMessageShowPage({ id: dataRef['id'] }).then((res) => {
            contentInfo.value = res;
            console.log('contentInfo', contentInfo);
          });
        }
      });

      function handleAction() {
        console.log('handleAction actionType', contentInfo.value.actionType);
        if (contentInfo.value.action != -1) {
          if (contentInfo.value.actionType == 'flow_dispose') {
            openProcessDrawer(true, {
              myActionId: contentInfo.value.action,
              type: contentInfo.value.flowType,
            });
          } else if (contentInfo.value.actionType == 'flow_show') {
            if (contentInfo.value.flowId != -1) {
              let params = {
                flowId: contentInfo.value.action,
                visitKey: contentInfo.value.visitKey,
              };
              openProcessShowDrawer(true, params);
            }
          }
        }
      }

      const getTitle = '查看';
      return {
        registerDrawer,
        getTitle,
        contentInfo,
        handleAction,
        registerProcessDrawer,
        registerProcessShowDrawer,
      };
    },
  });
</script>
