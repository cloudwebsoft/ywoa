<template>
	<view class="pageStyle">
		<web-view :src="urls"></web-view>
	</view>
</template>
<script>
	import {
		Api
	} from "@/utils/Api.js";
	import {
		mapState
	} from "vuex";
	export default {
		data() {
			return {
				describe: "系统消息",
				model: {},
				url: {
					list: "/weixin/notice/notice_list.jsp",
				},
				urls: "",
			};
		},
		onReady() {
			// 双指缩放
			// #ifdef APP-PLUS
			// this为undefined
			// var wv = this.$scope.$getAppWebview();
			let pages = getCurrentPages();
			console.log('pages', pages);
			var page = pages[pages.length - 1];
			var wv = page.$getAppWebview();
			setTimeout(function() {
				wv.setStyle({
					scalable: true
				})
			}, 1000); //如果是页面初始化调用时，需要延时一下
			// #endif
		},
		onShow() {},
		// 自定义返回键事件
		// 只有在该函数中返回值为 true 时，才表示不执行默认的返回，自行处理此时的业务逻辑
		/* onBackPress(options) {
			console.log('options', options);
			if (options.from === 'navigateBack') {
				return false;
			}
			// #ifdef APP-PLUS
			// this为undefined
			// var wv = this.$scope.$getAppWebview();
			let pages = getCurrentPages();
			console.log('pages', pages);
			var page = pages[pages.length - 1];
			var wv = page.$getAppWebview();
			if (wv) {
				wv.canBack(e => { // 查询Webview窗口是否可后退 返回true即返回H5上一级
					if (e.canBack) {
						wv.back();
					} else { //返回 false 即到 app加载H5的第一页，直接返回app，即H5页面回到app页面中
						uni.navigateBack({
							delta: 1
						})
					}
				})
			}
			// 返回true 表示不执行返回键默认操作
			return true;
			// #endif
		}, */
		onLoad(options) {
			this.model = JSON.parse(options.record);

			//动态获取webview链接
			console.log('onLoad this.loginInfo', this.loginInfo);
			this.urls = Api.baseUrl + this.url.list + "?skey=" + this.loginInfo.skey + "&isUniWebview=true";

			// 取得当前的webview，监听标题的变化
			// let pages = getCurrentPages();
			// var page = pages[pages.length - 1];
			// var currentWebview = page.$getAppWebview();
			// // var currentWebview = this.$scope.$getAppWebview();
			// currentWebview.setTitleNViewButtonStyle(0, {
			// 	text: this.urls.indexOf('list') == -1 ? '' : '+'
			// });
			// currentWebview.addEventListener('titleUpdate', function(e) {
			// 	if (e && e.title) {
			// 		console.log('-------------->Webview titleUpdate', e.title);
			// 	}
			// });
		},
		computed: {
			...mapState(["loginInfo"]),
		},
		methods: {
			onNavigationBarButtonTap(e) {
				// console.log('onNavigationBarButtonTap', e);
				// 此方法会使得添加页面打开后再回退时，会回到九宫格页面，偶尔正常
				// if (e.text == '+') {
				// 	// 操作webview中的元素样式
				// 	// #ifdef APP-PLUS
				// 	var currentWebview = this.$scope.$getAppWebview()
				// 	let wv = currentWebview.children()[0];
				// 	// wv.evalJS("document.getElementById('J_loginIframe').style.height = '500px'");
				// 	wv.evalJS('add()');
				// 	// #endif	

				// 	// plus.runtime.openURL("http://www.baidu.com");//在webview用浏览器打开外部的网址
				// }
			},
		},
	};
</script>
<style lang="scss" scoped>
	.pageStyle {
		height: 100%;
		padding: 20rpx 30rpx;
	}
</style>
