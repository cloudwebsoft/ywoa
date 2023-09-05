<template>
  <view class="main">
    <!-- <view class="input" :style="disabled?'background-color:#f5f7fa':''">
      <input @click="showModal" v-model="_value" :style="disabled?'color:#c0c4cc':''" :placeholder="placeholder" disabled />
      <text v-if="clearable&&!disabled" @click="empty" class="selectIcon iconcross"></text>
    </view> -->
    <view class="select-modal" :class="isShowModal?'show':''" @tap="hideModal">
      <view class="select-dialog" @tap.stop="" :style="{backgroundColor:bgColor}">
        <view class="select-bar bg-white">
          <view class="action text-blue" @tap="cancelClick">{{cancelText}}</view>
          <view class="action text-green" @tap="confirmClick">{{confirmText}}</view>
        </view>
        <view class="select-content">
          <view class="select-item" v-for="(item,index) in list" :key="index" :style="valueIndexOf(item)?'color:'+selectColor+';background-color:'+selectBgColor+';':'color:'+color+';'" @click="select(item)">
            <view class="title">{{getLabelKeyValue(item)}}</view>
            <text class="selectIcon icongou" v-if="valueIndexOf(item)"></text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      isShowModal: false,
      selectValue: "",
    };
  },
  props: {
    value: {
      type: [Number, String, Array, Object],
      default: null,
    },
    placeholder: {
      // 占位符
      default: "",
      type: String,
    },
    multiple: {
      // 是否多选
      default: false,
      type: Boolean,
    },
    list: {
      default: () => [],
      type: Array,
    },
    valueKey: {
      // 指定list中valueKey的值作为下拉框绑定内容
      default: "value",
      type: String,
    },
    labelKey: {
      // 指定list中labelKey的值作为下拉框显示内容
      default: "label",
      type: String,
    },
    disabled: {
      default: false,
      type: Boolean,
    },
    clearable: {
      default: false,
      type: Boolean,
    },
    cancelText: {
      default: "取消",
      type: String,
    },
    confirmText: {
      default: "确定",
      type: String,
    },
    color: {
      default: "#000000",
      type: String,
    },
    selectColor: {
      default: "#0081ff",
      type: String,
    },
    bgColor: {
      default: "#ffffff",
      type: String,
    },
    selectBgColor: {
      default: "#fafafa",
      type: String,
    },
  },
  computed: {
    _value: {
      get() {
        console.log("selectValue", this.selectValue);
        return this.get_value(this.selectValue);
      },
      set(val) {
        this.$emit("change", val);
      },
    },
  },
  watch: {
    value: {
      handler(newValue, oldValue) {
        console.log("监听", this.selectValue, newValue, oldValue);
        if (this.multiple) {
          this.selectValue = newValue ? newValue.split(",") : [];
        } else {
          this.selectValue = newValue ? newValue : "";
        }
      },
      deep: true,
      immediate: true,
    },
  },
  created() {},
  methods: {
    get_value(val) {
      // 将数组值转换为以,隔开的字符串
      if (val || val === 0) {
        if (Array.isArray(val)) {
          let chooseAttr = [];
          val.forEach((item) => {
            let choose = this.list.find((temp) => {
              let val_val = this.getValueKeyValue(temp);
              return item === val_val;
            });
            chooseAttr.push(choose);
          });
          let values = chooseAttr
            .map((temp) => this.getLabelKeyValue(temp))
            .join(",");
          return values;
        } else {
          let choose = this.list.find((temp) => {
            let val_val = this.getValueKeyValue(temp);
            return val === val_val;
          });
          return this.getLabelKeyValue(choose);
        }
      } else {
        return "";
      }
    },
    select(item) {
      // 点击选项
      let val = this.getValueKeyValue(item);
      if (this.multiple) {
        let _value = this.selectValue;
        console.log("_value", _value);
        let index = _value.indexOf(val);
        if (index != -1) {
          _value.splice(index, 1);
          this.$emit("change", _value.join(","));
        } else {
          _value.push(val);
          this.$emit("change", _value.join(","));
        }
      } else {
        this.selectValue = val;
        let label = this.getLabelKeyValue(item);
        console.log(val, label);
        this.$emit("confirm", this.selectValue, label);
        this.$emit("change", val);
        this.hideModal();
      }
    },
    valueIndexOf(item) {
      let val = this.getValueKeyValue(item);
      if (Array.isArray(this.selectValue)) {
        return this.selectValue.indexOf(val) != -1;
      } else {
        return this.selectValue === val;
      }
    },
    getLabelKeyValue(item) {
      // 获取label
      return item[this.labelKey];
    },
    getValueKeyValue(item) {
      // 获取value
      return item[this.valueKey];
    },
    empty() {
      // 清空
      if (this.multiple) {
        this.$emit("change", []);
      } else {
        this.$emit("change", "");
      }
    },
    cancelClick() {
      // 点击取消
      if (this.multiple && Array.isArray(this.selectValue)) {
        this.$emit("cancel", this.selectValue.join(","), this._value);
      } else {
        this.$emit("cancel", this.selectValue, this._value);
      }
      console.log("cancel", this.selectValue, this._value);
      this.hideModal();
    },
    confirmClick() {
      // 点击确定
      if (this.multiple && Array.isArray(this.selectValue)) {
        this.$emit("confirm", this.selectValue.join(","), this._value);
      } else {
        this.$emit("confirm", this.selectValue, this._value);
      }
      this.hideModal();
    },
    showModal() {
      // 显示model
      if (!this.disabled) {
        this.isShowModal = true;
      }
    },
    hideModal() {
      // 隐藏model
      this.isShowModal = false;
    },
  },
};
</script>
<style>
@font-face {
  font-family: "selectIcon";
  src: url("//at.alicdn.com/t/font_1833441_ycfzdhg2u3.eot?t=1590375117208"); /* IE9 */
  src: url("//at.alicdn.com/t/font_1833441_ycfzdhg2u3.eot?t=1590375117208#iefix")
      format("embedded-opentype"),
    /* IE6-IE8 */
      url("data:application/x-font-woff2;charset=utf-8;base64,d09GMgABAAAAAAMEAAsAAAAABvQAAAK4AAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHEIGVgCDBgqBRIFCATYCJAMMCwgABCAFhQUHNRsfBsg+QCa3uoO0oAJTMwhxVu965keqWBy1hkbwtfzWb2Z279/shRhJisKF6FApKLI7oyBbpAaHo3w24k+ca9EUJbDmjaeznUdZ/FOUlkWdJ33rizZY/Pw6J5Xw0qKYxHTMesePHVT6EFpaC4zV70sKi2bYgNPc1w0WHnDVC/e/UnNTgyP+4Jq6BBpIHoisgypLaIAFEtU0wgeaIG8Yu4nAIZwnUK1QgFfOT6nUUoBpgXjj2lqplTMpiuXtCW3N2iK+aPTS2/Qdnzny8d+5IEiaDMy99exklra//FrKnX48pChmgrq5QcYRQCEe17ruqgqLAKv8WntwqwhpLms/nB5yW/iHRxJEC0QOgT3NnfgF01NBKvOuIzNoZdh5gJuAeGrsozE8vOJ7u5D832oz55039W5G+S52K0H+zNf1TJz07k26kqoQybRfwVFV4rjDS/K8EXUyuF1cXnT3weKS9Rvdm/xe7h8oA1hLwOR18R+Y4n4zwpr4z5SU089Vc+cpfWL+mn5APmT3Z39jeOs/GbWjK+DnmsuL/u6ehMX4j4yedSVkAUUuPh3TY022MtKZUEOtPqCb8Bkvnr5XT6imU0gGrEJW7aAL/gw0OhegVV2F6pC7uTOppirKIA4MFQhTrpCM+AbZlDu64L/QmAkQWlMhQXU75D07O9Gtl0PUYjTBLyAzOLNQYtypIEEjvsXtBLQTooV2nrQrGEau2gKmZlR4L8gwnGtBJbUn1diCOOQUnEkTkRAOeci9KHOQxvFro+tx3ZcGAaeljstCSBNDJuArgIyBYyy6OdZxAhHIELu1IC9AtgShCVtLltEKrSff1XoHJo3RC33hM63o3j6pSNkmqmIWEAtxFHB2OwoRBAfyeqE3r2ogHeF42dBhs7gvf7CukH5MmlUGOCpHihxFfs6TehDyKCqVAA==")
      format("woff2"),
    url("//at.alicdn.com/t/font_1833441_ycfzdhg2u3.woff?t=1590375117208")
      format("woff"),
    url("//at.alicdn.com/t/font_1833441_ycfzdhg2u3.ttf?t=1590375117208")
      format("truetype"),
    /* chrome, firefox, opera, Safari, Android, iOS 4.2+ */
      url("//at.alicdn.com/t/font_1833441_ycfzdhg2u3.svg?t=1590375117208#selectIcon")
      format("svg"); /* iOS 4.1- */
}

