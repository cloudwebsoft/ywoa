<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :minHeight="100"
  >
    <BasicForm @register="registerForm">
      <template #plusUserRealNames="{ model, field }">
        <Row class="justify-between">
          <Col :span="19">
            <Input
              disabled
              style="width: 100%"
              v-model:value="model[field]"
              placeholder="请选择人员"
            />
          </Col>
          <Col :span="4">
            <Button type="primary" @click="handleSelectUser">选择</Button>
          </Col>
        </Row>
      </template>
    </BasicForm>
    <SelectUser @register="registerSelectUserModal" @success="handleSuccess" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { FormSchema } from '/@/components/Table';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { SelectUser } from '/@/components/CustomComp';
  import { useModal } from '/@/components/Modal';
  import { Row, Col, Button, Input } from 'ant-design-vue';
  import { getPlus } from '/@/api/process/process';

  export default defineComponent({
    components: { BasicModal, BasicForm, SelectUser, Row, Col, Button, Input },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let isUpdate = ref(true);
      let dataRef = reactive<Recordable>({});
      let isFlowStarted = ref(true);
      let myActionId = ref(-1);

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false, width: '40%' });
        isUpdate.value = !!data?.isUpdate;
        isFlowStarted.value = !!data?.isFlowStarted;
        myActionId.value = data?.myActionId;
        console.log('data', data);

        dataRef = data.record;

        setFieldsValue({
          ...dataRef,
        });

        //设置表单控件
        if (!unref(isFlowStarted)) {
          updateSchema([
            {
              field: 'type',
              componentProps: {
                options: [
                  { label: '后加签', value: '1' },
                  { label: '并签', value: '2' },
                ],
              },
            },
          ]);
        }
      });
      const FormSchema: FormSchema[] = [
        {
          field: 'type',
          label: '加签类型',
          component: 'RadioGroup',
          required: true,
          componentProps: {
            options: [
              { label: '前加签', value: '0' },
              { label: '后加签', value: '1' },
              { label: '并签', value: '2' },
            ],
          },
          colProps: {
            span: 24,
          },
        },
        {
          field: 'mode',
          label: '审批方式',
          component: 'RadioGroup',
          defaultValue: '',
          colProps: {
            span: 24,
          },
          required: ({ values }) => values.type != 2,
          componentProps: {
            options: [
              { label: '顺序审批', value: '0' },
              { label: '只需其中一人处理', value: '1' },
              { label: '全部审批', value: '2' },
            ],
            getPopupContainer: () => document.body,
            onchange: () => {},
          },
          ifShow: ({ values }) => values.type != 2,
        },

        {
          field: 'plusUserRealNames', //plusUserRealNames  plusUsers
          label: '选择人员',
          component: 'Input',
          required: true,
          componentProps: {
            disabled: true,
          },
          slot: 'plusUserRealNames',
          colProps: {
            span: 24,
          },
        },
      ];
      const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
        labelWidth: 100,
        schemas: FormSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 23,
        },
      });

      let selectUserArr = [];
      const [registerSelectUserModal, { openModal }] = useModal();
      const handleSuccess = (data) => {
        console.log('user sel', data);
        selectUserArr = data;
        if (data && data.length > 0) {
          dataRef['plusUsers'] = data.map((item) => item.name);
          dataRef['users'] = dataRef['plusUsers'].join(',');
          dataRef['plusUserRealNames'] = data.map((item) => item.realName);
          setFieldsValue({
            plusUserRealNames: dataRef['plusUserRealNames'],
          });
        }
      };
      const handleSelectUser = () => {
        // openModal(true, { users: dataRef['plusUsers'] });
        openModal(true, { users: selectUserArr });
      };

      const getTitle = '加签';

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });
          const formData = Object.assign({}, dataRef, values);
          formData['myActionId'] = myActionId.value;

          console.log('handleSubmit formData', formData);
          let data = await getPlus(formData);
          emit('success', data.type, data.plusDesc);
          closeModal();
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        registerForm,
        getTitle,
        handleSubmit,
        registerSelectUserModal,
        handleSelectUser,
        handleSuccess,
      };
    },
  });
</script>
