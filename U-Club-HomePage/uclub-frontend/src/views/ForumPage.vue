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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, EditPen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import PostCard from '../components/PostCard.vue'
import ForumSidebar from '../components/ForumSidebar.vue'
import { fetchPostList, likePost } from '../api/forum'

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
  try {
    // 动态生成时间筛选条件
    const filterParams = {
      ...filter.value,
      page: page.value,
      pageSize
    }

    // 时间范围处理
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

    const res = await fetchPostList(filterParams)
    
    // 处理后端返回的数据结构
    if (res.data) {
      posts.value = res.data.posts || []
      total.value = res.data.total || 0
    } else {
      // 如果后端返回的是直接的数组格式
      if (Array.isArray(res)) {
        posts.value = res
        total.value = res.length
      } else {
        posts.value = []
        total.value = 0
      }
    }
    
    console.log('加载的帖子数据:', posts.value)
  } catch (error) {
    console.error('加载帖子列表失败:', error)
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

const handleLike = async (postId) => {
  try {
    await likePost(postId)
    // 更新本地帖子数据
    const post = posts.value.find(p => p.id === postId)
    if (post) {
      post.like_count = (post.like_count || 0) + 1
    }
    ElMessage.success('点赞成功')
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败')
  }
}

const goToPostCreate = () => {
  router.push('/post/create')
}
const handleFilterSearch = () => {
  page.value = 1 // 每次筛选回到第一页
  loadPosts()
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
