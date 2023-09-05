<template>
  <div class="pl-2 pr-2">
    <div class="flex justify-center">
      <a-button type="primary">保存</a-button>
      <a-button type="primary" class="ml-2">增加</a-button>
    </div>
    <div>
      <Card
        v-for="item in 2"
        :key="item"
        style="margin-bottom: 5px"
        :bodyStyle="{ padding: '12px' }"
      >
        <div class="flex items-center">
          <div class="w-1/24">如果</div>
          <!-- <Divider type="vertical" /> -->
          <div class="w-10/24 flex">
            <div>
              <Select :options="[]" class="w-30" />
            </div>
            <div class="ml-1 mr-1">
              <Select :options="[]" class="w-20" />
            </div>
            <div>
              <Select :options="[]" class="w-30" />
            </div>
          </div>
          <div class="w-1/24">则</div>
          <di class="w-10/24">
            <div class="flex items-center mb-1" v-for="el in 3" :key="`363${el}`">
              <div>
                <Select :options="[]" class="w-30" />
              </div>
              <div class="ml-1 mr-1">
                <Select :options="[]" class="w-20" />
              </div>
              <div class="cursor-pointer">
                <PlusOutlined v-if="el === 1" />
                <MinusOutlined v-if="el !== 1" />
              </div>
            </div>
          </di>
          <div class="w-2/24">
            <CloseOutlined class="cursor-pointer" />
            <ArrowUpOutlined class="ml-2 mr-2 cursor-pointer" />
            <ArrowDownOutlined class="cursor-pointer" />
          </div>
        </div>
      </Card>
    </div>
    <div> 注：日期型字段格式，例：2020-10-08，2020-10-09 15:30:10，current表示当前日期 </div>
  </div>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { Divider, Card, Select } from 'ant-design-vue';
  import {
    PlusOutlined,
    MinusOutlined,
    ArrowUpOutlined,
    ArrowDownOutlined,
    CloseOutlined,
  } from '@ant-design/icons-vue';
  const title = ref('显示规则');
</script>
