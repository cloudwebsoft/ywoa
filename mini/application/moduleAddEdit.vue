<template>
  <view class="pageStyle">
    <web-view :src="urls"></web-view>
  </view>
</template>
<script>
import { Api } from "@/utils/Api.js";
import { mapState } from "vuex";

export default {
  data() {
    return {
      describe: "通用编辑",
      url: {
        list: "/weixin/visual/module_add_edit.jsp",
      },
      urls: "",
    };
  },
  onLoad(options) {
    let records = JSON.parse(options.record);
	console.log('options.record', options.record);
	let scanActionType = '', scanModuleCode = '', scanTargetField = '', scanId = '';
	if (records) {
		scanActionType = records.scanActionType;
		scanModuleCode = records.scanModuleCode;
		scanTargetField = records.scanTargetField;
		scanId = records.scanId;
	}
	console.log('this.loginInfo', this.loginInfo);
    //动态获取webview链接
    if (records.id) {
      //编辑
      this.urls =
        Api.baseUrl +
        this.url.list +
        "?skey=" +
        this.loginInfo.skey +
        "&moduleCode=" +
        records.code +
        "&id=" +
        records.id +
        "&isUniWebview=true";
    } else {
		if (scanModuleCode) {
			this.urls =
			  Api.baseUrl +
			  this.url.list +
			  "?skey=" +
			  this.loginInfo.skey +
			  "&moduleCode=" +
			  scanModuleCode +
					'&scanActionType=' + scanActionType + '&scanTargetField=' + scanTargetField + '&scanId=' + scanId +
			  "&isUniWebview=true";
		} else {
			this.urls =
			  Api.baseUrl +
			  this.url.list +
			  "?skey=" +
			  this.loginInfo.skey +
			  "&moduleCode=" +
			  records.code +
			  "&isUniWebview=true";
		}
      
		console.log('this.urls', this.urls);
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
