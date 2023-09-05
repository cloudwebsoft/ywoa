<template>
  <div class="cus-pop">
    <div class="w-32.8px h-25.8px cursor-pointer" style="line-height: 25.8px" v-if="isUploadPanel">
      <Tooltip title="上传进度">
        <template v-if="type === 'drawer'">
          <VerticalAlignTopOutlined v-if="!isUploadFile" @click="handleClick" />
          <VerticalAlignTopOutlined
            v-if="isUploadFile"
            v-show="isShowIcon"
            style="color: #1890ff"
            @click="handleClick"
          />
        </template>
        <template v-if="type === 'tab'">
          <Icon
            icon="material-symbols:arrow-circle-up"
            :size="26"
            v-if="!isUploadFile"
            style="color: #000; opacity: 0.5"
            @mouseenter="handleMouseenter"
            @mouseleave="handleMouseleave"
            @click="handleClick"
          />
          <Icon
            icon="material-symbols:arrow-circle-up"
            :size="26"
            v-if="isUploadFile"
            v-show="isShowIcon"
            style="color: #1890ff"
            @mouseenter="handleMouseenter"
            @mouseleave="handleMouseleave"
            @click="handleClick"
          />
        </template>
      </Tooltip>
    </div>
    <div
      class="content"
      :class="{ 'c-right': type === 'drawer', 'c-right-suspension': type === 'tab' }"
      v-show="visible"
      ref="cusContentRef"
      @mousedown="handleMousedown"
    >
      <UploadFileList @close="visible = false" />
    </div>
    <!-- draggable="true" -->
    <!-- <Popover placement="bottomRight" v-model:visible="visible" overlayClassName="cus-pop">
    <template #content>
      <div :class="[prefixCls]">
        <UploadFileList />
      </div>
    </template>
    <div class="w-32.8px h-16.8px cursor-pointer" v-if="fileList && fileList.length">
      <Icon icon="material-symbols:arrow-circle-up" :size="26" v-if="!isUploadFile" />
      <Icon
        icon="material-symbols:arrow-circle-up"
        :size="26"
        v-if="isUploadFile"
        v-show="isShowIcon"
        style="color: #1890ff"
      />
    </div>
  </Popover> -->
  </div>
