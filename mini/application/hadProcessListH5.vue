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
      describe: "我的流程",
      url: {
        list: "/weixin/flow/flow_attend.jsp?skey=",
      },
      // urls: "",
    };
  },
  onLoad(options) {
    // let records = JSON.parse(options.record);
    // 动态获取webview链接
    // this.urls =
    //   Api.baseUrl +
    //   "/" +
    //   records.code +
    //   "?skey=" +
    //   this.loginInfo.skey +
    //   "&isUniWebview=true";
  },
  computed: {
    ...mapState(["loginInfo"]),
    urls() {
      let urls = Api.baseUrl + this.url.list + this.loginInfo.skey + "&isUniWebview=true";
      return urls;
    },
  },
};
</script>

<style lang="scss" scoped>
</style>
