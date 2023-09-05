<template>
  <div>
    <grid-layout
      :layout="layoutList"
      :col-num="column"
      :row-height="30"
      :is-draggable="true"
      :is-resizable="true"
      :is-mirrored="false"
      :vertical-compact="true"
      :margin="[10, 10]"
      :use-css-transforms="true"
    >
      <grid-item
        v-for="(item, index) in layoutList"
        :key="`embedded${parentI}${item?.i}`"
        :drag-allow-from="`.toolboxChildren${parentI}`"
        :i="item.i"
        :x="item.x"
        :y="item.y"
        :w="item.w"
        :h="item.h"
      >
        <div class="flex flex-col h-full">
          <div
            class="bg-green-400 pt-1 flex justify-left w-full h-30px text-center"
            :class="`toolboxChildren${parentI}`"
            @dblclick="handleItem(item)"
          >
            <span class="mb-2 ml-2"><Icon :icon="item?.item?.icon" :size="18" class="mr-2" /></span>
            <span>{{ item?.item?.title || '请设置' }}</span>
          </div>
          <div
            style="background-color: #eee; padding: 20px"
            class="cursor-pointer h-full text-left"
            @dblclick="handleItem(item)"
          >
            <div class="mb-2 flex justify-between">
              <div>条数：{{ item?.item?.rowCount }}</div>
              <div>
                <Icon icon="ant-design:edit-outlined" class="mr-2" @click="handleItem(item)" />
                <Icon icon="ant-design:close-outlined" @click="delDrag(item, index)" />
              </div>
            </div>
          </div>
        </div>
      </grid-item>
    </grid-layout>
    <PortalManageDesignModal
      @register="registerPortalManageDesignModal"
      @success="handlePortalManageDesignModalCallBack"
    />
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, h, watchEffect, watch, isRef } from 'vue';
  import { GridLayout, GridItem } from 'v3-grid-layout';
  import Icon from '/@/components/Icon/index';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import PortalManageDesignModal from './PortalManageDesignModal.vue';

  export default defineComponent({
    name: 'EmbeddedGridLayout',
    components: {
      GridLayout,
      GridItem,
      Icon,
      PortalManageDesignModal,
    },
    props: {
      children: {
        type: Array,
        default: () => [],
      },
      childColumn: {
        type: Number,
        default: 12,
      },
      imgeLists: {
        type: Array,
        default: () => [],
      },
      parentI: {
        type: Number || String,
        default: 0,
      },
    },
    emits: ['update:children'],
    setup(props, { emit }) {
      const { createConfirm } = useMessage();
      const layoutList = ref([]);
      // layoutList.value = [
      //   { x: 0, y: 0, w: 2, h: 2, i: '0', static: false, minH: 5 },
      //   { x: 2, y: 0, w: 2, h: 4, i: '1', static: true },
      //   { x: 4, y: 2, w: 2, h: 5, i: '2', static: false },
      // ];
      const column = ref(12);
      watchEffect(() => {
        console.log('props', props);
        if (props.childColumn) {
          console.log('props.childColumn', props.childColumn);
          column.value = props.childColumn;
        }
        layoutList.value = props.children || [];
      });
      // watch(
      //   () => props.children,
      //   (newValue) => {
      //     console.log('parentI', props.parentI);
      //     console.log('props.children', newValue);
      //     // layoutList.value = props.children || [];
      //     // layoutList.value = isRef(newValue)?newValue:ref(newValue)  || [];
      //   },
      //   {
      //     immediate: true,
      //   },
      // );
      const [registerPortalManageDesignModal, { openModal }] = useModal();

      const handlePortalManageDesignModalCallBack = (record: Recordable) => {
        console.log('record==>', record);
        layoutList.value.forEach((item) => {
          if (item.i == record.id) {
            item = Object.assign(item, record);
          }
        });
        emit('update:children', layoutList.value);
        console.log('layoutList.value==>', layoutList.value);
      };

      const handleItem = (record: Recordable) => {
        openModal(true, { isUpdate: true, record, imgeLists: props.imgeLists });
      };
      const delDrag = (record: Recordable, index) => {
        console.log(record, index);

        createConfirm({
          iconType: 'warning',
          title: () => h('span', '确定删除吗？'),
          onOk: async () => {
            layoutList.value = layoutList.value.filter((_, i) => i != index);
            layoutList.value.forEach((item, i) => {
              item.i = i;
            });
            layoutList.value = [...layoutList.value];
            emit('update:children', layoutList.value);
            console.log('删除后', layoutList.value);
          },
        });
      };
      return {
        layoutList,
        column,
        delDrag,
        handleItem,
        registerPortalManageDesignModal,
        handlePortalManageDesignModalCallBack,
      };
    },
  });
</script>
