<template>
  <div class="club-management">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="社团管理" name="manage">
        <el-card>
          <div class="header">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索我的社团..."
              clearable
              @clear="fetchClubs"
              @keyup.enter="fetchClubs"
              style="width: 300px;"
            />
            <el-button type="primary" @click="fetchClubs">搜索</el-button>
            <el-button type="success" @click="openCreateDialog">新建社团</el-button>
          </div>

          <el-table :data="clubList" style="width: 100%" border>
            <el-table-column prop="name" label="社团名称" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ statusMap[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="320">
              <template #default="{ row }">
                <el-button size="small" @click="viewDetail(row.id)">详情</el-button>
                <el-button size="small" type="primary" @click="editClub(row)">编辑</el-button>
                <el-button size="small" type="warning" @click="manageMembers(row)">成员管理</el-button>
                <el-button size="small" type="danger" @click="deleteClub(row.id)">解散社团</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="申请处理" name="applications">
        <div class="applications-container">
          <el-row :gutter="24">
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
            <el-col :span="24" v-if="pendingApplications.length === 0 && processedApplications.length === 0">
              <el-empty description="暂无申请信息" />
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      <el-tab-pane label="活动管理" name="activities">
        <div class="activities-container">
          <el-card>
            <div class="header">
              <el-input
                v-model="activitySearchKeyword"
                placeholder="搜索活动标题或描述"
                clearable
                @clear="fetchActivities"
                @keyup.enter="fetchActivities"
                style="width: 300px;"
              />
              <el-button type="primary" @click="fetchActivities">搜索</el-button>
              <el-button type="success" @click="showCreateActivityDialog = true">发布活动</el-button>
            </div>

            <el-table :data="activityList" style="width: 100%" border>
              <el-table-column prop="title" label="活动标题" />
              <el-table-column prop="location" label="活动地点" width="150" />
              <el-table-column prop="startTime" label="开始时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.startTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="applyStatus" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getActivityStatusType(row.applyStatus)">
                    {{ getActivityStatusText(row.applyStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" @click="viewActivityDetail(row)">详情</el-button>
                  <el-button size="small" type="primary" @click="editActivity(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteActivityHandler(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="社团详情" width="500px">
      <div v-if="clubDetail">
        <p><strong>名称：</strong>{{ clubDetail.name }}</p>
        <p><strong>描述：</strong>{{ clubDetail.description }}</p>
        <p><strong>标签：</strong>{{ clubDetail.tags }}</p>
        <p><strong>状态：</strong>{{ statusMap[clubDetail.status] || clubDetail.status }}</p>
        <p><strong>类型：</strong>{{ clubDetail.type }}</p>
        <p><strong>创建时间：</strong>{{ clubDetail.createdAt }}</p>
      </div>
    </el-dialog>

    <el-dialog v-model="showMemberDialog" title="成员管理" width="600px">
      <el-table :data="memberList" style="width: 100%">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-select v-model="row.role" size="small" @change="role => setMemberRole(row, role)" :disabled="row.role === '社长'">
              <el-option label="成员" value="成员" />
              <el-option label="干事" value="干事" />
              <el-option label="副社长" value="副社长" />
              <el-option v-if="row.role === '社长'" label="社长" value="社长" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button v-if="row.role !== '社长'" size="small" type="danger" @click="transferPresident(row)">转让社长</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 创建活动对话框 -->
    <el-dialog 
      v-model="showCreateActivityDialog" 
      title="发布活动" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="activityForm" 
        :rules="activityRules" 
        ref="activityFormRef" 
        label-width="100px"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="activityForm.title" placeholder="请输入活动标题" />
        </el-form-item>
        
        <el-form-item label="活动描述" prop="description">
          <el-input 
            v-model="activityForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入活动描述"
          />
        </el-form-item>
        
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="activityForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="activityForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="activityForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number 
            v-model="activityForm.maxParticipants" 
            :min="1" 
            placeholder="不填表示人数不限"
          />
        </el-form-item>
        
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="activityForm.clubId" placeholder="请选择所属社团">
            <el-option 
              v-for="club in clubList" 
              :key="club.id" 
              :label="club.name" 
              :value="club.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateActivityDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitActivity"
          :disabled="clubList.length === 0"
        >
          发布活动
        </el-button>
      </template>
    </el-dialog>

    <!-- 活动详情对话框 -->
    <el-dialog 
      v-model="showActivityDetailDialog" 
      title="活动详情" 
      width="700px"
    >
      <div v-if="selectedActivity" class="activity-detail">
        <div class="detail-header">
          <h2>{{ selectedActivity.title }}</h2>
          <div class="detail-status" :class="getActivityStatusClass(selectedActivity.applyStatus)">
            {{ getActivityStatusText(selectedActivity.applyStatus) }}
          </div>
        </div>
        
        <div class="detail-content">
          <p class="detail-description">{{ selectedActivity.description }}</p>
          
          <div class="detail-info">
            <div class="info-row">
              <span class="label">活动时间：</span>
              <span>{{ formatDateTime(selectedActivity.startTime) }} - {{ formatDateTime(selectedActivity.endTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">活动地点：</span>
              <span>{{ selectedActivity.location || '地点待定' }}</span>
            </div>
            <div class="info-row">
              <span class="label">参与人数：</span>
              <span>{{ selectedActivity.currentParticipants || 0 }}/{{ selectedActivity.maxParticipants ? selectedActivity.maxParticipants : '∞' }}人</span>
            </div>
            <div class="info-row">
              <span class="label">创建时间：</span>
              <span>{{ formatDateTime(selectedActivity.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑活动对话框 -->
    <el-dialog 
      v-model="showEditActivityDialog" 
      title="编辑活动" 
      width="600px"
    >
      <el-form 
        ref="editActivityFormRef" 
        :model="editActivityForm" 
        :rules="activityRules" 
        label-width="100px"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="editActivityForm.title" placeholder="请输入活动标题" />
        </el-form-item>
        
        <el-form-item label="活动描述" prop="description">
          <el-input 
            v-model="editActivityForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入活动描述"
          />
        </el-form-item>
        
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="editActivityForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="editActivityForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="editActivityForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number 
            v-model="editActivityForm.maxParticipants" 
            :min="1" 
            placeholder="不填表示人数不限"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditActivityDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEditActivity">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import request from '../utils/request'
import { 
  createActivity,
  deleteActivity,
  updateActivity,
  getActivitiesByCreatorId
} from '@/api/activityApi'

const clubList = ref([])
const searchKeyword = ref('')
const showDetailDialog = ref(false)
const clubDetail = ref(null)
const showMemberDialog = ref(false)
const memberList = ref([])
let currentClubId = null
const activeTab = ref('manage')

// 申请处理相关
const pendingApplications = ref([])
const processedApplications = ref([])

// 活动管理相关
const activityList = ref([])
const activitySearchKeyword = ref('')
const showCreateActivityDialog = ref(false)
const showActivityDetailDialog = ref(false)
const showEditActivityDialog = ref(false)
const selectedActivity = ref(null)
const currentEditActivityId = ref(null)
const activityFormRef = ref()
const editActivityFormRef = ref()

// 活动表单
const activityForm = ref({
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: null,
  clubId: null
})

const editActivityForm = ref({
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: null
})

// 表单验证规则
const activityRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  clubId: [{ required: true, message: '请选择所属社团', trigger: 'change' }]
}

const statusMap = {
  正常: '正常',
  待审核: '待审核',
  已封禁: '已封禁',
}

const getStatusTagType = (status) => {
  switch (status) {
    case '正常': return 'success'
    case '待审核': return 'warning'
    case '已封禁': return 'danger'
    default: return ''
  }
}

const fetchClubs = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    const res = await axios.get(`/clubs/creator/${user.id}`)
    let data = res.data || []
    if (searchKeyword.value.trim()) {
      data = data.filter(club => club.name.includes(searchKeyword.value.trim()))
    }
    clubList.value = data
  } catch (error) {
    ElMessage.error('加载社团失败')
    clubList.value = []
  }
}

const viewDetail = async (clubId) => {
  try {
    const res = await axios.get(`/clubs/${clubId}/detail`)
    if (res.code === 0) {
      clubDetail.value = res.data
      showDetailDialog.value = true
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const openCreateDialog = () => {
  ElMessage.info('新建社团功能待实现')
}

const editClub = (row) => {
  ElMessage.info('编辑社团功能待实现')
}

const manageMembers = async (club) => {
  currentClubId = club.id
  // 获取成员列表
  const res = await axios.get(`/clubs/${club.id}/detail`)
  memberList.value = res.data.members || []
  showMemberDialog.value = true
}

const setMemberRole = async (member, role) => {
  try {
    await axios.put(`/clubs/${currentClubId}/members/${member.id}/role`, { creatorId: memberList.value.find(m => m.role === '社长').userId, role })
    ElMessage.success('角色设置成功')
    manageMembers({ id: currentClubId })
  } catch (e) {
    ElMessage.error('角色设置失败')
  }
}

const transferPresident = async (member) => {
  try {
    // 判断当前用户是否为社长
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const president = memberList.value.find(m => m.role === '社长')
    if (!president || Number(user.id) !== Number(president.userId)) {
      ElMessage.error('只有社长本人才能转让社长身份')
      return
    }
    await axios.put(`/clubs/${currentClubId}/transfer-president`, {
      fromUserId: president.userId,
      toUserId: member.userId
    })
    ElMessage.success('社长已转让')
    showMemberDialog.value = false // 转让后关闭弹窗
    fetchClubs()
  } catch (e) {
    ElMessage.error('转让失败')
  }
}

const deleteClub = async (clubId) => {
  try {
    await ElMessageBox.confirm('确定要解散该社团吗？', '删除确认', { type: 'warning' })
    await axios.delete(`/clubs/${clubId}`)
    ElMessage.success('解散成功')
    fetchClubs()
  } catch (error) {
    ElMessage.error('解散失败')
  }
}

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
    } else {
      ElMessage.error('获取申请信息失败')
    }
  } catch (e) {
    ElMessage.error('获取申请信息失败')
  }
}

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
      await fetchApplications()
    } else {
      ElMessage.error(response.data.message || `操作失败`)
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 活动管理相关函数
const fetchActivities = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    
    const response = await getActivitiesByCreatorId(user.id)
    if (response.data.code === 0) {
      let activities = response.data.data || []
      
      // 搜索过滤
      if (activitySearchKeyword.value.trim()) {
        activities = activities.filter(activity => 
          activity.title.includes(activitySearchKeyword.value.trim()) || 
          activity.description.includes(activitySearchKeyword.value.trim())
        )
      }
      
      activityList.value = activities
    } else {
      ElMessage.error('获取活动列表失败')
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
    ElMessage.error('获取活动列表失败')
  }
}

const submitActivity = async () => {
  try {
    await activityFormRef.value.validate()
    
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    
    const activityData = {
      ...activityForm.value,
      creatorId: user.id
    }
    
    const response = await createActivity(activityData)
    if (response.data.code === 0) {
      ElMessage.success('发布活动成功')
      showCreateActivityDialog.value = false
      activityForm.value = {
        title: '',
        description: '',
        location: '',
        startTime: '',
        endTime: '',
        maxParticipants: null,
        clubId: null
      }
      fetchActivities()
    } else {
      ElMessage.error(response.data.message || '发布活动失败')
    }
  } catch (error) {
    console.error('发布活动失败:', error)
    ElMessage.error('发布活动失败，请检查表单数据')
  }
}

const viewActivityDetail = (activity) => {
  selectedActivity.value = activity
  showActivityDetailDialog.value = true
}

const editActivity = (activity) => {
  currentEditActivityId.value = activity.id
  editActivityForm.value = {
    title: activity.title,
    description: activity.description,
    location: activity.location,
    startTime: activity.startTime,
    endTime: activity.endTime,
    maxParticipants: activity.maxParticipants
  }
  showEditActivityDialog.value = true
}

const submitEditActivity = async () => {
  try {
    await editActivityFormRef.value.validate()
    
    if (!currentEditActivityId.value) {
      ElMessage.error('编辑活动ID不存在')
      return
    }
    
    const response = await updateActivity(currentEditActivityId.value, editActivityForm.value)
    if (response.data.code === 0) {
      ElMessage.success('编辑活动成功')
      showEditActivityDialog.value = false
      currentEditActivityId.value = null
      fetchActivities()
    } else {
      ElMessage.error(response.data.message || '编辑活动失败')
    }
  } catch (error) {
    console.error('编辑活动失败:', error)
    ElMessage.error('编辑活动失败，请检查表单数据')
  }
}

const deleteActivityHandler = async (activityId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteActivity(activityId)
    if (response.data.code === 0) {
      ElMessage.success('删除活动成功')
      fetchActivities()
    } else {
      ElMessage.error(response.data.message || '删除活动失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除活动失败:', error)
      ElMessage.error('删除活动失败')
    }
  }
}

// 工具函数
const getActivityStatusType = (status) => {
  switch (status) {
    case '通过': return 'success'
    case '待审核': return 'warning'
    case '拒绝': return 'danger'
    default: return ''
  }
}

const getActivityStatusText = (status) => {
  switch (status) {
    case '通过': return '已通过'
    case '待审核': return '待审核'
    case '拒绝': return '已拒绝'
    default: return status
  }
}

const getActivityStatusClass = (status) => {
  switch (status) {
    case '通过': return 'status-approved'
    case '待审核': return 'status-pending'
    case '拒绝': return 'status-rejected'
    default: return ''
  }
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchClubs()
  fetchApplications()
  fetchActivities()
})
</script>

<style scoped>
.club-management {
  padding: 20px;
}
.header {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;
}
.applications-container {
  padding: 20px;
}
.activities-container {
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

/* 活动详情样式 */
.activity-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-header h2 {
  margin: 0;
  color: #303133;
}

.detail-status {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
}

.status-approved {
  background-color: #f0f9ff;
  color: #67c23a;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

.detail-description {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
  margin-bottom: 20px;
}

.detail-info {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  font-weight: bold;
  color: #303133;
  width: 100px;
  flex-shrink: 0;
}
</style>
