<template>
	<view class="content">
		<view>
			<view class="flex year-month">
				<view @click="prevMonth">上一月</view>
				<view>
					<picker mode="date" fields="month" @change="monthChange">
						<text class="bold">{{time.year}} - {{time.month + 1}}</text>
					</picker>
				</view>
				<view @click="nextMonth">下一月</view>
			</view>
			<view class="flex">
				<view class="flex-item flex" v-for="item in weeks" >
					<text class="week">{{item}}</text>
				</view>
			</view>
			<view class="flex-wrap">
				<template v-for="item in visibleDays" :Key="item.day">
					<view class="day-box flex-column">
						<text
							class="day"
							@click="clickDate(item.day)"
							:style="[
							isToday(item.day) && todayObj,
							isClick(item.day) && selectedObj,
						]"
							:class="[
							{selected: isClick(item.day)},
							{notCurrentMonth: !isCurrentMonth(item.day)}
						]"
						>{{item.day | dayFilter}}</text>
						<template v-if="showText">
							<text
								v-if="isCurrentMonth(item.day)"
								class="day-text"
								:style="{color: textColor}"
							>
								{{item.data.value || ''}}
							</text>
						</template>
						<template v-if="showDot">
							<text
								v-if="isCurrentMonth(item.day) && item.data.dot && item.data.active"
								class="day-dot"
							></text>
							<text
								v-if="isCurrentMonth(item.day) && item.data.dot && !item.data.active"
								class="day-dot dot-gray"
							></text>
						</template>
					</view>
				</template>
			</view>
		</view>
	</view>
</template>

