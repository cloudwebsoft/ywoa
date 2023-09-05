<template>
  <BasicModal
    :footer="null"
    title="请点选位置"
    v-bind="$attrs"
    :class="prefixCls"
    @register="register"
    :width="760"
  >
    <div class="h-400px flex flex-col justify-center">
      <Row class="w-full h-370px" style="overflow-y: auto">
        <Col :span="isShow ? 24 : 18"><div id="container" class="w-full h-full"></div> </Col>
        <Col :span="6" v-show="!isShow"
          ><div id="address" style="border: 1px solid #eee" class="h-full">
            <Input :value="curAddr" class="w-full" placeholder="请选择位置" readonly />
            <div
              v-for="(item, index) in items"
              :key="item.uid"
              :title="item.address"
              class="pl-2 cursor-pointer"
              @click="handleClick(item)"
            >
              {{ item.title }}</div
            >
          </div>
        </Col>
      </Row>
      <div :class="`${prefixCls}__footer mt-5`" v-if="!isShow">
        <a-button type="primary" size="middle" @click="handleOk"> 确定 </a-button>
        <a-button type="primary" size="middle" class="ml-2" @click="handleCancel"> 取消 </a-button>
      </div>
      <div :class="`${prefixCls}__footer mt-5`" v-else>
        <a-button type="primary" size="middle" class="ml-2" @click="handleCancel"> 关闭 </a-button>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { Row, Col, Input } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicModal, useModalInner } from '/@/components/Modal/index';
  const { createMessage } = useMessage();

  export default defineComponent({
    name: 'LocationMarkModal',
    components: { BasicModal, Row, Col, Input },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { prefixCls } = useDesign('header-lock-modal');
      const items = ref([]);
      const curAddr = ref('');
      const isShow = ref(true);
      const title = ref('');
      let map;

      const [register, { closeModal }] = useModalInner(async (data) => {
        console.log('data', data);
        map = new BMap.Map('container'); // 创建地图实例
        let lng = 116.331398;
        let lat = 39.897445;
        let maps = data.maps;
        console.log('maps', maps);
        isShow.value = data.isShow;
        if (maps) {
          let mapArr = maps.split(',');
          console.log('mapArr', mapArr);
          if (mapArr != null && mapArr.length == 3) {
            lng = mapArr[0];
            lat = mapArr[1];
            curAddr.value = mapArr[2];
          }
        }

        let point = new BMap.Point(lng, lat); // 创建点坐标
        map.centerAndZoom(point, 16); // 初始化地图，设置中心点坐标和地图级别
        map.addOverlay(new BMap.Marker(point));

        map.addControl(new BMap.NavigationControl());
        map.addControl(new BMap.ScaleControl());
        map.addControl(new BMap.OverviewMapControl()); // 缩略地图
        map.addControl(new BMap.GeolocationControl()); // 定位
        // map.addControl(new BMap.MapTypeControl());
        // map.setCurrentCity("北京"); // 仅当设置城市信息时，MapTypeControl的切换功能才能可用
        if (!isShow) {
          map.setDefaultCursor('crosshair');
        }

        // 开启滚轮缩放地图
        map.enableScrollWheelZoom();

        if (!isShow.value) {
          title.value = '请点选位置';

          // 进行浏览器定位
          let geolocation = new BMap.Geolocation();
          geolocation.getCurrentPosition(
            function (r) {
              // 定位成功事件
              if (this.getStatus() == BMAP_STATUS_SUCCESS) {
                //alert('您的位置：'+r.point.lng+','+r.point.lat);
                let point = new BMap.Point(r.point.lng, +r.point.lat);
                map.centerAndZoom(point, 16);
                map.addOverlay(new BMap.Marker(point));
              }
            },
            { enableHighAccuracy: true },
          );

          map.addEventListener('click', function (e) {
            // var myIcon = new BMap.Icon(
            //   'http://api.map.baidu.com/img/markers.png',
            //   new BMap.Size(23, 25),
            //   {
            //     offset: new BMap.Size(10, 25), // 指定定位位置
            //     imageOffset: new BMap.Size(0, 0 - 10 * 25), // 设置图片偏移
            //   },
            // );
            // var marker = new BMap.Marker(e.point, { icon: myIcon });
            // map.clearOverlays();
            // map.addOverlay(marker);

            // 设置标注的图标
            // 创建图标
            // let icon = new BMap.Icon(
            //   'http://api.map.baidu.com/img/markers.png',
            //   new BMap.Size(20, 25),
            //   {
            //     // imageOffset: new BMap.Size(0, 0 - 10 * 30), // 设置图片偏移
            //   },
            // );

            // let icon = new BMap.Icon(
            //   'http://api.map.baidu.com/img/markers.png',
            //   new BMap.Size(8, 10),
            //   {
            //     // offset: new BMap.Size(2, 2), // 相当于CSS精灵
            //     // anchor: new BMap.Size(10, 30), // anchor设置的是定位点距离图片左上角的偏移量。如果设置anchor参数的话，API会自动获取图片中心点作为anchor位置
            //     imageOffset: new BMap.Size(0, 0 - 10 * 10), // 使对准坐标点，Size(0, 0) 可使图片底部中心对准坐标点
            //   },
            // );

            // var icon = new BMap.Icon(
            //   'http://webmap0.map.bdstatic.com/wolfman/static/common/images/us_cursor_9517a2b.png', // 百度图片
            //   new BMap.Size(10, 22), // 视窗大小
            //   {
            //     imageSize: new BMap.Size(144, 92), // 引用图片实际大小
            //     imageOffset: new BMap.Size(-10, 0), // 图片相对视窗的偏移
            //   },
            // );

            // //设置标注的经纬度
            // let marker = new BMap.Marker(e.point, { icon: icon });
            // marker.setTop(true);
            // map.clearOverlays();
            // map.addOverlay(marker);

            map.clearOverlays();
            map.addOverlay(new BMap.Marker(e.point));
            displayPOI(e.point);
          });
        } else {
          title.value = '查看位置';
        }
      });

      let mOption = {
        poiRadius: 500, // 半径为500米内的POI,默认100米
        numPois: 12, // 列举出12个POI,默认10个
      };

      if (typeof BMap == 'undefined') {
        // createMessage.warn('百度地图对象BMap未定义, 请检查网络联接');
        console.error('百度地图对象BMap未定义, 请检查网络联接');
        return;
      }

      let myGeo = new BMap.Geocoder(); //创建地址解析实例

      function displayPOI(mPoint) {
        // map.addOverlay(new BMap.Circle(mPoint, 500)); //添加一个圆形覆盖物
        myGeo.getLocation(
          mPoint,
          function mCallback(rs) {
            // console.log('rs.surroundingPois', rs.surroundingPois);
            items.value = rs.surroundingPois; //获取全部POI（该点半径为100米内有6个POI点）
          },
          mOption,
        );
      }

      let curItem = {};
      function handleClick(item) {
        console.log('handleClick item', item);
        curAddr.value = item.address;
        curItem = item;
      }

      async function handleOk() {
        if (!curAddr.value) {
          createMessage.warn('请选择位置');
          return;
        }
        closeModal();
        emit('success', curItem);
      }

      async function handleCancel() {
        curAddr.value = '';
        closeModal();
      }

      return {
        t,
        prefixCls,
        register,
        handleOk,
        handleCancel,
        items,
        handleClick,
        curAddr,
        isShow,
        title,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-lock-modal';

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

      &-name {
        margin-top: 5px;
      }
    }

    &__footer {
      text-align: center;
    }
  }

  .BMap_Marker img {
    max-width: max-content;
  }
</style>
