<template>
  <Card
    :loading="loading"
    :bordered="bordered"
    :headStyle="{
      borderBottom: cardData.cardHeadBorderBottomColor
        ? `1px solid ${cardData.cardHeadBorderBottomColor}`
        : 'none',
      color: cardData.titleColor,
      fontSize: cardData.titleSize,
      textAlign: cardData.titleAlign,
      background: cardData.titleBackgroundImge ? `url(${cardData.titleBackgroundImge})` : 'none',
      backgroundRepeat: cardData.titleBackgroundImge ? 'no-repeat' : 'none',
      backgroundSize: cardData.titleBackgroundImge && cardData.imgeCenter != 1 ? 'cover' : 'none',
      backgroundPosition: cardData.imgeCenter == 1 ? 'center' : 'none',
    }"
  >
    <template #title>
      <div class="cursor-pointer font-bold" :style="{ textAlign: cardData?.titleAlign }">
        <slot name="title"></slot>
      </div>
    </template>
    <slot></slot>
  </Card>
</template>
<script lang="ts" setup>
  // import { } from 'vue';
  import { Card } from 'ant-design-vue';

  const props = defineProps({
    loading: Boolean,
    bordered: Boolean,
    cardData: {
      type: Object as PropType<object>,
      default: () => {
        return {};
      },
    },
  });
</script>
