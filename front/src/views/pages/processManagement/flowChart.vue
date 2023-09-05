<template>
  <div class="bg-white">
    <div id="myflow"></div>
    <!-- <div class="isPlay">
      <Button type="primary" @click="PlayDesigner">回放</Button>
    </div> -->
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, watchEffect, onMounted } from 'vue';
  import { ajaxGet, ajaxPost } from '/@/utils/utils';
  import { getFlowShowChart } from '/@/api/process/process';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Button } from 'ant-design-vue';
  export default defineComponent({
    components: { Button },
    props: {
      flowId: {
        type: [Number],
        required: false,
      },
      activeKey: {
        type: [String],
        required: false,
      },
      isFlowDebug: {
        type: Boolean,
        default: false,
      },
    },
    setup(props) {
      const flowData = ref({});
      let $flow;
      const {} = useMessage();

      watchEffect(() => {
        props.activeKey == '2' && props.flowId && fetch();
      });
      function fetch() {
        getFlowShowChart({ flowId: props.flowId, isFlowDebug: props.isFlowDebug }).then((res) => {
          flowData.value = res;
          $flow = $('#myflow').myflow({
            allowStateMultiLine: false,
            editable: false,
            textEllipsis: true, // 如果节点的text过长，是否显示省略号
            textMaxLen: 18, // 与ellipsis联用，当ellipsis为true时生效
            expireUnit: flowData.value.flowExpireUnit,
            licenseKey: flowData.value.licenseKey,
            // cloudUrl: flowData.value.cloudUrl,
            restore: eval('(' + flowData.value.flowJson + ')'),
            activeRects: {
              rects: flowData.value.activeActions ? flowData.value.activeActions : [],
            },
            finishRects: {
              rects: flowData.value.finishActions ? flowData.value.finishActions : [],
            },
            ignoreRects: {
              rects: flowData.value.ignoreActions ? flowData.value.ignoreActions : [],
            },
            discardRects: {
              rects: flowData.value.discardActions ? flowData.value.discardActions : [],
            },
            returnRects: {
              rects: flowData.value.returnActions ? flowData.value.returnActions : [],
            },
          });
        });
      }
      function PlayDesigner() {
        $flow.resetAllRectStatus();
        doPlayDesigner();
      }

      let playCount = 0;
      function doPlayDesigner() {
        let ary = new Array();
        let kk = 0;
        flowData.value.aryMyAction.forEach((json) => {
          let receiveTime = json.receiveTime;
          ary[kk] = [receiveTime, json.internalName, json.actionStatus]; // 到达
          kk++;
          let checkTime = json.checkTime;
          ary[kk] = [checkTime, json.internalName, json.checkStatus]; // 处理
          kk++;
        });
        // [# th:each="json,stat : ${aryMyAction}"]
        //     var receiveTime = "[(${json.receiveTime})]";
        //     ary[kk] = [receiveTime, "[(${json.internalName})]", "[(${json.actionStatus})]]"]; // 到达
        //     kk++;
        //     var checkTime = "[(${json.checkTime})]";
        //     ary[kk] = [checkTime, "[(${json.internalName})]", "[(${json.checkStatus})]"]; // 处理
        //     kk++;
        // [/]

        if (playCount == 0) {
          // 对ary中的元素按照时间排序
          ary.sort(function (a, b) {
            return parseInt(a[0]) - parseInt(b[0]);
          });
        }

        let rectId = ary[playCount][1];
        let status = ary[playCount][2];
        if (status == flowData.value.STATE_DOING) {
          status = 'active';
        } else if (status == flowData.value.STATE_FINISHED) {
          status = 'finish';
        } else if (status == flowData.value.STATE_RETURN) {
          status = 'return';
        } else if (status == flowData.value.STATE_IGNORED) {
          status = 'ignore';
        } else if (status == flowData.value.STATE_DISCARDED) {
          status = 'discard';
        }

        $flow.setRectStatus(rectId, status);

        playCount++;

        if (playCount == ary.length) {
          $.toaster({ priority: 'info', message: '[(#{endPlayback})]' });
          playCount = 0;
          return;
        }

        // timeoutid = window.setTimeout('doPlayDesigner()', '1000');
        timeoutid = setTimeout(() => {
          doPlayDesigner();
        }, 1000);
      }

      function handleSuccess() {}
      onMounted(() => {
        window.ajaxGet = ajaxGet;
        window.ajaxPost = ajaxPost;
      });
      return {
        handleSuccess,
        PlayDesigner,
      };
    },
  });
</script>
<style lang="less" scoped>
  @import '/@/assets/css/myflow.css';
  .bg-white {
    position: relative;
    .isPlay {
      position: absolute;
      top: 0;
      left: 50%;
    }
  }
</style>
