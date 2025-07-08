<template>
  <div class="activities-container">
    <!-- Banner -->
    <div class="banner">
      <img src="../assets/ABack.jpg" class="banner-img" alt="活动横幅" />
      <div class="banner-content">
        <h1>2025年武汉大学社团活动开始啦</h1>
        <p>欢迎关注武汉大学社团活动，这里将分享武汉大学社团近期的活动信息！</p>
        <el-button type="primary" size="large" @click="handle">点击了解当下热门活动</el-button>
      </div>
    </div>

    <div class="search-box">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索活动标题或描述"
        clearable
        @input="handleSearch"
        @clear="handleSearch"
      >
        <template #suffix>
          <span style="font-size: 22px; margin-right: 8px; cursor: pointer;">🔍</span>
        </template>
      </el-input>
    </div>

    <div class="filter-tabs">
      <el-tabs v-model="activeTab" class="club-tabs" @tab-click="handleTabChange">
        <el-tab-pane label="全部活动" name="all" :disabled="tabLoading"></el-tab-pane>
        <el-tab-pane label="即将开始" name="upcoming" :disabled="tabLoading"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 活动列表 -->
    <div class="activities-list">

    
      <el-row :gutter="24">
        <el-col 
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="6" 
          v-for="activity in filteredActivities" 
          :key="activity.id"
        >
          <el-card 
            class="activity-card modern-card"
            shadow="hover"
            @click="viewActivityDetail(activity)"
          >
            <div class="activity-img-wrap">
              <img
                :src="activity.imageUrl ? getImageUrl(activity.imageUrl) : '/src/assets/vue.svg'"
                :alt="activity.title || '活动图片'"
                class="activity-img-preview"
              />
            </div>
            <div class="activity-card-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-meta">
                <span class="meta-item"><i class="el-icon-date"></i> {{ formatDate(activity.startTime) }}</span>
                <span class="meta-item"><i class="el-icon-location"></i> {{ activity.location || '地点待定' }}</span>
                <span class="meta-item"><i class="el-icon-user"></i> {{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants ? activity.maxParticipants : '∞' }}人</span>
              </div>
              <div class="activity-description-ellipsis">
                {{ getShortDescription(activity.description) }}
              </div>
            </div>
            <div class="activity-card-footer">
              <el-button type="primary" size="small" @click.stop="viewActivityDetail(activity)">详情</el-button>
              <el-button v-if="canEditActivity(activity)" type="warning" size="small" @click.stop="editActivity(activity)">编辑</el-button>
              <el-button v-if="canDeleteActivity(activity)" type="danger" size="small" @click.stop="deleteActivityHandler(activity)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    
      <!-- 空状态 -->
      <div v-if="filteredActivities.length === 0" class="empty-state">
        <el-empty description="暂无活动" />
      </div>
    </div>

    <!-- 悬浮创建按钮 -->
    <el-button
      v-if="isLoggedIn"
      class="fab-create-activity"
      type="primary"
      circle
      @click="showCreateDialog = true"
      style="position: fixed; right: 40px; bottom: 40px; z-index: 1000; width: 60px; height: 60px; box-shadow: 0 4px 16px rgba(64,158,255,0.2); display: flex; align-items: center; justify-content: center; font-size: 28px;"
    >
      <span style="font-size: 32px;">+</span>
      <i class="el-icon-plus"></i>
    </el-button>

    <!-- 创建活动对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="✨ 创建新活动" 
      width="700px"
      :close-on-click-modal="false"
      class="activity-dialog"
    >
      <div class="dialog-header">
        <h3>📝 活动信息</h3>
      </div>
      
      <el-form 
        :model="activityForm" 
        :rules="activityRules" 
        ref="activityFormRef" 
        label-width="120px"
        class="activity-form"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动标题" prop="title" class="form-item-highlight">
              <el-input 
                v-model="activityForm.title" 
                placeholder="请输入活动标题" 
                class="custom-input"
                size="large"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动描述" prop="description" class="form-item-highlight">
              <QuillEditor
                v-model="activityForm.description"
                placeholder="请输入活动描述"
                :height="400"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动地点" prop="location" class="form-item-highlight">
              <el-input 
                v-model="activityForm.location" 
                placeholder="请输入活动地点" 
                class="custom-input"
                size="large"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxParticipants">
              <el-input-number 
                v-model="activityForm.maxParticipants" 
                :min="1" 
                placeholder="不填表示人数不限"
                class="custom-input-number"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime" class="form-item-highlight">
              <el-date-picker
                v-model="activityForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                :disabled-date="disabledStartDate"
                class="custom-date-picker"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime" class="form-item-highlight">
              <el-date-picker
                v-model="activityForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                :disabled-date="disabledEndDate"
                class="custom-date-picker"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属社团" prop="clubId" class="form-item-highlight">
              <el-select 
                v-model="activityForm.clubId" 
                placeholder="请选择所属社团"
                class="custom-select"
                size="large"
                style="width: 100%"
                :disabled="clubList.length === 0"
              >
                <el-option 
                  v-for="club in clubList" 
                  :key="club.id" 
                  :label="club.name" 
                  :value="club.id" 
                />
              </el-select>
              <div v-if="clubList.length === 0" style="color: #f56c6c; font-size: 12px; margin-top: 5px;">
                您没有可以发布活动的社团（需要担任社长、副社长或干事）
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动图片" prop="imageUrl">
              <div class="upload-section">
                <el-button 
                  type="primary" 
                  @click="triggerFileInput"
                  class="upload-btn"
                  size="large"
                >
                  <i class="el-icon-upload"></i>
                  上传图片
                </el-button>
                <input ref="fileInput" type="file" style="display:none" @change="handleFileChange" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="activityForm.imageUrl" style="margin-top: 10px; text-align: center;">
          <img :src="getImageUrl(activityForm.imageUrl)" alt="活动图片" class="uploaded-image" />
        </div>
        <el-form-item v-if="clubList.length === 0 && isLoggedIn">
          <el-alert
            title="您还没有创建任何社团"
            description="请先创建社团，然后才能发布活动"
            type="warning"
            show-icon
            :closable="false"
            class="custom-alert"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitActivity"
            :disabled="clubList.length === 0 || !isLoggedIn || createLoading"
            class="submit-btn"
            :loading="createLoading"
          >
            <i class="el-icon-plus"></i>
            创建活动
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 活动详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="活动详情" 
      width="600px"
      class="activity-detail-dialog"
    >
      <div v-if="selectedActivity" class="activity-detail-card">
        <div class="detail-header">
          <div class="detail-title">{{ selectedActivity.title }}</div>
          <el-tag :type="selectedActivity.applyStatus === '通过' ? 'success' : (selectedActivity.applyStatus === '待审核' ? 'warning' : 'danger')" class="detail-status">
            {{ getStatusText(selectedActivity.applyStatus) }}
          </el-tag>
        </div>
        <div v-if="selectedActivity.imageUrl" class="detail-img-wrap">
          <img :src="getImageUrl(selectedActivity.imageUrl)" alt="活动图片" class="detail-img" />
        </div>
        <div class="detail-section">
          <div class="detail-label">活动描述：</div>
          <div class="detail-desc" v-html="safeHtml(selectedActivity.description)"></div>
        </div>
        <div class="detail-section">
          <div class="detail-info-row"><i class="el-icon-date"></i> <span class="detail-label">活动时间：</span>{{ formatDateTime(selectedActivity.startTime) }} - {{ formatDateTime(selectedActivity.endTime) }}</div>
          <div class="detail-info-row"><i class="el-icon-location"></i> <span class="detail-label">活动地点：</span>{{ selectedActivity.location || '地点待定' }}</div>
          <div class="detail-info-row"><i class="el-icon-user"></i> <span class="detail-label">参与人数：</span>{{ selectedActivity.currentParticipants || 0 }}/{{ selectedActivity.maxParticipants ? selectedActivity.maxParticipants : '∞' }}人</div>
          <div class="detail-info-row"><i class="el-icon-collection"></i> <span class="detail-label">所属社团：</span>{{ getClubNameById(selectedActivity.clubId) }}</div>
          <div class="detail-info-row"><i class="el-icon-time"></i> <span class="detail-label">创建时间：</span>{{ formatDateTime(selectedActivity.createdAt) }}</div>
        </div>

        
        <div class="detail-actions" v-if="selectedActivity && userInfo && selectedActivity.creatorId == userInfo.id">
          <el-button type="primary" size="large" @click="handleViewParticipants" style="margin-bottom: 12px; width: 100%; font-size: 18px;">查看成员列表</el-button>
        </div>
        <div class="detail-actions" v-if="isLoggedIn && selectedActivity.applyStatus === '通过'">
          <div style="display: flex; gap: 16px;">
            <el-button v-if="canEditActivity(selectedActivity)" type="primary" size="large" style="flex:1;" @click="editActivity(selectedActivity)">编辑活动</el-button>
            <el-button 
              :type="selectedActivity.isParticipating ? 'danger' : 'success'"
              size="large"
              style="flex:1;"
              @click="selectedActivity.isParticipating ? leaveActivityHandler(selectedActivity) : joinActivityHandler(selectedActivity)"
              :disabled="!canJoinActivity(selectedActivity)"
            >
              {{ selectedActivity.isParticipating ? '退出活动' : '加入活动' }}
            </el-button>
          </div>
        </div>
        <div v-if="isLoggedIn && selectedActivity.applyStatus !== '通过'" class="detail-actions">
          <el-alert
            :title="`活动状态: ${getStatusText(selectedActivity.applyStatus)}`"
            :description="selectedActivity.applyStatus === '待审核' ? '活动正在等待管理员审核，审核通过后才能加入' : '活动已被拒绝，无法加入'"
            :type="selectedActivity.applyStatus === '待审核' ? 'warning' : 'error'"
            show-icon
            :closable="false"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 成员列表对话框 -->
    <el-dialog 
      v-model="showParticipantDialog" 
      title="👥 活动成员列表"
      width="600px"
      class="participant-dialog"
    >
      <div v-if="selectedActivity" class="participant-list">
        <div class="participant-header">
          <h3>{{ selectedActivity.title }} - 成员列表</h3>
          <p class="participant-count">共 {{ participantList.length }} 人参与</p>
        </div>
        
        <div v-if="participantList.length === 0" class="empty-participants">
          <el-empty description="暂无成员参与" />
        </div>
        
        <div v-else class="participant-items">
          <div 
            v-for="participant in participantList" 
            :key="participant.id"
            class="participant-item"
          >
            <div class="participant-avatar">
              <el-avatar 
                :size="50"
                :src="getImageUrl(participant.headUrl)"
                :alt="participant.nickname || `用户${participant.userId}`"
              >
                {{ (participant.nickname || `用户${participant.userId}`).charAt(0) }}
              </el-avatar>
            </div>
            <div class="participant-info">
              <div class="participant-name">{{ participant.nickname || `用户${participant.userId}` }}</div>
              <div class="participant-join-time">加入时间：{{ formatDateTime(participant.joinTime) }}</div>
            </div>
            <div class="participant-status">
              <el-tag :type="participant.status === '已加入' ? 'success' : 'info'" size="small">
                {{ participant.status }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑活动对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      title="✏️ 编辑活动"
      width="700px"
      class="activity-dialog"
    >
      <div class="dialog-header">
        <h3>📝 修改活动信息</h3>
        <p>请修改活动的相关信息，带 * 的为必填项</p>
      </div>
      
      <el-form 
        ref="editFormRef" 
        :model="editForm" 
        :rules="activityRules" 
        label-width="120px"
        class="activity-form"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动标题" prop="title" class="form-item-highlight">
              <el-input 
                v-model="editForm.title" 
                placeholder="请输入活动标题" 
                class="custom-input"
                size="large"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动描述" prop="description" class="form-item-highlight">
              <QuillEditor
                v-model="editForm.description"
                placeholder="请输入活动描述"
                :height="400"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动地点" prop="location" class="form-item-highlight">
              <el-input 
                v-model="editForm.location" 
                placeholder="请输入活动地点" 
                class="custom-input"
                size="large"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxParticipants">
              <el-input-number 
                v-model="editForm.maxParticipants" 
                :min="1" 
                placeholder="不填表示人数不限"
                class="custom-input-number"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime" class="form-item-highlight">
              <el-date-picker
                v-model="editForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                class="custom-date-picker"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime" class="form-item-highlight">
              <el-date-picker
                v-model="editForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                class="custom-date-picker"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动图片" prop="imageUrl">
              <div class="upload-section">
                <el-button 
                  type="primary" 
                  @click="triggerFileInput"
                  class="upload-btn"
                  size="large"
                >
                  <i class="el-icon-upload"></i>
                  上传图片
                </el-button>
                <input ref="fileInput" type="file" style="display:none" @change="handleFileChange" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="editForm.imageUrl" style="margin-top: 10px; text-align: center;">
          <img :src="getImageUrl(editForm.imageUrl)" alt="活动图片" class="uploaded-image" />
        </div>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelEdit" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="submitEdit" class="submit-btn">
            <i class="el-icon-check"></i>
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAllActivities, 
  getUpcomingActivities, 
  getActivitiesByCreatorId, 
  getPendingActivities,
  searchActivities,
  createActivity,
  deleteActivity,
  updateActivityApplyStatus,
  updateActivity,
  joinActivity,
  leaveActivity,
  getActivityParticipants,
  isUserParticipating,
  getActivityParticipantCount,
  getActivitiesByParticipantId
} from '@/api/activityApi'
import { clubApi } from '@/utils/api'
import request from '@/utils/request'
import QuillEditor from '@/components/QuillEditor.vue'
import { useRouter } from 'vue-router'

