<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <Transfer
      v-model:target-keys="targetKeys"
      :data-source="mockData"
      show-search
      :titles="[' · 候选角色', ' · 已选角色']"
      :filter-option="filterOption"
      :render="(item) => item.title"
      :list-style="{
        width: '500px',
        height: '500px',
      }"
      @change="handleChange"
      @search="handleSearch"
    />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Divider, Transfer } from 'ant-design-vue';
  import { getRolePostTransferPage, getRolePostUpdate } from '/@/api/system/system';
  import { useMessage } from '/@/hooks/web/useMessage';

  interface MockData {
    key: string;
    title: string;
    description: string;
    chosen: boolean;
  }

  export default defineComponent({
    name: 'SelectRoleModal',
    components: { BasicModal, Divider, Transfer },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive({});
      const { createMessage } = useMessage();

      let mockData = ref<MockData[]>([]);

      let targetKeys = ref<string[]>([]);
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '50%' });
        isUpdate.value = !!data?.isUpdate;
        dataRef = data.record;
        mockData.value = [];
        targetKeys.value = [];
        getRoleUserList();
      });

      const getTitle = '新增';

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });
          // TODO custom api
          if (targetKeys.value.length == 0) {
            createMessage.warning('选择数据不能为空');
            return;
          }
          let params = {
            ids: targetKeys.value.join(','),
            roleCode: dataRef.code,
          };
          await getRolePostUpdate(params).then((res) => {
            closeModal();
            emit('success');
          });
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      async function getRoleUserList() {
        let params = {
          roleCode: dataRef.code,
        };
        await getRolePostTransferPage(params).then((res) => {
          let data = res.list || [];
          data.forEach((item) => {
            item.key = item.id;
            item.title = item.name;
            item.description = item.name;
          });
          mockData.value = data;
        });
      }

      const filterOption = (inputValue: string, option: MockData) => {
        return option.description.indexOf(inputValue) > -1;
      };
      const handleChange = (keys: string[], direction: string, moveKeys: string[]) => {};

      const handleSearch = (dir: string, value: string) => {};

      return {
        registerModal,
        getTitle,
        handleSubmit,
        mockData,
        targetKeys,
        filterOption,
        handleChange,
        handleSearch,
      };
    },
  });
</script>
