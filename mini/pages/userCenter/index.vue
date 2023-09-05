<template>
	<view class="userCenter">
		<view class="top_bg"> </view>
		<view class="userHeader">
			<view class="headSculpture">
				<view class="leftHead">
					<view class="headImg">
						<image :src="imgShow" class="icons"></image>
					</view>
					<view class="name">
						<view class="name_top">
							<view>
								{{ uesrInfo.real_name }}
							</view>
							<view>
								<u-tag :text="uesrInfo.userType" shape="circle" border-color="#48D4D0"
									bg-color="#48D4D0" color="#fff" v-if="uesrInfo.userType"></u-tag>
							</view>
						</view>
						<view>{{ uesrInfo.phone }}</view>
					</view>
				</view>
			</view>
		</view>
		<view class="user_sections">
			<view class="user_infos_items" @click="showMyInfo()">
				<view class="left_items">
				  <image class="left_img" src="../../static/usercenter/agreement.png" mode=""></image>
				  <text>个人信息</text>
				</view>
				<view>
				  <text class="arrow_title">详情</text>
				  <image class="arrow" src="../../static/usercenter/xiangyou.png" mode=""></image>
				</view>
			</view>
			<view class="user_infos_items" @click="changPwd()">
			  <view class="left_items">
			    <image class="left_img" src="../../static/usercenter/certificate.png" mode=""></image>
			    <text>修改密码</text>
			  </view>
			</view>
			<!-- <view class="user_infos_items">
				<view class="left_items">
					<image class="left_img" src="../../static/usercenter/version.png" mode=""></image>
					<text>版本号</text>
				</view>
				<view>
					<text>v1.0.0</text>
				</view>
			</view> -->
			<view class="user_infos_items" @click="scanCode" v-if="loginMode == 1 || loginMode == 2">
				<view class="left_items">
					<image class="left_img" src="../../static/login/login_lock.png" mode=""></image>
					<text>扫码登录</text>
				</view>
				<view>
					<text></text>
				</view>
			</view>
			<view class="user_infos_items" @click="outLogin" v-if="wxMiniLoginMode == 0">
				<view class="left_items">
					<image class="left_img" src="../../static/usercenter/setting.png" mode=""></image>
					<text>退出</text>
				</view>
				<view> </view>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		mapState
	} from "vuex";
	import {
		Api
	} from "@/utils/Api.js";
	import {
		postAction,
		getAction,
		postFormAction
	} from "@/utils/request.js";
	import {
		getDeptCode,
		cache,
		reCheck
	} from "@/utils/commonHeader.js";
	export default {
		data() {
			return {
				userUrl: "https://cdn.uviewui.com/uview/swiper/2.jpg", // require('../../static/usercenter/rightArrow.png')
				url: {
					list: "/public/android/i/info",
				},
				uesrInfo: {},
				blockList: [], //小区集合
				xiaoqu: "",
				isReadShow: false,
				wxMiniLoginMode: 0,
				// loginMode PC端登录模式 0|帐户登录,1|扫码登录,2|帐户/扫码登录
				// 当为1或2时可以扫码登录
				loginMode: 1,
				// imgShow: "",
			};
		},
		computed: {
			...mapState(["companyConfig", "loginInfo", "iStatusBarHeight"]),
			 imgShow() {
			   return Api.baseUrl + "/" + this.loginInfo.photo;
			 },
		},
		onLoad() {
			// #ifdef MP-WEIXIN
			// console.log("wx.getAccountInfoSync()", wx.getAccountInfoSync());
			// console.log("wx.getSystemInfoSync()", wx.getSystemInfoSync());
			// #endif

			// #ifdef APP-PLUS || H5
			// #endif
			this.search();
			this.wxMiniLoginMode = uni.getStorageSync("wxMiniLoginMode");
			this.loginMode = uni.getStorageSync("loginMode");
			this.uesrInfo = this.loginInfo;
			// this.imgShow = Api.baseUrl + "/" + this.loginInfo.photo;
			// console.log("this.imgShow", this.imgShow);
			console.log("loginMode", this.loginMode);
			console.log("loginInfo", this.loginInfo);
		},
		onShow() {
			if (!this.isReadShow) {
				this.isReadShow = true;
			} else {
				this.search();
			}
		},
		methods: {
			search() {
				getAction(this.url.list).then((res) => {
					if (res.res == 0) {
						this.uesrInfo = res.result;
						this.blockList =
							res.result.blockList && res.result.blockList.length > 0 ?
							res.result.blockList :
							[];
						if (this.blockList.length > 0) {
							//小区
							let deptCode = getDeptCode();
							this.blockList.forEach((item) => {
								if (item.id == deptCode) {
									this.xiaoqu = item.name;
								}
							});
						}
					} else {
						// uni.showToast({
						//   title: "",
						//   duration: 2000,
						// });
					}
				});
			},
			confirmSelect(e) {
				if (e && Array.isArray(e) && e.length > 0 && e[0].label) {
					this.xiaoqu = e[0].label;
					let info = uni.getStorageSync("loginInfo");
					info.header.curDeptCode = e[0].value;
					uni.setStorage({
						key: "loginInfo",
						data: {
							...info,
						},
					});

					uni.showToast({
						title: `已切换${e[0].label}`,
						icon: "none",
						duration: 1000,
					});
				}
			},
			//跳转页面
			linkPage(url) {
				uni.navigateTo({
					url: url,
				});
			},
			showMyInfo() {
				this.linkPage('/pages/userCenter/myInfo');

			},
			changPwd() {
				this.linkPage('/pages/userCenter/changeMyPassword');
			},
			//扫码
			scanCode() {
				uni.scanCode({
					success: (res) => {
						console.log("scanCode", res);
						let result = JSON.parse(res.result);
						if (result == null) {
							uni.showToast({
								icon: "none",
								title: "扫码失败",
							});
							return;
						}
						console.log("scanCode-result", result);
						let params = {
							skey: this.loginInfo.skey,
						};

						postFormAction("/" + result.path, params)
							.then((res) => {
								let result = res;
								console.log("result", result);
								if (result == null) {
									uni.showToast({
										icon: "none",
										title: "网络连接失败",
									});
									return;
								}
								if (result.statusCode && result.statusCode == "500") {
									uni.showToast({
										icon: "none",
										title: "登录失败",
									});
								} else {
									console.log("登录成功", result);
									if (result.res == 0) {
										uni.showToast({
											icon: "none",
											title: "扫码登录成功",
										});
									}
								}
							})
							.catch((e) => {
								console.log("登录失败", e);
								uni.showToast({
									icon: "none",
									title: "登录失败",
								});
							});
					},
				});
			},
			outLogin() {
				try {
					let appBaseUrl = uni.getStorageSync("appBaseUrl");
					console.log('appBaseUrl', appBaseUrl);
					uni.clearStorageSync();
					// 退出清空了缓存，故此处需保存appBaseUrl
					if (appBaseUrl) {
						uni.setStorageSync('appBaseUrl', appBaseUrl);
					}
					
					uni.reLaunch({
						url: "/pages/login/login",
					});
					// ni.navigateTo({
					//   url: "/pages/login/login",
					// });u
				} catch (e) {
					// error
					console.log(e);
				}
			},
		},
	};
