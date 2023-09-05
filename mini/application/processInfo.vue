<template>
  <view class="pageStyle">
    <!-- <web-view :webview-styles="webviewStyles" src="https://uniapp.dcloud.io/static/web-view.html"></web-view> -->
    <!-- <web-view :webview-styles="webviewStyles" src="https://www.baidu.com/s?wd=知乎"></web-view> -->
    <!-- <view class="subsection_top">
      <u-subsection :list="list" :current="curNow" @change="sectionChange" active-color="#ffffff" button-color="#1c6ec4" bg-color="#ffffff"></u-subsection>
    </view> -->
    <view class="form_conter">
      <uni-forms ref="form" :modelValue="model" :label-width="108">
        <!-- <uni-forms-item>
          <view class="form_top">
            {{cwsWorkflowTitle}}
          </view>
        </uni-forms-item> -->
        <template v-for="(item,index) in fields">
          <uni-forms-item :label="item.title" :key="index" :required="item.isNull==false" :name="item.isNull==false?(item.type ==='text'||item.type ==='DATE_TIME'||item.type ==='textarea'?'value':item.macroType === 'select' ||item.macroType === 'textarea' ||item.macroType === 'text' ?'text':''):''" :class="{textarea:item.type === 'macro'&&item.macroType === 'textarea' || item.type ==='textarea'}" v-if="item.isHidden=='false'">
            <view v-if="item.type ==='macro'">
              <view v-if="item.macroType === 'select'">
                <u-input v-model="item.value" type="select" input-align="left" :placeholder="`请输入${item.title}`" disabled />
              </view>
              <view v-else-if="item.macroType === 'textarea' && !item.macroCode">
                <u-input v-model="item.value" type="textarea" input-align="left" :placeholder="`请输入${item.title}`" :disabled="item.editable=='true'" />
              </view>
              <view v-else-if="item.macroType === 'textarea' && item.macroCode=='macro_opinion'">
                <u-input v-model="item.value.opinionContent" type="textarea" input-align="left" :placeholder="`请输入${item.title}`" :disabled="item.editable=='true'" />
                <!-- <view style="text-align:right">
                  {{item.value.opinionName}} {{moment().format('YYYY-MM-DD HH:mm')}}
                </view>
                <view v-if="item.text &&Array.isArray(item.text) && item.text.length>0">
                  <view class="" v-for="(area,ar) in item.text" :key="ar">
                    <view style="font-weight:bold">
                      {{area.opinionContent}}
                    </view>
                    <view style="text-align:right">
                      {{area.opinionName}} {{area.opinionTime}}
                    </view>
                  </view>
                </view> -->
              </view>
              <view v-else-if="item.macroType === 'text'&&!item.macroCode">
                <u-input v-model="item.value" type="text" input-align="left" :placeholder="`请输入${item.title}`" :disabled="item.editable=='true'" />
              </view>
              <view v-else-if="item.macroType === 'text'&&item.macroCode=='nest_sheet'">
                <u-input v-model="item.value" type="text" input-align="left" :placeholder="`请输入${item.title}`" :disabled="item.editable=='true'" />
              </view>
              <view v-else-if="item.macroType === 'text'&&item.macroCode=='macro_current_user'">
                <u-input v-model="item.value" type="text" input-align="left" :placeholder="`请输入${item.title}`" :disabled="item.editable=='true'" />
              </view>
              <view v-else-if="item.macroType === 'ModuleFieldSelect'">
                <u-input v-model="item.value" type="text" input-align="left" :placeholder="`请输入${item.title}`" :disabled="item.editable=='true'" />
              </view>
            </view>
            <view v-else-if="item.type ==='text'&&!item.macroCode">
              <u-input v-model="item.value" type="text" input-align="left" :placeholder="`请输入${item.title}`" :disabled="item.editable=='true'" />
            </view>
            <view v-else-if="item.type ==='textarea'">
              <u-input v-model="item.value" type="textarea" input-align="left" :placeholder="`请输入${item.title}`" :disabled="item.editable=='true'" />
            </view>
            <view v-else-if="item.type ==='DATE_TIME'">
              <u-input v-model="item.value" type="text" input-align="left" :placeholder="`请输入${item.title}`" disabled @click="e => changeTime(e,item,index)" />
            </view>
          </uni-forms-item>
        </template>
        <!-- <template v-if="isNext">
          <uni-forms-item>
            下一步用户:
          </uni-forms-item>
          <uni-forms-item>
            <view class="form_bottom">
              <view class="form_bottom_list">
                <u-radio-group v-model="uesrValue" @change="radioGroupChange" :wrap="true">
                  <u-radio @change="radioChange" v-for="(uesr,u) in model.users" :key="u" :name="uesr.value" :disabled="uesr.isSelected==true">
                    {{uesr.actionTitle}}:{{uesr.value}}
                  </u-radio>
                </u-radio-group>
              </view>
              <view class="form_bottom_but">
                <u-row gutter="16">
                  <u-col span="3">
                    <u-button size="mini" type="primary" :custom-style="{width:'100%',backgroundColor:'#31E6E1'}" :hover-class="{backgroundColor:'#31E6E1'}">保存</u-button>
                  </u-col>
                  <u-col span="3">
                    <u-button size="mini" type="primary" :custom-style="{width:'100%',backgroundColor:'#31E6E1'}" :hover-class="{backgroundColor:'#31E6E1'}">同意</u-button>
                  </u-col>
                  <u-col span="3">
                    <u-button size="mini" type="primary" :custom-style="{width:'100%',backgroundColor:'#31E6E1'}" :hover-class="{backgroundColor:'#31E6E1'}">删除</u-button>
                  </u-col>
                  <u-col span="3">
                    <u-button size="mini" type="primary" :custom-style="{width:'100%',backgroundColor:'#31E6E1'}" :hover-class="{backgroundColor:'#31E6E1'}">照片</u-button>
                  </u-col>
                </u-row>
              </view>
            </view>
          </uni-forms-item>
        </template> -->

      </uni-forms>
    </view>
    <u-picker v-model="showTime" mode="time" :params="params" :defaultTime="defaultTime" @confirm="confirmTime"></u-picker>
    <!-- :rules="[{required: true,errorMessage: `${item.title}必填`}]",{validateFunction:(rule,value,data, callback) => {if(!value) {callback('请输入')} return true}} -->
    <!-- <uni-easyinput type="text" :disabled="item.editable" :inputBorder="false" :placeholder="`请输入${item.title}`" v-model="item.value" ></uni-easyinput> -->
  </view>
