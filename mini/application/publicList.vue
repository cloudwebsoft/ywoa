<template>
  <view class="pageStyle">
    <view class="body_top" v-if="selectList && selectList.length>0 || selectTime && selectTime.length>0">
      <view class="marginBottom20">
        <u-row gutter="16" v-if="selectList && selectList.length>0">
          <u-col :span="3">
            <u-input type="select" input-align="right" placeholder="请选择" v-model="selectText" disabled @click="selectShow=true" :height="50" :placeholder-style="'font-size:28rpx'" :custom-style="{fontSize:'28rpx'}" />
          </u-col>
          <u-col :span="9">
            <u-search placeholder="搜索" :show-action="false" :input-style="{fontSize:'28rpx'}" :height="50" v-model="searchQuery.value"></u-search>
          </u-col>
        </u-row>
      </view>
      <view>
        <u-row gutter="16" v-if="selectTime && selectTime.length>0">
          <u-col :span="3">
            <u-input type="select" input-align="right" placeholder="请选择" v-model="selectTimeText" disabled @click="selectTimeShow=true" :height="50" :placeholder-style="'font-size:28rpx'" :custom-style="{fontSize:'28rpx'}" />
          </u-col>
          <u-col :span="9">
            <u-search placeholder="搜索" :show-action="false" :input-style="{fontSize:'28rpx'}" :height="50" disabled @click="selectTimeValueShow=true" v-model="selectTimeValue"></u-search>
          </u-col>
        </u-row>
      </view>
      <view class="search_top_but">
        <u-button size="mini" :hover-class="'buttons'" :custom-style="{width:'200rpx',backgroundColor:'#CE614A',padding:'20rpx',border:'none',color:'#fff', fontSize:'32rpx'}" @click="search">查询</u-button>
      </view>
    </view>

    <u-empty mode="list" v-if="isNull"></u-empty>
    <slot name="headerMain"></slot>
    <view class="body_conter">
      <view v-if="defaultMain">
        <uni-swipe-action ref="swipeAction">
          <uni-swipe-action-item v-for="(item, index) in list" :key="index" style="margin-bottom: 20rpx;">
            <view class="content_box" @click="itemClick(item,index)">
              <view class="" v-if="pageSetup && pageSetup.length>0">
                <view v-for="(row,r) in pageSetup" :key="r">
                  <u-row gutter="16">
                    <u-col v-for="(col,c) in row.blocks" :key="c" :span="col.width*2" @click="itemClick(item,index)">
                      <view v-if="col.type=='field'" :style="[{backgroundColor:col.bgColor?col.bgColor:'transparent',font:col.font?col.font:'',fontSize:col.fontSize?col.fontSize+'rpx':'32rpx',fontWeight:col.isBold=='true'?'bold':'',width:'100%',height:'100%'}, {paddingTop:col.paddingTop?col.paddingTop+'rpx':'0rpx',paddingBottom:col.paddingBottom?col.paddingBottom+'rpx':'0rpx',paddingLeft:col.paddingLeft?col.paddingLeft+'rpx':'0rpx',paddingRight:col.paddingRight?col.paddingRight+'rpx':'0rpx',marginTop:col.marginTop?col.marginTop+'rpx':'0rpx',marginRight:col.marginRight?col.marginRight+'rpx':'0rpx',marginBottom:col.marginBottom?col.marginBottom+'rpx':'0rpx',marginLeft:col.marginLeft?col.marginLeft+'rpx':'0rpx'}]">
                        <view v-if="item.fields && item.fields.filter(t => t.name==col.fieldName).length>0 && item.fields.filter(t => t.name==col.fieldName)[0].controlType && item.fields.filter(t => t.name==col.fieldName)[0].controlType=='img'" :style="{width:'100%',height:'100%',textAlign:col.align?col.align:'left'}">
                          <!-- 如果字段为图片型 -->
						  <!-- <u-image width="100%" height="100%" :src="baseUrl+'/'+item.fields.filter(t => t.name==col.fieldName)[0].text"></u-image> -->
                          <image style="width: 100%; height: 40rpx;" :src="baseUrl+'/'+item.fields.filter(t => t.name==col.fieldName)[0].text"></image>
                        </view>
                        <view v-else :style="[{textAlign:col.align?col.align:'left', backgroundImage: 'url(' +baseUrl+'/'+ col.bgImgUrl + ')', backgroundRepeat:'no-repeat', color: col.fontColor}]">
                          <text v-if="col.fontIcon" :class="col.fontIcon?'fa '+col.fontIcon:'fa-phone'"></text>{{'  '}} {{col.label.indexOf('%s')==-1?col.label:col.label.replace('%s', item.fields.filter(t => t.name==col.fieldName)[0].text)}}
                          <!-- {{item.fields?item.fields.filter(t => t.name==col.fieldName).length>0?item.fields.filter(t => t.name==col.fieldName)[0].text?item.fields.filter(t => t.name==col.fieldName)[0].text:'':' ':' '}} -->
                          <text v-if="item.fields && item.fields.length>0 && item.fields.filter(t => t.name==col.fieldName).length>0 && item.fields.filter(t => t.name==col.fieldName)[0].text && col.label.indexOf('%s')==-1">
                            {{item.fields.filter(t => t.name==col.fieldName)[0].text}}
                          </text>
                          <text v-else style="opacity:0">blank</text>
                        </view>
                      </view>
                      <view v-else-if="col.type=='comb' && col.fieldName" :style="[{backgroundColor:col.bgColor?col.bgColor:'transparent',font:col.font?col.font:'',fontSize:col.fontSize?col.fontSize+'rpx':'32rpx',fontWeight:col.isBold=='true'?'bold':'',background: 'url(' +baseUrl+'/'+ col.bgImgUrl + ') no-repeat', backgroundSize: '100% 100%', color: col.fontColor ,textAlign:col.align?col.align:'left' }, {paddingTop:col.paddingTop?col.paddingTop+'rpx':'0rpx',paddingBottom:col.paddingBottom?col.paddingBottom+'rpx':'0rpx',paddingLeft:col.paddingLeft?col.paddingLeft+'rpx':'0rpx',paddingRight:col.paddingRight?col.paddingRight+'rpx':'0rpx',marginTop:col.marginTop?col.marginTop+'rpx':'0rpx',marginRight:col.marginRight?col.marginRight+'rpx':'0rpx',marginBottom:col.marginBottom?col.marginBottom+'rpx':'0rpx',marginLeft:col.marginLeft?col.marginLeft+'rpx':'0rpx'}]">
                        <!-- {{item.fields?item.fields.filter(t => t.name==col.deptCode).length>0?item.fields.filter(t => t.name==col.deptCode)[0].text?item.fields.filter(t => t.name==col.deptCode)[0].text:' ':' ':' '}} -->
                        <text v-if="item.fields && item.fields.length>0 && item.fields.filter(t => t.name==col.blockId).length>0 && item.fields.filter(t => t.name==col.blockId)[0].text">
                          {{item.fields.filter(t => t.name==col.blockId)[0].text}}
                        </text>
                        <text v-else style="opacity:0">blank</text>
                      </view>
                      <view v-else-if="col.type=='text'" :style="[{backgroundColor:col.bgColor?col.bgColor:'transparent',font:col.font?col.font:'',fontSize:col.fontSize?col.fontSize+'rpx':'32rpx',fontWeight:col.isBold=='true'?'bold':'',textAlign:col.align?col.align:'left', backgroundImage: 'url(' +baseUrl+'/'+ col.bgImgUrl + ')', backgroundRepeat:'no-repeat', color: col.fontColor}, {paddingTop:col.paddingTop?col.paddingTop+'rpx':'0rpx',paddingBottom:col.paddingBottom?col.paddingBottom+'rpx':'0rpx',paddingLeft:col.paddingLeft?col.paddingLeft+'rpx':'0rpx',paddingRight:col.paddingRight?col.paddingRight+'rpx':'0rpx',marginTop:col.marginTop?col.marginTop+'rpx':'0rpx',marginRight:col.marginRight?col.marginRight+'rpx':'0rpx',marginBottom:col.marginBottom?col.marginBottom+'rpx':'0rpx',marginLeft:col.marginLeft?col.marginLeft+'rpx':'0rpx'}]">
						{{col.label}}
					  </view>
                      <view v-else-if="col.type=='img'" class="imgs" :style="[{backgroundColor:col.bgColor?col.bgColor:'transparent',font:col.font?col.font:'',fontSize:col.fontSize?col.fontSize+'rpx':'32rpx',fontWeight:col.isBold?'bold':'',backgroundImage: 'url(' +baseUrl+'/'+ col.bgImgUrl + ') no-repeat' ,textAlign:col.align?col.align:'left'}, {paddingTop:col.paddingTop?col.paddingTop+'rpx':'0rpx',paddingBottom:col.paddingBottom?col.paddingBottom+'rpx':'0rpx',paddingLeft:col.paddingLeft?col.paddingLeft+'rpx':'0rpx',paddingRight:col.paddingRight?col.paddingRight+'rpx':'0rpx',marginTop:col.marginTop?col.marginTop+'rpx':'0rpx',marginRight:col.marginRight?col.marginRight+'rpx':'0rpx',marginBottom:col.marginBottom?col.marginBottom+'rpx':'0rpx',marginLeft:col.marginLeft?col.marginLeft+'rpx':'0rpx'}]">
						  <image style="width: 100%; height: 40rpx;" :src="baseUrl+'/'+ col.imgUrl"></image>
					  </view>
                      <view v-else-if="col.type=='blank'" style="opacity:0">blank</view>
                      <view class="line" v-else-if="col.type=='line'">
                      </view>
                    </u-col>
                  </u-row>
                </view>
              </view>
              <view v-else>
                <view v-for="(row,co) in cols" :key="co">
                  <u-row gutter="16">
                    <u-col style="margin-bottom:10rpx" @click="itemClick(item,index)">
                      <view>{{item.fields?item.fields.filter(t => t.name==row.name).length>0?item.fields.filter(t => t.name==row.name)[0].text:'':''}}</view>
                    </u-col>
                  </u-row>
                </view>
              </view>
            </view>
            <template slot="right">
			<view class="slot-button">
			  <view class="slot-button-con" style="background-color:#de9a10" @click="swipeClick({content:{text:'编辑'}},index,item)" v-if="canDel">
				<text class="slot-button-text">编辑</text>
			  </view>
			</view>
              <view class="slot-button">
                <view class="slot-button-con" style="background-color:#ff5a5f" @click="swipeClick({content:{text:'删除'}},index,item)" v-if="canDel">
                  <text class="slot-button-text">删除</text>
                </view>
              </view>
            </template>
          </uni-swipe-action-item>
        </uni-swipe-action>

      </view>
      <slot name="bottomMain"></slot>
    </view>

    <view class="body_bottom" @click="addModule" v-if="isAdd || canAdd">
      <!-- &&canAdd -->
      {{addName}}
    </view>
    <!-- 搜索 -->
    <u-select v-model="selectShow" mode="single-column" label-name="fieldTitle" :value-name="'fieldName'" :list="selectList" @confirm="confirmSelect" @cancel="cancelSelect"></u-select>
    <!-- 时间字段搜索 -->
    <u-select v-model="selectTimeShow" mode="single-column" label-name="fieldTitle" :value-name="'fieldName'" :list="selectTime" @confirm="confirmSelectTime" @cancel="cancelSelectTime"></u-select>
    <!-- 时间 -->
    <u-calendar v-model="selectTimeValueShow" :mode="searchQueryTime.condValue==1?'date':'range'" @change="confirmTime">
      <view slot="tooltip">
        <view class="timeBut" @click="clearTime">
          清除
        </view>
      </view>
    </u-calendar>
    <!-- <u-picker v-model="selectTimeValueShow" mode="time" :params="times" @confirm="confirmTime" @cancel="cancelTime"></u-picker> -->
  </view>
