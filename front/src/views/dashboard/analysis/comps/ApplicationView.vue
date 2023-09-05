<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div> <Icon :icon="icon" v-if="icon" :size="26" class="mr-2" />{{ header }}</div>
      </template>
      <template v-if="dataSource?.length > 0">
        <div class="flex justify-between flex-wrap">
          <div
            v-for="item in dataSource"
            :key="item.id"
            class="flex justify-between mb-2 item-box"
            @click="handleGo(item)"
          >
            <div
              :style="[{ height: '85px', width: '90px' }]"
              class="flex justify-center cursor-pointer mt-2 mb-2"
            >
              <div class="flex flex-col justify-center items-center">
                <div class="text-center">
                  <i
                    :class="item.icon ? 'portal-fa fa ' + item.icon : ''"
                    :style="[{ color: item.bgColor }]"
                  ></i>
                </div>
                <div class="text-center mt-2">
                  {{ item.title }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <Empty :image="simpleImage" />
      </template>
    </ShareCard>
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted, defineExpose } from 'vue';
  import { Card, Empty, Tooltip, Row, Col } from 'ant-design-vue';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { getListCardByApplication } from '/@/api/system/system';

  import { CountTo } from '/@/components/CountTo';
  import ShareCard from './ShareCard.vue';

  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;

  // 抛出事件
  const emit = defineEmits<{
    (e: 'loaded', len: number): void;
  }>();

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
    formCode: {
      type: String as PropType<string>,
      default: '',
    },
    header: {
      type: String as PropType<string>,
      default: '',
    },
    pageSize: {
      type: Number,
      default: 6,
    },
    portalId: {
      type: Number,
      default: -1,
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
    getListCardByApplication({ id: props.portalId, count: props.pageSize })
      .then((res) => {
        dataSource.value = res || [];
      })
      .finally(() => {
        isLoading.value = false;
      });
    console.log('dataSource.value.lenth', dataSource.value.length);
    emit('loaded', dataSource.value.length);
  };
  onMounted(() => {});
  const go = useGo();
  const handleGo = (item) => {
    if (item.url != null && item.url != '') {
      go({ path: item.url, query: item.query });
    }
    console.log('dataSource.value', dataSource.value);
  };
  watch(
    () => props.portalId,
    () => {
      console.log('props.portalId', props.portalId);
      fetch();
    },
    { immediate: true },
  );
</script>
<style scoped>
  .portal-fa {
    padding: 0 20px;
    font-size: 40px;
    opacity: 0.8;
  }
  .module-fa {
    padding: 0 20px;
    font-size: 30px;
    opacity: 0.7;
  }

  .item-box:hover {
    /* box-shadow: 1px 1px 4px rgb(0, 0, 0, 0.5); */
    background: #f2f2f2;
  }
</style>
