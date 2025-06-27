<template>
  <el-card class="post-card" @click="goToDetail">
    <div class="post-header">
      <span class="post-title">{{ post.title }}</span>
      <el-button type="text" @click.stop="$emit('like', post.id)">
        <el-icon><i class="el-icon-thumb"></i></el-icon>
        {{ post.like_count }}
      </el-button>
    </div>
    <div class="post-meta">
      <el-tag type="success" size="small">社团ID: {{ post.club_id }}</el-tag>
      <span class="meta-sep">|</span>
      <span>作者ID：{{ post.user_id }}</span>
      <span class="meta-sep">|</span>
      <span>评论数：{{ post.comment_count }}</span>
      <span class="meta-sep">|</span>
      <span>时间：{{ new Date(post.created_at).toLocaleString() }}</span>
    </div>
    <div class="post-content" v-if="post.content">
      <p>{{ post.content.substring(0, 100) }}{{ post.content.length > 100 ? '...' : '' }}</p>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  post: Object
})

const goToDetail = () => {
  router.push(`/post/${props.post.id}`)
}
</script>

<style scoped>
.post-card {
  margin: 20px 0;
  cursor: pointer;
  transition: all 0.3s;
}
.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}
.post-meta {
  margin-top: 10px;
  color: #999;
  font-size: 13px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
}
.meta-sep {
  margin: 0 5px;
}
.post-content {
  margin-top: 10px;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}
</style>
