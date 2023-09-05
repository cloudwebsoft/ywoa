<template>
  <div class="bg-white">
    <div id="myflowEdit"></div>
    <div id="myflow_tools" class="ui-widget-content">
      <div id="myflow_tools_handle" style="text-align: center" class="ui-widget-header"
        >工具条
      </div>
      <div class="node" id="myflow_redraw"
        ><img src="../../../../../../../../../assets/images/flow/create.png" />&nbsp;&nbsp;新建</div
      >
      <div class="node" id="myflow_save" style="display: none"
        ><img src="../../../../../../../../../assets/images/flow/save.png" />&nbsp;&nbsp;保存</div
      >
      <div class="node" id="myflow_revoke"
        ><img src="../../../../../../../../../assets/images/flow/undo.png" />&nbsp;&nbsp;撤销</div
      >
      <!-- <div class="node" onclick="window.location.reload()"
        ><img src="../../../../../../../assets/images/flow/refresh.png" />&nbsp;&nbsp;刷新</div
      > -->
      <div class="node" id="myflow_del"
        ><img src="../../../../../../../../../assets/images/flow/del.png" />&nbsp;&nbsp;删除</div
      >
      <div>
        <hr />
      </div>
      <div class="node selectable selected" id="pointer"
        ><img src="../../../../../../../../../assets/images/flow/select.png" />&nbsp;&nbsp;选择
      </div>
      <div class="node selectable" id="path"
        ><img src="../../../../../../../../../assets/images/flow/path.png" />&nbsp;&nbsp;连线
      </div>
      <div class="node selectable" id="pathReturn"
        ><img src="../../../../../../../../../assets/images/flow/return.png" />&nbsp;&nbsp;返回
      </div>
      <div>
        <hr />
      </div>
      <div class="node state" id="task" type="task"
        ><img src="../../../../../../../../../assets/images/flow/task.png" />&nbsp;&nbsp;任务
      </div>
    </div>
    <!-- <div class="isPlay">
      <Button type="primary" @click="PlayDesigner">回放</Button>
    </div> -->
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, watchEffect, onMounted, nextTick, watch, computed } from 'vue';
  import { ajaxGet, ajaxPost } from '/@/utils/utils';
  import { getAdminFlowGetFlowJson } from '/@/api/flowManage/flowManage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Button } from 'ant-design-vue';
  export default defineComponent({
    components: { Button },
    props: {
      typeCode: {
        type: [String, null],
        required: false,
      },
    },
    setup(props) {
      const flowData = ref({});
      let $flow;
      const {} = useMessage();
      // watchEffect(() => {
      //   props.typeCode && fetch();
      // });
      // watch(
      //   () => props.typeCode,
      //   (newVal) => {
      //     if (newVal) fetch();
      //   },
      //   {
      //     immediate: true,
      //   },
      // );
      function fetch() {
        getAdminFlowGetFlowJson({ typeCode: props.typeCode }).then((res) => {
          flowData.value = res;
          const id = '#myflowEdit';
          console.log('id', id);
          // $(`${id}`).myflow({
          //   editable: false,
          // });
          $(`${id}`).html('');
          $flow = $(`${id}`).myflow({
            allowStateMultiLine: false,
            editable: true,
            textEllipsis: true, // 如果节点的text过长，是否显示省略号
            textMaxLen: 18, // 与ellipsis联用，当ellipsis为true时生效
            // expireUnit: flowData.value.flowExpireUnit,
            // licenseKey: flowData.value.licenseKey,
            // cloudUrl: flowData.value.cloudUrl,
            restore: flowData.value, //eval('(' + flowData.value + ')'),
            tools: {
              save: function (data) {
                // console.log("保存", data);
                submitDesigner();
                // window.localStorage.setItem("data", data)
              },
              /*publish: function (data) {
                    console.log("发布", eval("(" + data + ")"));
                },*/
              addPath: function (id, data) {
                console.log('添加路径', id, eval('(' + data + ')'));
              },
              addRect: function (id, data) {
                console.log('添加状态', id, eval('(' + data + ')'));
              },
              clickPath: function (id, data) {
                console.log('点击线', id, eval('(' + data + ')'));
                // 取得其from节点，判断是否为条件分支
                var myflow = $flow;
                var path = myflow.getPaths()[id];

                // console.log(path.from());
                var flag = path.from().getPropVal('ActionFlag');
                console.log('flag', flag);
                // var isFlagXorRadiate = false;
                // if (flag.length >= 7) {
                //   if (flag.substr(6, 1) == '1') {
                //     isFlagXorRadiate = true;
                //   }
                // }

                // if (isFlagXorRadiate) {
                //   OpenLinkPropertyWin(id);
                // } else {
                //   OpenLinkPropertyNormalWin(id);
                // }
              },
              clickRect: function (id, data) {
                // console.log(data);
                console.log('点击状态', id, eval('(' + data + ')'));
                // OpenModifyWin(eval("(" + data + ")"));
                // if (curInternalName != id) {
                //   OpenModifyWin(id);
                // }
              },
              deletePath: function (id) {
                console.log('删除线', id);
              },
              deleteRect: function (id, data) {
                console.log('删除状态', id, eval('(' + data + ')'));
              },
              revoke: function (id) {
                console.log('撤销', id);
              },
            },
          });
          console.log('$flow', $flow, $('#myflowEdit'));
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

      //重绘流程图
      const handleResize = () => {
        $flow.resize();
      };
      return {
        handleSuccess,
        PlayDesigner,
        fetch,
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
    .node {
      display: flex;
    }
  }
</style>
