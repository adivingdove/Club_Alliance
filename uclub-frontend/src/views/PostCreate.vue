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
      v-model="form.clubId"
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
  :headers="uploadHeaders"
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
import { ElMessage } from 'element-plus'
import Vue3MarkdownIt from 'vue3-markdown-it'
import { createPost } from '@/api/forum'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { computed } from 'vue'

const store = useStore()
const userId = computed(() => store.getters.currentUser?.id || null)

const router = useRouter()

const form = ref({
  clubId: '', 
  title: '',
  content: '',
  imageUrlList: [],
  userId: userId.value || 1001 // 默认用户ID为1001
})

const clubs = ref([])

const loadClubs = () => {
  clubs.value = [
    { id: 1, name: '摄影社' },
    { id: 2, name: '编程协会' },
  ]
}

onMounted(loadClubs)

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  }
})

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
  form.value.imageUrlList = form.value.imageUrlList.filter((img) => img !== url)
}

const submitPost = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('请先登录后再发帖')
    return
  }
  const payload = {
    ...form.value,
    imageUrlList: form.value.imageUrlList,
  }

  console.log('[提交帖子] 请求体:', payload)

  try {
    const res = await createPost(payload)
    console.log('[提交帖子] 成功响应:', res)

    // 后端直接返回 { code: 200, message: "发布成功", data: { post_id: xxx } }
    if (res.data && res.data.code === 200) {
      const postId = res.data.data?.post_id
      if (postId) {
        ElMessage.success('发布成功')
        // 跳转回论坛页面
        router.push('/forum')

        // 重置表单
        form.value = {
          clubId: '',
          title: '',
          content: '',
          imageUrlList: [], // 修改字段名以匹配后端
          userId: userId.value || 1001 // 默认用户ID为1001
        }
      } else {
        console.error('[提交帖子] 响应中缺少post_id:', res.data)
        ElMessage.error('发布失败：响应数据异常')
      }
    } else {
      console.error('[提交帖子] 响应状态异常:', res.data)
      ElMessage.error(res.data?.message || '发布失败')
    }

  } catch (err) {
    console.error('[提交帖子] 请求失败:', err)
    console.error('[提交帖子] err.message:', err.message)
    console.error('[提交帖子] err.response:', err.response)
    console.error('[提交帖子] err.response?.data:', err.response?.data)

    ElMessage.error(err?.response?.data?.message || '发布失败，请检查后端 /api/posts 接口')
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
  form.value.imageUrlList.push(url)
  ElMessage.success('图片上传成功')
}

const handleUploadError = (err, file) => {
  console.error('[上传失败]', err, file)
  ElMessage.error('图片上传失败，请检查后端是否启用 /api/forum/upload 接口')
}

</script>

<style scoped>
.post-create {
  padding: 30px;
}
</style>
