<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    @ok="handleSubmit"
  >
    <Card title="部门管理" style="width: 100%">
      <template #extra>
        <a-button type="primary" style="margin-right: 10px" @click="handleCreate">新增</a-button>
        <a-button type="default" @click="handleDelete">清空</a-button>
      </template>
      <p>{{ deptNames }}</p>
    </Card>
    <DeptInfoDrawer @register="registerChildDrawer" @success="handleSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getRoleAdminDept, getSetRoleAdminDept } from '/@/api/system/system';
  import { Card } from 'ant-design-vue';
  import { useDrawer } from '/@/components/Drawer';
  import DeptInfoDrawer from './deptInfoDrawer.vue';

  export default defineComponent({
    name: 'deptInfo',
    components: {
      BasicDrawer,
      Card,
      DeptInfoDrawer,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const treeData = ref<TreeItem[]>([]);
      let dataRef = reactive({});
      let title = ref('');
      let deptNames = ref('');
      let deptCodes = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于角色：${dataRef.description} 的部门`;
        selectDeptList();
      });

      const getTitle = computed(() => title.value);
      async function selectDeptList() {
        let params = {
          roleCode: dataRef.code,
        };
        await getRoleAdminDept(params).then((res) => {
          deptNames.value = res.deptNames;
          deptCodes.value = res.depts;
        });
      }

      async function handleSubmit() {
        try {
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          let params = {
            deptCodes: deptCodes.value,
            roleCode: dataRef.code,
            roleDesc: dataRef.description,
          };
          getSetRoleAdminDept(params).then(() => {
            closeDrawer();
            dataRef = {};
            emit('success');
          });
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }
      const [registerChildDrawer, { openDrawer }] = useDrawer();
      function handleCreate() {
        openDrawer(true, {
          isUpdate: false,
          record: dataRef,
        });
      }
      function handleSuccess(keys, data) {
        deptCodes.value = deptCodes.value ? deptCodes.value + ',' + keys : keys;
        let names = data.map((item) => item.name).join(',');
        deptNames.value = deptNames.value ? deptNames.value + ',' + names : names;
      }

      function handleDelete() {
        deptCodes.value = '';
        deptNames.value = '';
      }

      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        treeData,
        dataRef,
        handleSuccess,
        deptNames,
        deptCodes,
        registerChildDrawer,
        handleCreate,
        handleDelete,
      };
    },
  });
</script>
