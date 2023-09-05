<template>
  <div class="h-full overflow-auto">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增 </a-button>
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
              icon: 'clarity:note-edit-line',
              onClick: handleEdit.bind(null, record),
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
    <SelectAll @register="registerSelectAllModal" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, watch, nextTick } from 'vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import {
    getAdminFlowListPriv,
    getAdminFlowCreatePriv,
    getAdminFlowUpdatePriv,
    getAdminFlowDelPriv,
  } from '/@/api/flowManage/flowManage';
  import { useModal } from '/@/components/Modal';

  import { SelectAll } from '/@/components/CustomComp';

  import { columns } from './authority.data';
  export default defineComponent({
    components: { BasicTable, TableAction, SelectAll },
    props: {
      activeKey: {
        type: String,
        default: '',
      },
      currentRecord: {
        type: Object,
        default: () => {
          return {};
        },
      },
    },
    setup(props) {
      const [registerSelectAllModal, { openModal }] = useModal();

      const [registerTable, { reload, setProps }] = useTable({
        title: '权限',
        api: getAdminFlowListPriv,
        columns,
        searchInfo: {
          op: 'search',
        }, //额外的参数
        useSearchForm: false,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        immediate: false,
        pagination: false, //无分页
        actionColumn: {
          width: 120,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: 'right',
        },
      });

      const handleCreate = () => {
        openModal(true, {
          isUpdate: false,
          tabKeys: ['2', '3', '4'],
        });
      };

      const handleEdit = async (record: Recordable) => {
        await getAdminFlowUpdatePriv(record);
        reload();
      };

      const handleDelete = async (record: Recordable) => {
        await getAdminFlowDelPriv({ id: record.id });
        reload();
      };

      const handleSuccess = async (users) => {
        console.log('users', users);
        if (users && users.length > 0) {
          let dataList: Recordable[] = [];
          users.forEach((item) => {
            let name = item.key;
            if (item.privType == 3) {
              name = item.code;
            }
            dataList.push({
              name: name,
              privType: item.privType,
            });
          });
          const formData = {
            nodeCode: props.currentRecord.code,
            privs: dataList,
          };
          await getAdminFlowCreatePriv(formData);
          reload();
        }
      };
      watch(
        () => props.activeKey,
        async (newVal) => {
          if (newVal === 'Authority') {
            await nextTick();
            setProps({ searchInfo: { code: props.currentRecord.code } });
            reload();
          }
        },
      );
      return {
        registerTable,
        registerSelectAllModal,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
      };
    },
  });
</script>
