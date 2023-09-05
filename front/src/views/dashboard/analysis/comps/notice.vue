<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div @click="handleGo"> <Icon v-if="icon"  :icon="icon" :size="26" class="mr-2" />{{ header }}</div>
      </template>
      <template v-if="dataSource?.length > 0">
        <div v-for="item in dataSource" :key="item.id" class="flex justify-between mb-2">
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-2/3 cursor-pointer"
            @click="handleView(item)"
          >
            <Tooltip :title="item.title"
              ><span
                :class="item.isBold == 1 ? 'font-bold' : ''"
                :style="[{ color: item.color ? item.color : '' }]"
                >{{ item.title }}</span
              ></Tooltip
            >
          </div>
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-1/3 text-right"
            v-if="item.createDate"
          >
            <Tooltip :title="item.createDate">
              {{ item.createDate.substring(0, 10) }}
            </Tooltip>
          </div>
        </div>
      </template>
      <template v-else>
        <Empty :image="simpleImage" />
      </template>
      <NoticeInfoDrawer @register="registerInfoDrawer" />
    </ShareCard>
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted } from 'vue';
  import { Card, Empty, Tooltip } from 'ant-design-vue';
  import { getNoticeList } from '/@/api/administration/administration';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { useDrawer } from '/@/components/Drawer';
  import NoticeInfoDrawer from '/@/views/pages/administrativeManagement/notice/noticeInfoDrawer.vue';
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
    getNoticeList({ pageSize: props.pageSize })
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
