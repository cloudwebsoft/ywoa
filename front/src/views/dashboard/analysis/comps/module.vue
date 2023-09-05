<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <!-- <template #title>
      <div class="cursor-pointer font-bold flex align-center" @click="handleGo">
        <Icon :icon="icon" :size="26" class="mr-2" />智能模块</div
      >
    </template> -->
      <SmartModule
        start="6"
        :moduleCode="formCode"
        :showSearch="showSearch"
        :pagination="pagination"
        :showToolbar="showToolbar"
        :showOpCol="showOpCol"
      />
    </ShareCard>
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted } from 'vue';
  import { Card, Empty, Tooltip } from 'ant-design-vue';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { useDrawer } from '/@/components/Drawer';
  import { getVisualList } from '/@/api/module/module';
  import SmartModule from '/@/views/pages/smartModule/smartModule.vue';
  import ShareCard from './ShareCard.vue';
  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;

  const props = defineProps({
    loading: Boolean,
    width: {
      type: String as PropType<string>,
      default: '100%',
    },
    height: {
      type: String as PropType<string>,
      default: '300px',
    },
    icon: {
      type: String as PropType<string>,
      default: '',
    },
    pageSize: {
      type: Number,
      default: 6,
    },
    formCode: {
      type: String,
      default: '',
    },
    showSearch: {
      type: Boolean,
      default: true,
    },
    pagination: {
      type: Boolean,
      default: true,
    },
    showToolbar: {
      type: Boolean,
      default: true,
    },
    showOpCol: {
      type: Boolean,
      default: true,
    },
    cardData: {
      type: Object as PropType<object>,
      default: () => {
        return {};
      },
    },
  });
  const isLoading = ref(false);
  const dataSource = ref<Recordable>([]);
  const fetch = () => {
    if (!props.formCode) return;
    isLoading.value = true;
    getVisualList({ pageSize: props.pageSize, moduleCode: props.formCode })
      .then((res) => {
        dataSource.value = res.list || [];
      })
      .finally(() => {
        isLoading.value = false;
      });
  };

  // watch(
  //   () => props.formCode,
  //   () => {
  //     fetch();
  //   },
  //   {
  //     immediate: true,
  //   },
  // );
  const go = useGo();
  const handleGo = () => {
    go('/notice');
  };
  const [registerInfoDrawer, { openDrawer: openInfoDrawer }] = useDrawer();

  const handleView = (record: any) => {
    openInfoDrawer(true, {
      isUpdate: true,
      record,
    });
  };
  // watch(
  //   () => props.loading,
  //   () => {
  //     if (props.loading) {
  //       return;
  //     }
  //   },
  //   { immediate: true },
  // );
</script>
<style lang="less" scoped>
  :deep(.m-2) {
    margin: 0 !important;
  }
</style>
