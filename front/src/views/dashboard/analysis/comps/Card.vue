<template>
  <div>
    <!--直接放在门户中的卡片-->
    <template v-if="!isEmpty">
      <div class="flex justify-between flex-wrap">
        <div
          :style="[{ height: '85px', width: '90px' }]"
          class="flex justify-center cursor-pointer mt-2 mb-2"
          @click="handleGo(cardInfo)"
        >
          <div class="flex flex-col justify-center items-center">
            <div class="text-center">
              <i
                :class="cardInfo.icon ? 'portal-fa fa ' + cardInfo.icon : ''"
                :style="[{ color: cardInfo.bgColor }]"
              ></i>
            </div>
            <div class="text-center mt-2">
              {{ cardInfo.title }}
            </div>
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <Empty :image="simpleImage" />
    </template>
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted, computed } from 'vue';
  import { Card, Empty, Tooltip, Row, Col } from 'ant-design-vue';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { getCard } from '/@/api/system/system';

  import { CountTo } from '/@/components/CountTo';

  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
  const cardInfo = ref({});

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
    header: {
      type: String as PropType<string>,
      default: '',
    },
    pageSize: {
      type: Number,
      default: 6,
    },
    cardId: {
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
    getCard({ id: props.cardId, count: props.pageSize })
      .then((res) => {
        cardInfo.value = res || {};
      })
      .finally(() => {
        isLoading.value = false;
      });
    console.log('cardInfo', cardInfo);
    // emit('loaded', cardInfo.value);
  };
  onMounted(() => {});
  const go = useGo();
  const handleGo = (item) => {
    if (item.url != null && item.url != '') {
      go({ path: item.url, query: item.query });
    }
  };
  const isEmpty = computed(() => JSON.stringify(cardInfo.value) == '{}');

  watch(
    () => props.cardId,
    () => {
      console.log('props.cardId', props.cardId);
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
