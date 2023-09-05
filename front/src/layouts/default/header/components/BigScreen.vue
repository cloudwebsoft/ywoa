<template>
  <Tooltip :title="getTitle" placement="bottom" :mouseEnterDelay="0.5">
    <span @click="toggle">
      <FundProjectionScreenOutlined />
    </span>
  </Tooltip>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Tooltip } from 'ant-design-vue';
  import { FundProjectionScreenOutlined } from '@ant-design/icons-vue';
  import { useUserStore } from '/@/store/modules/user';
  export default defineComponent({
    name: 'BigScreen',
    components: { FundProjectionScreenOutlined, Tooltip },

    setup() {
      const getTitle = '大屏';
      const userStore = useUserStore();
      const toggle = () => {
        userStore.setBigFullScreen(true);
      };
      return {
        getTitle,
        toggle,
      };
    },
  });
</script>
