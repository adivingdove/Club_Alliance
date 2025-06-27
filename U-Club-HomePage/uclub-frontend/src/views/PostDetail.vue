<template>
  <div class="post-detail-container">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button @click="goBack" type="text" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回论坛
      </el-button>
    </div>

    <el-card class="post-detail-card">
      <!-- 帖子标题 -->
      <h2 class="post-title">{{ post.title }}</h2>

<!-- 元信息 + 删除按钮 -->
<div class="post-meta">
  <el-tag type="success" size="small">社团ID: {{ post.club_id }}</el-tag>
  <span>作者ID: {{ post.user_id }}</span>
  <span>发表于: {{ formatTime(post.created_at) }}</span>

  <!-- 删除按钮：仅当前用户是作者才显示 -->
  <el-button
    v-if="post.user_id === currentUserId"
    type="danger"
    size="small"
    plain
    style="margin-left: auto"
    @click="handleDeletePost"
  >
    删除
  </el-button>
    </div>

      <!-- 正文 Markdown，含图片 -->
      <div class="post-content">
        <vue3-markdown-it :source="post.content || ''" />
      </div>

      <!-- 点赞与评论数 -->
      <div class="post-stats">
        <el-button size="small" type="primary" @click="handleLike" class="like-button">
          <img :src="thumbIcon" alt="点赞" class="icon-thumb" />
          {{ post.like_count }}
        </el-button>
        <el-tag class="ml-10">评论数: {{ post.comment_count }}</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import Vue3MarkdownIt from 'vue3-markdown-it'
import thumbIcon from '@/assets/icons/thumb_up.svg'
import { getPostDetail, likePost, deletePost } from '@/api/forum'

const route = useRoute()
const router = useRouter()
const postId = route.params.id
const post = ref({})

// 当前登录用户ID（从localStorage获取）
let currentUserId = null
const userStr = localStorage.getItem('user')
if (userStr) {
  try {
    const user = JSON.parse(userStr)
    currentUserId = user.id
  } catch (e) {
    currentUserId = null
  }
}

// 返回上一页
function goBack() {
  router.go(-1)
}

// 时间格式化
function formatTime(str) {
  return new Date(str).toLocaleString()
}

// 加载帖子详情
async function loadPost() {
  try {
    const res = await getPostDetail(postId)
    post.value = res.data
  } catch (err) {
    console.error('加载帖子失败', err)
    ElMessage.error('加载帖子失败')
  }
}

// 删除帖子逻辑
async function handleDeletePost() {
  try {
    await ElMessageBox.confirm('确认删除此帖子？此操作不可撤销', '提示', {
      type: 'warning',
    })

    await deletePost(postId, currentUserId)
    ElMessage.success('删除成功')
    router.push('/forum') // 跳转回论坛页面
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败', err)
      ElMessage.error('删除失败')
    }
  }
}

// 点赞逻辑
async function handleLike() {
  try {
    await likePost(postId)
    post.value.like_count = (post.value.like_count || 0) + 1
    ElMessage.success('点赞成功')
  } catch (err) {
    console.error('点赞失败', err)
    ElMessage.error('点赞失败')
  }
}

onMounted(loadPost)
</script>


<style scoped>
.post-detail-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.post-detail-card {
  padding: 24px;
}

.post-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.post-meta {
  font-size: 14px;
  color: #888;
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.post-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 30px;
  word-break: break-word;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.ml-10 {
  margin-left: 10px;
}

.icon-thumb {
  width: 16px;
  height: 16px;
  vertical-align: middle;
  margin-right: 6px;
}

.like-button {
  border: none;
  background-color: transparent;
  box-shadow: none;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 4px;
}
.like-button:hover {
  background-color: transparent;
  color: #66b1ff;
}
</style>
