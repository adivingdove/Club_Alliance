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
            <el-menu-item index="favorites">
              <el-icon><Star /></el-icon>
              <span>我的收藏</span>
            </el-menu-item>

            <el-menu-item index="posts">
              <el-icon><Document /></el-icon>
              <span>我的贴子</span>
            </el-menu-item>

            <el-menu-item index="history">
              <el-icon><Clock /></el-icon>
              <span>浏览历史</span>
            </el-menu-item>
            <el-menu-item index="recent">
              <el-icon><Timer /></el-icon>
              <span>最近活动</span>
            </el-menu-item>
            
            <!-- 分隔线 -->
            <el-divider style="margin: 10px 0;"></el-divider>
            
            <!-- 退出登录 -->
            <el-menu-item index="logout" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
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
                    更换昵称
                  </el-button>
                  <el-button type="primary" size="small" @click="showChangePassword = true">
                    修改密码
                  </el-button>
                  <el-button type="primary" size="small" @click="showChangeEmail = true">
                    更换邮箱
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
                <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="club in pagedClubs" :key="club.id" class="favorite-col">
                  <el-card shadow="hover" class="favorite-card" @click="goToClubDetail(club.id)">
                    <img :src="club.logoUrl || defaultAvatar" class="club-img" />
                    <div class="club-info">
                      <div class="club-title">{{ club.name }}</div>
                      <div class="club-desc">{{ club.description }}</div>
                      <div class="club-tags">
                        <el-tag :type="getClubRoleType(club.role)" size="small">
                          {{ club.role }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="club-actions" v-if="canQuitClub(club)">
                      <el-button 
                        type="danger" 
                        size="small" 
                        @click.stop="quitClub(club.id)"
                      >
                        退出社团
                      </el-button>
                    </div>
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
                <el-table-column prop="endTime" label="结束时间" width="180"></el-table-column>
                <el-table-column prop="activityStatus" label="活动状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getActivityStatusType(scope.row.activityStatus)">
                      {{ scope.row.activityStatus }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="participantCount" label="参与人数" width="100">
                  <template #default="scope">
                    {{ scope.row.participants ? scope.row.participants.length : (scope.row.participantCount || 0) }}
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

        <!-- 我的收藏 -->
        <div v-if="activeMenu === 'favorites'" class="content-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的收藏</span>
              </div>
            </template>
            <div v-if="favoriteClubs.length > 0">
              <el-row :gutter="20">
                <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="club in pagedFavoriteClubs" :key="club.id" class="favorite-col">
                  <el-card shadow="hover" class="favorite-card" @click="goToClubDetail(club.id)">
                    <img :src="club.logoUrl || defaultAvatar" class="club-img" />
                    <div class="club-info">
                      <div class="club-title">{{ club.name }}</div>
                      <div class="club-desc">{{ club.description }}</div>
                      <div class="club-tags">{{ club.tags }}</div>
                    </div>
                    <div class="club-actions">
                      <el-button type="danger" size="small" @click.stop="removeFromFavorites(club.id)">
                        取消收藏
                </el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
              <el-pagination
                v-if="favoriteClubs.length > favoritesPageSize"
                :current-page="favoritesPage"
                :page-size="favoritesPageSize"
                :total="favoriteClubs.length"
                @current-change="handleFavoritesPageChange"
                layout="prev, pager, next"
                style="text-align: center; margin-top: 20px;"
              ></el-pagination>
            </div>
            <el-empty v-else description="暂无收藏的社团" />
          </el-card>
        </div>

        <!-- 我的贴子 -->
        <div v-if="activeMenu === 'posts'" class="content-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的贴子</span>
              </div>
            </template>
            <div v-if="myPosts.length > 0">
              <div 
                v-for="post in pagedPosts" 
                :key="post.id" 
                class="history-item"
                @click="goToPost(post.id)"
              >
                <div class="history-content">
                  <div class="post-title">{{ post.title }}</div>
                  <div class="post-meta">
                    <span class="club-name">社团: {{ post.clubName }}</span>
                    <span class="post-time">发布时间: {{ formatDate(post.createdAt) }}</span>
                  </div>
                  <div class="post-preview">{{ post.content ? post.content.replace(/<[^>]+>/g, '').substring(0, 100) + '...' : '暂无内容' }}</div>
                </div>
                <div class="history-actions">
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click.stop="deletePost(post.id)"
                  >
                    删除
                </el-button>
                </div>
              </div>
              <el-pagination
                v-if="myPosts.length > postsPageSize"
                :current-page="postsPage"
                :page-size="postsPageSize"
                :total="myPosts.length"
                @current-change="handlePostsPageChange"
                layout="prev, pager, next"
                style="text-align: center; margin-top: 20px;"
              ></el-pagination>
            </div>
            <el-empty v-else description="暂无发布的贴子" />
          </el-card>
        </div>

        <!-- 浏览历史 -->
        <div v-if="activeMenu === 'history'" class="content-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>浏览历史</span>
                <div class="header-actions">
                  <el-button type="danger" size="small" @click="clearHistory">
                    清空历史
                </el-button>
                </div>
              </div>
            </template>
            <div v-if="browsingHistory.length > 0">
              <div 
                v-for="post in pagedHistory" 
                :key="post.id" 
                class="history-item"
                @click="goToPost(post.id)"
              >
                <div class="history-content">
                  <div class="post-title">{{ post.title }}</div>
                  <div class="post-meta">
                    <span class="author">作者: {{ post.author }}</span>
                    <span class="browse-time">浏览时间: {{ formatBrowseTime(post.browseTime) }}</span>
                  </div>
                  <div class="post-preview">{{ post.content ? post.content.replace(/<[^>]+>/g, '').substring(0, 100) + '...' : '暂无内容' }}</div>
                </div>
                <div class="history-actions">
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click.stop="removeHistoryItem(post.id)"
                  >
                    删除
                </el-button>
                </div>
              </div>
              <el-pagination
                v-if="browsingHistory.length > historyPageSize"
                :current-page="historyPage"
                :page-size="historyPageSize"
                :total="browsingHistory.length"
                @current-change="handleHistoryPageChange"
                layout="prev, pager, next"
                style="text-align: center; margin-top: 20px;"
              ></el-pagination>
            </div>
            <el-empty v-else description="暂无浏览历史" />
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
              <el-table :data="pagedRecentActivities" style="width: 100%">
                <el-table-column prop="title" label="活动名称" width="200"></el-table-column>
                <el-table-column prop="description" label="活动描述"></el-table-column>
                <el-table-column prop="location" label="活动地点" width="150"></el-table-column>
                <el-table-column prop="startTime" label="开始时间" width="180"></el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="180"></el-table-column>
                <el-table-column prop="activityStatus" label="活动状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getActivityStatusType(scope.row.activityStatus)">
                      {{ scope.row.activityStatus }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
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
      title="更换昵称" 
      width="500px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="新昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入新昵称" />
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

    <!-- 更换邮箱对话框 -->
    <el-dialog v-model="showChangeEmail" title="更换绑定邮箱" width="400px">
      <el-form :model="changeEmailForm" label-width="100px">
        <el-form-item label="新邮箱">
          <el-input v-model="changeEmailForm.newEmail" placeholder="请输入新邮箱" />
        </el-form-item>
        <el-form-item label="新邮箱验证码">
          <el-input v-model="changeEmailForm.newEmailCode" placeholder="请输入新邮箱验证码" />
        </el-form-item>
        <el-form-item label="旧邮箱验证码">
          <el-input v-model="changeEmailForm.oldEmailCode" placeholder="请输入旧邮箱验证码" />
        </el-form-item>
        <el-form-item>
          <el-button @click="sendBothEmailCodes" :disabled="emailCodeCountdown > 0">
            {{ emailCodeCountdown > 0 ? emailCodeCountdown + 's' : '发送验证码' }}
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showChangeEmail = false">取消</el-button>
          <el-button type="primary" @click="handleChangeEmail">确认更换</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, SwitchButton, Message, Key, Plus, Star, Clock, Timer, Document } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { 
  getProfileInfo, 
  updateProfile, 
  changePassword, 
  getMyClubs, 
  getMyActivities, 
  getRecentActivities,
  getMyPosts,
  uploadAvatar 
} from '../api/profileApi'
import { deletePost as apiDeletePost } from '../api/forum'

import request from '../utils/request'
import { 
  getBrowsingHistory, 
  clearBrowsingHistory, 
  removeBrowsingHistory, 
  formatBrowseTime 
} from '../utils/history'

const router = useRouter()
const store = useStore()

// 响应式数据
const isLoggedIn = computed(() => {
  // 检查store中的登录状态
  const storeLoggedIn = store.getters.isLoggedIn
  // 检查localStorage中的token
  const token = localStorage.getItem('token')
  const user = localStorage.getItem('user')
  
  // 如果store显示已登录，或者localStorage中有有效数据，则认为已登录
  return storeLoggedIn || (token && user)
})
const showAvatarUpload = ref(false)
const showEditDialog = ref(false)
const showChangePassword = ref(false)
const showChangeEmail = ref(false)
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

// 更换邮箱表单
const changeEmailForm = ref({ newEmail: '', newEmailCode: '', oldEmailCode: '' })
const emailCodeCountdown = ref(0)
let emailCodeTimer = null

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

// 我的收藏数据
const favoriteClubs = ref([])

// 我的帖子数据
const myPosts = ref([])

// 分页相关
const clubsPage = ref(1)
const clubsPageSize = ref(8)
const activitiesPage = ref(1)
const activitiesPageSize = ref(12)
const recentPage = ref(1)
const recentPageSize = ref(12)
const favoritesPage = ref(1)
const favoritesPageSize = ref(8)
const postsPage = ref(1)
const postsPageSize = ref(5)



// 浏览历史相关
const browsingHistory = ref([])
const historyPage = ref(1)
const historyPageSize = ref(5)

// 计算当前页数据
const pagedClubs = computed(() => {
  const start = (clubsPage.value - 1) * clubsPageSize.value
  return myClubs.value.slice(start, start + clubsPageSize.value)
})

const processActivities = (activities) => {
  const now = new Date()
  const filtered = activities
    .filter(act => act.isCreator || act.participantStatus === '已加入')
    .map(act => {
      if (act.applyStatus === '待审核' || act.participantStatus === '待审核') {
        act.activityStatus = '待审核'
      } else if (act.applyStatus === '已通过' || act.participantStatus === '已通过' || act.isCreator) {
        if (new Date(act.startTime) > now) {
          act.activityStatus = '未开始'
        } else if (new Date(act.endTime) < now) {
          act.activityStatus = '已结束'
        } else {
          act.activityStatus = '进行中'
        }
      }
      return act
    })
  return filtered
}

const pagedActivities = computed(() => {
  const filtered = processActivities(myActivities.value)
  const start = (activitiesPage.value - 1) * activitiesPageSize.value;
  return filtered.slice(start, start + activitiesPageSize.value);
});

const pagedRecentActivities = computed(() => {
  const filtered = processActivities(recentActivities.value)
  const start = (recentPage.value - 1) * recentPageSize.value;
  return filtered.slice(start, start + recentPageSize.value);
});

const pagedFavoriteClubs = computed(() => {
  const start = (favoritesPage.value - 1) * favoritesPageSize.value
  return favoriteClubs.value.slice(start, start + favoritesPageSize.value)
})

const pagedPosts = computed(() => {
  const start = (postsPage.value - 1) * postsPageSize.value
  return myPosts.value.slice(start, start + postsPageSize.value)
})

const pagedHistory = computed(() => {
  const start = (historyPage.value - 1) * historyPageSize.value
  return browsingHistory.value.slice(start, start + historyPageSize.value)
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
  // 如果是退出登录，不处理菜单选择
  if (index === 'logout') {
    return
  }
  
  activeMenu.value = index
  // 根据菜单加载相应数据
  switch (index) {
    case 'clubs':
      fetchMyClubs()
      break
    case 'activities':
      fetchMyActivities()
      break
    case 'favorites':
      fetchFavoriteClubs()
      break

    case 'posts':
      fetchMyPosts()
      break

    case 'history':
      loadBrowsingHistory()
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

    
    if (!token) {
      ElMessage.error('未找到登录token，请重新登录')
      options.onError(new Error('未找到登录token'))
      return
    }
    
    const formData = new FormData()
    formData.append('file', options.file)
    

    
    const response = await fetch(uploadUrl.value, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
    
    
    
    if (response.ok) {
      const result = await response.json()

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
    case '未开始':
      return 'info'
    case '进行中':
      return 'success'
    case '已结束':
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
      myClubs.value = (response.data.data || []).map(club => {
        let imgUrl = club.logoUrl || defaultAvatar
        if (imgUrl && imgUrl.startsWith('/uploads/')) {
          imgUrl = 'http://localhost:8080' + imgUrl
        }
        return {
          ...club,
          logoUrl: imgUrl
        }
      })
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
      myActivities.value = processActivities(response.data.data || [])
    } else {
      console.error('获取活动失败:', response.data.message)
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

// 获取我的收藏
const fetchFavoriteClubs = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    
    const response = await request.get('/api/clubs/favorites', {
      params: { userId: user.id }
    })
    
    if (response.data.code === 0) {
      favoriteClubs.value = (response.data.data || []).map(club => {
        let imgUrl = club.logoUrl || defaultAvatar
        if (imgUrl && imgUrl.startsWith('/uploads/')) {
          imgUrl = 'http://localhost:8080' + imgUrl
        }
        return {
          ...club,
          logoUrl: imgUrl
        }
      })
    } else {
      ElMessage.error('获取收藏列表失败')
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  }
}

// 获取我的帖子
const fetchMyPosts = async () => {
  try {
    const response = await getMyPosts()
    
    if (response.data.code === 200) {
      myPosts.value = response.data.data || []
    } else {
      ElMessage.error('获取我的帖子失败')
    }
  } catch (error) {
    console.error('获取我的帖子失败:', error)
    ElMessage.error('获取我的帖子失败')
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

// 跳转到社团详情
const goToClubDetail = (clubId) => {
  router.push(`/club/${clubId}`)
}

// 取消收藏
const removeFromFavorites = async (clubId) => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    
    const response = await request.delete(`/api/clubs/${clubId}/favorite`, {
      data: { userId: user.id }
    })
    
    if (response.data.code === 0) {
      ElMessage.success('已取消收藏')
      // 重新获取收藏列表
      await fetchFavoriteClubs()
    } else {
      ElMessage.error('取消收藏失败')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 收藏分页处理
const handleFavoritesPageChange = (newPage) => {
  favoritesPage.value = newPage
  fetchFavoriteClubs()
}

const handlePostsPageChange = (newPage) => {
  postsPage.value = newPage
  fetchMyPosts()
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

// 浏览历史相关方法
const loadBrowsingHistory = () => {
  browsingHistory.value = getBrowsingHistory()
}

const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有浏览历史吗？', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    clearBrowsingHistory()
    browsingHistory.value = []
    historyPage.value = 1
    ElMessage.success('浏览历史已清空')
  } catch (error) {
    // 用户取消操作
  }
}

const removeHistoryItem = (postId) => {
  removeBrowsingHistory(postId)
  browsingHistory.value = browsingHistory.value.filter(item => item.id !== postId)
  ElMessage.success('已删除该浏览记录')
}

const goToPost = (postId) => {
  router.push(`/post/${postId}`)
}

const getPostStatusText = (status) => {
  switch (status) {
    case 'active':
      return '正常'
    case 'deleted':
      return '已删除'
    case 'hidden':
      return '已隐藏'
    case 'violated':
      return '违规'
    default:
      return '未知'
  }
}

const getPostStatusClass = (status) => {
  switch (status) {
    case 'active':
      return 'status-active'
    case 'deleted':
      return 'status-deleted'
    case 'hidden':
      return 'status-hidden'
    case 'violated':
      return 'status-violated'
    default:
      return 'status-unknown'
  }
}

const deletePost = async (postId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    const response = await apiDeletePost(postId, user.id)
    if (response.data.code === 200 || response.data.code === 0) {
      ElMessage.success('帖子删除成功')
      await fetchMyPosts()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

const handleHistoryPageChange = (page) => {
  historyPage.value = page
}

// 同时发送新旧邮箱验证码
const sendBothEmailCodes = async () => {
  if (!changeEmailForm.value.newEmail) {
    ElMessage.error('请输入新邮箱')
    return
  }
  try {
    await request.post('/api/profile/send-both-email-codes', { newEmail: changeEmailForm.value.newEmail })
    ElMessage.success('验证码已分别发送到新旧邮箱')
    emailCodeCountdown.value = 60
    emailCodeTimer = setInterval(() => {
      emailCodeCountdown.value--
      if (emailCodeCountdown.value <= 0) clearInterval(emailCodeTimer)
    }, 1000)
  } catch (e) {
    ElMessage.error('发送验证码失败')
  }
}

// 修改 handleChangeEmail 方法，提交所有字段
const handleChangeEmail = async () => {
  if (!changeEmailForm.value.newEmail) {
    ElMessage.error('请输入新邮箱')
    return
  }
  if (!changeEmailForm.value.newEmailCode) {
    ElMessage.error('请输入新邮箱验证码')
    return
  }
  if (!changeEmailForm.value.oldEmailCode) {
    ElMessage.error('请输入旧邮箱验证码')
    return
  }
  try {
    const res = await request.put('/api/profile/change-email', {
      newEmail: changeEmailForm.value.newEmail,
      newEmailCode: changeEmailForm.value.newEmailCode,
      oldEmailCode: changeEmailForm.value.oldEmailCode
    })
    if (res.data.code === 200) {
      ElMessage.success('邮箱更换成功')
      showChangeEmail.value = false
      // 刷新用户信息
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
    } else {
      ElMessage.error(res.data.message || '邮箱更换失败')
    }
  } catch (e) {
    ElMessage.error('邮箱更换失败')
  }
}

// 判断是否可以退出社团（不是社长）
const canQuitClub = (club) => {
  return club.role !== '社长'
}

// 退出社团方法
const quitClub = async (clubId) => {
  try {
    // 确认对话框
    await ElMessageBox.confirm(
      '确定要退出该社团吗？退出后将无法访问社团内容。',
      '退出社团',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    
    const res = await request.delete(`/api/clubs/${clubId}/members/${user.id}`)
    if (res.data.code === 0) {
      ElMessage.success('已退出社团')
      // 重新获取我的社团列表
      await fetchMyClubs()
    } else {
      ElMessage.error(res.data.message || '退出失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出失败，请重试')
      console.error('退出社团错误:', error)
    }
  }
}

onMounted(() => {
  // 检查登录状态
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  
  if (token && userStr) {
    try {
      const user = JSON.parse(userStr)
      // 如果store中没有用户信息，则从localStorage恢复
      if (!store.getters.currentUser) {
        store.dispatch('login', user)
      }
    } catch (error) {
      console.error('解析用户信息失败:', error)
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
  
  if (isLoggedIn.value) {
    fetchRecentActivities()
  }
  
  // 监听用户登录成功事件，立即更新用户信息显示
  window.addEventListener('userLoginSuccess', (event) => {
    const newUserInfo = event.detail
    // 立即更新store中的用户信息
    store.dispatch('login', newUserInfo)
    // 如果当前在个人中心页面，重新获取用户相关数据
    if (activeMenu.value === 'clubs') {
      fetchMyClubs()
    } else if (activeMenu.value === 'activities') {
      fetchMyActivities()
    } else if (activeMenu.value === 'recent') {
      fetchRecentActivities()
    }
  })
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('userLoginSuccess', () => {})
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

.activity-meta {
  display: flex;
  gap: 15px;
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

.activity-club {
  color: #409EFF;
}

.activity-location {
  color: #67c23a;
}

.favorite-col {
  padding-left: 1%;
  padding-right: 1%;
  margin-bottom: 2%;
}

.favorite-card {
  width: 100%;
  min-height: 200px;
  margin-bottom: 0;
  cursor: pointer;
  transition: transform 0.2s;
}

.favorite-card:hover {
  transform: translateY(-2px);
}

.club-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
}

.club-info {
  padding: 0 8px;
}

.club-title {
  font-size: 1.3vw;
  font-weight: bold;
  margin-bottom: 1vw;
  color: #303133;
}

.club-desc {
  margin-bottom: 1vw;
  font-size: 1vw;
  color: #606266;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.club-tags {
  color: #409EFF;
  font-size: 0.9vw;
  font-weight: 500;
}

.club-actions {
  margin-top: 10px;
  text-align: center;
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

/* 浏览历史样式 */
.history-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.history-item:hover {
  background-color: #f8f9fa;
}

.history-item:last-child {
  border-bottom: none;
}

.history-content {
  flex: 1;
  min-width: 0;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.author {
  color: #409EFF;
}

.browse-time {
  color: #999;
}

.post-preview {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.history-actions {
  margin-left: 15px;
  flex-shrink: 0;
}

/* 帖子相关样式 */
.post-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.post-item:hover {
  background-color: #f8f9fa;
}

.post-item:last-child {
  border-bottom: none;
}

.post-content {
  font-size: 15px;
  color: #222;
  line-height: 1.7;
  background: #fafbfc;
  padding: 18px 24px;
  border-radius: 6px;
  margin-bottom: 12px;
  overflow-x: auto;
}

.post-content h1,
.post-content h2,
.post-content h3,
.post-content h4,
.post-content h5,
.post-content h6 {
  font-size: 1.2em;
  margin: 0.5em 0 0.3em 0;
  font-weight: bold;
}
.post-content ol,
.post-content ul {
  margin: 0.5em 0 0.5em 1.5em;
  padding-left: 1.5em;
}
.post-content li {
  font-size: 1em;
  margin: 0.2em 0;
}
.post-content p {
  margin: 0.4em 0;
}
.post-content a {
  color: #409eff;
  text-decoration: underline;
  word-break: break-all;
}

.post-actions {
  margin-left: 15px;
  flex-shrink: 0;
}

</style>