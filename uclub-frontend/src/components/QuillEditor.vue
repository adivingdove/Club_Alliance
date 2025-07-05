<template>
  <div>
    <div ref="editorRef"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import Quill from 'quill'
import ImageUploader from 'quill-image-uploader'
import 'quill/dist/quill.snow.css'

Quill.register('modules/imageUploader', ImageUploader)

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入内容...' },
  height: { type: Number, default: 300 }
})

const emit = defineEmits(['update:modelValue'])
const editorRef = ref(null)
let quill = null

onMounted(() => {
  nextTick(() => {
    if (editorRef.value) {
      quill = new Quill(editorRef.value, {
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
          ],
          imageUploader: {
            upload: file => {
              const formData = new FormData()
              formData.append('file', file)
              return fetch(import.meta.env.VITE_API_BASE_URL + '/api/upload', {
                method: 'POST',
                body: formData,
                headers: {
                  'Authorization': 'Bearer ' + localStorage.getItem('token')
                }
              })
                .then(res => res.json())
                .then(result => {
                  if (result.code === 0) {
                    return import.meta.env.VITE_API_BASE_URL + result.url
                  } else {
                    throw new Error('图片上传失败')
                  }
                })
            }
          }
        }
      })
      if (props.modelValue) {
        quill.root.innerHTML = props.modelValue
      }
      quill.on('text-change', () => {
        const html = quill.root.innerHTML
        emit('update:modelValue', html)
      })
    }
  })
})

onBeforeUnmount(() => {
  if (quill) quill = null
})

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