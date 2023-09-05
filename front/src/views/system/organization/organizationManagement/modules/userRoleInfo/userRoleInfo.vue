<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    @ok="handleSubmit"
  >
    <Card title="设置角色" style="width: 100%">
      <div style="min-height: 40px">
        <Select
          v-model:value="roleCodes"
          :options="roleList"
          mode="multiple"
          placeholder="请选择"
          style="width: 100%"
          :fieldNames="{ label: 'description', value: 'code' }"
          @change="seleceChange"
        />
      </div>

      <Divider />
      <div>
        <div>其用户组所属的角色：{{ groupRoleDesc }}</div>
        <div>注：用户默认属于“全部用户”角色</div>
      </div>
    </Card>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getSetRoleOfUser, getUserSetRole, getRoleMultilSel } from '/@/api/system/system';
  import { Card, Divider, Select } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  interface userRoleInfo {
    realName?: '';
    code?: '';
    roleCodes?: [];
    roleNames?: '';
    roleDescs?: '';
    user: {};
    groupRoleDesc?: '';
  }
  export default defineComponent({
    name: 'userRoleInfo',
    components: {
      BasicDrawer,
      Card,
      Divider,
      Select,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const treeData = ref([]);
      let dataRef = reactive<userRoleInfo>({});
      let title = ref('');
      let roleList = ref([]);
      let roleCodes = ref([]);
      let roleNames = ref('');
      let groupRoleDesc = ref('');
      const { createMessage } = useMessage();
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        roleNames.value = dataRef.roleNames || '';
        roleCodes.value = dataRef.roleCodes || [];
        groupRoleDesc.value = dataRef.groupRoleDesc || '';
        title.value = `属于：${dataRef.realName} 的角色`;
        await getRoleMultilSel().then((res) => {
          console.log('res111>', res);
          roleList.value = res || [];
        });
      });

      const getTitle = computed(() => title.value);
      async function selectUserRole() {
        let params = {
          userName: dataRef.user.name,
        };
        await getUserSetRole(params).then((res) => {
          console.log('res==>', res);
        });
      }

      async function handleSubmit() {
        try {
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          if (!roleCodes.value || roleCodes.value.length === 0) {
            createMessage.warning('选择数据不能为空');
            return;
          }
          let params = {
            roleDescs: dataRef.roleDescs,
            roleCodes: roleCodes.value.join(','),
            userName: dataRef.user.name,
          };
          getSetRoleOfUser(params).then(() => {
            closeDrawer();
            emit('success');
          });
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      function seleceChange(e, node) {
        if (e.length > 0) {
          dataRef.roleDescs = node.map((item) => item.description).join(',');
        } else {
          dataRef.roleDescs = '';
        }
      }

      return {
        registerDrawer,
        getTitle,
        roleList,
        handleSubmit,
        treeData,
        dataRef,
        roleCodes,
        roleNames,
        seleceChange,
        groupRoleDesc,
      };
    },
  });
</script>
