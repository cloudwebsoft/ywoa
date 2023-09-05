<template>
	<view class="page-pwd">
		<view class="cententPage">
			<view class="login-item heri-start-center">
				<uni-icons color="#bbb" size="20" type="person" />
				<input type="text" class="login-content" placeholder="请输入登录帐户" v-model="info.userName"
					 @blur="onBlur" />
			</view>
			<view class="login-item heri-start-center">
				<uni-icons color="#bbb" size="20" type="email" />
				<input type="text" class="login-content" placeholder="请输入邮箱地址" v-model="info.email" />
			</view>
			<view class="login-item heri-start-center">
				<uni-icons color="#bbb" size="20" type="info" />
				<input type="text" class="login-content" disabled="true" placeholder="输入帐户可显示邮箱地址" :value="note" />
			</view>
			<view class="login-btn" @click="handleOK">确定</view>
		</view>
	</view>
</template>

<script>
	import {
		Api
	} from '@/utils/Api.js';
	import {
		postAction
	} from '@/utils/request.js';
	import {
		Toast,
		getSkey
	} from '@/utils/commonHeader.js';
	export default {
		data() {
			return {
				info: {
					email: '',
					userName: ''
				},
				note:'',
				skey: ''
			};
		},
		onLoad(options) {
			this.skey = getSkey();
		},
		methods: {
			onBlur() {
				let baseUrl = Api.baseUrl;
				uni.request({
						header: {
							skey: this.skey,
							'content-type': 'application/x-www-form-urlencoded'
						},
						url: `${baseUrl}/public/getUserEmailObscured`,
						data: this.info,
						method: 'post'
					}).then(res => {
						let result = res[1];
						console.log('result', result);
						if (result == null) {
							Toast('none', '网络连接失败');
						}
						if (result.statusCode && result.statusCode == '500') {
							Toast('none', '请求失败');
						} else {
							console.log('result', result);
							if (result.data.ret == 1) {
								this.note = result.data.email
							} else {
								Toast('none', result.data.msg);
							}
						}
					})
					.catch(e => {
						console.log(e);
					});
			},
			handleOK() {
				let baseUrl = Api.baseUrl;
				uni.request({
						header: {
							skey: this.skey,
							'content-type': 'application/x-www-form-urlencoded'
						},
						url: `${baseUrl}/public/resetPwdSendLink`,
						data: this.info,
						method: 'post'
					}).then(res => {
						let result = res[1];
						console.log('result', result);
						if (result == null) {
							Toast('none', '网络连接失败');
						}
						if (result.statusCode && result.statusCode == '500') {
							Toast('none', '请求失败');
						} else {
							console.log('result', result);
							if (result.data.res == 0) {
								Toast('none', '操作成功，请进入邮箱点击重置密码链接');
							} else {
								Toast('none', result.data.msg);
							}
						}
					})
					.catch(e => {
						console.log(e);
					});
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
