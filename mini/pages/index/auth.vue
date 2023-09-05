<template>
  <view class="">
    <view style="width:100%;height:100rpx;display: flex;
    justify-content: center;align-items: center;font-size:36rpx;margin-bottom:30rpx">
      请点击授权后使用
    </view>
    <!-- 如果只是展示用户头像昵称，可以使用 <open-data /> 组件 -->
    <!-- <open-data type="userAvatarUrl"></open-data>
    <open-data style="display: flex;justify-content: center;" type="userNickName"></open-data> -->
    <!-- 需要使用 button 来授权登录 -->
    <button style="width:70%;" v-if="canIUse" open-type="getUserInfo" @click="bindGetUserInfo">授权登录</button>
    <view v-else style="width:100%;text-align:center">请升级微信版本</view>
  </view>
</template>
<script>
import loginInfo from "@/pages/mixin/method.js";
export default {
  mixins: [loginInfo],
  data() {
    return {
      canIUse: wx.canIUse("button.open-type.getUserInfo"),
    };
  },
  methods: {
    // 授权回调
    bindGetUserInfo(e) {
      console.log(e.detail.userInfo);
      this.getLoginCode();
    },
  },
};
</script>