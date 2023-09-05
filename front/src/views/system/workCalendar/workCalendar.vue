<template>
  <div class="h-full">
    <Spin :spinning="spinning">
      <div class="bg-white w-full flex justify-between p-2 mb-1">
        <div>
          <DatePicker
            v-model:value="year"
            picker="year"
            @change="getChangeYear"
            :allow-clear="false"
          />
        </div>
        <div>
          <Popconfirm
            placement="top"
            title="确定初始化吗？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="initList"
          >
            <a-button type="primary" class="mr-2">初始化</a-button>
          </Popconfirm>
          <a-button type="primary" class="mr-2" @click="batch(1)">批量修改日期类型</a-button>
          <a-button type="primary" class="mr-2" @click="batch(2)">批量修改工作时间</a-button>
        </div>
      </div>
      <Row justify="space-around" :gutter="[10, 10]">
        <Col :span="6" v-for="(item, index) in listData" :key="index">
          <Calendar
            v-model:value="item.month"
            :fullscreen="false"
            :disabledDate="(value) => disabledDate(value, item.month)"
            @select="handleSelect"
          >
            <template #headerRender>
              <div class="text-center pt-2 pb-2">{{ getMonth(item.month) }} 月</div>
            </template>
            <template #dateFullCellRender="{ current }">
              <div
                :style="{ backgroundColor: getWeekendDay(current, item) }"
                :class="{
                  'bg-gray-50 relative': true,
                  // 'bg-yellow-100': getWeekendDay(current, item),
                  'border-1 border-solid border-green-200':
                    dayjs(currentDay).format('YYYY-MM-DD') === dayjs(current).format('YYYY-MM-DD'),
                }"
                @mouseenter="mouseenter(item)"
              >
                <span>{{ getCurMonthDay(current, item.month) }}</span>
                <!-- <a-popover title="Title" :getPopupContainer="(triggerNode) => triggerNode.parentNode">
                <template #content>
                  <p>Content</p>
                  <p>Content</p>
                </template>
                <span>{{ current.format('DD') }}</span>
              </a-popover> -->
              </div>
            </template>
          </Calendar>
        </Col>
      </Row>
      <WorkCalendarModal @register="registerModal" @success="handleSuccess" />
    </Spin>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, onMounted } from 'vue';
  import { Calendar, Row, Col, DatePicker, Popconfirm, Popover, Spin } from 'ant-design-vue';
  import type { Dayjs } from 'dayjs';
  import dayjs from 'dayjs';
  import { useModal } from '/@/components/Modal';
  import WorkCalendarModal from './modules/workCalendarModal.vue';
  import {
    getOacalenderList,
    getOacalenderInit,
    getDayInfo,
    getConfig,
  } from '/@/api/workOffice/workOffice';
  const APopover = Popover;
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'workCalendar',
    components: {
      Calendar,
      Row,
      Col,
      DatePicker,
      WorkCalendarModal,
      Popconfirm,
      APopover,
      Popover,
      Spin,
    },
    setup() {
      const year = ref<Dayjs>(dayjs());
      const listData = ref<Recordable[]>([]);
      const currentDay = ref<Dayjs>(dayjs());
      const yearList = ref<Recordable>([]);
      const getListData = (value: Dayjs) => {
        listData.value = [];
        const newYearList = JSON.parse(JSON.stringify(yearList.value));
        for (let i = 0; i < 12; i++) {
          const days = [] as any;
          for (var j = newYearList.length - 1; j >= 0; j--) {
            if ((dayjs(newYearList[j].day).format('M') as any) == i + 1) {
              days.push(newYearList[j]);
              newYearList.splice(j, 1);
            }
          }
          listData.value.push({
            month: value.month(i),
            show: false,
            days: days,
          });
        }
      };

      //获取年份
      function getChangeYear() {
        currentDay.value = dayjs();
        init();
      }
      const spinning = ref(false);
      //初始化
      const initList = () => {
        spinning.value = true;
        getOacalenderInit({ year: dayjs(unref(year)).format('YYYY') }).then(() => {
          init();
        });
      };

      const init = () => {
        spinning.value = true;
        getOacalenderList({ year: dayjs(unref(year)).format('YYYY') })
          .then((data) => {
            yearList.value = data || [];

            getListData(unref(year));
          })
          .finally(() => {
            spinning.value = false;
          });
      };

      //返回当前月天
      function getCurMonthDay(value: Dayjs, month: Dayjs) {
        if (dayjs(value).format('YYYY-MM') == dayjs(month).format('YYYY-MM')) {
          return value.date();
        } else {
          return '';
        }
      }
      //点击当天
      function handleSelect(value: Dayjs) {
        currentDay.value = value;
        // let type = currentDay.value.day() === 0 || currentDay.value.day() === 6 ? 2 : 1;
        getDayInfo({ date: currentDay.value.format('YYYY-MM-DD') }).then((res) => {
          let record = {
            ...res,
            oa_date: currentDay.value.format('YYYY-MM-DD'),
          };
          openDayModal(record, 3);
        });
      }
      //获取月
      function getMonth(month: Dayjs) {
        return dayjs(month).format('MM');
      }
      onMounted(() => {
        getChangeYear();
      });
      //是否周末
      function getWeekendDay(value: Dayjs, record: Recordable) {
        let bgColor = '#ccc';
        if (record.days && record.days.length > 0) {
          let day = record.days.find((item) => item.day == dayjs(value).format('YYYY-MM-DD'));
          if (day) {
            if (day.content && day.content.dateType == 0) {
              bgColor = '#fff';
            }
            if (day.content && (day.content.dateType == 2 || day.content.dateType == 1)) {
              bgColor = '#FBF5C7';
            }
          }
        }
        return bgColor; //(dayjs(value).day() == 0 || dayjs(value).day() == 6)&&record.days ;
      }
      //弹窗
      const [registerModal, { openModal }] = useModal();
      function openDayModal(record: object, type: number) {
        openModal(true, {
          record,
          type,
        });
      }
      //批量处理 1:批量修改日期类型 2:批量修改工作时间
      function batch(value: number) {
        getConfig().then((res) => {
          let record = {
            work_time_begin_a: res.morningBegin,
            work_time_end_a: res.morningEnd,
            work_time_begin_b: res.afternoonBegin,
            work_time_end_b: res.afternoonEnd,
            work_time_begin_c: res.nightBegin,
            work_time_end_c: res.nightEnd,
          };
          openDayModal(record, value);
        });
      }

      //禁用非当前月日期
      const disabledDate = (value: Dayjs, month: string) => {
        return value < dayjs(month).startOf('month') || value > dayjs(month).endOf('month');
      };

      //鼠标移入
      const mouseenter = (record: Recordable) => {
        record.show = true;
      };
      //抽屉返回
      const handleSuccess = () => {
        init();
      };
      return {
        year,
        listData,
        getChangeYear,
        getCurMonthDay,
        handleSelect,
        getMonth,
        getWeekendDay,
        currentDay,
        dayjs,
        registerModal,
        initList,
        batch,
        mouseenter,
        handleSuccess,
        disabledDate,
        spinning,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.ant-picker-cell-disabled::before) {
    background-color: transparent;
  }
</style>
