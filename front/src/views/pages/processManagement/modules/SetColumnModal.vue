<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :maskClosable="false"
  >
    <Transfer
      v-model:target-keys="targetKeys"
      v-model:selected-keys="selectedKeys"
      :data-source="mockData"
      show-search
      :titles="[' · 备选的域', ' · 已选的域']"
      :filter-option="filterOption"
      :render="(item) => item.title"
      :list-style="{
        width: '300px',
        height: '400px',
      }"
      @change="handleChange"
      @search="handleSearch"
      @select-change="handleSelectChange"
    >
      <template #children="{ direction, filteredItems, onItemSelect }">
        <div class="transfer">
          <div v-if="direction === 'right'" class="transfer-right">
            <div
              v-for="(item, index) in filteredItems"
              draggable="true"
              @mouseenter="isTarget(true, item)"
              @mouseleave="isTarget(false, item)"
              @dragstart="handleDragstart(index)"
              @drop.prevent="handleDrop()"
              @dragover.prevent="handleDragover(index)"
              :key="item.key"
              @click="() => checkChange(item.checked, item.key, onItemSelect)"
              class="transfer-right-item cursor-move"
            >
              <Checkbox :checkedKeys="[...targetKeys]" v-model:checked="item.checked" />
              <div class="transfer-right-item-content">
                <span> &nbsp;{{ item.title }}</span>
                <div>
                  <Icon icon="ant-design:menu-outlined" />
                  <Icon
                    icon="ant-design:arrow-up-outlined"
                    class="cursor-pointer ml-2 mr-2"
                    @click.stop="handleSetIndex(index, 'up')"
                  />
                  <Icon
                    icon="ant-design:arrow-down-outlined"
                    class="cursor-pointer"
                    @click.stop="handleSetIndex(index, 'down')"
                  />
                </div>
              </div>
            </div>
          </div>
          <div v-if="direction === 'left'" class="transfer-left">
            <div
              v-for="item in filteredItems"
              :key="item.key"
              class="transfer-left-item"
              @click="() => checkChange(item.checked, item.key, onItemSelect)"
            >
              <Checkbox :checkedKeys="[...targetKeys]" v-model:checked="item.checked" />
              &nbsp; {{ item.title }}
            </div>
          </div>
        </div>
      </template>
    </Transfer>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Transfer, Checkbox } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getFlowListFields, getColProps, saveColProps } from '/@/api/process/process';
  import Icon from '/@/components/Icon';
  interface MockData {
    key: string;
    title: string;
    description: string;
    chosen: boolean;
  }

  export default defineComponent({
    name: 'SetColumnModal',
    components: { BasicModal, Transfer, Checkbox, Icon },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let typeCode = ref('');
      const { createMessage } = useMessage();

      let mockData = ref<MockData[]>([]);
      let targetKeys = ref<string[]>([]);
      let selectedKeys = ref<string[]>([]);
      let displayMode = 0;

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '60%' });
        typeCode.value = data.typeCode;
        displayMode = data.displayMode;
        mockData.value = [];
        targetKeys.value = [];
        await getFieldsList();
        await getColumnProps();
      });

      const getTitle = '设置列表';

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });
          // TODO custom api
          if (targetKeys.value.length == 0) {
            createMessage.warning('选择数据不能为空');
            return;
          }
          let params = {
            typeCode: typeCode.value,
            fields: targetKeys.value.join(','),
          };
          await saveColProps(params);

          closeModal();
          emit('success');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      async function getColumnProps() {
        let params = {
          typeCode: typeCode.value,
          displayMode,
        };
        await getColProps(params).then((res) => {
          let data = res || [];
          data.forEach((item) => {
            item.key = item.name ? item.name : item.field;
            item.title = item.title;
            item.description = item.title;
            targetKeys.value.push(item.name ? item.name : item.field);
          });
        });
      }

      async function getFieldsList() {
        let params = {
          typeCode: typeCode.value,
        };
        await getFlowListFields(params).then((res) => {
          let data = res || [];
          data.forEach((item) => {
            item.key = item.name ? item.name : item.field;
            item.title = item.title ? item.title : item.display;
            item.description = item.title;
          });
          mockData.value = data;
        });
      }
      const filterOption = (inputValue: string, option: MockData) => {
        return option.description.indexOf(inputValue) > -1;
      };

      // 选项在两栏之间转移时的回调函数
      const handleChange = (keys: string[], direction: string, moveKeys: string[]) => {
        // moveKeys.forEach((item) => {
        //   mockData.value.forEach((items, index) => {
        //     if (item === items.key) drawerData.mockData[index].checked = false;
        //   });
        // });
        if (direction === 'right') {
          console.log('handleChange moveKeys', moveKeys);
          console.log('targetKeys.value', targetKeys.value);
          // 将右移的元素置于末尾
          targetKeys.value.push(...moveKeys);
          targetKeys.value.splice(0, moveKeys.length);
          console.log('handleChange tagetKeys', targetKeys.value);
        }
      };

      const handleSearch = (dir: string, value: string) => {};

      // 穿梭框列表图标显示与隐藏
      const isTarget = (flag, e) => {
        let key = false;
        targetKeys.value.forEach((item) => {
          if (e.key === item) key = item;
        });
        if (key !== false) {
          // drawerData.mockData.forEach((item, index) => {
          //   if (e.key === item.key) drawerData.mockData[index].showMenu = flag;
          // });
        }
      };

      let drawerData: any = {};
      const handleDrop = () => {
        console.log('handleDrop targetKeys', targetKeys.value);
        // 删除老的
        console.log('handleDrop drawerData.oldItemIndex', drawerData.oldItemIndex);
        const changeItem = targetKeys.value.splice(drawerData.oldItemIndex, 1)[0];
        console.log('handleDrop changeItem', changeItem);
        // 在列表中目标位置增加新的
        console.log('handleDrop drawerData.newItemIndex', drawerData.newItemIndex);
        targetKeys.value.splice(drawerData.newItemIndex, 0, changeItem);
      };

      const handleDragstart = (index) => {
        drawerData.oldItemIndex = index;
      };
      const handleDragover = (index) => {
        drawerData.newItemIndex = index;
      };

      // 用于判断选中了哪些多选框
      const checkChange = (checked, key, onItemSelect) => {
        mockData.value.forEach((items, index) => {
          if (key === items.key) mockData.value[index].chosen = !checked;
        });
        onItemSelect(key, !checked);
      };

      // 选中项发生改变时的回调函数
      const handleSelectChange = (sourceSelectedKeys, targetSelectedKeys) => {
        mockData.value.forEach((item) => {
          if (sourceSelectedKeys.includes(item.key) || targetSelectedKeys.includes(item.key)) {
            item.checked = true;
          } else {
            // 取消选择
            item.checked = false;
          }
        });
      };

      //右侧排序
      const handleSetIndex = (index: Number, type: String) => {
        switch (type) {
          case 'up':
            if (index > 0) {
              handleDragstart(index);
              handleDragover(index - 1);
              handleDrop();
            }
            break;
          case 'down':
            if (index < mockData.value.length) {
              handleDragstart(index);
              handleDragover(index + 1);
              handleDrop();
            }
            break;
        }
      };

      return {
        registerModal,
        getTitle,
        handleSubmit,
        mockData,
        targetKeys,
        filterOption,
        handleChange,
        handleSearch,
        isTarget,
        handleDrop,
        handleDragstart,
        handleDragover,
        checkChange,
        selectedKeys,
        handleSelectChange,
        handleSetIndex,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.ant-transfer-list-body) {
    width: 100%;
    height: 400px;
  }
  :deep(.ant-transfer-list-body-customize-wrapper) {
    padding: 0 12px 0 0px;
    height: 100%;
    overflow-y: auto;
  }
  // .transfer {
  //   width: 250px;
  //   height: 400px;
  //   overflow: hidden;
  // }
  .transfer-left,
  .transfer-right {
    width: 100%;
    height: 400px;
    // overflow-y: auto;
    &-item {
      padding-left: 12px;
      width: 100%;
      height: 30px;
      display: flex;
      align-items: center;
      &-content {
        width: 100%;
        display: flex;
        padding: 10px;
        align-items: center;
        justify-content: space-between;
      }
    }
    &-item:hover {
      background: #ccc;
    }
  }
</style>
