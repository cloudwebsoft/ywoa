<template>
  <div
    class="overflow-auto h-full relative"
    ref="bigScreen"
    :style="{
      background: layoutRecord?.backgroundImge ? `url(${layoutRecord?.backgroundImge})` : '',
      backgroundRepeat: layoutRecord?.backgroundImge ? 'no-repeat' : 'none',
      backgroundSize: layoutRecord?.backgroundImge ? '100% 100%' : 'none',
      backgroundColor: layoutRecord?.backgroundColor,
    }"
  >
    <!-- :class="layoutList.length > 1 ? 'p-4' : 'pl-2 pr-2'" -->
    <!--如果长度为1，可能是只有1个智能模块，所以需调整padding-->
    <div class="pl-2.5 pr-2.5 pt-2.5">
      <CardView :portalId="myPortalId" ref="cardViewRef" />
    </div>
    <div
      :style="{
        width:
          layoutRecord?.widthType == 1
            ? `${layoutRecord?.screenWidth}px`
            : layoutRecord && layoutRecord?.widthType == 2
            ? `${layoutRecord?.screenWidth}%`
            : '100%',
      }"
    >
      <grid-layout
        v-if="layoutList.length > 0"
        v-model:layout="layoutList"
        :col-num="12"
        :row-height="30"
        :is-draggable="false"
        :is-resizable="false"
        :is-mirrored="false"
        :vertical-compact="true"
        :margin="[10, 10]"
        :use-css-transforms="true"
      >
        <grid-item
          v-for="(item, index) in layoutList"
          :key="`parent${index}`"
          drag-allow-from=".toolbox"
          v-bind="item"
          class="overflow-auto"
        >
          <template v-if="item?.item?.embedded">
            <SubPortal
              :children="item.item?.children"
              :boxRecord="{
                backgroundColor: layoutRecord?.backgroundColor,
                backgroundImge: layoutRecord?.backgroundImge,
                widthType: layoutRecord?.widthType,
                screenWidth: layoutRecord?.screenWidth,
              }"
              :portalId="myPortalId"
            />
          </template>
          <template v-else>
            <div
              class="h-full"
              :style="[
                {
                  background: item?.item?.boxBackgroundImge
                    ? `url(${item?.item?.boxBackgroundImge})`
                    : '',
                  backgroundRepeat: layoutRecord?.boxBackgroundImge ? 'no-repeat' : 'none',
                  backgroundSize: layoutRecord?.boxBackgroundImge ? '100% 100%' : 'none',
                  backgroundColor: item?.item?.boxBackgroundColor,
                  border: item?.item?.boxBorder ? `1px solid ${item?.item?.boxBorder}` : 'none',
                  borderImageSource:
                    item?.item?.showHorn == '1'
                      ? `radial-gradient(60% 75%, transparent 0px, transparent 100%, ${item?.item?.hornColor} 100%)`
                      : 'none',
                },
              ]"
              :class="{
                'box-horn': item?.item?.showHorn == '1',
              }"
            >
              <component
                :is="item?.item?.type"
                :pageSize="item?.item?.rowCount"
                :formCode="item?.item?.meta?.formCode"
                :chartId="item?.item?.meta?.chartId"
                :dirCode="item?.item?.meta?.dirCode"
                :carouselPictureId="item?.item?.meta?.carouselPictureId"
                :icon="item?.item?.icon"
                :leftField="item?.item?.meta?.leftField"
                :rightField="item?.item?.meta?.rightField"
                :isShowDirImage="item?.item?.meta?.isShowDirImage"
                :portalId="myPortalId"
                :cardId="item?.item?.meta?.cardId"
                :showSearch="item?.item?.meta?.showSearch == 1"
                :pagination="item?.item?.meta?.pagination == 1"
                :showToolbar="item?.item?.meta?.showToolbar == 1"
                :showOpCol="item?.item?.meta?.showOpCol == 1"
                :typeCode="item?.item?.meta?.typeCode"
                :header="item?.item?.title"
                :cardData="item?.item"
              />
            </div>
          </template>
        </grid-item>
      </grid-layout>
    </div>
    <NoticeModal @register="registerNoticeModal" @success="handleNoticeModalCallBack" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onActivated } from 'vue';
  import Card from './comps/Card.vue';
  import CardView from './comps/CardView.vue';
  import fileark from './comps/fileark.vue';
  import plan from './comps/plan.vue';
  import flow from './comps/flow.vue';
  import flowMine from './comps/flowMine.vue';
  import flowAttended from './comps/flowAttended.vue';
  import flowFavorite from './comps/flowFavorite.vue';
  import notice from './comps/notice.vue';
  import plan_calendar from './comps/plan_calendar.vue';
  import module from './comps/module.vue';
  import chart_bar from './comps/chart_bar.vue';
  import chart_line from './comps/chart_line.vue';
  import chart_gauge from './comps/chart_gauge.vue';
  import chart_funnel from './comps/chart_funnel.vue'; //漏斗图
  import chart_radar from './comps/chart_radar.vue'; //雷达图
  import chart_pie from './comps/chart_pie.vue';
  import CarouselPicture from './comps/CarouselPicture.vue';
  import moduleLight from './comps/moduleLight.vue';
  import ApplicationView from './comps/ApplicationView.vue';
  import NoticeModal from '../../pages/administrativeManagement/notice/noticeModal.vue';
  import SubPortal from './SubPortal.vue';
  import { Scrollbar } from '/@/components/Scrollbar';
  import { ScrollContainer } from '/@/components/Container';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getShowImg } from '/@/api/system/system';
  import { GridItem, GridLayout } from '/@/components/GridLayout';
  import '/@/components/GridLayout/style.css';
  import { whenever } from '@vueuse/core';

  export default defineComponent({
    name: 'Portal',
    components: {
      CardView,
      fileark,
      plan,
      flow,
      flowMine,
      flowAttended,
      flowFavorite,
      notice,
      plan_calendar,
      module,
      chart_bar,
      chart_line,
      chart_gauge,
      chart_funnel,
      chart_radar,
      chart_pie,
      CarouselPicture,
      moduleLight,
      ApplicationView,
      Card,
      SubPortal,
      Scrollbar,
      ScrollContainer,
      GridItem,
      GridLayout,
    },
  });