// 响应式数据
const activities = ref([])
const searchKeyword = ref('')
const activeTab = ref('all')
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const selectedActivity = ref(null)
const currentEditActivityId = ref(null)
const activityFormRef = ref()
const editFormRef = ref()
const clubList = ref([])
const allClubs = ref([])
const activityDescRef = ref(null)
const editDescRef = ref(null)
const fileInput = ref(null)
const tabLoading = ref(false)
const createLoading = ref(false)
const participantList = ref([])
const showParticipantDialog = ref(false)

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

const editForm = ref({
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: null,
  clubId: null
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

// Store
const store = useStore()
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const userInfo = computed(() => store.getters.currentUser)
const isAdmin = computed(() => userInfo.value?.role === '系统管理员')

// 过滤后的活动列表
const filteredActivities = computed(() => {
  let filtered = activities.value

  // 只显示"通过"的活动
  filtered = filtered.filter(activity => activity.applyStatus === '通过')

  // 根据搜索关键词过滤
  if (searchKeyword.value) {
    filtered = filtered.filter(activity => 
      activity.title.includes(searchKeyword.value) || 
      activity.description.includes(searchKeyword.value)
    )
  }

  // 如果是"即将开始"标签页，额外过滤开始时间在当前时间之后的活动
  if (activeTab.value === 'upcoming') {
    const now = new Date()
    
    filtered = filtered.filter(activity => {
      if (!activity.startTime) {
        return false
      }
      
      // 解析活动开始时间
      let startTime
      try {
        // 处理不同的时间格式
        if (typeof activity.startTime === 'string') {
          // 如果是字符串格式，尝试解析
          if (activity.startTime.includes('T')) {
            // ISO格式: "2025-01-15T14:00:00"
            startTime = new Date(activity.startTime)
          } else if (activity.startTime.includes(' ')) {
            // 数据库格式: "2025-01-15 14:00:00"
            startTime = new Date(activity.startTime.replace(' ', 'T'))
          } else {
            // 其他格式
            startTime = new Date(activity.startTime)
          }
        } else {
          // 如果已经是Date对象
          startTime = new Date(activity.startTime)
        }
        
        // 检查解析是否成功
        if (isNaN(startTime.getTime())) {
          return false
        }
        
      } catch (error) {
        return false
      }
      
      const isUpcoming = startTime > now
      
      return isUpcoming
    })
  }

  return filtered
})

// 获取活动列表
const fetchActivities = async () => {
  try {
    let response
    
    switch (activeTab.value) {
      case 'upcoming':
        response = await getAllActivities()
        break
      case 'pending':
        if (isAdmin.value) {
          response = await getPendingActivities()
        } else {
          response = { data: { code: 0, data: [] } }
        }
        break
      default:
        response = await getAllActivities()
    }
    
    if (response && response.data && response.data.code === 0) {
      activities.value = response.data.data || []
      
      // 检查用户参与状态
      await checkUserParticipation()
    } else {
      activities.value = []
      ElMessage.error('获取活动列表失败')
    }
  } catch (error) {
    activities.value = []
    ElMessage.error('获取活动列表失败，请检查网络连接')
  }
}

// 获取所有社团列表
const fetchAllClubs = async () => {
  try {
    const response = await clubApi.getAllClubs()
    if (response.data.code === 0) {
      allClubs.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取所有社团失败:', error)
    allClubs.value = []
  }
}

// 获取用户社团列表
const fetchUserClubs = async () => {
  if (!isLoggedIn.value) {
    return
  }
  
  try {
    // 获取用户所有的社团（包括担任干事、副社长、社长的社团）
    const token = localStorage.getItem('token')
    
    const response = await request({
      url: `/api/clubs/user/${userInfo.value.id}`,
      method: 'get',
      baseURL: 'http://localhost:8080',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data.code === 0) {
      // 过滤出用户有管理权限的社团
      clubList.value = (response.data.data || [])
        .filter(c => ['干事', '副社长', '社长'].includes(c.role))
        .map(c => ({
          ...c,
          myRole: c.role
        }))
    }
  } catch (error) {
    console.error('获取社团列表失败:', error)
    clubList.value = []
    ElMessage.error('获取社团列表失败')
  }
}

// 处理标签页切换
const handleTabChange = () => {
  fetchActivities()
}

// 监听创建对话框的显示状态
watch(showCreateDialog, (newVal) => {
  if (newVal) {
    fetchUserClubs()
  }
})

// 处理搜索
const handleSearch = () => {
  // 实时搜索，不需要额外处理，computed会自动过滤
}

// 查看活动详情
const viewActivityDetail = (activity) => {
  selectedActivity.value = activity
  showDetailDialog.value = true
}

// 获取活动成员列表
const fetchActivityParticipants = async (activityId) => {
  try {
    const response = await getActivityParticipants(activityId)
    if (response.data.code === 0) {
      participantList.value = response.data.data || []
    } else {
      participantList.value = []
      ElMessage.error('获取成员列表失败')
    }
  } catch (error) {
    participantList.value = []
    ElMessage.error('获取成员列表失败，请检查网络连接')
  }
}

// 处理查看成员列表
const handleViewParticipants = async () => {
  if (selectedActivity.value) {
    await fetchActivityParticipants(selectedActivity.value.id)
    showParticipantDialog.value = true
  }
}

// 编辑活动
const editActivity = (activity) => {
  currentEditActivityId.value = activity.id
  // 保证 description 为字符串
  let desc = activity.description
  if (typeof desc !== 'string') {
    try {
      desc = JSON.stringify(desc)
    } catch {
      desc = ''
    }
  }
  editForm.value = {
    title: activity.title,
    description: desc,
    location: activity.location,
    startTime: activity.startTime,
    endTime: activity.endTime,
    maxParticipants: activity.maxParticipants,
    clubId: activity.clubId
  }
  showDetailDialog.value = false
  showEditDialog.value = true
}

// 删除活动
const deleteActivityHandler = async (activity) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteActivity(activity.id)
    if (response.data.code === 0) {
      ElMessage.success('删除成功')
      fetchActivities()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除活动失败')
    }
  }
}

// 提交创建活动
const submitActivity = async () => {
  activityForm.value.description = typeof activityForm.value.description === 'string' ? activityForm.value.description : ''
  try {
    await activityFormRef.value.validate()
    if (!isLoggedIn.value) {
      ElMessage.error('请先登录')
      return
    }
    if (!userInfo.value?.id) {
      ElMessage.error('用户信息不完整，请重新登录')
      return
    }
    if (!activityForm.value.clubId) {
      ElMessage.error('请选择所属社团')
      return
    }
    createLoading.value = true
    const activityData = {
      ...activityForm.value,
      creatorId: userInfo.value.id
    }
    const response = await createActivity(activityData)
    if (response.data.code === 0) {
      ElMessage.success('创建活动成功')
      showCreateDialog.value = false
      activityForm.value = {
        title: '',
        description: '',
        location: '',
        startTime: '',
        endTime: '',
        maxParticipants: null,
        clubId: null,
        imageUrl: ''
      }
      fetchActivities()
    } else {
      ElMessage.error(response.data.message || '创建活动失败')
    }
  } catch (error) {
    ElMessage.error('创建活动失败，请检查表单数据')
  } finally {
    createLoading.value = false
  }
}

// 审核活动
const approveActivity = async (activityId) => {
  try {
    const response = await updateActivityApplyStatus(activityId, '通过')
    
    if (response.data.code === 0) {
      ElMessage.success('审核通过')
      
      // 更新本地活动列表中的状态
      const activityIndex = activities.value.findIndex(a => a.id === activityId)
      if (activityIndex !== -1) {
        activities.value[activityIndex].applyStatus = '通过'
      }
      
      // 如果当前选中的活动是被审核的活动，也更新其状态
      if (selectedActivity.value && selectedActivity.value.id === activityId) {
        selectedActivity.value.applyStatus = '通过'
      }
      
      showDetailDialog.value = false
      
      // 重新获取活动列表以确保数据同步
      await fetchActivities()
    } else {
      ElMessage.error(response.data.message || '审核失败')
    }
  } catch (error) {
    ElMessage.error('审核失败，请重试')
  }
}

const rejectActivity = async (activityId) => {
  try {
    const response = await updateActivityApplyStatus(activityId, '拒绝')
    
    if (response.data.code === 0) {
      ElMessage.success('已拒绝')
      
      // 更新本地活动列表中的状态
      const activityIndex = activities.value.findIndex(a => a.id === activityId)
      if (activityIndex !== -1) {
        activities.value[activityIndex].applyStatus = '拒绝'
      }
      
      // 如果当前选中的活动是被审核的活动，也更新其状态
      if (selectedActivity.value && selectedActivity.value.id === activityId) {
        selectedActivity.value.applyStatus = '拒绝'
      }
      
      showDetailDialog.value = false
      
      // 重新获取活动列表以确保数据同步
      await fetchActivities()
    } else {
      ElMessage.error(response.data.message || '审核失败')
    }
  } catch (error) {
    ElMessage.error('审核失败，请重试')
  }
}

// 权限检查
const canEditActivity = (activity) => {
  return isLoggedIn.value && (
    activity.creatorId === userInfo.value?.id || 
    isAdmin.value
  )
}

const canDeleteActivity = (activity) => {
  return isLoggedIn.value && (
    activity.creatorId === userInfo.value?.id || 
    isAdmin.value
  )
}

// 工具函数
const getStatusClass = (status) => {
  switch (status) {
    case '通过': return 'status-approved'
    case '待审核': return 'status-pending'
    case '拒绝': return 'status-rejected'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case '通过': return '已通过'
    case '待审核': return '待审核'
    case '拒绝': return '已拒绝'
    default: return status
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 监听登录状态变化
watch(isLoggedIn, (newVal) => {
  if (newVal) {
    fetchUserClubs()
  }
})

// 组件挂载
onMounted(async () => {
  // 获取活动数据
  await fetchActivities()
  
  // 获取所有社团数据（用于显示社团名称）
  await fetchAllClubs()
  
  if (isLoggedIn.value) {
    await fetchUserClubs()
  }
})

// 提交编辑
const submitEdit = async () => {
  try {
    if (!currentEditActivityId.value) {
      ElMessage.error('编辑活动ID不存在，请重新选择要编辑的活动')
      return
    }

    const response = await updateActivity(currentEditActivityId.value, editForm.value)
    
    if (response.data.code === 0) {
      ElMessage.success('编辑活动成功')
      showEditDialog.value = false
      currentEditActivityId.value = null
      fetchActivities()
    } else {
      ElMessage.error(response.data.message || '编辑活动失败')
    }
  } catch (error) {
    ElMessage.error('编辑活动失败，请检查表单数据')
  }
}

// 取消编辑
const cancelEdit = () => {
  showEditDialog.value = false
  currentEditActivityId.value = null // 清空编辑ID
}

// 检查是否可以加入活动
const canJoinActivity = (activity) => {
  // 允许创建者加入活动
  if (!activity) return false;
  if (!isLoggedIn.value) return false;
  // 只判断人数，不再排除创建者
  if (!activity.maxParticipants) return true;
  return (activity.currentParticipants || 0) < activity.maxParticipants;
}

// 加入活动
const joinActivityHandler = async (activity) => {
  try {
    if (!isLoggedIn.value) {
      ElMessage.error('请先登录')
      return
    }
    // 判断是否为社团成员
    const isClubMember = clubList.value.some(c => c.id === activity.clubId)
    if (!isClubMember) {
      ElMessage.error('请先加入对应社团')
      return
    }
    const response = await joinActivity(activity.id, userInfo.value.id)
    if (response.data.code === 0) {
      ElMessage.success('成功加入活动')
      activity.isParticipating = true
      activity.currentParticipants = (activity.currentParticipants || 0) + 1
    } else {
      ElMessage.error(response.data.message || '加入活动失败')
    }
  } catch (error) {
    ElMessage.error('加入活动失败')
  }
}

// 退出活动
const leaveActivityHandler = async (activity) => {
  try {
    await ElMessageBox.confirm('确定要退出这个活动吗？', '确认退出', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await leaveActivity(activity.id, userInfo.value.id)
    if (response.data.code === 0) {
      ElMessage.success('成功退出活动')
      activity.isParticipating = false
      activity.currentParticipants = Math.max(0, (activity.currentParticipants || 0) - 1)
    } else {f
      ElMessage.error(response.data.message || '退出活动失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出活动失败')
    }
  }
}

// 检查用户参与状态
const checkUserParticipation = async () => {
  if (!isLoggedIn.value) return
  
  for (const activity of activities.value) {
    try {
      const response = await isUserParticipating(activity.id, userInfo.value.id)
      if (response.data.code === 0) {
        activity.isParticipating = response.data.data
      }
    } catch (error) {
      console.error('检查参与状态失败:', error)
    }
  }
}

// 在<script setup>中添加图片上传相关方法
const handleImageSuccess = (response, form) => {
  // 兼容后端直接返回 {code, message, url}
  const url = response.url || (response.data && response.data.url)
  if (url) {
    form.imageUrl = url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

// 修改 getImageUrl 方法，确保 VITE_API_BASE_URL 读取正确，图片地址拼接为 http(s)://后端地址/uploads/xxx。并在方法内加一行 console.log 打印最终图片URL，便于调试。
const getImageUrl = (imageUrl) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
  if (!imageUrl) return '/src/assets/vue.svg';
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      return imageUrl;
}
if (imageUrl.startsWith('/uploads/')) {
  const url = `${baseUrl}${imageUrl}`;
  return url;
}
// 兼容没有斜杠的情况
const url = `${baseUrl}/uploads/${imageUrl}`;
return url;
};

function disabledStartDate(date) {
  const now = new Date()
  return date.getTime() < now.getTime() - 60000
}
function disabledEndDate(date) {
  if (!activityForm.value.startTime) return false
  return date.getTime() < new Date(activityForm.value.startTime).getTime()
}

// 修改 getClubNameById 方法
const getClubNameById = (clubId) => {
  if (!clubId) return '未知社团'
  
  // 1. 从用户社团列表查找（用户有权限的社团）
  const userClub = clubList.value.find(c => c.id === clubId)
  if (userClub) return userClub.name
  
  // 2. 从所有社团列表查找
  const allClub = allClubs.value.find(c => c.id === clubId)
  if (allClub) return allClub.name
  
  // 3. 从活动列表中查找（如果活动数据包含社团名称）
  const activity = activities.value.find(a => a.clubId === clubId && a.clubName)
  if (activity) return activity.clubName
  
  return '未知社团'
}

watch(showCreateDialog, (val) => {
  if (val) {
    nextTick(() => {
      window.$(activityDescRef.value).summernote({
        height: 180,
        placeholder: '请输入活动描述',
        callbacks: {
          onChange: function(contents) {
            activityForm.value.description = contents
          }
        }
      })
      window.$(activityDescRef.value).summernote('code', activityForm.value.description || '')
    })
  } else {
    nextTick(() => {
      if (activityDescRef.value) {
        window.$(activityDescRef.value).summernote('destroy')
      }
    })
  }
})

watch(showEditDialog, (val) => {
  if (val) {
    nextTick(() => {
      window.$(editDescRef.value).summernote({
        height: 180,
        placeholder: '请输入活动描述',
        callbacks: {
          onChange: function(contents) {
            editForm.value.description = contents
          }
        }
      })
      window.$(editDescRef.value).summernote('code', editForm.value.description || '')
    })
  } else {
    nextTick(() => {
      if (editDescRef.value) {
        window.$(editDescRef.value).summernote('destroy')
      }
    })
  }
})

const triggerFileInput = () => {
  fileInput.value && fileInput.value.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const response = await request.post('/api/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (response.data.code === 0) {
      // 判断是创建还是编辑
      if (showCreateDialog.value) {
        activityForm.value.imageUrl = response.data.url
      } else if (showEditDialog.value) {
        editForm.value.imageUrl = response.data.url
      }
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error('图片上传失败')
    }
  } catch (err) {
    ElMessage.error('图片上传失败')
  }
}

const router = useRouter()
const handle = () => {
  router.push('/ActivitiesManagerView')
}

// 活动卡片描述展示时去除 HTML 标签，仅显示纯文本
function getShortDescription(desc) {
  if (typeof desc === 'string') {
    // 去除 HTML 标签
    const text = desc.replace(/<[^>]+>/g, '');
    return text.length > 80 ? text.slice(0, 80) + '...' : text;
  }
  if (desc == null) return '';
  try {
    const str = JSON.stringify(desc);
    return str.length > 80 ? str.slice(0, 80) + '...' : str;
  } catch {
    return String(desc);
  }
}

// 安全渲染富文本描述（去除危险标签，仅保留基础格式）
function safeHtml(html) {
  if (!html) return '';
  // 只允许基础标签，去除 script/style 等危险内容
  return html.replace(/<(\/)?(script|style|iframe|object|embed|form|input|button|link|meta)[^>]*>/gi, '')
             .replace(/on\w+\s*=\s*(['"]).*?\1/gi, '');
}
</script>

<style scoped>
.top-wave {
  pointer-events: none;
}

.activities-container {
  padding: 48px 10vw 32px 10vw;
  background: #f7f8fa;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

@media (max-width: 1200px) {
  .activities-container {
    padding: 32px 5vw 24px 5vw;
  }
}
@media (max-width: 768px) {
  .activities-container {
    padding: 16px 2vw 12px 2vw;
  }
}

.banner {
  position: relative;
  margin: 32px 0 24px 0;
  border-radius: 16px;
  overflow: hidden;
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.7);
}
.banner-content {
  position: absolute;
  left: 40px;
  top: 50%;
  transform: translateY(-50%);
  color: #fff;
}
.banner-content h1 {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 12px;
}
.banner-content p {
  font-size: 16px;
  margin-bottom: 18px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 2.5rem;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 1.1rem;
  color: #606266;
}

.search-filter-section {
  margin-bottom: 30px;
}

.search-box {
  width: 520px;
  margin: 24px auto 20px auto;
  display: flex;
  justify-content: center;
  background: #f3f4f7;
  border-radius: 12px;
  box-shadow: none;
  border: 1.5px solid #f0f0f0;
  padding: 0 18px;
}
.search-box .el-input__wrapper {
  background: transparent;
  box-shadow: none;
  border: none;
}
.search-box .el-input__inner {
  background: transparent;
  color: #444;
}
.search-box .el-input__suffix {
  color: #bbb;
}

.filter-tabs {
  border-bottom: 1px solid #e4e7ed;
}

.filter-tabs .el-tabs__item {
  font-size: 17px;
  font-weight: 600;
  color: #888;
  padding: 0 32px 12px 32px;
  background: transparent;
  border: none;
  transition: color 0.2s;
}
.filter-tabs .el-tabs__item.is-active {
  color: #a18cd1;
  font-weight: 700;
  position: relative;
}
.filter-tabs .el-tabs__item.is-active::after {
  content: '';
  display: block;
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, #a18cd1 0%, #fbc2eb 100%);
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: -6px;
  width: 60%;
}
.filter-tabs .el-tabs__active-bar {
  display: none;
}

.activities-list {
  min-height: 400px;
}

.activity-card.modern-card {
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  transition: box-shadow 0.2s;
  margin-bottom: 32px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 340px;
}
.activity-img-wrap {
  width: 100%;
  height: 160px;
  background: #f4f4f4;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.activity-img-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0;
}
.activity-card-content {
  padding: 16px 12px 0 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.activity-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #222;
  text-align: left;
}
.activity-meta {
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.activity-description-ellipsis {
  font-size: 14px;
  color: #444;
  margin-bottom: 8px;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
  max-width: 100%;
}
.activity-card-footer {
  display: flex;
  gap: 8px;
  padding: 0 12px 12px 12px;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.fab-create-activity {
  position: fixed;
  right: 40px;
  bottom: 40px;
  width: 60px;
  height: 60px;
  box-shadow: 0 4px 16px rgba(64,158,255,0.2);
  z-index: 1000;
}

/* 活动详情样式 */
.activity-detail-dialog >>> .el-dialog__body {
  background: #fff;
  padding: 0;
}
.activity-detail-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  padding: 24px 24px 12px 24px;
}
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.detail-title {
  font-size: 1.6rem;
  font-weight: bold;
}
.detail-status {
  font-size: 1rem;
}
.detail-img-wrap {
  width: 100%;
  text-align: center;
  margin-bottom: 16px;
}
.detail-img {
  width: 200px;
  height: 150px;
  max-width: 500px;
  max-height: 360px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
  display: inline-block;
}
.detail-section {
  margin-bottom: 16px;
}
.detail-label {
  font-weight: bold;
  margin-right: 6px;
}
.detail-desc {
  color: #333;
  margin: 8px 0 0 0;
  word-break: break-all;
}
.detail-desc ::v-deep img {
  max-width: 400px;
  max-height: 300px;
  object-fit: cover;
  border-radius: 6px;
  display: inline-block;
}
.detail-info-row {
  margin-bottom: 6px;
  color: #555;
  font-size: 1rem;
}
.detail-actions {
  margin-top: 18px;
  text-align: right;
}

/* 编辑按钮样式 */
.edit-button {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

.edit-button:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activities-container {
    padding: 15px;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .activity-card {
    margin-bottom: 15px;
  }
  
  .activity-footer {
    flex-direction: column;
  }
  
  .fab-create-activity {
    right: 20px;
    bottom: 20px;
    width: 50px;
    height: 50px;
  }
}

.custom-button {
  color: #FFFFFF; 
  background-color: #409EFF; 
}

.activity-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  background: #f8f8f8;
}

.activity-img--dialog {
  width: 320px;
  height: 180px;
  max-width: 90%;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #ffffff;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #ffffff;
  border-radius: 8px;
  cursor: pointer;
}

.activity-content--bg {
  position: relative;
  overflow: hidden;
  min-height: 160px;
  padding: 10px 10px 6px 10px;
  border-radius: 12px;
}
.activity-img-bg {
  position: absolute;
  left: 0; top: 0; width: 100%; height: 95%;
  object-fit: cover;
  z-index: 1;
  filter: brightness(1);
}
.activity-content-inner {
  position: relative;
  z-index: 2;
  color: #ffffff;
  background: rgba(255, 255, 255, 0);
  border-radius: 12px;
  padding: 8px;
}

.activity-participants-badge {
  position: absolute;
  bottom: 16px;
  left: 16px;
  background: rgba(64,158,255,0.92);
  color: #fff;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
  display: flex;
  align-items: center;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  gap: 4px;
}

.avatar-uploader.activity-upload-highlight {
  border: 2px dashed #409EFF;
  border-radius: 8px;
  width: 104px;
  height: 104px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4faff;
  margin-bottom: 8px;
  transition: border-color 0.2s;
}
.avatar-uploader.activity-upload-highlight:hover {
  border-color: #66b1ff;
  background: #e6f7ff;
}

.detail-actions .el-button.edit-activity-btn {
  background: #ffcc00 !important;
  color: #222 !important;
  border-color: #ffcc00 !important;
}
.detail-actions .el-button.join-activity-btn {
  background: #409EFF !important;
  color: #fff !important;
  border-color: #409EFF !important;
}
.detail-actions .el-button.leave-activity-btn {
  background: #f56c6c !important;
  color: #fff !important;
  border-color: #f56c6c !important;
}

/* 活动对话框美化样式 */
.activity-dialog .el-dialog {
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.10);
  overflow: hidden;
}
.activity-dialog .el-dialog__header {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  color: white;
  padding: 28px 36px 18px;
  margin: 0;
}
.activity-dialog .el-dialog__title {
  font-size: 22px;
  font-weight: 700;
  color: white;
}
.activity-dialog .el-dialog__body {
  padding: 36px;
  background: #fff;
}
.activity-dialog .el-dialog__footer {
  padding: 22px 36px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
}

/* 按钮极简主色点缀 */
.cancel-btn {
  border-radius: 10px;
  padding: 12px 28px;
  font-weight: 500;
  border: 2px solid #e4e7ed;
  background: white;
  color: #606266;
  transition: all 0.3s ease;
}
.cancel-btn:hover {
  border-color: #a18cd1;
  background: #f7f8fa;
  color: #a18cd1;
}
.submit-btn {
  border-radius: 10px;
  padding: 12px 28px;
  font-weight: 500;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(161,140,209,0.13);
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(161,140,209,0.18);
  background: linear-gradient(135deg, #fbc2eb 0%, #a18cd1 100%);
}
.submit-btn:disabled {
  background: #c0c4cc;
  transform: none;
  box-shadow: none;
}

/* icon主色统一 */
.el-icon-location, .el-icon-user, .el-icon-time, .el-icon-upload, .el-icon-plus, .el-icon-check, .el-icon-collection {
  color: #a18cd1 !important;
  font-size: 18px !important;
}

/* 上传按钮样式 */
.upload-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.upload-btn {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
  color: white !important;
  font-weight: 500;
  border-radius: 8px;
  padding: 12px 20px;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.upload-btn:active {
  background-color: #3a8ee6 !important;
  border-color: #3a8ee6 !important;
  transform: translateY(0);
}

.upload-btn .el-icon-upload {
  margin-right: 6px;
  font-size: 16px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .activity-dialog .el-dialog {
    width: 98% !important;
    margin: 12px auto;
  }
  .activity-dialog .el-dialog__body {
    padding: 16px;
  }
  .activity-form {
    padding: 10px;
  }
  .dialog-footer {
    flex-direction: column;
    gap: 10px;
  }
  .cancel-btn,
  .submit-btn {
    width: 100%;
  }
  .search-box {
    width: 98vw;
    min-width: 0;
    padding: 0 6px;
  }
  .activity-card {
    padding: 16px 8px 12px 8px;
    margin-bottom: 18px;
    border-radius: 14px;
  }
}

.uploaded-image {
  width: 200px;
  height: 150px;
  max-width: 500px;
  max-height: 360px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
  display: inline-block;
}

/* 成员列表对话框样式 */
.participant-dialog .el-dialog {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.10);
  overflow: hidden;
}

.participant-dialog .el-dialog__header {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  color: white;
  padding: 24px 32px 16px;
  margin: 0;
}

.participant-dialog .el-dialog__title {
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.participant-dialog .el-dialog__body {
  padding: 24px;
  background: #fff;
}

.participant-list {
  max-height: 500px;
  overflow-y: auto;
}

.participant-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.participant-header h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.participant-count {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.empty-participants {
  text-align: center;
  padding: 40px 0;
}

.participant-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.participant-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
  transition: all 0.2s ease;
}

.participant-item:hover {
  background: #f1f3f4;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.participant-avatar {
  margin-right: 16px;
}

.participant-info {
  flex: 1;
  min-width: 0;
}

.participant-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.participant-join-time {
  font-size: 13px;
  color: #666;
}

.participant-status {
  margin-left: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .participant-dialog .el-dialog {
    width: 95% !important;
    margin: 12px auto;
  }
  
  .participant-dialog .el-dialog__body {
    padding: 16px;
  }
  
  .participant-item {
    padding: 12px;
  }
  
  .participant-avatar .el-avatar {
    width: 40px !important;
    height: 40px !important;
  }
}
</style>
