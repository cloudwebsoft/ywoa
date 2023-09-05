<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    @ok="handleSubmit"
    :showOkBtn="false"
    :cancelText="'关闭'"
  >
    <BasicTable @register="registerTable">
      <template #toolbar> </template>

      <template #canSee="{ record }">
        <a-tooltip
          placement="topLeft"
          :title="record.priv == '' && record.type == 0 ? '菜单项上未设权限' : ''"
        >
          <Checkbox
            :disabled="
              record.type == 2 ||
              record.type == 3 ||
              record.type == 4 ||
              (record.priv == '' && record.type == 0)
            "
            v-model:checked="record.canSee"
            @change="(e) => handleOnChange(e, record)"
          />
        </a-tooltip>
      </template>
    </BasicTable>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicColumn } from '/@/components/Table';
  import { getRoleMenu, getSetMenuPriv } from '/@/api/system/system';
  import { Checkbox } from 'ant-design-vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  export default defineComponent({
    name: 'menuInfo',
    components: {
      BasicTable,
      BasicDrawer,
      Checkbox,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();

      let dataRef = reactive({});
      let title = ref('');
      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        dataRef = data.record;
        title.value = `属于角色：${dataRef.description} 的菜单`;
        setProps({
          searchInfo: {
            roleCode: dataRef.code,
          },
        });

        handleSuccess();
      });

      const getTitle = computed(() => title.value);
      async function handleSubmit() {
        try {
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          closeDrawer();
          dataRef = {};
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }
      const columns: BasicColumn[] = [
        {
          title: '',
          dataIndex: 'canSee',
          width: 50,
          ellipsis: true,
          slots: { customRender: 'canSee' },
        },
        {
          title: '菜单项',
          dataIndex: 'name',
          width: 150,
          ellipsis: true,
        },
        {
          title: '类型',
          dataIndex: 'typeName',
          width: 100,
          ellipsis: true,
          customRender: function ({ record }) {
            if (record.type == 3) {
              return '流程';
            } else if (record.type == 0) {
              return '链接';
            } else if (record.type == 2) {
              return '模块';
            } else if (record.type == 4) {
              return '基础数据';
            } else {
              return '框架';
            }
          },
        },
        {
          title: '权限',
          dataIndex: 'privName',
          width: 150,
          ellipsis: true,
        },
        {
          title: '模块/流程/基础数据',
          dataIndex: 'moduleName',
          width: 150,
          ellipsis: true,
        },
      ];
      const [registerTable, { reload, setProps, expandRows }] = useTable({
        title: '菜单',
        api: getRoleMenu,
        columns,
        formConfig: {
          labelWidth: 120,
        },
        searchInfo: {}, //额外的参数
        resizeHeightOffset: 70,
        pagination: false,
        immediate: false,
        useSearchForm: false,
        showTableSetting: true,
        bordered: true,
        indexColumnProps: { width: 50 },
        showIndexColumn: true,
        defaultExpandAllRows: true,
        rowKey: 'id',
        isTreeTable: true,
        expandIconColumnIndex: 1, // 将折叠的图标放在第二列
        indentSize: 30,
        expandRowByClick: true,
        // expandedRowKeys: [], //展开的行
      });

      let keys = ref([]);
      let expendKeys = ref([]);
      async function getRoleMenuList() {
        await getRoleMenu({ roleCode: dataRef.code }).then(async (res) => {
          keys.value = [];
          expendKeys.value = [];
          if (res.list && res.list.length > 0) {
            let data = await depTree(res.list);
            // 在每行中加入id
            expandRows(expendKeys.value);
          }
        });
      }
      async function depTree(data) {
        await data.forEach((item) => {
          if (item.canSee) {
            keys.value.push(item.id);
          }
          expendKeys.value.push(item.id);
          if (item.children && item.children.length > 0) {
            depTree(item.children);
          }
        });
      }

      function handleOnChange(e, record) {
        let params = {
          isPriv: record.canSee,
          priv: record.priv,
          roleCode: dataRef.code,
          menuCode: record.id,
        };
        getSetMenuPriv(params).then((res) => {
          createMessage.success('操作成功');
          handleSuccess();
        });
      }

      function handleSuccess() {
        reload();
        getRoleMenuList();
      }

      return {
        registerDrawer,
        getTitle,
        handleSubmit,
        dataRef,
        registerTable,
        handleSuccess,
        keys,
        expendKeys,
        handleOnChange,
      };
    },
  });
</script>
