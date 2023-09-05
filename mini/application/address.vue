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
				describe: "通讯录",
				model: {},
				url: {
					list: "/weixin/address/address_list.jsp",
				},
				urls: "",
			};
		},
		onLoad(options) {
			this.model = JSON.parse(options.record);

			//动态获取webview链接
			this.urls = Api.baseUrl + this.url.list + "?skey=" + this.loginInfo.skey + "&isUniWebview=true";
		},
		computed: {
			...mapState(["loginInfo"]),
		}
	};
</script>
<style lang="scss" scoped>
	.pageStyle {
		height: 100%;
		padding: 20rpx 30rpx;
	}
</style>
