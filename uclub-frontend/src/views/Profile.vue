<template>
  <div class="profile-container">
    <!-- 未登录状态 -->
    <div v-if="!isLoggedIn" class="not-logged-in">
      <el-card class="login-card">
        <div class="login-header">
          <el-avatar :size="80" :src="defaultAvatar" />
          <h2>请先登录</h2>
          <p>登录后查看您的个人信息</p>
        </div>
        <el-button type="primary" size="large" @click="openLoginDialog">
          立即登录
        </el-button>
      </el-card>
    </div>

    <!-- 已登录状态 -->
    <div v-else class="profile-layout">
      <!-- 左侧菜单 -->
      <div class="profile-sidebar">
        <el-card class="sidebar-card">
          <!-- 用户头像和基本信息 -->
          <div class="user-profile">
            <el-avatar 
              :size="80" 
              :src="userAvatarUrl" 
              @error="handleAvatarError"
            />
            <h3>{{ userInfo.nickname || userInfo.account || '用户' }}</h3>
            <p>{{ userInfo.email }}</p>
            <el-tag :type="getStatusType(userInfo.status)" size="small">
              {{ userInfo.status || '正常' }}
            </el-tag>
          </div>
          
          <!-- 菜单列表 -->
          <el-menu 
            :default-active="activeMenu" 
            class="profile-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="clubs">
              <el-icon><Message /></el-icon>
              <span>我的社团</span>
            </el-menu-item>
            <el-menu-item index="activities">
              <el-icon><Key /></el-icon>
              <span>我的活动</span>
            </el-menu-item>
            <el-menu-item index="settings">
              <el-icon><Lock /></el-icon>
              <span>账户设置</span>
            </el-menu-item>
            <el-menu-item index="recent">
              <el-icon><SwitchButton /></el-icon>
              <span>最近活动</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </div>

      <!-- 右侧内容区 -->
      <div class="profile-content">
        <!-- 个人信息 -->
        <div v-if="activeMenu === 'profile'" class="content-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>个人信息</span>
                <div class="header-actions">
                  <el-button type="primary" size="small" @click="showAvatarUpload = true">
                    更换头像
                  </el-button>
                  <el-button type="primary" size="small" @click="editUserInfo">
                    编辑信息
                  </el-button>
                </div>
              </div>
            </template>
            
            <el-table :data="userTableData" style="width: 100%">
              <el-table-column prop="field" label="字段" width="150"></el-table-column>
              <el-table-column prop="value" label="值"></el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag 
                    :type="getStatusType(scope.row.status)" 
                    v-if="scope.row.status"
                  >
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 我的社团 -->
        <div v-if="activeMenu === 'clubs'" class="content-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的社团</span>
              </div>
            </template>
            <div v-if="myClubs.length > 0">
              <el-row :gutter="20">
                <el-col :span="8" v-for="club in pagedClubs" :key="club.id">
                  <el-card class="club-card" shadow="hover">
                    <img :src="club.logoUrl || defaultAvatar" class="club-logo" />
                    <h4>{{ club.name }}</h4>
                    <p>{{ club.description }}</p>
                    <el-tag :type="getClubRoleType(club.role)" size="small">
                      {{ club.role }}
                    </el-tag>
                  </el-card>
                </el-col>
              </el-row>
              <el-pagination
                v-if="myClubs.length > clubsPageSize"
                :current-page="clubsPage"
                :page-size="clubsPageSize"
                :total="myClubs.length"
                @current-change="handleClubsPageChange"
                layout="prev, pager, next"
                style="text-align: center; margin-top: 20px;"
              ></el-pagination>
            </div>
            <el-empty v-else description="暂无加入的社团" />
          </el-card>
        </div>

        <!-- 我的活动 -->
        <div v-if="activeMenu === 'activities'" class="content-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的活动</span>
              </div>
            </template>
            <div v-if="myActivities.length > 0">
              <el-table :data="pagedActivities" style="width: 100%">
                <el-table-column prop="title" label="活动名称" width="200"></el-table-column>
                <el-table-column prop="description" label="活动描述"></el-table-column>
                <el-table-column prop="location" label="活动地点" width="150"></el-table-column>
                <el-table-column prop="startTime" label="开始时间" width="180"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getActivityStatusType(scope.row.status)">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-if="myActivities.length > activitiesPageSize"
                :current-page="activitiesPage"
                :page-size="activitiesPageSize"
                :total="myActivities.length"
                @current-change="handleActivitiesPageChange"
                layout="prev, pager, next"
                style="text-align: center; margin-top: 20px;"
              ></el-pagination>
            </div>
            <el-empty v-else description="暂无参与的活动" />
          </el-card>
        </div>

        <!-- 账户设置 -->
        <div v-if="activeMenu === 'settings'" class="content-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>账户设置</span>
              </div>
            </template>
            <el-form label-width="120px">
              <el-form-item label="修改密码">
                <el-button type="primary" @click="showChangePassword = true">
                  修改密码
                </el-button>
              </el-form-item>
              <el-form-item label="隐私设置">
                <el-button type="primary" @click="showPrivacySettings = true">
                  隐私设置
                </el-button>
              </el-form-item>
              <el-form-item label="通知设置">
                <el-button type="primary" @click="showNotificationSettings = true">
                  通知设置
                </el-button>
              </el-form-item>
            </el-form>
            <el-button type="danger" style="width: 100%; margin-top: 20px;" @click="handleLogout">退出登录</el-button>
          </el-card>
        </div>

        <!-- 最近活动 -->
        <div v-if="activeMenu === 'recent'" class="content-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最近活动</span>
              </div>
            </template>
            <div v-if="recentActivities.length > 0">
              <div 
                v-for="activity in pagedRecentActivities" 
                :key="activity.id" 
                class="activity-item"
              >
                <div class="activity-icon">
                  <el-icon><Message /></el-icon>
                </div>
                <div class="activity-content">
                  <h4>{{ activity.title }}</h4>
                  <p>{{ activity.description }}</p>
                  <span class="activity-time">{{ formatDate(activity.createdAt) }}</span>
                </div>
              </div>
              <el-pagination
                v-if="recentActivities.length > recentPageSize"
                :current-page="recentPage"
                :page-size="recentPageSize"
                :total="recentActivities.length"
                @current-change="handleRecentPageChange"
                layout="prev, pager, next"
                style="text-align: center; margin-top: 20px;"
              ></el-pagination>
            </div>
            <el-empty v-else description="暂无最近活动" />
          </el-card>
        </div>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog 
      v-model="showAvatarUpload" 
      title="更换头像" 
      width="400px"
    >
      <div class="avatar-upload">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :on-error="handleAvatarUploadError"
          :before-upload="beforeAvatarUpload"
          :http-request="customUpload"
        >
          <img v-if="avatarUrl" :src="getFullAvatarUrl(avatarUrl)" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tips">
          <p>支持 JPG、PNG、GIF 格式，文件大小不超过 2MB</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarUpload = false">取消</el-button>
          <el-button type="primary" @click="confirmAvatarUpload">
            确认上传
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑用户信息对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      title="编辑个人信息" 
      width="500px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="账号">
          <el-input v-model="editForm.account" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" disabled>
            <el-option label="普通用户" value="普通用户" />
            <el-option label="社团管理员" value="社团管理员" />
            <el-option label="系统管理员" value="系统管理员" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" disabled>
            <el-option label="正常" value="正常" />
            <el-option label="禁言" value="禁言" />
            <el-option label="封禁" value="封禁" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="saveUserInfo">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog 
      v-model="showChangePassword" 
      title="修改密码" 
      width="400px"
      @close="resetChangePasswordForm"
    >
      <el-form :model="changePasswordForm" label-width="100px">
        <el-form-item label="旧密码">
          <el-input v-model="changePasswordForm.oldPassword" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="changePasswordForm.newPassword" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="changePasswordForm.confirmNewPassword" type="password" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showChangePassword = false">取消</el-button>
          <el-button type="primary" :loading="changePasswordLoading" @click="handleChangePassword">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, SwitchButton, Message, Key, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { 
  getProfileInfo, 
  updateProfile, 
  changePassword, 
  getMyClubs, 
  getMyActivities, 
  getRecentActivities,
  uploadAvatar 
} from '../api/profileApi'

