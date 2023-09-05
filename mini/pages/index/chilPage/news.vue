<template>
	<view class="pageStyle">
		<uni-swipe-action ref="swipeAction">
			<uni-swipe-action-item v-for="(item, index) in list" :key="index" style="margin-bottom: 20rpx;" >
				<view class="content_box" @click="appealDetails(item, index)">
					<view class="content_box_top">
						<view class="title">{{ item.title }}</view>
						<view class="icon"><u-tag :text="item.haveread == 'true' ? '已读' : '未读'" shape="circle" :type="item.haveread == 'true' ? 'success' : 'error'" /></view>
					</view>
					<view class="content_box_bottom">
						<view class="">{{ item.sender }}</view>
						<view class="">{{ item.createdate }}</view>
					</view>
				</view>
				<template slot="right">
					<view class="slot-button">
						<view class="slot-button-con" style="background-color:#ff5a5f" @click="swipeClick({ content: { text: '删除' } }, index, item)">
							<text class="slot-button-text">删除</text>
						</view>
					</view>
				</template>
			</uni-swipe-action-item>
		</uni-swipe-action>
	</view>
</template>
<script>
import listContent from '@/pages/mixin/listMixin.js';
import { mapState } from 'vuex';
import {
  postFormAction,
} from "@/utils/request.js";
export default {
	mixins: [listContent],
	data() {
		return {
			describe: '系统消息',
			model: {},
			url: {
				list: '/public/message/list',
				del: '/public/message/del'
			},
			queryParam: {
				skey: ''
				// pageSize:1
			},
			loadHock: false,
			isGet: false
		};
	},
	onLoad(options) {
		// weixin/message/msg_new_list.jsp?skey=***
		this.model = JSON.parse(options.record);
		console.log('loginInfo', this.loginInfo);
		// this.search();
	},
	computed: {
		...mapState(['loginInfo'])
	},
	methods: {
		searchList() {
			this.queryParam.skey = this.loginInfo.skey;
			this.getListData();
		},
		appealDetails(item, index) {
			uni.navigateTo({
				url: `/application/news/newsShowPage?record=${JSON.stringify(item)}`
			});
		},

		//删除
		swipeClick(e, index, item) {
		  let { content } = e;
		  if (content.text === "删除") {
		    uni.showModal({
		      title: "提示",
		      content: "是否确定删除",
		      success: (res) => {
		        if (res.confirm) {
		          this.handleDelete(item, index);
		        } else if (res.cancel) {
		          console.log("用户点击取消");
		        }
		      },
		    });
		  } else if (content.text === "提交") {
		    this.editModules(item);
		  }
		},
		//单个删除
		handleDelete(record, index) {
		  postFormAction(this.url.del, {
		    ids: record.id,
		  }).then((res) => {
		    uni.showToast({
		      title: res.msg,
		    });
		    if (res.code == 200) {
		      this.list.splice(index, 1);
		    }
		  });
		},
	}
};
</script>
<style lang="scss" scoped>
.pageStyle {
	height: 100%;
	background-color: #f9f9f9;
	overflow-y: auto;
	padding: 20rpx 30rpx;
	.content_box {
		background: #ffffff;
		padding: 40rpx;
		border-radius: 15rpx;
		box-shadow: -3rpx 0px 22rpx 3rpx rgba(87, 193, 189, 0.2);
		margin-bottom: 14rpx;
		.content_box_top {
			display: flex;
			justify-content: space-between;
			margin-bottom: 20rpx;
			align-items: center;
			.title {
				font-size: 34rpx;
				font-weight: 400;
				color: #323232;
				width: 80%;
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
			}
		}
		.content_box_bottom {
			font-size: 28rpx;
			font-weight: 400;
			color: #7d7d7d;
			display: flex;
			justify-content: space-between;
		}
	}
	
	.slot-button {
	  /* #ifndef APP-NVUE */
	  display: flex;
	  height: 100%;
	  /* #endif */
	  flex: 1;
	  flex-direction: row;
	  justify-content: center;
	  align-items: center;
	  background-color: transparent;
	  padding-bottom: 20rpx;
	  .slot-button-con {
	    height: 100%;
	    width: 100%;
	    padding: 0 20upx;
	    //   border-radius: 16upx;
	    /* #ifndef APP-NVUE */
	    display: flex;
	    height: 100%;
	    /* #endif */
	    display: flex;
	    // flex: 1;
	    flex-direction: row;
	    justify-content: center;
	    align-items: center;
	    margin-right: 10rpx;
	    margin-left: 10rpx;
	  }
	  .slot-button-text {
	    color: #ffffff;
	    font-size: 14px;
	  }
	}
}
</style>