.selectIcon {
  font-family: "selectIcon" !important;
  font-size: 16px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.icongou:before {
  content: "\e61c";
}

.iconcross:before {
  content: "\e61a";
}
</style>
<style lang="scss" scoped>
.main {
  font-size: 28rpx;
}
.bg-white {
  background-color: #ffffff;
}
.text-blue {
  color: #0081ff;
}
.text-green {
  color: #39b54a;
}
.input {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  height: 60rpx;
  padding: 10rpx 20rpx;
  border-radius: 10rpx;
  border-style: solid;
  border-width: 1rpx;
  border-color: rgba(0, 0, 0, 0.1);
  input {
    flex: 1;
  }
}
.select-modal {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 9999;
  opacity: 0;
  outline: 0;
  text-align: center;
  -ms-transform: scale(1.185);
  transform: scale(1.185);
  backface-visibility: hidden;
  perspective: 2000rpx;
  background: rgba(0, 0, 0, 0.6);
  transition: all 0.3s ease-in-out 0s;
  pointer-events: none;
  margin-bottom: -1000rpx;
  &::before {
    content: "\200B";
    display: inline-block;
    height: 100%;
    vertical-align: bottom;
  }
  .select-dialog {
    position: relative;
    display: inline-block;
    margin-left: auto;
    margin-right: auto;
    // background-color: #f8f8f8;

    background-color: #ffffff;
    overflow: hidden;
    width: 100%;
    border-radius: 0;
    .select-content {
      // background-color: #F1F1F1;
      max-height: 420rpx;
      overflow: auto;
      .select-item {
        padding: 20rpx;
        display: flex;
        .title {
          flex: 1;
        }
      }
    }
  }
}
.select-modal.show {
  opacity: 1;
  transition-duration: 0.3s;
  -ms-transform: scale(1);
  transform: scale(1);
  overflow-x: hidden;
  overflow-y: auto;
  pointer-events: auto;
  margin-bottom: 0;
}
.select-bar {
  padding: 0 20rpx;
  display: flex;
  position: relative;
  align-items: center;
  min-height: 80rpx;
  justify-content: space-between;
  .action {
    display: flex;
    align-items: center;
    height: 100%;
    justify-content: center;
    max-width: 100%;
  }
}
</style>
