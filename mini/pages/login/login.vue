<template>
	<view class="mainPage">
		<view class="mainPage_top" :style="{ background: 'url(' + backImg + ')' }">
			<view class="setting"><u-icon name="setting" color="#2979ff" size="40" @click="setBaseUrl"></u-icon></view>
			<view class="mainPage_top_center">
				<view class="mainPage_top_center_img"><image src="../../static/login/log.png"></image></view>
				<view class="mainPage_top_center_title">欢迎登录</view>
			</view>
		</view>
		<view class="cententPage">
			<view class="login-item heri-start-center" :class="{ is_red: isNotAllow }">
				<image src="../../static/login/login_user_red.png" class="login-icon" v-if="isNotAllow"></image>
				<image src="../../static/login/login_user.png" class="login-icon" v-else></image>
				<input type="text" class="login-content" placeholder="请输入用户名" v-model="loginInfo.name" @focus="isNotAllow = false" />
			</view>
			<view class="login-item heri-start-center" :class="{ is_red: isNotAllow }">
				<image src="../../static/login/login_lock_red.png" class="login-icon" v-if="isNotAllow"></image>
				<image src="../../static/login/login_lock.png" class="login-icon" v-else></image>
				<input type="password" class="login-content" placeholder="请输入密码" v-model="loginInfo.password" @focus="isNotAllow = false" />
			</view>
			<view class="login-btn" @click="login">登录</view>
			<view class="forget heri-between">
				<text @click="forgetPwd">忘记密码</text>
				<text @click="scanCode">扫码</text>
				<!-- <text @click="setBaseUrl">设置</text> -->
			</view>
		</view>
	</view>
</template>

