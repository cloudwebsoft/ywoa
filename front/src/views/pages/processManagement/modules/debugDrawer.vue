<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="40%"
    @ok="handleSubmit"
    :show-ok-btn="false"
    :cancel-text="'关闭'"
  >
    <CollapseContainer :title="`用户：${dataRef.myRealName}`" :isShow="false" :canExpan="false">
      <template #action>
        <a-button type="primary" class="mr-1" @click="handleSubmit"> 确定 </a-button>
        <Popconfirm
          placement="top"
          title="确定重新登录吗？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="loginAgain"
        >
          <a-button type="primary" class="mr-1"> 重新登录 </a-button>
        </Popconfirm>
        <Dropdown>
          <a class="ant-dropdown-link w-15" @click.prevent>
            操作
            <DownOutlined />
          </a>
          <template #overlay>
            <Menu>
              <MenuItem>
                <a href="javascript:;" @click="handleExpand(true)">全部展开</a>
              </MenuItem>
              <MenuItem>
                <a href="javascript:;" @click="handleExpand(false)">全部收起</a>
              </MenuItem>
            </Menu>
          </template>
        </Dropdown>
      </template>
    </CollapseContainer>
    <CollapseContainer title="主表可写字段" :isShow="activeKey.includes('1')">
      <div v-for="item in dataRef.aryFieldWritable || []" :key="item.fieldName">
        <Checkbox v-model:checked="item.isWrite">{{ item.fieldTitle }}</Checkbox>
      </div>
    </CollapseContainer>
    <CollapseContainer title="嵌套表可写字段" :isShow="activeKey.includes('2')">
      <div v-for="item in dataRef.aryNestFieldWritable || []" :key="item.fieldName">
        <Checkbox v-model:checked="item.isWrite">{{ item.fieldText }}</Checkbox>
      </div>
    </CollapseContainer>
    <CollapseContainer title="主表隐藏字段" :isShow="activeKey.includes('3')">
      <div v-for="item in dataRef.aryFieldHide || []" :key="item.fieldName">
        <Checkbox v-model:checked="item.isHide">{{ item.fieldTitle }}</Checkbox>
      </div>
    </CollapseContainer>
    <CollapseContainer title="嵌套表隐藏字段" :isShow="activeKey.includes('4')">
      <div v-for="item in dataRef.aryNextFieldHide || []" :key="item.fieldName">
        <Checkbox v-model:checked="item.isHide">{{ item.fieldText }}</Checkbox>
      </div>
    </CollapseContainer>
    <CollapseContainer title="验证脚本" :isShow="activeKey.includes('5')">
      <template #action="record">
        <span
          v-if="record.show"
          class="text-blue-800 mr-2 cursor-pointer"
          @click="handleRunValidateScript"
          >运行</span
        >
      </template>
      <a-textarea
        v-model:value="runValidateScriptText"
        :readonly="true"
        :auto-size="{ minRows: 2, maxRows: 8 }"
      />
    </CollapseContainer>
    <CollapseContainer title="结束脚本" :isShow="activeKey.includes('6')">
      <template #action="record">
        <span
          v-if="record.show"
          class="text-blue-800 mr-2 cursor-pointer"
          @click="handleRunFinishScript"
          >运行</span
        >
      </template>
      <a-textarea
        v-model:value="runFinishScriptText"
        :readonly="true"
        :auto-size="{ minRows: 2, maxRows: 8 }"
      />
    </CollapseContainer>
    <CollapseContainer title="流转脚本" :isShow="activeKey.includes('7')">
      <template #action="record">
        <span
          v-if="record.show"
          class="text-blue-800 mr-2 cursor-pointer"
          @click="handleRunDeliverScript"
          >运行</span
        >
      </template>
      <a-textarea
        v-model:value="runDeliverScriptText"
        :readonly="true"
        :auto-size="{ minRows: 2, maxRows: 8 }"
      />
    </CollapseContainer>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { Popconfirm, Checkbox, Dropdown, Menu } from 'ant-design-vue';
  import { DownOutlined } from '@ant-design/icons-vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';

  import {
    getFlowProcess,
    getApplyProps,
    getRunValidateScript,
    getRunFinishScript,
    getRunDeliverScript,
  } from '/@/api/process/process';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { CollapseContainer } from '/@/components/Container/index';
  import { useUserStore } from '/@/store/modules/user';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'debugDrawer',
    components: {
      BasicDrawer,
      Popconfirm,
      Checkbox,
      CollapseContainer,
      Dropdown,
      Menu,
      MenuItem: Menu.Item,
      DownOutlined,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const isUpdate = ref(true);
      let dataRef = ref<any>({});
      const activeKey = ref<any>([]);
      const isRun = ref(false);
      const runValidateScriptText = ref('');
      const runFinishScriptText = ref('');
      const runDeliverScriptText = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = data.isUpdate;
        isRun.value = false;
        runValidateScriptText.value = '';
        runFinishScriptText.value = '';
        runDeliverScriptText.value = '';
        dataRef.value = await getFlowProcess({ myActionId: data.myActionId });
      });

      const getTitle = '调试面板';

      const userStore = useUserStore();
      //重新登录
      function loginAgain() {
        userStore.logout(true);
      }

      async function handleSubmit() {
        try {
          let fieldWriteArr = ref<any>([]);
          let fieldHideArr = ref<any>([]);
          if (unref(dataRef).aryFieldWritable && unref(dataRef).aryFieldWritable.length > 0) {
            unref(dataRef).aryFieldWritable.forEach((item) => {
              if (item.isWrite) fieldWriteArr.value.push(item.fieldName);
            });
          }
          if (
            unref(dataRef).aryNestFieldWritable &&
            unref(dataRef).aryNestFieldWritable.length > 0
          ) {
            unref(dataRef).aryNestFieldWritable.forEach((item) => {
              if (item.isWrite) fieldWriteArr.value.push(`nest.${item.fieldName}`);
            });
          }
          if (unref(dataRef).aryFieldHide && unref(dataRef).aryFieldHide.length > 0) {
            unref(dataRef).aryFieldHide.forEach((item) => {
              if (item.isHide) fieldHideArr.value.push(item.fieldName);
            });
          }
          if (unref(dataRef).aryNextFieldHide && unref(dataRef).aryNextFieldHide.length > 0) {
            unref(dataRef).aryNextFieldHide.forEach((item) => {
              if (item.isHide) fieldHideArr.value.push(`nest.${item.fieldName}`);
            });
          }
          let params = {
            fieldWrite: fieldWriteArr.value.join(','),
            fieldHide: fieldHideArr.value.join(','),
            flowId: unref(dataRef).flowId,
            actionId: unref(dataRef).actionId,
          };
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          getApplyProps(params).then((res) => {
            createMessage.success(res.msg);
            closeDrawer();
            emit('success');
          });
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      //验证脚本
      function handleRunValidateScript(e) {
        e.stopPropagation();
        let params = {
          flowId: dataRef.value.flowId,
          actionId: dataRef.value.actionId,
        };
        isRun.value = true;
        getRunValidateScript(params).then((res) => {
          isRun.value = false;
          runValidateScriptText.value = res.data;
        });
      }

      //结束脚本
      function handleRunFinishScript(e) {
        e.stopPropagation();
        let params = {
          flowId: dataRef.value.flowId,
          actionId: dataRef.value.actionId,
        };
        isRun.value = true;
        getRunFinishScript(params).then((res) => {
          isRun.value = false;
          runFinishScriptText.value = res.data;
        });
      }

      //流转脚本
      function handleRunDeliverScript(e) {
        e.stopPropagation();
        let params = {
          flowId: dataRef.value.flowId,
          myActionId: dataRef.value.myActionId,
        };
        isRun.value = true;
        getRunDeliverScript(params).then((res) => {
          isRun.value = false;
          runDeliverScriptText.value = res.data;
        });
      }

      function handleExpand(val: boolean) {
        activeKey.value = val ? ['1', '2', '3', '4', '5', '6', '7'] : [];
      }
      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        activeKey,
        loginAgain,
        dataRef,
        handleRunValidateScript,
        handleRunFinishScript,
        handleRunDeliverScript,
        isRun,
        runValidateScriptText,
        runFinishScriptText,
        runDeliverScriptText,
        handleExpand,
      };
    },
  });
</script>
<style lang="less" scoped>
  ::v-deep .ant-card-body {
    padding: 10px 0;
  }
  textarea.ant-input {
    background-color: black;
    color: white;
  }
</style>
