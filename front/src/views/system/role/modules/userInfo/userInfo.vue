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
      <template #deptJson="{ record }">
        <div v-if="record.deptJson && record.deptJson.length > 0">
          <div v-for="(item, index) in record.deptJson" :key="index">
            <!-- <Popover placement="top">
              <template #content>
                <RadioGroup
                  v-model:value="item.roleOfDept"
                  @change="(e) => getChange(e, record, index)"
                >
                  <Radio :value="0">不属于</Radio>
                  <Radio :value="1">默认属于</Radio>
                  <Radio :value="2">显式属于</Radio>
                </RadioGroup>
              </template>
              <Button type="link" size="small">
                <MoreOutlined class="icon-more" />
              </Button>
            </Popover> -->
            <Checkbox
              :checked="item.roleOfDept == 0 ? false : true"
              @change="(e) => getChange(e, record, index)"
            >
              {{ item.deptName }}</Checkbox
            >
          </div>
        </div>
      </template>
    </BasicTable>

    <SelectUser @register="registerModal" @success="handleModelSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive, watchEffect } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicColumn } from '/@/components/Table';
  import {
    getListUserOfRole,
    getSetUserRoleOfDept,
    getAddRoleUser,
    getDelRoleUserBatch,
  } from '/@/api/system/system';
  import { SelectUser } from '/@/components/CustomComp';
  import { RadioGroup, Radio, Button, Popover, Checkbox } from 'ant-design-vue';
  import { MoreOutlined } from '@ant-design/icons-vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  export default defineComponent({
    name: 'userInfo',
    components: {
      BasicTable,
      TableAction,
      BasicDrawer,
      SelectUser,
      RadioGroup,
      Radio,
      MoreOutlined,
      Button,
      Popover,
      Checkbox,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let dataRef = reactive({});
      let title = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于角色：${dataRef.description} 的用户`;
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
          title: '用户名',
          dataIndex: 'name',
          width: 100,
          ellipsis: true,
        },
        {
          title: '真实姓名',
          dataIndex: 'realName',
          width: 150,
          ellipsis: true,
        },
        {
          title: '工号',
          dataIndex: 'account',
          width: 150,
          ellipsis: true,
        },
        {
          title: '性别',
          dataIndex: 'gender',
          width: 100,
          ellipsis: true,
        },
        {
          title: '所属部门',
          dataIndex: 'deptJson',
          align: 'left',
          width: 150,
          ellipsis: true,
          slots: { customRender: 'deptJson' },
        },
      ];
      const [registerTable, { reload, setProps }] = useTable({
        title: '用户列表',
        api: getListUserOfRole,
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
        openModal(true, {
          isUpdate: false,
        });
      }
      async function handleDelete(record: Recordable) {
        let params = {
          userNames: record.name,
          roleCode: dataRef?.code,
          roleDesc: dataRef?.description,
        };
        await getDelRoleUserBatch(params).then((res) => {
          handleSuccess();
        });
      }

      const [registerModal, { openModal }] = useModal();

      function handleSuccess() {
        reload();
      }

      function getChange(e, record, index) {
        let roleOfDept = e.target.checked ? '2' : '0';
        let params = {
          roleCode: dataRef.code,
          deptCode: record.deptJson[index]['deptCode'],
          userName: record.name,
          roleOfDept: roleOfDept,
        };
        setDrawerProps({ confirmLoading: true });
        getSetUserRoleOfDept(params).then(() => {
          setDrawerProps({ confirmLoading: false });
          handleSuccess();
        });
      }
      function handleModelSuccess(data) {
        //选择用户后的回调
        let params = {
          userNames: '',
          userRealNames: '',
          roleCode: dataRef?.code,
          roleDesc: dataRef?.description,
        };
        let userNames = ref([]);
        let userRealNames = ref([]);
        data.forEach((item) => {
          userNames.value.push(item.name);
          userRealNames.value.push(item.realName);
        });
        params.userNames = userNames.value.join(',');
        params.userRealNames = userRealNames.value.join(',');
        getAddRoleUser(params).then(() => {
          handleSuccess();
        });
      }

      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        dataRef,
        handleCreate,
        handleDelete,
        registerTable,
        registerModal,
        handleSuccess,
        handleModelSuccess,
        getChange,
      };
    },
  });
</script>
