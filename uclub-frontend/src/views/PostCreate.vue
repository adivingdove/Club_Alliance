<template>
  <el-card class="post-create">
    <h2>📝 发布新帖</h2>

    <!-- 标题 -->
    <el-input
      v-model="form.title"
      placeholder="请输入标题"
      size="large"
      style="margin: 20px 0"
    />

    <!-- 社团选择 -->
    <el-select
      v-model="form.club_id"
      placeholder="请选择社团"
      style="width: 300px; margin-bottom: 20px"
    >
      <el-option v-for="club in clubs" :key="club.id" :label="club.name" :value="club.id" />
    </el-select>

    <!-- 内容编辑区（原生 textarea）-->
    <textarea
      v-model="form.content"
      class="markdown-textarea"
      rows="10"
      placeholder="请输入 Markdown 格式正文内容"
      style="width: 100%; padding: 10px; border: 1px solid #dcdfe6; border-radius: 4px; font-size: 14px"
    ></textarea>

    <!-- 实时预览 -->
    <el-divider>实时预览</el-divider>
    <vue3-markdown-it :source="form.content" />

    <!-- 图片上传 -->
<el-upload
  action="/api/forum/upload"
  list-type="picture-card"
  :limit="9"
  :on-success="handleUploadSuccess"
  :on-error="handleUploadError"
  :before-upload="handleBeforeUpload"
  :on-remove="handleUploadRemove"
  multiple
>
  <i class="el-icon-plus" />
</el-upload>

    <!-- 提交按钮 -->
    <div style="margin-top: 20px; text-align: right">
      <el-button type="primary" @click="submitPost">发布帖子</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import Vue3MarkdownIt from 'vue3-markdown-it'
import { createPost } from '@/api/forum'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({
  club_id: '',
  title: '',
  content: '',
  image_urls: [],
  user_id: 1001, // 临时测试用户 ID
})

const clubs = ref([])

const loadClubs = () => {
  clubs.value = [
    { id: 1, name: '摄影社' },
    { id: 2, name: '编程协会' },
  ]
}

onMounted(loadClubs)



const insertAtCursor = (text) => {
  const textarea = document.querySelector('.markdown-textarea')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = form.value.content.slice(0, start)
  const after = form.value.content.slice(end)
  form.value.content = before + text + after

  nextTick(() => {
    textarea.selectionStart = textarea.selectionEnd = start + text.length
    textarea.focus()
  })
}

const handleUploadRemove = (file) => {
  const url = file.response?.url || file.url
  form.value.image_urls = form.value.image_urls.filter((img) => img !== url)
}

const submitPost = async () => {
  const payload = {
    ...form.value,
    image_urls: form.value.image_urls,
  }

  try {
    const res = await createPost(payload)
    const result = res.data

if (result && (result.post_id || result.id)) {
  ElMessage.success('发布成功')
  const newPostId = result.post_id || result.id
  router.push(`/post/${newPostId}`)

  // 重置表单
  form.value = {
    club_id: '',
    title: '',
    content: '',
    image_urls: [],
    user_id: 1001
  }
} else {
  ElMessage.error(result.message || '发布失败')
}
  } catch (err) {
    console.error('请求失败：', err)
    if (err.response) {
      ElMessage.error(`服务器错误：${err.response.status}`)
    } else {
      ElMessage.error('请求失败：' + err.message)
    }
  }
}

const handleBeforeUpload = (file) => {
  console.log('[上传准备]', file)
  return true // 如果 return false 会阻止上传
}

const handleUploadSuccess = (res, file) => {
  console.log('[上传成功]', res, file)

  const url = res.url?.startsWith('http') ? res.url : `http://localhost:8080${res.url}`
  const markdownImage = `\n![${file.name}](${url})\n`
  insertAtCursor(markdownImage)
  form.value.image_urls.push(url)
  ElMessage.success('图片上传成功')
}

const handleUploadError = (err, file) => {
  console.error('[上传失败]', err, file)
  ElMessage.error('图片上传失败，请检查后端是否启用 /api/upload 接口')
}

</script>

<style scoped>
.post-create {
  padding: 30px;
}
</style>
