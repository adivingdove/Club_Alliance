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
    <el-dialog :visible.sync="dialogVisible" title="帖子详情" width="50%">
      <div v-if="selectedPost">
        <p><strong>ID：</strong>{{ selectedPost.id }}</p>
        <p><strong>标题：</strong>{{ selectedPost.title }}</p>
        <p><strong>内容：</strong></p>
        <p style="white-space: pre-wrap;">{{ selectedPost.content }}</p>
        <p><strong>社团：</strong>{{ selectedPost.clubName }}</p>
        <p><strong>发布时间：</strong>{{ formatDate(selectedPost.createdAt) }}</p>
        <p>
          <strong>点赞状态：</strong>
          <span v-if="selectedPost.liked">已点赞</span>
          <span v-else>未点赞</span>
        </p>
      </div>
      <div v-else>加载中...</div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from '@/utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const postList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)
const emptyText = ref('暂无帖子')

const searchTitle = ref('')
const searchClub = ref('')
const searchTimeRange = ref(null) // [start, end]

const dialogVisible = ref(false)
const selectedPost = ref(null)

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
      },
    })
    // 你后端返回是 { posts: [], total: x }
    postList.value = res.posts || []
    total.value = res.total || 0
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

  try {
    const res = await axios.get(`/posts/${id}`, { params: { userId: 5 } })
    console.log('获取帖子详情响应:', res)
    if (res.post) {
      selectedPost.value = res.post
      if (typeof res.liked === 'boolean') {
        selectedPost.value.liked = res.liked
      }
    } else {
      selectedPost.value = null
      ElMessage.error('数据格式异常')
      dialogVisible.value = false
    }
  } catch (error) {
    ElMessage.error('获取帖子详情失败')
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
</style>
