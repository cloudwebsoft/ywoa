<template>
  <div class="w-full">
    <Card title="附言">
      <template #extra>
        <div>
          <a-button type="primary" class="mr-2" @click="isShowMsg = !isShowMsg">{{
            isShowMsg ? '收起' : '展开'
          }}</a-button>
          <a-button type="primary" @click="isShow = !isShow">回复</a-button>
        </div>
      </template>
      <div>
        <div>
          <template v-if="isShow">
            <a-textarea
              v-model:value="firstData.remark"
              placeholder="请输入回复"
              :auto-size="{ minRows: 2, maxRows: 3 }"
              class="custom-text"
            />
            <div class="flex justify-between mt-2">
              <div>
                <!-- <Checkbox v-model:checked="firstData.isSecret">隐藏</Checkbox> -->
              </div>
              <div>
                <a-button class="custom-btn" @click="handleOk(firstData, -1)">确定</a-button>
              </div>
            </div>
          </template>
          <template v-if="isShowMsg">
            <div class="pt-2 pb-2" v-for="item in list" :key="item.id">
              <div class="w-full" style="border-bottom: #eee 1px solid">
                <div class="flex">
                  <template v-if="!item.isLeaf">
                    <div style="color: #85c4f0" class="w-100px" :title="`${item.realName}`">{{
                      item.realName
                    }}</div>
                  </template>
                  <template v-if="item.isLeaf">
                    <div
                      style="color: #85c4f0"
                      class="text-right w-100px overflow-hidden"
                      :title="`${item.realName} 评论了 ${item.replyRealName}`"
                    >
                      {{ item.realName }}
                      <!-- 发表评论 {{ item.replyRealName }} -->
                    </div>
                  </template>
                  <div class="flex-1 pl-2 pr-2">{{ item.content }}</div>
                  <div>
                    <!-- <span class="cursor-pointer ml-2 mr-2" @click="handleDel(item.id)">删除</span> -->
                    {{ item.addDate }}
                    <span class="cursor-pointer mr-2"
                      ><Icon
                        style="color: #ed6f6f"
                        title="删除"
                        icon="ant-design:delete-outlined"
                        v-if="isFlowManager"
                        @click="handleDel(item.id)"
                    /></span>
                    <span
                      class="cursor-pointer"
                      v-if="flowStatus == 1"
                      :style="{ visibility: item.parentId == -1 ? 'visible' : 'hidden' }"
                    >
                      <Icon
                        style="color: #ffc24d"
                        icon="ant-design:message-outlined"
                        @click="item.show = !item.show"
                      />
                    </span>
                  </div>
                </div>
                <div class="mt-2 mb-2" v-if="item.show && item.parentId == -1">
                  <a-textarea
                    v-model:value="item.remark"
                    placeholder="请输入回复"
                    :auto-size="{ minRows: 2, maxRows: 3 }"
                    class="custom-text"
                  />
                  <div class="flex justify-between mt-2">
                    <div>
                      <Checkbox v-model:checked="item.isSecret">隐藏</Checkbox>
                    </div>
                    <div>
                      <a-button
                        class="custom-btn"
                        @click="handleOk(item, item.isLeaf ? item.parentId : item.id)"
                        >确定</a-button
                      >
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </Card>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, watchEffect, h } from 'vue';
  import { Card, Checkbox } from 'ant-design-vue';
  import Icon from '/@/components/Icon/index';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { dateUtil as dayjs } from '/@/utils/dateUtil';
  import {
    getListAnnex,
    getAddReply,
    getDelAnnex,
    getDownloadAnnexAttachment,
  } from '/@/api/process/process';
  export default defineComponent({
    components: { Card, Checkbox, Icon },
    props: {
      flowInfo: {
        type: Object,
        default: () => {
          return {};
        },
      },
    },
    emits: ['success', 'register'],
    setup(props) {
      const { createMessage, createConfirm } = useMessage();
      const list = ref<Recordable>([]);
      const firstData = ref<Recordable>({
        remark: '',
        isSecret: false,
      });
      const isShow = ref(false);
      const isShowMsg = ref(true);
      const isFlowManager = ref(false);
      const flowStatus = ref(1); // 流程状态，默认为处理中
      const fetch = () => {
        getListAnnex({ flowId: props?.flowInfo?.flowId }).then((res) => {
          console.log('getListAnnex res', res);
          list.value = [];
          setChild(res);
        });
      };
      const setChild = (data: Recordable, isLeaf = false) => {
        if (data?.length) {
          data.forEach((item) => {
            item.addDate = dayjs(item.addDate).format('YYYY-MM-DD HH:MM');
            item.isSecret = item.show = false;
            item.isLeaf = isLeaf;
            list.value.push(item);
            if (item.aryAnnexSub?.length) {
              setChild(item.aryAnnexSub, true);
            }
          });
        }
      };
      const search = () => {
        list.value = [];
        fetch();
      };
      watchEffect(() => {
        props?.flowInfo?.flowId && search();
        isFlowManager.value = props.flowInfo.isFlowManager;
        flowStatus.value = props.flowInfo.flowStatus;
      });
      const handleOk = (record: Recordable, parentId: number) => {
        console.log('props', props.flowInfo);
        let params = {
          flowId: props.flowInfo.flowId,
          myActionId: props.flowInfo.myActionId,
          parentId: parentId,
          content: record.remark,
          isSecret: record.isSecret ? 1 : 0,
          progress: 0,
        };
        if (!params.content) {
          createMessage.warning('回复内容不能为空');
          return;
        }
        console.log('params', params);
        getAddReply(params).then((res) => {
          console.log('res==>', res);
          search();
          notShow();
        });
      };

      const notShow = () => {
        isShow.value = false;
        firstData.value.remark = '';
        firstData.value.isSecret = false;
      };

      const handleDel = (id: number) => {
        createConfirm({
          iconType: 'warning',
          title: () => h('span', '您确定要删除么？'),
          onOk: async () => {
            await del(id);
          },
        });
      };
      const del = async (id: number) => {
        await getDelAnnex({ id });
        search();
      };
      return { firstData, isShow, handleOk, list, handleDel, isShowMsg, isFlowManager, flowStatus };
    },
  });
</script>

<style lang="less" scoped>
  :deep(.ant-card-head-title) {
    font-weight: bold;
  }
  .custom-text {
    border: solid 1px #ffa200;
    &:focus {
      border: solid 1px #0960bd;
    }
  }
  .custom-btn {
    background-color: #ffc24d;
    color: #fff;
  }
</style>
