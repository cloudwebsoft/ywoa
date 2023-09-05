<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref, onMounted, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getOacalenderModifyDays, getOacalenderModifyDay } from '/@/api/workOffice/workOffice';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { FormSchema } from '/@/components/Table';
  export default defineComponent({
    name: 'DeptModal',
    components: {
      BasicModal,
      BasicForm,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const type = ref(1); //type为1时批量修改日期类型；type为2时修改工作时间（仅针对所选时间段内的工作日进行修改） 3是修改某天
      const dataRef = ref<Recordable>({});
      const formSchema: FormSchema[] = [
        {
          field: 'modifyBeginDate',
          label: '选择开始时间',
          component: 'DatePicker',
          colProps: { span: 12 },
          required: true,
          componentProps: {
            placeholder: '请选择',
            style: 'width:100%',
            format: 'YYYY-MM-DD',
            valueFormat: 'YYYY-MM-DD',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => !values.oa_date,
        },
        {
          field: 'modifyEndDate',
          label: '选择结束时间',
          component: 'DatePicker',
          colProps: { span: 12 },
          required: true,
          componentProps: {
            placeholder: '请选择',
            style: 'width:100%',
            format: 'YYYY-MM-DD',
            valueFormat: 'YYYY-MM-DD',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => !values.oa_date,
        },
        {
          field: 'oa_date',
          label: '当前选择时间',
          component: 'Input',
          colProps: { span: 24 },
          componentProps: {
            readonly: true,
            style: 'width:100%',
            placeholder: '请选择',
          },
          ifShow: ({ values }) => !!values.oa_date,
        },
        {
          field: 'date_type',
          label: '选择日期类型',
          component: 'Select',
          colProps: { span: 24 },
          required: true,
          componentProps: {
            options: [
              { value: '0', label: '工作日' },
              { value: '2', label: '休息日' },
            ],
          },
        },
        {
          field: 'work_time_begin_a',
          label: '上午上班时间',
          component: 'TimePicker',
          colProps: { span: 12 },
          componentProps: {
            style: 'width:100%',
            format: 'HH:mm',
            valueFormat: 'HH:mm',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.date_type == 0,
        },
        {
          field: 'work_time_end_a',
          label: '上午下班时间',
          component: 'TimePicker',
          colProps: { span: 12 },
          componentProps: {
            style: 'width:100%',
            format: 'HH:mm',
            valueFormat: 'HH:mm',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.date_type == 0,
        },
        {
          field: 'work_time_begin_b',
          label: '下午上班时间',
          component: 'TimePicker',
          colProps: { span: 12 },
          componentProps: {
            style: 'width:100%',
            format: 'HH:mm',
            valueFormat: 'HH:mm',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.date_type == 0,
        },
        {
          field: 'work_time_end_b',
          label: '下午下班时间',
          component: 'TimePicker',
          colProps: { span: 12 },
          componentProps: {
            style: 'width:100%',
            format: 'HH:mm',
            valueFormat: 'HH:mm',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.date_type == 0,
        },
        {
          field: 'work_time_begin_c',
          label: '晚上上班时间',
          component: 'TimePicker',
          colProps: { span: 12 },
          componentProps: {
            style: 'width:100%',
            format: 'HH:mm',
            valueFormat: 'HH:mm',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.date_type == 0,
        },
        {
          field: 'work_time_end_c',
          label: '晚上下班时间',
          component: 'TimePicker',
          colProps: { span: 12 },
          componentProps: {
            style: 'width:100%',
            format: 'HH:mm',
            valueFormat: 'HH:mm',
            getPopupContainer: () => document.body,
          },
          ifShow: ({ values }) => values.date_type == 0,
        },
        {
          field: 'containRestDays',
          label: '含周六/周日',
          component: 'Select',
          colProps: { span: 24 },
          componentProps: {
            mode: 'multiple',
            options: [
              { value: 'containSat', label: '含周六' },
              { value: 'containSun', label: '含周日' },
            ],
          },
          ifShow: ({ values }) => !values.oa_date,
        },
        // {
        //   field: 'containRestDays',
        //   label: '含周六',
        //   component: 'Checkbox',
        //   colProps: { span: 12 },
        //   ifShow: ({ values }) => values.date_type == 0,
        // },
        // {
        //   field: 'contains',
        //   label: '含周日',
        //   component: 'Checkbox',
        //   colProps: { span: 12 },
        //   ifShow: ({ values }) => values.date_type == 0,
        // },
      ];
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '60%' });
        resetFields();
        type.value = data.type;
        dataRef.value = data.record || {};
        dataRef.value.date_type =
          data.record.dateType != null && data.record.dateType != undefined
            ? data.record.dateType + ''
            : '';
        delete dataRef.value.dateType;
        if (type.value === 2) {
          dataRef.value.date_type = '0';
          dataRef.value.type = type.value;
          setFieldsValue({ ...dataRef.value });
          updateSchema([
            {
              field: 'date_type',
              // field: 'date_type',
              // label: '选择日期类型',
              // component: 'Select',
              // colProps: { span: 24 },
              // required: true,
              componentProps: {
                disabled: true,
              },
            },
          ]);
        } else if (type.value == 1) {
          dataRef.value.type = type.value;
          setFieldsValue({ ...dataRef.value });
          updateSchema([
            {
              field: 'date_type',
              componentProps: {
                disabled: false,
              },
            },
          ]);
        } else if (type.value == 3) {
          //编辑单天
          setFieldsValue({ ...dataRef.value });
          updateSchema([
            {
              field: 'date_type',
              componentProps: {
                disabled: false,
              },
            },
          ]);
        }
        setSchemaShow(type.value);
      });
      function setSchemaShow(value: number) {
        switch (value) {
          case 1:
            // updateSchema([
            //   {
            //     field: 'time1',
            //     ifShow: false,
            //   },
            //   {
            //     field: 'time2',
            //     ifShow: false,
            //   },
            //   {
            //     field: 'time3',
            //     ifShow: false,
            //   },
            //   {
            //     field: 'time4',
            //     ifShow: false,
            //   },
            //   {
            //     field: 'time5',
            //     ifShow: false,
            //   },
            //   {
            //     field: 'time6',
            //     ifShow: false,
            //   },
            // ]);
            break;
        }
      }
      const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
        labelWidth: 120,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const getTitle = '设置时间';

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });

          const values = await validate();
          const formData = Object.assign({}, dataRef.value, values);
          // formData.containRestDays = formData.containRestDays?'containSat':''
          // formData.containRestDays = formData.containRestDays?'containSat':''
          if (type.value === 3) {
            //编辑单天
            await getOacalenderModifyDay(formData);
          } else {
            await getOacalenderModifyDays(formData);
          }
          closeModal();
          emit('success');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return {
        registerModal,
        getTitle,
        handleSubmit,
        registerForm,
      };
    },
  });
</script>
