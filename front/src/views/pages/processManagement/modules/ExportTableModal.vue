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
        width: '400px',
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
              :key="item.key"
              @click="() => checkChange(item.checked, item.key, onItemSelect)"
              class="transfer-right-item cursor-move"
              @mouseenter="isTarget(true, item)"
              @mouseleave="isTarget(false, item)"
              @dragstart="handleDragstart(index)"
              @drop.prevent="handleDrop()"
              @dragover.prevent="handleDragover(index)"
            >
              <Checkbox :checkedKeys="[...targetKeys]" v-model:checked="item.checked" />
              <div class="transfer-right-item-content">
                <span> &nbsp;{{ item.title }}</span>
                <!-- <MenuOutlined v-show="item.showMenu" /> -->
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
              class="transfer-left-item cursor-pointer"
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
  import { downloadByData } from '/@/utils/file/download';
  import {
    getFieldsWithNest,
    getExportFields,
    getExportExcel,
    getExportExcelAsync,
  } from '/@/api/process/process';
  import Icon from '/@/components/Icon';
  import { upIndex, downIndex } from '/@/utils/uFun';
  import { useUserStore } from '/@/store/modules/user';

  interface MockData {
    key: string;
    title: string;
    description: string;
    chosen: boolean;
  }

  export default defineComponent({
    name: 'ExportTableModal',
    components: { BasicModal, Transfer, Checkbox, Icon },
    emits: ['success', 'register', 'asyncDownload'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let typeCode = ref('');
      const { createMessage } = useMessage();
      const userStore = useUserStore();
      let serverInfo = userStore.getServerInfo;

      let mockData = ref<MockData[]>([]);

      let targetKeys = ref<string[]>([]);
      let selectedKeys = ref<string[]>([]);
      let formParams = {};
      let displayMode = 2;
      let ids = '';

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '60%' });
        isUpdate.value = !!data?.isUpdate;
        typeCode.value = data.typeCode;
        formParams = data.formParams;
        displayMode = data.displayMode;
        ids = data.ids;
        mockData.value = [];
        targetKeys.value = [];
        getExportFieldsList();
        getFieldsList();
      });

      const getTitle = '导出';

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });
          // TODO custom api
          if (targetKeys.value.length == 0) {
            createMessage.warning('选择的字段不能为空');
            return;
          }
          if (targetKeys.value.length > 256) {
            createMessage.warning('选择的字段不能超过256个');
            return;
          }
          let params = {
            typeCode: typeCode.value,
            fields: targetKeys.value.join(','),
            type: 'search',
            displayMode: displayMode,
            ids,
            ...formParams,
          };

          if (!serverInfo.isExportExcelAsync) {
            await getExportExcel(params).then((res) => {
              downloadByData(res, '查询结果导出.xls');
              closeModal();
              emit('success');
            });
          } else {
            await getExportExcelAsync(params).then((res) => {
              closeModal();
              let data: any = {};
              data.moduleName = '查询结果导出';
              data.uid = res;
              emit('asyncDownload', data);
            });
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      async function getExportFieldsList() {
        let params = {
          typeCode: typeCode.value,
        };
        await getExportFields(params).then((res) => {
          let data = res || [];
          data.forEach((item) => {
            item.key = item.name;
            item.title = item.title;
            item.description = item.title;
            targetKeys.value.push(item.name);
          });
        });
      }

      async function getFieldsList() {
        let params = {
          typeCode: typeCode.value,
        };
        await getFieldsWithNest(params).then((res) => {
          let data = res || [];
          data.forEach((item) => {
            item.key = item.name;
            item.title = item.title;
            item.description = item.title;
            item.checked = false;
          });
          mockData.value = data;
        });
      }
      const filterOption = (inputValue: string, option: MockData) => {
        return option.description.indexOf(inputValue) > -1;
      };
      const handleChange = (keys: string[], direction: string, moveKeys: string[]) => {
        // mockData.value = mockData.value.map((item) => {
        //   return {
        //     ...item,
        //     checked: false,
        //   };
        // });

        if (direction === 'right') {
          // 将右移的元素置于末尾，元素较多时，置于末尾会看不到
          // targetKeys.value.push(...moveKeys);
          // targetKeys.value.splice(0, moveKeys.length);
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
        // 删除老的
        const changeItem = targetKeys.value.splice(drawerData.oldItemIndex, 1)[0];
        // 在列表中目标位置增加新的
        targetKeys.value.splice(drawerData.newItemIndex, 0, changeItem);
      };

      const handleDragstart = (index) => {
        drawerData.oldItemIndex = index;
      };
      const handleDragover = (index) => {
        drawerData.newItemIndex = index;
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

      // 用于判断选中了哪些多选框
      const checkChange = (checked, key, onItemSelect) => {
        mockData.value.forEach((items, index) => {
          if (key === items.key) mockData.value[index].chosen = !checked;
        });
        onItemSelect(key, !checked);
      };

      const handleSelectChange = (sourceSelectedKeys, targetSelectedKeys) => {
        mockData.value.forEach((item) => {
          if (sourceSelectedKeys.includes(item.key) || targetSelectedKeys.includes(item.key)) {
            item.checked = true;
          } else {
            item.checked = false;
          }
        });
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
        handleSetIndex,
        handleSelectChange,
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
  }
  .transfer {
    height: 400px;
    overflow: hidden;
  }
  .transfer-left,
  .transfer-right {
    width: 100%;
    height: 400px;
    overflow-y: auto;
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
