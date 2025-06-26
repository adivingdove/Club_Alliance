<template>
  <el-card class="post-card">
    <div class="post-header">
      <span class="post-title">{{ post.title }}</span>
      <el-button type="text" @click="$emit('like', post.id)">
        <el-icon><i class="el-icon-thumb"></i></el-icon>
        {{ post.like_count }}
      </el-button>
    </div>
    <div class="post-meta">
      <span>作者：{{ post.user_name || '匿名用户' }}</span>
      <span class="meta-sep">|</span>
      <span>评论数：{{ post.comment_count }}</span>
      <span class="meta-sep">|</span>
      <span>时间：{{ new Date(post.created_at).toLocaleString() }}</span>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  post: Object
})
</script>

<style scoped>
.post-card {
  margin: 20px 0;
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
}
.meta-sep {
  margin: 0 5px;
}
</style>
