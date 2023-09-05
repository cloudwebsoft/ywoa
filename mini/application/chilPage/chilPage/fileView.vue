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
      describe: "预览",
      urls: "",
    };
  },
  onLoad(options) {
    let records = JSON.parse(options.record);
    //动态获取webview链接
    this.urls = records.isurl;
  },
  computed: {
    ...mapState(["loginInfo"]),

    // urls() {
    //   let urls = Api.baseUrl + this.url.list + this.loginInfo.skey;
    //   console.log("urls", urls);
    //   return urls;
    // },
  },
};
</script>

<style lang="scss" scoped>
</style>
