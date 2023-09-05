<template>
  <div class="h-full">
    <Carousel arrows autoplay :class="`carousel-${carouselPictureId}`">
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
      <div v-for="item in dataSource" :key="item.id" class="slid-img-box">
        <img
          @click="handleClick(item)"
          :src="item.buf"
          class="flex items-center w-full h-3/4 cursor-pointer slide-img"
        /><h3 style="margin-top: -65px">
          {{ item.title }}
        </h3></div
      >

      <!-- <div><h3>1</h3></div>
      <div><h3>2</h3></div>
      <div><h3>3</h3></div>
      <div><h3>4</h3></div> -->
    </Carousel>
    <SmartModuleDrawer @register="registerSmartModuleDrawer" @edit-action="handleOpenEditDrawer" />
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch, onMounted } from 'vue';
  import { Empty, Carousel } from 'ant-design-vue';
  import { LeftCircleOutlined, RightCircleOutlined } from '@ant-design/icons-vue';
  import SmartModuleDrawer from '../../../pages/smartModule/modules/smartModuleDrawer.vue';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { getCarouselPictureInfo, getShowImg } from '/@/api/system/system';
  import { useUserStore } from '/@/store/modules/user';
  import { useDrawer } from '/@/components/Drawer';
  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
  const userStore = useUserStore();

  const props = defineProps({
    loading: Boolean,
    width: {
      type: String as PropType<string>,
      default: '100%',
    },
    height: {
      type: String as PropType<string>,
      default: '300px',
    },
    icon: {
      type: String as PropType<string>,
      default: '',
    },
    pageSize: {
      type: Number,
      default: 6,
    },
    carouselPictureId: {
      type: Number,
      default: 6,
    },
    cardData: {
      type: Object as PropType<object>,
      default: () => {
        return {};
      },
    },
  });

  const [registerSmartModuleDrawer, { openDrawer: openSmartModuleDrawer }] = useDrawer();
  function openSmartModuleDrawerForShow(moduleCode, id, visitKey) {
    let params = {
      moduleCode,
      id,
      visitKey,
    };
    openSmartModuleDrawer(true, {
      isUpdate: 3,
      record: {
        ...params,
      },
    });
  }

  function handleOpenEditDrawer(record: object) {
    console.log('handleOpenEditDrawer record', record);
    openSmartModuleDrawer(true, {
      isUpdate: 2,
      record: {
        moduleCode: 'document',
        id: record['id'],
      },
    });
  }

  const baseUrl = ref(userStore.getServerUrl);
  const isLoading = ref(false);
  const dataSource = ref<Recordable>([]);
  const fetch = async () => {
    isLoading.value = true;
    let res = await getCarouselPictureInfo({
      id: props.carouselPictureId,
      rowCount: props.pageSize,
    });
    isLoading.value = false;

    // 转成前端显示的图片
    for (let k in res) {
      let item = res[k];
      item.buf = `${baseUrl.value}/${item.path}`;
      let showImgStr = 'showImg?path=';
      let p = item.path.indexOf(showImgStr);
      if (p != -1) {
        let path = item.path.substring(p + showImgStr.length);
        console.log('path', path);
        let res2 = await getShowImg({ path: path });
        item.buf = bufToUrl(res2)!;
        console.log('res2', res2, 'item.buf', item.buf);
      }
    }

    dataSource.value = res;

    console.log('props.height', props.height, 'dataSource.value', dataSource.value);
    setTimeout(() => {
      // 注意找限定选择器的范围，否则可能会跟取其它跑马灯的slick-list高度，如果fileark.vue中的carousel
      let cls = `.carousel-${props.carouselPictureId} .slick-list`;
      $('.slide-img').height($(cls).height());
    }, 200);
  };

  watch(
    () => props.pageSize,
    () => {
      fetch();
    },
    {
      immediate: true,
    },
  );

  const handleClick = (item: any) => {
    openSmartModuleDrawerForShow('document', item.docId, '');
  };

  onMounted(() => {});

  // watch(
  //   () => props.loading,
  //   () => {
  //     if (props.loading) {
  //       return;
  //     }
  //   },
  //   { immediate: true },
  // );
</script>
<style scoped>
  /* For demo */
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
    background: #364d79;
    overflow: hidden;
  }
  .ant-carousel :deep(.slick-arrow.custom-slick-arrow) {
    width: 25px;
    height: 25px;
    font-size: 25px;
    color: #fff;
    background-color: rgba(31, 45, 61, 0.11);
    opacity: 0.3;
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
</style>
