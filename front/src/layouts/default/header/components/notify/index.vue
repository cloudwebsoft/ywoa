<template>
  <div :class="prefixCls" style="z-index: 1">
    <Popover
      title=""
      trigger="click"
      :overlayClassName="`${prefixCls}__overlay`"
      @click="handleClick"
    >
      <Badge :count="count" dot :numberStyle="numberStyle">
        <BellOutlined />
      </Badge>
      <template #content>
        <div v-if="listData.length > 0">
          <Tabs>
            <template v-for="item in listData" :key="item.key">
              <TabPane>
                <template #tab>
                  {{ item.name }}
                  <span v-if="item.list.length !== 0">({{ item.list.length }})</span>
                </template>
                <!-- 绑定title-click事件的通知列表中标题是“可点击”的-->
                <NoticeList
                  :list="item.list"
                  v-if="item.key === '1'"
                  @title-click="onNoticeClick"
                />
                <NoticeList :list="item.list" v-else />
              </TabPane>
            </template>
          </Tabs>
          <div class="mt-2 flex justify-center cursor-pointer" @click="toMessage">更多</div>
        </div>
        <div v-else class="flex justify-center">无</div>
      </template>
    </Popover>
    <MessageCenterInfoDrawer @register="registerDrawer" />
  </div>
</template>
<script lang="ts">
  import { computed, defineComponent, ref, onMounted } from 'vue';
  import { Popover, Tabs, Badge } from 'ant-design-vue';
  import { BellOutlined } from '@ant-design/icons-vue';
  import { ListItem } from './data';
  import NoticeList from './NoticeList.vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  // import { useMessage } from '/@/hooks/web/useMessage';
  import { getNewMsgsOfUser } from '/@/api/workOffice/workOffice';
  import { useDrawer } from '/@/components/Drawer';
  import MessageCenterInfoDrawer from '/@/views/pages/workOffice/messageCenter/messageCenterInfoDrawer.vue';
  import { getToken } from '/@/utils/auth';

  import { useGo } from '/@/hooks/web/usePage';
  export default defineComponent({
    components: {
      Popover,
      BellOutlined,
      Tabs,
      TabPane: Tabs.TabPane,
      Badge,
      NoticeList,
      MessageCenterInfoDrawer,
    },
    setup() {
      const { prefixCls } = useDesign('header-notify');
      // const { createMessage } = useMessage();
      const listData = ref<any>([]);
      onMounted(() => {
        getNewMsg();
        let sint = setInterval(() => {
          if (getToken() == undefined) {
            window.clearInterval(sint);
          } else {
            getNewMsg();
          }
        }, 30000);
      });
      function handleClick() {
        // getNewMsg();
      }
      function getNewMsg() {
        listData.value = [];
        getNewMsgsOfUser().then((res) => {
          if (res && res.length > 0) {
            let data = res.map((item) => {
              item.avatar = '';
              item.description = '';
              item.datetime = item.sendTime;
              item.type = '1';
              return item;
            });
            listData.value.push({
              key: '1',
              name: '消息',
              list: data,
            });
          }
        });
      }
      const go = useGo();
      //跳转
      function toMessage() {
        go('/messageCenter');
      }
      const [registerDrawer, { openDrawer }] = useDrawer();
      function handleView(record: any) {
        openDrawer(true, {
          isUpdate: true,
          record,
        });
      }
      const count = computed(() => {
        let count = 0;
        for (let i = 0; i < listData.value.length; i++) {
          count += listData.value[i].list.length;
        }
        return count;
      });

      function onNoticeClick(record: ListItem) {
        // createMessage.success('你点击了通知，ID=' + record.id);
        // 可以直接将其标记为已读（为标题添加删除线）,此处演示的代码会切换删除线状态
        record.titleDelete = !record.titleDelete;
        handleView(record);
        getNewMsg();
      }

      return {
        prefixCls,
        listData,
        count,
        onNoticeClick,
        numberStyle: {},
        registerDrawer,
        handleClick,
        toMessage,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-notify';

  .@{prefix-cls} {
    padding-top: 2px;
    &__overlay {
      max-width: 360px;
      min-width: 300px;
      z-index: 1000;
    }
    .ant-tabs-content {
      width: 300px;
    }

    .ant-badge {
      font-size: 18px;

      .ant-badge-multiple-words {
        padding: 0 4px;
      }

      svg {
        width: 0.9em;
      }
    }
  }
</style>
