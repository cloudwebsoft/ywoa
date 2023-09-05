<template>
  <div class="flex justify-center w-full">
    <Spin :spinning="spinning">
      <Card style="width: 100%">
        <template #title> 流程类型：{{ flowData['flowTypeName'] }} </template>
        <template #extra>
          <Button type="primary" size="small" @click="handleSuccess">
            <template #icon><SaveOutlined /></template>
            保存
          </Button>
        </template>
        <Row>
          <Col :span="24">
            <FormItem label="流程等级">
              <RadioGroup name="radioGroup" v-model:value="flowData['level']">
                <Radio :value="0">普通</Radio>
                <Radio :value="1">重要</Radio>
                <Radio :value="2">紧急</Radio>
                <Radio :value="-1">其他</Radio>
              </RadioGroup>
            </FormItem>
          </Col>
          <Col :span="24">
            <FormItem label="流程名称">
              <Input v-model:value="flowData['flowTitle']" placeholder="请输入流程名称" />
            </FormItem>
          </Col>
        </Row>
      </Card>
    </Spin>
  </div>
</template>
<script lang="ts">
  import { defineComponent, onMounted, ref, unref, watchEffect } from 'vue';
  import { Card, Spin, Button, Form, Radio, Row, Col, Input } from 'ant-design-vue';
  import { SaveOutlined } from '@ant-design/icons-vue';
  import { getFlowModifyTitle, getModifyTitle } from '/@/api/process/process';
  import { useMessage } from '/@/hooks/web/useMessage';
  export default defineComponent({
    components: {
      Card,
      Spin,
      Button,
      FormItem: Form.Item,
      RadioGroup: Radio.Group,
      SaveOutlined,
      Row,
      Col,
      Radio,
      Input,
    },
    props: {
      flowId: {
        type: [Number],
        required: false,
      },
      activeKey: {
        type: [String],
        required: false,
      },
    },
    emits: ['success'],
    setup(props, { emit }) {
      const flowData = ref<any>({});
      const { createMessage } = useMessage();
      const spinning = ref(false);
      onMounted(() => {});
      function fetch() {
        getFlowModifyTitle({ flowId: props.flowId }).then((res) => {
          flowData.value = res;
          console.log('flowData.value', flowData.value);
        });
      }

      watchEffect(() => {
        props.activeKey == '3' && props.flowId && fetch();
      });
      async function handleSuccess() {
        console.log('formData', flowData.value);
        if (!unref(flowData).flowTitle) {
          createMessage.warning('请输入流程名称');
          return;
        }
        let params = {
          flowId: props.flowId,
          typeCode: unref(flowData).flowTypeCode,
          title: unref(flowData).flowTitle,
          level: unref(flowData).level,
        };
        spinning.value = true;
        await getModifyTitle(params).then((res) => {
          createMessage.success(res.msg);
        });
        spinning.value = false;
        emit('success');
      }
      return {
        handleSuccess,
        flowData,
        spinning,
      };
    },
  });
</script>
