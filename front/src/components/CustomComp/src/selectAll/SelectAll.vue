<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :minHeight="480"
  >
    <div class="flex justify-between">
      <Card class="w-4/9">
        <Tabs v-model:activeKey="activeKey" type="card" centered @change="getActiveKey">
          <TabPane key="1" tab="部门" v-if="tabKeys.includes('1')">
            <div style="height: 450px" class="overflow-y-auto">
              <BasicTree
                title="部门"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="departmentList"
                :expandedKeys="expandedKeys"
                @select="handleSelect"
                treeKey="id"
              />
            </div>
          </TabPane>
          <TabPane key="2" tab="角色" v-if="tabKeys.includes('2')">
            <div style="height: 450px" class="overflow-y-auto">
              <BasicTree
                title="角色"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="roleList"
                @select="handleSelect"
                treeKey="code"
              />
            </div>
          </TabPane>
          <TabPane key="3" tab="用户组" v-if="tabKeys.includes('3')">
            <div style="height: 450px" class="overflow-y-auto">
              <BasicTree
                title="用户组"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="groupList"
                @select="handleSelect"
                treeKey="code"
              />
            </div>
          </TabPane>
          <TabPane key="4" tab="人员" v-if="tabKeys.includes('4')" />
        </Tabs>
      </Card>
      <div>
        <Divider type="vertical" style="border-color: #7cb305; height: 100%" dashed />
      </div>
      <Card class="w-4/9">
        <div class="overflow-y-auto">
          <BasicTree
            title="权限范围"
            search
            draggable
            :clickRowToExpand="false"
            :treeData="selectUserList"
            @select="hadUserHandleSelect"
            @dblclick="dblclickToLeft"
            :selectedKeys="hadUserSelectedKeys"
            treeKey="newTitle"
          >
            <template #title="{ newTitle, key: treeKey }">
              <div class="flex w-full justify-between">
                <div>{{ newTitle }}</div>
                <div>
                  <DeleteOutlined style="color: red" @click="deleteSelected(treeKey)" />
                </div>
              </div>
            </template>
          </BasicTree>
        </div>
      </Card>
    </div>

    <SelectUser @register="registerSelectUser" @success="handleSuccess" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Card, Tabs, TabPane, Divider } from 'ant-design-vue';
  import { DoubleRightOutlined, DoubleLeftOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import { BasicTree } from '/@/components/Tree';
  import { getUserMultiSel, getDeptUsers, getRoleUsers, getGroupUsers } from '/@/api/system/system';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import SelectUser from '../selectUser/SelectUser.vue';
  export default defineComponent({
    name: 'DeptModal',
    components: {
      BasicModal,
      Card,
      Tabs,
      TabPane,
      Divider,
      BasicTree,
      DoubleRightOutlined,
      DoubleLeftOutlined,
      SelectUser,
      DeleteOutlined,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      const { createMessage } = useMessage();
      let activeKey = ref('2');
      const tabKeys = ref(['1', '2', '3', '4']);
      let recentSelectedList = ref<Recordable>([]); //近期选择的
      let roleList = ref<Recordable>([]); //角色
      let groupList = ref<Recordable>([]); //用户组
      let departmentList = ref<Recordable>([]); //部门
      let expandedKeys = ref<Recordable>([]); //部门展开
      let hadSelectRecord = reactive<Recordable>({}); //左侧选中用户
      let selectUserList = ref<Recordable>([]); //已选用户
      const selectedNodes = ref<Recordable>([]); //多选人员
      const userSelectedKeys = ref<Recordable>([]); //用户选中userSelectedKeys
      const type = ref(1);
      let selectUserListRecord = reactive<Recordable>({}); //右侧选中用户
      const hadUserSelectedKeys = ref<Recordable>([]); //已选用户选中
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '60%' });
        isUpdate.value = !!data?.isUpdate;
        selectUserList.value = data?.users || [];
        type.value = data.type == 1 ? 1 : 0; //传1是只能选择一条 其他是所有
        hadUserSelectedKeys.value = [];
        selectedNodes.value = [];
        userSelectedKeys.value = [];
        if (data.tabKeys && data.tabKeys.length > 0) {
          tabKeys.value = data.tabKeys;
        }
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '选择用户' : '选择用户'));

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });
          // TODO custom api
          if (unref(selectUserList).length === 0) {
            createMessage.warning('请选择用户');
            return;
          }
          if (unref(type) === 1) {
            if (unref(selectUserList).length != 1) {
              createMessage.warning('只能选择一条数据');
              return;
            }
            closeModal();
            emit('success', unref(selectUserList));
          } else {
            closeModal();
            emit('success', unref(selectUserList));
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      const filterOption = (inputValue: string, option) => {
        return option.description.indexOf(inputValue) > -1;
      };

      function initData() {
        //初始化各个tab数据
        getUserMultiSel().then((res: any) => {
          recentSelectedList.value = res.recentSelectedList || [];
          recentSelectedList.value.forEach((item) => {
            item.key = item.name;
            item.title = item.realName;
          });
          roleList.value = res.roleList || [];

          roleList.value = setChildren(roleList.value, 'code', 'description');
          groupList.value = res.groupList;
          groupList.value = setChildren(groupList.value, 'code', 'description');
          departmentList.value = res.departmentList || [];
          if (departmentList.value && departmentList.value.length > 0) {
            expandedKeys.value = [departmentList.value[0].id];
            departmentList.value = setChildren(departmentList.value, 'id');
          }
        });
      }
      function setChildren(node, key = 'code', name = 'name') {
        node.forEach((item) => {
          item.show = false;
          item.key = item[key];
          item.title = item[name];
          item.newTitle = item[name];
          if (item.children && Array.isArray(item.children) && item.children.length > 0) {
            item.isLeaf = false;
            setChildren(item.children, key, name);
          } else {
            item.isLeaf = true;
          }
        });
        return node;
      }
      function handleSelect(e, trc) {
        hadSelectRecord = {};
        if (e) {
          hadSelectRecord = trc.node.dataRef;
          // 权限拥有者的类型 0用户组,1用户,2角色,3部门
          hadSelectRecord.privType =
            activeKey.value === '1'
              ? 3
              : activeKey.value === '2'
              ? 2
              : activeKey.value === '3'
              ? 0
              : 2;
          toRightOne();
        }
      }
      function userHandleSelect(e, trc) {
        hadSelectRecord = {};
        selectedNodes.value = trc.selectedNodes;
        if (e) {
          hadSelectRecord = trc.node.dataRef;
        }
      }

      function toRightOne() {
        if (hadSelectRecord) {
          let isHad = selectUserList.value.some((item) => item.key == hadSelectRecord.key);
          if (!isHad) {
            hadSelectRecord.title = hadSelectRecord.newTitle;
            selectUserList.value.push(hadSelectRecord);
          }
        }
      }
      function hadUserHandleSelect(e, trc) {
        selectUserListRecord = {};
        if (e) {
          selectUserListRecord = trc.node.dataRef;
        }
      }

      function toLeftOne() {
        if (selectUserListRecord) {
          selectUserList.value = selectUserList.value.filter(
            (item) => item.key != selectUserListRecord.key,
          );
        }
      }

      //双击向右
      function dblclickToRight(_, node) {
        let { dataRef } = node;
        hadSelectRecord = dataRef;
        toRightOne();
      }
      //双击向左
      function dblclickToLeft(_, node) {
        let { dataRef } = node;
        selectUserListRecord = dataRef;
        toLeftOne();
      }

      //已选删除
      const deleteSelected = (key: string) => {
        selectUserList.value = selectUserList.value.filter((item) => item.key != key);
      };

      const [registerSelectUser, { openModal }] = useModal();
      const oldActiveKey = ref('2');
      const getActiveKey = (key: string) => {
        if (key === '4') {
          activeKey.value = oldActiveKey.value;
          openModal(true, {});
        } else {
          oldActiveKey.value = key;
        }
      };

      const handleSuccess = (records: Recordable) => {
        selectUserList.value = [
          ...selectUserList.value.map((item) => item),
          ...records.map((item) => {
            return { ...item, newTitle: item.realName, privType: 1 };
          }),
        ];
      };

      onMounted(() => {
        initData();
      });
      return {
        registerModal,
        getTitle,
        handleSubmit,
        filterOption,
        activeKey,
        recentSelectedList,
        roleList,
        groupList,
        departmentList,
        expandedKeys,
        handleSelect,
        userHandleSelect,
        hadSelectRecord,
        selectUserList,
        hadUserHandleSelect,
        selectUserListRecord,
        dblclickToRight,
        dblclickToLeft,
        userSelectedKeys,
        hadUserSelectedKeys,
        getActiveKey,
        registerSelectUser,
        handleSuccess,
        deleteSelected,
        tabKeys,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.ant-card-body) {
    padding: 5px;
    height: 530px;
  }
</style>
