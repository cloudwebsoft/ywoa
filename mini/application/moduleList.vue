<template>
  <view class="pageStyle">
    <public-list ref="publicListShow" :params="queryParam" :listRecord="model" nativeTo="/application/moduleShow" nativeToEdit="/application/moduleAddEdit"></public-list>
  </view>
</template>
<script>
import PublicList from "./publicList.vue";
export default {
  components: { PublicList },
  data() {
    return {
      describe: "模块列表",
      model: {},
      queryParam: {
        moduleCode: "",
      },
      isDoRefresh: false,
      isReadShow: false,
    };
  },
  onLoad(options) {
    this.model = JSON.parse(options.record);
    this.queryParam.moduleCode = this.model.code;
	this.queryParam.treeNodeCode = this.model.treeNodeCode;
    this.search();
  },
  onShow() {
    if (!this.isReadShow) {
      this.isReadShow = true;
    } else {
      this.search();
    }
  },
  //新增完之后刷页面
  // onShow(e) {
  //   const that = this;
  //   let pages = getCurrentPages();
  //   //刷新页面
  //   let currPage = pages[pages.length - 1];
  //   if (currPage.isDoRefresh == true) {
  //     currPage.isDoRefresh = false;
  //     that.search();
  //   }
  // },
  //下拉刷新
  onPullDownRefresh() {
    // #ifdef MP-WEIXIN
    this.search();
    wx.stopPullDownRefresh();
    // #endif

    // #ifdef APP-PLUS || H5
    uni.startPullDownRefresh();
    this.search();
    uni.stopPullDownRefresh();
    // #endif
  },
  //上滑动刷新
  onReachBottom() {
    this.$refs.publicListShow.getOnReachBottom();
  },
  methods: {
    search() {
      setTimeout(() => {
        this.$refs.publicListShow.search();
      }, 200);
    },
  },
};
</script>
<style lang="scss" scoped>
.pageStyle {
  height: 100%;
}
</style>
