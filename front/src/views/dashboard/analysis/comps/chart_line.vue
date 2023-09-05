<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div> <Icon :icon="icon" :size="26" v-if="icon" class="mr-2" />{{ header }}</div>
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
  import { getLine } from '/@/api/system/system';
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
  const title = ref('折线图');

  let isShadow = false;
  let shadowColor = '';
  let shadowBlur = 20;

  const fetch = () => {
    getLine({ id: props.chartId }).then((res) => {
      title.value = res?.title;
      const isLegend = res?.isLegend;
      const legend1 = res?.legend1;
      const legend2 = res?.legend2;
      const isData2 = res?.isData2;
      const chartData = res?.chartData;
      const chartData2 = res?.chartData2;
      const xItems = chartData?.map((item) => item.name);
      const yItems = chartData?.map((item) => item.value);
      const yItems2 = chartData2?.map((item) => item.value);
      const color = res?.color ? res.color : '#80ffc0';
      const color2 = res?.color2 ? res.color2 : '#80ffc0';

      let options = {
        grid: {
          top: '10%',
          left: '20%',
          right: '5%',
          bottom: '25%',
          // y: 25,
          // height: 160,
        },
        /*title:{
            text:'折线图'
        },*/
        tooltip: {
          trigger: 'axis',
        },
        xAxis: {
          data: xItems,
          splitNumber: 6, // 设置x轴刻度间隔个数
          boundaryGap: true,
        },
        yAxis: {
          name: '数值',
          type: 'value',
          min: 0, // 设置y轴刻度的最小值
          // max:1800,  // 设置y轴刻度的最大值
          splitNumber: 6, // 设置y轴刻度间隔个数
          /*axisLine: {
              lineStyle: {
                  // 设置y轴颜色
                  color: '#87CEFA'
              }
          }*/
        },
        legend: {
          show: isLegend,
          // align: 'center',
          // // right: '10%',
          // // top: '1%',
          // // type: 'plain',
          // textStyle: {
          //   // color: '#7ec7ff',
          //   fontSize: 12,
          // },
          // // icon:'rect',
          itemGap: 20,
          itemWidth: 12,
          icon: 'path://M0 2a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v0a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2z',
          data: [legend1, legend2],
        },
        series: isData2
          ? [
              {
                name: legend1,
                type: 'line',
                // symbol:'circle',  // 默认是空心圆（中间是白色的），改成实心圆
                // symbolSize: 0,
                itemStyle: {
                  color: color,
                  borderColor: '#ff0000',
                },
                lineStyle: {
                  normal: {
                    width: 1,
                    color: color, // 线条颜色
                  },
                  borderColor: 'rgba(0,0,0,.4)',
                },
                areaStyle: {
                  normal: {
                    // color: color,
                    // 线性渐变，前4个参数分别是x0,y0,x2,y2(范围0~1);相当于图形包围盒中的百分比。如果最后一个参数是‘true’，则该四个值是绝对像素位置。
                    color: new echarts.graphic.LinearGradient(
                      0,
                      0,
                      0,
                      1,
                      [
                        {
                          offset: 0,
                          color: setAlpha(color, 0.6),
                        },
                        {
                          offset: 1,
                          color: setAlpha(color, 0),
                        },
                      ],
                      false,
                    ),
                  },
                },
                smooth: 0.5,
                data: yItems,
              },
              {
                name: legend2,
                type: 'line',
                // symbol:'circle',  // 默认是空心圆（中间是白色的），改成实心圆
                // symbolSize: 0,
                itemStyle: {
                  color: color2,
                  borderColor: '#ff0000',
                },
                lineStyle: {
                  normal: {
                    width: 1,
                    color: color2, // 线条颜色
                  },
                  borderColor: 'rgba(0,0,0,.4)',
                },
                areaStyle: {
                  normal: {
                    // color: color,
                    color: new echarts.graphic.LinearGradient(
                      0,
                      0,
                      0,
                      1,
                      [
                        {
                          offset: 0,
                          color: setAlpha(color2, 0.6),
                        },
                        {
                          offset: 1,
                          color: setAlpha(color2, 0),
                        },
                      ],
                      false,
                    ),
                  },
                },
                smooth: 0.5,
                data: yItems2,
              },
            ]
          : [
              {
                name: legend1,
                type: 'line',
                // symbol:'circle',  // 默认是空心圆（中间是白色的），改成实心圆
                // symbolSize: 0,
                itemStyle: {
                  color: color,
                  borderColor: '#ff0000',
                },
                lineStyle: {
                  normal: {
                    width: 1,
                    color: color, // 线条颜色
                  },
                  borderColor: 'rgba(0,0,0,.4)',
                },
                areaStyle: {
                  normal: { color: color },
                },
                smooth: 0.5,
                data: yItems,
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
        for (let i in options.series) {
          Object.assign(options.series[i].areaStyle.normal, shadowStyle);
        }
      }

      setOptions(options);
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
<style>
  .ant-card-body {
    padding: 12px;
  }
</style>
