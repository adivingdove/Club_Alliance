<template>
  <div class="report-manage-page">
    <el-card style="width: 100%">
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="举报状态" @change="fetchReports">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="待处理" />
          <el-option label="已处理" value="已处理" />
        </el-select>
      </div>

      <el-table :data="reports" style="width: 100%" v-if="reports.length > 0">
        <el-table-column prop="reporterNickname" label="举报人昵称" width="120" />
        <el-table-column prop="reportedUserNickname" label="被举报人昵称" width="120"/>
        <el-table-column prop="targetType" label="目标类型" width="100" />
        <el-table-column prop="reason" label="举报原因" min-width="200" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="举报时间" :formatter="(row) => formatTime(row.createdAt)" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button type="success" size="small" @click="changeStatus(scope.row.id, '已处理')">设为已处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDetail" title="举报详情" width="50%">
      <div v-if="selectedReport">
        <div><strong>举报ID：</strong>{{ selectedReport.id }}</div>
        <div><strong>举报人ID：</strong>{{ selectedReport.reporterId }}</div>
        <div><strong>目标类型：</strong>{{ selectedReport.targetType }}</div>
        <div><strong>目标ID：</strong>{{ selectedReport.targetId }}</div>
        <div><strong>帖子ID：</strong>{{selectedReport.postId}}</div>
        <div><strong>目标链接：</strong>
          <a v-if="selectedReport.targetType === '帖子'" :href="`/post/${selectedReport.targetId}`" target="_blank">查看帖子</a>
          <a v-else-if="selectedReport.targetType === '评论'" :href="`/post/${selectedReport.postId}#comment/${selectedReport.targetId}`" target="_blank">查看评论</a>
          <span v-else>无链接</span>
        </div>
        <div><strong>举报原因：</strong>{{ selectedReport.reason }}</div>
        <div><strong>状态：</strong>{{ selectedReport.status }}</div>
        <div><strong>创建时间：</strong>{{ formatTime(selectedReport.createdAt) }}</div>
      </div>
      <div v-else>加载中...</div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const reports = ref([])
const statusFilter = ref('')
const showDetail = ref(false)
const selectedReport = ref(null)


const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const fetchReports = async () => {
  const { data } = await axios.get('/report/admin/list', {
    params: { status: statusFilter.value }
  })
  reports.value = data
}

const viewDetail = async (report) => {
  selectedReport.value = null
  showDetail.value = true

  try {
    const res = await axios.get(`/report/${report.id}`)
    console.log('完整响应:', res) // 这里的 res 就是数据对象
    selectedReport.value = res     // 直接赋值
  } catch (error) {
    showDetail.value = false
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('举报记录不存在')
      } else {
        ElMessage.error(error.response.data?.error || '查询失败')
      }
    } else {
      ElMessage.error('请求举报详情失败')
    }
  }
}

const changeStatus = async (id, status) => {
  await axios.put(`/report/status/${id}`, null, {
    params: { status }
  })
  ElMessage.success('状态更新成功')
  fetchReports()
}


onMounted(fetchReports)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 1rem;
}
</style>
