<template>
  <div>
    <BasicModal
      :footer="null"
      :bodyStyle="{ height: '320px' }"
      width="600px"
      v-bind="$attrs"
      :class="prefixCls"
      @register="register"
    >
      <template #title>
        <exclamation-circle-filled
          class="mr-2"
          style="color: rgba(239, 145, 72, 0.863); font-size: 18px"
        />
        通知
      </template>
      <div class="h-250px">
        <Carousel arrows autoplay class="carousel-notice">
          <template #prevArrow>
            <div class="custom-slick-arrow" style="left: 10px; z-index: 1">
              <left-circle-outlined />
            </div>
          </template>
          <template #nextArrow>
            <div class="custom-slick-arrow" style="right: 10px">
              <right-circle-outlined />
            </div>
          </template>
          <div v-for="item in noticeList" :key="item.id">
            <div class="cursor-pointer" @click="handleClick(item)">
              <div class="flex justify-center items-center h-10 text-base font-bold bg-gray-100">{{
                item.title
              }}</div>
              <div class="flex justify-center items-center mt-2"
                >发布者：{{ item.user.realName }} 发布日期：{{ item.createDate }}</div
              >
              <div class="p-2 text-left" v-html="item.content"></div>
            </div>
          </div>
        </Carousel>

        <div :class="`${prefixCls}__footer`">
          <!-- <a-button type="primary" size="middle" @click="handleClick(item)"> 确定 </a-button> -->
          <a-button type="primary" size="middle" class="ml-2" @click="handleCancel">
            关闭
          </a-button>
        </div>
      </div>
    </BasicModal>
    <NoticeInfoDrawer @register="registerInfoDrawer" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { Carousel } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicModal, useModalInner } from '/@/components/Modal/index';
  import {
    LeftCircleOutlined,
    RightCircleOutlined,
    ExclamationCircleFilled,
  } from '@ant-design/icons-vue';
  import NoticeInfoDrawer from './noticeInfoDrawer.vue';
  import { useDrawer } from '/@/components/Drawer';

  const { createMessage } = useMessage();

  export default defineComponent({
    name: 'NoticeModal',
    components: {
      BasicModal,
      Carousel,
      LeftCircleOutlined,
      RightCircleOutlined,
      NoticeInfoDrawer,
      ExclamationCircleFilled,
    },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { prefixCls } = useDesign('notice-modal');
      const noticeList = ref([]);

      const [register, { closeModal }] = useModalInner(async (data) => {
        noticeList.value = data;
        createMessage.warn('重要通知，请点击通知内容查看，查看后通知将不再弹出');
      });

      const [registerInfoDrawer, { openDrawer: openInfoDrawer }] = useDrawer();

      async function handleOk() {}

      async function handleCancel() {
        closeModal();
      }

      function handleClick(record) {
        openInfoDrawer(true, {
          isUpdate: true,
          record,
        });
        // 如果只有一条，则关闭对话框
        console.log('noticeList.value', noticeList.value, 'length', noticeList.value.length);
        if (noticeList.value.length == 1) {
          closeModal();
        } else {
          // 删除当前被点击的通知（删除后显示得不正确）
          // for (let k = 0; k < noticeList.value.length; k++) {
          //   console.log('noticeList.value[k].id', noticeList.value[k].id, 'record.id', record.id);
          //   if (noticeList.value[k].id == record.id) {
          //     noticeList.value.splice(k, 1);
          //     console.log('noticeList.value', noticeList.value);
          //     break;
          //   }
          // }
        }
      }

      return {
        t,
        prefixCls,
        register,
        handleOk,
        handleCancel,
        noticeList,
        handleClick,
        registerInfoDrawer,
      };
    },
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-notice-modal';
  .ant-carousel {
    height: 100%;
  }
  :deep(.slick-slider) {
    height: 100%;
  }
  :deep(.slick-list) {
    height: 100%;
  }
  :deep(.slick-track) {
    height: 100%;
    display: flex;
    align-items: center;
  }
  .ant-carousel :deep(.slick-slide) {
    text-align: center;
    height: 100%;
    // background: #364d79;
    overflow: hidden;
  }
  .ant-carousel :deep(.slick-arrow.custom-slick-arrow) {
    width: 25px;
    height: 25px;
    font-size: 25px;
    color: rgba(63, 77, 225, 0.863);
    // background-color: rgba(31, 45, 61, 0.11);
    opacity: 0.1;
    z-index: 1;
  }
  .ant-carousel :deep(.custom-slick-arrow:before) {
    display: none;
  }
  .ant-carousel :deep(.custom-slick-arrow:hover) {
    opacity: 0.5;
  }
  .ant-carousel :deep(.slick-slide h3) {
    color: #fff;
  }

  .@{prefix-cls} {
    &__entry {
      position: relative;
      //height: 240px;
      padding: 130px 30px 30px;
      border-radius: 10px;
    }

    &__header {
      position: absolute;
      top: 0;
      left: calc(50% - 45px);
      width: auto;
      text-align: center;

      &-img {
        width: 70px;
        border-radius: 50%;
      }

      &-name {
        margin-top: 5px;
      }
    }

    &__footer {
      text-align: center;
    }
  }
</style>
