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
          </div>

          <el-table :data="clubList" style="width: 100%" border>
            <el-table-column prop="name" label="社团名称" >
              <template #default="{ row }">
                <span class="club-link" @click="goToClubDetail(row.id)">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="发布公告" width="120">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="openAnnouncementDialog(row)">发布公告</el-button>
              </template>
            </el-table-column>
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
                <el-button
                  v-if="row.myRole === '社长'"
                  size="small"
                  type="primary"
                  @click="openEditDialog(row)"
                >
                  编辑
                </el-button>
                <el-button size="small" type="warning" @click="manageMembers(row)">成员管理</el-button>
                <el-button
                  v-if="getMyRole(row) === '社长'"
                  size="small"
                  type="danger"
                  @click="deleteClub(row.id)"
                >解散社团</el-button>
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
                      <el-button
                        v-if="canHandleApplication(scope.row.clubId)"
                        type="success"
                        size="small"
                        @click="handleApplication(scope.row, 'approve')"
                      >通过</el-button>
                      <el-button
                        v-if="canHandleApplication(scope.row.clubId)"
                        type="danger"
                        size="small"
                        @click="handleApplication(scope.row, 'reject')"
                      >拒绝</el-button>
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
      <el-tab-pane label="社团公告" name="announcements">
        <ActivitiesAnnouncementView />
      </el-tab-pane>
    </el-tabs>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="社团详情" width="500px">
      <div v-if="clubDetail">
        <p><strong>名称：</strong>{{ clubDetail.name }}</p>
        <p><strong>描述：</strong>{{ clubDetail.description }}</p>
        <p><strong>标签：</strong>{{ typeMap[clubDetail.type] || clubDetail.type }}</p>
        <p><strong>状态：</strong>{{ statusMap[clubDetail.status] || clubDetail.status }}</p>
        <p><strong>类型：</strong>{{ clubDetail.type }}</p>
        <p><strong>创建时间：</strong>{{ clubDetail.createdAt }}</p>
        <div v-if="clubDetail.announcements && clubDetail.announcements.length" style="margin-top: 20px;">
          <h4>社团公告</h4>
          <el-timeline>
            <el-timeline-item
              v-for="a in clubDetail.announcements"
              :key="a.id"
              :timestamp="a.createdAt ? new Date(a.createdAt).toLocaleString('zh-CN') : ''"
              placement="top"
            >
              <strong>{{ a.title }}</strong>
              <div style="white-space: pre-line;">{{ a.content }}</div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showMemberDialog" width="600px">
      <template #title>
        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
          <span>成员管理</span>
          <el-input
            v-model="memberSearchKeyword"
            placeholder="搜索成员姓名..."
            clearable
            size="small"
            style="width: 220px; margin-left: 16px;"
          />
        </div>
      </template>
      <el-table :data="filteredMemberList" style="width: 100%">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-select
              v-if="clubList.find(c => c.id === currentClubId && c.myRole === '社长')"
              v-model="row.role"
              size="small"
              @change="role => setMemberRole(row, role)"
              :disabled="row.role === '社长'"
            >
              <el-option label="成员" value="成员" />
              <el-option label="干事" value="干事" />
              <el-option label="副社长" value="副社长" />
              <el-option v-if="row.role === '社长'" label="社长" value="社长" />
            </el-select>
            <span v-else>{{ row.role }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== '社长' && String(row.userId) !== String(user.id)"
              size="small"
              type="danger"
              @click="kickMember(row)"
            >踢出</el-button>
            <el-button
              v-if="row.role !== '社长' && clubList.find(c => c.id === currentClubId && c.myRole === '社长')"
              size="small"
              @click="transferPresident(row)"
            >转让社长</el-button>
          </template>
        </el-table-column>
      </el-table>
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

    <el-dialog v-model="showAnnouncementDialog" title="📢 发布社团公告" width="500px" class="announce-dialog">
      <el-input
        v-model="announcementTitle"
        placeholder="请输入公告标题"
        class="announce-input announce-title-input"
      />
      <el-input
        v-model="announcementContent"
        type="textarea"
        :rows="6"
        placeholder="请输入公告内容"
        class="announce-input announce-content-input"
      />
      <template #footer>
        <el-button @click="showAnnouncementDialog = false" class="announce-cancel-btn">取消</el-button>
        <el-button type="primary" @click="submitAnnouncement" class="announce-submit-btn">
          <i class="el-icon-message"></i> 发布
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑社团信息" width="400px" :close-on-click-modal="false">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="90px">
        <el-form-item label="社团名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="主页图片" prop="logoUrl">
          <el-input v-model="editForm.logoUrl" placeholder="图片URL或上传" style="width: 70%; margin-right: 8px;" />
          <el-upload
            action="http://localhost:8080/api/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleEditLogoUploadSuccess"
            :on-error="handleEditLogoUploadError"
            :before-upload="handleBeforeEditLogoUpload"
          >
            <el-button type="primary" size="small">上传图片</el-button>
          </el-upload>
          <div v-if="editForm.logoUrl" style="margin-top: 10px;">
            <img :src="getImageUrl(editForm.logoUrl)" style="max-width: 200px; max-height: 150px; border-radius: 8px; border: 1px solid #ddd;" alt="图片预览" />
            <p style="margin-top: 5px; font-size: 12px; color: #666;">图片预览</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import ActivitiesAnnouncementView from '@/views/ActivitiesAnnouncementView.vue'
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { 
  createActivity,
  deleteActivity,
  updateActivity,
  getActivitiesByCreatorId,
  getUserManagementClubs
} from '@/api/activityApi'


const clubList = ref([])
const searchKeyword = ref('')
const showDetailDialog = ref(false)
const clubDetail = ref(null)
const showMemberDialog = ref(false)
const memberList = ref([])
const memberSearchKeyword = ref('')
const filteredMemberList = computed(() => {
  if (!memberSearchKeyword.value.trim()) return memberList.value
  const kw = memberSearchKeyword.value.trim().toLowerCase()
  return memberList.value.filter(m => (m.name || '').toLowerCase().includes(kw))
})
let currentClubId = null
const activeTab = ref('manage')

// 申请处理相关
const pendingApplications = ref([])
const processedApplications = ref([])

// 活动管理相关
const activityList = ref([])
const activitySearchKeyword = ref('')
const showActivityDetailDialog = ref(false)
const showEditActivityDialog = ref(false)
const selectedActivity = ref(null)
const currentEditActivityId = ref(null)
const activityFormRef = ref()
const editActivityFormRef = ref()

// 表单验证规则
const activityRules = {
  clubId: [
    { required: true, message: '请选择所属社团', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const statusMap = {
  正常: '正常',
  待审核: '待审核',
  已封禁: '已封禁',
}

const typeMap = {
  1: '学术科技',
  2: '文化艺术',
  3: '体育竞技',
  4: '公益实践',
  5: '创新创业'
}

const getStatusTagType = (status) => {
  switch (status) {
    case '正常': return 'success'
    case '待审核': return 'warning'
    case '已封禁': return 'danger'
    default: return ''
  }
}

const router = useRouter()
const goToClubDetail = (id) => {
  router.push(`/club/${id}`)
}

const fetchClubs = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    // 获取用户加入的所有社团
    const res = await axios.get(`/clubs/joined`, { params: { userId: user.id } })
    let data = res.data || []

    // 并发获取自己在每个社团的成员信息
    const rolePromises = data.map(async club => {
      try {
        const detailRes = await axios.get(`/clubs/${club.id}/detail`)
        const members = detailRes.data && detailRes.data.members ? detailRes.data.members : []
        const myMember = members.find(m => String(m.userId) === String(user.id))
        if (myMember && ['干事', '副社长', '社长'].includes(myMember.role)) {
          return { ...club, myRole: myMember.role, members }
        }
        return null
      } catch (e) {
        return null
      }
    })
    const clubsWithRole = (await Promise.all(rolePromises)).filter(Boolean)

    // 搜索过滤
    if (searchKeyword.value.trim()) {
      clubList.value = clubsWithRole.filter(club => club.name.includes(searchKeyword.value.trim()))
    } else {
      clubList.value = clubsWithRole
    }
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
    await ElMessageBox.confirm(
      `确定要将社长转让给【${member.name}】吗？`,
      '转让社长确认',
      { type: 'warning' }
    )
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
    if (e !== 'cancel') {
      ElMessage.error('转让失败')
    }
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

const showAnnouncementDialog = ref(false)
const announcementTitle = ref('')
const announcementContent = ref('')
const currentAnnouncementClub = ref(null)

const openAnnouncementDialog = (club) => {
  currentAnnouncementClub.value = club
  announcementTitle.value = ''
  announcementContent.value = ''
  showAnnouncementDialog.value = true
}

const submitAnnouncement = async () => {
  if (!announcementTitle.value.trim()) {
    ElMessage.error('公告标题不能为空')
    return
  }
  if (!announcementContent.value.trim()) {
    ElMessage.error('公告内容不能为空')
    return
  }
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    const res = await axios.post('/announcements', {
      clubId: currentAnnouncementClub.value.id,
      title: announcementTitle.value,
      content: announcementContent.value,
      type: '社团',
      creatorId: user.id
    })
    if (res.code === 0) {
      ElMessage.success('公告发布成功')
      showAnnouncementDialog.value = false
      fetchClubs()
    } else {
      ElMessage.error(res.message || '公告发布失败')
    }
  } catch (e) {
    ElMessage.error('公告发布失败')
    console.log('catch错误：', e)
  }
}

const kickMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要将【${member.name}】踢出社团吗？`, '踢出确认', { type: 'warning' })
    await axios.delete(`/clubs/${currentClubId}/members/${member.userId}`)
    ElMessage.success('成员已踢出')
    manageMembers({ id: currentClubId }) // 刷新成员列表
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('踢出失败')
    }
  }
}

const showEditDialog = ref(false)
const editForm = ref({
  id: null,
  name: '',
  description: '',
  logoUrl: ''
})
const editRules = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入简介', trigger: 'blur' }],
}
const editFormRef = ref()

const openEditDialog = (club) => {
  editForm.value = {
    id: club.id,
    name: club.name || '',
    description: club.description || '',
    logoUrl: club.logoUrl || ''
  }
  showEditDialog.value = true
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'Authorization': token ? `Bearer ${token}` : '',
    'X-Requested-With': 'XMLHttpRequest'
  }
})

const handleEditLogoUploadSuccess = (response) => {
  let url = response.url
  if (!url && response.data && response.data.url) {
    url = response.data.url
  }
  if (response.code === 0 && url) {
    editForm.value.logoUrl = url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleEditLogoUploadError = (err) => {
  console.error('图片上传失败:', err);
  ElMessage.error('图片上传失败，请重试');
};

const handleBeforeEditLogoUpload = (file) => {
  const token = localStorage.getItem('token');
  if (!token) {
    ElMessage.error('请先登录后再上传图片');
    return false;
  }
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isJPG) {
    ElMessage.error('上传图片必须是 JPG 或 PNG 格式');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB');
    return false;
  }
  return true;
};

const handleEditSubmit = () => {
  editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const clubId = editForm.value.id
        const payload = {
          name: editForm.value.name,
          description: editForm.value.description,
          logoUrl: editForm.value.logoUrl
        }
        const res = await axios.put(`/clubs/${clubId}`, payload)
        if (res.code === 0) {
          ElMessage.success('社团信息更新成功')
          showEditDialog.value = false
          fetchClubs()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } catch (e) {
        ElMessage.error('更新失败，请重试')
      }
    }
  })
}

const getImageUrl = (url) => {
  if (url && url.startsWith('/uploads/')) {
    return 'http://localhost:8080' + url
  }
  return url
}

const user = JSON.parse(localStorage.getItem('user') || '{}')
const getMyRole = (club) => {
  if (!club || !club.members || !user.id) return null
  const me = club.members.find(m => Number(m.userId) === Number(user.id))
  return me ? me.role : null
}

const canHandleApplication = (clubId) => {
  const club = clubList.value.find(c => c.id === clubId)
  return club && (club.myRole === '社长' || club.myRole === '副社长')
}

const disabledStartDate = (date) => {
  const now = new Date()
  return date < now || date > new Date(now.setDate(now.getDate() + 30))
}

const disabledEndDate = (date) => {
  const now = new Date()
  return date < now || date > new Date(now.setDate(now.getDate() + 30))
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

.club-link {
  color: #222;
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.18s, font-weight 0.18s;
}

.club-link:hover {
  color: #409EFF;
  font-weight: bold;
  background: #f4f8ff;
  border-radius: 4px;
  padding: 0 2px;
}

.announce-dialog >>> .el-dialog__body {
  background: linear-gradient(135deg, #f4faff 0%, #e3f0ff 100%);
  border-radius: 18px;
  box-shadow: 0 8px 32px 0 rgba(64,158,255,0.13);
}

.announce-input {
  border-radius: 8px;
  background: #f8fbff;
  margin-bottom: 16px;
}

.announce-title-input {
  font-weight: bold;
  font-size: 18px;
}

.announce-content-input {
  min-height: 120px;
}

.announce-submit-btn {
  background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  color: #fff;
  font-weight: bold;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.13);
  transition: background 0.2s;
}

.announce-submit-btn:hover {
  background: linear-gradient(90deg, #66b1ff 0%, #409EFF 100%);
}

.announce-cancel-btn {
  border-radius: 8px;
}
</style>