const router = useRouter()
const store = useStore()

// 响应式数据
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const showAvatarUpload = ref(false)
const showEditDialog = ref(false)
const showChangePassword = ref(false)
const avatarUrl = ref('')
const activeMenu = ref('profile')

// 编辑表单
const editForm = ref({
  account: '',
  email: '',
  nickname: '',
  role: '',
  status: ''
})

// 修改密码表单
const changePasswordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: ''
})

const changePasswordLoading = ref(false)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 用户信息
const userInfo = computed(() => store.getters.currentUser || {})

// Token状态
const tokenStatus = computed(() => {
  try {
    const token = localStorage.getItem('token')
    return {
      exists: !!token,
      length: token ? token.length : 0
    }
  } catch (error) {
    return {
      exists: false,
      length: 0
    }
  }
})

// 用户头像URL
const userAvatarUrl = computed(() => {
  if (userInfo.value.headUrl) {
    // 如果是完整的URL（以http开头），直接使用
    if (userInfo.value.headUrl.startsWith('http')) {
      return userInfo.value.headUrl
    }
    // 如果是相对路径，添加后端域名
    if (userInfo.value.headUrl.startsWith('/')) {
      return `http://localhost:8080${userInfo.value.headUrl}`
    }
    // 其他情况，添加完整路径
    return `http://localhost:8080/uploads/avatars/${userInfo.value.headUrl}`
  }
  return defaultAvatar
})

