<template>
  <!-- 顶部渐变波浪装饰 -->
  <svg class="top-wave" viewBox="0 0 1440 120" style="position:absolute;top:0;left:0;width:100vw;height:120px;z-index:0;">
    <path fill="url(#waveGradient)" fill-opacity="1" d="M0,32L80,53.3C160,75,320,117,480,117.3C640,117,800,75,960,64C1120,53,1280,75,1360,85.3L1440,96L1440,0L1360,0C1280,0,1120,0,960,0C800,0,640,0,480,0C320,0,160,0,80,0L0,0Z"></path>
    <defs>
      <linearGradient id="waveGradient" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#a18cd1"/>
        <stop offset="100%" stop-color="#fbc2eb"/>
      </linearGradient>
    </defs>
  </svg>
  <div class="forum-container">
    <!-- 标题 + 发帖按钮 -->
    <div class="forum-header">
      <h2>交流论坛 <el-icon><Edit /></el-icon></h2>
      <div class="forum-actions">
   <el-button type="primary" @click="goToPostCreate">
    发布帖子
  </el-button>
  <el-button type="success" @click="goToChatroom">
    在线聊天
  </el-button>
</div>

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
            <el-option label="最近24小时" value="today" />
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
    v-motion="motionOptions"
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
import { EditPen } from '@element-plus/icons-vue'


import PostCard from '../components/PostCard.vue'
import ForumSidebar from '../components/ForumSidebar.vue'
import { fetchPostList } from '../api/forum'


const motionOptions = {
  initial: { opacity: 0, y: 30, scale: 0.95 },
  enter: { opacity: 1, y: 0, scale: 1, transition: { type: 'spring', bounce: 0.2, duration: 0.6 } },
  leave: { opacity: 0, y: -10, duration: 0.3 },
}

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
  console.log('[加载帖子] Token:', localStorage.getItem('token'))
  console.log('[加载帖子] User:', localStorage.getItem('user'))

  try {
    const res = await fetchPostList(filterParams)
    console.log('[加载帖子] 响应数据:', res)

    posts.value = res.posts
    total.value = res.total

  } catch (err) {
    console.error('[加载帖子] 请求失败:', err)
    console.error('[加载帖子] 请求配置:', err.config)
    console.error('[加载帖子] 响应状态:', err.response?.status)
    console.error('[加载帖子] 响应数据:', err.response?.data)
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
const goToChatroom = () => {
  router.push('/chatroom')
}

const handleFilterSearch = () => {
  page.value = 1 // 每次筛选回到第一页
  loadPosts()
}



onMounted(() => {
  loadPosts()
 
})

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
.top-wave {
  pointer-events: none;
}

.forum-container {
  padding: 48px 7vw 32px 7vw;
  background: #f7f8fa;
  border-radius: 18px;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}
@media (max-width: 1200px) {
  .forum-container {
    padding: 32px 3vw 24px 3vw;
  }
}
@media (max-width: 768px) {
  .forum-container {
    padding: 12px 2vw 8px 2vw;
  }
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.forum-header h2 {
  display: flex;
  align-items: center;
  font-size: 28px;
  font-weight: 800;
  color: #a18cd1;
  letter-spacing: 1px;
}

.forum-header el-icon {
  margin-left: 8px;
  color: #a18cd1;
}

.filter-card {
  margin-bottom: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 18px rgba(161,140,209,0.08);
  background: #fff;
  border: 1.5px solid #f3eaff;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  padding: 10px 0 10px 0;
}

.filter-form .el-form-item {
  margin-bottom: 0;
}

.filter-form .el-input,
.filter-form .el-select {
  width: 200px;
}

.post-list-title {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: 700;
  color: #a18cd1;
  margin-bottom: 18px;
}
.post-list-title el-icon {
  margin-left: 6px;
  color: #a18cd1;
}

.pagination {
  margin-top: 36px;
  text-align: center;
}
.pagination .el-pager li.active {
  background: linear-gradient(90deg, #a18cd1 0%, #fbc2eb 100%);
  color: #fff;
  border-radius: 8px;
}
.pagination .el-pager li {
  color: #a18cd1;
  font-weight: 600;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
}
.pagination .el-pager li:hover {
  background: #f3eaff;
  color: #a18cd1;
}

.el-col > .el-card {
  border-radius: 16px;
  box-shadow: 0 4px 18px rgba(161,140,209,0.08);
  background: #fff;
  border: 1.5px solid #f3eaff;
}

.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.el-button + .el-button {
  margin-left: 10px;
}

.forum-actions {
  display: flex;
  gap: 12px;
}

.el-button[type="primary"], .el-button[type="success"] {
  background: linear-gradient(90deg, #a18cd1 0%, #fbc2eb 100%);
  color: #fff;
  border: none;
  font-weight: bold;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(161,140,209,0.06);
  transition: background 0.2s, color 0.2s;
}
.el-button[type="primary"]:hover, .el-button[type="success"]:hover {
  background: linear-gradient(90deg, #fbc2eb 0%, #a18cd1 100%);
  color: #fff;
}
.el-button:not([type="primary"]):not([type="success"]) {
  background: #fff;
  color: #a18cd1;
  border: 1.5px solid #a18cd1;
  border-radius: 12px;
  font-weight: bold;
  transition: background 0.2s, color 0.2s;
}
.el-button:not([type="primary"]):not([type="success"]):hover {
  background: #a18cd1;
  color: #fff;
}

/* icon主色统一 */
.el-icon, .el-icon svg {
  color: #a18cd1 !important;
  font-size: 20px !important;
}

.filter-form .el-input__wrapper, .filter-form .el-select__wrapper {
  border-radius: 8px;
  border: 1.5px solid #e0c3fc;
  background: #fafbfc;
  box-shadow: none;
}
.filter-form .el-input__inner, .filter-form .el-select__selected {
  color: #444;
}

/* 其它细节优化 */
.filter-form .el-form-item__label {
  color: #a18cd1;
  font-weight: 600;
}

</style>
