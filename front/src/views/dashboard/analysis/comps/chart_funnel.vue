<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div> <Icon :icon="icon" v-if="icon" :size="26" class="mr-2" />{{ header }}</div>
      </template>
      <div ref="chartRef" :style="{ height, width }"></div>
    </ShareCard>
  </div>
</template>
<script lang="ts">
  import { basicProps } from '../components/props';
</script>
<script lang="ts" setup>
  import { onMounted, ref, Ref, watch } from 'vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { Card } from 'ant-design-vue';
  import { getFunnel } from '/@/api/system/system';
  import Icon from '/@/components/Icon/index';
  import ShareCard from './ShareCard.vue';

  const props = defineProps({
    ...basicProps,
    icon: {
      type: String as PropType<string>,
      default: '',
    },
    header: {
      type: String as PropType<string>,
      default: '',
    },
    chartId: {
      type: Number,
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
  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions, echarts } = useECharts(chartRef as Ref<HTMLDivElement>);
  const title = ref('漏斗图');
  onMounted(() => {
    setData([]);
  });
  const fetch = () => {
    getFunnel({ id: props.chartId }).then((res) => {
      title.value = res?.title;
      const chartData = res?.chartData;
      setData(chartData);
    });
  };

  const setData = (chartData) => {
    setOptions({
      grid: {
        top: '4%',
        left: '6%',
        right: '4%',
        bottom: '10%',
        // y: 25,
        // height: 160,
      },
      /*title: {
          text: '漏斗图',
          subtext: '纯属虚构'
      },*/
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c}%',
      },
      /*toolbox: {
                    feature: {
                        dataView: {readOnly: false},
                        restore: {},
                        saveAsImage: {}
                    }
                },*/
      /*legend: {
                    data: ['展现','点击','访问','咨询','订单']
                },*/
      series: [
        {
          name: '漏斗图',
          type: 'funnel',
          left: '10%',
          top: 30,
          //x2: 80,
          bottom: 30,
          width: '80%',
          // height: {totalHeight} - y - y2,
          min: 0,
          max: 100,
          minSize: '0%',
          maxSize: '100%',
          sort: 'descending',
          gap: 2,
          label: {
            normal: {
              show: true,
              // color: '#353535',
              position: 'inside', // left
              formatter: '{b}-{c}',
              lineStyle: {
                width: 2,
                align: 'center',
              },
            },
          },
          labelLine: {
            length: 10,
            lineStyle: {
              width: 1,
              type: 'solid',
            },
          },
          itemStyle: {
            borderColor: '#fff',
            borderWidth: 1,
          },
          emphasis: {
            label: {
              fontSize: 16,
            },
          },
          data: chartData,
          // data: [
          //   { value: 60, name: '访问' },
          //   { value: 40, name: '咨询' },
          //   { value: 20, name: '订单' },
          //   { value: 80, name: '点击' },
          //   { value: 100, name: '展现' },
          // ],
        },
      ],
    });
  };
  watch(
    () => props.chartId,
    (newVal) => {
      if (newVal) fetch();
    },
    {
      immediate: true,
    },
  );
</script>
