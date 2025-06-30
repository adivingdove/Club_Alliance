<template>
  <div class="club-audit-container">
    <el-card>
      <div class="header">
        <h2>社团创建审核</h2>
      </div>
      <el-table :data="pendingClubs" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="社团名称" />
        <el-table-column prop="creatorId" label="创建者ID" />
        <el-table-column prop="description" label="社团简介" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="success" size="small" @click="audit(scope.row.id, 'approve')">通过</el-button>
            <el-button type="danger" size="small" @click="audit(scope.row.id, 'reject')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const pendingClubs = ref([])
const loading = ref(false)

const fetchPendingClubs = async () => {
  loading.value = true
  try {
    const res = await axios.get('/clubs/pending')
    pendingClubs.value = res.data
  } catch (err) {
    ElMessage.error('获取待审核社团失败')
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