</template>
<script>
import {
  getAction,
  deleteDefaultAction,
  deleteAction,
  postFormAction,
} from "@/utils/request.js";
import listContent from "@/pages/mixin/listMixin.js";
import { Api } from "@/utils/Api.js";
export default {
  props: {
    params: {
      type: Object,
      default: () => {
        return {};
      },
    },
    url: {
      type: Object,
      default: () => {
        return {
          list: "/modular/list",
        };
      },
    },
    listRecord: {
      type: Object,
      default: () => {
        return {};
      },
    },
    //额外参数
    otherRecord: {
      type: Object,
      default: () => {
        return {};
      },
    },
    nativeTo: {
      type: String,
      default: "/application/chilPage/rectificationNoticeDetails",
    },
    isDefaultNativeTo: {
      type: Boolean,
      default: true,
    },
    defaultMain: {
      type: Boolean,
      default: true,
    },
    //编辑
    nativeToEdit: {
      type: String,
      default: "", ///application/moduleAddEdit
    },
    //新增
    nativeToAdd: {
      type: String,
      default: "/application/moduleAddEdit",
    },
    isAdd: {
      type: Boolean,
      default: false,
    },
    addName: {
      type: String,
      default: "新增",
    },
  },
  mixins: [listContent],
  //下拉刷新
  // onPullDownRefresh() {
  //   // #ifdef MP-WEIXIN
  //   this.search();
  //   wx.stopPullDownRefresh();
  //   // #endif

  //   // #ifdef APP-PLUS || H5
  //   uni.startPullDownRefresh();
  //   this.search();
  //   uni.stopPullDownRefresh();
  //   // #endif
  // },
  //上滑动刷新
  // onReachBottom() {
  //   this.getOnReachBottom();
  // },
  data() {
    return {
      describe: "列表",
      model: {},
      queryParam: {},
      selectShow: false,
      selectText: "",
      selectTimeShow: false,
      selectTimeText: "",
      selectTimeValue: "",
      selectTimeValueShow: false,
      times: {
        year: true,
        month: true,
        day: true,
        hour: false,
        minute: false,
        second: false,
      },
      searchQuery: {
        label: "",
        value: "",
        cond: "",
        condValue: "",
      },
      searchQueryTime: {
        label: "",
        value: "",
        cond: "",
        condValue: "",
        fromDate: "",
        toDate: "",
        fromDateValue: "",
        toDateValue: "",
      },
      urlDelete: "/modular/del",
      isHadSearch: true,
      baseUrl: Api.baseUrl,
    };
  },
  onLoad(options) {
    // this.model = JSON.parse(options.record);
    // this.search();
  },
  onShow() {},
  methods: {
    search() {
      this.pageNum = 1;
      if (this.searchQuery.label) {
        let param = {};
        param[this.searchQuery.label] = this.searchQuery.value;
        param[this.searchQuery.cond] = this.searchQuery.condValue;
        this.queryParam = Object.assign({}, this.params, param, {
          op: "search",
        });
      } else {
        this.queryParam = this.params;
      }
      if (this.searchQueryTime.label) {
        let paramTime = {};
        paramTime[this.searchQueryTime.label] = this.searchQueryTime.value;
        paramTime[this.searchQueryTime.cond] = this.searchQueryTime.condValue;
        this.queryParam = Object.assign({}, this.queryParam, paramTime, {
          op: "search",
        });
      }
      if (this.searchQueryTime.fromDate) {
        let paramTime = {};
        paramTime[this.searchQueryTime.fromDate] =
          this.searchQueryTime.fromDateValue;
        paramTime[this.searchQueryTime.toDate] =
          this.searchQueryTime.toDateValue;
        paramTime[this.searchQueryTime.cond] = this.searchQueryTime.condValue;
        this.queryParam = Object.assign({}, this.queryParam, paramTime, {
          op: "search",
        });
      }
      setTimeout(() => {
        this.getListData();
      }, 200);
    },
    itemClick(item, index) {
      console.log("点击了", item, index);
      let cwsVisiteds = item.fields.filter((el) => el.name === "cws_visited");
      // 判断是否已读 有cws_visited字段后再判断 如果是否就调接口,如果不是否就不调接口
      if (cwsVisiteds.length === 0) {
        // this.itemToDetails(item);
		uni.navigateTo({
		  url: `${this.nativeTo}?record=${JSON.stringify({
		    ...item,
		    code: this.queryParam.moduleCode,
		  })}`,
		});
      } else {
        if (cwsVisiteds[0].text === "否") {
          uni.showLoading({
            title: "加载中",
            mask: true,
          });
          getAction("/modular/visit", {
            id: item.id,
            moduleCode: this.queryParam.moduleCode,
          }).then((res) => {
            uni.hideLoading();
            if (res.res == 0) {
              // this.itemToDetails(item);
			  uni.navigateTo({
			    url: `${this.nativeTo}?record=${JSON.stringify({
			      ...item,
			      code: this.queryParam.moduleCode,
			    })}`,
			  });
              this.getThisPage();
            } else {
              uni.showToast({
                icon: "none",
                mask: false,
                title: res.msg,
              });
            }
          });
        } else {
          // this.itemToDetails(item);
		  uni.navigateTo({
		    url: `${this.nativeTo}?record=${JSON.stringify({
		      ...item,
		      code: this.queryParam.moduleCode,
		    })}`,
		  });
        }
      }
    },
    itemToDetails(item) {
      console.log(
        "item===>",
        item,
        this.nativeTo,
        this.queryParam.moduleCode,
        this.isDefaultNativeTo
      );
      if (this.isDefaultNativeTo) {
        item.fields.forEach((el) => {
          if (el.name == "location" || el.name == "fujian") {
            el.text = "";
          }
        });
        if (this.canEdit && this.nativeToEdit) {
          uni.navigateTo({
            url: `${this.nativeToEdit}?record=${JSON.stringify({
              ...item,
              code: this.queryParam.moduleCode,
            })}`,
          });
        } else {
          console.log(
            "item===>",
            item,
            this.nativeTo,
            this.queryParam.moduleCode
          );
          uni.navigateTo({
            url: `${this.nativeTo}?record=${JSON.stringify({
              ...item,
              code: this.queryParam.moduleCode,
            })}&listRecord=${JSON.stringify(
              this.listRecord
            )}&otherRecord=${JSON.stringify(this.otherRecord)}`,
          });
        }
      } else {
        this.$emit("itemClick", item, index);
      }
    },
    //列表请求完成
    hadSearchData(list) {
      if (this.isHadSearch) {
        this.isHadSearch = false;
        //输入栏
        if (this.selectList && this.selectList.length > 0) {
          this.confirmSelect([
            {
              value: this.selectList[0].fieldName,
              label: this.selectList[0].fieldTitle,
            },
          ]);
        }
        //时间栏
        if (this.selectTime && this.selectTime.length > 0) {
          this.confirmSelectTime([
            {
              value: this.selectTime[0].fieldName,
              label: this.selectTime[0].fieldTitle,
            },
          ]);
        }
      }
    },
    confirmSelect(e) {
      if (e) {
        this.selectText = e[0].label;
        this.searchQuery.label = e[0].value;
        this.searchQuery.value = "";
        this.searchQuery.cond = e[0].value + "_cond";
        this.searchQuery.condValue = this.selectList.filter(
          (item) => item.fieldTitle == e[0].label
        )[0].fieldCond;
      }
    },
    //取消
    cancelSelect() {
      this.selectText = "";
      for (let v in this.searchQuery) {
        this.searchQuery[v] = "";
      }
    },
    //新增
    addModule() {
      uni.navigateTo({
        url: `${this.nativeToAdd}?record=${JSON.stringify(this.listRecord)}`,
      });
    },
    swipeClick(e, index, item) {
      let { content } = e;
	  if (content.text == '编辑') {
		  this.itemToDetails(item);
	  } else if (content.text === "删除") {
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
      postFormAction(this.urlDelete, {
        id: record.id,
        moduleCode: this.params.moduleCode,
      }).then((res) => {
        uni.showToast({
          title: res.msg,
        });
        if (res.res == 0) {
          this.list.splice(index, 1);
        }
      });
    },
    // -------------------------时间选择字段------------------------------------------------------
    confirmSelectTime(e) {
      // fieldCond 0 是时间段 1是时间点
      if (e) {
        this.searchQueryTime.condValue = this.selectTime.filter(
          (item) => item.fieldTitle == e[0].label
        )[0].fieldCond;
        this.searchQueryTime.cond = e[0].value + "_cond";
        this.selectTimeText = e[0].label;
        this.searchQueryTime.label = "";
        this.searchQueryTime.value = "";
        this.searchQueryTime.fromDate = "";
        this.searchQueryTime.toDate = "";
        this.searchQueryTime.fromDateValue = "";
        this.searchQueryTime.toDateValue = "";
        if (this.searchQueryTime.condValue == 0) {
          this.searchQueryTime.fromDate = e[0].value + "FromDate";
          this.searchQueryTime.toDate = e[0].value + "ToDate";
        } else {
          this.searchQueryTime.label = e[0].value;
        }
      }
    },
    //取消
    cancelSelectTime() {
      this.selectTimeText = "";
      this.selectTimeValue = "";
      for (let v in this.searchQueryTime) {
        this.searchQueryTime[v] = "";
      }
    },
    // -------------------------时间选择字段------------------------------------------------------
    //--------------------------时间值选择--------------------------------------------------------
    confirmTime(e) {
      // this.selectTimeValue = `${e.year}-${e.month}-${e.day} ${e.hour}:${e.minute}:${e.second}`;
      // this.selectTimeValue = `${e.year}-${e.month}-${e.day}`;
      // this.searchQueryTime.value = this.selectTimeValue;
      if (this.searchQueryTime.condValue == 0) {
        this.selectTimeValue = `${e.startDate}-${e.endDate}`;
        let fromDate = e.startDate;
        let toDate = e.endDate;
        this.searchQueryTime.fromDateValue = fromDate;
        this.searchQueryTime.toDateValue = toDate;
      } else {
        this.selectTimeValue = e.result;
        this.searchQueryTime.value = this.selectTimeValue;
      }
    },
    //清除
    clearTime() {
      this.selectTimeValueShow = false;
      this.selectTimeValue = "";
      this.searchQueryTime.fromDateValue = "";
      this.searchQueryTime.toDateValue = "";
      this.searchQueryTime.value = "";
    },
    //--------------------------时间值选择--------------------------------------------------------
  },
};
</script>
<style lang="scss" scoped>
.pageStyle {
  height: 100%;
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
  .buttons {
    background-color: "#31E6E1";
    padding: "20rpx";
    border: "none";
    color: "#fff";
  }
  .marginBottom20 {
    margin-bottom: 20rpx;
  }
  .body_top {
    padding: 20rpx 40rpx;
    .search_top_but {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 20rpx;
    }
  }
  .body_conter {
    padding: 20rpx 20rpx 100rpx;
  }
  .content_box {
    background: #ffffff;
    padding: 20rpx;
    border-radius: 15rpx;
    box-shadow: -3rpx 0px 22rpx 3rpx rgba(87, 193, 189, 0.2);
    margin-bottom: 20rpx;
    .imgs {
      width: 100%;
      height: 100%;
      background-size: cover;
      background-repeat: no-repeat;
      background-position: center;
    }
    .line {
      width: 100%;
      height: 1;
      margin: 20rpx 0;
      border-top: 1px solid #eee;
    }
  }
  .body_bottom {
    position: fixed;
    bottom: 0;
    height: 100rpx;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ffffff;
    background-color: #31e6e1;
  }

  .timeBut {
    padding: 20rpx;
  }
}
</style>
