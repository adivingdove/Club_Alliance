<template>
  <div class="post-detail-container">
    <el-card class="post-detail-card">
      <!-- 帖子标题 -->
      <h2 class="post-title">{{ post.title }}</h2>

      <!-- 元信息 + 删除按钮 -->
      <div class="post-meta">
        <el-tag type="success" size="small">社团ID: {{ post.club_id }}</el-tag>
        <span>作者ID: {{ post.user_id }}</span>
        <span>发表于: {{ formatTime(post.created_at) }}</span>

        <el-button
          v-if="post.user_id === currentUserId"
          type="danger"
          size="small"
          plain
          style="margin-left: auto"
          @click="deletePost"
        >
          删除
        </el-button>
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
          {{ post.like_count }}
        </el-button>
        <el-tag class="ml-10">评论数: {{ post.comment_count }}</el-tag>
      </div>

      <!-- 评论区域 -->
      <div class="post-comments">
        <h3>💬 评论</h3>

        <!-- 评论输入框 -->
        <el-input
          v-model="newComment"
          type="textarea"
          placeholder="写下你的评论..."
          :rows="3"
          resize="none"
        />
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
            <p class="comment-meta">
              👤 用户ID: {{ comment.userId }} 发表时间：{{ formatTime(comment.createdAt) }}
              <!-- 删除按钮 -->
              <el-tooltip content="删除评论" placement="top">
                <el-button
                  v-if="comment.userId === currentUserId"
                  :icon="Delete"
                  circle
                  type="default"
                  size="small"
                  @click="deleteComment(comment.id)"
                  class="delete-icon-btn"
                  style="color: #888; border-color: #ccc;"
                />
              </el-tooltip>
            </p>

            <p class="comment-content">{{ comment.content }}</p>

            <!-- 点赞按钮右下角 -->
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
        <p v-else class="no-comment">暂无评论</p>
      </div>
    </el-card>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Vue3MarkdownIt from 'vue3-markdown-it'
import thumbIcon from '@/assets/icons/thumb_up.svg'
import { Delete } from '@element-plus/icons-vue'
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
const currentUserId = 1001

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
    const res = await axios.get(`http://localhost:8080/api/posts/${postId}`, {
      params: { userId: currentUserId }
    });

    post.value = res.data.post
    liked.value = res.data.liked ?? false 
    console.log('帖子详情返回:', res.data)

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

    await axios.delete(`http://localhost:8080/api/posts/${postId}`, {
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
    const res = await axios.get(`http://localhost:8080/api/posts/${postId}/comments`)
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

  const url = `http://localhost:8080/api/posts/${postId}/comments`
  const payload = {
    userId: currentUserId,
    content: newComment.value.trim(),
  }

  try {
    await axios.post(url, payload)
    ElMessage.success('评论成功')
    newComment.value = ''
    await loadComments()
    await loadPost()
  } catch (err) {
    console.error('评论失败 AxiosError:')
    console.error(' 请求地址:', url)
    console.error('请求参数:', payload)

    if (axios.isAxiosError(err)) {
      console.error(' 响应状态码:', err.response?.status)
      console.error(' 响应内容:', err.response?.data)
      console.error(' 请求配置:', err.config)
    } else {
      console.error(' 非 Axios 错误:', err)
    }

    ElMessage.error('评论失败，请查看控制台详细信息')
  }
}



async function deleteComment(commentId) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning'
    })

    const url = `http://localhost:8080/api/posts/${postId}/comments/${commentId}`
    await axios.delete(url, {
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
    const url = `http://localhost:8080/api/posts/${postId}/like?userId=${currentUserId}`
    const res = await axios.post(url)

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
async function likeComment(comment) {
  try {
    const url = `http://localhost:8080/api/posts/comments/${comment.id}/like?userId=${currentUserId}`;
    const res = await axios.post(url);

    ElMessage.success(res.data.message);
    comment.liked = res.data.liked;
    comment.likeCount = (comment.likeCount || 0) + (res.data.liked ? 1 : -1);
  } catch (err) {
    console.error('评论点赞失败', err);
    ElMessage.error('点赞失败');
  }
}
async function toggleCommentLike(comment) {
  try {
    const url = `http://localhost:8080/api/posts/${postId}/comments/${comment.id}/like?userId=${currentUserId}`
    const res = await axios.post(url)
    comment.liked = res.data.liked
    comment.likeCount += res.data.likeCount
    ElMessage.success(res.data.message)
  } catch (err) {
    console.error('评论点赞失败', err)
    ElMessage.error('点赞失败')
  }
}


// 初始化加载
onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<style scoped>
.delete-icon-btn {
  margin-left: 10px;
  vertical-align: middle;
}
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

.post-comments {
  margin-top: 40px;
}

.comment-list {
  margin-top: 20px;
}


.comment-meta {
  font-size: 13px;
  color: #999;
}

.comment-content {
  font-size: 15px;
  margin-top: 4px;
}

.no-comment {
  color: #ccc;
  margin-top: 10px;
}

.comment-item {
  position: relative;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
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
}

.icon-thumb {
  width: 16px;
  height: 16px;
}

</style>
