<template>
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
          @click="handleEdit(item)"
        >
          <Tooltip :title="item.title">
            {{ item.title }}
          </Tooltip>
        </div>
        <div
          class="overflow-hidden whitespace-nowrap overflow-ellipsis w-1/3 text-right"
          v-if="item.myDate"
        >
          <Tooltip :title="item.myDate">
            {{ item.myDate.substring(0, 10) }}
          </Tooltip>
        </div>
      </div>
    </template>
    <template v-else>
      <Empty :image="simpleImage" />
    </template>
    <AllScheduleDrawer @register="registerDrawer" @success="handleSuccess" />
  </ShareCard>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted } from 'vue';
  import { Card, Empty, Tooltip } from 'ant-design-vue';
  import { getPlanList } from '/@/api/workOffice/workOffice';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { useDrawer } from '/@/components/Drawer';
  import AllScheduleDrawer from '/@/views/pages/workOffice/schedule/modules/allSchedule/AllScheduleDrawer.vue';
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
  const fetch = () => {
    isLoading.value = true;
    getPlanList({ pageSize: props.pageSize })
      .then((res) => {
        dataSource.value = res.list || [];
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
    go('/schedule');
  };
  const [registerDrawer, { openDrawer }] = useDrawer();
  const handleEdit = (record: Recordable) => {
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  };

  const handleSuccess = () => {
    fetch();
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
