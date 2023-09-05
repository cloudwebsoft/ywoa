<template>
	<view class="pageStyle">
		<view class="bodyStyle">
			<view class="content_box">
				<view class="content_box_center flex">
					<view class="content_box_center_text" v-for="(item,index) in list" in :key="index"
						:style="[{marginRight:(index+1)%4==0?'0':'1.5%',marginBottom:'1.5%',height: 'calc(94.5% / 4)'}]"
						@click="todoItem(item,index)">
						<u-badge :count="nums.noticeNum" bgColor="#f00" :offset="[-10,-10]" class="badge"
							v-if="item.mId=='27'"></u-badge>
						<u-badge :count="nums.waitNum" bgColor="#f00" :offset="[-10,-10]" class="badge"
							v-if="item.mId=='25'"></u-badge>
						<u-badge :count="nums.messageNum" bgColor="#f00" :offset="[-10,-10]" class="badge"
							v-if="item.mId=='16'"></u-badge>
						<view class="con_block">
							<view class="con_img">
								<image :src="item.imgUrls" class="img"></image>
							</view>
							<view class="con_title">
								{{item.mName}}
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>
<script>
	import {
		postAction,
		getAction
	} from "@/utils/request";
	import {
		Api
	} from "@/utils/Api.js";
	import {
		setUserInfo,
		Toast,
		getSkey
	} from "@/utils/commonHeader.js";
	import {
		mapState
	} from "vuex";
	export default {
		data() {
			return {
				customStyle: {
					margin: "0rpx",
				},
				url: {
					list: "/mobile/getAppIcons",
				},
				list: [],
				nums: {
					noticeNum: 0, //告知单数量
					waitNum: 0, //待办流程数量
					messageNum: 0, //消息数量
				},
			};
		},
		onLoad() {
			this.getUserInfo();
		},
		onShow() {
			this.getList();
			this.getNoticeNum();
		},
		methods: {
			getList() {
				getAction(this.url.list).then((res) => {
					if (res.res == 0) {
						this.list = res.result.datas;
						if (this.list && this.list.length > 0) {
							this.list.map((item) => {
								item.imgUrls = item.imgUrl ? Api.baseUrl + "/" + item.imgUrl : "";
								// if (item.code == "usercardrecord") {
								//   this.getNoticeNum(item.mId);
								// }
							});
						} else {
							this.list = [];
						}
					}
				});
			},
			//获得消息数
			getNoticeNum(mId) {
				// getAction("/i/notice/getNum").then((res) => {
				//   if (res.res == 0) {
				//     console.log("1212", res);
				//     this.nums = Object.assign(this.nums, res);
				//     console.log("nums", this.nums);
				//   }
				// });
			},
			todoItem(item, index) {
				let mId = item.mId;
				let code = item.code;
				let pageUrl = "";
				if (code == 'flow_wait') {
					// 待办流程
					pageUrl = "/application/waitProcessListH5";
				} else if (code == 'myflow') {
					// 我的流程
					pageUrl = "/application/hadProcessListH5";
				} else if (code == 'flow_launch') {
					// 发起流程
					pageUrl = "/application/initProcessListH5";
				} else if (mId == 16) {
					// 系统消息
					pageUrl = "/application/sysMessage";
				} else if (mId == 21) {
					pageUrl = "/application/noticeList";
				} else if (code == 'fileark') {
					pageUrl = "/application/filearkDir";
				} else if (mId == 13) {
					pageUrl = "/application/calendar";
				} else if (mId == 14) {
					pageUrl = "/application/calendarShared";
				} else if (mId == 18) {
					pageUrl = "/application/punch";
				} else if (mId == 23) {
					pageUrl = "/application/address";
				} else {
					if (item.type === 3) {
						pageUrl = "/application/moduleList";
					} else {
						if (item.code != '/scan') {
							uni.showToast({
								title: "开发中",
								icon: "none",
								duration: 1000,
							});
						}
					}
				}

				if (item.code == '/scan') {
					this.scanQrcode();
				} else {
					uni.navigateTo({
						url: `${pageUrl}?record=${JSON.stringify(item)}`,
					});
				}
			},
			scanQrcode() {
				let r = false;
				uni.scanCode({
					success: (res) => {
						console.log("scan1 res", res);
						r = this.doScan(res);
						if (!r) {
							Toast('none', '扫码失败，尝试第2次');
							uni.scanCode({
								success: (res) => {
									console.log("scan2 res", res);
									r = this.doScan(res);
									if (!r) {
										Toast('none', '扫码失败，尝试第3次');
										uni.scanCode({
											success: (res) => {
												console.log("scan3 res", res);
												r = this.doScan(res);
												if (!r) {
													Toast('none', '扫码失败，请重新扫码');
												}
											},
										});
									}
								},
							});
						}
					},
				});
			},
			async doScan(res) {
				let resultRaw = res.result;
				console.log('resultRaw', resultRaw);
				let result = null;
				try {
					result = JSON.parse(resultRaw);
				} catch (e) {
					console.log(e);
				}
				console.log('result', result);
				if (result == null) {
					console.log('扫码结果: ' + resultRaw);
					let resBar = await postAction("/public/android/getBarcode", {barcode: resultRaw}, 'post', {
							skey: getSkey(),
							'content-type': 'application/x-www-form-urlencoded'
						});
					console.log('resBar', resBar);
					if (resBar.code == 200) {
						result = resBar.data;
					} else {
						Toast('none', resBar.msg);
						return false;
					}
				}
				console.log("scanCode-result", result);
				if (result.scanActionType) {
					if (result.scanActionType == 'flow') {
						result.scanId = result.id;
						let pageUrl = '/application/process';
						uni.navigateTo({
							url: `${pageUrl}?record=${JSON.stringify(result)}`,
						});
						return true;
					} else if (result.scanActionType == 'create') {
						result.scanId = result.id;
						delete result['id'];
						console.log('create result', result);
						let pageUrl = '/application/moduleAddEdit';
						uni.navigateTo({
							url: `${pageUrl}?record=${JSON.stringify(result)}`,
						});
						return true;
					}
				}

				// 如果存在url
				if (result.url) {
					let pageUrl = result.url;
					uni.navigateTo({
						url: `${pageUrl}?record=${JSON.stringify(result)}`,
					});
				} else {
					if (!result.id) {
						console.log('扫码识别失败: ' + res.result);
						return false;
					} else {
						let pageUrl = '/application/moduleShow';
						uni.navigateTo({
							url: `${pageUrl}?record=${JSON.stringify(result)}`,
						});
					}
				}
				return true;
			},
			getUserInfo() {
				getAction("/public/android/i/info").then((res) => {
					if (res.res == 0) {
						let uesrInfo = res.result;
						uni.setStorage({
							key: "uesrInfo",
							data: uesrInfo,
							success() {
								//存储个人信息
								setUserInfo();
							},
						});
					}
				});
			},
		},
		computed: {
			...mapState(["loginInfo"]),
		},
	};
