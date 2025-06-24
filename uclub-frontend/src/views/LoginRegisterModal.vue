<template>
  <div v-if="visible" class="modal-overlay" @click.self="close">
    <div class="modal-window">
      <button class="close-btn" @click="close">&times;</button>
      <div class="modal-title">登录/注册页面</div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  visible: Boolean,
});
const emit = defineEmits(['update:visible']);

function close() {
  emit('update:visible', false);
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-window {
  background: #fff;
  width: 300px;
  padding: 24px 20px;
  border-radius: 10px;
  position: relative;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  text-align: center;
  font-family: sans-serif;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 12px;
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}
</style>
