<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
    :width="630"
  >
    <Row v-for="(item, index) in fieldsList" :key="index">
      <Col :span="24" class="col-item">
        <template
          v-if="item['type'] == FormField.TYPE_DATE || item['type'] == FormField.TYPE_DATE_TIME"
        >
          <Checkbox v-model:checked="item['checked']">{{ item['title'] }}</Checkbox>
          <Select :default-value="'0'" style="width: 200px" v-model:value="item['value']">
            <SelectOption value="0">时间段</SelectOption>
            <SelectOption value="1">时间点</SelectOption>
          </Select>
        </template>
        <template v-else-if="item['type'] == FormField.TYPE_MACRO">
          <Checkbox v-model:checked="item['checked']">{{ item['title'] }}</Checkbox>
          <Select :default-value="'0'" style="width: 200px" v-model:value="item['value']">
            <SelectOption value="1">等于</SelectOption>
            <SelectOption value="0">包含</SelectOption>
          </Select>
        </template>
        <template
          v-else-if="
            item['type'] ==
            (FormField.FIELD_TYPE_INT ||
              FormField.FIELD_TYPE_DOUBLE ||
              FormField.FIELD_TYPE_FLOAT ||
              FormField.FIELD_TYPE_LONG ||
              FormField.FIELD_TYPE_PRICE)
          "
        >
          <Checkbox v-model:checked="item['checked']">{{ item['title'] }}</Checkbox>
          <Select :default-value="'='" style="width: 200px" v-model:value="item['value']">
            <SelectOption value="=">等于</SelectOption>
            <SelectOption value="&gt;">大于</SelectOption>
            <SelectOption value="&lt;">小于</SelectOption>
            <SelectOption value="&gt;=">大于等于</SelectOption>
            <SelectOption value="&lt;=">小于等于</SelectOption>
          </Select>
        </template>
        <template v-else>
          <Checkbox v-model:checked="item['checked']">{{ item['title'] }}</Checkbox>
          <Select :default-value="'1'" style="width: 200px" v-model:value="item['value']">
            <SelectOption value="1">等于</SelectOption>
            <SelectOption
              value="0"
              v-if="item['type'] == (FormField.TYPE_TEXTFIELD || FormField.TYPE_TEXTAREA)"
              >包含</SelectOption
            >
          </Select>
        </template>
      </Col>
    </Row>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getFields, getSetConds } from '/@/api/process/process';
  import { Row, Col, Checkbox, Select } from 'ant-design-vue';
  import { FormField } from '/@/enums/formField';
  export default defineComponent({
    name: 'SelectField',
    components: {
      BasicModal,
      Row,
      Col,
      Checkbox,
      Select,
      SelectOption: Select.Option,
    },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const {} = useMessage();
      const typeCode = ref('');
      let fieldsList = ref([]);
      let hadSelect = ref([]);
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, width: '50%' });
        typeCode.value = data.typeCode;
        hadSelect.value = data.forms;
        console.log('SelectField hadSelect', hadSelect.value);
        initData();
      });

      const getTitle = '选择条件';

      function initData() {
        let fieldNames = hadSelect.value.map((item: any) => item.fieldName);
        getFields({ typeCode: typeCode.value }).then((res) => {
          console.log('res==>', res);
          fieldsList.value = res;
          fieldsList.value.forEach((item: any) => {
            item.checked = fieldNames.includes(item.name);

            // 判断是否已经在所选的数组中
            let isFound = false;
            hadSelect.value.forEach((it: any) => {
              if (it.fieldName == item.name) {
                isFound = true;
                item.value = it.condType;
                console.log('isFound', isFound, 'fieldName', item.name, 'value', item.value);
                return;
              }
            });

            if (!isFound) {
              if (item['type'] == FormField.TYPE_DATE || item['type'] == FormField.TYPE_DATE_TIME) {
                console.log('item', item);
                item.value = '0';
              } else if (item['type'] == FormField.TYPE_MACRO) {
                item.value = '0';
              } else if (
                item['type'] ==
                (FormField.FIELD_TYPE_INT ||
                  FormField.FIELD_TYPE_DOUBLE ||
                  FormField.FIELD_TYPE_FLOAT ||
                  FormField.FIELD_TYPE_LONG ||
                  FormField.FIELD_TYPE_PRICE)
              ) {
                item.value = '=';
              } else {
                item.value = '1';
              }
            }
          });
        });
      }

      async function handleSubmit() {
        try {
          setModalProps({ confirmLoading: true });
          let formData = new FormData();
          formData.append('typeCode', typeCode.value);
          const values = fieldsList.value.filter((item) => item['checked'] == true);
          console.log('values', values);
          if (values.length > 0) {
            values.forEach((el) => {
              let temp = el;
              console.log('temp', unref(temp));
              formData.append('queryFields', unref(temp)['name']);
              formData.append(unref(temp)['name'] + '_cond', unref(temp)['value']);
            });
          }
          // let jsonForm = {};
          // formData.forEach((value, key) => (jsonForm[key] = value));
          // jsonForm['typeCode'] = typeCode.value;
          // let params = {
          //   typeCode: typeCode.value,
          //   queryFields: jsonForm,
          // };
          await getSetConds(formData);

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
        fieldsList,
        FormField,
      };
    },
  });
</script>
<style lang="less" scoped>
  .ant-row {
    width: 70%;
    margin: 0 auto;
  }
  .col-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
  }
</style>
