<template>
  <view class="pageStyle">
    <view class="body_top">
      <u-section :font-size="29" :title="form.title" line-color="#1c6ec4" color="#191919" :arrow="false" :right="false">
      </u-section>
      <view class="body_top_bottom">
        <view class="color7d bottom_info">
          发布人：{{form.sender}}
        </view>
        <view class="color7d bottom_info">
          发布时间：{{form.createdate}}
        </view>
      </view>
    </view>
    <u-divider :margin-top="0" :margin-bottom="0" half-width="45%" :use-slot="false"></u-divider>
    <view class="body_center">
      <view class="body_center_top">
        <view class="sub_title">
          <u-section title="发布内容:" line-color="#1c6ec4" :right="false" :show-line="false" :font-size="30"></u-section>
        </view>
        <view class="sub_con">
          {{form.content}}
        </view>
      </view>
    </view>
  </view>
</template>
<script>
import { postAction,getAction } from "@/utils/request.js";
import { mapState } from "vuex";
export default {
  components: {},
  data() {
    return {
      describe: "消息详情",
      model: {},
      url: {
        list: "/public/message/showPage",
      },
      queryParam: {
        id: "",
		skey:''
      },
      list: [],
	  form:{}
    };
  },
  computed: {
    ...mapState(["loginInfo"]),
  },
  onLoad(options) {
    this.model = JSON.parse(options.record);
    this.queryParam.id = this.model.id;
    // this.url.list = `${this.url.list}?skey=${this.loginInfo.skey}`
	this.queryParam.skey = this.loginInfo.skey
    this.search();
  },
  methods: {
    search() {
      getAction(this.url.list, this.queryParam).then((res) => {
        if (res.code == 200) {
        this.form = res.data
        } else {
          uni.showModal({
            icon: "none",
            title: res.msg,
          });
        }
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.pageStyle {
  padding: 20rpx 0;
  background-color: #f9f9f9;
  height: 100%;
  .color7d {
    color: #7d7d7d;
  }
  .colorblack {
    color: #000;
  }
  .body_top {
    background-color: #ffffff;
    padding: 20rpx;
    .body_top_bottom {
      font-size: 28rpx;
      padding: 20rpx;
      display: flex;
      justify-content: space-between;
      .bottom_info {
        overflow: hidden;
      }
    }
  }
  .body_center {
    padding: 20rpx;
    background-color: #ffffff;
    .body_center_top {
      padding: 20rpx 0;
      .sub_title {
        padding-bottom: 20rpx;
      }
      .sub_con {
        font-size: 32rpx;
        font-weight: 400;
        color: #7d7d7d;
        line-height: 45rpx;
      }
    }
    .body_center_bottom {
      padding: 20rpx 0;
      .sub_title {
        padding-bottom: 20rpx;
      }
      .sub_con {
        display: flex;
        justify-content: flex-start;
        flex-flow: wrap;
        .imgs {
          margin-right: 20rpx;
          margin-bottom: 20rpx;
        }
      }
    }
  }
  .look {
    padding: 20rpx 30% 30rpx;
    text-align: center;
  }
}
</style>