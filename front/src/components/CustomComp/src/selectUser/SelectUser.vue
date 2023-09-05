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
        <Tabs v-model:activeKey="activeKey" type="card" centered>
          <TabPane key="1" tab="最近">
            <div class="overflow-y-auto">
              <BasicTree
                title="最近"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="recentSelectedList"
                @select="userHandleSelect"
                treeKey="name"
                @dblclick="dblclickToRight"
                multiple
                v-model:selectedKeys="userSelectedKeys"
              />
            </div>
          </TabPane>
          <TabPane key="2" tab="部门">
            <div style="height: 210px" class="overflow-y-auto">
              <BasicTree
                title="部门"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="departmentList"
                :expandedKeys="expandedKeys"
                :fieldNames="{ key: 'id', title: 'name' }"
                @select="handleSelect"
                treeKey="id"
              />
            </div>
            <Divider style="border-color: #7cb305" dashed />
            <div style="height: 210px" class="overflow-y-auto">
              <BasicTree
                title="部门用户"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="departUser"
                @select="userHandleSelect"
                treeKey="name"
                @dblclick="dblclickToRight"
                multiple
                v-model:selectedKeys="userSelectedKeys"
              />
            </div>
          </TabPane>
          <TabPane key="3" tab="角色">
            <div style="height: 210px" class="overflow-y-auto">
              <BasicTree
                title="角色"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="roleList"
                :fieldNames="{ key: 'code', title: 'description' }"
                @select="handleSelect"
                treeKey="code"
              />
            </div>
            <Divider style="border-color: #7cb305" dashed />
            <div style="height: 210px" class="overflow-y-auto">
              <BasicTree
                title="角色用户"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="roleUser"
                @select="userHandleSelect"
                treeKey="name"
                @dblclick="dblclickToRight"
                multiple
                v-model:selectedKeys="userSelectedKeys"
              />
            </div>
          </TabPane>
          <TabPane key="4" tab="用户组">
            <div style="height: 210px" class="overflow-y-auto">
              <BasicTree
                title="用户组"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="groupList"
                :fieldNames="{ key: 'code', title: 'description' }"
                @select="handleSelect"
                treeKey="code"
              />
            </div>
            <Divider style="border-color: #7cb305" dashed />
            <div style="height: 210px" class="overflow-y-auto">
              <BasicTree
                title="用户组用户"
                search
                draggable
                :clickRowToExpand="false"
                :treeData="groupUser"
                @select="userHandleSelect"
                treeKey="name"
                @dblclick="dblclickToRight"
                multiple
                v-model:selectedKeys="userSelectedKeys"
              />
            </div>
          </TabPane>
        </Tabs>
      </Card>
      <div class="flex flex-1 justify-center items-center flex-col">
        <div style="margin-bottom: 30px">
          <DoubleRightOutlined style="font-size: 30px; cursor: pointer" @click="toRightMore" />
        </div>
        <div
          ><DoubleLeftOutlined style="font-size: 30px; cursor: pointer" @click="toLeftMore"
        /></div>
      </div>
      <Card class="w-4/9">
        <div class="overflow-y-auto">
          <BasicTree
            title="已选用户"
            search
            draggable
            :clickRowToExpand="false"
            v-model:treeData="selectUserList"
            @select="hadUserHandleSelect"
            treeKey="name"
            @dblclick="dblclickToLeft"
            multiple
            v-model:selectedKeys="hadUserSelectedKeys"
          />
        </div>
      </Card>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Card, Tabs, TabPane, Divider } from 'ant-design-vue';
  import { DoubleRightOutlined, DoubleLeftOutlined } from '@ant-design/icons-vue';
  import { BasicTree } from '/@/components/Tree';
  import { getUserMultiSel, getDeptUsers, getRoleUsers, getGroupUsers } from '/@/api/system/system';
  import { useMessage } from '/@/hooks/web/useMessage';
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
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      const { createMessage } = useMessage();
      let activeKey = ref('2');
      let recentSelectedList = ref<Recordable>([]); //近期选择的
      let roleList = ref<Recordable>([]); //角色
      let roleUser = ref<Recordable>([]); //角色用户
      let groupList = ref<Recordable>([]); //用户组
      let groupUser = ref<Recordable>([]); //用户组用户
      let departmentList = ref<Recordable>([]); //部门
      let expandedKeys = ref<Recordable>([]); //部门展开
      let departUser = ref<Recordable>([]); //部门用户
      let initUsers = ref<Recordable>([]); //初始用户
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
        console.log('data?.users', data?.users);
        data?.users?.forEach((item) => {
          item.key = item.name;
          item.title = item.realName;
        });

        selectUserList.value = data?.users || [];
        type.value = data?.type == 0 ? 0 : 1;
        hadUserSelectedKeys.value = [];
        selectedNodes.value = [];
        userSelectedKeys.value = [];

        initData();
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
          // const types = ['jobNum', 'user'];
          if (unref(type) === 0) {
            //工号管理 | 组织人员  | 入口
            if (unref(selectUserList).length != 1) {
              createMessage.warning('只能选择一个用户');
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
          roleList.value = res.roleList;
          groupList.value = res.groupList;
          departmentList.value = res.departmentList || [];
          if (departmentList.value && departmentList.value.length > 0) {
            expandedKeys.value = [departmentList.value[0].id];
          }
        });
      }
      function handleSelect(e, trc) {
        selectedNodes.value = [];
        userSelectedKeys.value = [];
        //选择返回
        const active = unref(activeKey);

        if (e && e.length > 0) {
          switch (active) {
            case '1':
              break;
            case '2':
              const rec2 = trc.node.dataRef;
              getDeptUsersList(rec2.code);
              break;
            case '3':
              const rec3 = trc.node.dataRef;
              getRoleUsersList(rec3.code);
              break;
            case '4':
              const rec4 = trc.node.dataRef;
              getGroupUsersList(rec4.code);
              break;
            default:
              break;
          }
        }
      }

      function getDeptUsersList(deptCode) {
        //通过部门获取用户
        let params = {
          op: 'getDeptUsers',
          deptCode: deptCode,
          isIncludeChildren: false,
          limitDepts: '',
        };
        getDeptUsers(params).then((res) => {
          departUser.value = res?.list || [];
          departUser.value?.forEach((item) => {
            item.key = item.name;
            item.title = item.realName;
          });
        });
      }
      function getRoleUsersList(code) {
        //通过角色获取用户
        let params = {
          roleCode: code,
        };
        getRoleUsers(params).then((res) => {
          roleUser.value = res || [];

          roleUser.value?.forEach((item) => {
            item.key = item.name;
            item.title = item.realName;
          });
        });
      }
      function getGroupUsersList(code) {
        //通过用户组获取用户
        let params = {
          groupCode: code,
        };
        getGroupUsers(params).then((res) => {
          groupUser.value = res || [];
          groupUser.value?.forEach((item) => {
            item.key = item.name;
            item.title = item.realName;
          });
        });
      }
      function userHandleSelect(e, trc) {
        hadSelectRecord = {};
        selectedNodes.value = trc.selectedNodes;
        if (e) {
          hadSelectRecord = trc.node.dataRef;
        }
      }

      function toRightMore() {
        if (selectedNodes.value.length > 0) {
          let names = selectUserList.value.map((item) => item.name);
          selectedNodes.value?.forEach((item) => {
            item.title = item.realName;
            if (!names.includes(item.name)) selectUserList.value.push(item);
          });
        }
      }

      function toRightOne() {
        if (hadSelectRecord) {
          let isHad = selectUserList.value.some((item) => item.name == hadSelectRecord.name);
          if (!isHad) {
            hadSelectRecord.title = hadSelectRecord.realName;
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

      function toLeftMore() {
        selectUserList.value = selectUserList.value.filter(
          (item) => !hadUserSelectedKeys.value.includes(item.name),
        );
        hadUserSelectedKeys.value = [];
      }
      function toLeftOne() {
        if (selectUserListRecord) {
          selectUserList.value = selectUserList.value.filter(
            (item) => item.name != selectUserListRecord.name,
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

      onMounted(() => {
        // initData();
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
        initUsers,
        handleSelect,
        toRightMore,
        toLeftMore,
        roleUser,
        groupUser,
        departUser,
        userHandleSelect,
        hadSelectRecord,
        selectUserList,
        hadUserHandleSelect,
        selectUserListRecord,
        dblclickToRight,
        dblclickToLeft,
        userSelectedKeys,
        hadUserSelectedKeys,
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
