<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    :showCancelBtn="false"
    okText="关闭"
    @ok="handleSubmit"
  >
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" class="mr-1" @click="handleSelect"> 选择 </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'clarity:note-edit-line',
              tooltip: '编辑',
              onClick: handleEdit.bind(null, record),
            },
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              tooltip: '删除',
              popConfirm: {
                title: '是否确认删除',
                placement: 'leftBottom',
                confirm: handleDelete.bind(null, record),
              },
            },
          ]"
        />
      </template>
      <template #priv="{ record }">
        <div>
          <Checkbox v-model:checked="record.privSee">浏览</Checkbox>
          <Checkbox v-model:checked="record.privAdd">添加</Checkbox>
          <Checkbox v-model:checked="record.privEdit">修改</Checkbox>
          <!-- <Checkbox v-model:checked="record.privDownload">下载</Checkbox> -->
          <Checkbox v-model:checked="record.privDel">删除</Checkbox>
          <Checkbox v-model:checked="record.privImport">导入</Checkbox>
          <Checkbox v-model:checked="record.privExport">导出</Checkbox>
          <Checkbox v-model:checked="record.privExportWord" title="生成Word文件">生成</Checkbox>
          <Checkbox v-model:checked="record.privManage">管理</Checkbox>
        </div>
      </template>
    </BasicTable>
  </BasicDrawer>
  <SelectAll @register="registerSelectAll" @success="handleSlectSuccess" />
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicColumn } from '/@/components/Table';
  import { getSetRolePrivs } from '/@/api/system/system';
  import { SelectAll } from '/@/components/CustomComp';
  import { useI18n } from '/@/hooks/web/useI18n';

  import { useModal } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Checkbox } from 'ant-design-vue';
  import {
    getPrivList,
    getPrivListCreate,
    getPrivListUpdate,
    getPrivListDel,
  } from '/@/api/module/module';
  export default defineComponent({
    components: {
      BasicTable,
      BasicDrawer,
      SelectAll,
      Checkbox,
      TableAction,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { t } = useI18n();
      let dataRef = reactive<Recordable>({});
      let title = ref('');
      const rootCode = ref('');
      const [registerSelectAll, { openModal }] = useModal();
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        rootCode.value = data.code;
        title.value = `属于：${dataRef.name} 的权限`;
        setProps({
          searchInfo: {
            rootCode: data.code,
            nodeCode: dataRef.code,
          },
        });
        handleSuccess();
      });

      const { createMessage } = useMessage();
      const getTitle = computed(() => title.value);
      async function handleSubmit() {
        if (true) {
          closeDrawer();
          emit('success');
          return;
        }
        console.log('getDataSource();', getDataSource());
        try {
          let checkes = ref<Recordable>([]);
          // checkes.value = getSelectRows();
          checkes.value = getDataSource();
          if (checkes.value.length === 0) {
            createMessage.warning('选择数据不能为空');
            return;
          }
          setDrawerProps({ confirmLoading: true });
          let params = {
            privs: checkes.value.map((item) => item.code).join(','),
            roleCode: dataRef.code,
          };
          await getSetRolePrivs(params);
          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }
      const columns: BasicColumn[] = [
        {
          title: '名称',
          dataIndex: 'title',
          width: 100,
          ellipsis: true,
        },
        {
          title: '类型',
          dataIndex: 'privTypeName',
          width: 100,
          ellipsis: true,
        },
        {
          title: '目录',
          dataIndex: 'nodeName',
          width: 100,
          ellipsis: true,
        },
        {
          title: '权限',
          dataIndex: 'priv',
          width: 200,
          slots: { customRender: 'priv' },
        },
      ];
      const [
        registerTable,
        { reload, getSelectRows, getDataSource, setProps, setSelectedRowKeys, setTableData },
      ] = useTable({
        title: '权限列表',
        api: getPrivList,
        columns,
        formConfig: {
          labelWidth: 120,
        },
        rowKey: 'code',
        // rowSelection: {
        //   type: 'checkbox',
        // },
        searchInfo: {}, //额外的参数
        afterFetch: afterFetch,
        resizeHeightOffset: 120,
        immediate: false,
        pagination: true,
        useSearchForm: false,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        actionColumn: {
          width: 60,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
      });
      function afterFetch(res) {
        let data = res;
        if (data && data.length > 0) {
          data.forEach((item) => {
            item.privSee = item.privSee == 1;
            item.privAdd = item.privAdd == 1;
            item.privEdit = item.privEdit == 1;
            item.privDownload = item.privDownload == 1;
            item.privDel = item.privDel == 1;
            item.privImport = item.privImport == 1;
            item.privExport = item.privExport == 1;
            item.privExportWord = item.privExportWord == 1;
            item.privManage = item.privManage == 1;
          });
          setTableData(data);
        }
      }
      //选择完新增
      const handleSlectSuccess = async (records: Recordable) => {
        console.log('records', records);
        if (records && records.length > 0) {
          let dataList: Recordable[] = [];
          records.forEach((item) => {
            let name = item.key;
            if (item.privType == 3) {
              //0：用户组 1:用户 2：角色
              name = item.code;
            }
            dataList.push({
              rootCode: rootCode.value,
              nodeCode: dataRef.code,
              name: name,
              privType: item.privType,
            });
          });
          await getPrivListCreate(dataList);
          handleSuccess();
        }
      };
      //修改
      async function handleEdit(record) {
        const params = { ...record };
        params.privSee = params.privSee ? 1 : 0;
        params.privAdd = params.privAdd ? 1 : 0;
        params.privEdit = params.privEdit ? 1 : 0;
        params.privDownload = params.privDownload ? 1 : 0;
        params.privDel = params.privDel ? 1 : 0;
        params.privExport = params.privExport ? 1 : 0;
        params.privImport = params.privImport ? 1 : 0;
        params.privExportWord = params.privExportWord ? 1 : 0;
        params.privManage = params.privManage ? 1 : 0;
        let data = await getPrivListUpdate(params);
        if (data.res == 0) {
          createMessage.success(t('common.opSuccess'));
        }
        handleSuccess();
      }
      async function handleDelete(record) {
        await getPrivListDel({ id: record.id }).then(() => {
          handleSuccess();
        });
      }

      function handleSuccess() {
        reload();
      }

      const handleSelect = () => {
        openModal(true, {
          isUpdate: false,
          type: 'user',
        });
      };
      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        dataRef,
        registerTable,
        handleSelect,
        registerSelectAll,
        handleEdit,
        handleDelete,
        handleSlectSuccess,
      };
    },
  });
</script>
