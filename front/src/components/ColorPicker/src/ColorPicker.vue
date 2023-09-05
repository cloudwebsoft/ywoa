<template>
  <div>
    <!-- <a-button @click="handleShow">{{ selectColor }}</a-button> -->
    <!-- <teleport :disabled="true"> -->
    <ColorPicker
      v-model:pureColor="selectColor"
      :shape="shape"
      :useType="useType"
      :format="format"
      :isWidget="isWidget"
      :disableAlpha="disableAlpha"
      :zIndex="zIndex"
      :lang="lang"
      :disableHistory="disableHistory"
      :roundHistory="roundHistory"
      @pure-color-change="handlePureColorChange"
      ref="colorPickerRef"
    />
    <!-- </teleport> -->
  </div>
</template>

<script lang="ts">
  import { defineComponent, watchEffect, watch, ref } from 'vue';
  import { ColorPicker } from 'vue3-colorpicker';
  import 'vue3-colorpicker/style.css';

  import { propTypes } from '/@/utils/propTypes';
  export default defineComponent({
    name: 'ColorPickerSelect',
    components: { ColorPicker },
    props: {
      value: propTypes.string,
      shape: {
        type: String as PropType<string>,
        default: 'square',
      },
      useType: {
        type: String as PropType<string>,
        default: 'pure',
      },
      format: {
        type: String as PropType<string>,
        default: 'rgb',
      },
      //show full
      isWidget: {
        type: Boolean as PropType<boolean>,
        default: false,
      },
      disableAlpha: {
        type: Boolean as PropType<boolean>,
        default: false,
      },
      zIndex: {
        type: Number as PropType<number>,
        default: 100000,
      },
      lang: {
        type: String as PropType<string>,
        default: 'ZH-cn',
      },
      disableHistory: {
        type: Boolean as PropType<boolean>,
        default: false,
      },
      roundHistory: {
        type: Boolean as PropType<boolean>,
        default: false,
      },
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const colorPickerRef = ref<HTMLElement | null>(null);
      const selectColor = ref('#000000');
      watchEffect(() => {
        console.log('color=>', props.value);
        selectColor.value = props.value || selectColor.value;
      });

      watch(
        () => selectColor.value,
        (v) => {
          console.log('color2=>', v);
          emit('update:value', v);
          return emit('change', v);
        },
      );
      const handlePureColorChange = (e) => {
        console.log('e', e);
      };
      return {
        selectColor,
        colorPickerRef,
        handlePureColorChange,
      };
    },
  });
</script>
