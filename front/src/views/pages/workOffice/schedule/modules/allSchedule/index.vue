<template>
  <div class="bg-white h-full">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增 </a-button>
        <a-button type="primary"> 删除 </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'clarity:note-edit-line',
              onClick: handleEdit.bind(null, record),
            },
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              popConfirm: {
                title: '是否确认删除',
                placement: 'leftBottom',
                confirm: handleDelete.bind(null, record),
              },
            },
          ]"
        />
      </template>
    </BasicTable>
    <AllScheduleDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, watch } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import type { Dayjs } from 'dayjs';
  import { dateUtil as dayjs } from '/@/utils/dateUtil';
  import { columns, searchFormSchema, isChange } from './allSchedule.data';
  import AllScheduleDrawer from './AllScheduleDrawer.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { getPlanList, getPlanDel } from '/@/api/workOffice/workOffice';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'schedule',
    components: {
      BasicTable,
      TableAction,
      AllScheduleDrawer,
    },
    setup() {
      const activeKey = ref('1');
      const date = ref<Dayjs>(dayjs());
      const activeType = ref<any>('date');
      onMounted(() => {
        init();
      });
      const init = () => {};
      const [registerTable, { reload }] = useTable({
        title: '全部日程列表',
        api: getPlanList,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
        },
        rowSelection: {
          type: 'checkbox',
        },
        rowKey: 'id',
        beforeFetch: (info) => {
          let newInfo = info;
          if (newInfo.dates && newInfo.dates.length > 0) {
            newInfo.beginDate = newInfo.dates[0];
            newInfo.endDate = newInfo.dates[1];
            delete newInfo.dates;
          }
          return newInfo;
        },
        searchInfo: { op: 'search' }, //额外的参数
        useSearchForm: true,
        showTableSetting: false,
        bordered: true,
        showIndexColumn: false,
        clickToRowSelect: false,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });

      const [registerDrawer, { openDrawer }] = useDrawer();

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
        await getPlanDel({ id: record.id }).then(() => {
          handleSuccess();
        });
      }

      watch(
        () => isChange.value,
        () => {
          if (isChange.value) {
            handleSuccess();
            isChange.value = false;
          }
        },
      ); //监听启用成功后isChange为true时刷新列表

      function handleSuccess() {
        reload();
      }
      return {
        activeKey,
        registerTable,
        activeType,
        date,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
      };
    },
  });
</script>