</template>
<script lang="ts">
  import { Popover, Tooltip } from 'ant-design-vue';
  import { VerticalAlignTopOutlined, DragOutlined } from '@ant-design/icons-vue';

  import { defineComponent, computed, ref, watch } from 'vue';

  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';

  import UploadFileList from './uploadFileList.vue';
  import { useUploadFileStore } from '/@/store/modules/uploadFile';
  import { Icon } from '/@/components/Icon';
  export default defineComponent({
    name: 'UploadFileView',
    components: {
      Popover,
      UploadFileList,
      VerticalAlignTopOutlined,
      DragOutlined,
      Tooltip,
      Icon,
    },
    props: {
      type: {
        type: String,
        default: 'drawer',
      },
    },
    setup() {
      const { prefixCls } = useDesign('header-upload-file-list');
      const { t } = useI18n();
      const userStore = useUserStore();

      const getUserInfo = computed(() => {
        const { realName = '', avatar, desc, gender } = userStore.getUserInfo || {};
        return { realName, avatar: avatar || gender ? womanImg : manImg, desc };
      });

      const uploadFileStore = useUploadFileStore();

      const serverInfo = userStore.getServerInfo;
      const isUploadPanel = ref(true);
      console.log('serverInfo', serverInfo);
      isUploadPanel.value = serverInfo.isUploadPanel;

      const fileList = computed(() => {
        return uploadFileStore.getUploadFileList;
      });
      const isUploadFile = computed(() => {
        const bool = uploadFileStore.getUploadFileList.some((item) => item.progress != 100);
        return bool;
      });
      const isShowIcon = ref(true);
      const times = ref(0);
      watch(isUploadFile, (newVal) => {
        if (newVal) {
          times.value = setInterval(() => {
            isShowIcon.value = !isShowIcon.value;
          }, 500);
        } else {
          isShowIcon.value = true;
          clearInterval(times.value);
        }
      });
      const isSelectFileAfter = computed(() => uploadFileStore.isSelectFileAfter);
      const visible = ref(false);

      watch(fileList, (newVal) => {
        if (newVal.length == 0) {
          visible.value = false;
        }
      });
      watch(isSelectFileAfter, (newVal) => {
        console.log('newVal==》', newVal);
        if (newVal) {
          visible.value = true;
          uploadFileStore.setIsSelectFileAfter(false);
        }
      });
      const handleMouseenter = () => {
        // visible.value = true;
      };
      const handleMouseleave = () => {
        // visible.value = false;
      };
      const handleClick = () => {
        visible.value = !visible.value;
        console.log('visible.value', visible.value);
      };
      const cusContentRef = ref<HTMLElement | null>(null);
      const getStyle = (dom: any, attr: any) => {
        return getComputedStyle(dom)[attr];
      };
      const handleMousedown = (e) => {
        // 鼠标按下，计算当前元素距离可视区的距离
        const disX = e.clientX;
        const disY = e.clientY;
        const screenWidth = document.body.clientWidth; // body当前宽度
        const screenHeight = document.documentElement.clientHeight; // 可见区域高度(应为body高度，可某些环境下无法获取)

        const dragDomWidth = cusContentRef.value.offsetWidth; // 对话框宽度
        const dragDomheight = cusContentRef.value.offsetHeight; // 对话框高度

        const minDragDomLeft = cusContentRef.value.offsetLeft;

        const maxDragDomLeft = screenWidth - cusContentRef.value.offsetLeft - dragDomWidth;
        const minDragDomTop = cusContentRef.value.offsetTop;
        const maxDragDomTop = screenHeight - cusContentRef.value.offsetTop - dragDomheight;
        // 获取到的值带px 正则匹配替换
        const domLeft = getStyle(cusContentRef.value, 'left');
        const domTop = getStyle(cusContentRef.value, 'top');
        let styL = +domLeft;
        let styT = +domTop;

        // 注意在ie中 第一次获取到的值为组件自带50% 移动之后赋值为px
        if (domLeft.includes('%')) {
          styL = +document.body.clientWidth * (+domLeft.replace(/%/g, '') / 100);
          styT = +document.body.clientHeight * (+domTop.replace(/%/g, '') / 100);
        } else {
          styL = +domLeft.replace(/px/g, '');
          styT = +domTop.replace(/px/g, '');
        }

        document.onmousemove = function (e) {
          // 通过事件委托，计算移动的距离
          let left = e.clientX - disX;
          let top = e.clientY - disY;

          // 边界处理
          if (-left > minDragDomLeft) {
            left = -minDragDomLeft;
          } else if (left > maxDragDomLeft) {
            left = maxDragDomLeft;
          }

          if (-top > minDragDomTop) {
            top = -minDragDomTop;
          } else if (top > maxDragDomTop) {
            top = maxDragDomTop;
          }

          // 移动当前元素
          // cusContentRef.value.removeAttribute('right');
          // inherit
          cusContentRef.value.classList.remove('c-right');
          cusContentRef.value.classList.remove('c-right-suspension');

          cusContentRef.value.style.cssText += `;left:${left + styL}px;top:${top + styT}px;`;
        };

        document.onmouseup = () => {
          document.onmousemove = null;
          document.onmouseup = null;
        };
      };
      return {
        prefixCls,
        t,
        getUserInfo,
        fileList,
        visible,
        isUploadFile,
        isShowIcon,
        handleMouseenter,
        handleMouseleave,
        handleClick,
        cusContentRef,
        handleMousedown,
        isUploadPanel,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-upload-file-list';

  .@{prefix-cls} {
    min-height: 100px;
    overflow: hidden;
    min-width: 200px;
    cursor: pointer;
    align-items: center;
  }
  .cus-pop {
    background: transparent;
    // .ant-popover-inner {
    //   background-color: rgba(255, 255, 255, 0.3);
    // }
    .ant-tree {
      background-color: transparent;
    }
    .content {
      z-index: 9999999;
      min-width: 300px;
      background-color: rgba(255, 255, 255, 0.8);
      position: fixed;
      // left: 84%;
      padding: 10px 5px;
      box-shadow: 0 3px 6px -4px #0000001f, 0 6px 16px #00000014, 0 9px 28px 8px #0000000d;
    }
    .c-right {
      right: 10px;
      top: 80px;
    }
    .c-right-suspension {
      right: 10px;
    }
  }
</style>