<script>

	const getYearMonthDay = (date) => {
		let year = date.getFullYear();
		let month = date.getMonth();
		let day = date.getDate();
		return {
			year,
			month,
			day
		}
	}
	const getDate = (year, month, day) => {
		return new Date(year, month, day)
	}

	export default {
		data() {
			return {
				iArr: [1,2,3,4,5,6],
				jArr: [1,2,3,4,5,6,7],
				value: new Date(),
				weeks: ['日', '一', '二', '三', '四', '五', '六'],
				click_time: {},
				month_data: this.extraData,
				time: this.defaultTime,
				todayObj: {
					background: this.bgColor,
					color: '#ffffff'
				},
				selectedObj: {
					background: this.selColor,
					color: '#ffffff'
				}
			}
		},
		props: {
			bgColor: {
				type: String,
				default: '#4198f8'
			},
			selColor: {
				type: String,
				default: '#4198f8'
			},
			textColor: {
				type: String,
				default: '#4198f8'
			},
			defaultTime: {
				type: Object,
				default: ()=> {
					return {
						year: getYearMonthDay(new Date()).year,
						month: getYearMonthDay(new Date()).month
					}
				}
			},
			extraData: {
				type: Array,
				default: ()=> {
					return [] // {date: '2020-6-3', value: '签到', dot: true, active: true}
				}
			},
			showText: {
				type: Boolean,
				default: true
			},
			showDot: {
				type: Boolean,
				default: false
			}
		},
		filters: {
			dayFilter(val) {
				return val.getDate();
			}
		},
    watch: {
      extraData:{
        handler(newV, oldV) {
          if (newV !== oldV) {
            this.month_data = newV
          }
        },
        deep:true
      }
    },
		computed: {
			visibleDays() { // 计算当月展示日期
				let {time: {year, month}, month_data} = this;
				let currentFirstDay = getDate(year, month, 1);
				let week = currentFirstDay.getDay();
				let startDay = currentFirstDay - week * 60 * 60 * 1000 * 24;
				let arr = [];
				for(let i = 0; i < 42; i++) {
					let day = new Date(startDay + i * 60 * 60 * 1000 * 24);
					let {year: dayY, month: dayM, day: dayD} = getYearMonthDay(day);
					let data = {};
					for (let item of month_data) {
						let dateString = item.date;
						let dateArr = dateString.indexOf('-') !== -1
							? dateString.split('-')
							: dateString.indexOf('/') !== -1
								? dateString.split('/')
								: [];
						if (dateArr.length === 3
							&& Number(dateArr[0]) === Number(dayY)
							&& Number(dateArr[1]) === (Number(dayM) + 1)
							&& Number(dateArr[2]) === Number(dayD)) {
							data = item
						}
					}
					let obj = {
						day,
						data
					}
					arr.push(obj)
				}
				return arr
			}
		},
		mounted() {

		},
		methods: {
			isCurrentMonth(date) { // 是否当月
				let {year, month} = getYearMonthDay(getDate(this.time.year, this.time.month, 1));
				let {year: y, month:m} = getYearMonthDay(date);
				return year === y && month === m;
			},
			isToday(date) { // 是否当天
				let {year, month, day} = getYearMonthDay(new Date());
				let {year: y, month: m, day: d} = getYearMonthDay(date);
				return year === y && month === m && day === d;
			},
			isClick(date) { // 是否是点击日期
				let {click_time} = this;
				if (!click_time.day) return false;
				let {year, month, day} = getYearMonthDay(getDate(click_time.year, click_time.month, click_time.day));
				let {year: y, month: m, day: d} = getYearMonthDay(date);
				return year === y && month === m && day === d;
			},
			clickDate(date) { // 点击日期
				let {year, month, day} = getYearMonthDay(date);
				this.click_time = {year, month, day};
				this.$emit('calendarTap', {year, month, day})
			},
			prevMonth() { // 上一月
				let { time: { year, month} } = this;
				let d = getDate(year, month, 1);
				d.setMonth(d.getMonth() - 1);
				this.time = getYearMonthDay(d);
				// this.click_time = {};
        this.$emit('monthTap', getYearMonthDay(d))
			},
			nextMonth() { // 下一月
				// 获取当前的年月的日期
				let { time: { year, month} } = this;
				let d = getDate(year, month, 1);
				d.setMonth(d.getMonth() + 1);
				this.time = getYearMonthDay(d);
				// this.click_time = {};
        this.$emit('monthTap', getYearMonthDay(d))
			},
			monthChange(e) {
				let {value} = e.detail;
				let timeArr = value.split('-');
				this.time = {year: timeArr[0], month: timeArr[1] - 1, day: 1};
        this.$emit('monthTap',{year: timeArr[0], month: timeArr[1] - 1, day: 1})
			}
		}
	}
</script>

<style scoped lang="scss">
	.content {
		width: 750rpx;
		margin: 0 auto;
	}
	.flex {
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: space-between;
		flex-direction: row;
	}
	.flex-wrap {
		width: 100%;
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		flex-direction: row;
	}
	.flex-column {
		width: 100%;
		height: 106rpx;
		display: flex;
		flex-direction: column;
		justify-content: flex-start;
		align-items: center;
	}
	.flex-item {
		flex: 1;
	}
	.bold {
		font-weight: bold;
		font-size: 28rpx;
	}
	.year-month {
		width: 400rpx;
		margin: 0 auto 20rpx;
		padding-top: 20rpx;
	}
	.week {
		margin: 20rpx 20rpx 40rpx;
		width: 60rpx;
		text-align: center;
		color: #999999;
	}
	.day-box {
		width: 100rpx;
		text-align: center;
		display: flex;
		flex-direction: column;
	}
	.day {
		width: 60rpx;
		height: 60rpx;
		line-height: 60rpx;
		border-radius: 50%;
		text-align: center;
		font-weight: 600;
	}
	.day-text {
		font-size: 22rpx;
	}
	.day-dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 50%;
		background: #4cd964;
		&.dot-gray {
			background: gray;
		}
	}
	.today, .selected {
		background: #4198f8;
		color: #ffffff;
	}
	.notCurrentMonth {
		color: #999999;
		pointer-events: none;
		background: none;
	}
</style>
