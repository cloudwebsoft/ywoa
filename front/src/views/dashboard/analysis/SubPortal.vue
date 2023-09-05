<template>
  <div
    :style="{
      background: layoutRecord?.backgroundImge ? `url(${layoutRecord?.backgroundImge})` : '',
      backgroundRepeat: layoutRecord?.backgroundImge ? 'no-repeat' : 'none',
      backgroundSize: layoutRecord?.backgroundImge ? '100% 100%' : 'none',
      backgroundColor: layoutRecord?.backgroundColor,
      width:
        layoutRecord?.widthType == 1
          ? `${layoutRecord?.screenWidth}px`
          : layoutRecord && layoutRecord?.widthType == 2
          ? `${layoutRecord?.screenWidth}%`
          : '100%',
    }"
  >
    <grid-layout
      :layout="layoutList"
      :col-num="12"
      :row-height="30"
      :is-draggable="false"
      :is-resizable="false"
      :is-mirrored="false"
      :vertical-compact="true"
      :margin="[5, 5]"
      :use-css-transforms="true"
    >
      <grid-item
        v-for="(item, index) in layoutList"
        :key="`child${index}`"
        drag-allow-from=".toolbox"
        v-bind="item"
        class="overflow-auto"
      >
        <div
          class="h-full"
          :style="[
            {
              border: item?.item?.boxBorder ? `1px solid ${item?.item?.boxBorder}` : 'none',
              background: item?.item?.boxBackgroundImge
                ? `url(${item?.item?.boxBackgroundImge})`
                : 'none',
              backgroundRepeat: layoutRecord?.backgroundImge ? 'no-repeat' : 'none',
              backgroundSize: layoutRecord?.backgroundImge ? '100% 100%' : 'none',
              backgroundColor: item?.item?.boxBackgroundColor,
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
            :portalId="portalId"
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
      </grid-item>
    </grid-layout>
  </div>
</template>
<script lang="ts">
  import { defineComponent, watchEffect, ref } from 'vue';
  import { GridItem, GridLayout } from '/@/components/GridLayout';
  import '/@/components/GridLayout/style.css';
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
  import { getShowImg } from '/@/api/system/system';
  import { bufToUrl } from '/@/utils/file/base64Conver';
  export default defineComponent({
    name: 'SubPortal',
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
      GridItem,
      GridLayout,
    },
    props: {
      children: {
        type: Array,
        default: () => [],
      },
      boxRecord: {
        type: Object,
        default: () => {
          return {};
        },
      },
      portalId: {
        type: Number,
        default: -1,
      },
    },
    setup(props) {
      const layoutRecord = ref({});
      const layoutList = ref([]);
      watchEffect(() => {
        console.log('props.children', props.children);
        layoutRecord.value = props.boxRecord || {};
        layoutList.value = props.children || [];
      });
      return {
        layoutList,
        layoutRecord,
      };
    },
  });
</script>

<style lang="less" scoped>
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
