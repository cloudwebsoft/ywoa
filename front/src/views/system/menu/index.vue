<template>
  <PageWrapper dense contentFullHeight fixedHeight contentClass="flex">
    <MenuTree
      class="w-1/4 xl:w-1/5 treeList"
      @select="handleSelect"
      @add-action="addAction"
      @del-action="delAction"
      @ok="onClose"
      ref="treeRef"
    />
    <div class="mt-4 mb-4 ml-1 w-3/4 xl:w-4/5">
      <Card
        :headStyle="{ padding: '0 16px' }"
        :bodyStyle="{ padding: '10px 0' }"
        :title="getTitle"
        style="height: 100%; overflow-y: auto"
      >
        <BasicForm @register="registerForm">
          <template #pvg="{ model, field }">
            <div>
              <FormItemRest>
                <!-- <div class="mb-2">
                  <RadioGroup v-model:value="radioValue" button-style="solid">
                    <RadioButton value="radioall" @click="getRadioValue('radioall')"
                      >全部用户</RadioButton
                    >
                    <RadioButton value="radioSelect" @click="getRadioValue('radioSelect')"
                      >选择用户</RadioButton
                    >
                  </RadioGroup>
                </div> -->
              </FormItemRest>
              <Select
                v-model:value="model[field]"
                :options="pvgs"
                mode="multiple"
                :showSearch="true"
                optionFilterProp="label"
              />
            </div>
          </template>
          <template #advanceBefore>
            <Button type="primary" class="mr-2" @click="handleOpen" v-if="isBtnOpenShow"
              >打开</Button
            >
          </template>
        </BasicForm>
      </Card>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref, computed, unref, onBeforeMount } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Select, Form, Button } from 'ant-design-vue';
  import MenuTree from './MenuTree.vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { setEditMenu } from '/@/api/system/system';
  import { FormSchema } from '/@/components/Table';
  import { useGo } from '/@/hooks/web/usePage';
  import {
    getFlowDirTree,
    getModulesAll,
    getBasicList,
    getPortalListForMenu,
    getApplications,
  } from '/@/api/system/system';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: 'MenuManagement',
    components: {
      PageWrapper,
      Card,
      MenuTree,
      BasicForm,
      Select,
      FormItemRest: Form.ItemRest,
      Button,
    },
    setup() {
      const treeRef: any = ref(null);
      let dataRef = reactive<any>({});
      let model = reactive({});
      const isUpdate = ref(true);
      let pvgs = ref<any>([]);
      const isBtnOpenShow = ref(false);
      const isMenuGroupByApplication = ref(true);
      const userStore = useUserStore();

      function handleSelect(deptId = '', trc) {
        if (deptId) {
          let node = trc.node.dataRef;
          dataRef = node;
          isUpdate.value = true;
          dataRef.pvg = dataRef.pvg
            ? Array.isArray(dataRef.pvg)
              ? dataRef.pvg
              : dataRef.pvg.split(',')
            : [];

          setEditMenu({ code: dataRef.code }).then((res: any) => {
            // updateSchema使用后会导致点击模块后，不能显示模块选择框及门户选择框，故通过在onMount中getServerInfo获取isMenuGroupByApplication
            // isMenuGroupByApplication.value = res.isMenuGroupByApplication;
            // updateSchema([
            //   {
            //     field: 'applicationCode',
            //     required: isMenuGroupByApplication.value,
            //     ifShow: isMenuGroupByApplication.value,
            //   },
            // ]);

            pvgs.value = res.allPrivList || [];
            if (pvgs.value && pvgs.value.length > 0) {
              pvgs.value.forEach((item) => {
                item.disabled = item.layer == 1 ? true : false;
                item.label = item.description;
                item.value = item.priv;
              });
            }
          });
          console.log('handleSelect dataRef', dataRef);
          editSetFieldsValue(dataRef);
        } else {
          onClose();
        }
      }

      let serverInfo = userStore.getServerInfo;
      console.log('serverInfo', serverInfo);
      isMenuGroupByApplication.value = serverInfo.isMenuGroupByApplication;
      // onBeforeMount(async () => {
      //   const serverInfo = await getServerInfo();
      //   isMenuGroupByApplication.value = serverInfo.isMenuGroupByApplication;
      // });

      const isPreCode = (type: string) => {
        if (type == 'iframe' || type == 'portals') {
          return true;
        } else {
          return !type;
        }
      };
      const formSchema: FormSchema[] = [
        {
          field: 'name',
          label: '名称',
          component: 'Input',
          required: true,
          colProps: {
            span: 24,
          },
        },
        {
          field: 'preCode',
          label: '类型',
          component: 'RadioButtonGroup',
          defaultValue: '',
          colProps: {
            span: 24,
          },
          componentProps: {
            options: [
              { label: '链接', value: '' },
              { label: '流程', value: 'flow' },
              { label: '模块', value: 'module' },
              // { label: '基础数据', value: 'basicdata' },
              { label: '框架', value: 'iframe' },
              { label: '门户', value: 'portals' },
            ],
            onchange: (e) => {
              if (e.target.value == 'module') {
                setFieldsValue({
                  formCode: '',
                  component: 'pages/smartModule/smartModule',
                });
              } else if (e.target.value == 'iframe') {
                setFieldsValue({ formCode: '', component: 'pages/processManagement/iframePage' });
              } else if (e.target.value == 'flow') {
                setFieldsValue({
                  formCode: '',
                  component: 'pages/processManagement/processHandleView',
                });
              } else if (e.target.value == 'portals') {
                setFieldsValue({
                  formCode: '',
                  component: 'dashboard/analysis/portals',
                });
              } else {
                // 如果是添加操作
                if (!isUpdate.value) {
                  let comp = dataRef.layer == 2 ? 'LAYOUT' : '';
                  setFieldsValue({ formCode: '', component: comp });
                }
              }
            },
          },
        },
        {
          field: 'formCode',
          label: '流程',
          component: 'ApiTreeSelect',
          required: true,
          colProps: {
            span: 24,
          },
          componentProps: {
            api: getFlowDirTree,
            fieldNames: { label: 'name', value: 'code' },
            showSearch: true,
            treeNodeFilterProp: 'name',
          },
          ifShow: ({ values }) => values.preCode === 'flow',
        },
        {
          field: 'formCode',
          label: '模块',
          component: 'ApiSelect',
          required: true,
          colProps: {
            span: 24,
          },
          componentProps: {
            api: getModulesAll,
            labelField: 'name',
            valueField: 'code',
            showSearch: true,
            optionFilterProp: 'label',
          },
          ifShow: ({ values }) => values.preCode === 'module',
        },
        {
          field: 'formCode',
          label: '基础数据',
          component: 'ApiSelect',
          required: true,
          colProps: {
            span: 24,
          },
          componentProps: {
            api: getBasicList,
            labelField: 'name',
            valueField: 'id',
            key: 'id',
          },
          ifShow: ({ values }) => values.preCode === 'basicdata',
        },
        {
          field: 'formCode',
          label: '门户',
          component: 'ApiSelect',
          required: true,
          colProps: {
            span: 24,
          },
          componentProps: {
            mode: 'multiple',
            // multiple: true,
            api: getPortalListForMenu,
            labelField: 'name',
            valueField: 'id',
            optionFilterProp: 'label',
            key: 'id',
          },
          ifShow: ({ values }) => values.preCode === 'portals',
        },
        {
          field: 'link',
          label: '路由地址',
          component: 'Input',
          required: true,
          colProps: {
            span: 24,
          },
          componentProps: {
            onchange: (e) => {
              model.link = e.target.value;
            },
          },
          // ifShow: ({ values }) => values.preCode === '',
        },
        {
          field: 'description',
          label: '描述',
          component: 'Input',
          required: false,
          colProps: {
            span: 24,
          },
        },
        {
          field: 'icon',
          label: '图标',
          component: 'IconPicker',
          colProps: {
            span: 24,
          },
        },
        {
          field: 'pvg',
          label: '菜单可见的权限', //多选
          component: 'Select',
          helpMessage: ['不选择表示默认可见'],
          colProps: {
            span: 24,
          },
          slot: 'pvg',
          // componentProps: {
          //   api: getRolePriv,
          //   labelField: 'name',
          //   valueField: 'code',
          //   showSearch: true,
          //   optionFilterProp: 'label',
          //   getPopupContainer: () => document.body,
          // },
          // ifShow: ({ values }) => isPreCode(values.preCode),
        },
        {
          field: 'outerLink',
          label: '是否外链',
          component: 'Switch',
          colProps: {
            span: 24,
          },
          defaultValue: false,
          componentProps: {
            checkedChildren: '是',
            unCheckedChildren: '否',
          },
        },
        {
          field: 'cachable',
          label: '是否缓存',
          component: 'Switch',
          colProps: {
            span: 24,
          },
          defaultValue: 0,
          componentProps: {
            checkedChildren: '是',
            unCheckedChildren: '否',
          },
        },
        {
          field: 'isUse',
          label: '是否启用',
          component: 'RadioButtonGroup',
          colProps: {
            span: 24,
          },
          defaultValue: 1,
          componentProps: {
            options: [
              { label: '启用', value: 1 },
              { label: '禁用', value: 0 },
            ],
          },
        },
        {
          field: 'front',
          label: '是否前端菜单项',
          component: 'RadioButtonGroup',
          colProps: {
            span: 24,
          },
          defaultValue: 2,
          componentProps: {
            options: [
              { label: '同时', value: 0 },
              { label: '后端', value: 1 },
              { label: '前端', value: 2 },
            ],
            onchange: (e) => {
              // if (e.target.value !== 1) {
              //   setFieldsValue({ component: '' });
              // }
            },
          },
        },
        {
          field: 'applicationCode',
          label: '所属应用',
          component: 'ApiSelect',
          required: isMenuGroupByApplication.value,
          colProps: {
            span: 24,
          },
          defaultValue: 'all',
          componentProps: {
            api: getApplications,
            labelField: 'name',
            valueField: 'code',
            key: 'code',
          },
          ifShow: isMenuGroupByApplication.value,
        },
        {
          field: 'component',
          label: '组件地址', //多选
          required: ({ values }) => values.front === 0 || values.front === 2,
          component: 'Input',
          defaultValue: 'LAYOUT',
          // helpMessage: [''],
          colProps: {
            span: 24,
          },
          ifShow: ({ values }) => values.front === 0 || values.front === 2,
        },
      ];

      const { createMessage } = useMessage();

      const [registerForm, { validate, setProps, setFieldsValue, resetFields, updateSchema }] =
        useForm({
          labelCol: {
            span: 8,
          },
          wrapperCol: {
            span: 10,
          },
          schemas: formSchema,
          actionColOptions: {
            offset: 8,
            span: 24,
            style: { textAlign: 'center' },
          },
          submitButtonOptions: {
            text: '保存',
          },
          submitFunc: customSubmitFunc,
        });

      function addAction() {
        if (!dataRef.code) {
          createMessage.warning('请选择节点');
          return;
        }
        model = { parentCode: dataRef.code };
        setEditMenu({ code: dataRef.code }).then((res) => {
          console.log('setEditMenu res', res);

          // updateSchema使用后会导致点击模块后，不能显示模块选择框，故通过在onMount中getServerInfo获取isMenuGroupByApplication
          // isMenuGroupByApplication.value = res.isMenuGroupByApplication;
          // updateSchema([
          //   {
          //     field: 'applicationCode',
          //     required: isMenuGroupByApplication.value,
          //     ifShow: isMenuGroupByApplication.value,
          //   },
          // ]);

          pvgs.value = res.allPrivList || [];
          if (pvgs.value && pvgs.value.length > 0) {
            pvgs.value.forEach((item) => {
              item.disabled = item.layer == 1 ? true : false;
              item.label = item.description;
              item.value = item.priv;
            });
          }
          addSetFieldsValue(model);
        });
      }
      function addSetFieldsValue(model) {
        isUpdate.value = false;
        resetFields();
        setFieldsValue({ ...model });
      }

      function editSetFieldsValue(values) {
        resetFields();

        // 注意不能在此赋值，因为model = values，而values实际上为dataRef，而后者为reactive型变量，这样会导致之前所选的节点的link和component变为空
        // model['link'] = '';
        // model['component'] = '';
        console.log('editSetFieldsValue values', values);
        model = values;

        isBtnOpenShow.value =
          model['link'] && model['link'].length > 0 && model['component'] != 'LAYOUT';

        if (model['preCode'] == 'portals') {
          // 将字符串型转为整型，因为其中为门户的id，是整型的
          if (!Array.isArray(model['formCode'])) {
            model['formCode'] = model['formCode'] ? model['formCode'].split(',') : [];
            model['formCode'] = model['formCode'].map((item) => {
              return item * 1;
            });
          }
        }
        console.log('editSetFieldsValue model2', model);
        setFieldsValue({ ...model });
      }
      function delAction() {
        if (!dataRef.code) {
          createMessage.warning('请选择节点');
          return;
        }
        unref(treeRef)?.setMenuDelete(dataRef.code);
        onClose();
      }
      async function customSubmitFunc() {
        try {
          let values = await validate();
          // model中含有对象，在submit提交遍历时会因层级过多，而报：
          // Avoid app logic that relies on enumerating keys on a component instance. The keys will be empty in production mode to avoid performance overhead
          // const formData = Object.assign({}, model, values);
          const formData = Object.assign({}, values);
          formData['code'] = model['code'];
          formData['parentCode'] = model['parentCode'];
          formData.pvg = formData.pvg && formData.pvg.length > 0 ? formData.pvg.join(',') : '';
          if (formData['preCode'] == 'portals') {
            formData.formCode =
              formData.formCode && formData.formCode.length > 0 ? formData.formCode.join(',') : '';
          }

          let result = ref(null);
          setProps({
            submitButtonOptions: {
              loading: true,
            },
          });
          if (!formData.code) {
            formData.code = new Date().getTime();
            if (dataRef.code) {
              formData.parentCode = dataRef.code;
            }
            result.value = await unref(treeRef)?.setMuneAdd(formData);
            onClose(1);
          } else {
            result.value = await unref(treeRef)?.setMuneEdit(formData);
          }
          createMessage.success('操作成功');
          setProps({
            submitButtonOptions: {
              loading: false,
            },
          });
          unref(treeRef)?.fetch();
        } catch (error) {}
      }
      const getTitle = computed(() => (!unref(isUpdate) ? '新增菜单' : '编辑菜单'));
      function onClose(e?: number) {
        if (e != 1) {
          //1是新增，不用清空
          dataRef = {};
          model = {};
        }
        resetFields();
      }

      const go = useGo();
      function handleOpen() {
        console.log('handleOpen model', model);
        go({
          path: model.link,
          meta: { formCode: model.formCode, type: model.type },
        });
      }

      return {
        handleSelect,
        registerForm,
        getTitle,
        addSetFieldsValue,
        editSetFieldsValue,
        treeRef,
        dataRef,
        model,
        addAction,
        delAction,
        onClose,
        pvgs,
        handleOpen,
        isBtnOpenShow,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.ant-card-head) {
    min-height: 37px;
  }
  :deep(.ant-card-head-title) {
    padding: 0;
    line-height: 35px;
  }
</style>
