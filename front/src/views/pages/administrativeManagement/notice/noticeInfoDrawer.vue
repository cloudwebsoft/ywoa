<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :showFooter="isNotForce"
    :title="getTitle"
    width="80%"
    :closable="isNotForce"
    :maskClosable="isNotForce"
    :showOkBtn="false"
    :cancelText="'关闭'"
  >
    <div class="border-1 border-solid border-gray-200 min-h-full">
      <div class="flex justify-center items-center h-10 text-base font-bold bg-gray-100">{{
        contentInfo.title
      }}</div>
      <div class="flex justify-center items-center"
        >发布者：{{ user.realName }} 发布日期：{{ contentInfo.createDate }}</div
      >
      <div class="p-2" v-html="contentInfo.content"></div>
      <div class="p-2">
        已查看通知的用户：{{ readedList ? readedList.length : 0 }}人
        <div v-if="readedList.length > 0">
          <span v-for="(item, i) in readedList || []" :key="i">{{ item?.user?.realName }}</span>
        </div>
      </div>

      <div class="p-2">
        未查看通知的用户：{{ notReadedList ? notReadedList.length : 0 }}人
        <div v-if="notReadedList.length > 0">
          <span style="margin-right: 20px" v-for="(item, index) in notReadedList" :key="index">{{
            item?.user?.realName
          }}</span>
        </div>
      </div>
      <Divider />
      <div class="mt-2 mb-2 p-2">
        <div class="mb-2 font-bold" v-if="showData?.notice?.isReply == 1"> 回复 </div>
        <div v-if="showData?.notice?.isReply == 1 && showData.canReply">
          <a-textarea
            v-model:value="content"
            placeholder="请输入"
            :auto-size="{ minRows: 2, maxRows: 3 }"
            class="custom-text"
          />
          <div class="flex justify-end mt-2">
            <div>
              <a-button class="custom-btn" @click="handleSaveReply" :loading="isReplyLoading">
                确定
              </a-button>
            </div>
          </div>
        </div>
        <div v-if="replyList && replyList.length">
          <div v-for="item in replyList" :key="item.id">
            <div>
              <span style="color: #85c4f0">{{ item?.user?.realName }}</span>
              <span class="ml-2">{{ item.replyTime }}</span>
            </div>
            <div>{{ item?.content }}</div>
          </div>
        </div>
      </div>
    </div>
    <BasicTable @register="registerTable" v-show="oaNoticeAttList && oaNoticeAttList.length > 0">
      <template #action="{ record, index }">
        <TableAction
          :actions="[
            {
              icon: 'ion:download-outline',
              tooltip: '下载',
              onClick: handleDownload.bind(null, record),
              loading: record.isDownloadAtt ? true : false,
            },
          ]"
        /> </template
    ></BasicTable>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, reactive, h } from 'vue';
  import { BasicColumn } from '/@/components/Table';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { downloadByData } from '/@/utils/file/download';
  import {
    getNoticeShow,
    getNoticeReplyList,
    getNoticeReplyAdd,
    getNoticeAttDownload,
  } from '/@/api/administration/administration';
  import { Divider } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { stubTrue } from 'lodash-es';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'noticeInfoDrawer',
    components: { BasicDrawer, Divider, BasicTable, TableAction },
    emits: ['success', 'register'],
    setup(_, {}) {
      const isUpdate = ref(true);
      const { t } = useI18n();
      const { createMessage, createConfirm } = useMessage();
      let dataRef = reactive<Recordable>({});
      let contentInfo = ref<Recordable>({});
      let user = ref<Recordable>({});
      let notReadedList = ref<Recordable>([]);
      let readedList = ref<Recordable>([]);
      const showData = ref<Recordable>({});
      const content = ref('');
      const replyList = ref<Recordable>([]);
      const isReplyLoading = ref(false);
      let oaNoticeAttList = ref([]);

      const [registerDrawer, { setDrawerProps }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        content.value = '';
        if (unref(isUpdate)) {
          dataRef = {
            ...data.record,
          };
          await fetchNoticeShow();
          await fetchNoticeReplyList();
        }
      });

      const getTitle = '查看';
      const isNotForce = ref(true);

      const fetchNoticeShow = () => {
        getNoticeShow({ id: dataRef['id'] }).then((res) => {
          showData.value = res;
          contentInfo.value = res.notice;
          user.value = res.notice.user;
          notReadedList.value = res.notReadedList || [];
          readedList.value = res.readedList || [];
          if (unref(contentInfo).isForcedResponse == 1 && unref(showData).canReply) {
            isNotForce.value = false;
            // createConfirm({
            //   iconType: 'info',
            //   title: () => h('span', '请回复'),
            //   onOk: async () => {},
            // });
            createMessage.warn('请回复');
          } else {
            isNotForce.value = true;
          }

          oaNoticeAttList.value = res.attList || [];
          if (unref(oaNoticeAttList).length > 0) {
            setTableData(unref(oaNoticeAttList));
          }
        });
      };

      const fetchNoticeReplyList = () => {
        getNoticeReplyList({ id: dataRef.id, pageSize: 99999 }).then((res) => {
          replyList.value = res.list;
        });
      };

      const handleSaveReply = () => {
        if (!unref(content)) {
          createMessage.warning('回复内容不能为空');
          return;
        }
        isReplyLoading.value = true;
        getNoticeReplyAdd({ id: dataRef.id, content: unref(content) })
          .then(() => {
            fetchNoticeShow();
            fetchNoticeReplyList();
          })
          .finally(() => {
            isReplyLoading.value = false;
          });
      };

      const columns: BasicColumn[] = [
        {
          title: '标题',
          dataIndex: 'name',
          align: 'left',
        },
        {
          title: '创建时间',
          dataIndex: 'uploadDate',
        },
      ];

      const [registerTable, { setTableData }] = useTable({
        title: '文件列表',
        api: '' as any,
        columns,
        formConfig: {},
        searchInfo: {
          op: 'search',
        },
        useSearchForm: false,
        showTableSetting: false,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        immediate: false,
        pagination: false,
        canResize: false,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      function handleDownload(record: any) {
        record.isDownloadAtt = true;
        const params = {
          visitKey: record.visitKey,
          attachId: record.id,
        };
        console.log('reocrd.name', record.name);
        getNoticeAttDownload(params)
          .then((data) => {
            if (data) {
              downloadByData(data, `${record.name}`);
            }
          })
          .finally(() => {
            record.isDownloadAtt = false;
          });
      }

      return {
        registerDrawer,
        getTitle,
        showData,
        contentInfo,
        user,
        notReadedList,
        readedList,
        content,
        replyList,
        handleSaveReply,
        isReplyLoading,
        isNotForce,
        registerTable,
        oaNoticeAttList,
        handleDownload,
      };
    },
  });
</script>

<style lang="less" scoped>
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
