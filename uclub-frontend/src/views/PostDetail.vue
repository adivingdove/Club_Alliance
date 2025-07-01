<template>
  <div class="post-detail-container">
    <el-card class="post-detail-card">
      <!-- 帖子标题 -->
      <h2 class="post-title">{{ post.title }}</h2>

      <!-- 元信息 + 删除按钮 -->
    <div class="post-meta">
  <el-avatar
    :src="getUserAvatar(post.user?.avatarUrl)"
    :size="40"
    style="margin-right: 10px"
  />
  <span class="post-author-nickname">{{ post.user?.nickname || '匿名用户' }}</span>
  <el-tag type="success" size="small">社团ID: {{ post.clubId }}</el-tag>
  <span>作者ID: {{ post.userId }}</span>
  <span>发表于: {{ formatTime(post.createdAt) }}</span>


  <!-- 删除按钮 -->
<el-tooltip
  v-if="post.userId === currentUserId"
  content="删除帖子"
  placement="top"
>
  <el-button
    :icon="Delete"
    circle
    type="default"
    size="small"
    @click="deletePost"
    style="color: #888; border-color: #ccc; margin-left: auto;"
  />
</el-tooltip>


  <!-- 举报按钮 -->
  <el-tooltip content="举报帖子" placement="top">
    <el-button
      circle
      type="default"
      size="small"
      @click="() => openReportDialog('帖子', post.id)"

      style="color: #888; border-color: #ccc; margin-left: 8px;"
    >
      <el-icon><WarnTriangleFilled  /></el-icon>
    </el-button>
  </el-tooltip>
</div>

      <!-- 正文 Markdown -->
      <div class="post-content" v-if="post.content">
        <vue3-markdown-it :source="post.content" />
      </div>
      <div class="post-content" v-else>
        正文加载中...
      </div>

      <!-- 点赞与评论数 -->
      <div class="post-stats">
        <el-button
          size="small"
          type="primary"
          @click="likePost"
          class="like-button"
          :plain="!liked"
        >
          <img :src="thumbIcon" alt="点赞" class="icon-thumb" />
          {{ post.likeCount }}
        </el-button>
        <el-tag class="ml-10">评论数: {{ post.commentCount }}</el-tag>
      </div>

      <!-- 评论区域 -->
      <div class="post-comments">
        <h3>💬 评论</h3>
       
<!-- Emoji 面板容器 -->
<div class="emoji-picker-wrapper" ref="emojiWrapper">
 <el-button
  circle
  size="small"
  @click="showEmoji = !showEmoji"
  style="margin-bottom: 6px; font-size: 18px;"
>😊</el-button>

  <emoji-picker
    v-show="showEmoji"
    @emoji-click="onEmojiClick"
  ></emoji-picker>
</div>

       <!-- 评论输入框区域 -->
<div ref="textareaWrapper">
  <el-input
    v-model="newComment"
    type="textarea"
    placeholder="写下你的评论..."
    :rows="3"
    resize="none"
  />
</div>

        <el-button
          type="primary"
          size="small"
          style="margin-top: 10px"
          @click="submitComment"
        >
          发表评论
        </el-button>

        <!-- 评论列表 -->
        <div v-if="comments.length" class="comment-list">
          <div v-for="(comment, index) in comments" :key="index" class="comment-item">
  <!-- 用户头像与昵称 -->
  <div class="comment-user-info">
    <el-avatar
      :src="getUserAvatar(comment.user?.avatarUrl)"
      :size="40"
      style="margin-right: 10px"
    />
    <span class="comment-nickname">{{ comment.user?.nickname || '匿名用户' }}</span>
    <span class="comment-time">发表于：{{ formatTime(comment.createdAt) }}</span>
      <!-- 删除/举报按钮 -->
  <div class="comment-actions">
   <el-tooltip
  v-if="comment.user?.id === currentUserId || comment.userId === currentUserId"
  content="删除评论"
  placement="top"
>

      <el-button
        :icon="Delete"
        circle
        type="default"
        size="small"
        @click="deleteComment(comment.id)"
        class="delete-icon-btn"
      />
    </el-tooltip>

    <el-tooltip content="举报评论" placement="top">
      <el-button
        circle
        type="default"
        size="small"
        @click="() => openReportDialog('评论', comment.id)"
        style="color: #888; border-color: #ccc; margin-left: 8px;"
      >
        <el-icon><WarnTriangleFilled /></el-icon>
      </el-button>
    </el-tooltip>
  </div>
  </div>



  <!-- 评论内容 -->
  <p class="comment-content">{{ comment.content }}</p>

  <!-- 点赞按钮 -->
  <div class="comment-like-bar">
    <el-button
      size="small"
      class="like-comment-button"
      @click="toggleCommentLike(comment)"
      :plain="!comment.liked"
      text
    >
      <img :src="thumbIcon" alt="点赞" class="icon-thumb" />
      {{ comment.likeCount }}
    </el-button>
  </div>
