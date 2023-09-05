<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="500px"
    :destroyOnClose="true"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm">
      <template #before="{ model, field }">
        <FormItemRest>
          <div class="flex items-center">
            <!-- <Switch v-model:checked="model[field]" checkedChildren="是" unCheckedChildren="否" /> -->
            <Select
              style="width: 100px; margin: 0 10px"
              v-model:value="model[field]"
              defaultValue="1"
              :options="timeRemind"
            />
            之前
          </div>
        </FormItemRest>
      </template>
    </BasicForm>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { Select, Form } from 'ant-design-vue';
  import { formSchema } from './allSchedule.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';

  import { getPlanCreate, getPlanUpdate, getPlan } from '/@/api/workOffice/workOffice';

  export default defineComponent({
    name: 'AllScheduleDrawer',
    components: {
      BasicDrawer,
      BasicForm,
      Select,
      FormItemRest: Form.ItemRest,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive<Recordable>({});
      const timeRemind = [
        {
          label: '请选择',
          value: 0,
        },
        {
          label: '十分钟',
          value: 10,
        },
        {
          label: '二十分钟',
          value: 20,
        },
        {
          label: '三十分钟',
          value: 30,
        },
        {
          label: '四十五分钟',
          value: 45,
        },
        {
          label: '一小时',
          value: 60,
        },
        {
          label: '二小时',
          value: 120,
        },
        {
          label: '三小时',
          value: 180,
        },
        {
          label: '四小时',
          value: 240,
        },
        {
          label: '五小时',
          value: 300,
        },
        {
          label: '六小时',
          value: 360,
        },
        {
          label: '十二小时',
          value: 720,
        },
      ];
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
          updateSchema([
            {
              field: 'closed',
              ifShow: true,
            },
          ]);
          getPlan({ id: data.record.id }).then((res) => {
            dataRef = res;
            setFieldsValue({
              ...dataRef,
            });
          });
        } else {
          dataRef['before'] = 10;
          setFieldsValue({
            ...dataRef,
          });
        }
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      async function handleSubmit() {
        try {
          const values = await validate();
          let formDate = Object.assign({}, dataRef, values);
          formDate.shared = formDate.shared ? '1' : '0';
          formDate.isClosed = formDate.closed ? '1' : '0';

          setDrawerProps({ confirmLoading: true });
          if (unref(isUpdate)) {
            await getPlanUpdate(formDate);
          } else {
            await getPlanCreate(formDate);
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
        timeRemind,
        dataRef,
      };
    },
  });
</script>
