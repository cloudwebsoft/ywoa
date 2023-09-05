<template>
  <view class="pageStyle">
    <view class="body_top">
      <u-search placeholder="流程标题或流程号" bg-color="#e9e9e9" v-model="queryParam.title" @search="search" @custom="search"></u-search>
    </view>
    <u-empty mode="list" v-if="list && list.length <= 0"></u-empty>
    <view class="body_conter">
      <view class="content" v-for="(item,index) in list" :key="index" @click="todoItem(item,index)">
        <view class="content_top" :style="[{borderBottom:index!=(list.length-1)?'1px solid #f4f4f4':'0'}]">
          <view class="title">
            {{item.typeName}}
          </view>
          <view class="flowId">
            <text>ID:{{item.flowId}}</text>
            <text style="margin-left:10rpx">{{item.status}}</text>
          </view>
          <view class="name">
            {{item.name}}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>
<script>
import { Api } from "@/utils/Api.js";
import listContent from "@/pages/mixin/listMixin.js";
import { filterObj, serialize } from "@/utils/util.js";
import { postAction } from "@/utils/request.js";
export default {
  mixins: [listContent],
  data() {
    return {
      describe: "待办流程列表",
      queryParam: {
        op: "search",
        title: "",
      },
      url: {
        list: "",
        listToDo: "/mobile/flow/listToDo",
      },
    };
  },
  onLoad() {},
  onShow() {
    // this.searchData();
  },
  //下拉刷新
  onPullDownRefresh() {
    uni.startPullDownRefresh();
    this.searchData();
    uni.stopPullDownRefresh();
  },
  methods: {
    search() {
      this.pagenum = 1;
      this.list = [];
      // this.getListData();
    },

    /*获取list*/
    getListData() {
      uni.showLoading({
        title: "加载中",
        mask: true,
      });
      let query = serialize(
        filterObj({
          pagenum: this.pagenum,
          pagesize: this.pagesize,
          ...this.isorter,
          ...this.queryParam,
        })
      );
      console.log("query", query);
      this.url.list = this.url.listToDo + "?" + query;
      postAction(this.url.list)
        .then((res) => {
          if (res.res == 0) {
            let arrs = [];
            arrs = res.result.flows ? res.result.flows : [];
            //总条数
            this.totals = res.total;
            arrs.map((res) => {
              res.checked = false;
            });
            if (this.pagenum == 1) {
              this.list = [...arrs];
            } else {
              this.list = this.list.concat(arrs);
            }
          }
        })
        .finally(() => {
          uni.hideLoading();
        });
    },
    //跳转流程表单
    todoItem(item, index) {
      uni.navigateTo({
        url: `/pages/application/chilPage/chilPage/processInfo?record=${JSON.stringify(
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
  background-color: #f4f4f4;
  .body_top {
    padding: 20rpx;
  }
  .body_conter {
    padding-bottom: 20rpx;
    background-color: #f4f4f4;
    .content {
      background-color: #fff;
      padding-left: 40rpx;
      .content_top {
        padding: 20rpx 0;
        .title {
          font-size: 32rpx;
          font-weight: bold;
        }
        .flowId {
          font-size: 28rpx;
          font-weight: #7d7d7d;
          margin: 16rpx 0;
        }
        .name {
          font-size: 24rpx;
          font-weight: #7d7d7d;
        }
      }
    }
  }
}
</style>