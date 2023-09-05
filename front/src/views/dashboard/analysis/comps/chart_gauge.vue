<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div> <Icon :icon="icon" v-if="icon" :size="26" class="mr-2" />{{ title }}</div>
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
  import { getGauge } from '/@/api/system/system';
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
  const title = ref('仪表盘');
  const unit = ref('');
  const fetch = () => {
    getGauge({ id: props.chartId }).then((res) => {
      title.value = res?.title;
      const count = res?.count;
      unit.value = res?.unit;
      const max = res?.max;
      setOptions({
        grid: {
          top: '2%',
          left: '6%',
          right: '4%',
          bottom: '20%',
        },
        series: [
          {
            type: 'gauge',
            startAngle: 180,
            endAngle: 0,
            min: 0,
            max: max,
            splitNumber: 12,
            radius: '100%', // 仪表盘大小
            center: ['50%', '50%'], // 位置
            itemStyle: {
              color: '#58D9F9',
              shadowColor: 'rgba(0,138,255,0.45)',
              shadowBlur: 10,
              shadowOffsetX: 2,
              shadowOffsetY: 2,
            },
            progress: {
              show: true,
              roundCap: true,
              width: 18,
            },
            pointer: {
              icon: 'path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2097.05155,732.369577 2094.2393,735.416212 2090.62566,735.56078 C2090.53845,735.564269 2090.45117,735.566014 2090.36389,735.566014 L2090.36389,735.566014 C2086.74736,735.566014 2083.81557,732.63423 2083.81557,729.017692 C2083.81557,728.930412 2083.81732,728.84314 2083.82081,728.755929 L2088.2792,617.312956 C2088.32396,616.194028 2089.24407,615.30999 2090.36389,615.30999 Z',
              length: '75%',
              width: 16,
              offsetCenter: [0, '5%'],
            },
            axisLine: {
              roundCap: true,
              lineStyle: {
                width: 18,
              },
            },
            axisTick: {
              splitNumber: 2,
              lineStyle: {
                width: 2,
                color: '#999',
              },
            },
            splitLine: {
              length: 12,
              lineStyle: {
                width: 3,
                color: '#999',
              },
            },
            axisLabel: {
              distance: 30,
              color: '#999',
              fontSize: 12,
              formatter: function (v) {
                return v.toFixed(0);
              },
            },
            title: {
              show: false,
            },
            detail: {
              backgroundColor: '#fff',
              borderColor: '#eee',
              borderWidth: 1,
              width: '50%',
              lineHeight: 20,
              height: 20,
              borderRadius: 8,
              offsetCenter: [0, '35%'],
              valueAnimation: true,
              formatter: function (value) {
                return '{value|' + value.toFixed(0) + '}{unit|' + unit.value + title.value + '}';
              },
              rich: {
                value: {
                  fontSize: 12,
                  fontWeight: 'bolder',
                  color: '#777',
                },
                unit: {
                  fontSize: 12,
                  color: '#999',
                  padding: [0, 0, 0, 10],
                },
              },
            },
            data: [
              {
                value: count,
              },
            ],
          },
        ],
      });
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
