<template>
	<view class="pageStyle">
		<web-view :src="urls"></web-view>
		<!-- <view class="content_box" v-for="(item,index) in list" :key="index" @click="appealDetails(item,index)">
      <view class="content_box_top">
        <view class="title">
          {{item.title}}
        </view>
        <view class="icon">
          <u-tag :text="item.status" shape="circle" bg-color="#EF5748" border-color="#EF5748" color="#ffffff" />
        </view>
      </view>
      <view class="content_box_bottom">
        {{item.content}}
      </view>
    </view> -->
	</view>
</template>
<script>
	import listContent from "@/pages/mixin/listMixin.js";
	import {
		Api
	} from "@/utils/Api.js";
	import {
		mapState
	} from "vuex";
	export default {
		mixins: [listContent],
		data() {
			return {
				describe: "系统消息",
				model: {},
				url: {
					list: "/weixin/message/msg_new_list.jsp",
				},
				urls: "",
			};
		},
		onReady() {
			// 操作webview中的元素样式
			// #ifdef APP-PLUS
			// var currentWebview = this.$scope.$getAppWebview()
			// let wv = currentWebview.children()[0];
			// wv.evalJS("document.getElementById('J_loginIframe').style.height = '500px'");
			// #endif
		},
		onLoad(options) {
			// weixin/message/msg_new_list.jsp?skey=***
			this.model = JSON.parse(options.record);
			//动态获取webview链接
			this.urls = Api.baseUrl + this.url.list + "?skey=" + this.loginInfo.skey + "&isUniWebview=true";
			// this.search();

			// // 调整webview中页面的样式，仅对本webview有效，对于消息详情页无效
			// let height = 0; //定义动态的高度变量
			// let statusbar = 0; // 动态状态栏高度
			// uni.getSystemInfo({ // 获取当前设备的具体信息
			// 	success: (sysinfo) => {
			// 		console.log(JSON.stringify(sysinfo));
			// 		statusbar = sysinfo.statusBarHeight;
			// 		height = sysinfo.windowHeight;
			// 	}
			// });
			// let currentWebview = this.$scope.$getAppWebview(); //获取当前web-view
			// setTimeout(function() {
			// 	var wv = currentWebview.children()[0];
			// 	wv.setStyle({ //设置web-view距离顶部的距离以及自己的高度，单位为px
			// 		top: statusbar, //此处是距离顶部的高度，应该是你页面的头部
			// 		height: height - statusbar
			// 	})
			// }, 200);
		},
		computed: {
			...mapState(["loginInfo"]),
		},
		methods: {
			search() {
				this.list = [{
						title: "消息来啦",
						content: "最新消息123",
						status: "未读",
					},
					{
						title: "消息来啦",
						content: "最新消息123",
						status: "未读",
					},
					{
						title: "消息来啦",
						content: "最新消息123",
						status: "未读",
					},
					{
						title: "消息来啦",
						content: "最新消息123",
						status: "未读",
					},
					{
						title: "消息来啦",
						content: "最新消息123",
						status: "未读",
					},
					{
						title: "消息来啦",
						content: "最新消息123",
						status: "未读",
					},
				];
				this.list = [...this.list, ...this.list];
			},
			appealDetails(item, index) {
				uni.navigateTo({
					url: `/application/chilPage/appealDetails?record=${JSON.stringify(
          item
        )}`,
				});
			},
		},
	};
</script>
<style lang="scss" scoped>
	.pageStyle {
		height: 100%;
		padding: 20rpx 30rpx;

		.content_box {
			background: #ffffff;
			padding: 40rpx;
			border-radius: 15rpx;
			box-shadow: -3rpx 0px 22rpx 3rpx rgba(87, 193, 189, 0.2);
			margin-bottom: 14rpx;

			.content_box_top {
				display: flex;
				justify-content: space-between;
				margin-bottom: 20rpx;

				.title {
					font-size: 25rpx;
					font-weight: 400;
					color: #323232;
				}
			}

			.content_box_bottom {
				font-size: 22rpx;
				font-weight: 400;
				color: #7d7d7d;
			}
		}
	}
</style>
