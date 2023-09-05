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
  import type { EChartsOption } from 'echarts';
</script>
<script lang="ts" setup>
  import { onMounted, ref, Ref, watch } from 'vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { Card } from 'ant-design-vue';
  import { getBar } from '/@/api/system/system';
  import Icon from '/@/components/Icon/index';
  import { getRGBAArray } from '/@/utils/colorUtils';
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
  const title = ref('柱状图');
  const fetch = () => {
    getBar({ id: props.chartId }).then((res) => {
      title.value = res?.title;
      const isAaverage = res?.isAaverage;
      const chartData = res?.chartData;
      const chartData2 = res?.chartData2;
      const seriesCount = res?.seriesCount;
      const seriesName = res?.seriesName;
      const seriesName2 = res?.seriesName2;
      const unit = res?.unit;
      const is3D = res?.is3D;
      const isLegend = res?.isLegend;

      // 顶部是否显示标签
      const isTopLabel: boolean = res?.isTopLabel;
      // 系统1的颜色
      const color = res?.color ? res?.color : 'rgba(78,173,237,1)';
      let colorArr = getRGBAArray(color);

      const r = colorArr[0];
      const g = colorArr[1];
      const b = colorArr[2];
      const rr = r - 30 >= 0 ? r - 30 : r;
      const gg = g - 30 >= 0 ? g - 30 : g;
      const bb = b - 30 >= 0 ? b - 30 : b;

      // 立体图顶部的颜色
      const rTop = r + 30 >= 255 ? 255 : r + 30;
      const gTop = g + 30 >= 255 ? 255 : g + 30;
      const bTop = b + 30 >= 255 ? 255 : b + 30;

      console.log('r', r, 'rTop', rTop, 'r+30', r + 30);

      // 系统2的颜色
      const color2 = res?.color2 ? res?.color2 : 'rgba(247,222,32,1)';
      let colorArr2 = getRGBAArray(color2);
      console.log('color2', color2);
      console.log('colorArr2', colorArr2);
      const r2 = colorArr2[0];
      const g2 = colorArr2[1];
      const b2 = colorArr2[2];
      const rr2 = r2 - 30 >= 0 ? r2 - 30 : r2;
      const gg2 = g2 - 30 >= 0 ? g2 - 30 : g2;
      const bb2 = b2 - 30 >= 0 ? b2 - 30 : b2;
      // 立体图顶部的颜色
      const rTop2 = r2 + 30 >= 255 ? 255 : r2 + 30;
      const gTop2 = g2 + 30 >= 255 ? 255 : g2 + 30;
      const bTop2 = b2 + 30 >= 255 ? 255 : b2 + 30;

      const xItems = chartData?.map((item) => item.name);
      const yItems = chartData?.map((item) => item.value);
      const yItems2 = chartData2?.map((item) => item.value);

      let w = 20; // 柱子的宽度
      if (is3D) {
        w = w / 2;
      }

      let symbolOffset = [0, -w / 2];
      let symbolOffset2 = [];
      if (seriesCount == 2) {
        symbolOffset = [-w, -w / 2];
        symbolOffset2 = [w, -w / 2];
      }

      let options: EChartsOption = {
        grid: {
          top: '4%', // 上距离
          left: '6%', // 距离左边
          right: '4%', // 距离右边
          bottom: '10%', // 下距离
          // y: 25,
          // height: 160,
          // containLabel: true,
        },
        axisLabel: {
          interval: 0, // 防止横坐标值显示不全(自动隐藏)
          formatter: function (params) {
            // 以免坐标轴名称过长出现重叠
            var newParamsName = '';
            const paramsNameNumber = params.length;
            const provideNumber = 6; // 单行显示文字个数
            const rowNumber = Math.ceil(paramsNameNumber / provideNumber);
            if (paramsNameNumber > provideNumber) {
              for (let p = 0; p < rowNumber; p++) {
                var tempStr = '';
                var start = p * provideNumber;
                var end = start + provideNumber;
                if (p === rowNumber - 1) {
                  tempStr = params.substring(start, paramsNameNumber);
                } else {
                  tempStr = params.substring(start, end) + '\n';
                }
                newParamsName += tempStr;
              }
            } else {
              newParamsName = params;
            }
            return newParamsName;
          },
        },
        // 标题
        /*title: {
                      text: '示例',
                      subtext:'出品',
                      left:'left',
                      borderColor:'red',
                      borderWidth:3
                  },*/
        // 工具箱
        toolbox: {
          show: false,
          feature: {
            saveAsImage: {
              show: true,
            },
            restore: {
              show: true,
            },
            dataView: {
              show: true,
            },
            dataZoom: {
              show: true,
            },
            magicType: {
              type: ['line', 'bar'],
            },
          },
        },
        tooltip: {
          trigger: 'axis',
        },
        // 图例
        /*legend: {
            data: ['title']
        },*/
        // x轴
        xAxis: {
          data: xItems,
        },
        yAxis: {},
        // 数据
        series: [
          // 数据底部的形状
          // {
          //   name: '',
          //   type: 'pictorialBar',
          //   symbol: 'diamond',
          //   symbolSize: [40, 21], // 宽，高
          //   symbolOffset: [0, 10], // 左 上
          //   symbolPosition: 'start',
          //   z: 1,
          //   data: yItems,
          //   itemStyle: {
          //     color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          //       { offset: 0, color: 'rgba(' + r + ',' + g + ',' + b + ', 1)' }, // // 0% 处的颜色
          //       // { offset: 0.5, color: 'rgba(' + r + ',' + g + ',' + b + ', 1)' },
          //       { offset: 1, color: 'rgba(' + r + ',' + g + ',' + b + ', 0)' },
          //     ]),
          //   },
          // },
          {
            name: seriesName,
            type: 'bar',
            barWidth: w, // 柱子的宽度
            data: yItems,
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  // { offset: 0, color: '#83bff6' },
                  // { offset: 0.5, color: '#188df0' },
                  // { offset: 1, color: '#188df0' },
                  { offset: 0, color: 'rgba(' + r + ',' + g + ',' + b + ', 1)' }, // // 0% 处的颜色
                  // { offset: 0.5, color: 'rgba(' + r + ',' + g + ',' + b + ', 1)' },
                  { offset: 1, color: 'rgba(' + r + ',' + g + ',' + b + ', 0)' },
                ]),
              },
            },
            emphasis: {
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(' + rr + ',' + gg + ',' + bb + ', 0.5)' },
                  { offset: 0.7, color: 'rgba(' + rr + ',' + gg + ',' + bb + ', 1)' },
                  { offset: 1, color: 'rgba(' + rr + ',' + gg + ',' + bb + ', 0.5)' },
                ]),
              },
            },
            // markPoint: {
            //   data: [
            //     { type: 'max', name: '最大值' },
            //     { type: 'min', name: '最小值', symbol: 'arrow' },
            //   ],
            // },
            markLine: {
              symbol: ['circle', 'arrow'], // 箭头
              itemStyle: {
                normal: {
                  silent: true,
                  lineStyle: {
                    type: 'dashed',
                    color: color,
                    // type: 'solid'  //'dotted'虚线 'solid'实线
                  },
                  label: {
                    show: true,
                    //  position: 'center',
                    //  formatter: '{a}',
                  },
                },
              },
              data: isAaverage
                ? [
                    {
                      type: 'average',
                      name: '平均值',
                      label: {
                        show: true, // 显示平均值
                        color: color,
                        fontSize: '14',
                      },
                    },
                  ]
                : [],
              large: true,
              effect: {
                show: false,
                loop: true,
                period: 0,
                scaleSize: 2,
                color: null,
                shadowColor: null,
                shadowBlur: null,
              },
            },
          },
        ],
      };

      if (is3D) {
        // 如果是3D显示效果，因为series中增加了顶部形状及右侧的bar，所以需用自定义的formatter
        if (seriesCount == 1) {
          options.tooltip = {
            trigger: 'axis',
            formatter(params) {
              console.log(params);
              return `${params[0].axisValueLabel}<br/><span style="background: ${color}; height:10px; width: 10px; border-radius: 50%;display: inline-block;margin-right:10px;"></span>${params[0].seriesName} ${params[0].data}
              `;
            },
          };
        } else {
          options.tooltip = {
            trigger: 'axis',
            formatter(params) {
              console.log(params);
              return `${params[0].axisValueLabel}<br/><span style="background: ${color}; height:10px; width: 10px; border-radius: 50%;display: inline-block;margin-right:10px;"></span>${params[0].seriesName} ${params[0].data} <br/>
              <span style="background: ${color2}; height:10px; width: 10px; border-radius: 50%;display: inline-block;margin-right:10px;"></span>${params[3].seriesName} ${params[3].data}
              `;
            },
          };
        }

        options?.series?.push({
          name: '',
          type: 'bar',
          barWidth: w, // 柱条的宽度，不设时自适应。
          barGap: 0, // 不同系列的柱间距离
          data: yItems,
          itemStyle: {
            normal: {
              color: () => {
                return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(' + r + ',' + g + ',' + b + ', 1)' }, // 0% 处的颜色
                  { offset: 1, color: 'rgba(' + r + ',' + g + ',' + b + ', 0)' },
                ]);
              },
              borderWidth: 0.1,
              borderColor: 'transparent',
            },
          },
        });

        options?.series?.push(
          // 数据顶部的样式
          {
            name: '',
            type: 'pictorialBar',
            symbol: 'diamond',
            symbolSize: [w * 2, w + 1],
            symbolOffset: symbolOffset,
            symbolPosition: 'end',
            z: 3,
            itemStyle: {
              normal: {
                color: () => {
                  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(' + rTop + ',' + gTop + ',' + bTop + ', 1)' }, // // 0% 处的颜色
                    { offset: 1, color: 'rgba(' + rTop + ',' + gTop + ',' + bTop + ', 1)' },
                  ]);
                },
                // label: {
                //   show: true, // 开启显示
                //   position: 'top', // 在上方显示
                //   textStyle: {
                //     fontSize: '12',
                //     color: '#B0E1FF',
                //   },
                // },
              },
            },
            data: yItems,
          },
        );
      }

      if (isTopLabel) {
        let mySeries: any = options.series;
        mySeries[0].itemStyle.normal['label'] = {
          formatter: '{c}' + unit, // 自定义展示，默认显示原值
          show: true,
          position: 'top',
          textStyle: {
            // fontWeight: 'bolder',
            fontSize: '12',
            color: color,
          },
        };
      }

      if (seriesCount == 2) {
        if (isLegend) {
          options.legend = {
            data: [seriesName, seriesName2],
          };
        }

        let series2 = {
          name: seriesName2,
          type: 'bar',
          barWidth: w, //柱子的宽度
          data: yItems2,
          itemStyle: {
            normal: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(' + r2 + ',' + g2 + ',' + b2 + ', 1)' }, // // 0% 处的颜色
                // { offset: 0.5, color: 'rgba(' + r2 + ',' + g2 + ',' + b2 + ', 1)' },
                { offset: 1, color: 'rgba(' + r2 + ',' + g2 + ',' + b2 + ', 0)' },
              ]),
              // barBorderRadius: 8, // 圆角
            },
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(' + rr2 + ',' + gg2 + ',' + bb2 + ', 0.5)' },
                { offset: 0.7, color: 'rgba(' + rr2 + ',' + gg2 + ',' + bb2 + ', 1)' },
                { offset: 1, color: 'rgba(' + rr2 + ',' + gg2 + ',' + bb2 + ', 0.5)' },
              ]),
            },
          },
          // markPoint: {
          //   data: [
          //     { type: 'max', name: '最大值' },
          //     { type: 'min', name: '最小值', symbol: 'arrow' },
          //   ],
          // },
          markLine: {
            symbol: ['circle', 'arrow'], // 箭头
            itemStyle: {
              normal: {
                silent: true,
                lineStyle: {
                  type: 'dashed',
                  color: color2,
                  // type: 'solid'  //'dotted'虚线 'solid'实线
                },
                label: {
                  show: true,
                  //  position: 'center',
                  //  formatter: '{a}',
                },
              },
            },
            data: isAaverage
              ? [
                  {
                    type: 'average',
                    name: '平均值',
                    label: {
                      show: true, // 显示平均值
                      color: color2,
                      fontSize: '14',
                    },
                  },
                ]
              : [],
            large: true,
            effect: {
              show: false,
              loop: true,
              period: 0,
              scaleSize: 2,
              color: null,
              shadowColor: null,
              shadowBlur: null,
            },
          },
        };
        options['series']?.push(series2);

        if (is3D) {
          let series2_3 = {
            name: '',
            type: 'bar',
            barWidth: w, // 柱条的宽度，不设时自适应。
            barGap: 0, // 不同系列的柱间距离
            data: yItems2,
            itemStyle: {
              normal: {
                color: () => {
                  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(' + r2 + ',' + g2 + ',' + b2 + ', 1)' }, // 0% 处的颜色
                    { offset: 1, color: 'rgba(' + r2 + ',' + g2 + ',' + b2 + ', 0)' },
                  ]);
                },
                borderWidth: 0.1,
                borderColor: 'transparent',
              },
            },
          };
          // 数据顶部的样式
          let series2Top = {
            name: '',
            type: 'pictorialBar',
            symbol: 'diamond',
            symbolSize: [w * 2, w + 1],
            symbolOffset: symbolOffset2,
            symbolPosition: 'end',
            z: 3,
            itemStyle: {
              normal: {
                color: () => {
                  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(' + rTop2 + ',' + gTop2 + ',' + bTop2 + ', 1)' }, // // 0% 处的颜色
                    { offset: 1, color: 'rgba(' + rTop2 + ',' + gTop2 + ',' + bTop2 + ', 1)' },
                  ]);
                },
                // label: {
                //   show: true, // 开启显示
                //   position: 'top', // 在上方显示
                //   textStyle: {
                //     fontSize: '12',
                //     color: '#B0E1FF',
                //   },
                // },
              },
            },
            data: yItems2,
          };

          options['series']?.push(series2_3);
          options['series']?.push(series2Top);
        }
      }

      setOptions(options);
      if (isTopLabel) {
        let mySeries: any = options.series;
        mySeries[1].itemStyle.normal['label'] = {
          formatter: '{c}' + unit, // 自定义展示，默认显示原值
          show: true,
          position: 'top',
          textStyle: {
            // fontWeight: 'bolder',
            fontSize: '12',
            color: color2,
          },
        };
      }
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
