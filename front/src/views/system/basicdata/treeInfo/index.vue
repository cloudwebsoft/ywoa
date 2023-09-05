<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="70%"
    :showOkBtn="false"
  >
    <PageWrapper dense contentFullHeight fixedHeight contentClass="flex">
      <Tree
        class="w-1/4 xl:w-1/5 treeList"
        @select="handleSelect"
        @add-action="addAction"
        @del-action="delAction"
        @ok="onClose"
        ref="treeRef"
        :code="code"
      />
      <div class="mt-4 mb-4 ml-1 w-3/4 xl:w-4/5">
        <Card
          :headStyle="{ padding: '0 16px' }"
          :bodyStyle="{ padding: '10px 0' }"
          :title="getTitle"
          style="height: 100%; overflow-y: auto"
        >
          <BasicForm @register="registerForm" />
        </Card>
      </div>
    </PageWrapper>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref, computed, unref, onMounted, watch } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { Card } from 'ant-design-vue';
  import Tree from './tree.vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema, preCode } from './tree.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';

  export default defineComponent({
    name: 'TreeDrawer',
    components: { PageWrapper, Card, Tree, BasicForm, BasicDrawer },
    setup() {
      const treeRef: any = ref(null);
      let dataRef = reactive<Recordable>({});
      let model = reactive({});
      const isUpdate = ref(true);
      const code = ref('');
      watch(
        () => preCode,
        () => {
          setFieldsValue({ formCode: '', formCode2: '' });
        },
        {
          deep: true,
        },
      );
      function handleSelect(deptId = '', trc) {
        console.log('deptId', deptId, 'trc==>', trc);
        if (deptId) {
          dataRef = trc.node.dataRef;
          isUpdate.value = true;
          dataRef.isOpen = dataRef.open ? 1 : 0;
          editSetFieldsValue(dataRef);
        } else {
          onClose();
        }
      }

      const { createMessage } = useMessage();

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        resetFields();
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        code.value = data.record.code;

        // 已被tree.vue中watchEffect取代
        // setTimeout(() => {
        //   treeRef.value.fetch();
        // }, 200);

        if (unref(isUpdate)) {
          dataRef = data.record;
          console.log('dataRef', dataRef);
          setFieldsValue({
            ...dataRef,
          });
          updateSchema([
            {
              field: 'code',
              componentProps: { readOnly: true },
            },
          ]);
        } else {
          dataRef = {};
          updateSchema([
            {
              field: 'code',
              componentProps: { readOnly: false },
            },
          ]);
        }
      });

      const [registerForm, { validate, setProps, setFieldsValue, updateSchema, resetFields }] =
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
            span: 16,
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
        model = { parent_code: dataRef.code };
        addSetFieldsValue(model);
      }
      function addSetFieldsValue(model) {
        isUpdate.value = false;
        resetFields();
        setFieldsValue({ ...model });
      }
      function editSetFieldsValue(values) {
        resetFields();
        model = values;
        model.formCode2 = model.formCode;
        console.log('model==>', model);
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
          // 不能直接把组件对象放在formData中，保存菜单时报： Avoid app logic that relies on enumerating keys on a component instance.
          // const formData = Object.assign({}, model, values);
          const formData = Object.assign({}, values);

          if (formData.formCode2) formData.formCode = formData.formCode2;
          let result = ref(null);
          setProps({
            submitButtonOptions: {
              loading: true,
            },
          });
          if (!unref(isUpdate)) {
            formData.code = new Date().getTime();
            if (dataRef.code) {
              formData.parentCode = dataRef.code;
            }
            result.value = await unref(treeRef)?.setMuneAdd(formData);
            onClose(1);
          } else {
            delete formData.children;
            formData.code = model['code'];
            formData.parentCode = model['parentCode'];
            result.value = await unref(treeRef)?.setMuneEdit(formData);
            if (result.value && result.value.res == 0) {
              createMessage.success('操作成功');
            }
          }

          unref(treeRef)?.fetch();
        } catch (error) {
        } finally {
          setProps({
            submitButtonOptions: {
              loading: false,
            },
          });
        }
      }

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      function onClose(e) {
        if (e != 1) {
          //1是新增，不用清空
          dataRef = {};
          model = {};
        }
        resetFields();
      }

      onMounted(() => {
        // fetch();
        window.setFieldsValue = setFieldsValue;
      });

      return {
        handleSelect,
        registerForm,
        registerDrawer,
        getTitle,
        addSetFieldsValue,
        editSetFieldsValue,
        treeRef,
        dataRef,
        model,
        code,
        addAction,
        delAction,
        onClose,
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
