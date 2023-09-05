<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    :maskClosable="false"
    :showOkBtn="false"
    @cancel="handleCancel"
    cancelText="关闭"
  >
    <div></div>
    <Row>
      <div v-if="rows >= 0"
        ><span>数量{{ rows }}条</span><span class="ml-2">已导{{ curRows }}条</span
        ><span class="ml-2">耗时{{ seconds }}秒</span
        ><span class="ml-2" v-if="secondsRemained > 0">预估{{ secondsRemained }}秒结束</span></div
      >
      <div v-else>正在导出 ...</div>
    </Row>
    <Row>
      <Progress :percent="progress" />
    </Row>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Row, Progress } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { downloadByData } from '/@/utils/file/download';
  import { getExportExcelProgress, downloadExportExcelAsync } from '/@/api/module/module';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: 'ExportExcelProgressModal',
    components: { BasicModal, Progress, Row },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();

      const userStore = useUserStore();
      let serverInfo = userStore.getServerInfo;
      let uid = '';
      const progress = ref(0);
      const rows = ref(0);
      const curRows = ref(0);
      const seconds = ref(0);
      const beginTime = ref(0);
      const secondsRemained = ref(0);
      let moduleName = '';

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '40%', bodyStyle: { height: '100px' } });

        rows.value = -1;
        progress.value = 0;
        seconds.value = 0;
        uid = data.uid;
        moduleName = data.moduleName;

        let d = new Date();
        beginTime.value = d.getTime();
        getProgress(uid);
        refreshProgress(uid);
      });

      const getTitle = '导出进度';

      let timerId: ReturnType<typeof setInterval> | null;
      function clear() {
        timerId && window.clearInterval(timerId);
      }
      const refreshProgress = async (uid) => {
        timerId = setInterval(async () => {
          getProgress(uid);
        }, 3000);
      };

      const getProgress = async (uid) => {
        seconds.value = parseInt((new Date().getTime() - beginTime.value) / 1000);
        let params = {
          uid,
        };
        let data = await getExportExcelProgress(params);
        rows.value = data.rows;
        curRows.value = data.curRow;
        console.log('getProgress data', data);
        progress.value = parseInt((data.curRow * 100) / data.rows);

        if (seconds.value > 0) {
          let rowsPerSec = curRows.value / seconds.value;
          secondsRemained.value = parseInt((rows.value - curRows.value) / rowsPerSec);
        }

        if (progress.value == 100) {
          clear();
          let params = {
            uid,
            id: data.id,
          };
          await downloadExportExcelAsync(params).then((res) => {
            downloadByData(res, moduleName + '.xls');
            // closeModal();
            createMessage.success('操作成功');
          });
        }
      };

      const handleCancel = () => {
        clear();
        closeModal();
      };

      return {
        registerModal,
        getTitle,
        progress,
        handleCancel,
        rows,
        curRows,
        seconds,
        secondsRemained,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.ant-transfer-list-body) {
    width: 100%;
    height: 400px;
  }
  :deep(.ant-transfer-list-body-customize-wrapper) {
    padding: 0 12px 0 0px;
    height: 100%;
  }
  .transfer {
    height: 300px;
    overflow: hidden;
  }
  .transfer-left,
  .transfer-right {
    width: 100%;
    height: 300px;
    overflow-y: auto;
    &-item {
      padding-left: 12px;
      width: 100%;
      height: 30px;
      display: flex;
      align-items: center;
      &-content {
        width: 100%;
        display: flex;
        padding: 10px;
        align-items: center;
        justify-content: space-between;
      }
    }
    &-item:hover {
      background: #ccc;
    }
  }
</style>
