<template>
  <div>
    <!--门户型-->
    <div v-if="props.kind == 0">
      <!--默认-->
      <div v-if="style == 0">
        <Row :gutter="[10, 10]">
          <Col
            :span="dataSource.length == 6 ? 4 : 6"
            v-for="item in dataSource"
            :key="item.type"
            @click="handleGo(item)"
          >
            <div
              :style="[
                {
                  backgroundColor: item.bgColor,
                  height: dataSource.length == 6 ? '85px' : '105px',
                  width: '100%',
                },
              ]"
              class="flex flex-col justify-between text-white cursor-pointer item-box"
            >
              <div class="flex items-center h-3/4">
                <div>
                  <i
                    :class="
                      item.icon
                        ? dataSource.length == 6
                          ? 'portal-fa-6 fa ' + item.icon
                          : 'portal-fa fa ' + item.icon
                        : ''
                    "
                  ></i>
                </div>
                <div>
                  <!-- startVal -->
                  <div>
                    <span class="text-2xl">
                      <CountTo :startVal="item.startVal" :endVal="item.endVal" />
                    </span>
                    {{ item.unit }}
                  </div>
                  <div class="text-sm mt-1">{{ item.title }}</div>
                </div>
              </div>
              <div
                class="w-full h-1/4 bg-black bottom-0 flex justify-between pl-5 pr-5 pt-1.5 bg-opacity-10 text-xs"
                :style="[{ paddingTop: dataSource.length == 6 ? '3px' : '5px' }]"
              >
                <div>查看详情</div>
                <div> <arrow-right-outlined /></div>
              </div>
              <!-- :style="[{ backgroundColor: 'rgba(0, 0, 0, 0.1)' }]" -->
            </div>
          </Col>
        </Row>
      </div>
      <div v-else-if="style == 1">
        <!--优雅-->
        <Row :gutter="[10, 10]">
          <Col :span="6" v-for="item in dataSource" :key="item.type" @click="handleGo(item)">
            <div
              :style="[{ height: '105px', width: '100%' }]"
              class="flex flex-col justify-between text-black cursor-pointer item-box"
            >
              <div class="flex items-center h-full">
                <div
                  class="w-2 h-full rounded-tl-md rounded-bl-md"
                  :style="[{ backgroundColor: item.bgColor }]"
                ></div>
                <div
                  class="flex w-full h-full bg-opacity-10 rounded-tr-md rounded-br-md"
                  :style="[{ backgroundColor: colorToRGB(item.bgColor, 0.1) }]"
                >
                  <div class="w-2/3 h-full flex flex-col justify-center pl-8">
                    <div>
                      <span class="text-2xl">
                        <CountTo :startVal="item.startVal" :endVal="item.endVal" />
                      </span>
                      {{ item.unit }}
                    </div>
                    <div class="text-sm mt-2">{{ item.title }}</div>
                  </div>
                  <div class="w-1/3 h-full">
                    <i
                      :class="item.icon ? 'pl-6 pt-6 fa ' + item.icon : ''"
                      :style="[{ fontSize: '42px', color: colorToRGB(item.bgColor, 0.4) }]"
                    ></i>
                  </div>
                </div>
              </div>
            </div>
          </Col>
        </Row>
      </div>
      <div v-else>
        <!--简洁-->
        <Row :gutter="[10, 10]" class="flex justify-center">
          <Col :span="6" v-for="item in dataSource" :key="item.type" @click="handleGo(item)">
            <div
              :style="[{ height: '105px', width: '100%' }]"
              class="flex flex-col justify-between text-black cursor-pointer item-box"
            >
              <div class="flex items-center h-full">
                <div
                  class="flex w-full h-full rounded-tl-md rounded-bl-md bg-opacity-10 rounded-tr-md rounded-br-md"
                  :style="[{ backgroundColor: item.bgColor, color: item.color }]"
                >
                  <div class="w-2/3 h-full flex flex-col justify-center pl-8">
                    <div>
                      <span class="text-2xl">
                        <CountTo :startVal="item.startVal" :endVal="item.endVal" />
                      </span>
                      {{ item.unit }}
                    </div>
                    <div class="text-sm mt-2">{{ item.title }}</div>
                  </div>
                  <div class="w-1/3 h-full">
                    <i
                      :class="item.icon ? 'pl-6 pt-6 fa ' + item.icon : ''"
                      :style="[{ fontSize: '42px' }]"
                    ></i>
                  </div>
                </div>
              </div>
            </div>
          </Col>
        </Row>
      </div>
    </div>
    <div v-else>
      <!--模块型-->
      <Row class="pl-1" :gutter="[10, 10]">
        <Col :span="4" v-for="item in dataSource" :key="item.type" @click="handleGo(item)">
          <div
            :style="[{ backgroundColor: item.bgColor, height: '75px', width: '100%' }]"
            class="flex flex-col justify-between text-white cursor-pointer"
          >
            <div class="flex items-center h-full">
              <div>
                <i :class="item.icon ? 'module-fa fa ' + item.icon : ''"></i>
              </div>
              <div>
                <div>
                  <span class="text-xl">
                    <CountTo :startVal="item.startVal" :endVal="item.endVal" />
                  </span>
                  {{ item.unit }}
                </div>
                <div class="text-sm">{{ item.title }}</div>
              </div>
            </div>
          </div>
        </Col>
      </Row>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted, defineExpose } from 'vue';
  import { Card, Empty, Tooltip, Row, Col } from 'ant-design-vue';
  import { ArrowRightOutlined } from '@ant-design/icons-vue';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { getListCard, getListCardByModule } from '/@/api/system/system';
  import { colorToRGB } from '/@/utils/utils';

  import { CountTo } from '/@/components/CountTo';
  import ShareCard from './ShareCard.vue';

  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
  const kind = ref(0); // 0 门户 1 模块
  const style = ref(0); // 0 默认 1 优雅 2 简洁

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
    kind: {
      type: Number as PropType<number>,
      default: 0,
    },
    moduleCode: {
      type: String as PropType<string>,
      default: '',
    },
    portalId: {
      type: [Number, String],
      default: 0,
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
  const colSpan = ref(6);
  const fetch = () => {
    isLoading.value = true;
    if (props.kind == 0) {
      getListCard({ id: props.portalId })
        .then((res) => {
          dataSource.value = res || [];
          if (dataSource.value.length > 0) {
            style.value = dataSource.value[0].style;
          }

          for (let i in dataSource.value) {
            if (!dataSource.value[i].color) {
              dataSource.value[i].color = 'white';
            }
          }
        })
        .finally(() => {
          isLoading.value = false;
        });
    } else {
      if (props.moduleCode && props.moduleCode.length > 0) {
        getListCardByModule({ moduleCode: props.moduleCode })
          .then((res) => {
            dataSource.value = res || [];
            if (dataSource.value.length > 0) {
              style.value = dataSource.value[0].style;
            }
          })
          .finally(() => {
            isLoading.value = false;
          });
      } else {
        console.log('moduleCode is empty.');
      }
    }

    emit('loaded', dataSource.value.length);
  };

  defineExpose({
    fetch,
  });
  onMounted(() => {
    kind.value = props.kind;

    // fetch();
    if (props.kind == 1) {
      colSpan.value = 4;
    }
  });
  const go = useGo();
  const handleGo = (item) => {
    if (item.url != null && item.url != '') {
      go({ path: item.url, query: item.query });
    }
    console.log('dataSource.value', dataSource.value);
    console.log('item.url', item.url, 'item.query', item.query);
    // go(dataSource.value.url);
  };
  watch(
    () => props.moduleCode,
    () => {
      fetch();
    },
    // { immediate: true },
  );
  // 会产生两次调用，故通过在父组件中ref.fetch()来调用
  // watch(
  //   () => props.portalId,
  //   () => {
  //     fetch();
  //   },
  //   { immediate: true },
  // );
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
<style scoped>
  .portal-fa {
    padding: 0 20px;
    font-size: 50px;
    opacity: 0.7;
  }
  .portal-fa-6 {
    padding: 0 20px;
    font-size: 40px;
    opacity: 0.7;
  }
  .module-fa {
    padding: 0 20px;
    font-size: 30px;
    opacity: 0.7;
  }
  .item-box:hover {
    box-shadow: 1px 1px 4px rgba(0, 0, 0, 0.374);
  }
</style>