</template>
<script>
import { postAction } from "@/utils/request.js";
import UniForms from "@/components/uni-forms/uni-forms";
import UniFormsItem from "@/components/uni-forms-item/uni-forms-item";
import UniEasyinput from "@/components/uni-easyinput/uni-easyinput";
import moment from "moment";
export default {
  components: { UniForms, UniFormsItem, UniEasyinput },
  props: {
    isNext: {
      type: Boolean,
      default: true,
    },
    isDefaultSave: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      list: [
        {
          name: "待办",
        },
        {
          name: "过程",
        },
      ],
      curNow: 0,
      // fields: [
      //   {
      //     type: "text",
      //     editable: false,
      //     title: "域名",
      //     value: "12",
      //     code: "code",
      //     isNull: true,
      //     macroType: "",
      //   },
      //   {
      //     type: "macro",
      //     editable: false,
      //     title: "文本",
      //     value: "12",
      //     code: "code",
      //     isNull: false,
      //     macroType: "textarea",
      //   },
      // ],
      url: {
        queryById: "",
        dispose: "/mobile/flow/dispose",
      },
      webviewStyles: {
        progress: {
          color: "#FF3333",
        },
      },
      showTime: false,
      filedIndex: 0,
      params: {
        year: true,
        month: true,
        day: true,
        hour: true,
        minute: true,
        second: true,
        province: true,
        city: true,
        area: true,
        timestamp: true,
      },
      defaultTime: "",
      model: {},
      cwsWorkflowTitle: "", //表单标题
      uesrValue: "",
      fields: [],
    };
  },
  onLoad(option) {
    let record = JSON.parse(option.record);
    this.search(record);
  },
  methods: {
    moment,
    onBackPress() {
      this.$scope.$getAppWebView().children()[0].back();
    },
    sectionChange(index) {
      this.curNow = index;
    },
    search(record) {
      this.url.queryById =
        this.url.dispose + "?myActionId=" + record.myActionId;
      postAction(this.url.queryById).then((res) => {
        if (res.res == 0) {
          this.model = res.result;
          this.cwsWorkflowTitle = res.cwsWorkflowTitle;
          // this.fields = res.result.fields;
        }
      });
    },
    // 组件初始化
    initFields(fields) {
      this.$nextTick(() => {
        setTimeout(() => {
          this.fields = JSON.parse(JSON.stringify(fields));
        }, 200);
      });
    },
    //打开时间
    changeTime(e, item, index) {
      this.filedIndex = index;
      this.defaultTime = item.value;
      this.showTime = true;
      if (item.editable == "true") {
        this.showTime = true;
      }
    },
    //时间回调
    confirmTime(e) {
      let time =
        e.year +
        "-" +
        e.month +
        "-" +
        e.day +
        " " +
        e.hour +
        ":" +
        e.minute +
        ":" +
        e.second;
      this.fields[this.filedIndex].value = time;
      this.$forceUpdate();
    },
    // 选中某个单选框时，由radio时触发
    radioChange(e) {},
    // 选中任一radio时，由radio-group触发
    radioGroupChange(e) {},
    handleOk(val) {
      let isNullList = this.fields.filter(
        (item) =>
          item.isNull == false && !item.value && item.isHidden == "false"
      );
      if (isNullList.length > 0) {
        uni.showToast({
          icon: "none",
          mask: false,
          title: isNullList[0].title + "必填",
        });
        return;
      } else {
        // uni.showToast({
        //   icon: "none",
        //   mask: true,
        //   title: "通过",
        // });
        if (this.isDefaultSave) {
          //TO DO
        } else {
          this.$emit("handleOk", this.fields, val);
        }
      }
      //   for (let item in obj) {
      //     return obj[item].errors[0].message;
      //   }
      //   this.$refs["form"].validate((err, value) => {
      //     // 如果校验成功 ，err 返回 null
      //     if (!err) {
      //       return;
      //     }
      //   });
    },
    isErrors(obj) {
      if (Object.keys(obj).length > 0) {
        for (let item in obj) {
          return obj[item].errors[0].message;
        }
      }
    },
  },
};
</script>
<style lang="scss" scoped>
.pageStyle {
  .subsection_top {
    padding: 20rpx;
    background-color: #f4f4f4;
  }
  .form_conter {
    padding: 20rpx;
    background-color: #ffffff;
    .form_top {
    }
    /deep/ .uni-forms-item {
      border-bottom: 1px #eee solid;
    }
    /deep/ .uni-forms-item__inner {
      padding: 20rpx 0;
    }
    .textarea {
      /deep/ .uni-forms-item__inner {
        display: flex;
        flex-direction: column !important;
        .uni-textarea-textarea {
          // background-color: #eee;
        }
      }
    }
    .form_bottom {
      .form_bottom_list {
        padding-left: 30rpx;
      }
      .form_bottom_but {
        margin-top: 20rpx;
        padding-left: 50rpx;
      }
    }
  }
}
</style>