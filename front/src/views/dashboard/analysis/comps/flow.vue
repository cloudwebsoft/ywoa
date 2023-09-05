<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div @click="handleGo">
          <Icon :icon="icon" v-if="icon" :size="26" class="mr-2" />{{ header }}</div
        >
      </template>
      <template v-if="dataSource?.length > 0">
        <div v-for="item in dataSource" :key="item.id" class="flex justify-between mb-2">
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-2/3 cursor-pointer"
            :title="item['f.title']"
            :style="[{ fontWeight: !item['isReaded'] ? 'bold' : 'normal' }]"
            @click="handleClickTitle(item)"
          >
            <Tooltip :title="item['f.title']">
              {{ item['f.title'] }}
            </Tooltip>
          </div>
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-1/3 text-right"
            v-if="item['f.begin_date']"
          >
            <Tooltip :title="item['f.begin_date']">
              {{ item['f.begin_date'].substring(0, 10) }}
            </Tooltip>
          </div>
        </div>
      </template>
      <template v-else>
        <Empty :image="simpleImage" />
      </template>
      <ProcessDrawer @register="registerDrawer" @success="handleSuccess" />
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
  import ProcessDrawer from '/@/views/pages/processManagement/processDrawer.vue';
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
    typeCode: {
      type: String as PropType<string>,
      default: '',
    },
    cardData: {
      type: Object as PropType<object>,
      default: () => {
        return {};
      },
    },
  });
  let flowOpStyle = 0;
  const isLoading = ref(false);
  const dataSource = ref<Recordable>([]);
  const fetch = () => {
    isLoading.value = true;
    getFlowList({ pageSize: props.pageSize, type: 'doing', typeCode: props.typeCode })
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
    go({ path: '/flow/doing', query: { typeCode: props.typeCode } });
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
  const [registerDrawer, { openDrawer }] = useDrawer();

  const handleClickTitle = (record: any) => {
    handleEdit(record);
  };

  function handleEdit(record: any) {
    if (flowOpStyle == 1) {
      let title = record['f.id'] + '-' + record['f.title'];
      if (title.length > 18) {
        title = title.substring(0, 18) + '...';
      }
      if (record.type == 2) {
        go({
          path: '/processHandle',
          query: {
            myActionId: record.id,
            isFromProcess: true,
            title: title,
            cacheName: `processHandle${record['f.id']}`,
          },
        });
      } else {
        go({
          path: '/processHandleFree',
          query: {
            myActionId: record.id,
            isFromProcess: true,
            title: title,
            cacheName: `processHandle${record['f.id']}`,
          },
        });
      }
    } else {
      openDrawer(true, {
        myActionId: record.id,
        type: record.type,
      });
    }
  }
  //页面回调
  function handleSuccess() {
    fetch();
  }
</script>
