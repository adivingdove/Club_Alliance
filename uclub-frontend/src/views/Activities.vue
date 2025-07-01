<template>
  <div class="activities-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>社团活动</h1>
      <p>发现精彩活动，参与社团生活</p>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索活动标题或描述"
          prefix-icon="Search"
          clearable
          @input="handleSearch"
          @clear="handleSearch"
        />
      </div>
      
      <div class="filter-tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabChange">
          <el-tab-pane label="全部活动" name="all"></el-tab-pane>
          <el-tab-pane label="即将开始" name="upcoming"></el-tab-pane>
          <el-tab-pane label="我的活动" name="my"></el-tab-pane>
          <el-tab-pane label="待审核" name="pending" v-if="isAdmin"></el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 活动列表 -->
    <div class="activities-list">
      <!-- 调试信息：显示当前活动列表状态 -->
      <div v-if="isAdmin" style="font-size: 12px; color: #666; margin-bottom: 10px; padding: 10px; background: #f0f0f0; border-radius: 4px;">
        调试信息: 当前标签页={{activeTab}}, 活动总数={{activities.length}}, 过滤后数量={{filteredActivities.length}}
        <br>
        活动状态列表: {{activities.map(a => `${a.id}:${a.title}:${a.applyStatus}`).join(', ')}}
      </div>
      
      <el-row :gutter="24">
        <el-col 
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="6" 
          v-for="activity in pagedActivities" 
          :key="activity.id"
        >
          <el-card 
            class="activity-card" 
            :class="{ 'pending': activity.applyStatus === '待审核' }"
            @click="viewActivityDetail(activity)"
          >
            <div class="activity-header">
              <div class="activity-status" :class="getStatusClass(activity.applyStatus)">
                {{ getStatusText(activity.applyStatus) }}
              </div>
              <div class="activity-time">
                <i class="el-icon-time"></i>
                {{ formatDate(activity.startTime) }}
              </div>
            </div>
            
            <div class="activity-content">
              <h3 class="activity-title">{{ activity.title }}</h3>
              <p class="activity-description">{{ activity.description }}</p>
              
              <div class="activity-info">
                <div class="info-item">
                  <i class="el-icon-location"></i>
                  <span>{{ activity.location || '地点待定' }}</span>
                </div>
                <div class="info-item">
                  <i class="el-icon-user"></i>
                  <span>
                    {{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants ? activity.maxParticipants : '∞' }}人
                  </span>
                </div>
              </div>
            </div>
            
            <div class="activity-footer">
              <el-button 
                type="primary" 
                size="small" 
                @click.stop="viewActivityDetail(activity)"
              >
                查看详情
              </el-button>
              
              <!-- 参与/退出按钮 -->
              <el-button 
                v-if="isLoggedIn && activity.applyStatus === '通过' && !canEditActivity(activity)"
                :type="activity.isParticipating ? 'danger' : 'success'"
                size="small" 
                @click.stop="activity.isParticipating ? leaveActivityHandler(activity) : joinActivityHandler(activity)"
                :disabled="!canJoinActivity(activity)"
              >
                {{ activity.isParticipating ? '退出活动' : '加入活动' }}
              </el-button>
              
              <!-- 如果活动状态不是"通过"，显示状态信息 -->
              <div v-if="isLoggedIn && activity.applyStatus !== '通过'" style="font-size: 12px; color: #999; margin-top: 5px;">
                活动状态: {{ getStatusText(activity.applyStatus) }}
              </div>
              
              <!-- 如果用户是活动创建者，显示提示 -->
              <div v-if="isLoggedIn && canEditActivity(activity)" style="font-size: 12px; color: #409EFF; margin-top: 5px;">
                您是活动创建者
              </div>
              
              <el-button 
                v-if="canEditActivity(activity)" 
                type="warning" 
                size="small" 
                @click.stop="editActivity(activity)"
              >
                编辑
              </el-button>
              <el-button 
                v-if="canDeleteActivity(activity)" 
                type="danger" 
                size="small" 
                @click.stop="deleteActivityHandler(activity)"
              >
                删除
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 空状态 -->
      <div v-if="pagedActivities.length === 0" class="empty-state">
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
    >
      <i class="el-icon-plus"></i>
    </el-button>

    <!-- 创建活动对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="创建活动" 
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
        
        <el-form-item label="所属社团" prop="clubId" v-if="userClubs.length > 0">
          <el-select v-model="activityForm.clubId" placeholder="请选择所属社团">
            <el-option 
              v-for="club in userClubs" 
              :key="club.id" 
              :label="club.name" 
              :value="club.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="userClubs.length === 0 && isLoggedIn">
          <el-alert
            title="您还没有创建任何社团"
            description="请先创建社团，然后才能发布活动"
            type="warning"
            show-icon
            :closable="false"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitActivity"
          :disabled="userClubs.length === 0"
        >
          创建活动
        </el-button>
      </template>
    </el-dialog>

    <!-- 活动详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="活动详情" 
      width="700px"
    >
      <div v-if="selectedActivity" class="activity-detail">
        <div class="detail-header">
          <h2>{{ selectedActivity.title }}</h2>
          <div class="detail-status" :class="getStatusClass(selectedActivity.applyStatus)">
            {{ getStatusText(selectedActivity.applyStatus) }}
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
        
        <div class="detail-actions" v-if="isAdmin && selectedActivity.applyStatus === '待审核'">
          <el-button type="success" @click="approveActivity(selectedActivity.id)">通过审核</el-button>
          <el-button type="danger" @click="rejectActivity(selectedActivity.id)">拒绝审核</el-button>
        </div>
        
        <div class="detail-actions" v-if="canEditActivity(selectedActivity)">
          <el-button type="primary" @click="editActivity(selectedActivity)">编辑活动</el-button>
        </div>
        
        <div class="detail-actions" v-if="isLoggedIn && selectedActivity.applyStatus === '通过' && !canEditActivity(selectedActivity)">
          <el-button 
            :type="selectedActivity.isParticipating ? 'danger' : 'success'"
            @click="selectedActivity.isParticipating ? leaveActivityHandler(selectedActivity) : joinActivityHandler(selectedActivity)"
            :disabled="!canJoinActivity(selectedActivity)"
          >
            {{ selectedActivity.isParticipating ? '退出活动' : '加入活动' }}
          </el-button>
        </div>
        
        <!-- 如果活动状态不是"通过"，显示状态信息 -->
        <div v-if="isLoggedIn && selectedActivity.applyStatus !== '通过'" class="detail-actions">
          <el-alert
            :title="`活动状态: ${getStatusText(selectedActivity.applyStatus)}`"
            :description="selectedActivity.applyStatus === '待审核' ? '活动正在等待管理员审核，审核通过后才能加入' : '活动已被拒绝，无法加入'"
            :type="selectedActivity.applyStatus === '待审核' ? 'warning' : 'error'"
            show-icon
            :closable="false"
          />
        </div>
        
        <!-- 如果用户是活动创建者，显示提示 -->
        <div v-if="isLoggedIn && canEditActivity(selectedActivity)" class="detail-actions">
          <el-alert
            title="您是活动创建者"
            description="您可以编辑和管理这个活动"
            type="info"
            show-icon
            :closable="false"
          />
        </div>
        
        <!-- 调试信息：显示详情对话框中的按钮显示条件 -->
        <div v-if="isLoggedIn" style="font-size: 10px; color: #999; margin-top: 10px; padding: 10px; background: #f5f5f5; border-radius: 4px;">
          调试信息: 登录={{isLoggedIn}}, 活动状态={{selectedActivity.applyStatus}}, 可编辑={{canEditActivity(selectedActivity)}}, 可加入={{canJoinActivity(selectedActivity)}}, 参与状态={{selectedActivity.isParticipating}}
        </div>
      </div>
    </el-dialog>

    <!-- 编辑活动对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      :title="`编辑活动 (ID: ${currentEditActivityId || '未知'})`" 
      width="600px"
    >
      <el-form 
        ref="editFormRef" 
        :model="editForm" 
        :rules="activityRules" 
        label-width="100px"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入活动标题" />
        </el-form-item>
        
        <el-form-item label="活动描述" prop="description">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入活动描述"
          />
        </el-form-item>
        
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="editForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="editForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="editForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number 
            v-model="editForm.maxParticipants" 
            :min="1" 
            placeholder="不填表示人数不限"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <el-pagination
      v-if="filteredActivities.length > recentPageSize"
      :current-page="recentPage"
      :page-size="recentPageSize"
      :total="filteredActivities.length"
      @current-change="handleRecentPageChange"
      layout="prev, pager, next"
      style="text-align: center; margin-top: 20px;"
    ></el-pagination>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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
  getActivityParticipantCount
} from '@/api/activityApi'
import request from '@/utils/request'

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
const userClubs = ref([])

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
    console.log('当前时间:', now.toISOString())
    
    filtered = filtered.filter(activity => {
      if (!activity.startTime) {
        console.log('活动缺少开始时间:', activity.title)
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
          console.error('无法解析活动时间:', activity.startTime)
          return false
        }
        
      } catch (error) {
        console.error('解析活动时间失败:', activity.startTime, error)
        return false
      }
      
      const isUpcoming = startTime > now
      console.log(`活动 "${activity.title}" 开始时间:`, startTime.toISOString(), '是否在未来:', isUpcoming)
      
      return isUpcoming
    })
    
    console.log('即将开始的活动数量:', filtered.length)
  }

  return filtered
})

