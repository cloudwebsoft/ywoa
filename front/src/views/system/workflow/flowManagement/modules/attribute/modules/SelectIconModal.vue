<template>
  <BasicModal v-bind="$attrs" title="选择图标" @ok="handleOk" @register="registerModal">
    <div class="flex flex-wrap">
      <div
        v-for="(item, index) in icons"
        :key="index"
        class="m-2 cursor-pointer"
        @click="handleItemIcon(item)"
      >
        <SvgIcon size="30" :name="item" v-if="item" />
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { SvgIcon } from '/@/components/Icon';
  import icons from '/@/assets/icons/icons.data.ts';
  export default defineComponent({
    components: { BasicModal, SvgIcon },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const [registerModal, { setModalProps, closeModal }] = useModalInner(() => {
        setModalProps({ confirmLoading: false, width: '60%', footer: false });
      });

      async function handleOk(icon) {
        emit('success', icon);
        closeModal();
      }
      const handleItemIcon = (icon) => {
        handleOk(icon);
      };
      return {
        registerModal,
        handleOk,
        icons,
        handleItemIcon,
      };
    },
  });
</script>
