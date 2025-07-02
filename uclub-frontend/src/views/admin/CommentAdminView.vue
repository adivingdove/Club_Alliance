<template>
  <div class="comment-admin">
    <el-card>
      <template #header>
        <div class="header">
          <span>评论管理</span>
        </div>
      </template>

      <!-- 搜索 -->
      <div class="search-bar">
        <el-input
          v-model="searchPostId"
          placeholder="根据帖子ID搜索"
          style="width: 200px; margin-right: 10px"
        />
        <el-button type="primary" @click="fetchComments">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <!-- 评论表格 -->
      <el-table :data="commentList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="content" label="评论内容" />
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            {{ row.user?.nickname || '匿名用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞数" width="80" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="danger" size="small" @click="deleteComment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="commentList.length === 0" description="暂无评论数据" />

    </el-card>

    <!-- 评论详情弹窗 -->
    <el-dialog v-model="showDetail" title="评论详情" width="500px">
      <p><strong>ID：</strong>{{ detailItem.id }}</p>
      <p><strong>内容：</strong>{{ detailItem.content }}</p>
      <p><strong>用户ID：</strong>{{ detailItem.userId }}</p>
      <p><strong>所属帖子ID：</strong>{{ detailItem.postId }}</p>
      <p><strong>发布时间：</strong>{{ formatTime(detailItem.createdAt) }}</p>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const commentList = ref([])
const searchPostId = ref('')
const showDetail = ref(false)
const detailItem = ref({})

// 获取评论（可选搜索 postId）
const fetchComments = async () => {
  try {
    if (!searchPostId.value) {
      ElMessage.warning('请输入帖子ID')
      return
    }
    const res = await fetch(`http://localhost:8080/api/posts/${searchPostId.value}/comments`)
    const data = await res.json()
    commentList.value = data
  } catch (error) {
    ElMessage.error('加载评论失败')
  }
}

// 重置搜索
const resetSearch = () => {
  searchPostId.value = ''
  commentList.value = []
}

// 删除评论
const deleteComment = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除该评论？（ID: ${row.id}）`, '警告', {
      type: 'warning'
    })

    await fetch(`http://localhost:8080/api/posts/${row.postId}/comments/${row.id}?`, {
      method: 'DELETE'
    })

    ElMessage.success('删除成功')
    fetchComments()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 详情查看
const viewDetail = (row) => {
  detailItem.value = row
  showDetail.value = true
}

// 时间格式化
const formatTime = (str) => new Date(str).toLocaleString()
</script>

<style scoped>
.comment-admin {
  max-width: 1000px;
  margin: auto;
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.header {
  font-size: 18px;
  font-weight: bold;
}
</style>
