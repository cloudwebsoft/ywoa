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
	import {
		getDeptCode
	} from "@/utils/commonHeader.js";

	export default {
		data() {
			return {
				describe: "待办流程",
				url: {
					list: "/weixin/flow/flow_dispose.jsp?skey=",
				},
				urls: "",
			};
		},
		onLoad(options) {
			let records = JSON.parse(options.record);
			console.log('options.record', options.record);
			let scanActionType = '', scanFlowTypeCode = '', scanTargetField = '', scanId = '';
			if (records) {
				scanActionType = records.scanActionType;
				scanFlowTypeCode = records.scanFlowTypeCode;
				scanTargetField = records.scanTargetField;
				scanId = records.scanId;
			}

			this.urls = Api.baseUrl + this.url.list + this.loginInfo.skey + '&flowTypeCode=' + records.scanFlowTypeCode +
				'&scanActionType=' + scanActionType + '&scanTargetField=' + scanTargetField + '&scanId=' + scanId + '&isUniWebview=true';
			console.log('urls', this.urls);
		},
		computed: {
			...mapState(["loginInfo"]),

			// urls() {
			//   let urls = Api.baseUrl + this.url.list + this.loginInfo.skey + '&isUniWebview=true';
			//   return urls;
			// },
		},
	};
</script>

<style lang="scss" scoped>
</style>
