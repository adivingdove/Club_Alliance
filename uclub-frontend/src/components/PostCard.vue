<template>
  <el-card class="post-card">
    <div class="post-card-body">
      <!-- 左侧：回复数 -->
      <div class="reply-count-box">
        <div class="reply-number">{{ post.commentCount }}</div>
        <div class="reply-text">回复</div>
      </div>

      <!-- 中间：内容 -->
      <div class="post-content-box">
      <div class="post-title" @click="goToDetail">
  {{ post.title }}
</div>
        <div class="post-content-preview">{{ getTextSummary(post.content) }}</div>

        <div class="post-meta">
          <el-tag type="success" size="small" class="club-tag"> {{ post.clubName || '未知社团' }}</el-tag>
          <span class="author">发布用户ID: {{ post.userId }}</span>
          <span class="time">{{ formatTime(post.createdAt) }}</span>
        </div>
      </div>

      <!-- 右侧：操作 -->
      <div class="post-actions">
   <el-button link
  size="small"

  @click="$emit('like', post.id)"
  class="like-button"
>
  <img src="@/assets/icons/thumb_up.svg" alt="点赞" class="icon-thumb" />
  {{ post.likeCount }}
</el-button>
        <el-button size="small" type="info" @click="goToDetail">查看</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { addBrowsingHistory } from '../utils/history'

const router = useRouter()

function goToDetail() {
  // 记录浏览历史
  addBrowsingHistory({
    id: props.post.id,
    title: props.post.title,
    content: props.post.content,
    author: `用户${props.post.userId}`,
    createdAt: props.post.createdAt
  })
  
  router.push(`/post/${props.post.id}`)  
}
const props = defineProps({
  post: Object
})

function formatTime(dateStr) {
  return new Date(dateStr).toLocaleString()
}

const getTextSummary = (markdown) => {
  if (!markdown) return ''
  return markdown
    .replace(/!\[.*?\]\(.*?\)/g, '')  // 去掉图片语法
    .replace(/\[.*?\]\(.*?\)/g, '')   // 去掉链接
    .replace(/[#>*`]/g, '')           // 去掉特殊符号
    .replace(/\n/g, ' ')              // 替换换行
    .trim()
    .slice(0, 100) + '...'            // 截断并加省略号
}

</script>

<style scoped>
.post-card {
  margin-bottom: 16px;
}

.post-card-body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.reply-count-box {
  width: 60px;
  height: 60px;
  border: 1px solid #ccc;
  text-align: center;
  font-weight: bold;
  border-radius: 4px;
  padding-top: 6px;
  color: #409EFF;
  background-color: #f4f6f9;
}

.reply-number {
  font-size: 20px;
}

.reply-text {
  font-size: 14px;
  color: #666;
}

.post-content-box {
  flex: 1;
}

.post-title {
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.post-content-preview {
  font-size: 14px;
  color: #444;
  margin-bottom: 8px;
}

.post-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 12px;
}

.club-tag {
  margin-right: 8px;
}

.post-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}
.like-button {
  padding: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.icon-thumb {
  width: 18px;
  height: 18px;
}
.post-title {
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  color: #409EFF;
}
.post-title:hover {
  text-decoration: underline;
}

.post-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: box-shadow 0.3s ease;
}
.post-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

</style>
