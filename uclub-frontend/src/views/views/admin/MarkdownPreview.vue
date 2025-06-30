<!-- src/components/MarkdownPreview.vue -->
<template>
  <div v-html="compiledMarkdown" class="markdown-preview-content"></div>
</template>

<script setup>
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'

const props = defineProps({
  source: {
    type: String,
    default: ''
  }
})

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

const compiledMarkdown = computed(() => md.render(props.source || ''))
</script>

<style scoped>
.markdown-preview-content {
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
}

.markdown-preview-content img {
  max-width: 100%;
  height: auto;
  margin-top: 10px;
}
</style>
