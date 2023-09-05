<template>
  <div>
    <ShareCard :loading="loading" :cardData="cardData">
      <template #title v-if="dirCode?.length == 1">
        <div @click="handleGo()">
          <Icon :icon="icon" v-if="icon" :size="26" class="mr-2" />{{ dirName }}</div
        >
      </template>
      <Tabs v-if="dirCode?.length != 1" v-model:activeKey="key">
        <TabPane :key="item.code" v-for="item in tabList">
          <template #tab>
            <div @mouseenter="onMousemove(item)" @click="onTabClick(item.code)">{{
              item.name
            }}</div>
          </template>
        </TabPane>
      </Tabs>
      <Carousel :autoplay="true" :dots="true" arrows v-if="isShowDirImage == '2'">
        <div v-for="(item, index) in imageList" :key="index" class="carousel-box">
          <img
            class="cursor-pointer carousel-box-dir"
            :src="`${baseUrl}/${item.path}`"
            alt=""
            @click="handleClickImage(item)"
          />
        </div>
      </Carousel>
      <Row
        class="mt-1 pt-1 pb-2 pl-1 pr-3 w-full cursor-pointer"
        justify="space-between"
        v-if="isShowDirImage == '1' && topId != -1"
        @click="handleClickTop()"
      >
        <Col :span="8">
          <img :src="topImage" alt="" style="width: 100%; height: 60px" />
        </Col>
        <Col :span="15" class="text-left width-over-two">
          <strong>{{ topTitle }}</strong>
        </Col>
      </Row>
      <template v-if="dataSource?.length > 0">
        <div v-for="item in dataSource" :key="item.id" class="flex justify-between mb-2">
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-2/3 cursor-pointer"
            :title="item['title']"
            @click="handleClick(item)"
          >
            <Tooltip :title="item['title']">
              <span
                :style="[
                  { color: item['color'] },
                  { fontWeight: item['is_bold'] == 1 ? 'bold' : 'normal' },
                ]"
              >
                {{ item['title'] }}
              </span>
            </Tooltip>
          </div>
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-1/3 text-right"
            v-if="item['createDate']"
          >
            <Tooltip :title="item['createDate']">
              {{ item['createDate'] }}
            </Tooltip>
          </div>
        </div>
      </template>
      <template v-else>
        <Empty :image="simpleImage" />
      </template>
    </ShareCard>
    <SmartModuleDrawer @register="registerSmartModuleDrawer" @edit-action="handleOpenEditDrawer" />
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { Card, Tabs, TabPane, Empty, Tooltip, Row, Col, Carousel } from 'ant-design-vue';
  import { getDirNames, getListDoc, listImageByDirCode } from '/@/api/system/system';
  import SmartModuleDrawer from '../../../pages/smartModule/modules/smartModuleDrawer.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { useDebounceFn } from '@vueuse/core';
  import { getShowImg } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { useUserStore } from '/@/store/modules/user';
  import ShareCard from './ShareCard.vue';

  const userStore = useUserStore();
  const go = useGo();
  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
  const baseUrl = ref(userStore.getServerUrl);

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
    dirCode: {
      type: [Array, String] as PropType<string[]>,
      default: () => [],
    },
    pageSize: {
      type: Number,
      default: 6,
    },
    isShowDirImage: {
      type: String as PropType<string>,
      default: '1',
    },
    cardData: {
      type: Object as PropType<object>,
      default: () => {
        return {};
      },
    },
  });
  const key = ref('');
  const tabList = ref<Recordable>([]);
  const dataSource = ref<Recordable>([]);
  const dirName = ref('');
  const topImage = ref<any>('');
  const topId = ref(-1);
  const topTitle = ref('');
  const imageList = ref([]);
  const onTabClick = (key) => {
    handleGo();
  };
  const fetch = () => {
    getDirNames({ dirCodes: props.dirCode?.join(',') })
      .then((res) => {
        tabList.value = res || [];
        key.value = tabList.value[0]?.code;
        dirName.value = tabList.value[0]?.name;
        getLis();
      })
      .then(() => {
        if (props.isShowDirImage == '2') {
          listImageByDirCode({ dirCode: key.value }).then((data) => {
            imageList.value = data;
          });
        }
      });
  };
  const getLis = () => {
    getListDoc({ dirCode: key.value, pageSize: props.pageSize })
      .then((res) => {
        dataSource.value = res?.list || [];
        topId.value = res?.topId || -1;
        console.log('topId', topId.value);
        if (topId.value != -1) {
          topTitle.value = res?.topTitle || '';
          return getShowImg({ path: res.topImage });
        }
      })
      .then((res2) => {
        topImage.value = bufToUrl(res2)!;
      });
  };

  const handleGo = () => {
    // go('/fileark/fileark_dir');

    // go({
    //   path: '/smartModulePage',
    //   query: { isTreeView: true, treeNodeCode: props.dirCode[0] },
    // });

    go({
      path: '/smartModuleTreeViewPage',
      query: {
        treeBasicCode: 'fileark_dir',
        moduleCode: 'document',
        treeNodeCode: props.dirCode[0],
      },
    });
  };

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

  const handleClickImage = (item: any) => {
    openSmartModuleDrawerForShow('document', item.docId, '');
  };

  const handleClick = (item: any) => {
    openSmartModuleDrawerForShow('document', item.id, '');
  };

  const handleClickTop = () => {
    openSmartModuleDrawerForShow('document', topId, '');
  };

  // 防抖，延迟500ms执行
  const debounceFresh = useDebounceFn(getLis, 500);
  //鼠标移动事件
  const onMousemove = (item: any) => {
    //如果值一样不执行
    if (key.value === item.code) return;
    key.value = item.code;
    debounceFresh();
  };

  watch(
    () => props.dirCode,
    () => {
      if (props.dirCode && props.dirCode.length > 0) {
        fetch();
      }
    },
    { immediate: true },
  );
</script>
<style lang="less" scoped>
  .ant-carousel :deep(.slick-dots-bottom) {
    bottom: 0px;
  }
  .carousel-box {
    &-dir {
      width: 100%;
      height: 80px;
    }
  }
</style>