</div>

        </div>
        <p v-else class="no-comment">暂无评论，快来抢沙发！</p>
      </div>
    </el-card>
  </div>
</template>


<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'

import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import Vue3MarkdownIt from 'vue3-markdown-it'
import thumbIcon from '@/assets/icons/thumb_up.svg'
import { Delete } from '@element-plus/icons-vue'
import{ WarnTriangleFilled }from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { addBrowsingHistory } from '../utils/history'
import 'emoji-picker-element'


const showEmoji = ref(false)
const textareaWrapper = ref(null)
function onEmojiClick(event) {
  const emoji = event.detail.unicode
  insertEmoji(emoji)
  showEmoji.value = false
}

const store = useStore()
const userId = computed(() => store.getters.currentUser?.id || null)

const reportReason = ref('')
const route = useRoute()
const router = useRouter()
const postId = route.params.id
const post = ref({
  title: '',
  content: '',
  club_id: null,
  user_id: null,
  created_at: '',
  like_count: 0,
  comment_count: 0
})
const newComment = ref('')
const comments = ref([])

// 模拟当前登录用户ID（应从登录信息中获取）
const currentUserId = userId.value || 1001 // 默认用户ID为1001

// 时间格式化
function formatTime(str) {
  if (!str) return '无时间'
  try {
    return new Date(str.replace('T', ' ')).toLocaleString()
  } catch (e) {
    return '无效时间'
  }
}

// 加载帖子详情
async function loadPost() {
  try {
    const res = await request.get(`/api/posts/${postId}`, {
      params: { userId: currentUserId }
    });
    post.value = res.data.post
    liked.value = res.data.liked ?? false 
    console.log('帖子详情返回:', res.data)

    // 记录浏览历史
    addBrowsingHistory({
      id: post.value.id,
      title: post.value.title,
      content: post.value.content,
      author: `用户${post.value.userId}`,
      createdAt: post.value.createdAt
    })

  } catch (err) {
    console.error('加载帖子失败', err)
  }
}

// 删除帖子
async function deletePost() {
  try {
    await ElMessageBox.confirm('确认删除此帖子？此操作不可撤销', '提示', {
      type: 'warning',
    })

    await request.delete(`/api/posts/${postId}`, {
      params: { userId: currentUserId },
    })
    ElMessage.success('删除成功')
    router.push('/')
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败', err)
      ElMessage.error('删除失败')
    }
  }
}

// 加载评论
async function loadComments() {
  try {
    const res = await request.get(`/api/posts/${postId}/comments`)
    console.log(' 获取评论数据:', res.data)
    comments.value = res.data
  } catch (err) {
    console.error('加载评论失败', err)
  }
}

// 提交评论
async function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  const url = `/api/posts/${postId}/comments`
  const payload = {
    userId: currentUserId,
    content: newComment.value.trim(),
  }
  try {
    await request.post(url, payload)
    ElMessage.success('评论成功')
    newComment.value = ''
    await loadComments()
    await loadPost()
  } catch (err) {
    console.error('评论失败 AxiosError:')
    console.error(' 请求地址:', url)
    console.error('请求参数:', payload)
    ElMessage.error('评论失败，请查看控制台详细信息')
  }
}

async function deleteComment(commentId) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning'
    })
    const url = `/api/posts/${postId}/comments/${commentId}`
    await request.delete(url, {
      params: {
        userId: currentUserId
      }
    })
    ElMessage.success('删除成功')
    await loadComments()
    await loadPost()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除评论失败', err)
      ElMessage.error('删除失败')
    }
  }
}

const liked = ref(false)
const likeLoading = ref(false)

async function likePost() {
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    const url = `/api/posts/${postId}/like?userId=${currentUserId}`
    const res = await request.post(url)

    ElMessage.success(res.data.message)
    liked.value = res.data.liked
    await loadPost()
  } catch (err) {
    console.error('点赞失败', err)
    ElMessage.error('点赞失败')
  } finally {
    likeLoading.value = false
  }
}

async function toggleCommentLike(comment) {
  try {
    const url = `/api/posts/${postId}/comments/${comment.id}/like?userId=${currentUserId}`
    const res = await request.post(url)
    comment.liked = res.data.liked
    comment.likeCount = Number(res.data.likeCount || 0)
    ElMessage.success(res.data.message)
  } catch (err) {
    console.error('评论点赞失败', err)
    ElMessage.error('点赞失败')
  }
}

