<template>
  <div>
    <div ref="editorRef"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  height: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
let quill = null

onMounted(() => {
  nextTick(() => {
    if (window.Quill && editorRef.value) {
      quill = new window.Quill(editorRef.value, {
        theme: 'snow',
        placeholder: props.placeholder,
        modules: {
          toolbar: [
            [{ 'header': [1, 2, false] }],
            ['bold', 'italic', 'underline', 'strike'],
            [{ 'list': 'ordered'}, { 'list': 'bullet' }],
            [{ 'color': [] }, { 'background': [] }],
            [{ 'align': [] }],
            ['link', 'image'],
            ['clean']
          ]
        }
      })
      
      // 设置初始内容
      if (props.modelValue) {
        quill.root.innerHTML = props.modelValue
      }
      
      // 监听内容变化
      quill.on('text-change', () => {
        const html = quill.root.innerHTML
        emit('update:modelValue', html)
      })
    }
  })
})

onBeforeUnmount(() => {
  if (quill) {
    quill = null
  }
})

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (quill && newVal !== quill.root.innerHTML) {
    quill.root.innerHTML = newVal || ''
  }
})
</script>

<style scoped>
.ql-editor {
  min-height: 200px;
}
</style>