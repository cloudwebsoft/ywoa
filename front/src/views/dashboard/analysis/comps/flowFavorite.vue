<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div @click="handleGo">
          <Icon :icon="icon" v-if="icon" :size="26" class="mr-2" />header</div
        >
      </template>
      <template v-if="dataSource?.length.length > 0">
        <div v-for="item in dataSource" :key="item.id" class="flex justify-between mb-2">
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-2/3 cursor-pointer"
            @click="handleView(item)"
          >
            <Tooltip :title="item['f.title']">
              {{ item['f.title'] }}
            </Tooltip>
          </div>
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-1/3 text-right"
            v-if="item['f.end_date']"
          >
            <Tooltip :title="item['f.end_date']">
              {{ item['f.end_date'].substring(0, 10) }}
            </Tooltip>
          </div>
        </div>
      </template>
      <template v-else>
        <Empty :image="simpleImage" />
      </template>
      <ProcessShowDrawer @register="registerViewDrawer" />
    </ShareCard>
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted } from 'vue';
  import { Card, Empty, Tooltip } from 'ant-design-vue';
  import { getFlowList } from '/@/api/process/process';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { useDrawer } from '/@/components/Drawer';
  import ProcessShowDrawer from '/@/views/pages/processManagement/processShowDrawer.vue';
  import ShareCard from './ShareCard.vue';
  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
  const props = defineProps({
    loading: Boolean,
    header: {
      type: String as PropType<string>,
      default: '',
    },
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
    cardData: {
      type: Object as PropType<object>,
      default: () => {
        return {};
      },
    },
  });
  const isLoading = ref(false);
  const dataSource = ref<Recordable>([]);
  let flowOpStyle = 0;
  const fetch = () => {
    isLoading.value = true;
    getFlowList({ pageSize: props.pageSize, type: 'favorite' })
      .then((res) => {
        dataSource.value = res.list || [];
        flowOpStyle = res.flowOpStyle;
      })
      .finally(() => {
        isLoading.value = false;
      });
  };
  watch(
    () => props.pageSize,
    () => {
      fetch();
    },
    {
      immediate: true,
    },
  );
  const go = useGo();
  const handleGo = () => {
    go('/flow/favorite');
  };
  const [registerViewDrawer, { openDrawer: openViewDrawer }] = useDrawer();
  //查看详情
  const handleView = (record: object) => {
    console.log('record==>', record);
    if (flowOpStyle == 1) {
      let title = record['f.id'] + '-' + record['f.title'];
      if (title.length > 18) {
        title = title.substring(0, 18) + '...';
      }
      go({
        path: '/processShow',
        query: {
          flowId: record['f.id'],
          title: title,
          isFromProcess: true,
          cacheName: `processShow${record['f.id']}`,
        },
      });
    } else {
      openViewDrawer(true, {
        flowId: record['f.id'],
      });
    }
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
