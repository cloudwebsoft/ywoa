<template>
  <BasicModal
    :footer="null"
    title="手写签批"
    v-bind="$attrs"
    :class="prefixCls"
    :width="630"
    @register="register"
  >
    <div class="h-260px flex flex-col">
      <div :id="`wp_${fieldName}`" class="container">
        <div
          style="background-size: cover"
          class="js-signature"
          data-line-color="#01018b"
          data-background="transparent url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAlgAAADICAYAAAA0n5+2AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTM4IDc5LjE1OTgyNCwgMjAxNi8wOS8xNC0wMTowOTowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTcgKFdpbmRvd3MpIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjNGQjExNjZFNUMzRDExRThBMEVBODlEOEI3QUZEQTA3IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjNGQjExNjZGNUMzRDExRThBMEVBODlEOEI3QUZEQTA3Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6M0ZCMTE2NkM1QzNEMTFFOEEwRUE4OUQ4QjdBRkRBMDciIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6M0ZCMTE2NkQ1QzNEMTFFOEEwRUE4OUQ4QjdBRkRBMDciLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4uosxwAAAJuUlEQVR42uza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)"
        ></div>
      </div>
      <div :class="`${prefixCls}__footer mt-2`">
        <a-button type="primary" size="middle" @click="handleClear"> 清除 </a-button>
        <a-button type="primary" size="middle" class="ml-2" @click="handleOk"> 确定 </a-button>
        <a-button type="primary" size="middle" class="ml-2" @click="handleCancel"> 取消 </a-button>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { BasicModal, useModalInner } from '/@/components/Modal/index';
  export default defineComponent({
    name: 'WritePadModal',
    components: { BasicModal },
    emits: ['register', 'success'],
    setup(_, { emit }) {
      const { t } = useI18n();
      const { prefixCls } = useDesign('header-lock-modal');

      const width = ref(600);
      const height = ref(200);
      const w = ref(200);
      const h = ref(100);
      const fieldName = ref('');
      const [register, { closeModal }] = useModalInner(async (data) => {
        if (data) {
          if (data.width) {
            width.value = data.width;
          }
          if (data.w) {
            w.value = data.w;
          }
          fieldName.value = data.fieldName;
        }

        setTimeout(() => {
          if ($('#wp_' + fieldName.value + ' .js-signature').length) {
            var _width = width.value;
            var _height = height.value;
            var rate = 0;
            if (screen.width < _width) {
              rate = _width / screen.width;
              _width = _width / rate - 15;
              _height = _height / rate;
            }
            $('#wp_' + fieldName.value + ' .js-signature').jqSignature({
              autoFit: false,
              color: '#01018b',
              // background-color: '#fff',
              lineWidth: 3,
              width: _width,
              height: _height,
            });
          }
        }, 100);
      });

      async function handleOk() {
        var dataUrl = $('#wp_' + fieldName.value + ' .js-signature').jqSignature('getDataURL');
        var $img = $('<img>').attr('src', dataUrl);
        $img.css({ width: w.value + 'px', height: h.value + 'px' });

        console.log('handleOk dataUrl', dataUrl);
        setInputObjValue(dataUrl);
        $(findObj('pad_' + fieldName.value)).html($img.prop('outerHTML'));

        $('#wp_' + fieldName.value + ' .js-signature').jqSignature('clearCanvas');
        closeModal();
        emit('success', {});
      }

      async function handleCancel() {
        $('#wp_' + fieldName.value + ' .js-signature').jqSignature('clearCanvas');
        closeModal();
      }

      async function handleClear() {
        $('#wp_' + fieldName.value + ' .js-signature').jqSignature('clearCanvas');
      }

      return {
        t,
        prefixCls,
        register,
        handleOk,
        handleCancel,
        width,
        height,
        handleClear,
        fieldName,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-lock-modal';

  .@{prefix-cls} {
    &__entry {
      position: relative;
      //height: 240px;
      padding: 130px 30px 30px;
      border-radius: 10px;
    }

    &__header {
      position: absolute;
      top: 0;
      left: calc(50% - 45px);
      width: auto;
      text-align: center;

      &-img {
        width: 70px;
        border-radius: 50%;
      }

      &-name {
        margin-top: 5px;
      }
    }

    &__footer {
      text-align: center;
    }
  }
</style>
