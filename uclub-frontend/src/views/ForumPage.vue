<template>
  <div class="forum-container">
    <!-- 标题 + 发帖按钮 -->
    <div class="forum-header">
      <h2>交流论坛 <el-icon><Edit /></el-icon></h2>
      <el-button type="primary" @click="goToPostCreate">发布帖子</el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="标题">
          <el-input v-model="filter.title" placeholder="请输入关键词" />
        </el-form-item>
        <el-form-item label="社团名称">
          <el-input v-model="filter.clubName" placeholder="请输入社团名" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-select v-model="filter.timeRange" placeholder="请选择">
            <el-option label="今日" value="today" />
            <el-option label="最近7天" value="7days" />
            <el-option label="最近30天" value="30days" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPosts">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20">
      <!-- 帖子列表 -->
      <el-col :span="18">
        <div class="post-list-title">
          <span>帖子</span>
          <el-icon><EditPen /></el-icon>
        </div>

        <PostCard
          v-for="post in posts"
          :key="post.id"
          :post="post"
          @like="handleLike"
        />

        <!-- 分页 -->
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
         :current-page="page"
          @current-change="loadPosts"
          class="pagination"
        />
      </el-col>

      <!-- 侧边栏 -->
      <el-col :span="6">
        <ForumSidebar />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, EditPen } from '@element-plus/icons-vue'

import PostCard from '../components/PostCard.vue'
import ForumSidebar from '../components/ForumSidebar.vue'
import { fetchPostList } from '../api/forum'

const router = useRouter()
const posts = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10

const filter = ref({
  title: '',
  clubName: '',
  timeRange: '',
})
const handlePageChange = (val) => {
  page.value = val
  loadPosts()
}
const loadPosts = async () => {
  const res = await fetchPostList({
    ...filter.value,
    page: page.value,
    pageSize,
  })
  posts.value = res.data.posts
  total.value = res.data.total
}

const resetFilter = () => {
  filter.value = {
    title: '',
    clubName: '',
    timeRange: '',
  }
  page.value = 1
  loadPosts()
}

const handleLike = (postId) => {
  console.log('点赞帖子', postId)
}

const goToPostCreate = () => {
  router.push('/post/create')
}

onMounted(loadPosts)
</script>

<style scoped>
.forum-container {
  padding: 20px;
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.post-list-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
