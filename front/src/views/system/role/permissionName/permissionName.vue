<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" style="margin-right: 10px" @click="handleCreate"> 新增 </a-button>
        <a-button type="primary" @click="handleSubmit"> 保存 </a-button>
      </template>
      <template #action="{ record, index }">
        <TableAction
          :actions="[
            {
              icon: 'ant-design:arrow-up-outlined',
              onClick: handleUp.bind(null, record, index),
              ifShow: index != 0,
              tooltip: '上移',
            },
            {
              icon: 'ant-design:arrow-down-outlined',
              onClick: handleDown.bind(null, record, index),
              ifShow: index != len - 1,
              tooltip: '下移',
            },
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              tooltip: '删除',
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record, index),
              },
            },
          ]"
        />
      </template>
    </BasicTable>
    <PermissionNameDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>
<script lang="ts">
  import { computed, defineComponent, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getListPriv, getSetPrivs } from '/@/api/system/system';
  import { useDrawer } from '/@/components/Drawer';
  import PermissionNameDrawer from './permissionNameDrawer.vue';
  import { columns, searchFormSchema } from './permissionName.data';

  export default defineComponent({
    name: 'permissionName',
    components: {
      BasicTable,
      PermissionNameDrawer,
      TableAction,
    },
    setup() {
      const [registerDrawer, { openDrawer: openDrawerIndex }] = useDrawer();
      const [registerTable, { reload, getDataSource, setTableData }] = useTable({
        title: '权限列表',
        api: getListPriv,
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
        },
        searchInfo: {}, //额外的参数
        pagination: false,
        useSearchForm: false,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: false,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
        afterFetch: afterFetch,
      });

      function handleCreate() {
        openDrawerIndex(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        openDrawerIndex(true, {
          record: {
            ...record,
            desc: record.description,
          },
          isUpdate: true,
        });
      }

      async function handleDelete(record: Recordable, index) {
        let dataSource = await getDataSource();
        dataSource.splice(index, 1);
        setTableData([...dataSource]);
      }
      const oldDataSource = ref([]);
      function afterFetch(e) {
        if (e && e.length > 0) {
          oldDataSource.value = e;
        }
      }
      let len = computed(() => oldDataSource.value.length);
      async function handleSubmit() {
        let data = await getDataSource();

        let params = {
          oldPrivs: '',
          newRowOrder: '',
        };
        for (let i = 0; i < data.length; i++) {
          data[i].desc1 = data[i].layer == 1 ? data[i].description : '';
          data[i].desc2 = data[i].layer == 2 ? data[i].description : '';
          params[`new_rivs_${data[i].orders}`] = data[i].priv;
          params[`new_rivs_desc_${data[i].orders}`] = data[i].desc1;
          params[`new_rivs_desc2_${data[i].orders}`] = data[i].desc2;
        }
        params.oldPrivs = oldDataSource.value.map((item) => item.priv).join(',');
        params.newRowOrder = data.map((item) => item.orders).join(',');
        await getSetPrivs(params).then((res) => {
          handleSuccess();
        });
      }

      async function handleUp(record: Recordable, index) {
        let dataSource = await getDataSource();
        let arr = JSON.parse(JSON.stringify(dataSource));
        let newArr = up(index, arr);
        setTableData([...newArr]);
      }
      //向上移动
      function up(index, arr) {
        if (index == 0) {
          //开头不移动
          return;
        }
        let curObj = arr[index]; //当前对象
        let preObj = arr[index - 1]; //上一个对象
        arr.splice(index, 1, preObj); //上一个对象移动到当前对象
        arr.splice(index - 1, 1, curObj); //当前对象往上移动
        return arr;
      }
      async function handleDown(record: Recordable, index) {
        let dataSource = await getDataSource();
        let arr = JSON.parse(JSON.stringify(dataSource));
        let newArr = down(index, arr);
        setTableData([...newArr]);
      }

      function down(index, arr) {
        if (index == arr.length - 1) {
          //结尾不移动
          return;
        }
        let curObj = arr[index]; //当前对象
        let downObj = arr[index + 1]; //下一个对象
        arr.splice(index, 1, downObj); //下一个对象移动到当前对象位置
        arr.splice(index + 1, 1, curObj); //当前对象往下移动
        return arr;
      }

      function handleSuccess() {
        reload();
      }
      return {
        registerTable,
        registerDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleSubmit,
        handleUp,
        handleDown,
        len,
      };
    },
  });
</script>
