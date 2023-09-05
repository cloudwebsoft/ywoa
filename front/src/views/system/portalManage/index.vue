<template>
  <div class="p-2 h-11/12">
    <div class="text-right bg-white p-2">
      <a-button type="primary" class="mr-2" @click="handleAdd">新增</a-button>
      <a-button type="primary" class="mr-2" @click="handleEdit"> 编辑 </a-button>
      <Popconfirm title="确定复制么？" ok-text="确定" cancel-text="取消" @confirm="handleCopy">
        <a-button type="primary" class="mr-2"> 复制 </a-button>
      </Popconfirm>
      <a-button type="primary" class="mr-2" @click="handleDel" :loading="isSpinning">
        删除
      </a-button>
      <a-button type="primary" @click="handleDesign"> 设计 </a-button>
    </div>
    <div class="h-full bg-white p-2 mt-2">
      <!-- tag="transition-group" -->
      <draggable
        class="list-group grid sm:grid-cols-2 md:grid-cols-4 lg:grid-cols-6 xl:grid-cols-6 2xl:grid-cols-10 grid-flow-row gap-1"
        :list="columns"
        item-key="id"
        :component-data="{
          tag: 'ul',
          type: 'transition-group',
          name: !dragging ? 'flip-list' : null,
        }"
        v-bind="dragOptions"
        :move="checkMove"
        @start="onStart"
        @end="onEnd"
        @change="onChange"
        filter=".undraggable"
      >
        <template #item="{ element }">
          <li
            class="text-white rounded-md w-140px h-44px flex justify-center items-center list-group-item"
            :class="[
              { 'bg-green-400': element.status },
              { 'bg-gray-400': !element.status },
              { 'bg-orange-300': currentRecord?.id === element.id },
              { undraggable: element.orders == 1 },
            ]"
            @click="onClick(element)"
            @dblclick="handleDesign(null, element)"
          >
            <Icon :icon="element.icon" :size="26" class="mr-2" v-if="element.icon" />{{
              element.name + (element.kind == 0 ? '(桌面)' : '(菜单)')
            }}
          </li>
        </template>
      </draggable>
    </div>
    <PortalManageModal
      @register="registerPortalManageModal"
      @success="handlePortalManageCallBack"
    />
    <PortalManageDesignDrawer
      @register="registerPortalManageDesignDrawer"
      @success="handlePortalManageDesignCallBack"
    />
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, h } from 'vue';
  import Icon from '/@/components/Icon/index';
  import { Popconfirm } from 'ant-design-vue';
  import draggable from 'vuedraggable';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import PortalManageModal from './modules/PortalManageModal.vue';
  import PortalManageDesignDrawer from './modules/PortalManageDesignDrawer.vue';
  import { getPortalList, getPortalDel, getPortalSort, getPortalCopy } from '/@/api/system/system';
  import { debounce } from 'lodash-es';

  export default defineComponent({
    components: { draggable, PortalManageModal, PortalManageDesignDrawer, Icon, Popconfirm },
    setup() {
      const { createMessage, createConfirm } = useMessage();
      const columns = ref<Recordable>([]);
      const dragging = ref(false);
      const currentRecord = ref<Recordable>({});

      onMounted(() => {
        fetch();
      });

      const fetch = () => {
        getPortalList().then((res) => {
          columns.value = res || [];

          // 当修改门户后，需再次赋值当前选中的currentRecord
          if (Object.keys(currentRecord).length > 0) {
            columns.value.forEach((item) => {
              if (item.id === currentRecord.value.id) {
                console.log('fetch columns item', item);
                currentRecord.value.kind = item.kind;
                currentRecord.value.status = item.status;
                currentRecord.value.name = item.name;
                currentRecord.value.depts = item.depts;
                currentRecord.value.roles = item.roles;
                console.log('fetch currentRecord updated', currentRecord.value);
                return;
              }
            });
          }
        });

        console.log('fetch currentRecord', currentRecord.value);
      };

      const checkMove = (e) => {
        // 禁止拖动到orders为1的对象，仅使用.filter，仍可拖动其它元素排到第1位
        console.log(e.relatedContext.element.orders);
        if (e.relatedContext.element.orders == 1) {
          return false;
        } else {
          return true;
        }
      };
      const onStart = (e) => {
        dragging.value = true;
      };
      const onEnd = (e) => {
        dragging.value = false;
      };
      const onChange = (e) => {
        const ids = columns.value.map((item) => item.id).join(',');
        getPortalSort({ ids }).then(() => {
          fetch();
        });
      };
      const onClick = debounce((e) => {
        console.log('e', e);
        currentRecord.value = e;
        console.log('currentRecord.value', currentRecord.value);
      }, 200);

      const [registerPortalManageModal, { openModal }] = useModal();
      const handleAdd = () => {
        openModal(true, { isUpdate: false });
      };
      //编辑
      const handleEdit = () => {
        console.log('currentRecord.value', currentRecord.value);
        const id = currentRecord?.value?.id;
        if (!id) {
          createMessage.warning('请选择门户');
          return;
        }

        openModal(true, { isUpdate: true, record: currentRecord.value });
      };

      const handleCopy = () => {
        const id = currentRecord?.value?.id;
        if (!id) {
          createMessage.warning('请选择门户');
          return;
        }

        getPortalCopy({ id: id }).then(() => {
          fetch();
        });
      };

      const isSpinning = ref(false);
      //删除
      const handleDel = () => {
        const id = currentRecord?.value?.id;

        if (!id) {
          createMessage.warning('请选择门户');
          return;
        }

        if (currentRecord.value.orders == 1) {
          createMessage.warning('您不能删除首页');
          return;
        }

        createConfirm({
          iconType: 'warning',
          title: () => h('span', '确定删除吗？'),
          onOk: async () => {
            isSpinning.value = true;
            await getPortalDel({ id }).then(() => {
              fetch();
            });
            isSpinning.value = false;
          },
        });
      };
      const handlePortalManageCallBack = () => {
        fetch();
      };

      const [registerPortalManageDesignDrawer, { openDrawer }] = useDrawer();
      const handleDesign = (e, element) => {
        if (!element) {
          const id = currentRecord?.value?.id;
          if (!id) {
            createMessage.warning('请选择门户');
            return;
          }
        } else {
          // 双击时
          currentRecord.value = element;
        }

        openDrawer(true, {
          update: true,
          record: currentRecord.value,
        });
      };
      const handlePortalManageDesignCallBack = () => {
        fetch();
      };
      return {
        columns,
        dragging,
        checkMove,
        onStart,
        onEnd,
        onClick,
        onChange,
        currentRecord,
        dragOptions: {
          animation: 200,
          group: 'description',
          disabled: false,
        },
        handleAdd,
        handleEdit,
        handleDel,
        isSpinning,
        registerPortalManageModal,
        handlePortalManageCallBack,
        handleDesign,
        registerPortalManageDesignDrawer,
        handlePortalManageDesignCallBack,
        handleCopy,
      };
    },
  });
</script>
<style lang="less" scoped>
  .flip-list-move {
    transition: transform 0.5s;
  }
  .list-group {
    min-height: 20px;
  }

  .no-move {
    transition: transform 0s;
  }
  .list-group-item {
    cursor: move;
  }
  .list-group-item i {
    cursor: pointer;
  }
</style>