</script>
<script lang="ts" setup>
  import { ref, onMounted, watch, nextTick, onUnmounted } from 'vue';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { getSetup } from '/@/api/system/system';
  import { useRoute } from 'vue-router';
  import { useModal } from '/@/components/Modal';
  import { getNoticeListImportant } from '/@/api/administration/administration';
  import { useUserStore } from '/@/store/modules/user';
  import { useMultipleTabWithOutStore } from '/@/store/modules/multipleTab';
  import { isObject } from '/@/utils/is';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  import { useFullscreen } from '@vueuse/core';
  const bigScreen = ref<HTMLElement | null>(null);
  const { t } = useI18n();
  const loading = ref(true);
  const route = useRoute();
  const { setTitle } = useTabs();
  const myPortalId = ref(-1);
  let name = '首页';

  const props = defineProps({
    portalId: {
      type: Number,
      default: -1,
    },
  });
  const getLoading = ref(false);
  setTimeout(() => {
    loading.value = false;
  }, 1500);

  const layoutRecord = ref({});
  const layoutList = ref<Recordable>([]);
  const mainId = ref(0);
  const fetch = (portalId) => {
    getLoading.value = true;
    getSetup({ id: portalId })
      .then(async (res) => {
        mainId.value = res?.id;
        layoutRecord.value =
          res?.setup && isObject(JSON.parse(res.setup)) ? JSON.parse(res.setup) : {};
        if (layoutRecord.value && Object.keys(layoutRecord.value).length > 0) {
          if (layoutRecord.value['backgroundImge']) {
            await getShowImg({ path: layoutRecord.value['backgroundImge'] }).then(async (res) => {
              layoutRecord.value['backgroundImge'] = bufToUrl(res);
            });
          }

          layoutList.value = layoutRecord.value.layoutList || [];
          layoutList.value.forEach(async (o) => {
            if (o.item['boxBackgroundImge']) {
              await getShowImg({ path: o.item['boxBackgroundImge'] }).then(async (res) => {
                o.item['boxBackgroundImge'] = bufToUrl(res);
              });
            }
            if (o.item['titleBackgroundImge']) {
              await getShowImg({ path: o.item['titleBackgroundImge'] }).then(async (res) => {
                o.item['titleBackgroundImge'] = bufToUrl(res);
              });
            }
            if (o.item['children'] && o.item['children'].length > 0) {
              o.item['children'].forEach(async (n) => {
                if (n.item['boxBackgroundImge']) {
                  await getShowImg({ path: n.item['boxBackgroundImge'] }).then(async (res) => {
                    n.item['boxBackgroundImge'] = bufToUrl(res);
                  });
                }
                if (n.item['titleBackgroundImge']) {
                  await getShowImg({ path: n.item['titleBackgroundImge'] }).then(async (res) => {
                    n.item['titleBackgroundImge'] = bufToUrl(res);
                  });
                }
              });
            }
          });
          console.log('layoutRecord.value', layoutRecord.value);
        }
        console.log('layoutList.value', layoutList.value);
      })
      .finally(() => {
        getLoading.value = false;
      });
  };
  const cardViewRef = ref(null);
  const userStore = useUserStore();
  //全屏
  whenever(
    () => userStore.bigFullScreen,
    (newValue) => {
      const { toggle, isFullscreen } = useFullscreen(bigScreen.value);
      toggle().then(() => {
        userStore.setBigFullScreen(false);
      });
    },
  );

  onUnmounted(() => {
    console.log('卸载');
    userStore.setBigFullScreen(false);
  });
  const [registerNoticeModal, { openModal }] = useModal();

  onMounted(async () => {
    let query: any = route.query;
    myPortalId.value = query.id;

    if (!myPortalId.value) {
      let serverInfo = userStore.getServerInfo;
      myPortalId.value = serverInfo.portalId;
    } else {
      name = query.name || name;
    }

    // 从菜单进入时不直接加载卡片，而是在watch中加载卡片，以免重复加载
    if (!route.meta.formCode) {
      await nextTick();
      setTitle(name);
      setTimeout(() => {
        if (cardViewRef.value) {
          cardViewRef.value.fetch();
        }
      }, 10);
    }

    if (myPortalId.value != -1) {
      fetch(myPortalId.value);
    }

    getNoticeListImportant({}).then((data) => {
      if (data.length > 0) {
        openModal(true, data);
      }
    });
  });

  const multipleTabStore = useMultipleTabWithOutStore();
  onActivated(() => {
    // if (multipleTabStore.isRefreshPage) {
    //   fetch(myPortalId.value);
    //   multipleTabStore.updateIsRefreshPage(false);
    // }
  });

  async function handleNoticeModalCallBack(record) {}

  whenever(
    () => props.portalId,
    (newVal) => {
      myPortalId.value = newVal;
      fetch(myPortalId.value);

      setTimeout(() => {
        if (cardViewRef.value) {
          cardViewRef.value.fetch();
        }
      }, 10);
    },
    {
      deep: true,
    },
  );
</script>

<style lang="less" scoped>
  ::-webkit-scrollbar {
    height: 0;
  }

  :deep(.iconify) {
    color: #5d92c8;
  }
  // :deep(.scrollbar__view) {
  //   height: 100%;
  // }

  .box-horn {
    // background-color: transparent;
    // border-image-source: radial-gradient(60% 60%, transparent 0px, transparent 100%, cyan 100%);
    border-image-slice: 1 !important;
    border-width: 1px !important;
    border-style: solid !important;
    // border-image-outset: 1cm;
  }

  :deep(.vue-grid-item::-webkit-scrollbar) {
    width: 0;
  }
</style>
