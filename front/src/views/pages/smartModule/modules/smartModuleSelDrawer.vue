<template>
  <!-- 表单域选择 -->
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    width="90%"
    @ok="handleSubmit"
    :cancelText="'关闭'"
    :destroyOnClose="true"
    @close="onClose"
  >
    <template #title>
      <div class="flex justify-between items-center">
        <div>{{ getTitle }}</div>
        <div>
          <a-button type="primary" @click="clearValue">清空</a-button>
        </div>
      </div>
    </template>
    <SmartModuleTable
      :smartModuleSel="dataRef"
      :selMode="selMode"
      :start="start"
      ref="getTableInfo"
    />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';

  import { useMessage } from '/@/hooks/web/useMessage';
  import { removeScript } from '/@/utils/utils';

  import SmartModuleTable from './smartModuleTable.vue';
  export default defineComponent({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'smartModuleSelDrawer',
    components: {
      BasicDrawer,
      SmartModuleTable,
    },
    emits: ['success', 'register', 'clear', 'close'],
    setup(_, { emit }) {
      const selMode = ref(1); //1单选，2多选
      const start = ref(1); //1、表单域选择 2、嵌套表格选择 3、form_js选择记录
      let dataRef = ref<any>({});
      const getTableInfo = ref<any>(null);
      const { createMessage } = useMessage();
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });

        removeScript('-src');
        console.log('smartModuleSelDrawer useDrawerInner data', data);
        dataRef.value = data.record || {};
        selMode.value = data.selMode;
        start.value = data.start;
        // setDrawerProps({ showOkBtn: unref(selMode) != 3 });
      });
      const getTitle = '选择';
      async function handleSubmit() {
        let ids = getTableInfo.value.getSelectRowKeys();
        let rows = getTableInfo.value.getSelectRows();
        console.log('getTableInfo', ids);
        if (ids.length === 0) {
          createMessage.warning('请选择记录');
          return;
        }
        // let names = [] as any;
        // rows.forEach((item) => {
        //   names.push(item.name);
        // });
        console.log('rows==>', rows);
        //to do
        emit('success', rows);
        closeDrawer();
        onClose();
      }

      function onClose() {
        console.log('smartModuleSelDrawer onClose');
        emit('close');
      }

      const clearValue = () => {
        console.log('清空');
        emit('clear');
        closeDrawer();
        onClose();
      };

      return {
        registerDrawer,
        handleSubmit,
        getTitle,
        dataRef,
        selMode,
        start,
        getTableInfo,
        clearValue,
        onClose,
      };
    },
  });
</script>
