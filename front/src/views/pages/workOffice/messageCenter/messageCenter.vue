<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleRead"> 已读 </a-button>
        <a-button type="primary" @click="handleNotRead"> 未读 </a-button>
        <Popconfirm
          placement="bottom"
          title="确定删除吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="handleAllDelete"
        >
          <a-button type="primary">批量删除</a-button>
        </Popconfirm>
      </template>
      <template #title="{ record }">
        <span :class="!record.isReaded ? 'font-bold' : ''">{{ record.title }}</span>
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
              icon: 'ant-design:delete-outlined',
              color: 'error',
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record),
              },
            },
          ]"
        />
      </template>
    </BasicTable>
    <MessageCenterInfoDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getSysMessageList, getSetReaded, getDelToDustbin } from '/@/api/workOffice/workOffice';

  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Popconfirm } from 'ant-design-vue';
  import MessageCenterInfoDrawer from './messageCenterInfoDrawer.vue';

  import { columns, searchFormSchema } from './messageCenter.data';

  export default defineComponent({
    name: 'MessageCenter',
    components: { BasicTable, TableAction, Popconfirm, MessageCenterInfoDrawer },
    setup() {
      const { createMessage } = useMessage();
      const [registerDrawer, { openDrawer }] = useDrawer();
      const [registerTable, { reload, getSelectRowKeys, setSelectedRowKeys }] = useTable({
        title: '消息列表',
        api: getSysMessageList,
        rowKey: 'id',
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
        },

        rowSelection: {
          type: 'checkbox',
        },
        searchInfo: {
          action: 'search',
          isRecycle: '0',
        }, //额外的参数
        useSearchForm: true,
        showTableSetting: true,
        indexColumnProps: { width: 50 },
        bordered: true,
        showIndexColumn: true,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      function handleView(record: any) {
        openDrawer(true, {
          isUpdate: true,
          record,
        });
      }

      async function handleRead() {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('数据不能为空');
          return;
        }
        let params = {
          ids: keys.join(','),
          isReaded: true,
          isSys: true,
        };
        await getSetReaded(params);
        handleSuccess();
      }
      async function handleNotRead() {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('数据不能为空');
          return;
        }
        let params = {
          ids: keys.join(','),
          isReaded: false,
        };
        await getSetReaded(params);
        handleSuccess();
      }

      function handleEdit(record: Recordable) {
        openDrawer(true, {
          record,
          isUpdate: true,
        });
      }
      function handleAllDelete() {
        let keys = getSelectRowKeys();
        if (keys.length == 0) {
          createMessage.warning('数据不能为空');
          return;
        }
        let params = {
          ids: keys.join(','),
        };
        getDelToDustbin(params).then(() => {
          handleSuccess();
        });
      }

      async function handleDelete(record: Recordable) {
        await getDelToDustbin({ ids: record.id }).then(() => {
          handleSuccess();
        });
      }

      function handleSuccess() {
        reload();
        setSelectedRowKeys([]);
      }

      return {
        registerTable,
        registerDrawer,
        handleRead,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleNotRead,
        handleView,
        handleAllDelete,
      };
    },
  });
</script>