</script>
<style lang='scss' scoped>
	.pageStyle {
		height: 100%;
		background-color: #1c6ec4;

		image {
			will-change: transform;
		}

		.bodyStyle {
			height: 100%;
			background-color: #f4f4f4;
			border-top-left-radius: 35rpx;
			border-top-right-radius: 35rpx;
			padding: 94rpx 32rpx 0;

			.content_box {
				position: relative;
				width: 100%;
				height: 0;
				padding-bottom: 100%;
				/* padding百分比是相对父元素宽度计算的 */
				//   margin-bottom: 30px;
			}

			.content_box_center {
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				/* 铺满父元素容器，这时候宽高就始终相等了 */
			}

			.content_box_center>.content_box_center_text {
				width: calc(94.5% / 4);
				/* calc里面的运算符两边要空格 */
				box-shadow: #1c6ec4 0px 0px 20rpx;
				border-radius: 10rpx;
				position: relative;
				display: flex;
				align-items: center;
				justify-content: center;
			}

			.flex {
				display: flex;
				flex-wrap: wrap;
				align-content: flex-start;
			}

			.flex>.content_box_center_text {
				//   flex-grow: 1; /* 子元素按1/n的比例进行拉伸 */
				background-color: #ffffff;
				text-align: center;
				color: #999;
				font-size: 50px;
				// line-height: 1.5;
			}

			.badge {
				z-index: 1000;
			}

			.con_block {
				.con_img {
					position: relative;
					width: 100%;
					height: 42rpx;
					margin-bottom: 20rpx;

					.img {
						position: absolute;
						top: 0;
						left: 50%;
						transform: translateX(-50%);
						width: 42rpx;
						height: 42rpx;
					}
				}

				.con_title {
					font-size: 29rpx;
				}
			}
		}
	}
</style>
