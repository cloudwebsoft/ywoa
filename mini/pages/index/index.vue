<template>
	<view class="container">
		<!-- <u-button class="" style="width: 100%;" @click="goClick">
			测试按钮
		</u-button> -->
		<view class="bodyStyle"><News ref="newsRef" /></view>
	</view>
</template>

<script>
// import { mapState } from "vuex";
import News from './chilPage/news';
export default {
	components: { News },
	data() {
		return {
			isDoRefresh: false
		};
	},
	onLoad() {
		console.log('首页onload');
		this.search();
	},
	onShow() {
		console.log('首页秀');
		const that = this;
		// let pages = getCurrentPages();
		//刷新页面
		// let currPage = pages[pages.length - 1];
		if (that.isDoRefresh == false) {
			that.isDoRefresh = true;
		}else{
			that.search();
		}
	},
	//下拉刷新
	onPullDownRefresh() {
		console.log('下拉');
		// #ifdef H5
		uni.startPullDownRefresh();
		this.search();
		uni.stopPullDownRefresh();

		// #endif
		// #ifdef MP-WEIXIN
		this.search();
		wx.stopPullDownRefresh();
		// #endif

		// #ifdef APP-PLUS
		uni.startPullDownRefresh();
		this.search();
		uni.stopPullDownRefresh();
		// #endif
	},
	//上滑动刷新
	onReachBottom() {
		console.log('上滑首页');
		this.getOnReachBottom();
	},
	// computed: {
	//   ...mapState([
	//     "userInfos",
	//     "companyConfig",
	//     "iStatusBarHeight",
	//     "allResult",
	//   ]),
	// },
	methods: {
		search() {
			setTimeout(() => {
				this.$refs.newsRef.searchList();
			});
		},
		//上滑
		getOnReachBottom() {
			this.$refs.newsRef.getOnReachBottom();
		},
		goClick(){
			//跳转到修改密码页
			uni.reLaunch({
				url: '/cmn/changePassword'
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.container {
	height: 100%;
	// background-color: #f4f4f4;

	.bodyStyle {
		height: 100%;
		// #ifdef APP-PLUS
		padding-bottom: 140rpx;
		// #endif
	}
}
</style>
