<template>
  <div class="club-audit-container">
    <el-card>
      <div class="header">
        <h2>社团创建审核</h2>
      </div>
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="待审核社团" name="pending">
          <el-table :data="pendingClubs" v-loading="loading" border stripe>
            <el-table-column prop="name" label="社团名称" />
            <el-table-column prop="creatorNickname" label="创建者昵称" />
            <el-table-column label="操作" width="240">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewDetail(scope.row)">查看详情</el-button>
                <el-button type="success" size="small" @click="audit(scope.row.id, 'approve')">通过</el-button>
                <el-button type="danger" size="small" @click="audit(scope.row.id, 'reject')">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="历史申请记录" name="history">
          <el-table :data="historyClubs" v-loading="loading" border stripe>
            <el-table-column prop="name" label="社团名称" />
            <el-table-column prop="creatorNickname" label="创建者昵称" />
            <el-table-column prop="status" label="审核状态" />
            <el-table-column prop="createdAt" label="申请时间" :formatter="formatTime" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-dialog v-model="showDetailDialog" title="社团详情" width="500px">
        <div v-if="detailClub">
          <p><strong>社团名称：</strong>{{ detailClub.name }}</p>
          <p><strong>创建者昵称：</strong>{{ detailClub.creatorNickname }}</p>
          <p><strong>社团简介：</strong>{{ detailClub.description }}</p>
          <p><strong>创建时间：</strong>{{ formatTime(detailClub, null, detailClub.createdAt) }}</p>
        </div>
        <template #footer>
          <el-button @click="showDetailDialog = false">关闭</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const activeTab = ref('pending')
const loading = ref(false)

const pendingClubs = ref([])
const historyClubs = ref([])

const showDetailDialog = ref(false)
const detailClub = ref(null)

const viewDetail = (club) => {
  detailClub.value = club
  showDetailDialog.value = true
}

const fetchPendingClubs = async () => {
  loading.value = true
  try {
    const res = await axios.get('/clubs/admin/applications') 
    pendingClubs.value = res.data
  } catch (err) {
    ElMessage.error('获取待审核社团失败')
  } finally {
    loading.value = false
  }
}

const fetchHistoryClubs = async () => {
  loading.value = true
  try {
    const res = await axios.get('/clubs/history') 
    historyClubs.value = res.data
  } catch (err) {
    ElMessage.error('获取历史申请失败')
  } finally {
    loading.value = false
  }
}

const audit = async (clubId, action) => {
  const actionText = action === 'approve' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认要${actionText}该社团申请吗？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: action === 'approve' ? 'success' : 'warning'
    })
    await axios.put(`/clubs/admin/${clubId}/audit`, null, {
      params: { action }
    })
    ElMessage.success(`${actionText}成功`)
    fetchPendingClubs()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(`${actionText}失败`)
  }
}

const formatTime = (row, column, cellValue) => {
  if (!cellValue) return '-';

  // 把6位小数秒截成3位小数秒，dayjs能解析
  const truncated = cellValue.replace(/(\.\d{3})\d+/, '$1');

  return dayjs(truncated).format('YYYY-MM-DD HH:mm:ss');
}

const handleTabChange = (tab) => {
  if (tab.props.name === 'pending') {
    fetchPendingClubs()
  } else if (tab.props.name === 'history') {
    fetchHistoryClubs()
  }
}

onMounted(() => {
  fetchPendingClubs()
})
</script>

<style scoped>
.club-audit-container {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
</style>