// 获取活动列表
const fetchActivities = async () => {
  try {
    console.log('开始获取活动列表，当前标签页:', activeTab.value)
    let response
    
    switch (activeTab.value) {
      case 'upcoming':
        console.log('获取所有活动，然后在前端过滤即将开始的活动')
        response = await getAllActivities()
        break
      case 'my':
        if (isLoggedIn.value) {
          console.log('获取我的活动，用户ID:', userInfo.value.id)
          response = await getActivitiesByCreatorId(userInfo.value.id)
        } else {
          console.log('用户未登录，返回空列表')
          response = { data: { code: 0, data: [] } }
        }
        break
      case 'pending':
        if (isAdmin.value) {
          console.log('获取待审核活动')
          response = await getPendingActivities()
        } else {
          console.log('非管理员用户，返回空列表')
          response = { data: { code: 0, data: [] } }
        }
        break
      default:
        console.log('获取所有活动')
        response = await getAllActivities()
    }
    
    console.log('API响应:', response)
    
    if (response && response.data && response.data.code === 0) {
      activities.value = Array.isArray(response.data.data) ? response.data.data : []
      console.log('成功获取活动列表，数量:', activities.value.length)
      
      // 检查用户参与状态
      await checkUserParticipation()
    } else {
      console.error('API返回错误:', response)
      activities.value = []
      ElMessage.error('获取活动列表失败')
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
    console.error('错误详情:', error.response || error.message)
    activities.value = []
    ElMessage.error('获取活动列表失败，请检查网络连接')
  }
}

// 获取用户社团列表
const fetchUserClubs = async () => {
  if (!isLoggedIn.value) return
  
  try {
    console.log('获取用户社团，用户ID:', userInfo.value.id)
    const response = await request.get(`/api/clubs/creator/${userInfo.value.id}`)
    console.log('用户社团响应:', response)
    if (response.data.code === 0) {
      userClubs.value = response.data.data || []
      console.log('获取到用户社团数量:', userClubs.value.length)
    }
  } catch (error) {
    console.error('获取用户社团失败:', error)
    userClubs.value = []
  }
}

// 处理标签页切换
const handleTabChange = () => {
  fetchActivities()
}

// 处理搜索
const handleSearch = () => {
  // 实时搜索，不需要额外处理，computed会自动过滤
}

// 查看活动详情
const viewActivityDetail = (activity) => {
  selectedActivity.value = activity
  showDetailDialog.value = true
}

// 编辑活动
const editActivity = (activity) => {
  console.log('编辑活动:', activity)
  
  // 保存当前编辑的活动ID
  currentEditActivityId.value = activity.id
  
  // 填充编辑表单
  editForm.value = {
    title: activity.title,
    description: activity.description,
    location: activity.location,
    startTime: activity.startTime,
    endTime: activity.endTime,
    maxParticipants: activity.maxParticipants,
    clubId: activity.clubId
  }
  
  // 关闭详情对话框，打开编辑对话框
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
      console.error('删除活动失败:', error)
      ElMessage.error('删除活动失败')
    }
  }
}

