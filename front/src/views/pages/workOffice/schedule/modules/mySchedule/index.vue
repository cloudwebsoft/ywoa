<template>
  <div class="bg-white h-full my-schedule">
    <div class="w-full flex justify-around h-12 items-center">
      <a-button type="primary" @click="today"> 今天 </a-button>
      <div>
        <DatePicker
          v-model:value="date"
          :picker="activeType"
          :bordered="false"
          @change="changeDate"
          :allow-clear="false"
        />
      </div>
      <div>
        <RadioGroup
          v-model:value="activeType"
          button-style="solid"
          @change="(e) => changeType(e.target.value)"
        >
          <RadioButton value="date">日</RadioButton>
          <RadioButton value="week">周</RadioButton>
          <RadioButton value="month">月</RadioButton>
        </RadioGroup>
      </div>
    </div>
    <Table :data-source="myDataSource" :pagination="false" :scroll="{ y: y }" bordered>
      <template v-for="item in cusColumns" :key="item.dataIndex">
        <TableColumn
          :data-index="item.dataIndex"
          :align="item.align"
          :custom-cell="customCell"
          :width="item.width ? item.width : ''"
        >
          <template #title>
            <span class="font-bold">
              <div v-if="item.day && activeType != 'month'">{{ item.day }}</div>
              <div v-if="item.week">{{ item.week }}</div>
            </span>
          </template>
          <template #default="{ text }">
            <template v-if="activeType === 'month'">
              <div v-if="item.title === '周数'">{{ text }}</div>
              <div v-else class="min-h-40 flex flex-col justify-items-start">
                <div
                  class="text-right hover:text-red-400 hover:underline cursor-pointer"
                  @click="toItemDay(text.day)"
                >
                  {{ text.day ? dayjs(text.day).format('DD') : '' }}
                </div>
                <div class="max-h-30 overflow-auto">
                  <div
                    v-for="(d, i) in text.con"
                    :key="i"
                    :class="{ bg_green: d.closed, bg_red: !d.closed, 'p-1': true }"
                  >
                    <Dropdown trigger="click">
                      <span class="text-left hover:text-red-400 hover:underline cursor-pointer">
                        <div>
                          {{ dayjs(d.myDate).format('HH:mm') }} ~
                          {{ dayjs(d.endDate).format('HH:mm') }}
                        </div>
                        <div>
                          {{ d.title }}
                        </div>
                      </span>
                      <template #overlay>
                        <Menu>
                          <MenuItem key="1" @click="clickItmeInfo(d)">编辑</MenuItem>
                          <Popconfirm
                            title="您确定要删除么"
                            ok-text="确定"
                            cancel-text="取消"
                            @confirm="confirm(d)"
                          >
                            <MenuItem key="2">删除</MenuItem>
                          </Popconfirm>
                        </Menu>
                      </template>
                    </Dropdown>
                  </div>
                </div>
              </div>
            </template>
            <template v-else-if="activeType === 'date' && item.dataIndex == 'con1'">
              <span v-if="text && text.length > 0">
                <div
                  v-for="(d, i) in text"
                  :key="i"
                  class="text-left p-1"
                  :class="{ bg_green: d.closed, bg_red: !d.closed }"
                >
                  <Dropdown trigger="click">
                    <span class="text-left hover:text-red-400 hover:underline cursor-pointer">
                      {{ dayjs(d.myDate).format('HH:mm') }} ~ {{ dayjs(d.endDate).format('HH:mm') }}
                      {{ d.title }}
                      <!-- <DownOutlined /> -->
                    </span>
                    <template #overlay>
                      <Menu>
                        <MenuItem key="1" @click="clickItmeInfo(d)">编辑</MenuItem>
                        <Popconfirm
                          title="您确定要删除么"
                          ok-text="确定"
                          cancel-text="取消"
                          @confirm="confirm(d)"
                        >
                          <MenuItem key="2">删除</MenuItem>
                        </Popconfirm>
                      </Menu>
                    </template>
                  </Dropdown>
                </div>
              </span>
            </template>
            <template v-else>
              <template
                v-if="item.dataIndex != 'con100' && text && text.con && text.con.length > 0"
              >
                <div
                  v-for="(d, i) in text.con"
                  :key="i"
                  :class="{ bg_green: d.closed, bg_red: !d.closed, 'p-1': true }"
                >
                  <Dropdown trigger="click">
                    <span class="text-left hover:text-red-400 hover:underline cursor-pointer">
                      <div>
                        {{ dayjs(d.myDate).format('HH:mm') }} ~
                        {{ dayjs(d.endDate).format('HH:mm') }}
                      </div>
                      <div>
                        {{ d.title }}
                      </div>
                    </span>
                    <template #overlay>
                      <Menu>
                        <MenuItem key="1" @click="clickItmeInfo(d)">编辑</MenuItem>
                        <Popconfirm
                          title="您确定要删除么"
                          ok-text="确定"
                          cancel-text="取消"
                          @confirm="confirm(d)"
                        >
                          <MenuItem key="2">删除</MenuItem>
                        </Popconfirm>
                      </Menu>
                    </template>
                  </Dropdown>
                </div>
              </template>
              <template v-if="item.dataIndex == 'con100'">
                {{ text }}
              </template>
            </template>
          </template>
        </TableColumn>
      </template>
    </Table>
    <AllScheduleDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, unref } from 'vue';
  import { DatePicker, Radio, Table, Dropdown, Menu, Popconfirm } from 'ant-design-vue';
  import { useTable } from '/@/components/Table';
  import type { Dayjs } from 'dayjs';
  import { dateUtil as dayjs } from '/@/utils/dateUtil';
  import { getListPhase, getPlanDel } from '/@/api/workOffice/workOffice';
  import { useDrawer } from '/@/components/Drawer';
  import AllScheduleDrawer from '../allSchedule/AllScheduleDrawer.vue';
  import { DownOutlined } from '@ant-design/icons-vue';
  export default defineComponent({
    name: 'MySchedule',
    components: {
      DatePicker,
      RadioGroup: Radio.Group,
      RadioButton: Radio.Button,
      Table,
      TableColumn: Table.Column,
      AllScheduleDrawer,
      DownOutlined,
      Dropdown,
      Menu,
      MenuItem: Menu.Item,
      Popconfirm,
    },
    props: {
      listPage: {
        type: Object,
        default: () => {
          return {};
        },
      },
    },
    setup(props) {
      const date = ref<Dayjs>(dayjs());
      const activeType = ref<any>('week');
      const y = ref(500);
      onMounted(() => {
        const clientHeight = document.getElementsByClassName('my-schedule')[0].clientHeight;
        y.value = clientHeight - 184;
        init();
      });
      const init = () => {
        fetch();
      };
      function fetch() {
        changeType(unref(activeType));
      }
      // -------------------------------------------------------我的日程开始---------------------------------------------------
      const [registerTable, {}] = useTable({
        title: '',
        rowKey: 'id',
        searchInfo: {}, //额外的参数
        useSearchForm: false,
        showTableSetting: false,
        bordered: true,
        showIndexColumn: false,
        pagination: false,
      });

      //改变日 周 月
      function changeType(value) {
        activeType.value = value;
        switch (value) {
          case 'date':
            date.value = dayjs();
            changeDay();
            break;
          case 'week':
            date.value = dayjs(dayjs().format('YYYY-MM-DD'));
            changeWeek();
            break;
          case 'month':
            date.value = dayjs();
            changeMonth();
            break;
        }
      }

      let myDataSource = ref<Recordable[]>([]);

      let cusColumns = ref<Recordable[]>([]);
      //改变日期
      function changeDate() {
        switch (unref(activeType)) {
          case 'date':
            changeDay();
            break;
          case 'week':
            changeWeek();
            break;
          case 'month':
            changeMonth();
            break;
        }
      }
      //今天
      function today() {
        changeType('date');
      }
      //改变日
      function changeDay() {
        const days = ref<Recordable[]>([
          {
            day: '00-23点',
            week: '',
            con: 'con100',
            width: 100,
          },
          {
            con: 'con1',
            day: dayjs(date.value).format('YYYY-MM-DD'),
            week: getWeek(dayjs(date.value).isoWeekday()),
          },
        ]);
        setWeekTableCol(unref(days));
      }

      //改变周
      function changeWeek() {
        const days = ref<any>([
          {
            day: '00-23点',
            week: '',
            con: 'con100',
            width: 100,
          },
        ]);
        for (let i = 1; i <= 7; i++) {
          days.value.push({
            day: dayjs(date.value)
              .startOf('isoWeek')
              .add(i - 1, 'day')
              .format('YYYY-MM-DD'),
            week: getWeek(i),
            con: `con${i}`,
          });
        }
        setWeekTableCol(unref(days));
      }
      function setWeekTableCol(data) {
        cusColumns.value = [];
        if (data.length > 0) {
          data.forEach((item) => {
            cusColumns.value.push({
              title: item.day,
              day: item.day,
              week: item.week,
              width: item.width,
              dataIndex: item.con,
              align: 'center',
            });
          });
          myDataSource.value = [];
          if (unref(activeType) == 'date') {
            getListPhase({
              beginDate: dayjs(unref(date)).format('YYYY-MM-DD'),
              endDate: dayjs(unref(date)).format('YYYY-MM-DD'),
            }).then((res) => {
              if (res.list && res.list.length > 0) {
                const list = res.list;
                list.forEach((item) => {
                  const keys = Object.keys(item);
                  myDataSource.value.push({
                    con100: keys[0],
                    con1: item[keys[0]],
                  });
                });
              }
            });
          } else if (unref(activeType) == 'month') {
            // 拿到当月所有日期
            const allDays = ref<Recordable[]>([]);
            const newAllDays = ref<Recordable[]>([]);
            //获取当月天数
            const dayCon = dayjs(unref(date)).daysInMonth();
            for (let i = 1; i <= dayCon; i++) {
              let ni = i < 10 ? `0${i}` : `${i}`;
              const temp = dayjs(unref(date)).format('YYYY-MM') + `-${ni}`;
              allDays.value.push({
                day: temp,
                days: temp,
                weekDay: dayjs(temp).isoWeekday(),
                week: dayjs(temp).isoWeek(),
              });
            }
            newAllDays.value = JSON.parse(JSON.stringify(allDays.value));
            const weekBeginNum = dayjs(unref(allDays)[0].days).isoWeek();
            const weekEndNum = dayjs(unref(allDays)[unref(allDays).length - 1].days).isoWeek();
            const getDay = (weekDay: number, w: number) => {
              return newAllDays.value.filter((item) => item.weekDay === weekDay && item.week === w)
                .length > 0
                ? newAllDays.value.filter(
                    (item, i) =>
                      item.weekDay === weekDay && item.week === w && newAllDays.value.splice(i, 1),
                  )[0].day
                : '';
            };
            getListPhase({
              beginDate: dayjs(unref(date)).startOf('month').format('YYYY-MM-DD'),
              endDate: dayjs(unref(date)).endOf('month').format('YYYY-MM-DD'),
            }).then((res) => {
              if (res.list && res.list.length > 0) {
                const list = res.list;
                const newList = [] as Recordable;
                list.forEach((item) => {
                  const keys = Object.keys(item);
                  newList.push({
                    con: item[keys[0]],
                    week: dayjs(keys[0]).isoWeek(),
                    weekDay: dayjs(keys[0]).isoWeekday(),
                  });
                });
                for (let w = weekBeginNum; w <= weekEndNum; w++) {
                  myDataSource.value.push({
                    con100: `第${w}周`,
                    con1: {
                      day: getDay(1, w),
                      con:
                        newList.filter((f) => f.week == w && f.weekDay == 1).length > 0
                          ? newList.filter((f) => f.week == w && f.weekDay == 1)[0].con
                          : [],
                    },
                    con2: {
                      day: getDay(2, w),
                      con:
                        newList.filter((f) => f.week == w && f.weekDay == 2).length > 0
                          ? newList.filter((f) => f.week == w && f.weekDay == 2)[0].con
                          : [],
                    },
                    con3: {
                      day: getDay(3, w),
                      con:
                        newList.filter((f) => f.week == w && f.weekDay == 3).length > 0
                          ? newList.filter((f) => f.week == w && f.weekDay == 3)[0].con
                          : [],
                    },
                    con4: {
                      day: getDay(4, w),
                      con:
                        newList.filter((f) => f.week == w && f.weekDay == 4).length > 0
                          ? newList.filter((f) => f.week == w && f.weekDay == 4)[0].con
                          : [],
                    },
                    con5: {
                      day: getDay(5, w),
                      con:
                        newList.filter((f) => f.week == w && f.weekDay == 5).length > 0
                          ? newList.filter((f) => f.week == w && f.weekDay == 5)[0].con
                          : [],
                    },
                    con6: {
                      day: getDay(6, w),
                      con:
                        newList.filter((f) => f.week == w && f.weekDay == 6).length > 0
                          ? newList.filter((f) => f.week == w && f.weekDay == 6)[0].con
                          : [],
                    },
                    con7: {
                      day: getDay(7, w),
                      con:
                        newList.filter((f) => f.week == w && f.weekDay == 7).length > 0
                          ? newList.filter((f) => f.week == w && f.weekDay == 7)[0].con
                          : [],
                    },
                  });
                }
              }
            });
          } else if (unref(activeType) == 'week') {
            const beginCol = unref(cusColumns)[1];
            const endCol = unref(cusColumns)[unref(cusColumns).length - 1];
            getListPhase({
              beginDate: beginCol.day,
              endDate: endCol.day,
            }).then((res) => {
              if (res.list && res.list.length > 0) {
                const list = res.list;
                const newList = [] as Recordable;
                list.forEach((l) => {
                  const keys = Object.keys(l);
                  const chi = l[keys[0]]; //子级内容
                  chi.forEach((c) => {
                    const chikeys = Object.keys(c);
                    c['time'] = chikeys[0];
                    c['con'] = c[chikeys[0]];
                  });
                  newList.push({
                    day: keys[0],
                    allTimeList: chi,
                  });
                });
                for (let i = 0; i <= 23; i++) {
                  const ind = i;
                  myDataSource.value.push({
                    con100: `${i < 10 ? '0' + i : i}:00`,
                    con1: newList[0]['allTimeList'][ind],
                    con2: newList[1]['allTimeList'][ind],
                    con3: newList[2]['allTimeList'][ind],
                    con4: newList[3]['allTimeList'][ind],
                    con5: newList[4]['allTimeList'][ind],
                    con6: newList[5]['allTimeList'][ind],
                    con7: newList[6]['allTimeList'][ind],
                  });
                }
              }
            });
          }
        }
      }

      //改变月
      function changeMonth() {
        const days = ref<any>([
          {
            day: '周数',
            week: '周数',
            con: 'con100',
            width: 100,
          },
        ]);
        for (let i = 1; i <= 7; i++) {
          days.value.push({
            day: '',
            week: getWeek(i),
            con: `con${i}`,
            width: 150,
          });
        }
        setWeekTableCol(unref(days));
      }
      //返回周几
      function getWeek(i) {
        return i === 1
          ? '星期一'
          : i === 2
          ? '星期二'
          : i === 3
          ? '星期三'
          : i === 4
          ? '星期四'
          : i === 5
          ? '星期五'
          : i === 6
          ? '星期六'
          : i === 7
          ? '星期日'
          : '';
      }

      function customCell(record, index, column): any {
        // if (index === 0 && column.key != 'con100' && unref(activeType) === 'week') {
        //   return { rowSpan: 0 };
        // }
      }

      //月点击跳转到当前日期
      function toItemDay(text: string) {
        date.value = dayjs(text);
        activeType.value = 'date';
        changeDate();
      }
      // -------------------------------------------------------我的日程结束---------------------------------------------------

      //点击日程事情
      function clickItmeInfo(record: Recordable) {
        openDrawer(true, {
          record,
          isUpdate: true,
        });
      }

      const confirm = (record: Recordable) => {
        console.log('record', record);
        handleDelete(record);
      };

      //删除
      async function handleDelete(record: Recordable) {
        console.log('record', record);
        await getPlanDel({ id: record.id }).then(() => {
          handleSuccess();
        });
      }
      //抽屉编辑
      const [registerDrawer, { openDrawer }] = useDrawer();
      //抽屉回调
      function handleSuccess() {
        changeType(unref(activeType));
      }

      return {
        registerTable,
        activeType,
        date,
        changeType,
        changeWeek,
        cusColumns,
        myDataSource,
        customCell,
        changeDate,
        today,
        dayjs,
        toItemDay,
        clickItmeInfo,
        registerDrawer,
        handleSuccess,
        y,
        handleDelete,
        confirm,
      };
    },
  });
</script>
<style scoped>
  .bg_green {
    background-color: #d8fedd;
  }
  .bg_red {
    background-color: #ffecec;
  }
</style>