<script>
import { postAction, getAction, getResultAllAction } from '@/utils/request.js';
import { Toast, reCheck, cache, getOpenId } from '@/utils/commonHeader.js';
import { Api, setApiBaseUrl } from "@/utils/Api.js";
export default {
	data() {
		return {
			loginInfo: {
				name: '',
				password: '',
				// client: "IOS",
				deviceId: '', //设备id
				openId: ''
			},
			url: {
				login: '/mobile/login'
			},
			isNotAllow: false,
			backImg: '../../static/login/login_bg.png'
		};
	},
	onLoad() {
		uni.setNavigationBarColor({
			frontColor: '#000000', // 必写项
			backgroundColor: '#E3EEFF', // 必写项
			animation: {
				// 可选项
				duration: 400,
				timingFunc: 'easeIn'
			}
		});
	},
	created() {},
	methods: {
		login() {
			this.isNotAllow = false;
			console.log('getOpenId()', getOpenId());
			/*校验用户名和密码*/
			if (!this.loginInfo.name) {
				Toast('none', '请输入用户名');
				return;
			}
			if (!this.loginInfo.password) {
				Toast('none', '请输入密码');
				return;
			}

			console.log('this.url.login', this.url.login);
			
			// #ifdef MP-WEIXIN
			let openId = getOpenId();
			if (openId) this.loginInfo.openId = openId;
			// #endif
			
			getResultAllAction(this.url.login, this.loginInfo).then(res => {
				console.log(res.data);
				if (res.data.res == 0) {
					console.log('res登录', res);
					//登录成功
					let result = res.data.result;

					uni.setStorage({
						key: 'wxMiniLoginMode',
						data: res.data.wxMiniLoginMode
					});
					uni.setStorage({
						key: 'loginMode',
						data: res.data.loginMode
					});

					let token = '';
					//1.缓存token
					if (res.header) {
						token = res.header.authorization ? res.header.authorization : res.header.Authorization ? res.header.Authorization : '';
					}

					// #ifdef MP-WEIXIN ||  APP-PLUS
					// token = res.header.Authorization ? res.header.Authorization : "";
					// #endif

					// #ifdef H5
					// token = res.header.authorization ? res.header.authorization : "";
					// #endif
					//2.缓存用户信息 loginInfo
					let loginInfo = result ? result : {};
					//缓存登录头部
					let header = res.header ? res.header : {};
					console.log('login header', header);
					uni.setStorage({
						key: 'loginInfo',
						data: {
							token: token,
							loginInfo: loginInfo,
							header: header
						},
						success() {
							// 存储参数
							reCheck();
							// cache("times", 1);
							uni.setStorage({
								key: 'curRoleCode',
								data: header.curRoleCode,
								success() {
									// 是否可切换角色
									if (result.isRoleSwitchable) {
										//跳转
										uni.redirectTo({
											url: `${'/pages/login/selectRole'}?roleList=${JSON.stringify(result.roleList)}`
										});
									} else {
										//跳转
										uni.reLaunch({
											url: '../index/index'
										});
									}
								}
							});
						}
					});
					Toast('none', res.data.msg);
				} else {
					this.isNotAllow = true;
					// result.isPwdCanReset
					if (res.data.isForceChangePwd) {
						//跳转到修改密码页
						uni.reLaunch({
							url: `/cmn/changePassword?skey=${res.header.skey}`
						});
						return;
					} else {
						Toast('none', res.data.msg);
					}
				}
			});
		},
		//扫码
		scanCode() {
			uni.scanCode({
				success: res => {
					console.log('scanCode', res);
					let result = res.result;
					console.log('scanCode result', result);
					if (result == null) {
						uni.showToast({
							icon: 'none',
							title: '扫码失败'
						});
						return;
					}

					var index = result.indexOf('public');
					if (index == -1) {
						uni.showToast({
							icon: 'none',
							mask: false,
							title: '服务器地址错误，请重新设置'
						});
					} else {
						var appBaseUrl = result.substring(0, index-1);
						console.log('appBaseUrl: ' + appBaseUrl);
						uni.showLoading({
							title: '检测地址中...',
							mask: true
						});
						setApiBaseUrl(appBaseUrl);
						// 检测URL连接
						getResultAllAction(appBaseUrl, {})
							.then(res => {
								console.log('res', res);
								if (res.statusCode == 200) {
									uni.setStorage({
										key: 'appBaseUrl',
										data: appBaseUrl
									});
									Toast('none', '登录地址配置成功');
								} else {
									Toast('none', '登录地址检测失败');
								}
							})
							.finally(() => {
								uni.hideLoading();
							});
					}
				}
			});
		},
		setBaseUrl() {
			uni.navigateTo({
				url: '/pages/login/setAppBaseUrl'
			});
		},
		forgetPwd() {
			uni.navigateTo({
				url: '/cmn/forgetPassword'
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.mainPage {
	// padding: 80upx 50upx 0;
	// min-height: auto;
	height: 100vh;
	box-sizing: border-box;
	background: linear-gradient(180deg, #e3eeff 0%, #f3f8ff 100%);
	// position: relative;
	// background-color: #1c6ec4;
	// display: flex;
	// align-items: center;
	// justify-content: center;
	// background: url(../../static/login/background.png) no-repeat;
	// background-size: cover;

	.mainPage_top {
		height: 813upx;
		width: 100%;
		position: relative;
		// background: url(../../static/login/login_bg.png);
		background-size: 100% 100%;
		display: flex;
		justify-content: center;
		align-items: center;
		.setting {
			position: absolute;
			right: 20px;
			top: 50px;
		}
		.mainPage_top_center {
			text-align: center;

			.mainPage_top_center_img {
				width: 130upx;
				height: 130upx;
				margin: 0 auto;

				image {
					width: 100%;
					height: 100%;
				}
			}

			.mainPage_top_center_title {
				margin-top: 13.5upx;
				font-size: 44upx;
				font-weight: 600;
				color: #2b2c2f;
			}
		}
	}

	/deep/ .input-placeholder {
		color: #abafba;
	}

	/deep/ .uni-input-input {
		color: #2b2c2f;
	}

	.cententPage {
		margin-top: -110upx;
		padding: 0 60upx;
	}

	.logo {
		margin-bottom: 30upx;

		.logoImg {
			width: 160upx;
			height: 160upx;
		}
	}

	.login-title {
		width: 100%;
		font-size: 36upx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #ffffff;
		margin-bottom: 123upx;
	}

	.login-item {
		width: 100%;
		margin-bottom: 54upx;
		border-bottom: 1px solid #cbced8;

		.login-icon {
			width: 30upx;
			height: 30upx;
		}

		.CheckCode {
			width: 150upx;
			height: 70upx;
		}

		.login-content {
			height: 90upx;
			margin-left: 20upx;
			font-size: 28upx;
			flex: 1;
		}

		&:nth-child(3) {
			margin-bottom: 60upx;
		}
	}

	.is_red {
		border-bottom: 1px solid red;
	}

	.login-btn {
		width: 100%;
		height: 80upx;
		line-height: 80upx;
		text-align: center;
		font-size: 30upx;
		font-family: PingFang SC;
		font-weight: bold;
		color: #ffffff;
		background: #4b7aff;
		border-radius: 40upx;
		margin-top: 90upx;
		opacity: 0.9;
		z-index: 1000;
	}

	.forget {
		height: 80upx;
		font-size: 24upx;
		font-family: PingFang SC;
		font-weight: 500;
		color: #aaa;
		line-height: 40upx;
		opacity: 0.5;
	}
}
</style>
