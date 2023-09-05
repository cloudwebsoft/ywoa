<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :minHeight="100"
  >
    <BasicForm @register="registerForm">
      <template #userRealNames="{ model, field }">
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
  import { Row, Col, Button, TreeSelect, Input } from 'ant-design-vue';
  import { getDepartment, getUnitTree } from '/@/api/system/system';
  import { getDistribute } from '/@/api/process/process';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';

  export default defineComponent({
    components: { BasicModal, BasicForm, SelectUser, Row, Col, Button, Input },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      let isUpdate = ref(true);
      let dataRef = reactive<Recordable>({});

      const { createMessage } = useMessage();
      const { t } = useI18n();

      const [registerModal, { setModalProps, closeModal, updateSchema }] = useModalInner(
        async (data) => {
          resetFields();
          setModalProps({ confirmLoading: false, width: '35%' });
          isUpdate.value = !!data?.isUpdate;
          console.log('data', data);
          if (unref(isUpdate)) {
            dataRef = {
              title: data.record.cwsWorkflowTitle,
              isFlowDisplay: '1',
              divUnit: [],
              userNames: '',
              userRealNames: '',
              flowId: data.record.flowId,
            };
            setFieldsValue({
              ...dataRef,
            });
          } else {
            dataRef = {};
          }
        },
      );
      const FormSchema: FormSchema[] = [
        {
          field: 'title',
          label: '标题',
          component: 'Input',
          colProps: {
            span: 24,
          },
        },
        {
          field: 'isFlowDisplay',
          label: '能否看到流程',
          component: 'RadioGroup',
          componentProps: {
            options: [
              { label: '是', value: '1' },
              { label: '否', value: '0' },
            ],
          },
          colProps: {
            span: 24,
          },
          ifShow: false,
        },
        {
          field: 'divUnit',
          label: '单位',
          component: 'ApiTreeSelect',
          defaultValue: '',
          colProps: {
            span: 24,
          },
          componentProps: {
            api: getDepartment,
            // showSearch: true,
            multiple: true,
            showCheckedStrategy: TreeSelect.SHOW_ALL,
            maxTagCount: 10,
            // resultField: 'list',
            fieldNames: {
              label: 'name',
              key: 'code',
              value: 'code',
            },
            treeNodeFilterProp: 'name',
            getPopupContainer: () => document.body,
          },
          ifShow: false,
        },
        {
          field: 'userRealNames',
          label: '选择人员',
          component: 'Input',
          componentProps: {
            disabled: true,
          },
          slot: 'userRealNames',
          colProps: {
            span: 24,
          },
        },
      ];
      const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
        labelWidth: 100,
        schemas: FormSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 23,
        },
      });

      const [registerSelectUserModal, { openModal }] = useModal();
      let oldData: Recordable[] = [];
      const handleSuccess = (data) => {
        oldData = data || [];
        if (data && data.length > 0) {
          dataRef['userNames'] = data.map((item) => item.name).join(',');
          dataRef['userRealNames'] = data.map((item) => item.realName).join(',');
          setFieldsValue({
            userRealNames: dataRef['userRealNames'],
          });
        }
      };
      const handleSelectUser = () => {
        console.log('userNames', dataRef['userNames']);
        // openModal(true, {});
        openModal(true, { users: oldData });
      };

      const getTitle = '选择抄送人员';

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });
          const formData = Object.assign({}, dataRef, values);
          formData.units =
            formData.divUnit && formData.divUnit.length ? formData.divUnit.join(',') : '';
          formData.users = dataRef['userNames'];

          console.log('formData', formData);
          let data = await getDistribute(formData);
          if (data.res == 0) {
            createMessage.success(t('common.opSuccess'));
          }

          if (formData.units == '' && formData.users == '') {
            createMessage.success('请选择单位或用户');
            return;
          }

          closeModal();
          emit('success');
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