</script>

<style lang="scss" scoped>
	.userCenter {
		height: 100%;
		background-color: #f4f4f4;

		.top_bg {
			width: 100%;
			height: 120rpx;
			background-color: #1c6ec4;
			border-bottom-left-radius: 30rpx;
			border-bottom-right-radius: 30rpx;
		}

		.userHeader {
			// position: absolute;
			margin: 0 auto;
			width: 96%;
			height: 240rpx;
			margin-top: -120rpx;
			background-color: #ffffff;
			border-radius: 20rpx;
			display: flex;
			align-items: center;

			.headSculpture {
				padding: 0 40upx;
				width: 100%;
				height: 136upx;
				display: flex;
				align-items: center;
				justify-content: space-between;

				.leftHead {
					width: 100%;
					display: flex;
					align-items: center;

					.headImg {
						width: 136rpx;
						height: 136rpx;
						border: 1px solid #f3f5f8;
						border-radius: 50%;
						margin-right: 40upx;
						display: flex;
						align-items: center;
						justify-content: center;

						.icons {
							width: 120upx;
							height: 120upx;
							border-radius: 50%;
						}
					}

					.name {
						width: 100%;
						color: #000;
						font-weight: 600;
						font-size: 16px;

						.name_top {
							width: 100%;
							margin-bottom: 20rpx;
							display: flex;
							justify-content: space-between;
						}
					}
				}
			}
		}

		.user_sections {
			width: 96%;
			margin: 0 auto;
			margin-top: 20rpx;
			padding: 0 40upx;
			background: #ffffff;

			.user_infos_items {
				height: 98upx;
				border-bottom: 1upx solid #f0f0f0;
				display: flex;
				align-items: center;
				justify-content: space-between;
				font-size: 16px;

				.left_items {
					// text-align: center;
					display: flex;
					align-items: center;

					.left_img {
						width: 24rpx;
						height: 28rpx;
						margin-right: 20rpx;
					}
				}

				.arrow_title {
					margin-right: 20rpx;
					color: #4ea779;
				}
			}

			.arrow {
				width: 14upx;
				height: 24upx;
			}
		}
	}
</style>
