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
          <el-button type="primary" @click="handleFilterSearch">查询</el-button>

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

       <transition-group name="fade" tag="div">
  <PostCard
    v-for="post in posts"
    :key="post.id"
    :post="post"
    @like="handleLike"
  />
</transition-group>


        <!-- 分页 -->
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
         :current-page="page"
         @current-change="handlePageChange"
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
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Edit, EditPen } from '@element-plus/icons-vue'

import PostCard from '../components/PostCard.vue'
import ForumSidebar from '../components/ForumSidebar.vue'
import { fetchPostList } from '../api/forum'

const router = useRouter()
const route = useRoute()
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
  const filterParams = {
    ...filter.value,
    page: page.value,
    pageSize
  }

  if (filter.value.timeRange) {
    const now = new Date()
    let startTime = null

    if (filter.value.timeRange === 'today') {
      startTime = new Date(now.setHours(0, 0, 0, 0)).toISOString()
    } else if (filter.value.timeRange === '7days') {
      startTime = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString()
    } else if (filter.value.timeRange === '30days') {
      startTime = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000).toISOString()
    }

    filterParams.startTime = startTime
  }

  console.log('[加载帖子] 请求参数:', filterParams)

  try {
    const res = await fetchPostList(filterParams)
    console.log('[加载帖子] 响应数据:', res)

    posts.value = res.posts
    total.value = res.total

  } catch (err) {
    console.error('[加载帖子] 请求失败:', err)
    posts.value = []
    total.value = 0
  }
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
const handleFilterSearch = () => {
  page.value = 1 // 每次筛选回到第一页
  loadPosts()
}

onMounted(loadPosts)

// 添加路由监听，当从发帖页面返回时自动刷新帖子列表
watch(route, (newRoute, oldRoute) => {
  // 只有当从发帖页面返回时才刷新
  if (oldRoute?.path === '/post/create' && newRoute.path === '/forum') {
    console.log('[ForumPage] 从发帖页面返回，刷新帖子列表')
    loadPosts()
  }
})
</script>

<style scoped>
/* 主容器 */
.forum-container {
  padding: 24px;
  background-color: #f5f7fa;
  border-radius: 10px;
  min-height: 100vh;
}

/* 标题 + 发布按钮区域 */
.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forum-header h2 {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.forum-header el-icon {
  margin-left: 8px;
}

/* 筛选卡片样式 */
.filter-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 筛选表单 */
.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  padding: 10px;
}

.filter-form .el-form-item {
  margin-bottom: 0;
}

.filter-form .el-input,
.filter-form .el-select {
  width: 200px;
}

/* 帖子列表标题 */
.post-list-title {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 16px;
}

.post-list-title el-icon {
  margin-left: 6px;
}

/* 分页样式 */
.pagination {
  margin-top: 30px;
  text-align: center;
}

/* 侧边栏卡片样式（需配合 ForumSidebar.vue） */
.el-col > .el-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 动画过渡（若启用 transition-group） */
.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 按钮组间隔 */
.el-button + .el-button {
  margin-left: 10px;
}
</style>
