<template>
  <div class="post-admin-page">
    <el-card>
      <!-- 搜索栏 -->
      <div class="toolbar">
        <el-input
          v-model="searchTitle"
          placeholder="搜索标题"
          clearable
          @clear="fetchPosts(1)"
          @keyup.enter.native="fetchPosts(1)"
          style="width: 200px; margin-right: 10px;"
        />
        <el-input
          v-model="searchClub"
          placeholder="搜索社团"
          clearable
          @clear="fetchPosts(1)"
          @keyup.enter.native="fetchPosts(1)"
          style="width: 200px; margin-right: 10px;"
        />
        <el-date-picker
          v-model="searchTimeRange"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          align="right"
          unlink-panels
          @change="fetchPosts(1)"
          style="margin-right: 10px;"
        />
        <el-button type="primary" @click="fetchPosts(1)">搜索</el-button>
      </div>

      <!-- 帖子列表表格 -->
      <el-table :data="postList" style="width: 100%" v-loading="loading" :empty-text="emptyText">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="user.nickname" label="作者" width="120">
          <template #default="scope">
            {{ scope.row.user?.nickname || '未知用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="clubName" label="社团" />
        <el-table-column
          label="发布时间"
          width="180"
          :formatter="(row) => formatDate(row.createdAt)"
        />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewPost(scope.row.id)">详情</el-button>
            <el-button size="small" type="danger" @click="confirmDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        style="margin-top: 16px; text-align: right;"
        background
        layout="prev, pager, next, jumper"
        :page-size="pageSize"
        :current-page.sync="currentPage"
        :total="total"
        @current-change="fetchPosts"
      />
    </el-card>

    <!-- 帖子详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="帖子详情" width="60%" append-to-body>
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 帖子内容 -->
        <el-tab-pane label="帖子内容" name="post">
          <div v-if="selectedPost">
            <p><strong>ID：</strong>{{ selectedPost.id }}</p>
            <p><strong>作者：</strong>{{ selectedPost.user?.nickname || '未知用户' }}</p>
            <p><strong>标题：</strong>{{ selectedPost.title }}</p>
            <div class="markdown-content">
              <Markdown :source="selectedPost.content" />
            </div>
            <p><strong>点赞数：</strong>{{ selectedPost.likeCount }}</p>
            <p><strong>评论数：</strong>{{ selectedPost.commentCount }}</p>
            <p><strong>发布时间：</strong>{{ formatDate(selectedPost.createdAt) }}</p>
          </div>
          <div v-else>加载中...</div>
        </el-tab-pane>

        <!-- 评论列表 -->
        <el-tab-pane label="评论列表" name="comments">  
          <div v-if="commentList.length > 0">
            <el-scrollbar height="300px">
              <el-card
                v-for="comment in commentList"
                :key="comment.id"
                class="comment-card"
                style="margin-bottom: 10px;"
                >
                <!-- 用户名 + 时间 -->
                <div class="comment-header">
                  <span class="comment-username">{{ comment.user?.nickname || '匿名' }}</span>
                  <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                </div>

                <!-- 内容 -->
                <div class="comment-content">{{ comment.content }}</div>

                <!-- 点赞信息 -->
                <div class="comment-footer">
                  <span class="like-count">👍 {{ comment.likeCount }}</span>
                  <span v-if="comment.liked" class="liked-label">已点赞</span>
                </div>
              </el-card>
          </el-scrollbar>
          </div>
          <div v-else style="text-align: center; padding: 20px;">
            暂无评论
          </div>
        </el-tab-pane>
  </el-tabs>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
</el-dialog>
    
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from '@/utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { useStore } from 'vuex'
import Markdown from 'vue3-markdown-it'

const postList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)
const emptyText = ref('暂无帖子')

const searchTitle = ref('')
const searchClub = ref('')
const searchTimeRange = ref(null) // 日期范围

const dialogVisible = ref(false)
const selectedPost = ref(null)

const store = useStore()
const currentUserId = computed(() => store.state.user.id) // 获取当前登录用户ID

const activeTab = ref("post")
const commentList = ref([])

// 格式化时间
const formatDate = (dateStr) => {
  return dateStr ? dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 获取帖子列表
const fetchPosts = async (page = currentPage.value) => {
  loading.value = true
  currentPage.value = page

  let startTime = ''
  let endTime = ''
  if (searchTimeRange.value && searchTimeRange.value.length === 2) {
    startTime = dayjs(searchTimeRange.value[0]).format('YYYY-MM-DD')
    endTime = dayjs(searchTimeRange.value[1]).format('YYYY-MM-DD')
  }

  try {
    const res = await axios.get('http://localhost:8080/api/posts', {
      params: {
        page,
        pageSize,
        title: searchTitle.value,
        clubName: searchClub.value,
        startTime,
        endTime,
      },
    })
    postList.value = res.posts || []
    total.value = res.total || 0
    console.log(res)
  } catch (error) {
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

// 查看帖子详情
const viewPost = async (id) => {
  dialogVisible.value = true
  selectedPost.value = null
  commentList.value = []
  activeTab.value = 'post'

  try {
    // 帖子详情
    const res = await axios.get(`/posts/${id}`, { params: { userId: currentUserId.value } })
    console.log(res)
    if (res.post) {
      selectedPost.value = res.post
      if (typeof res.liked === 'boolean') {
        selectedPost.value.liked = res.liked
      }
      if(typeof res.post.user === 'object' && res.post.user !== null) {
        selectedPost.value.user.nickname = res.post.user.nickname
      } else {
        selectedPost.value.user = { nickname: '未知用户' }
      }
    } else {
      ElMessage.error('帖子数据格式异常')
    }

    // 评论列表
    const commentRes = await axios.get(`/posts/${id}/comments`)
    commentList.value = commentRes || []
  } catch (error) {
    ElMessage.error('加载帖子或评论失败')
    dialogVisible.value = false
  }
}




// 删除帖子
const deletePost = async (id) => {
  try {
    await axios.delete(`/posts/${id}`, {
      params: { userId: 5 }, // 当前用户ID，替换成实际登录用户ID
    })
    ElMessage.success('删除成功')
    fetchPosts(currentPage.value)
  } catch (error) {
    const msg = error.response?.data?.message || '删除失败'
    ElMessage.error(msg)
  }
}

const confirmDelete = (id) => {
  ElMessageBox.confirm('确定删除该帖子吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      deletePost(id)
    })
    .catch(() => {})
}

// 页面加载时请求列表
fetchPosts()
</script>

<style scoped>
.post-admin-page .toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
.markdown-content {
  border: 1px solid #eaecef;
  border-radius: 4px;
  padding: 16px;
  background-color: #fdfdfd;
  font-size: 14px;
  line-height: 1.6;
  white-space: normal;
  overflow-x: auto;
  max-height: 200px;
  overflow-y: auto;
  max-width: 100%;
  overflow-x: auto;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3 {
  margin-top: 1em;
  margin-bottom: 0.5em;
}

.markdown-content code {
  background-color: #f3f4f5;
  padding: 2px 4px;
  border-radius: 4px;
  font-family: monospace;
}

.markdown-content pre {
  background-color: #f3f4f5;
  padding: 12px;
  overflow-x: auto;
  border-radius: 6px;
}

.comment-card {
  background-color: #f9f9f9;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}

.comment-username {
  font-weight: bold;
  color: #333;
}

.comment-content {
  font-size: 14px;
  color: #444;
  line-height: 1.5;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  color: #999;
}
</style>
