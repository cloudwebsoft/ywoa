<template>
  <div>
    <ShareCard :loading="isLoading" :headStyle="cardData">
      <template #title>
        <div> <Icon :icon="icon" :size="26" v-if="icon"  class="mr-2" />{{ header }}</div>
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
  import { getRadar } from '/@/api/system/system';
  import Icon from '/@/components/Icon/index';
  import { setAlpha } from '/@/utils/colorUtils';
  import ShareCard from './ShareCard.vue';

  const props = defineProps({
    ...basicProps,
    header: {
      type: String as PropType<string>,
      default: '',
    },
    icon: {
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
  const title = ref('雷达图');
  let isLegend = false;
  let color = '';
  let color2 = '';
  let splitAreaColor = '';
  let isShadow = false;
  let shadowColor = '';
  let shadowBlur = 20;

  onMounted(() => {
    // setData([]);
  });
  const fetch = () => {
    getRadar({ id: props.chartId }).then((res) => {
      title.value = res?.title ? res?.title : '雷达图';
      isLegend = res?.isLegend;
      color = res?.color ? res?.color : '#4df0bb';
      color2 = res?.color2 ? res?.color2 : '#ded774';
      splitAreaColor = res?.splitAreaColor ? res?.splitAreaColor : '#4af8f8';
      isShadow = res?.isShadow;
      shadowColor = res?.shadowColor;
      shadowBlur = res?.shadowBlur;

      const legend1 = res?.legend1 || 'Allocated Budget';
      const legend2 = res?.legend2 || 'Actual Spending';
      const chartData = res?.chartData;
      const arrIndicator =
        chartData.arrIndicator.length > 0
          ? chartData.arrIndicator
          : [
              { name: '销售', max: 6500 },
              { name: '管理', max: 16000 },
              { name: '信息', max: 30000 },
              { name: '客服', max: 38000 },
              { name: '开发', max: 52000 },
              { name: '市场', max: 25000 },
            ];
      const data1 =
        chartData.data1.length > 0 ? chartData.data1 : [4200, 3000, 20000, 35000, 50000, 18000];
      const data2 =
        chartData.data2.length > 0 ? chartData.data2 : [5000, 14000, 28000, 26000, 42000, 21000];
      const isData2 = data2.length > 0;

      console.log('data1', data1);
      console.log('data2', data2);
      console.log('arrIndicator', arrIndicator);
      console.log('legend1', legend1);
      console.log('legend2', legend2);
      setData(legend1, legend2, isData2, data1, data2, arrIndicator);
    });
  };

  const setData = (legend1, legend2, isData2, data1, data2, arrIndicator) => {
    let data = [
      {
        value: data1,
        name: legend1,
        areaStyle: {
          normal: {
            color: setAlpha(color, 0.4),
          },
        },
        itemStyle: {
          color: setAlpha(color, 0.8),
          borderColor: setAlpha(color, 0.2),
          borderWidth: 10,
        },
        lineStyle: {
          color: setAlpha(color, 0.6),
          width: 2,
        },
        label: {
          normal: {
            show: true,
            position: 'top',
            distance: 5,
            // color: 'rgba(155,237,45,1)',
            fontSize: 12,
            formatter: function (params) {
              return params.value;
            },
          },
        },
      },
    ];
    if (isData2) {
      data.push({
        value: data2,
        name: legend2,
        areaStyle: {
          normal: {
            color: setAlpha(color2, 0.4),
          },
        },
        itemStyle: {
          color: setAlpha(color2, 0.8),
          borderColor: setAlpha(color2, 0.2),
          borderWidth: 10,
        },
        lineStyle: {
          color: setAlpha(color2, 0.6),
          width: 2,
        },
        // 单个数据标记的大小，可以设置成诸如 10 这样单一的数字，也可以用数组分开表示宽和高，例如 [20, 10] 表示标记宽为20，高为10。
        label: {
          // 单个拐点文本的样式设置
          normal: {
            show: false,
            // 单个拐点文本的样式设置。[ default: false ]
            position: 'top',
            // 标签的位置。[ default: top ]
            distance: 5,
            // 距离图形元素的距离。当 position 为字符描述值（如 'top'、'insideRight'）时候有效。[ default: 5 ]
            // color: 'rgba(255,237,145,1)',
            // 文字的颜色。如果设置为 'auto'，则为视觉映射得到的颜色，如系列色。[ default: "#fff" ]
            fontSize: 10,
            // 文字的字体大小
            formatter: function (params) {
              return params.value;
            },
          },
        },
      });
    }

    let options = {
      grid: {
        top: '4%',
        left: '6%',
        right: '4%',
        bottom: '10%',
        // y: 25,
        // height: 160,
      },
      title: {
        text: '', // 'Basic Radar Chart',
      },
      tooltip: {
        show: true,
        trigger: 'item',
      },
      legend: {
        show: isLegend,
        data: [legend1, legend2],
      },
      radar: {
        shape: 'circle', // 默认为多边形
        // splitNumber: 6, //设置分隔段
        // indicator为圆外的标签
        indicator: arrIndicator,
        // radius: '70%',
        splitArea: {
          // 雷达图每一圈所分割出的区域的样式
          areaStyle: {
            color: [
              setAlpha(splitAreaColor, 0.1),
              setAlpha(splitAreaColor, 0.2),
              setAlpha(splitAreaColor, 0.3),
              setAlpha(splitAreaColor, 0.4),
              setAlpha(splitAreaColor, 0.5),
              setAlpha(splitAreaColor, 0.6),
            ].reverse(),
          },
        },
      },
      series: [
        {
          name: 'Budget vs spending',
          type: 'radar',
          // 设置拐点的大小和形状
          symbol: 'circle', // 'circle', 'rect', 'roundRect', 'triangle', 'diamond', 'pin', 'arrow', 'none', emptyCircle
          symbolSize: 5,
          // symbolRotate: 45,
          itemStyle: {
            emphasis: {
              // 高亮时的样式
              lineStyle: {
                width: 3,
              },
              opacity: 1,
            },
          },
          data: data,
        },
      ],
    };

    if (isShadow) {
      let shadowStyle = {
        shadowColor: shadowColor, // 'rgba(0, 0, 0, 1)'
        shadowBlur: shadowBlur, // 30
        shadowOffsetX: 10,
        shadowOffsetY: 10,
      };
      Object.assign(options.radar.splitArea.areaStyle, shadowStyle);
    }

    setOptions(options);
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
