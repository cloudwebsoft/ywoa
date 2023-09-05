<template>
  <div>
    <BasicTable @register="registerTable" @resize-column="handleResizeColumn">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate" v-if="listPage.canManage"> 新增 </a-button>
      </template>
      <template #title="{ record }">
        <div style="text-align: left">
          <span :class="record.isBold == 1 ? 'font-bold' : ''" :style="[{ color: record.color }]">{{
            record.title
          }}</span>
        </div>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'clarity:info-standard-line',
              tooltip: '查看',
              onClick: handleView.bind(null, record),
            },
            {
              icon: 'clarity:note-edit-line',
              onClick: handleEdit.bind(null, record),
              ifShow: listPage.canManage,
            },
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record),
              },
              ifShow: listPage.canManage,
            },
          ]"
        />
      </template>
    </BasicTable>
    <NoticeDrawer @register="registerDrawer" @success="handleSuccess" />
    <NoticeInfoDrawer @register="registerInfoDrawer" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, onActivated } from 'vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import {
    getNoticeListPage,
    getNoticeList,
    getNoticeDel,
  } from '/@/api/administration/administration';

  import { useMultipleTabWithOutStore } from '/@/store/modules/multipleTab';

  import { useDrawer } from '/@/components/Drawer';
  import NoticeDrawer from './noticeDrawer.vue';
  import NoticeInfoDrawer from './noticeInfoDrawer.vue';

  import { columns, searchFormSchema } from './notice.data';

  export default defineComponent({
    name: 'Notice',
    components: { BasicTable, NoticeDrawer, TableAction, NoticeInfoDrawer },
    setup() {
      const multipleTabStore = useMultipleTabWithOutStore();
      onActivated(() => {
        console.log('onActivated1', multipleTabStore.isRefreshPage);
        // if (multipleTabStore.isRefreshPage) {
        //   reload();
        //   multipleTabStore.updateIsRefreshPage(false);
        // }
        // console.log('onActivated2', multipleTabStore.isRefreshPage);
      });

      const [registerDrawer, { openDrawer }] = useDrawer();
      const [registerInfoDrawer, { openDrawer: openInfoDrawer }] = useDrawer();

      const listPage = ref({});
      onMounted(async () => {
        listPage.value = await getNoticeListPage();
      });

      const [registerTable, { reload }] = useTable({
        title: '通知公告',
        api: getNoticeList,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
        },
        searchInfo: {
          op: 'search',
        }, //额外的参数
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        beforeFetch: (info) => {
          let newInfo = info;
          if (newInfo.dates && newInfo.dates.length > 0) {
            newInfo.fromDate = newInfo.dates[0];
            newInfo.toDate = newInfo.dates[1];
            delete newInfo.dates;
          }
          return newInfo;
        },
        actionColumn: {
          width: 120,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: 'right',
        },
      });

      function handleCreate() {
        openDrawer(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        openDrawer(true, {
          record,
          isUpdate: true,
        });
      }

      async function handleDelete(record: Recordable) {
        await getNoticeDel({ ids: record.id }).then(() => {
          handleSuccess();
        });
      }

      function handleView(record: any) {
        openInfoDrawer(true, {
          isUpdate: true,
          record,
        });
      }

      function handleSuccess() {
        reload();
      }
      onMounted(() => {});
      return {
        registerTable,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        registerInfoDrawer,
        handleView,
        listPage,
        handleResizeColumn: (w, col) => {
          col.width = w;
        },
      };
    },
  });
</script>
