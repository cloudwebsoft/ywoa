<template>
  <div>
    <div class="flex justify-center items-center">
      <div>发文</div>
      <div class="ml-2 mr-2">
        <Select class="w-50" />
      </div>
      <div class="mr-2">
        <a-button>套用</a-button>
      </div>
      <div>
        <a-button @click="handleDesign">设计</a-button>
      </div>
    </div>
    <div>
      <EditFlowChart :typeCode="typeCode" :editable="false" />
    </div>
    <DesignView @register="registerModalDesignView" @success="handleModalDesignView" />
  </div>
</template>
<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Select } from 'ant-design-vue';
  import { useModal } from '/@/components/Modal';
  import DesignView from './modules/DesignView.vue';
  import EditFlowChart from './modules/EditFlowChart.vue';
  const title = ref('流程图');
  const props = defineProps({
    activeKey: {
      type: String,
      default: '',
    },
    currentRecord: {
      type: Object,
      default: () => {
        return {};
      },
    },
  });
  const typeCode = computed(() => props.currentRecord.code);
  const [registerModalDesignView, { openModal }] = useModal();
  //设计
  const handleDesign = () => {
    openModal(true, { typeCode });
  };

  //设计回调
  const handleModalDesignView = () => {
    console.log('设计回调');
  };
</script>
