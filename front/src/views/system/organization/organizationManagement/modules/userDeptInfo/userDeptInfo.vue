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
      <div style="min-height: 100px">{{ deptNames }}</div>

      <Divider />
      <div class="flex">
        <div>所属用户组的角色：</div>
        <div>
          <div v-for="(item, index) in adminDeptMap" :key="index"
            >{{ item.label }}:{{ item.value }}</div
          >
        </div>
      </div>
    </Card>
    <DeptInfoDrawer @register="registerChildDrawer" @success="handleSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getUserAdminDept, getSetUserAdminDept } from '/@/api/system/system';
  import { Card, Divider } from 'ant-design-vue';
  import { useDrawer } from '/@/components/Drawer';
  import DeptInfoDrawer from './userDeptInfoDrawer.vue';
  interface deptMap {
    label: '';
    value: '';
  }
  export default defineComponent({
    name: 'userDeptInfo',
    components: {
      BasicDrawer,
      Card,
      DeptInfoDrawer,
      Divider,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let dataRef = reactive({});
      let title = ref('');
      let deptNames = ref('');
      let deptCodes = ref('');
      let adminDeptMap = ref<deptMap[]>([]);
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于用户：${dataRef.realName} 的部门`;
        deptNames.value = '';
        deptCodes.value = '';
        selectDeptList();
      });

      const getTitle = computed(() => title.value);
      async function selectDeptList() {
        let params = {
          userName: dataRef.user.name,
        };
        await getUserAdminDept(params).then((res) => {
          let data = res;
          if (data.adminDeptMap) {
            for (let v in data.adminDeptMap) {
              adminDeptMap.value.push({
                label: v as any,
                value: data.adminDeptMap[v],
              });
            }
          }
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
            userName: dataRef.user.name,
          };
          console.log('params', params);
          getSetUserAdminDept(params).then(() => {
            closeDrawer();
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
        handleSuccess,
        deptNames,
        deptCodes,
        registerChildDrawer,
        handleCreate,
        handleDelete,
        adminDeptMap,
      };
    },
  });
</script>