// 提交创建活动
const submitActivity = async () => {
  try {
    await activityFormRef.value.validate()
    
    // 检查用户是否登录
    if (!isLoggedIn.value) {
      ElMessage.error('请先登录')
      return
    }
    
    // 检查用户信息
    if (!userInfo.value?.id) {
      console.error('用户信息不完整:', userInfo.value)
      ElMessage.error('用户信息不完整，请重新登录')
      return
    }
    
    // 检查表单数据
    console.log('表单数据:', activityForm.value)
    console.log('用户信息:', userInfo.value)
    console.log('用户ID:', userInfo.value.id)
    console.log('用户角色:', userInfo.value.role)
    
    // 添加创建者ID
    const activityData = {
      ...activityForm.value,
      creatorId: userInfo.value.id
    }
    
    console.log('提交的活动数据:', activityData)
    
    // 验证必需字段
    if (!activityData.title || !activityData.title.trim()) {
      ElMessage.error('活动标题不能为空')
      return
    }
    if (!activityData.clubId) {
      ElMessage.error('请选择所属社团')
      return
    }
    if (!activityData.startTime) {
      ElMessage.error('请选择开始时间')
      return
    }
    if (!activityData.endTime) {
      ElMessage.error('请选择结束时间')
      return
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
        clubId: null
      }
      fetchActivities()
    }
  } catch (error) {
    console.error('创建活动失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error('创建活动失败，请检查表单数据')
  }
}

