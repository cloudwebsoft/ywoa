<template>
  <div class="w-2/3 mx-auto h-full">
    <ScrollContainer class="pr-4" v-loading="loading" :loading-tip="t('common.loadingText')">
      <BasicForm @register="registerForm">
        <template #icon="{ field, model }">
          <div class="flex items-center">
            <SvgIcon size="50" :name="model[field]" v-if="model[field]" />
            <a-button type="primary" @click="handleSelectIcon" class="ml-5">选择</a-button>
          </div>
        </template>
        <template #queryId="{ field, model }">
          <div class="flex items-center">
            <Select
              style="width: 90%"
              :options="queryIdList"
              v-model:value="model[field]"
              :fieldNames="{ label: 'queryName', value: 'id', key: 'id' }"
              @change="getQueryId"
            />
            <a-button type="primary" @click="handleAddFieldSelect" class="ml-5">添加映射</a-button>
          </div>
        </template>
        <template #queryCondMap>
          <FormItemRest>
            <div
              v-for="(item, index) in queryCondMapList"
              :key="`queryCondMap${index}`"
              class="mb-2"
            >
              <Select
                style="width: 160px"
                :options="formGetFields"
                v-model:value="item.label"
                :fieldNames="{ label: 'title', value: 'name', key: 'name' }"
              />
              <arrow-right-outlined class="ml-2 mr-2" />
              <Select
                style="width: 160px"
                :options="condFieldList"
                v-model:value="item.value"
                :fieldNames="{ label: 'title', value: 'name', key: 'name' }"
              />
              <close-outlined
                class="ml-2 cursor-pointer"
                @click="handleDeleteQueryCondMap(index)"
              />
            </div>
          </FormItemRest>
        </template>
        <template #description="{ field, model }">
          <FormItemRest>
            <Popover placement="bottomLeft">
              <template #content>
                <div class="p-2 flex">
                  <div>
                    <div
                      v-for="item in descriptionOptions.filter((_, index) => index < 7)"
                      :key="item.value"
                      class="cursor-pointer"
                      @click="handleItemDescription(item)"
                    >
                      {{ item.label }}
                    </div>
                  </div>
                  <div class="ml-5 pr-2 max-h-56 overflow-auto">
                    <div
                      v-for="item in descriptionOptions.filter((_, index) => index > 6)"
                      :key="item.value"
                      class="cursor-pointer"
                      @click="handleItemDescription(item)"
                    >
                      {{ item.label }}
                    </div>
                  </div>
                </div>
              </template>

              <Input
                style="width: 100%"
                v-model:value="model[field]"
                autocomplete="off"
                @change="(e) => setDescriptionSuffix(e.target.value)"
              />
              <span>{{ descriptionText }}</span>
            </Popover>
          </FormItemRest>
        </template>
      </BasicForm>
    </ScrollContainer>
    <SelectIconModal @register="registerSelectIconModal" @success="handleSelectIconModal" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted, watchEffect } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './Attribute.data';
  import { ScrollContainer } from '/@/components/Container';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useModal } from '/@/components/Modal';
  import SelectIconModal from './modules/SelectIconModal.vue';
  import { SvgIcon } from '/@/components/Icon';
  import { Input, Select, Popover, Form } from 'ant-design-vue';
  import { ArrowRightOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import {
    getAdminFlowCreateNode,
    getAdminFlowEditNode,
    getAdminFlowUpdateNode,
    getVisualListQuery,
    getFormGetFields,
    getVisualGetCondField,
    getFormListByFlowType,
  } from '/@/api/flowManage/flowManage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { buildUUID } from '/@/utils/uuid';
  import { isArray } from '/@/utils/is';
  const { t } = useI18n();

  export default defineComponent({
    components: {
      BasicForm,
      ScrollContainer,
      SelectIconModal,
      SvgIcon,
      Input,
      Select,
      ArrowRightOutlined,
      CloseOutlined,
      Popover,
      FormItemRest: Form.ItemRest,
    },
    props: {
      currentRecord: {
        type: Object as PropType<object>,
        default: () => {
          return {};
        },
      },
      currentKey: {
        type: String as PropType<string>,
        default: '',
      },
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      const isUpdate = ref(true);
      const loading = ref(false);
      const defaultKeys = ref({
        root_code: 'root',
        isMobileStart: 1,
        type: 0,
        isHome: true,
        isDebug: 0,
        seltype: 0,
        isOpen: '1',
        parent_code: 'root',
        code: '',
        unitCode: '',
      });
      const dataRef = ref({});
      const descriptionText = ref('');
      const defaultDescription = [
        {
          value: '{dept}',
          label: '部门名称',
          description: '行政部',
        },
        {
          value: '{user}',
          label: '发起人',
          description: '张三',
        },
        {
          value: '流程名称',
          label: '流程名称',
          description: '流程名称',
        },
        {
          value: '{date:yyyy-MM-dd}',
          label: '年-月-日',
          description: '日期：2001-10-01',
        },
        {
          value: '{date:MM-dd}',
          label: '月-日',
          description: '日期：10-01',
        },
        {
          value: '{date:MM-dd-yyyy}',
          label: '月-日-年',
          description: '日期：10-01-2001',
        },
        {
          value: ':',
          label: ':',
          description: ':',
        },
      ];
      const descriptionOptions = ref([]);
      const queryCondMapList = ref([]); //条件映射
      const [
        registerForm,
        { setFieldsValue, updateSchema, resetFields, validate, getFieldsValue },
      ] = useForm({
        labelWidth: 180,
        schemas: formSchema,
        showActionButtonGroup: true,
        actionColOptions: {
          span: 23,
        },
        submitButtonOptions: {
          text: '保存',
        },
        submitFunc: customSubmitFunc,
      });
      const currentRecord = ref({});
      const initData = () => {
        currentRecord.value = Object.assign({}, props.currentRecord);
        console.log('currentRecord.value', currentRecord.value);
        initForm();
      };
      const initForm = async () => {
        resetFields();
        isUpdate.value = props.currentKey === 'AttributeAdd' ? false : true; //false 新增 true 编辑
        if (!isUpdate.value) {
          dataRef.value = defaultKeys.value;
          //新增
          dataRef.value.code = buildUUID(20);
          dataRef.value.parent_code = currentRecord.value.code;
          dataRef.value.parentCode = currentRecord.value.code;

          dataRef.value.layer = currentRecord.value.layer - 0 + 1;
          if (dataRef.value.layer == 2 || dataRef.value.layer == 1) {
            dataRef.value.seltype = 0;
            dataRef.value.type = 0;
          } else {
            dataRef.value.seltype = 2;
            dataRef.value.type = 2;
          }
          dataRef.value.isDebug = '0';
          dataRef.value.templateId = -1;

          updateSchema([
            {
              field: 'seltype',
              componentProps: {
                disabled: false,
                options:
                  dataRef.value.layer == 1
                    ? [
                        {
                          value: 0, // op是AddChild或者op是modify if (("AddChild".equals(op) && "root".equals(parent_code)) || (isModify && leaf.getType() == Leaf.TYPE_NONE))
                          label: '分类',
                        },
                        {
                          value: 1, //AddChild".equals(op) 禁用这个
                          label: '自由流程',
                        },
                        {
                          value: 2,
                          label: '固定流程',
                        },
                      ]
                    : [
                        {
                          value: 1, //AddChild".equals(op) 禁用这个
                          label: '自由流程',
                        },
                        {
                          value: 2,
                          label: '固定流程',
                        },
                      ],
                onChange: (val) => {
                  setFieldsValue({ type: val });
                },
              },
            },
            {
              field: 'code',
              componentProps: {
                disabled: false,
              },
            },
            {
              field: 'queryId',
              ifShow: false,
            },
            {
              field: 'queryCondMap',
              ifShow: false,
            },
            {
              field: 'queryRole',
              ifShow: false,
            },
          ]);
        } else {
          await getAdminFlowEditNode({ code: currentRecord.value.code }).then((res) => {
            let result = res || {};
            if (result.children) delete result.children;

            dataRef.value = result;
            //发起
            if (dataRef.value.mobileStart) {
              dataRef.value.isMobileStart = 1;
            } else {
              dataRef.value.isMobileStart = 0;
            }
            if (dataRef.value.debug) {
              dataRef.value.isDebug = '1';
            } else {
              dataRef.value.isDebug = '0';
            }
            if (dataRef.value.open) {
              dataRef.value.isOpen = '1';
            } else {
              dataRef.value.isOpen = '0';
            }
            delete dataRef.value.open;
            dataRef.value.seltype = dataRef.value.type;
            updateSchema([
              {
                field: 'seltype',
                componentProps: {
                  disabled: true,
                  options: [
                    {
                      value: 0, // op是AddChild或者op是modify if (("AddChild".equals(op) && "root".equals(parent_code)) || (isModify && leaf.getType() == Leaf.TYPE_NONE))
                      label: '分类',
                    },
                    {
                      value: 1, //AddChild".equals(op) 禁用这个
                      label: '自由流程',
                    },
                    {
                      value: 2,
                      label: '固定流程',
                    },
                  ],
                  onChange: (val) => {
                    setFieldsValue({ type: val });
                  },
                },
              },
              {
                field: 'code',
                componentProps: {
                  disabled: true,
                },
              },
              {
                field: 'queryId',
                ifShow: true,
              },
              {
                field: 'queryCondMap',
                ifShow: true,
              },
              {
                field: 'queryRole',
                ifShow: true,
              },
            ]);
          });
        }

        if (dataRef.value.dept) {
          dataRef.value.depts = dataRef.value.dept.split(',');
        } else {
          dataRef.value.depts = undefined;
        }

        if (dataRef.value.queryRole) {
          dataRef.value.queryRole = dataRef.value.queryRole.split(',');
        } else {
          dataRef.value.queryRole = undefined;
        }
        queryCondMapList.value = [];
        if (dataRef.value.queryCondMap) {
          const queryCondMap = JSON.parse(dataRef.value.queryCondMap);
          let newCondMap = [];
          for (let v in queryCondMap) {
            newCondMap.push({
              label: v,
              value: queryCondMap[v],
            });
          }
          queryCondMapList.value = newCondMap;
        } else {
          dataRef.value.queryCondMap = '';
        }

        if (dataRef.value.formCode) {
          getFormGetFieldsData(dataRef.value.formCode);
        }
        if (dataRef.value.queryId) {
          getQueryId(dataRef.value.queryId);
        }

        // setModalProps({ confirmLoading: false });

        setFieldsValue({
          ...dataRef.value,
        });

        descriptionOptions.value = [...defaultDescription];
        setDescriptionSuffix(dataRef.value.description);

        //获取关联查询
        getVisualListQueryData();
        //获取流程下的表单
        getFormListByFlowTypeData();
      };

      //自动生成标题
      //点击块
      const handleItemDescription = (record: Record) => {
        let formData = getFieldsValue();
        const values = record.value === '流程名称' ? formData.name : record.value;
        if (formData.description) {
          formData.description += ' ' + values;
        } else {
          formData.description = values;
        }
        setFieldsValue({
          description: formData.description,
        });
        setDescriptionSuffix(formData.description);
      };

      //设置 自动生成图标 右侧提示
      const setDescriptionSuffix = (text: String) => {
        let descriptions = text;
        if (descriptions) {
          descriptionOptions.value.forEach((item) => {
            if (descriptions.includes(item.value)) {
              descriptions = descriptions.replaceAll(item.value, item.description);
            }
          });
        } else {
          descriptions = '';
        }
        descriptionText.value = `例如：${descriptions}`;
        // updateSchema([
        //   {
        //     field: 'description',
        //     suffix: `例如：${descriptions}`,
        //   },
        // ]);
      };

      const queryIdList = ref([]); //关联查询表
      const condFieldList = ref([]);
      //获取关联查询
      const getVisualListQueryData = async () => {
        const res = await getVisualListQuery({ type: 'all' });
        queryIdList.value = res;
      };

      //获取关联查询id
      const getQueryId = (id) => {
        getVisualGetCondField({ id }).then((res) => {
          condFieldList.value = res;
        });
      };

      //增加条件映射
      const handleAddFieldSelect = () => {
        const obj = {
          label: '',
          value: '',
        };
        queryCondMapList.value.push(obj);
      };

      //删除条件映射
      const handleDeleteQueryCondMap = (index) => {
        queryCondMapList.value.splice(index, 1);
      };

      const formListByFlowType = ref([]);
      //获取流程下的表单
      const getFormListByFlowTypeData = () => {
        getFormListByFlowType({ code: dataRef.value.parentCode }).then((res) => {
          formListByFlowType.value = res;
          updateSchema([
            {
              field: 'formCode',
              componentProps: {
                options: formListByFlowType.value,
                fieldNames: { label: 'name', value: 'code', key: 'code' },
                onChange: (values: String, valueList: Array) => {
                  getFormGetFieldsData(values);
                },
              },
            },
          ]);
        });
      };

      const formGetFields = ref([]);
      //获取条件映射数据
      const getFormGetFieldsData = (formCode) => {
        getFormGetFields({ formCode }).then((res) => {
          formGetFields.value = res || [];
          descriptionOptions.value = [...defaultDescription];
          formGetFields.value.forEach((item) => {
            descriptionOptions.value.push({
              value: `{${item.name}}`,
              label: `${item.title}`,
              description: `${item.title}`,
            });
          });
        });
      };

      async function customSubmitFunc() {
        try {
          const values = await validate();
          loading.value = true;
          const formData = Object.assign({}, dataRef.value, values);
          if (isArray(formData.depts)) {
            formData.depts = formData.depts.join(',');
          }
          if (isArray(formData.queryRole)) {
            formData.queryRole = formData.queryRole.join(',');
          }
          if (queryCondMapList.value.length) {
            let queryCondMap = {};
            queryCondMapList.value.forEach((item) => {
              queryCondMap[item.label] = item.value;
            });
            formData.queryCondMap = JSON.stringify(queryCondMap);
          }
          if (!isUpdate.value) {
            await getAdminFlowCreateNode(formData);
            initForm();
            emit('success', 'add');
          } else {
            await getAdminFlowUpdateNode(formData);
            currentRecord.value.code = formData.code;
            initForm();
            emit('success', 'edit');
          }
        } finally {
          loading.value = false;
        }
      }

      //选择图标
      const [registerSelectIconModal, { openModal: openSelectIconModal }] = useModal();

      const handleSelectIcon = () => {
        openSelectIconModal(true, {});
      };
      //选择图标回调
      const handleSelectIconModal = (icon: string) => {
        console.log('icon', icon);
        setFieldsValue({ icon });
      };
      return {
        registerForm,
        loading,
        t,
        initData,
        initForm,
        registerSelectIconModal,
        handleSelectIconModal,
        handleSelectIcon,
        queryIdList,
        getQueryId,
        handleAddFieldSelect,
        handleDeleteQueryCondMap,
        condFieldList,
        descriptionOptions,
        formGetFields,
        queryCondMapList,
        handleItemDescription,
        descriptionText,
        setDescriptionSuffix,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.ant-form-item:not(.ant-form-item-with-help)) {
    margin-bottom: 10px;
  }
</style>
