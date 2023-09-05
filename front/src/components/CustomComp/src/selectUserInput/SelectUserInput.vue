<template>
  <div>
    <FormItemRest>
      <InputSearch
        v-model:value="selectValue"
        :placeholder="placeholder"
        :size="size"
        readonly
        @search="handleSearch"
        enterButton="选择"
      />
      <SelectUser @register="registerSelectUserModal" @success="handleSuccess" />
    </FormItemRest>
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { InputSearch, Form } from 'ant-design-vue';
  import { SelectUser } from '/@/components/CustomComp';
  import { useModal } from '/@/components/Modal';
  import { isArray } from '/@/utils/is';
  const FormItemRest = Form.ItemRest;

  const emit = defineEmits(['change', 'update:value']);
  const props = defineProps({
    size: { type: String, default: 'default' },
    placeholder: { type: String, default: '请选择人员' },
    value: { type: String, default: '' },
    //已选人员
    hadUsers: { type: Array, default: () => [] },
    //0只能选择一个
    type: { type: [Number, String], default: 1 },
  });
  const selectValue = ref('');
  const oldUsers = ref([]);
  const [registerSelectUserModal, { openModal }] = useModal();
  const handleOpen = () => {
    const hadSelect =
      isArray(props.hadUsers) && props.hadUsers.length > 0
        ? JSON.parse(JSON.stringify(props.hadUsers))
        : [];
    openModal(true, {
      users: hadSelect.length == 0 ? oldUsers.value : [],
      type: props.type,
    });
  };
  //点击按钮
  const handleSearch = () => {
    handleOpen();
  };
  const handleSuccess = (data) => {
    oldUsers.value = data;
    console.log({ oldUsers11: oldUsers.value });
    console.log({ hadUsers: props.hadUsers });
    if (data && data.length > 0) {
      let dataRef = {};
      dataRef['userNames'] = data.map((item) => item.name).join(',');
      dataRef['userRealNames'] = data.map((item) => item.realName).join(',');
      console.log('dataRef', dataRef);
      emit('update:value', dataRef['userRealNames']);
      emit('change', dataRef['userRealNames'], { selectUsers: data, dataRef });
    }
  };
  watch(
    () => props.value,
    (newValue) => {
      console.log('newValue', newValue);
      selectValue.value = newValue;
    },
    {
      // deep: true,
      immediate: true,
    },
  );
  watch(
    () => props.hadUsers,
    (newValue) => {
      console.log('hadUsers', newValue);
    },
    {
      deep: true,
    },
  );
</script>
