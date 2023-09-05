<template>
  <div>
    <ShareCard :loading="isLoading" :cardData="cardData">
      <template #title>
        <div @click="handleGo">
          <Icon v-if="icon" :icon="icon" :size="26" class="mr-2" />{{ header }}</div
        >
      </template>
      <template v-if="dataSource?.length > 0">
        <div v-for="item in dataSource" :key="item.ID" class="flex justify-between mb-2">
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-2/3 cursor-pointer"
            @click="handleView(item)"
          >
            <Tooltip :title="item[leftField]">
              {{ item[leftField] }}
            </Tooltip>
          </div>
          <div
            class="overflow-hidden whitespace-nowrap overflow-ellipsis w-1/3 text-right"
            v-if="item[rightField]"
          >
            <Tooltip :title="item[rightField]">
              {{
                item[rightField].indexOf(':') != -1
                  ? item[rightField].substring(0, 10)
                  : item[rightField]
              }}
            </Tooltip>
          </div>
        </div>
      </template>
      <template v-else>
        <Empty :image="simpleImage" />
      </template>
    </ShareCard>
    <SmartModuleDrawer @register="registerSmartModuleDrawer" @edit-action="handleOpenEditDrawer" />
  </div>
</template>
<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { Card, Empty, Tooltip } from 'ant-design-vue';
  import { useGo } from '/@/hooks/web/usePage';
  import Icon from '/@/components/Icon/index';
  import { useDrawer } from '/@/components/Drawer';
  import { getVisualList } from '/@/api/module/module';
  import SmartModuleDrawer from '../../../pages/smartModule/modules/smartModuleDrawer.vue';
  import ShareCard from './ShareCard.vue';

  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;

  const props = defineProps({
    loading: Boolean,
    width: {
      type: String as PropType<string>,
      default: '100%',
    },
    height: {
      type: String as PropType<string>,
      default: '300px',
    },
    icon: {
      type: String as PropType<string>,
      default: '',
    },
    formCode: {
      type: String as PropType<string>,
      default: '',
    },
    leftField: {
      type: String as PropType<string>,
      default: '',
    },
    rightField: {
      type: String as PropType<string>,
      default: '',
    },
    header: {
      type: String as PropType<string>,
      default: '',
    },
    pageSize: {
      type: Number,
      default: 6,
    },
    cardData: {
      type: Object as PropType<object>,
      default: () => {
        return {};
      },
    },
  });
  const isLoading = ref(false);
  const dataSource = ref<Recordable>([]);

  const fetch = () => {
    isLoading.value = true;
    getVisualList({ moduleCode: props.formCode, pageSize: props.pageSize })
      .then((res) => {
        dataSource.value = res.list || [];
      })
      .finally(() => {
        isLoading.value = false;
      });
  };

  watch(
    () => props.pageSize,
    () => {
      fetch();
    },
    // {
    //   immediate: true,
    // },
  );
  const go = useGo();
  const handleGo = () => {
    go({
      path: '/smartModulePage',
      query: {
        moduleCode: props.formCode,
      },
    });
  };
  const [registerSmartModuleDrawer, { openDrawer }] = useDrawer();

  const handleView = (record: any) => {
    let params = {
      moduleCode: props.formCode,
      id: record.id,
      visitKey: '',
    };
    openDrawer(true, {
      isUpdate: 3,
      record: {
        ...params,
      },
    });
  };

  function handleOpenEditDrawer(record: object) {
    console.log('handleOpenEditDrawer record', record);
    openDrawer(true, {
      isUpdate: 2,
      record: {
        moduleCode: props.formCode,
        id: record['id'],
      },
    });
  }

  watch(
    () => props.formCode,
    () => {
      if (props.formCode && props.formCode.length > 0) {
        fetch();
      }
    },
    { immediate: true },
  );
</script>