// 用户表格数据
const userTableData = computed(() => [
  {
    field: '用户ID',
    value: userInfo.value.id || '未知',
    status: null
  },
  {
    field: '账号',
    value: userInfo.value.account || '未知',
    status: null
  },
  {
    field: '邮箱',
    value: userInfo.value.email || '未知',
    status: null
  },
  {
    field: '昵称',
    value: userInfo.value.nickname || '未设置',
    status: null
  },
  {
    field: '角色',
    value: userInfo.value.role || '普通用户',
    status: userInfo.value.role || '普通用户'
  },
  {
    field: '账号状态',
    value: userInfo.value.status || '正常',
    status: userInfo.value.status || '正常'
  },
  {
    field: '注册时间',
    value: formatDate(userInfo.value.createdAt) || '未知',
    status: null
  }
])

// 我的社团数据
const myClubs = ref([])

// 我的活动数据
const myActivities = ref([])

// 最近活动
const recentActivities = ref([])

// 分页相关
const clubsPage = ref(1)
const clubsPageSize = ref(6)
const activitiesPage = ref(1)
const activitiesPageSize = ref(6)
const recentPage = ref(1)
const recentPageSize = ref(6)

// 计算当前页数据
const pagedClubs = computed(() => {
  const start = (clubsPage.value - 1) * clubsPageSize.value
  return myClubs.value.slice(start, start + clubsPageSize.value)
})
const pagedActivities = computed(() => {
  const start = (activitiesPage.value - 1) * activitiesPageSize.value
  return myActivities.value.slice(start, start + activitiesPageSize.value)
})
const pagedRecentActivities = computed(() => {
  const start = (recentPage.value - 1) * recentPageSize.value
  return recentActivities.value.slice(start, start + recentPageSize.value)
})

// 上传相关
const uploadUrl = computed(() => {
  const baseURL = 'http://localhost:8080'
  return `${baseURL}/api/profile/upload/avatar`
})

const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
}))

// 方法
const openLoginDialog = () => {
  window.dispatchEvent(new CustomEvent('showLoginDialog'))
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
  // 根据菜单加载相应数据
  switch (index) {
    case 'clubs':
      fetchMyClubs()
      break
    case 'activities':
      fetchMyActivities()
      break
    case 'recent':
      fetchRecentActivities()
      break
  }
}

