<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="500px"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm">
      <template #realName="{ model, field }">
        <Row>
          <Col :span="20">
            <Input v-model:value="model[field]" readonly />
          </Col>
          <Col :span="4">
            <Button type="primary" @click="selectUser">选择</Button>
          </Col>
        </Row>
      </template>
    </BasicForm>
    <SelectUser @register="registerModal" @success="handleModelSuccess" />
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { Input, Row, Col, Button } from 'ant-design-vue';
  import { formSchema } from './jobNum.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { SelectUser } from '/@/components/CustomComp';
  import { useModal } from '/@/components/Modal';

  import { getCreateAccount, getUpdateAccount } from '/@/api/system/system';

  export default defineComponent({
    name: 'JobNumDrawer',
    components: { BasicDrawer, BasicForm, Input, Row, Col, Button, SelectUser },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive<any>({});
      const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
        labelWidth: 90,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        resetFields();
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        dataRef = {};
        if (unref(isUpdate)) {
          dataRef = {
            ...data.record,
            name: data.record.account,
            realName: data.record.realName,
            userName: data.record.userName,
          };
          setFieldsValue({
            ...dataRef,
          });
        }
        updateSchema([
          {
            field: 'name',
            componentProps: {
              disabled: !!dataRef.name,
            },
          },
        ]);
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      const [registerModal, { openModal }] = useModal();
      function selectUser() {
        openModal(true, {
          type: 'jobNum', //工号入口判断
        });
      }

      function handleModelSuccess(data) {
        //选择用户后的回调
        let params = {
          realName: data[0].realName,
          userName: data[0].name,
        };
        dataRef.realName = params.realName;
        dataRef.userName = params.userName;
        setFieldsValue({
          realName: dataRef.realName,
        });
      }
      async function handleSubmit() {
        try {
          const values = await validate();
          let formDate = Object.assign({}, dataRef, values);
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          console.log(formDate);
          let params = {
            name: formDate.name,
            realName: formDate.realName,
            userName: formDate.userName,
          };
          if (unref(isUpdate)) {
            await getUpdateAccount(params);
          } else {
            await getCreateAccount(params);
          }
          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      return {
        registerDrawer,
        registerForm,
        getTitle,
        handleSubmit,
        registerModal,
        selectUser,
        handleModelSuccess,
      };
    },
  });
</script>
