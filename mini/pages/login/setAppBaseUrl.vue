<template>
	<view class="pageStyle">
		<view class="content">
			<view class="label heri-start-center">后台地址:</view>
			<view class="heri-start-center" style="width:100%"><input type="text"  placeholder="请输入地址" v-model="appBaseUrl" /></view>
		</view>
		<view>
			<u-button
				type="primary"
				:hover-class="'buttons'"
				:custom-style="{ width: '100%', backgroundColor: '#31E6E1', padding: '20rpx', border: 'none', color: '#fff' }"
				@click="save"
			>
				确定
			</u-button>
		</view>
	</view>
</template>
<script>
import { getResultAllAction, setRequestBaseUrl } from '@/utils/request.js';
import { Toast } from '@/utils/commonHeader.js';
import { setApiBaseUrl } from "@/utils/Api.js";
export default {
	data() {
		return {
			appBaseUrl: ''
		};
	},
	onLoad(options) {
		this.appBaseUrl = uni.getStorageSync('appBaseUrl');
		console.log('appBaseUrl', this.appBaseUrl);
	},
	methods: {
		save() {
			let self = this;
			uni.setStorage({
				key: 'appBaseUrl',
				data: self.appBaseUrl
			});
			setApiBaseUrl(self.appBaseUrl);
			setRequestBaseUrl(self.appBaseUrl);
			console.log('save appBaseUrl', self.appBaseUrl);
			
			uni.showLoading({
				title: '检测地址中...',
				mask: true
			});
			// 检测URL连接
			getResultAllAction(this.appBaseUrl, {})
				.then(res => {
					console.log('res', res);
					if (res.statusCode == 200) {
						Toast('none', '登录地址配置成功');
						uni.navigateBack({
							delta: 1
						});
					} else {
						Toast('none', '登录地址检测失败');
					}
				})
				.finally(() => {
					uni.hideLoading();
				});
		}
	}
};
</script>
<style lang="scss" scoped>
.pageStyle {
	width: 100%;
	height: 100%;
	padding: 0 20px;
	.content {
		display: flex;
		margin-top: 36%;
		margin-bottom: 10px;
	}
	.label {
		height: 120rpx;
		width: 100px;
	}
	uni-input{
		height: 30px;
		width: 100%;
	}
	::v-deep .uni-input-input{
		border-bottom: 1px solid #cbced8;
	}
	::v-deep .uni-input-placeholder{
		font-size: 14px;
	}
}
</style>
