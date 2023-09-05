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
      <template #postsExcluded="{ model, field }">
        <Select
          v-model:value="model[field]"
          :options="deptCodeAllList"
          :fieldNames="{ label: 'name', value: 'id' }"
          optionFilterProp="name"
          mode="multiple"
          :maxTagCount="3"
          showSearch
          placeholder="请选择职位"
        />
      </template>
    </BasicForm>
  </BasicDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, reactive } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './postion.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { getPostCreate, getPostUpdate, getPostEditPage, getPostList } from '/@/api/system/system';
  import { Select } from 'ant-design-vue';

  export default defineComponent({
    name: 'PositionDrawer',
    components: { BasicDrawer, BasicForm, Select },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const isUpdate = ref(true);
      let dataRef = reactive({});
      const deptCodeAllList = ref<Recordable>([]);
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        labelWidth: 90,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        resetFields();
        setDrawerProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        console.log('PositionDrawer data', data);
        dataRef = data.record;
        deptCodeAllList.value = [];
        getPostDeptCodeAllList({ deptCode: data.record.deptCode });
        if (unref(isUpdate)) {
          await getPostEditPage({ id: dataRef.id }).then((res) => {
            let params = res.post;
            params.postsExcluded =
              Array.isArray(res.aryExcluded) && res.aryExcluded.length > 0
                ? res.aryExcluded.map((item) => item.value)
                : [];
            setFieldsValue({
              ...params,
            });
          });
        } else {
          setFieldsValue({ deptCode: dataRef.deptCode });
        }
      });

      //获取当前部门下的所有岗位
      async function getPostDeptCodeAllList(record: Recordable) {
        const params: any = {
          pageSize: 99999,
          ...record,
        };
        if (unref(isUpdate)) {
          // 如果编辑，则取全部的岗位
          params.deptCode = 'root';
        }
        const res = await getPostList(params);
        deptCodeAllList.value = res.list || [];

        // 如果是编辑
        if (unref(isUpdate)) {
          for (let index in deptCodeAllList.value) {
            // 去除正在编辑的职位
            if (deptCodeAllList.value[index].id == dataRef.id) {
              console.log('index', index, 'dataRef.id', dataRef.id);
              deptCodeAllList.value.splice(index, 1);
            }
          }
        }
      }
      const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

      async function handleSubmit() {
        try {
          const values = await validate();
          setDrawerProps({ confirmLoading: true });
          // TODO custom api
          let formData = Object.assign({}, dataRef, values);
          if (!unref(isUpdate)) {
            await getPostCreate(formData);
          } else {
            let params = {
              deptCode: formData.deptCode,
              orders: formData.orders,
              id: formData.id,
              name: formData.name,
              description: formData.description,
              numLimited: formData.numLimited,
              postsExcluded:
                Array.isArray(formData.postsExcluded) && formData.postsExcluded.length > 0
                  ? formData.postsExcluded.join(',')
                  : '',
              status: formData.status ? 1 : '',
              limited: formData.limited,
              exclusive: formData.excluded,
            };
            await getPostUpdate(params);
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
        deptCodeAllList,
      };
    },
  });
</script>
