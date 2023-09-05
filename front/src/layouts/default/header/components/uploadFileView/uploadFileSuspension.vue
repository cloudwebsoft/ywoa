<template>
  <div class="upload-file-suspension">
    <UploadFile type="tab" />
  </div>
</template>
<script>
  import { defineComponent } from 'vue';
  import UploadFile from './index.vue';
  export default defineComponent({
    components: { UploadFile },
    setup() {},
  });
</script>
<style scoped>
  .upload-file-suspension {
    position: fixed;
    top: 40%;
    right: 10px;
    z-index: 999999999;
    color: #000;
  }
</style>
