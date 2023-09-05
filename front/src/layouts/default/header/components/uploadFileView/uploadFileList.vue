<template>
  <div>
    <div class="text-right mb-1 mr-2 flex justify-between cursor-move">
      <div>
        <!-- <a-button type="primary" size="small" @click="openKeys" v-if="fileTreeList?.length"
          >展开</a-button
        >
        <a-button class="ml-2" size="small" @click="closeKeys" v-if="fileTreeList?.length"
          >折叠</a-button
        > -->
        <Tooltip :title="`折叠`"
          ><PlusSquareOutlined
            class="cursor-pointer"
            @click="closeKeys"
            v-if="fileTreeList?.length"
          />
        </Tooltip>
        <Tooltip :title="`展开`">
          <MinusSquareOutlined
            class="cursor-pointer ml-2"
            @click="openKeys"
            v-if="fileTreeList?.length"
        /></Tooltip>
      </div>
      <div>
        <!-- <DragOutlined class="cursor-move mr-2" /> -->
        <CloseOutlined class="cursor-pointer" @click="emit('close')" />
      </div>
    </div>
    <div v-if="fileTreeList?.length == 0">
      <Empty :image="simpleImage" description="暂无文件上传" />
    </div>
    <div style="overflow-y: auto; max-height: 290px">
      <Tree
        :treeData="fileTreeList"
        v-model:expandedKeys="expandedKeys"
        :selectable="false"
        @expand="handleExpand"
      >
        <template #title="{ title, level, fullPath, progress, fieldName }">
          <template v-if="level === 1">
            <span :style="{ color: currentRoute.fullPath == fullPath ? '#1890FF' : '#000' }">
              {{ title }}</span
            >
          </template>
          <template v-else>
            <div class="w-full">
              <div>
                {{ title }}
                <CloseOutlined @click="handleDelete(fieldName)" style="color: red" class="ml-2"
              /></div>
              <Progress :percent="progress" />
            </div>
          </template>
        </template>
      </Tree>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import { useUploadFileStore } from '/@/store/modules/uploadFile';
  import { Progress, Tree, Empty, Tooltip } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import { isArray } from '/@/utils/is';
  import { deleteByField } from '/@/utils/uploadFile';

  import {
    CloseOutlined,
    DragOutlined,
    MinusSquareOutlined,
    PlusSquareOutlined,
  } from '@ant-design/icons-vue';

  export default defineComponent({
    components: {
      Progress,
      Tree,
      CloseOutlined,
      Empty,
      DragOutlined,
      MinusSquareOutlined,
      PlusSquareOutlined,
      Tooltip,
    },
    emits: ['close'],
    setup(_, { emit }) {
      const expandedKeys = ref([]);
      const { currentRoute } = useRouter();

      const uploadFileStore = useUploadFileStore();
      const fileTreeList = computed(() => {
        console.log('uploadFileStore', uploadFileStore);
        let { uploadFileTreeList } = uploadFileStore;
        // const treeList = uploadFileStore.getUploadFileTreeList;
        if (isArray(uploadFileTreeList) && uploadFileTreeList.length > 0) {
          uploadFileTreeList.forEach((item) => {
            if (item.fullPath == currentRoute.value.fullPath) {
              if (expandedKeys.value.length == 0) {
                expandedKeys.value = [item.key];
              }
            }
          });
        }
        // 过滤掉当前页之外的页面
        // uploadFileTreeList = uploadFileTreeList.filter(
        //   (item) => item.fullPath == currentRoute.value.fullPath,
        // );
        return uploadFileTreeList;
      });
      const handleDelete = (fieldName) => {
        // let treeList = uploadFileStore.getUploadFileTreeList;
        // console.log('fieldName', fieldName);
        // console.log('treeList', treeList);
        // treeList.forEach((item) => {
        //   item.children = item.children.filter((item) => item.fieldName != fieldName);
        // });
        // treeList = treeList.filter((item) => item.children.length > 0);
        // uploadFileStore.setUploadFileTreeList(treeList);

        deleteByField(fieldName);
      };
      const handleExpand = (keys) => {
        console.log('keys', keys);
        // expandedKeys.value = keys;
        // uploadFileStore.setUploadFileTreeList(uploadFileStore.getUploadFileTreeList);
      };
      const openKeys = () => {
        expandedKeys.value = uploadFileStore.uploadFileTreeList.map((item) => item.key);
      };
      const closeKeys = () => {
        expandedKeys.value = [];
      };
      return {
        fileTreeList,
        handleDelete,
        currentRoute,
        expandedKeys,
        handleExpand,
        openKeys,
        closeKeys,
        simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
        emit,
      };
    },
  });
</script>

<style lang="less" scoped>
  :deep(.ant-tree .ant-tree-treenode) {
    width: 100%;
  }
  :deep(.ant-tree .ant-tree-node-content-wrapper) {
    width: 100%;
  }
</style>
