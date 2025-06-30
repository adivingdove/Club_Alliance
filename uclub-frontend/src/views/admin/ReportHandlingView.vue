<template>
  <div class="report-manage-page">
    <el-card>
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="举报状态" @change="fetchReports">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="待处理" />
          <el-option label="已处理" value="已处理" />
        </el-select>
      </div>

      <el-table :data="reports" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60"/>
        <el-table-column prop="postId" label="帖子ID"/>
        <el-table-column prop="reason" label="举报原因"/>
        <el-table-column prop="status" label="状态"/>
        <el-table-column prop="createdAt" label="举报时间"/>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button type="success" size="small" @click="changeStatus(scope.row.id, '已处理')">设为已处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDetail" title="举报详情" width="50%">
      <pre>{{ selectedReport }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/axios'
import { ElMessage } from 'element-plus'


const reports = ref([])
const statusFilter = ref('')
const showDetail = ref(false)
const selectedReport = ref(null)

const fetchReports = async () => {
  const { data } = await axios.get('/report/list', {
    params: { status: statusFilter.value }
  })
  reports.value = data
}

const viewDetail = async (report) => {
  const { data } = await axios.get(`/report/${report.id}`)
  if (!data) {
    ElMessage.error('未找到该举报信息')
    return
  }
  selectedReport.value = data
  showDetail.value = true
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
