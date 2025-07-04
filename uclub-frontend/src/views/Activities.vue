<template>
  <div class="activities-container">
     <!-- Banner -->
        <div class="banner">
            <img src="../assets/ABack.jpg" class="banner-img" />
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
            v-for="activity in filteredActivities" 
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
        <p>请填写活动的基本信息，带 * 的为必填项</p>
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
                :height="200"
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
            <el-form-item label="所属社团" prop="clubId" v-if="clubList.length > 0" class="form-item-highlight">
              <el-select 
                v-model="activityForm.clubId" 
                placeholder="请选择所属社团"
                class="custom-select"
                size="large"
                style="width: 100%"
              >
                <el-option 
                  v-for="club in (clubList.value || []).filter(c => ['干事', '副社长', '社长'].includes(c.myRole))" 
                  :key="club.id" 
                  :label="club.name" 
                  :value="club.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动图片" prop="imageUrl">
              <div class="upload-placeholder" @click="triggerFileInput">
                <i class="el-icon-upload"></i>
                <span>上传图片</span>
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
            :disabled="clubList.length === 0"
            class="submit-btn"
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
                :height="200"
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
              <div class="upload-placeholder" @click="triggerFileInput">
                <i class="el-icon-upload"></i>
                <span>上传图片</span>
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

// 获取用户社团列表
const fetchUserClubs = async () => {
  if (!isLoggedIn.value) return
  try {
    const response = await request.get(`/api/clubs/creator/${userInfo.value.id}`)
    if (response.data.code === 0) {
      clubList.value = (response.data.data || []).map(c => ({
        ...c,
        myRole: c.role || c.rol
      }))
    }
  } catch (error) {
    clubList.value = []
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
      ElMessage.error('删除活动失败')
    }
  }
}

// 提交创建活动
const submitActivity = async () => {
  activityForm.value.description = window.$(activityDescRef.value).summernote('code')
  try {
    await activityFormRef.value.validate()
    
    // 检查用户是否登录
    if (!isLoggedIn.value) {
      ElMessage.error('请先登录')
      return
    }
    
    // 检查用户信息
    if (!userInfo.value?.id) {
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
    ElMessage.error('创建活动失败，请检查表单数据')
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
  
  if (isLoggedIn.value) {
    await fetchUserClubs()
  }
})

// 提交编辑
const submitEdit = async () => {
  editForm.value.description = window.$(editDescRef.value).summernote('code')
  try {
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

// 在<script setup>中添加图片URL拼接方法
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8080' + url
}

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
  // 1. 从 userClubs 查找
  const club = clubList.value.find(c => c.id === clubId)
  if (club) return club.name
  // 2. 从 activities 查找
  const activity = activities.value.find(a => a.clubId === clubId && a.clubName)
  if (activity) return activity.clubName
  // 3. 从 allClubs 查找
  const allClub = allClubs.value.find(c => c.id === clubId)
  if (allClub) return allClub.name
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
</script>

<style scoped>
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




.activities-container {
  padding: 20px;
  background: #87CEEB;

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
  width: 600px;
  margin: 24px auto 20px auto;
  display: flex;
  justify-content: center;
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
  -webkit-line-clamp: 8;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 80px;
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
  background: #87CEEB;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
  background: #87CEEB;
}

.detail-header h2 {
  margin: 0;
  color: #303133;
  background: #87CEEB;
}

.detail-status {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  background: #87CEEB;
}

.detail-description {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
  margin-bottom: 20px;
  background: #87CEEB;
}

.detail-info {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  background: #87CEEB;

}

.info-row {
  display: flex;
  margin-bottom: 12px;
  background: #87CEEB;
}

.info-row:last-child {
  margin-bottom: 0;
  background: #87CEEB;
}

.info-row .label {
  font-weight: bold;
  color: #303133;
  width: 100px;
  flex-shrink: 0;
  background: #87CEEB;
}

.detail-actions {
  margin-top: 20px;
  text-align: center;
  background: #87CEEB;
}

.detail-actions .el-button {
  margin: 0 10px;
  background: #87CEEB;
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
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.activity-dialog .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px 30px 20px;
  margin: 0;
}

.activity-dialog .el-dialog__title {
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.activity-dialog .el-dialog__body {
  padding: 30px;
  background: #fafbfc;
}

.activity-dialog .el-dialog__footer {
  padding: 20px 30px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.dialog-header {
  margin-bottom: 24px;
  text-align: center;
}

.dialog-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.dialog-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.activity-form {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.form-item-highlight .el-form-item__label {
  font-weight: 600;
  color: #303133;
}

.form-item-highlight .el-form-item__label::before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
}

.custom-input .el-input__wrapper {
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
  box-shadow: none;
}

.custom-input .el-input__wrapper:hover {
  border-color: #409eff;
}

.custom-input .el-input__wrapper.is-focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.custom-input-number .el-input-number__decrease,
.custom-input-number .el-input-number__increase {
  border-radius: 6px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
}

.custom-date-picker .el-input__wrapper {
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
  box-shadow: none;
}

.custom-date-picker .el-input__wrapper:hover {
  border-color: #409eff;
}

.custom-date-picker .el-input__wrapper.is-focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.custom-select .el-input__wrapper {
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
  box-shadow: none;
}

.custom-select .el-input__wrapper:hover {
  border-color: #409eff;
}

.custom-select .el-input__wrapper.is-focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.custom-alert {
  border-radius: 8px;
  border: none;
  background: #fdf6ec;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 500;
  border: 2px solid #e4e7ed;
  background: white;
  color: #606266;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  border-color: #c0c4cc;
  background: #f5f7fa;
  color: #303133;
}

.submit-btn {
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  background: #c0c4cc;
  transform: none;
  box-shadow: none;
}

.uploaded-image {
  width: 240px;
  height: 120px;
  border-radius: 12px;
  border: 3px solid #409eff;
  object-fit: cover;
  display: block;
  margin: 0 auto;
  transition: all 0.3s ease;
}

.uploaded-image:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.2);
}

.upload-placeholder {
  width: 160px;
  height: 40px;
  border: 1px solid #d9ecff;
  border-radius: 8px;
  background: #f4faff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 0 auto;
  cursor: pointer;
  color: #409eff;
  font-size: 16px;
  font-weight: 500;
  transition: border-color 0.2s, background 0.2s;
}
.upload-placeholder:hover {
  border-color: #409eff;
  background: #eaf3ff;
}
.upload-placeholder i {
  font-size: 20px;
}

/* 富文本编辑器美化 */
.ql-editor {
  min-height: 200px !important;
  max-height: 200px !important;
  overflow-y: auto !important;
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
}

.ql-editor:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.ql-toolbar {
  border-radius: 8px 8px 0 0;
  border: 2px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  background: #fafbfc;
}

.ql-container {
  border-radius: 0 0 8px 8px;
  border: 2px solid #e4e7ed;
  border-top: 1px solid #e4e7ed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-dialog .el-dialog {
    width: 95% !important;
    margin: 20px auto;
  }
  
  .activity-dialog .el-dialog__body {
    padding: 20px;
  }
  
  .activity-form {
    padding: 16px;
  }
  
  .dialog-footer {
    flex-direction: column;
  }
  
  .cancel-btn,
  .submit-btn {
    width: 100%;
  }
}

/* 覆盖 el-upload 的边框和背景 */
.avatar-uploader.activity-upload-highlight .el-upload,
.avatar-uploader.activity-upload-highlight .el-upload-dragger {
  border: none !important;
  background: none !important;
  box-shadow: none !important;
  outline: none !important;
}
</style>
