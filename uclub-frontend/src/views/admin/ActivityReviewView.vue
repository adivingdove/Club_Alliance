<template>
  <div class="activity-audit-container">
    <el-card>
      <div class="header">
        <h2>社团活动审核</h2>
      </div>
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="待审核活动" name="pending">
          <el-table :data="pendingActivities" v-loading="loading" border stripe>
            <el-table-column prop="id" label="活动ID" width="80" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="clubId" label="所属社团ID" />
            <el-table-column prop="applyStatus" label="状态" />
            <el-table-column label="开始时间" :formatter="(row) => formatTime(row.startTime)" />
            <el-table-column label="结束时间" :formatter="(row) => formatTime(row.endTime)" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="success" size="small" @click="audit(scope.row.id, '通过')">通过</el-button>
                <el-button type="danger" size="small" @click="audit(scope.row.id, '拒绝')">拒绝</el-button>
                <el-button type="primary" size="small" @click="checkDetail(scope.row.id, '查看')">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="历史申请记录" name="history">
          <el-table :data="historyActivities" v-loading="loading" border stripe>
            <el-table-column prop="id" label="活动ID" width="80" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="clubId" label="所属社团ID" />
            <el-table-column prop="applyStatus" label="状态" />
            <el-table-column label="开始时间" :formatter="(row) => formatTime(row.startTime)" />
            <el-table-column label="结束时间" :formatter="(row) => formatTime(row.endTime)" />
            <el-table-column label="申请时间" :formatter="(row) => formatTime(row.createdAt)" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small" @click="checkDetail(scope.row.id, '查看')">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog title="活动详情" v-model="showDetailDialog" width="600px" :before-close="() => (showDetailDialog = false)">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="活动ID">{{ selectedActivity.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ selectedActivity.title }}</el-descriptions-item>
        <el-descriptions-item label="社团ID">{{ selectedActivity.clubId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selectedActivity.applyStatus }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(selectedActivity.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(selectedActivity.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatTime(selectedActivity.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="详细描述">{{ selectedActivity.description || '无' }}</el-descriptions-item>
      </el-descriptions>
  <template #footer>
    <el-button @click="showDetailDialog = false">关闭</el-button>
  </template>
</el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const activeTab = ref('pending')
const loading = ref(false)

const pendingActivities = ref([])
const historyActivities = ref([])

const showDetailDialog = ref(false)
const selectedActivity = ref({})

const fetchPendingActivities = async () => {
  loading.value = true
  try {
    const res = await axios.get('/activities/pending')
    pendingActivities.value = res.data
  } catch (err) {
    ElMessage.error('获取待审核活动失败')
  } finally {
    loading.value = false
  }
}

const fetchHistoryActivities = async () => {
  loading.value = true
  try {
    const res = await axios.get('/activities/history')  // 替换为你的历史记录接口
    historyActivities.value = res.data
  } catch (err) {
    ElMessage.error('获取历史申请失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const audit = async (activityId, status) => {
  try {
    await ElMessageBox.confirm(`确认将活动设为“${status}”？`, '审核确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: status === '通过' ? 'success' : 'warning'
    })

    await axios.put(`/activities/${activityId}/apply-status`, null, {
      params: { applyStatus: status }
    })

    ElMessage.success(`审核成功：${status}`)
    fetchPendingActivities()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(`审核失败：${status}`)
    }
  }
}

const checkDetail = async (id, mode) => {
  try {
    const response = await axios.get(`/activities/${id}`)
    selectedActivity.value = response.data
    showDetailDialog.value = true
  } catch (error) {
    console.error('获取活动详情失败', error)
    ElMessage.error('获取活动详情失败')
  }
}


const handleTabChange = (tab) => {
  if (tab.props.name === 'pending') {
    fetchPendingActivities()
  } else {
    fetchHistoryActivities()
  }
}

onMounted(() => {
  fetchPendingActivities()
})
</script>

<style scoped>
.activity-audit-container {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
</style>
