<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :minHeight="100"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, reactive, onMounted, onUpdated } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { FormSchema } from '/@/components/Table';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import {
    getChartTypes,
    getModulesAll,
    getFieldsByModuleCode,
    getBasicTree,
    getListCarouselPicture,
    getListAllCard,
  } from '/@/api/system/system';
  import { getDirTree } from '/@/api/process/process';
  import { deepMerge } from '/@/utils';

  export default defineComponent({
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let isUpdate = ref(true);
      let dataRef = reactive<Recordable>({});
      const imgeLists = ref([]);
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false, width: '60%' });
        isUpdate.value = !!data?.isUpdate;
        console.log('data', data);
        imgeLists.value = data?.imgeLists;
        dataRef = data?.record;
        let type = dataRef.item.type;
        if (
          type === 'chart_pie' ||
          type === 'chart_bar' ||
          type === 'chart_line' ||
          type === 'chart_gauge' ||
          type === 'chart_funnel' ||
          type === 'chart_radar'
        ) {
          await getChartTypesList(type);
        } else if (type === 'moduleLight') {
          await getFields(dataRef.item.meta.formCode);
        }

        setFieldsValue({
          ...dataRef.item,
          // title: dataRef.item.title,
          // rowCount: dataRef.item.rowCount,
          // icon: dataRef.item.icon,
          // type: dataRef.item.type,
          // dirCode: dataRef?.item?.meta?.dirCode || [],
          // typeCode: dataRef?.item?.meta?.typeCode,
          // chartId: dataRef?.item?.meta?.chartId,
          // formCode: dataRef?.item?.meta?.formCode,
          // carouselPictureId: dataRef?.item?.meta.carouselPictureId,
          // leftField: dataRef?.item.meta.leftField,
          // rightField: dataRef?.item.meta.rightField,
          // isShowDirImage: dataRef?.item.meta.isShowDirImage,
          // cardId: dataRef?.item.meta.cardId,
          // showSearch: dataRef?.item.meta.showSearch,
          // showToolbar: dataRef?.item.meta.showToolbar,
          // showOpCol: dataRef?.item.meta.showOpCol,
          // pagination: dataRef?.item.meta.pagination,
        });
        updateSchema([
          {
            field: 'titleBackgroundImge',
            componentProps: {
              treeData: imgeLists.value,
            },
          },
          {
            field: 'boxBackgroundImge',
            componentProps: {
              treeData: imgeLists.value,
            },
          },
        ]);
        await fetchCarousePictureList();
      });

      const chartList = ref<Recordable>([]);
      const fieldList = ref<Recordable>([]);

      async function getChartTypesList(chartType) {
        getChartTypes({ chartType }).then((res) => {
          chartList.value = res || [];
          updateSchema([
            {
              field: 'chartId',
              componentProps: {
                options: chartList.value,
              },
            },
          ]);
        });
      }

      async function getFields(moduleCode) {
        getFieldsByModuleCode({ moduleCode: moduleCode }).then((data) => {
          fieldList.value = data;
          updateSchema([
            {
              field: 'leftField',
              componentProps: {
                options: fieldList.value,
              },
            },
            {
              field: 'rightField',
              componentProps: {
                options: fieldList.value,
              },
            },
          ]);
        });
      }

      const carouselPictureList = ref<Recordable>([]);
      const fetchCarousePictureList = () => {
        getListCarouselPicture({}).then((res) => {
          console.log('res', res);
          carouselPictureList.value = res || [];
          updateSchema([
            {
              field: 'carouselPictureId',
              componentProps: {
                options: carouselPictureList.value,
              },
            },
          ]);
        });
      };

      onMounted(() => {});

      onUpdated(() => {});

      const moduleList: Recordable[] = [
        {
          value: 'fileark',
          label: '文件柜',
        },
        {
          value: 'plan',
          label: '日程安排',
        },
        {
          value: 'flow',
          label: '待办流程',
        },
        {
          value: 'flowMine',
          label: '发起的流程',
        },
        {
          value: 'flowAttended',
          label: '我参与的流程',
        },
        {
          value: 'flowFavorite',
          label: '我关注的流程',
        },
        {
          value: 'notice',
          label: '通知',
        },
        {
          value: 'plan_calendar',
          label: '日程安排日历',
        },
        {
          value: 'CarouselPicture',
          label: '图片轮播',
        },
        {
          value: 'module',
          label: '智能模块',
        },
        {
          value: 'moduleLight',
          label: '轻模块',
        },
        {
          value: 'ApplicationView',
          label: '应用',
        },
        {
          value: 'Card',
          label: '卡片',
        },
        {
          value: 'chart_pie',
          label: '饼图',
        },
        {
          value: 'chart_bar',
          label: '柱状图',
        },
        {
          value: 'chart_line',
          label: '折线图',
        },
        {
          value: 'chart_gauge',
          label: '仪表盘',
        },
        {
          value: 'chart_funnel',
          label: '漏斗图',
        },
        {
          value: 'chart_radar',
          label: '雷达图',
        },
      ];
      const FormSchema: FormSchema[] = [
        {
          field: 'title',
          label: '标题',
          component: 'Input',
          required: true,
          colProps: {
            span: 8,
          },
        },
        {
          field: 'showTitle',
          label: '显示标题',
          component: 'Switch',
          required: true,
          defaultValue: true,
          componentProps: {
            checkedChildren: '是',
            unCheckedChildren: '否',
          },
          colProps: {
            span: 8,
          },
        },
        {
          field: 'titleAlign',
          label: '标题位置',
          component: 'RadioButtonGroup',
          required: true,
          defaultValue: 'left',
          componentProps: {
            options: [
              { label: '左', value: 'left' },
              { label: '中', value: 'center' },
              { label: '右', value: 'right' },
            ],
          },
          colProps: {
            span: 8,
          },
        },
        {
          field: 'titleSize',
          label: '标题大小',
          component: 'Select',
          required: true,
          defaultValue: '16',
          componentProps: {
            options: [
              { label: '12', value: '12' },
              { label: '14', value: '14' },
              { label: '16', value: '16' },
              { label: '18', value: '18' },
              { label: '22', value: '22' },
              { label: '26', value: '26' },
              { label: '28', value: '28' },
              { label: '30', value: '30' },
              { label: '32', value: '32' },
              { label: '34', value: '34' },
              { label: '36', value: '36' },
              { label: '38', value: '38' },
              { label: '48', value: '48' },
              { label: '60', value: '60' },
            ],
          },
          colProps: {
            span: 8,
          },
        },
        {
          field: 'titleColor',
          label: '标题颜色',
          component: 'ColorPicker',
          required: false,
          colProps: {
            span: 8,
          },
        },
        {
          field: 'cardHeadBorderBottomColor',
          label: '标题底线颜色',
          component: 'ColorPicker',
          required: false,
          colProps: {
            span: 8,
          },
        },

        {
          field: 'titleBackgroundImge',
          label: '标题背景',
          component: 'TreeSelect',
          required: false,
          defaultValue: '',
          componentProps: {
            treeData: imgeLists.value,
            fieldNames: { label: 'title', value: 'value' },
            showSearch: true,
            treeNodeFilterProp: 'title',
            getPopupContainer: () => document.body,
            wrapFlex: true,
          },
          colProps: {
            span: 8,
          },
        },
        {
          field: 'boxBackgroundImge',
          label: '卡片背景',
          component: 'TreeSelect',
          required: false,
          defaultValue: '',
          componentProps: {
            treeData: imgeLists.value,
            fieldNames: { label: 'title', value: 'value' },
            showSearch: true,
            treeNodeFilterProp: 'title',
            getPopupContainer: () => document.body,
          },
          colProps: {
            span: 8,
          },
        },
        {
          field: 'boxBorder',
          label: '边框颜色',
          component: 'ColorPicker',
          required: true,
          componentProps: {
            style: 'width:200px',
            getPopupContainer: () => document.body,
          },
          colProps: {
            span: 8,
          },
        },
        {
          field: 'showHorn',
          label: '显示四角',
          component: 'RadioButtonGroup',
          required: true,
          defaultValue: '0',
          componentProps: {
            options: [
              { label: '否', value: '0' },
              { label: '是', value: '1' },
            ],
          },
          colProps: {
            span: 8,
          },
        },
        {
          field: 'hornColor',
          label: '四角颜色',
          component: 'ColorPicker',
          required: true,
          colProps: {
            span: 8,
          },
        },
        {
          field: 'imgeCenter',
          label: '图片居中',
          component: 'RadioButtonGroup',
          required: true,
          defaultValue: '0',
          componentProps: {
            options: [
              { label: '否', value: '0' },
              { label: '是', value: '1' },
            ],
          },
          colProps: {
            span: 8,
          },
        },
        {
          field: 'boxBackgroundColor',
          label: '背景色',
          component: 'ColorPicker',
          required: true,
          colProps: {
            span: 8,
          },
        },
        {
          field: 'rowCount',
          label: '条数',
          component: 'InputNumber',
          required: true,
          defaultValue: 6,
          colProps: {
            span: 8,
          },
          ifShow: ({ values }) => values.type !== 'Card',
        },
        {
          field: 'icon',
          label: '图标',
          component: 'IconPicker',
          // required: true,
          colProps: {
            span: 8,
          },
        },
        {
          field: 'type',
          label: '类型',
          component: 'Select',
          defaultValue: '',
          required: true,
          colProps: {
            span: 8,
          },
          componentProps: {
            showSearch: true,
            options: moduleList,
            optionFilterProp: 'label',
            getPopupContainer: () => document.body,
            onChange: (e) => {
              if (
                e === 'chart_pie' ||
                e === 'chart_bar' ||
                e === 'chart_line' ||
                e === 'chart_gauge' ||
                e === 'chart_funnel' ||
                e === 'chart_radar'
              ) {
                getChartTypesList(e);
                setFieldsValue({ chartId: '' });
              } else if (e === 'fileark') {
                // getBasicTree({code:fileark_dir})
              }

              console.log('carouselPictureList.value', carouselPictureList.value);
              // getChartTypes
            },
          },
        },
        {
          field: 'meta.formCode',
          label: '模块',
          component: 'ApiSelect',
          required: true,
          colProps: {
            span: 8,
          },
          componentProps: {
            api: getModulesAll,
            labelField: 'name',
            valueField: 'code',
            showSearch: true,
            optionFilterProp: 'label',
            getPopupContainer: () => document.body,
            onChange: (e) => {
              setFieldsValue({ leftField: '' });
              setFieldsValue({ rightField: '' });
              getFields(e);
            },
          },
          ifShow: ({ values }) => values.type === 'module' || values.type === 'moduleLight',
        },
        {
          field: 'meta.showSearch',
          label: '显示搜索区',
          component: 'RadioButtonGroup',
          // defaultValue: '0',
          componentProps: {
            options: [
              { label: '否', value: '0' },
              { label: '是', value: '1' },
            ],
          },
          colProps: { span: 8 },
          ifShow: ({ values }) => values.type === 'module',
        },
        {
          field: 'meta.pagination',
          label: '显示分页',
          component: 'RadioButtonGroup',
          // defaultValue: '0',
          componentProps: {
            options: [
              { label: '否', value: '0' },
              { label: '是', value: '1' },
            ],
          },
          colProps: { span: 8 },
          ifShow: ({ values }) => values.type === 'module',
        },
        {
          field: 'meta.showToolbar',
          label: '显示工具条按钮',
          component: 'RadioButtonGroup',
          // defaultValue: '0',
          componentProps: {
            options: [
              { label: '否', value: '0' },
              { label: '是', value: '1' },
            ],
          },
          colProps: { span: 8 },
          ifShow: ({ values }) => values.type === 'module',
        },
        {
          field: 'meta.showOpCol',
          label: '显示操作列',
          component: 'RadioButtonGroup',
          // defaultValue: '0',
          componentProps: {
            options: [
              { label: '否', value: '0' },
              { label: '是', value: '1' },
            ],
          },
          colProps: { span: 8 },
          ifShow: false, // ({ values }) => values.type === 'module',
        },
        {
          field: 'meta.cardId',
          label: '卡片',
          component: 'ApiSelect',
          required: true,
          colProps: {
            span: 8,
          },
          componentProps: {
            api: getListAllCard,
            labelField: 'name',
            valueField: 'id',
            showSearch: true,
            optionFilterProp: 'label',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.type === 'Card',
        },
        {
          field: 'meta.leftField',
          label: '左侧字段',
          component: 'Select',
          defaultValue: '',
          required: true,
          colProps: {
            span: 8,
          },
          componentProps: {
            // showSearch: true,
            fieldNames: {
              label: 'title',
              key: 'name',
              value: 'name',
            },
            options: fieldList.value,
            treeNodeFilterProp: 'title',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.type === 'moduleLight',
        },
        {
          field: 'meta.rightField',
          label: '右侧字段',
          component: 'Select',
          defaultValue: '',
          required: true,
          colProps: {
            span: 8,
          },
          componentProps: {
            // showSearch: true,
            fieldNames: {
              label: 'title',
              key: 'name',
              value: 'name',
            },
            options: fieldList.value,
            treeNodeFilterProp: 'title',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.type === 'moduleLight',
        },
        {
          field: 'meta.dirCode',
          label: '目录',
          component: 'ApiTreeSelect',
          defaultValue: '',
          required: true,
          colProps: {
            span: 8,
          },
          componentProps: {
            api: getBasicTree,
            // showSearch: true,
            params: { code: 'fileark_dir' },
            multiple: true,
            maxTagCount: 3,
            resultField: 'list',
            fieldNames: {
              label: 'name',
              key: 'code',
              value: 'code',
            },
            treeNodeFilterProp: 'name',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.type === 'fileark',
        },
        {
          field: 'meta.isShowDirImage',
          label: '显示栏目图片',
          component: 'RadioButtonGroup',
          defaultValue: '1',
          componentProps: {
            options: [
              { label: '否', value: '0' },
              { label: '是', value: '1' },
              { label: '轮播', value: '2' },
            ],
          },
          colProps: { lg: 24, md: 24 },
          ifShow: ({ values }) => values.type === 'fileark',
        },
        {
          field: 'meta.typeCode',
          label: '流程',
          component: 'ApiTreeSelect',
          defaultValue: '',
          required: false,
          colProps: {
            span: 8,
          },
          componentProps: {
            api: getDirTree,
            // showSearch: true,
            resultField: 'list',
            fieldNames: {
              label: 'name',
              key: 'code',
              value: 'code',
            },
            treeNodeFilterProp: 'name',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.type === 'flow',
        },
        {
          field: 'meta.chartId',
          label: '图形',
          component: 'Select',
          defaultValue: '',
          required: true,
          colProps: {
            span: 8,
          },
          componentProps: {
            // showSearch: true,
            fieldNames: {
              label: 'title',
              key: 'id',
              value: 'id',
            },
            options: chartList.value,
            treeNodeFilterProp: 'title',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) =>
            values.type === 'chart_pie' ||
            values.type === 'chart_bar' ||
            values.type === 'chart_line' ||
            values.type === 'chart_gauge' ||
            values.type === 'chart_funnel' ||
            values.type === 'chart_radar',
        },
        {
          field: 'meta.carouselPictureId',
          label: '图片轮播',
          component: 'Select',
          defaultValue: '',
          required: true,
          colProps: {
            span: 8,
          },
          componentProps: {
            // showSearch: true,
            fieldNames: {
              label: 'name',
              key: 'id',
              value: 'id',
            },
            treeNodeFilterProp: 'name',
            options: carouselPictureList.value,
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.type === 'CarouselPicture',
        },
        // {
        //   field: 'carouselPictureId',
        //   label: '图片轮播',
        //   component: 'ApiSelect',
        //   required: true,
        //   colProps: {
        //     span: 8,
        //   },
        //   componentProps: {
        //     api: getListCarouselPicture,
        //     labelField: 'name',
        //     valueField: 'id',
        //     showSearch: true,
        //     optionFilterProp: 'label',
        //     getPopupContainer: () => document.body,
        //   },
        //   ifShow: ({ values }) => values.type === 'CarouselPicture',
        // },
      ];
      const [
        registerForm,
        { setFieldsValue, resetFields, validate, updateSchema, getFieldsValue },
      ] = useForm({
        colProps: {
          span: 8,
        },
        labelWidth: 100,
        schemas: FormSchema,
        showActionButtonGroup: false,
        labelCol: {
          span: 8,
        },
        wrapperCol: {
          span: 15,
        },
        actionColOptions: {
          span: 23,
        },
      });

      const getTitle = '编辑';

      async function handleSubmit() {
        try {
          let values = await validate();
          setModalProps({ confirmLoading: true });
          if (!values.embedded) {
            values.children = [];
          }
          // dataRef.item = Object.assign({}, dataRef.item, values);
          // dataRef.item.title = formData.title;
          // dataRef.item.rowCount = formData.rowCount;
          // dataRef.item.icon = formData.icon;
          // dataRef.item.type = formData.type;
          // dataRef.item.showTitle = formData.showTitle;
          // dataRef.item.titleAlign = formData.titleAlign;
          // dataRef.item.titleSize = formData.titleSize;
          // dataRef.item.titleColor = formData.titleColor;
          // dataRef.item.titleBackgroundImge = formData.titleBackgroundImge;
          // dataRef.item.boxBorder = formData.boxBorder;
          // dataRef.item.showHorn = formData.showHorn;
          // dataRef.item.hornColor = formData.hornColor;
          // dataRef.item.imgeCenter = formData.imgeCenter;
          // dataRef.item.boxBackgroundColor = formData.boxBackgroundColor;

          // dataRef.item.meta.dirCode = dataRef.item.dirCode;
          // dataRef.item.meta.typeCode = dataRef.item.typeCode;
          // dataRef.item.meta.chartId = dataRef.item.chartId;
          // dataRef.item.meta.formCode = dataRef.item.formCode;
          // dataRef.item.meta.carouselPictureId = dataRef.item.carouselPictureId;
          // dataRef.item.meta.leftField = dataRef.item.leftField;
          // dataRef.item.meta.rightField = dataRef.item.rightField;
          // dataRef.item.meta.isShowDirImage = dataRef.item.isShowDirImage;
          // dataRef.item.meta.cardId = dataRef.item.cardId;
          // dataRef.item.meta.showSearch = dataRef.item.showSearch;
          // dataRef.item.meta.showToolbar = dataRef.item.showToolbar;
          // dataRef.item.meta.showOpCol = dataRef.item.showOpCol;
          // dataRef.item.meta.pagination = dataRef.item.pagination;
          deepMerge(dataRef.item, getFieldsValue());

          console.log('formData', dataRef);
          closeModal();
          emit('success', dataRef);
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
      };
    },
  });
</script>
