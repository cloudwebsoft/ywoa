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
				describe: "通用查看",
				url: {
					show: "/weixin/visual/module_detail.jsp",
				},
				urls: "",
			};
		},
		onLoad(options) {
			let records = JSON.parse(options.record);
			console.log('this.loginInfo', this.loginInfo);
			//动态获取webview链接
			if (records.id) {
				let moduleCode = '';
				if (records.moduleCode) {
					moduleCode = records.moduleCode;
				} else {
					moduleCode = records.code;
				}
				//编辑
				this.urls =
					Api.baseUrl +
					this.url.show +
					"?skey=" +
					this.loginInfo.skey +
					"&moduleCode=" +
					moduleCode +
					"&id=" +
					records.id +
					"&isUniWebview=true";
			} else {
				uni.showToast({
					title: '缺少ID',
				});
			}
		},
		computed: {
			...mapState(["loginInfo"]),

			// urls() {
			//   let urls = Api.baseUrl + this.url.list + this.loginInfo.skey;
			//   return urls;
			// },
		},
	};
</script>
<style lang="scss" scoped>
</style>
