<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    wrap-class-name="full-modal"
    :canFullscreen="false"
  >
    <div class="flex" style="height: calc(100vh - 114px)">
      <div class="w-1/4">
        <Tabs v-model:activeKey="activeKey" type="card">
          <TabPane tab="表单" key="1">
            <IframeFormData />
          </TabPane>
          <TabPane tab="脚本库" key="2">脚本库</TabPane>
          <TabPane tab="数据源" key="3"> 数据源 </TabPane>
        </Tabs>
      </div>
      <div class="w-3/4 bg-yellow-400">
        <iframe
          src="https://wap.baidu.com"
          frameborder="0"
          style="height: 100%; width: 100%"
        ></iframe>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Tabs, TabPane } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import IframeFormData from './modules/iframeFormData/IframeFormData.vue';
  const { t } = useI18n();
  const loading = ref(false);

  const activeKey = ref('1');
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false, width: '100%', height: '100%' });
    isUpdate.value = !!data?.isUpdate;
  });
</script>
