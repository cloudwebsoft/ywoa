<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    @ok="handleSubmit"
    :showOkBtn="false"
    :cancelText="'关闭'"
  >
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> 新增 </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
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
      <template #include="{ record }">
        <Switch
          v-model:checked="record.include"
          checked-children="包含"
          un-checked-children="不包含"
          @change="(e) => changeInclude(e, record)"
        ></Switch>
      </template>
    </BasicTable>

    <OrgInfoDrawerfrom @register="registerChildDrawer" @success="handleSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicColumn } from '/@/components/Table';
  import {
    getRoleDepartmentListPage,
    getDelRoleDepartmentList,
    getChangeRoleDepartmentStatus,
  } from '/@/api/system/system';
  import OrgInfoDrawerfrom from './orgInfoDrawer.vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { Switch } from 'ant-design-vue';
  export default defineComponent({
    name: 'orgInfo',
    components: {
      BasicTable,
      TableAction,
      BasicDrawer,
      OrgInfoDrawerfrom,
      Switch,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let dataRef = reactive({});
      let title = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于角色：${dataRef.description} 的组织`;
        setProps({
          searchInfo: {
            roleCode: dataRef.code,
          },
        });
        handleSuccess();
      });

      const getTitle = computed(() => title.value);
      async function handleSubmit() {
        try {
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          closeDrawer();
          dataRef = {};
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }
      const columns: BasicColumn[] = [
        {
          title: '部门名称',
          dataIndex: 'name',
          width: 100,
          ellipsis: true,
        },
        {
          title: '部门全名',
          dataIndex: 'fullDeptName',
          width: 150,
          ellipsis: true,
        },
        {
          title: '包含子部门',
          dataIndex: 'include',
          width: 150,
          ellipsis: true,
          ifShow: false, // 暂不支持包含子部门，因为过于复杂，在维护部门人员的时候，还得往上找父节点是否属于角色
          slots: { customRender: 'include' },
        },
      ];
      const [registerTable, { reload, setProps }] = useTable({
        title: '组织列表',
        api: getRoleDepartmentListPage,
        columns,
        formConfig: {
          labelWidth: 120,
        },
        searchInfo: {}, //额外的参数
        resizeHeightOffset: 70,
        immediate: false,
        useSearchForm: false,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        actionColumn: {
          width: 80,
          title: '操作',
          dataIndex: 'action',
          slots: { customRender: 'action' },
          fixed: undefined,
        },
      });
      function handleCreate() {
        openDrawer(true, {
          isUpdate: false,
          record: dataRef,
        });
      }

      function handleEdit(record: Recordable) {}
      async function handleDelete(record: Recordable) {
        let params = {
          ids: record.id,
        };
        await getDelRoleDepartmentList(params).then((res) => {
          handleSuccess();
        });
      }

      async function changeInclude(e, record) {
        let params = {
          id: record.id,
          include: e,
        };
        await getChangeRoleDepartmentStatus(params).then(() => {
          handleSuccess();
        });
      }

      const [registerChildDrawer, { openDrawer }] = useDrawer();

      function handleSuccess() {
        reload();
      }

      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        dataRef,
        handleCreate,
        handleEdit,
        handleDelete,
        registerTable,
        registerChildDrawer,
        handleSuccess,
        changeInclude,
      };
    },
  });
</script>
