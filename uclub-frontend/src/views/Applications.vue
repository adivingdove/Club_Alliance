<template>
  <div class="applications-container">
    <el-button type="primary" @click="createTestApplications" style="margin-bottom: 20px;">生成测试申请</el-button>
    <el-row :gutter="24">
      <el-col :span="24">
        <h2 class="page-title">申请信息</h2>
      </el-col>
      
      <!-- 待审核的申请 -->
      <el-col :span="24" v-if="pendingApplications.length > 0">
        <el-card class="application-card">
          <template #header>
            <div class="card-header">
              <span>待审核的申请</span>
              <el-tag type="warning">{{ pendingApplications.length }}个</el-tag>
            </div>
          </template>
          <el-table :data="pendingApplications" style="width: 100%">
            <el-table-column prop="clubName" label="社团名称" width="200" />
            <el-table-column prop="applicantName" label="申请人" width="150" />
            <el-table-column prop="applicantInfo" label="申请人信息" width="200" />
            <el-table-column prop="reason" label="申请理由" />
            <el-table-column prop="appliedAt" label="申请时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="success" size="small" @click="handleApplication(scope.row, 'approve')">
                  通过
                </el-button>
                <el-button type="danger" size="small" @click="handleApplication(scope.row, 'reject')">
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <!-- 已处理的申请 -->
      <el-col :span="24">
        <el-card class="application-card">
          <template #header>
            <div class="card-header">
              <span>已处理的申请</span>
            </div>
          </template>
          <el-table :data="processedApplications" style="width: 100%">
            <el-table-column prop="clubName" label="社团名称" width="200" />
            <el-table-column prop="applicantName" label="申请人" width="150" />
            <el-table-column prop="applicantInfo" label="申请人信息" width="200" />
            <el-table-column prop="reason" label="申请理由" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === '已通过' ? 'success' : 'danger'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="processedAt" label="处理时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      
      <!-- 空状态 -->
      <el-col :span="24" v-if="pendingApplications.length === 0 && processedApplications.length === 0">
        <el-empty description="暂无申请信息" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const pendingApplications = ref([])
const processedApplications = ref([])

// 获取申请信息
const fetchApplications = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    
    const response = await request.get('/api/clubs/applications', {
      params: { creatorId: user.id }
    })
    
    if (response.data.code === 0) {
      const data = response.data.data || {}
      // 只显示需要当前用户审批的、且不是自己发起的、且不是社长身份的
      pendingApplications.value = (data.pending || []).filter(app =>
        String(app.userId) !== String(user.id) &&
        app.memberRole !== '社长' &&
        app.status === '待审核'
      )
      processedApplications.value = (data.processed || []).filter(app =>
        String(app.userId) !== String(user.id) &&
        app.memberRole !== '社长' &&
        app.status !== '待审核'
      )
      console.log('pending原始数据:', data.pending)
      console.log('processed原始数据:', data.processed)
    } else {
      ElMessage.error('获取申请信息失败')
    }
  } catch (e) {
    console.error('获取申请信息失败:', e)
    ElMessage.error('获取申请信息失败')
  }
}

// 处理申请
const handleApplication = async (application, action) => {
  try {
    const actionText = action === 'approve' ? '通过' : '拒绝'
    const confirmText = action === 'approve' ? '确定通过该申请吗？' : '确定拒绝该申请吗？'
    
    await ElMessageBox.confirm(confirmText, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const response = await request.put(`/api/clubs/applications/${application.id}/${action}`, {
      creatorId: user.id
    })
    
    if (response.data.code === 0) {
      ElMessage.success(`申请已${actionText}`)
      // 重新获取申请信息
      await fetchApplications()
    } else {
      ElMessage.error(response.data.message || `操作失败`)
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('处理申请失败:', e)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 生成测试申请
const createTestApplications = async () => {
  const testData = [
    { userId: 12, applicant: '测试用户A', reason: '我想加入篮球社' },
    { userId: 12, applicant: '测试用户B', reason: '热爱篮球，想参与活动' },
    { userId: 12, applicant: '测试用户C', reason: '希望锻炼身体' }
  ]
  try {
    await Promise.all(testData.map(data =>
      request.post('/api/clubs/9/apply', data)
    ))
    ElMessage.success('测试申请已生成')
    await fetchApplications()
  } catch (e) {
    console.error(e)
    ElMessage.error('生成测试申请失败')
  }
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.applications-container {
  padding: 20px;
}

.page-title {
  font-size: 2vw;
  color: #409EFF;
  margin-bottom: 2vw;
  padding-left: 1%;
}

.application-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: bold;
}
</style> 