async function openReportDialog(targetType, targetId) {
  reportReason.value = ''
  try {
    await ElMessageBox.prompt('请输入举报理由（不少于5个字）', `举报${targetType}`, {
      confirmButtonText: '提交举报',
      cancelButtonText: '取消',
      inputPattern: /^.{5,}$/, 
      inputErrorMessage: '理由不得少于5个字',
      inputValue: '',
    }).then(async ({ value }) => {
      reportReason.value = value

      await request.post('/api/report', {
        reporterId: currentUserId,
        targetType,
        targetId,
        reason: reportReason.value,
      })
      ElMessage.success('举报成功，感谢你的反馈')
    })
  } catch (err) {
    if (err !== 'cancel') {
      console.error('举报失败', err)
      ElMessage.error('举报失败：服务器错误')
    }
  }
}

function getUserAvatar(url) {
  if (!url) {
    return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png' // 默认头像
  }
  if (url.startsWith('http')) {
    return url
  }
  if (url.startsWith('/')) {
    return `http://localhost:8080${url}`
  }
  return `http://localhost:8080/uploads/avatars/${url}`
}

function insertEmoji(emoji) {
  // 使用 wrapper 查询 textarea
  const textarea = textareaWrapper.value?.querySelector('textarea')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = newComment.value.slice(0, start)
  const after = newComment.value.slice(end)
  newComment.value = before + emoji + after

  nextTick(() => {
    textarea.selectionStart = textarea.selectionEnd = start + emoji.length
    textarea.focus()
  })
}

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
})

const emojiWrapper = ref(null)

function handleOutsideClick(event) {
  if (emojiWrapper.value && !emojiWrapper.value.contains(event.target)) {
    showEmoji.value = false
  }
}



// 初始化加载
onMounted(() => {
  loadPost()
  loadComments()
    document.addEventListener('click', handleOutsideClick)
})
</script>

<style scoped>
/* 容器整体样式 */
.post-detail-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

/* 帖子卡片风格 */
.post-detail-card {
  padding: 24px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  background-color: #ffffff;
}

/* 帖子标题样式 */
.post-title {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 16px;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

/* 元信息栏 */
.post-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 20px;
}

.post-author-nickname {
  font-weight: 600;
  margin-right: 12px;
  font-size: 14px;
}

/* 正文 Markdown 样式 */
.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
  background-color: #f9f9f9;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 30px;
  word-break: break-word;
}
.post-content h1,
.post-content h2,
.post-content h3 {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: bold;
}
.post-content code {
  background: #f0f0f0;
  padding: 2px 4px;
  border-radius: 4px;
  font-family: Consolas, Monaco, monospace;
}

/* 点赞统计 */
.post-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
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
  transition: color 0.2s;
}
.like-button:hover {
  background-color: transparent;
  color: #66b1ff;
}

/* 评论区域 */
.post-comments {
  margin-top: 40px;
}

.comment-list {
  margin-top: 20px;
  transition: all 0.3s ease-in-out;
}

.comment-item {
  position: relative;
  padding: 14px;
  margin-bottom: 16px;
  border-radius: 8px;
  background-color: #fafafa;
  transition: box-shadow 0.2s;
}
.comment-item:hover {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.comment-user-info {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.comment-nickname {
  font-weight: 600;
  margin-right: 12px;
}

.comment-time {
  font-size: 13px;
  color: #999;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.comment-content {
  font-size: 15px;
  margin-top: 4px;
}

.comment-like-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.like-comment-button {
  font-size: 13px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  transition: color 0.2s;
}
.like-comment-button:hover {
  color: #66b1ff;
}

/* 评论输入框 */
.el-textarea__inner {
  font-size: 14px;
  border-radius: 6px;
}


/* 删除按钮 */
.delete-icon-btn {
  margin-left: 10px;
  vertical-align: middle;
}

/* 无评论提示 */
.no-comment {
  color: #ccc;
  margin-top: 10px;
}
.el-button:not(.el-button--primary):not(.el-button--success):not(.el-button--danger):not(.el-button--warning):not(.is-plain) {
  border-color: #dcdfe6;
  color: #909399;
}
.el-button:hover {
  border-color: #409eff;
  color: #409eff;
}

.emoji-picker-wrapper button {
  transition: all 0.2s ease;
}
.emoji-picker-wrapper button:hover {
  background-color: #f0f0f0;
}

.emoji-picker-wrapper {
  position: relative;
}

emoji-picker {
  position: absolute;
  z-index: 999;
  top: 40px;
  left: 0;
  width: 320px;
  max-height: 360px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
}


</style>