// 审核活动
const approveActivity = async (activityId) => {
  try {
    console.log('开始审核活动，活动ID:', activityId, '状态: 通过')
    const response = await updateActivityApplyStatus(activityId, '通过')
    console.log('审核API响应:', response)
    
    if (response.data.code === 0) {
      ElMessage.success('审核通过')
      
      // 更新本地活动列表中的状态
      const activityIndex = activities.value.findIndex(a => a.id === activityId)
      if (activityIndex !== -1) {
        activities.value[activityIndex].applyStatus = '通过'
        console.log('本地活动状态已更新:', activities.value[activityIndex])
      }
      
      // 如果当前选中的活动是被审核的活动，也更新其状态
      if (selectedActivity.value && selectedActivity.value.id === activityId) {
        selectedActivity.value.applyStatus = '通过'
        console.log('选中活动状态已更新:', selectedActivity.value)
      }
      
      showDetailDialog.value = false
      
      // 重新获取活动列表以确保数据同步
      await fetchActivities()
    } else {
      ElMessage.error(response.data.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error('审核失败，请重试')
  }
}

const rejectActivity = async (activityId) => {
  try {
    console.log('开始审核活动，活动ID:', activityId, '状态: 拒绝')
    const response = await updateActivityApplyStatus(activityId, '拒绝')
    console.log('审核API响应:', response)
    
    if (response.data.code === 0) {
      ElMessage.success('已拒绝')
      
      // 更新本地活动列表中的状态
      const activityIndex = activities.value.findIndex(a => a.id === activityId)
      if (activityIndex !== -1) {
        activities.value[activityIndex].applyStatus = '拒绝'
        console.log('本地活动状态已更新:', activities.value[activityIndex])
      }
      
      // 如果当前选中的活动是被审核的活动，也更新其状态
      if (selectedActivity.value && selectedActivity.value.id === activityId) {
        selectedActivity.value.applyStatus = '拒绝'
        console.log('选中活动状态已更新:', selectedActivity.value)
      }
      
      showDetailDialog.value = false
      
      // 重新获取活动列表以确保数据同步
      await fetchActivities()
    } else {
      ElMessage.error(response.data.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
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
  console.log('组件挂载，检查用户状态...')
  console.log('登录状态:', isLoggedIn.value)
  console.log('用户信息:', userInfo.value)
  
  // 获取活动数据
  await fetchActivities()
  console.log('活动数据加载完成，活动数量:', activities.value.length)
  
  if (isLoggedIn.value) {
    await fetchUserClubs()
  }
})

// 提交编辑
const submitEdit = async () => {
  try {
    await editFormRef.value.validate()
    
    // 检查是否有当前编辑的活动ID
    if (!currentEditActivityId.value) {
      ElMessage.error('编辑活动ID不存在，请重新选择要编辑的活动')
      return
    }
    
    console.log('提交编辑数据:', editForm.value)
    console.log('编辑活动ID:', currentEditActivityId.value)
    
    const response = await updateActivity(currentEditActivityId.value, editForm.value)
    if (response.data.code === 0) {
      ElMessage.success('编辑活动成功')
      showEditDialog.value = false
      currentEditActivityId.value = null // 清空编辑ID
      fetchActivities() // 刷新活动列表
    } else {
      ElMessage.error(response.data.message || '编辑活动失败')
    }
  } catch (error) {
    console.error('编辑活动失败:', error)
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
  if (!activity.maxParticipants) return true // 人数不限
  return (activity.currentParticipants || 0) < activity.maxParticipants
}

// 加入活动
const joinActivityHandler = async (activity) => {
  try {
    if (!isLoggedIn.value) {
      ElMessage.error('请先登录')
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
    console.error('加入活动失败:', error)
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
    } else {
      ElMessage.error(response.data.message || '退出活动失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出活动失败:', error)
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

const recentPage = ref(1)
const recentPageSize = ref(8)
const handleRecentPageChange = (page) => {
  recentPage.value = page
}

const pagedActivities = computed(() => {
  const start = (recentPage.value - 1) * recentPageSize.value
  return filteredActivities.value.slice(start, start + recentPageSize.value)
})
</script>

<style scoped>
.activities-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
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
  margin-bottom: 20px;
}

.filter-tabs {
  border-bottom: 1px solid #e4e7ed;
}

.activities-list {
  min-height: 400px;
}

.activity-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.activity-card.pending {
  border-left: 4px solid #e6a23c;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.activity-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
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

.activity-time {
  font-size: 12px;
  color: #909399;
}

.activity-content {
  margin-bottom: 15px;
}

.activity-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.4;
}

.activity-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 12px;
  color: #909399;
}

.info-item i {
  margin-right: 5px;
}

.activity-footer {
  display: flex;
  gap: 8px;
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
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
  z-index: 1000;
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

.detail-actions {
  margin-top: 20px;
  text-align: center;
}

.detail-actions .el-button {
  margin: 0 10px;
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
</style>