const handleAvatarError = () => {
  ElMessage.error('头像加载失败')
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG、PNG、GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const customUpload = async (options) => {
  try {
    const token = localStorage.getItem('token')
    console.log('开始自定义上传，文件:', options.file)
    console.log('Token:', token)
    console.log('Token长度:', token ? token.length : 0)
    
    if (!token) {
      ElMessage.error('未找到登录token，请重新登录')
      options.onError(new Error('未找到登录token'))
      return
    }
    
    const formData = new FormData()
    formData.append('file', options.file)
    
    console.log('发送请求到:', uploadUrl.value)
    console.log('请求头:', {
      'Authorization': `Bearer ${token}`
    })
    
    const response = await fetch(uploadUrl.value, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
    
    console.log('上传响应状态:', response.status)
    console.log('响应头:', Object.fromEntries(response.headers.entries()))
    
    if (response.ok) {
      const result = await response.json()
      console.log('上传成功:', result)
      options.onSuccess(result)
    } else {
      const errorText = await response.text()
      console.error('上传失败:', response.status, errorText)
      ElMessage.error(`上传失败: ${response.status} - ${errorText}`)
      options.onError(new Error(`上传失败: ${response.status}`))
    }
  } catch (error) {
    console.error('上传异常:', error)
    ElMessage.error(`上传异常: ${error.message}`)
    options.onError(error)
  }
}

const handleAvatarSuccess = (response) => {
  console.log('头像上传响应:', response)
  if (response.code === 200) {
    avatarUrl.value = response.data.url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

const handleAvatarUploadError = (error) => {
  console.error('头像上传错误:', error)
  ElMessage.error('头像上传失败，请重试')
}

const confirmAvatarUpload = async () => {
  if (!avatarUrl.value) {
    ElMessage.error('请先上传头像')
    return
  }
  
  try {
    const res = await updateProfile({
      headUrl: avatarUrl.value
    })
    
    if (res.data.code === 200) {
      ElMessage.success('头像更新成功')
      showAvatarUpload.value = false
      
      // 更新本地用户信息
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.headUrl = avatarUrl.value
      localStorage.setItem('user', JSON.stringify(user))
      store.dispatch('login', user)
      
      // 重新获取用户信息以确保数据同步
      try {
        const profileRes = await getProfileInfo()
        if (profileRes.data.code === 200) {
          const updatedUser = profileRes.data.data
          localStorage.setItem('user', JSON.stringify(updatedUser))
          store.dispatch('login', updatedUser)
        }
      } catch (error) {
        console.error('重新获取用户信息失败:', error)
      }
      
      // 清空临时头像URL
      avatarUrl.value = ''
      
      // 强制刷新页面以确保头像正确显示
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } else {
      ElMessage.error(res.data.message || '头像更新失败')
    }
  } catch (error) {
    ElMessage.error('头像更新失败')
    console.error('更新头像错误:', error)
  }
}

const getStatusType = (status) => {
  switch (status) {
    case '正常':
      return 'success'
    case '禁言':
      return 'warning'
    case '封禁':
      return 'danger'
    case '普通用户':
      return 'info'
    case '社团管理员':
      return 'warning'
    case '系统管理员':
      return 'danger'
    default:
      return 'info'
  }
}

const getClubRoleType = (role) => {
  switch (role) {
    case '社长':
      return 'danger'
    case '副社长':
      return 'warning'
    case '干事':
      return 'info'
    case '成员':
      return 'success'
    default:
      return 'info'
  }
}

const getActivityStatusType = (status) => {
  switch (status) {
    case '通过':
      return 'success'
    case '待审核':
      return 'warning'
    case '拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

const editUserInfo = () => {
  editForm.value = {
    account: userInfo.value.account || '',
    email: userInfo.value.email || '',
    nickname: userInfo.value.nickname || '',
    role: userInfo.value.role || '普通用户',
    status: userInfo.value.status || '正常'
  }
  showEditDialog.value = true
}

const saveUserInfo = async () => {
  try {
    const res = await updateProfile({
      nickname: editForm.value.nickname
    })
    
    if (res.data.code === 200) {
      ElMessage.success('信息更新成功')
      showEditDialog.value = false
      // 更新本地用户信息
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.nickname = editForm.value.nickname
      localStorage.setItem('user', JSON.stringify(user))
      store.dispatch('login', user)
    } else {
      ElMessage.error(res.data.message || '信息更新失败')
    }
  } catch (error) {
    ElMessage.error('信息更新失败')
    console.error('更新用户信息错误:', error)
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取我的社团
const fetchMyClubs = async () => {
  try {
    const response = await getMyClubs()
    
    if (response.data.code === 200) {
      myClubs.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取我的社团失败:', error)
  }
}

// 获取我的活动
const fetchMyActivities = async () => {
  try {
    const response = await getMyActivities()
    
    if (response.data.code === 200) {
      myActivities.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取我的活动失败:', error)
  }
}

// 获取最近活动
const fetchRecentActivities = async () => {
  try {
    const response = await getRecentActivities()
    
    if (response.data.code === 200) {
      recentActivities.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取最近活动失败:', error)
  }
}

const getFullAvatarUrl = (url) => {
  if (!url) return defaultAvatar
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return `http://localhost:8080${url}`
  return `http://localhost:8080/uploads/avatars/${url}`
}

const handleClubsPageChange = (newPage) => {
  clubsPage.value = newPage
  fetchMyClubs()
}

const handleActivitiesPageChange = (newPage) => {
  activitiesPage.value = newPage
  fetchMyActivities()
}

const handleRecentPageChange = (newPage) => {
  recentPage.value = newPage
  fetchRecentActivities()
}

const resetChangePasswordForm = () => {
  changePasswordForm.value.oldPassword = ''
  changePasswordForm.value.newPassword = ''
  changePasswordForm.value.confirmNewPassword = ''
}

const handleChangePassword = async () => {
  if (
    !changePasswordForm.value.oldPassword.trim() ||
    !changePasswordForm.value.newPassword.trim() ||
    !changePasswordForm.value.confirmNewPassword.trim()
  ) {
    ElMessage.error('请填写所有字段')
    return
  }
  if (changePasswordForm.value.newPassword !== changePasswordForm.value.confirmNewPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  if (changePasswordForm.value.newPassword.length < 6) {
    ElMessage.error('新密码长度不能少于6位')
    return
  }
  changePasswordLoading.value = true
  try {
    const res = await changePassword({
      oldPassword: changePasswordForm.value.oldPassword,
      newPassword: changePasswordForm.value.newPassword,
      confirmPassword: changePasswordForm.value.confirmNewPassword
    })
    if (res.data.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      showChangePassword.value = false
      resetChangePasswordForm()
      setTimeout(() => {
        window.location.reload()
      }, 1500)
    } else {
      ElMessage.error(res.data.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error('密码修改失败')
    console.error('修改密码错误:', error)
  } finally {
    changePasswordLoading.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  store.dispatch('logout')
  ElMessage.success('已退出登录')
  setTimeout(() => {
    window.location.reload()
  }, 800)
}

onMounted(() => {
  if (isLoggedIn.value) {
    fetchRecentActivities()
  }
})
</script>

<style scoped>
.profile-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.not-logged-in {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.login-card {
  text-align: center;
  padding: 40px;
}

.login-header {
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 20px 0 10px;
  color: #333;
}

.login-header p {
  color: #666;
  margin: 0;
}

.profile-layout {
  display: flex;
  gap: 20px;
  min-height: 600px;
}

.profile-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.sidebar-card {
  height: 100%;
}

.user-profile {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.user-profile h3 {
  margin: 15px 0 5px 0;
  color: #333;
}

.user-profile p {
  margin: 5px 0 10px 0;
  color: #666;
  font-size: 14px;
}

.profile-menu {
  border: none;
}

.profile-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
}

.profile-content {
  flex: 1;
  min-width: 0;
}

.content-section {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.club-card {
  text-align: center;
  margin-bottom: 20px;
}

.club-logo {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-bottom: 10px;
}

.club-card h4 {
  margin: 10px 0 5px 0;
  color: #333;
}

.club-card p {
  margin: 5px 0 10px 0;
  color: #666;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  margin-right: 15px;
  color: #409EFF;
  margin-top: 2px;
}

.activity-content h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 16px;
}

.activity-content p {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 14px;
}

.activity-time {
  color: #999;
  font-size: 12px;
}

.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  margin: 0 auto;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.upload-tips {
  margin-top: 10px;
  color: #666;
  font-size: 12px;
}

.upload-tips p {
  margin: 0;
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
}
</style>