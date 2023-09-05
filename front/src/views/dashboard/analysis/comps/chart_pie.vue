<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div> <Icon :icon="icon" :size="26" v-if="icon" class="mr-2" />{{ header }}</div>
      </template>
      <div v-if="isPercent" :style="{ height, width }" class="flex justify-center w-full flex-wrap">
        <div v-for="(item, index) in chartData" :key="index">
          <div :id="`myChart${index}`" :style="[{ height: '120px', width: '150px' }]"></div>
          <div class="text-center mb-2" style="margin-top: -10px">{{ item.name }}</div>
        </div>
      </div>
      <div v-else>
        <div id="myChart0" ref="chartRef" :style="{ height, width }"></div>
      </div>
    </ShareCard>
  </div>
</template>
<script lang="ts" setup>
  import { basicProps } from '../components/props';
  import { onMounted, ref, Ref, watch } from 'vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { Card } from 'ant-design-vue';
  import { getPie } from '/@/api/system/system';
  import Icon from '/@/components/Icon/index';
  import * as echarts from 'echarts';
  import { Item } from 'ant-design-vue/lib/menu';
  import { multipleGradientColor } from '/@/utils/colorUtils';
  import { hexToRGB, darken } from '/@/utils/color';
  import 'echarts-liquidfill/src/liquidFill.js';
  import ShareCard from './ShareCard.vue';

  // 与上行效果一样
  // import echarts from '/@/utils/lib/echarts';

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
  // const { setOptions, getInstance } = useECharts(chartRef as Ref<HTMLDivElement>);
  const title = ref('饼图');
  const chartData = ref([]);
  const isPercent = ref(false);
  let isLegend = true;
  let isLabel = false;
  let isLabelInner = false;
  let labelFormatter;
  let isAmount = true;
  let amountTitle = '';
  let radius: any = '70%';
  let isRing = true;
  let sum = 0;
  let rotateType = 2;
  let isShadow = false;
  let shadowColor = '';
  let shadowBlur = 20;
  let names = [];
  let fontSize = '18';
  let subFontSize = '14';
  let color = ''; // 百分比组前景色
  let color2 = ''; // 百分比组背景色
  let isLiquidFill = false;
  let digit = 0; // 小数点后位数

  onMounted(() => {
    // 等待dom生成，否则会报invalid dom错误
    setTimeout(() => {}, 100);
  });

  const fetch = () => {
    getPie({ id: props.chartId }).then((res) => {
      title.value = res?.title;
      chartData.value = res?.chartData;
      console.log('chartData', chartData.value);
      isLegend = res?.isLegend;
      isLabel = res?.isLabel;
      isLabelInner = res?.isLabelInner;
      labelFormatter = res?.labelFormatter;
      isAmount = res?.isAmount;
      amountTitle = res?.amountTitle;
      isPercent.value = res?.isPercent;
      color = res?.color;
      color2 = res?.color2;
      isLiquidFill = res?.isLiquidFill;
      digit = res?.digit;

      radius = '70%';
      isRing = res?.isRing;
      if (isRing) {
        if (isPercent.value) {
          radius = ['70%', '80%'];
        } else {
          radius = ['60%', '80%'];
        }
      }
      rotateType = res?.rotateType;

      isShadow = res?.isShadow;
      shadowColor = res?.shadowColor;
      shadowBlur = res?.shadowBlur;

      if (isShadow) {
        for (let k in chartData.value) {
          chartData.value[k].itemStyle = {
            normal: {
              borderWidth: 0,
              shadowBlur: shadowBlur,
              borderColor: shadowColor,
              shadowColor: shadowColor,
            },
          };
        }
      }

      names = chartData.value.map((item) => item.name);
      const values = chartData.value.map((item) => item.value);
      for (let i in values) {
        sum += values[i];
      }

      // 如果按百分比模式显示
      if (isPercent.value) {
        fontSize = '20';
        subFontSize = '10';

        let colorArr = multipleGradientColor(['#0088cc', '#62b62f', '#fccb00'], chartData.length);
        console.log('colorArr', colorArr);
        let clr2;
        let clr2Darken;
        if (color2 == '') {
          clr2 = 'rgba(0,0,0,.2)';
          clr2Darken = 'rgba(0,0,0,.5)';
        } else {
          clr2Darken = darken(color2, 10);
          clr2Darken = hexToRGB(clr2Darken, 0.5);
          clr2 = hexToRGB(color2, 0.5);
        }

        for (let i in chartData.value) {
          let myData = chartData.value;
          myData.forEach((item) => {
            item.per = parseInt(((item.value / sum) * 100).toFixed(digit));
          });
          // 重新生成myData
          let clr;
          if (color == '') {
            clr = colorArr[i];
          } else {
            clr = color;
          }
          console.log('color', clr);
          myData = [
            {
              name: myData[i].name,
              value: myData[i].per,
              itemStyle: { normal: { color: clr } },
            },
            {
              name: '其它',
              value: 100 - myData[i].per,
              itemStyle: {
                normal: {
                  color: clr2,
                },
                emphasis: {
                  color: clr2Darken,
                },
              },
            },
          ];

          if (isShadow) {
            for (let k in myData) {
              Object.assign(myData[k].itemStyle.normal, {
                borderWidth: 0,
                shadowBlur: shadowBlur,
                borderColor: shadowColor,
                shadowColor: shadowColor,
              });
            }
          }

          // 等待dom生成，否则会报invalid dom错误
          setTimeout(() => {
            console.log('myChart' + i, myData);
            let myChart = echarts.init(document.getElementById('myChart' + i), 'light');
            let subTitle = '占比 ' + myData[0].value + '%';
            setChartOption(myChart, myData, chartData.value[i].value, subTitle, fontSize);
            window.onresize = function () {
              //自适应大小
              myChart.resize();
            };
          }, 100);
        }
      } else {
        let myChart = echarts.init(document.getElementById('myChart0'), 'light');
        setChartOption(myChart, chartData.value, sum, amountTitle, fontSize);
        window.onresize = function () {
          //自适应大小
          myChart.resize();
        };
      }
    });

    const setChartOption = (myChart, myData, mySum, amountTitle, fontSize) => {
      let titleTop = '40%';
      if (isPercent.value) {
        titleTop = '30%';
      }
      let option = {
        grid: {
          top: '4%',
          left: '6%',
          right: '4%',
          bottom: '15%',
        },
        animation: false, // 当旋转时，animation需置为false，否则会使环形结构脱节
        animationThreshold: 100,
        animationDurationUpdate: function (idx) {
          // 越往后的数据延迟越大
          return idx * 1000;
        },
        title: {
          show: isAmount,
          text: `{b|${mySum}}`,
          textStyle: {
            rich: {
              b: {
                fontSize: fontSize,
              },
            },
          },
          subtext: `{a|${amountTitle}}`,
          subtextStyle: {
            rich: {
              a: {
                fontSize: subFontSize,
              },
            },
          },
          left: 'center',
          top: titleTop,
        },
        tooltip: {
          show: !isPercent.value,
          enterable: true,
          trigger: 'item', // 'axis',
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow', // 默认为直线，可选为：'line' | 'shadow'
          },
          formatter: function (params) {
            // 水球是没有percent的，所以需判断
            if (params.percent) {
              return params.name + ':' + params.value + '(' + params.percent.toFixed(0) + '%)';
            }
          } /*,
          position: function (point, params, dom, rect, size) {
              // 固定在顶部
              return [point[0], '10%'];
          }*/,
        },
        toolbox: {
          show: false,
          feature: {
            saveAsImage: {
              show: true,
              excludeComponents: ['toolbox'],
              pixelRatio: 2,
            },
          },
        },
        legend: {
          show: isLegend,
          // 图例
          orient: 'vertical',
          left: 'left',
          data: names,
          // icon: 'circle', //图例的形状
          type: 'scroll', // 图例是否能滚动
        },
        series: [
          {
            name: title.value,
            type: 'pie', // 设置图表类型为饼图
            // radius: '70%', // 半径，外半径为可视区尺寸（容器高宽中较小一项）的 55% 长度。
            radius: radius, // 70% 表示镂空的部分，80%表示半径
            center: ['50%', '50%'], // 位置
            hoverAnimation: false, // 鼠标悬浮是否有区域弹出动画，false:无 true:有
            label: {
              // 饼图图形上的文本标签
              normal: {
                show: isLabel,
                position: isLabelInner ? 'inner' : 'outer', //标签的位置
                textStyle: {
                  fontWeight: 300,
                  fontSize: 12, //文字的字体大小
                },
                formatter: labelFormatter, // {a}{b}: {c} ({d}%) 模板变量有 {a}、{b}、{c}、{d}，分别表示系列名，数据名，数据值，百分比;
                /*formatter:function(data){
                    // return data.seriesName + "<br/>"+ data.name+ " : " + data.value + " ("+data.percent.toFixed(1)+"%)";
                    return data.name+ ":" + data.value + "("+data.percent.toFixed(0)+"%)";
                }*/
              },
            },
            // roseType: 'radius', // 是否展示成南丁格尔图：area radius
            data: myData, // 数据数组，name 为数据项名称，value 为数据项值
          },
        ],
      };

      if (isLiquidFill) {
        option.series.push({
          type: 'liquidFill',
          radius: '70%',
          center: ['50%', '50%'],
          // data: [0.5, 0.5], // [0.5, 0.5, 0.5], // data个数代表波浪数
          data: isPercent.value
            ? [
                {
                  value: myData[0].value / 100,
                  itemStyle: {
                    normal: {
                      color: color,
                      opacity: 0.4,
                    },
                  },
                },
              ]
            : [
                {
                  value: 0.4,
                  itemStyle: {
                    normal: {
                      color: color,
                      opacity: 0.3,
                    },
                  },
                },
                {
                  value: 0.4,
                  itemStyle: {
                    normal: {
                      color: color,
                      opacity: 0.3,
                    },
                  },
                },
                {
                  value: 0.4,
                  itemStyle: {
                    normal: {
                      color: color,
                      opacity: 0.3,
                    },
                  },
                },
              ],
          backgroundStyle: {
            borderWidth: 1,
            // color: 'rgb(255,255,255,1)',
            color: '#fff',
          },
          color: ['#53d5ff'],
          // 修改波浪颜色
          // color:['yellow'], 所有波浪一个颜色
          // color: ['yellow', 'red', 'pink'], // 每个波浪不同颜色，颜色数组长度为对应的波浪个数
          label: {
            normal: {
              // formatter: (0.5 * 100).toFixed(2) + '%',
              formatter: '',
              textStyle: {
                fontSize: 14,
              },
            },
          },
          outline: {
            show: false,
          },
        });
      }

      console.log('option', option);

      myChart.setOption(option);

      //自适应大小
      myChart.resize();

      // let myChart = getInstance();
      myChart?.on('mouseover', function (params) {
        if (rotateType == 1) {
          if (!timer) {
            startTimer();
          }
        } else if (rotateType == 2) {
          stopTimer();
        }
      });

      myChart?.on('mouseout', function (params) {
        if (rotateType == 1) {
          stopTimer();
        } else if (rotateType == 2) {
          startTimer();
        }
      });

      function doing() {
        let option = myChart?.getOption();
        option.series[0].startAngle = option.series[0].startAngle - 2;
        // option.series[6].data[0].value = option.series[6].data[0].value + 1;
        myChart?.setOption(option);
      }

      let timer;
      function startTimer() {
        timer = setInterval(doing, 50);
      }

      function stopTimer() {
        clearInterval(timer);
        timer = null;
      }

      if (rotateType == 2) {
        startTimer();
      }
    };
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
