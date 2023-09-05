<template>
	<view class="pageStyle">
		<view class="time_line">
			<u-time-line>
				<u-time-line-item node-top="10" v-for="(item, index) in runList" :key="index" :style="[{ display: item.show ? 'block' : 'none' }]">
					<template v-slot:node>
						<view v-show="item.show" :class="item.children.length > 0 ? 'point-has-children': 'point'"></view>
					</template>
					<template v-slot:content>
						<view
							class="content"
							:style="[
								{
									paddingLeft: item.rank * 15 + 'px',
									zIndex: item.rank * -1 + 50
								}
							]"
						>
							<view :class="item.phaseStatus == 'now' ? 'content_left blueCon' : item.phaseStatus == 'before' ? 'content_left' : 'content_left yellowCon'">
								<view class="" @click="_treeItemTap(item, index)">{{ item.name }}</view>
								<view class="">{{ item.phaseDate ? item.phaseDate : '' }}</view>
							</view>
							<view class="content_right">
								<view class="content_right_left">
									<u-tag text="详情" shape="circle" bg-color="#60E8E4" border-color="#60E8E4" color="#ffffff" @click="lineItem(item, index)" />
								</view>
							</view>
						</view>
					</template>
				</u-time-line-item>
			</u-time-line>
		</view>
	</view>
</template>
<script>
import { getAction } from '@/utils/request.js';
export default {
	data() {
		return {
			describe: '节点',
			list: [],
			runList: [],
			url: {
				list: '/visual/getModuleTree'
				// /weixin/fileark/dir.jsp
			},
			queryParam: {
				code: 'fileark_dir'
			},
			resultData: {},
			oldTreeList: [],
			idKey: 'code',
			rangeKey: 'name',
			num: 0
		};
	},
	onLoad(options) {
		let record = JSON.parse(options.record);
		this.search();
	},
	methods: {
		search() {
			this.runList = [];
			// this._renderTreeList(this.list);
			this.getDataList();
		},
		getDataList() {
			uni.showLoading({
				title: '加载中',
				mask: true
			});
			getAction(this.url.list, this.queryParam).then(res => {
				this.resultData = res.data;
				this.runList = [];
				this._renderTreeList(this.resultData.list || []);
				uni.hideLoading();
			});
		},
		//扁平化树结构
		_renderTreeList(list = [], rank = 0, parentId = [], parents = []) {
			list.forEach(item => {
				let obj = {
					id: item[this.idKey],
					name: item[this.rangeKey],
					source: item,
					parentId, // 父级id数组
					parents, // 父级id数组
					rank, // 层级
					showChild: false, //子级是否显示
					open: false, //是否打开
					show: rank === 0 || rank === 1, // 默认显示0及1级
					hideArr: [],
					orChecked: item.checked ? item.checked : false,
					checked: item.checked ? item.checked : false,
					notChecked: item.notChecked == undefined || item.notChecked == null || item.notChecked == true ? true : false, //是否展示选择框
					...item,
					childs: []
				};
				obj.childs = Array.isArray(item.children) && item.children.length > 0 ? this.getChilds(item.children, this.idKey) : [];
				this.runList.push(obj);
				if (Array.isArray(item.children) && item.children.length > 0) {
					let parentid = [...parentId],
						parentArr = [...parents],
						childrenid = [...childrenid];
					delete parentArr.children;
					parentid.push(item[this.idKey]);
					parentArr.push({
						[this.idKey]: item[this.idKey],
						[this.rangeKey]: item[this.rangeKey]
					});
					this._renderTreeList(item.children, rank + 1, parentid, parentArr);
				} else {
					this.runList[this.runList.length - 1].lastRank = true;
				}
			});
			this.oldTreeList = [...this.runList];
		},
		//获取所有子级key
		getChilds(data = [], key, ids = []) {
			data.forEach(item => {
				ids.push(item[key]);
				if (Array.isArray(item.children) && item.children.length > 0) {
					this.getChilds(item.children, key, ids);
				}
			});
			return ids;
		},
		// 点击
		_treeItemTap(record, index) {
			this.runList[index].showChild = !this.runList[index].showChild;
			let isShow = this.runList[index].showChild;
			this.runList.forEach(item => {
				item.show = record.childs.includes(item.code) ? isShow : item.show;
				item.showChild = record.childs.includes(item.code)?isShow:item.showChild
			});
			this.runList = [...this.runList];
		},
		lineItem(item, index) {
			console.log('item', item);
			let record = {};
			record.code = 'document';
			record.treeNodeCode = item.code;
			uni.navigateTo({
				// url: `/application/chilPage/rectificationNoticeDetails?record=${JSON.stringify(item)}`
				url: `/application/moduleList?record=${JSON.stringify(record)}`
			});
			// url: `${this.nativeTo}?record=${JSON.stringify({
			//   ...item,
			//   code: this.queryParam.moduleCode,
			// })}&listRecord=${JSON.stringify(
			//   this.listRecord
			// )}&otherRecord=${JSON.stringify(this.otherRecord)}`,
		}
	}
};
</script>
<style lang="scss" scoped>
.pageStyle {
	height: 100%;
	background-color: #fff;
	.time_line {
		padding: 20rpx 50rpx;
		// background-color: #f4f4f4;
		background-color: #fff;
	}
	.point {
		width: 16rpx;
		height: 16rpx;
		border-radius: 100%;
		background-color: #7dd6ff;
	}
	.point-has-children {
		width: 16rpx;
		height: 16rpx;
		border-radius: 100%;
		background-color: #0085c8;
	}
	.yellow {
		background-color: #e7be1a;
	}
	.yellowCon {
		color: #959595;
		font-size: 30rpx;
	}
	.blue {
		background-color: #1ebf82;
	}
	.blueCon {
		color: #323232;
		font-size: 28rpx;
	}
	.content {
		height: 100%;
		// margin-bottom: 30rpx;
		display: flex;
		align-items: center;
		opacity: 1;
		transition: all 0.2s ease-out;
		overflow: hidden;
		.content_left {
			width: 60%;
		}
		.content_right {
			width: 40%;
			display: flex;
			justify-content: flex-end;
			align-items: center;

			.content_right_left {
				margin-right: 20rpx;
			}
		}
	}
	// :class="{
	// 							show: item.show,
	// 							last: item.lastRank,
	// 							showchild: item.showChild,
	// 							open: item.open,
	// 						}"
	.content.show {
		height: 100%;
		margin-bottom: 30rpx;
		transition: all 0.2s ease-in;
		opacity: 1;
	}
	.content.showchild:before {
		transform: rotate(90deg);
	}
	.content.last:before {
		opacity: 0;
	}
}
</style>
