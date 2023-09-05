<template>
  <ScrollContainer class="pr-4" v-loading="loading" :loading-tip="t('common.loadingText')">
    <div class="flex justify-around w-1/3 m-auto">
      <a-button @click="handleResetFields">重置</a-button>
      <a-button type="primary" @click="customSubmitFunc">保存</a-button>
    </div>
    <BasicForm @register="registerForm">
      <template #handlePerson>
        <div class="flex flex-wrap cursor-pointer">
          <div class="mr-2 cursor-pointer hover:underline" @click="handleSelf">本人</div>
          <div class="mr-2 cursor-pointer hover:underline" @click="handleSelectRole">选择角色</div>
          <div class="mr-2 cursor-pointer hover:underline" @click="handleSelectPosition">
            选择职位
          </div>
          <div class="mr-2 cursor-pointer hover:underline" @click="handleSelectUser">选择用户</div>
          <div class="mr-2 cursor-pointer hover:underline" @click="handleSendPerson">发起人</div>
          <div class="mr-2 cursor-pointer hover:underline" @click="handleSelfSelectUser">
            自选用户
          </div>
        </div>
      </template>
      <template #trFlag="{ model }">
        <div>
          <span title="允许上一节点处理者选择用户">
            <Checkbox v-model:checked="model['flagSel']">选择用户</Checkbox>
          </span>
          <span>
            <Checkbox v-model:checked="model['flagDiscardFlow']">放弃流程</Checkbox>
          </span>
          <span>
            <Checkbox v-model:checked="model['flagDelFlow']">删除流程</Checkbox>
          </span>
          <span>
            <Checkbox v-model:checked="model['flagEditAttach']">编辑附件</Checkbox>
          </span>
          <span>
            <Checkbox v-model:checked="model['flagDelAttach']">删除附件</Checkbox>
          </span>
          <span title="根据条件判断走相应的分支线">
            <Checkbox v-model:checked="model['flagXorRadiate']">条件分支</Checkbox>
          </span>
          <span
            title="节点有多条路径汇合，如果置为异步，则只要有其中的一条到达，节点就会被激活，否则，只有当所有路径都到达后才会继续"
          >
            <Checkbox v-model:checked="model['flagXorAggregate']">异步聚合</Checkbox>
          </span>
          <span title="流程处理者可以拒绝流程，同时流程结束">
            <Checkbox v-model:checked="model['flagFinishFlow']">拒绝流程</Checkbox>
          </span>
          <span title="模板套红（强制）">
            <Checkbox v-model:checked="model['flagReceiveRevise']">模板套红</Checkbox>
          </span>
          <span title="加盖印章（非强制）">
            <Checkbox v-model:checked="model['flagSeal']">加盖印章</Checkbox>
          </span>
          <span title="同意并结束流程，可用于非开始节点">
            <Checkbox v-model:checked="model['flagModify']">流程抄送</Checkbox>
          </span>
          <span title="同意并结束流程，可用于非开始节点">
            <Checkbox v-model:checked="model['flagAgreeAndFinish']">结束流程</Checkbox>
          </span>
          <span
            title="同一节点中有多人处理时，每个人都可以立即往下提交，并且不能更改下一节点上之前被选择的用户"
          >
            <Checkbox v-model:checked="model['flagXorFinish']">异步提交</Checkbox>
          </span>
          <span
            title="同一节点中有多人处理时，退回时不会忽略本节点其他人及其他节点上的待办记录，并且在处理完毕再次提交时，不能更改之前选择的用户"
          >
            <Checkbox v-model:checked="model['flagXorReturn']">异步退回</Checkbox>
          </span>
        </div>
      </template>
      <template #isDelayTr="{ model }">
        <div>
          <div>
            <Checkbox v-model:checked="model['isDelayed']">延迟</Checkbox>
            <Input v-model:value="model['timeDelayedValue']" />
            <Select :options="timeDelayedUnitList" v-model:value="model['timeDelayedUnit']" />
          </div>
          <Checkbox v-model:checked="model['isDelayed']">前一用户可修改延迟时间</Checkbox>
        </div>
      </template>
      <template #fieldWriteText="{ model, field }">
        <div class="flex items-center">
          <InputTextArea v-model="model[field]" :disabled="true" />
          <a-button class="ml-2" type="primary" @click="handleSelectFieldWriteText">选择</a-button>
        </div>
      </template>
      <template #fieldHideText="{ model, field }">
        <div class="flex items-center">
          <InputTextArea v-model="model[field]" :disabled="true" />
          <a-button class="ml-2" type="primary" @click="handleSelectFieldWriteText">选择</a-button>
        </div>
      </template>

      <template #imgComb>
        <a-button type="primary">配置</a-button>
      </template>
    </BasicForm>
    <SelectPositionModal
      @register="registerModalSelectPostion"
      @success="handleSuccessSelectPosition"
    />
    <SelectRoleModal @register="registerModalSelectRole" @success="handleSuccessSelectRole" />
    <SelectUser @register="registerModalSelectUser" @success="handleSuccessSelectUser" />
    <SelectFieldWriteTextModal
      @register="registerModalSelectFieldWriteText"
      @success="handleSuccessSelectFieldWriteText"
    />
  </ScrollContainer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { ScrollContainer } from '/@/components/Container';
  import { formSchema } from './nodeEdit.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { Checkbox, Input, Select } from 'ant-design-vue';
  import { useModal } from '/@/components/Modal';
  import { SelectUser } from '/@/components/CustomComp/index';
  import SelectPositionModal from './modules/SelectPositionModal.vue';
  import SelectRoleModal from './modules/SelectRoleModal.vue';
  import SelectFieldWriteTextModal from './modules/SelectFieldWriteTextModal.vue';
  const InputTextArea = Input.TextArea;
  const { t } = useI18n();
  const loading = ref(false);
  const dataRef = ref({});

  const customSubmitFunc = async () => {
    try {
      const values = await validate();
      // TODO custom api
      console.log(values);
      // emit('success', { isUpdate: unref(isUpdate), values: { ...values, id: rowId.value } });
    } finally {
    }
  };
  const [registerForm, { setFieldsValue, updateSchema, resetFields, validate }] = useForm({
    labelWidth: 120,
    schemas: formSchema,
    showActionButtonGroup: false,
    actionColOptions: {
      span: 23,
    },
    submitFunc: customSubmitFunc,
  });

  const handleResetFields = () => {
    resetFields();
  };

  const timeDelayedUnitList = [
    {
      value: '0',
      label: '天',
    },
    {
      value: '1',
      label: '小时',
    },
    {
      value: '2',
      label: '工作日',
    },
    {
      value: '3',
      label: '工作小时',
    },
  ];

  //自己
  const handleSelf = () => {
    console.log('本人');
    setSpanMode('用户', '本人');
  };

  const [registerModalSelectRole, { openModal: openSelectRole }] = useModal();
  //选择角色
  const handleSelectRole = () => {
    console.log('选择角色');
    openSelectRole(true, {});
  };
  //选择角色回调
  const handleSuccessSelectRole = () => {
    console.log('选择角色回调');
    setSpanMode('角色', '角色');
  };

  const [registerModalSelectPostion, { openModal: openSelectPosition }] = useModal();
  //选择职位
  const handleSelectPosition = () => {
    console.log('选择职位');
    openSelectPosition(true, {});
  };

  //选择职位回调
  const handleSuccessSelectPosition = () => {
    console.log('职位回调');
    setSpanMode('职位', '职位');
  };

  const [registerModalSelectUser, { openModal: openSelectUser }] = useModal();
  //选择用户
  const handleSelectUser = () => {
    console.log('选择用户');
    openSelectUser(true, {});
  };
  //选择用户回调
  const handleSuccessSelectUser = (users: Recordable[]) => {
    console.log('选择用户回调', users);
    let userRealName = '';
    if (users && users.length) {
      let userNames = [];
      users.forEach((item) => {
        userNames.push(item.title);
      });
      userRealName = userNames.join(',');
    }
    setSpanMode('用户', userRealName);
  };

  //发起人
  const handleSendPerson = () => {
    console.log('发起人');
    setSpanMode('发起人', '发起人');
  };
  //自选用户
  const handleSelfSelectUser = () => {
    console.log('自选用户');
    setSpanMode('自选用户', '自选用户');
  };

  const setSpanMode = (spanMode, userRealName) => {
    dataRef.value.spanMode = spanMode;
    setFieldsValue({ spanMode: dataRef.value.spanMode, userRealName });
  };

  const [registerModalSelectFieldWriteText, { openModal: openSelectFieldWriteText }] = useModal();
  //可写字段打开
  const handleSelectFieldWriteText = () => {
    openSelectFieldWriteText(true, {});
  };
  //可写字段回调
  const handleSuccessSelectFieldWriteText = () => {};
</script>
