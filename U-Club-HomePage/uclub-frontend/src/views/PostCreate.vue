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
      placeholder="请选择您加入的社团"
      style="width: 300px; margin-bottom: 20px"
    >
      <el-option v-for="club in clubs" :key="club.id" :label="club.name" :value="club.id" />
    </el-select>

    <!-- 如果没有加入任何社团的提示 -->
    <div v-if="clubs.length === 0" style="margin-bottom: 20px; color: #f56c6c;">
      <el-alert
        title="您还没有加入任何社团"
        description="请先加入社团后再发布帖子"
        type="warning"
        show-icon
        :closable="false"
      />
    </div>

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
  action="/api/upload"
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
import { createPost, getUserClubs } from '@/api/forum'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({
  club_id: '',
  title: '',
  content: '',
  image_urls: [],
  user_id: null, // 将从localStorage获取
})

const clubs = ref([])

// 获取当前登录用户信息
const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      return user
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return null
    }
  }
  return null
}

// 检查用户是否已登录
const checkLoginStatus = () => {
  const user = getCurrentUser()
  if (!user || !user.id) {
    ElMessage.error('请先登录后再发布帖子')
    router.push('/') // 跳转到首页
    return false
  }
  
  // 设置用户ID
  form.value.user_id = user.id
  return true
}

const loadClubs = async () => {
  try {
    // 获取当前用户信息
    const user = getCurrentUser()
    if (!user || !user.id) {
      ElMessage.error('用户信息获取失败')
      return
    }

    console.log('当前用户信息:', user)
    console.log('用户ID类型:', typeof user.id, '值:', user.id)

    // 从后端获取用户加入的社团列表
    const response = await getUserClubs(user.id)
    console.log('API响应:', response)
    
    // 处理后端返回的Result格式
    if (response.data && (response.data.code === 200 || response.data.code === 0) && response.data.data) {
      clubs.value = response.data.data
    } else if (response.data && Array.isArray(response.data)) {
      // 如果直接返回数组格式
      clubs.value = response.data
    } else {
      // 如果后端接口不可用，使用模拟数据（用户加入的社团）
      clubs.value = [
        { id: 1, name: '摄影社' },
        { id: 2, name: '编程协会' },
      ]
    }
    
    console.log('用户加入的社团:', clubs.value)
  } catch (error) {
    console.error('加载用户社团列表失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    // 如果后端接口不可用，使用默认数据
    clubs.value = [
      { id: 1, name: '摄影社' },
      { id: 2, name: '编程协会' },
    ]
  }
}

onMounted(() => {
  if (checkLoginStatus()) {
    loadClubs()
    // 确保用户ID已设置
    const user = getCurrentUser()
    if (user && user.id) {
      form.value.user_id = user.id
    }
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
  form.value.image_urls = form.value.image_urls.filter((img) => img !== url)
}

const submitPost = async () => {
  // 再次检查用户登录状态
  if (!checkLoginStatus()) {
    return
  }

  // 检查是否加入了社团
  if (clubs.length === 0) {
    ElMessage.error('您还没有加入任何社团，无法发布帖子')
    return
  }

  // 表单验证
  if (!form.value.title.trim()) {
    ElMessage.error('请输入帖子标题')
    return
  }

  if (!form.value.content.trim()) {
    ElMessage.error('请输入帖子内容')
    return
  }

  if (!form.value.club_id) {
    ElMessage.error('请选择社团')
    return
  }

  const payload = {
    ...form.value,
    image_urls: form.value.image_urls,
    user_id: form.value.user_id, // 确保带上用户ID
    club_id: form.value.club_id, // 确保带上社团ID
  }

  try {
    const res = await createPost(payload)
    const result = res.data

    if (result && (result.post_id || result.id || (result.data && (result.data.post_id || result.data.id)))) {
      ElMessage.success('发布成功')
      const newPostId = result.post_id || result.id || (result.data && (result.data.post_id || result.data.id))
      router.push(`/post/${newPostId}`)

      // 重置表单
      form.value = {
        club_id: '',
        title: '',
        content: '',
        image_urls: [],
        user_id: getCurrentUser()?.id || null
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
