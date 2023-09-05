<template>
	<view class="page-pwd">
		<view class="cententPage">
			<view class="login-item heri-start-center" :class="{ is_red: isNotAllow }">
				<image src="./static/login/login_lock_red.png" class="login-icon" v-if="isNotAllow"></image>
				<image src="./static/login/login_lock.png" class="login-icon" v-else></image>
				<input type="text" class="login-content" placeholder="请输入密码" v-model="loginInfo.pwd" @focus="isNotAllow = false" />
			</view>
			<!-- type="password" -->
			<view class="login-item heri-start-center" :class="{ is_red: isNotAllow }">
				<image src="./static/login/login_lock_red.png" class="login-icon" v-if="isNotAllow"></image>
				<image src="./static/login/login_lock.png" class="login-icon" v-else></image>
				<input class="login-content" placeholder="请输入确认密码" v-model="loginInfo.confirmPwd" @blur="getConfirmPwd" />
			</view>
			<view class="login-btn" @click="handleOK">确定</view>
		</view>
	</view>
</template>

<script>
import { Api } from '@/utils/Api.js';
import { postAction } from '@/utils/request.js';
import { Toast } from '@/utils/commonHeader.js';
export default {
	data() {
		return {
			isNotAllow: false,
			loginInfo: {
				pwd: '',
				confirmPwd: ''
			},
			skey: ''
		};
	},
	onLoad(options) {
		this.skey = options.skey;
	},
	methods: {
		handleOK() {
			if (this.loginInfo.pwd != this.loginInfo.confirmPwd) {
				this.isNotAllow = true;
				Toast('none', '密码与确认密码不一致');
				return;
			}
			// postAction('/oa/user/updateInitPwd',this.loginInfo).then(res =>{
			// 	console.log(res)
			// })
			let baseUrl = Api.baseUrl;
			uni.request({
				header: { skey: this.skey, 'content-type': 'application/x-www-form-urlencoded' },
				url: `${baseUrl}/user/updateInitPwd`,
				data: this.loginInfo,
				method: 'post'
			})
				.then(res => {
					let result = res[1];
					console.log('result', result);
					if (result == null) {
						Toast('none', '网络连接失败');
					}
					if (result.statusCode && result.statusCode == '500') {
						Toast('none', '请求失败');
					} else {
						console.log('result', result);
						if (result.data.code == 200) {
							uni.reLaunch({
								url: '/pages/login/login'
							});
						} else {
							Toast('none', result.data.msg);
						}
					}
				})
				.catch(e => {
					console.log(e);
				});
			// uni.reLaunch({
			// 	url: '/pages/login/login'
			// });
		},
		getConfirmPwd(e) {
			console.log('e', e);
			const { value } = e.target;
			if (value != this.loginInfo.pwd) {
				this.isNotAllow = true;
				Toast('none', '两个密码不一致');
			} else {
				this.isNotAllow = false;
			}
		}
	}
};
</script>

<style lang="less" scoped>
.page-pwd {
	padding: 16px 20px;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: center;
	.cententPage {
		padding: 0 60upx;
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
}
</style>
