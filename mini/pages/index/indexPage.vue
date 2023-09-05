<template>
	<view class="page">
	-
	</view>
</template>
<script>
	import { Toast, reCheck } from '@/utils/commonHeader.js';
	export default {
		data() {
			return {};
		},
		created() {
			// 当App.vue中onShow时，reLaunch中跳转了页面，但因执行是异步的，结果仍会是默认首页
			// let info = uni.getStorageSync("loginInfo");
			// console.log("created loginInfo", JSON.stringify(info));
			// // Toast("none", "created loginInfo=" + JSON.stringify(info));
			// //用户信息存在时候跳转到首页
			// if (info) {
			// 	// 跳到首页, tabbar界面, 必须使用switchTab
			// 	uni.reLaunch({
			// 		url: "./pages/index/index"
			// 	})
			// } else {
			// 	uni.reLaunch({
			// 		url: "./pages/login/login"
			// 	});
			// }
		},
		onShow() {
			// 当App.vue中onShow时，reLaunch中跳转了页面，但因执行是异步的，结果仍会是默认首页
			let info = uni.getStorageSync("loginInfo");
			console.log("onShow loginInfo", info);
			// Toast("none", "onShow loginInfo=" + JSON.stringify(info));
			//用户信息存在时候跳转到首页
			if (info) {
				// uni.reLaunch({
				// 	url: "./pages/index/index"
				// });
				console.log('redirectTo index.');
				uni.reLaunch({
					url: "/pages/index/index"
				});
			} else {
				uni.reLaunch({
					url: "/pages/login/login"
				});
			}
		},
		onLoad() {
			// Toast("none", "onLoad");
			
			uni.setNavigationBarColor({
				frontColor: '#000000', // 必写项
				backgroundColor: '#E3EEFF', // 必写项
				animation: { // 可选项
					duration: 400,
					timingFunc: 'easeIn'
				}
			})
		},
		methods: {},
	};
</script>
