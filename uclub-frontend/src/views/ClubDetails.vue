<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request' // 你的 axios 封装
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import ClubChat from '@/components/ClubChat.vue'

const route = useRoute()
const club = ref({})
const isFavorited = ref(false)
const applicationStatus = ref(null)

// 添加切换按钮状态
const activeTab = ref('activities') // 默认显示活动

const DEFAULT_IMG = '/logo.png'

const showApplyDialog = ref(false)
const applyFormRef = ref()
const applyForm = ref({
  applicant: '',
  reason: ''
})
const applyRules = {
  applicant: [{ required: true, message: '请填写申请人信息', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写申请理由', trigger: 'blur' }]
}

const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}')
  } catch {
    return {}
  }
})

// 统一安全获取当前用户id
const safeUserId = computed(() => (user.value && typeof user.value.id !== 'undefined' && user.value.id !== null) ? Number(user.value.id) : null)

const isPresident = computed(() => safeUserId.value && Number(club.value.creatorId) === safeUserId.value)

// 检查收藏状态
const checkFavoriteStatus = async (clubId) => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) return
    
    const response = await request.get(`/api/clubs/${clubId}/favorite`, {
      params: { userId: user.id }
    })
    if (response.data.code === 0) {
      isFavorited.value = response.data.data
    }
  } catch (e) {
    console.error('检查收藏状态失败:', e)
  }
}

// 收藏/取消收藏
const toggleFavorite = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    
    const clubId = route.params.id
    if (isFavorited.value) {
      // 取消收藏
      const response = await request.delete(`/api/clubs/${clubId}/favorite`, {
        data: { userId: user.id }
      })
      if (response.data.code === 0) {
        isFavorited.value = false
        ElMessage.success('已取消收藏')
      } else {
        ElMessage.error('取消收藏失败')
      }
    } else {
      // 添加收藏
      const response = await request.post(`/api/clubs/${clubId}/favorite`, {
        userId: user.id
      })
      if (response.data.code === 0) {
        isFavorited.value = true
        ElMessage.success('收藏成功')
      } else {
        ElMessage.error('收藏失败')
      }
    }
  } catch (e) {
    console.error('收藏操作失败:', e)
    ElMessage.error('操作失败，请重试')
  }
}

// 检查申请状态
const checkApplicationStatus = async (clubId) => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) return
    
    const response = await request.get(`/api/clubs/${clubId}/application-status`, {
      params: { userId: user.id }
    })
    if (response.data.code === 0) {
      applicationStatus.value = response.data.data
    }
  } catch (e) {
    console.error('检查申请状态失败:', e)
  }
}

// 复制 /activities 页面的辅助方法
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}
function getShortDescription(desc) {
  if (typeof desc === 'string') {
    const text = desc.replace(/<[^>]+>/g, '')
    return text.length > 80 ? text.slice(0, 80) + '...' : text
  }
  if (desc == null) return ''
  try {
    const str = JSON.stringify(desc)
    return str.length > 80 ? str.slice(0, 80) + '...' : str
  } catch {
    return String(desc)
  }
}
function getImageUrl(imageUrl) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  if (!imageUrl) return '/src/assets/vue.svg'
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  if (imageUrl.startsWith('/uploads/')) {
    return `${baseUrl}${imageUrl}`
  }
  return `${baseUrl}/uploads/${imageUrl}`
}
const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}')
  } catch {
    return {}
  }
})
const isAdmin = computed(() => userInfo.value?.role === '系统管理员')
const isLoggedIn = computed(() => !!userInfo.value?.id)
const canEditActivity = (activity) => {
  return isLoggedIn.value && (
    activity.creatorId === userInfo.value?.id || isAdmin.value
  )
}
const canDeleteActivity = (activity) => {
  return isLoggedIn.value && (
    activity.creatorId === userInfo.value?.id || isAdmin.value
  )
}
const editActivity = (activity) => {
  selectedActivity.value = activity
  showActivityDetailDialog.value = true
  // TODO: 实现编辑弹窗
}
const deleteActivityHandler = async (activity) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用后端删除接口
    ElMessage.success('删除成功（请补充实际删除逻辑）')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除活动失败')
    }
  }
}

// 封装获取社团详情的方法
const fetchClub = async (id) => {
  try {
    console.log('获取社团详情，社团ID:', id)
    const response = await request.get(`/api/clubs/${id}/detail`)
    console.log('社团详情响应:', response)
    
    if (response.data.code === 0) {
      const data = response.data.data
      console.log('原始社团数据:', data)
      console.log('活动数据:', data.activities)
      
      // 兼容主页图片逻辑
      let imgUrl = data.logoUrl || DEFAULT_IMG
      if (imgUrl && imgUrl.startsWith('/uploads/')) {
        imgUrl = 'http://localhost:8080' + imgUrl
      }
      data.img = imgUrl
      
      // 处理成员头像
      if (Array.isArray(data.members)) {
        data.members.forEach(m => {
          if (m.avatar && m.avatar.startsWith('/uploads/')) {
            m.avatar = 'http://localhost:8080' + m.avatar
          } else if (!m.avatar) {
            m.avatar = DEFAULT_IMG
          }
        })
        // 按角色排序：社长 > 副社长 > 干事 > 成员
        const roleOrder = { '社长': 1, '副社长': 2, '干事': 3, '成员': 4 }
        data.members.sort((a, b) => (roleOrder[a.role] || 99) - (roleOrder[b.role] || 99))
      } else {
        data.members = []
      }
      
      // 处理活动数据，字段映射为 /activities 页面的格式
      if (Array.isArray(data.activities)) {
        data.activities = data.activities
          .filter(activity => activity.status === '通过')
          .map(activity => ({
            id: activity.id,
            title: activity.title,
            description: activity.description,
            imageUrl: activity.img || '',
            startTime: activity.startTime,
            location: activity.location || '',
            currentParticipants: activity.currentParticipants || 0,
            maxParticipants: activity.maxParticipants,
            creatorId: activity.creatorId,
            applyStatus: activity.status,
          }))
      } else {
        data.activities = []
      }
      
      club.value = data || { img: DEFAULT_IMG, activities: [], members: [] }
      console.log('社团数据设置完成:', club.value)
      console.log('最终活动数量:', club.value.activities.length)
      
      // 检查收藏状态
      await checkFavoriteStatus(id)
      // 检查申请状态
      await checkApplicationStatus(id)
    } else {
      console.error('获取社团详情失败:', response.data.message)
      club.value = { img: DEFAULT_IMG, activities: [], members: [] }
    }
  } catch (e) {
    console.error('获取社团详情失败:', e)
    club.value = { img: DEFAULT_IMG, activities: [], members: [] }
  }
}

// 监听路由变化，动态加载社团
watch(
  () => route.params.id,
  (id) => {
    if (id) fetchClub(id)
  },
  { immediate: true }
)

onMounted(() => {
  if (route.params.id) fetchClub(route.params.id)
  window.scrollTo({ top: 0, behavior: 'auto' })
})

// 申请加入社团
const submitApply = () => {
  applyFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        if (!user.id) {
          ElMessage.error('请先登录')
          return
        }
        
        const clubId = route.params.id
        const payload = {
          userId: user.id,
          applicant: applyForm.value.applicant,
          reason: applyForm.value.reason
        }
        
        const res = await request.post(`/api/clubs/${clubId}/apply`, payload)
        if (res.data.code === 0) {
          ElMessage.success('申请已提交，请等待审核')
          showApplyDialog.value = false
          // 重新检查申请状态
          await checkApplicationStatus(clubId)
          // 清空表单
          applyForm.value = { applicant: '', reason: '' }
        } else {
          ElMessage.error(res.data.message || '申请失败')
        }
      } catch (e) {
        console.error('申请失败:', e)
        ElMessage.error('申请失败，请重试')
      }
    }
  })
}

// 检查是否可以申请
const canApply = () => {
  // 如果是社团创建者，不能申请
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (user.id && club.value.creatorId === user.id) {
    return false
  }
  return applicationStatus.value !== '待审核' && applicationStatus.value !== '已通过'
}

// 获取申请状态文本
const getApplicationStatusText = () => {
  // 如果是社团创建者，显示"我是社长"
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (user.id && club.value.creatorId === user.id) {
    return '我是社长'
  }
  
  switch (applicationStatus.value) {
    case '待审核':
      return '申请审核中'
    case '已通过':
      return '已是成员'
    case '已拒绝':
      return '申请被拒绝'
    default:
      return '申请加入'
  }
}

// 获取申请状态按钮类型
const getApplicationButtonType = () => {
  // 如果是社团创建者，显示特殊样式
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (user.id && club.value.creatorId === user.id) {
    return 'info'
  }
  
  switch (applicationStatus.value) {
    case '待审核':
      return 'warning'
    case '已通过':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'primary'
  }
}

// 获取活动状态文本
const getActivityStatusText = (status) => {
  switch (status) {
    case '待审核':
      return '审核中'
    case '通过':
      return '已通过'
    case '拒绝':
      return '已拒绝'
    default:
      return '待审核'
  }
}

// 获取活动状态类
const getActivityStatusClass = (status) => {
  switch (status) {
    case '待审核':
      return 'activity-status-pending'
    case '通过':
      return 'activity-status-approved'
    case '拒绝':
      return 'activity-status-rejected'
    default:
      return 'activity-status-pending'
  }
}

// 编辑社团相关
const showEditDialog = ref(false)
const editForm = ref({
  name: '',
  description: '',
  logoUrl: ''
})
const editRules = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入简介', trigger: 'blur' }],
}
const editFormRef = ref()

const openEditDialog = () => {
  editForm.value = {
    name: club.value.name || '',
    description: club.value.description || '',
    logoUrl: club.value.logoUrl || ''
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
  // 兼容后端返回 { code: 0, data: { url: 'xxx' } } 或 { code: 0, url: 'xxx' }
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
        const clubId = route.params.id
        const payload = {
          name: editForm.value.name,
          description: editForm.value.description,
          logoUrl: editForm.value.logoUrl
        }
        const res = await request.put(`/api/clubs/${clubId}`, payload)
        if (res.data.code === 0) {
          ElMessage.success('社团信息更新成功')
          showEditDialog.value = false
          await fetchClub(clubId)
        } else {
          ElMessage.error(res.data.message || '更新失败')
        }
      } catch (e) {
        ElMessage.error('更新失败，请重试')
      }
    }
  })
}

const setMemberRole = async (member, role) => {
  try {
    const clubId = club.value.id
    const creatorId = safeUserId.value
    await request.put(`/api/clubs/${clubId}/members/${member.id}/role`, { creatorId, role })
    ElMessage.success('角色设置成功')
    await fetchClub(clubId) // 刷新成员列表
  } catch (e) {
    ElMessage.error('角色设置失败')
  }
}

const transferPresident = async (member) => {
  try {
    const clubId = club.value.id
    const creatorId = safeUserId.value
    // 调用后端接口，转让社长
    await request.put(`/api/clubs/${clubId}/transfer-president`, {
      fromUserId: creatorId,
      toUserId: member.userId
    })
    ElMessage.success('社长已转让')
    await fetchClub(clubId) // 刷新成员列表
  } catch (e) {
    ElMessage.error('转让失败')
  }
}

// 计算属性：当前用户是否可以退出社团（不是社长且在成员列表中）
const canQuitClub = computed(() => {
  const userId = safeUserId.value
  if (!userId || !club.value.members) return false
  const me = club.value.members.find(m => Number(m.userId) === userId)
  return me && me.role !== '社长'
})

// 退出社团方法
const quitClub = async () => {
  try {
    const userId = safeUserId.value
    const clubId = club.value.id
    if (!userId || !clubId) return
    const res = await request.delete(`/api/clubs/${clubId}/members/${userId}`)
    if (res.data.code === 0) {
      ElMessage.success('已退出社团')
      await fetchClub(clubId)
    } else {
      ElMessage.error(res.data.message || '退出失败')
    }
  } catch (e) {
    ElMessage.error('退出失败，请重试')
  }
}

// 在<script setup>中添加：
const canManageClub = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (!user.id || !club.value.members) return false
  const me = club.value.members.find(m => Number(m.userId) === Number(user.id))
  return me && me.role !== '成员'
})

console.log(club.value.members, user.value)

const showAnnouncementDrawer = ref(false)

watch(showAnnouncementDrawer, (val) => {
  if (val) {
    document.body.style.overflow = ''
  }
})
const manageTab = ref('members')
const applications = ref([])

const fetchApplications = async () => {
  if (!club.value.id || !user.value.id) return
  const res = await request.get(`/api/clubs/applications`, { params: { creatorId: user.value.id } })
  if (res.data.code === 0) {
    // 只保留当前社团的待审核申请
    const all = res.data.data
    const clubPending = (all.pending || []).filter(app => app.clubId === club.value.id)
    applications.value = clubPending
  } else {
    applications.value = []
  }
}

const removeMember = async (member) => {
  try {
    const clubId = club.value.id
    await request.delete(`/api/clubs/${clubId}/members/${member.userId}`)
    ElMessage.success('成员已移除')
    await fetchClub(clubId)
  } catch (e) {
    ElMessage.error('移除失败')
  }
}

const approveApplication = async (app) => {
  try {
    const clubId = club.value.id
    await request.put(`/api/clubs/applications/${app.id}/approve`, { creatorId: user.value.id })
    ElMessage.success('已同意')
    fetchApplications()
    fetchClub(clubId)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
const rejectApplication = async (app) => {
  try {
    await request.put(`/api/clubs/applications/${app.id}/reject`, { creatorId: user.value.id })
    ElMessage.success('已拒绝')
    fetchApplications()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

watch(
  () => [activeTab.value, club.value.id],
  ([tab, id]) => {
    if (tab === 'manage' && id) fetchApplications()
  },
  { immediate: false }
)

const mutedMembers = ref([])

const toggleMute = (member) => {
  const idx = mutedMembers.value.indexOf(member.userId)
  if (idx === -1) {
    mutedMembers.value.push(member.userId)
    ElMessage.info(`${member.name} 已被禁言`)
  } else {
    mutedMembers.value.splice(idx, 1)
    ElMessage.success(`${member.name} 已解除禁言`)
  }
}

// 排序函数：社长 > 副社长 > 干事 > 成员
const roleOrder = { '社长': 1, '副社长': 2, '干事': 3, '成员': 4 }
const sortedMembers = computed(() => {
  return (club.value.members || []).slice().sort((a, b) => (roleOrder[a.role] || 99) - (roleOrder[b.role] || 99))
})

const showActivityDetailDialog = ref(false)
const selectedActivity = ref(null)

function openActivityDetail(activity) {
  selectedActivity.value = activity
  showActivityDetailDialog.value = true
}

// 删除公告
const deleteAnnouncement = async (announcement) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除公告"${announcement.title}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 显示加载状态
    const loading = ElMessage({
      message: '正在删除公告...',
      type: 'info',
      duration: 0,
      showClose: false
    })
    
    const clubId = route.params.id
    const response = await request.delete(`/api/announcements/${announcement.id}`, {
      params: { userId: safeUserId.value }
    })
    
    // 关闭加载状态
    loading.close()
    
    if (response.data.code === 0) {
      ElMessage.success('公告删除成功')
      // 立即从本地状态中移除公告
      if (club.value.announcements) {
        const index = club.value.announcements.findIndex(a => a.id === announcement.id)
        if (index > -1) {
          club.value.announcements.splice(index, 1)
        }
      }
      // 同时刷新公告列表以确保数据同步
      await refreshAnnouncements()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 检查当前用户是否可以删除公告
const canDeleteAnnouncement = computed(() => {
  const userId = safeUserId.value
  if (!userId || !club.value.members) return false
  const me = club.value.members.find(m => Number(m.userId) === userId)
  return me && ['社长', '副社长', '干事'].includes(me.role)
})

// 刷新公告列表
const refreshAnnouncements = async () => {
  try {
    const clubId = route.params.id
    const response = await request.get(`/api/announcements/club/${clubId}`)
    if (response.data.code === 0) {
      club.value.announcements = response.data.data
    }
  } catch (error) {
    console.error('刷新公告列表失败:', error)
  }
}

// 安全渲染富文本描述（去除危险标签，仅保留基础格式）
function safeHtml(html) {
  if (!html) return '';
  return html.replace(/<(\/)?(script|style|iframe|object|embed|form|input|button|link|meta)[^>]*>/gi, '')
             .replace(/on\w+\s*=\s*(['"]).*?\1/gi, '');
}
</script>

<template>
  <div class="club-detail">
    <!-- Banner区 -->
    <div class="banner">
      <img :src="club.img || '/logo.png'" class="banner-img" />
      <div class="banner-content">
        <h1>{{ club.name }}</h1>
        <p>{{ club.description }}</p>
        <div class="banner-btns">
          <el-button :type="isFavorited ? 'success' : 'warning'" plain @click="toggleFavorite">
            {{ isFavorited ? '已收藏' : '收藏社团' }}
          </el-button>
          <el-button 
            :type="getApplicationButtonType()" 
            :disabled="!canApply()"
            @click="canApply() ? showApplyDialog = true : null"
          >
            {{ getApplicationStatusText() }}
          </el-button>
          <el-button v-if="isPresident" type="primary" plain @click="openEditDialog" style="margin-left: 12px;">
            编辑社团信息
          </el-button>
          <el-button
            v-if="canQuitClub"
            type="danger"
            plain
            @click="quitClub"
          >
            退出社团
          </el-button>
        </div>
      </div>
    </div>

    <!-- 切换按钮 -->
    <div class="tab-section">
      <el-card class="tab-card">
        <div class="tab-header">
          <el-button 
            :type="activeTab === 'activities' ? 'primary' : 'default'"
            @click="activeTab = 'activities'"
            size="large"
          >
            社团活动
          </el-button>
          <el-button 
            :type="activeTab === 'members' ? 'primary' : 'default'"
            @click="activeTab = 'members'"
            size="large"
          >
            成员列表
          </el-button>
          <el-button 
            :type="activeTab === 'chat' ? 'primary' : 'default'"
            @click="activeTab = 'chat'"
            size="large"
          >
            社团聊天室
          </el-button>
          <el-button 
            v-if="canManageClub"
            :type="activeTab === 'manage' ? 'primary' : 'default'"
            @click="activeTab = 'manage'"
            size="large"
          >
            社团管理
          </el-button>
        </div>
        
        <!-- 活动列表 -->
        <div v-if="activeTab === 'activities'" class="tab-content activities-list">
          <el-row :gutter="24" style="align-items: stretch;">
            <el-col 
              :xs="24" 
              :sm="12" 
              :md="8" 
              :lg="6" 
              v-for="activity in club.activities" 
              :key="activity.id"
              style="display: flex;"
            >
              <el-card 
                class="activity-card modern-card"
                shadow="hover"
                @click="openActivityDetail(activity)"
                style="flex: 1; display: flex; flex-direction: column;"
              >
                <div class="activity-img-wrap">
                  <img
                    :src="getImageUrl(activity.imageUrl)"
                    :alt="activity.title || '活动图片'"
                    class="activity-img-preview"
                    @error="event => event.target.src = '/src/assets/vue.svg'"
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
                  <el-button type="primary" size="small" @click.stop="openActivityDetail(activity)">详情</el-button>
                  <el-button v-if="canEditActivity(activity)" type="warning" size="small" @click.stop="editActivity(activity)">编辑</el-button>
                  <el-button v-if="canDeleteActivity(activity)" type="danger" size="small" @click.stop="deleteActivityHandler(activity)">删除</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <div v-if="club.activities && club.activities.length === 0" class="empty-state">
            <el-empty description="暂无活动" />
          </div>
        </div>

        <!-- 成员列表 -->
        <div v-if="activeTab === 'members'" class="tab-content">
          <el-row :gutter="16">
            <el-col :span="4" v-for="member in sortedMembers" :key="member.userId">
              <el-card class="member-card">
                <img :src="member.avatar || '/logo.png'" class="member-avatar" />
                <div class="member-name">{{ member.name }}</div>
                <div class="member-role">{{ member.role }}</div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 社团聊天室 -->
        <div v-if="activeTab === 'chat'">
          <ClubChat v-if="club && club.id" :clubId="club.id" :key="club.id" />
        </div>

        <!-- 社团管理 -->
        <div v-if="activeTab === 'manage'" class="tab-content">
          <el-tabs v-model="manageTab" style="margin-bottom: 20px;">
            <el-tab-pane label="成员管理" name="members">
              <el-table :data="sortedMembers" style="width: 100%">
                <el-table-column prop="name" label="昵称" />
                <el-table-column prop="role" label="角色">
                  <template #default="{ row }">
                    <el-select v-model="row.role" @change="setMemberRole(row, row.role)" :disabled="row.role === '社长'">
                      <el-option label="成员" value="成员" />
                      <el-option label="干事" value="干事" />
                      <el-option label="副社长" value="副社长" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column prop="joinedAt" label="加入时间" />
                <el-table-column label="操作">
                  <template #default="{ row }">
                    <el-button v-if="row.role !== '社长'" type="danger" size="small" @click="removeMember(row)">踢出社团</el-button>
                    <el-button type="warning" size="small" @click="toggleMute(row)">
                      {{ mutedMembers.includes(row.userId) ? '解除禁言' : '禁言' }}
                    </el-button>
                    <el-button v-if="isPresident && row.role !== '社长'" type="primary" size="small" @click="transferPresident(row)">转让社长</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="申请管理" name="applications">
              <el-table :data="applications" style="width: 100%">
                <el-table-column prop="applicantName" label="申请人" />
                <el-table-column prop="applicantInfo" label="信息" />
                <el-table-column prop="reason" label="理由" />
                <el-table-column prop="appliedAt" label="申请时间" />
                <el-table-column prop="status" label="状态" />
                <el-table-column label="操作">
                  <template #default="{ row }">
                    <el-button type="success" size="small" @click="approveApplication(row)">同意</el-button>
                    <el-button type="danger" size="small" @click="rejectApplication(row)">拒绝</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-card>
    </div>

    <!-- 申请加入弹窗表单 -->
    <el-dialog v-model="showApplyDialog" title="申请加入社团" width="400px" :close-on-click-modal="false">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="90px">
        <el-form-item label="申请人信息" prop="applicant">
          <el-input v-model="applyForm.applicant" placeholder="姓名/联系方式" />
        </el-form-item>
        <el-form-item label="申请理由" prop="reason">
          <el-input v-model="applyForm.reason" type="textarea" placeholder="请填写申请理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
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

    <!-- 公告侧边栏按钮 -->
    <el-button
      class="announcement-drawer-btn"
      type="warning"
      @click="showAnnouncementDrawer = true"
      style="position: fixed; top: 180px; right: 0; z-index: 2000;"
    >
      社团公告
    </el-button>
    <!-- 公告抽屉 -->
    <el-drawer
      v-model="showAnnouncementDrawer"
      title="社团公告"
      direction="rtl"
      size="400px"
      :lock-scroll="false"
    >
      <div v-if="club.announcements && club.announcements.length">
        <el-timeline>
          <el-timeline-item
            v-for="a in club.announcements"
            :key="a.id"
            :timestamp="a.createdAt ? new Date(a.createdAt).toLocaleString('zh-CN') : ''"
            placement="top"
          >
            <div class="announcement-item">
              <div class="announcement-header">
                <h4>{{ a.title }}</h4>
                <el-button
                  v-if="canDeleteAnnouncement"
                  type="danger"
                  size="small"
                  circle
                  @click.stop="deleteAnnouncement(a)"
                  class="delete-announcement-btn"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <div style="white-space: pre-line;">{{ a.content }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else>
        <el-empty description="暂无公告" />
      </div>
    </el-drawer>

    <el-dialog v-model="showActivityDetailDialog" title="活动详情" width="600px" class="activity-detail-dialog">
      <div v-if="selectedActivity" class="activity-detail-card">
        <div class="detail-header">
          <div class="detail-title">{{ selectedActivity.title }}</div>
          <el-tag :type="selectedActivity.applyStatus === '通过' ? 'success' : (selectedActivity.applyStatus === '待审核' ? 'warning' : 'danger')" class="detail-status">
            {{ getActivityStatusText(selectedActivity.applyStatus) }}
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
          <div class="detail-info-row"><i class="el-icon-date"></i> <span class="detail-label">活动时间：</span>{{ formatDate(selectedActivity.startTime) }}</div>
          <div class="detail-info-row"><i class="el-icon-location"></i> <span class="detail-label">活动地点：</span>{{ selectedActivity.location }}</div>
          <div class="detail-info-row"><i class="el-icon-user"></i> <span class="detail-label">参与人数：</span>{{ selectedActivity.currentParticipants }}/{{ selectedActivity.maxParticipants ? selectedActivity.maxParticipants : '∞' }}人</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.club-detail { background: #f7f8fa; min-height: 100vh; }
.banner { position: relative; height: 280px; border-radius: 16px; overflow: hidden; margin-bottom: 32px; }
.banner-img { width: 100%; height: 100%; object-fit: cover; filter: brightness(0.7); }
.banner-content { position: absolute; left: 40px; top: 50%; transform: translateY(-50%); color: #fff; }
.banner-content h1 { font-size: 32px; font-weight: bold; margin-bottom: 12px; }
.banner-content p { font-size: 16px; margin-bottom: 18px; }
.banner-btns .el-button { margin-right: 12px; }
.section { margin-bottom: 32px; }
.tab-section { margin-bottom: 32px; }
.tab-card { border-radius: 16px; }
.tab-header { 
  display: flex; 
  justify-content: center; 
  padding: 20px 0; 
  border-bottom: 1px solid #f0f0f0; 
  margin-bottom: 24px; 
}

.tab-header .el-button {
  font-size: 20px;
  font-weight: bold;
  padding: 15px 30px;
  height: auto;
  min-height: 50px;
  margin: 0 10px;
}
.tab-content { padding: 0 20px 20px; }
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
  min-height: 40px;
}
.activity-card-footer {
  display: flex;
  gap: 8px;
  padding: 0 12px 12px 12px;
  justify-content: flex-end;
  margin-top: auto;
}
.member-card { text-align: center; border-radius: 10px; }
.member-avatar { width: 48px; height: 48px; border-radius: 50%; margin-bottom: 8px; }
.member-name { font-weight: bold; }
.member-role { color: #409EFF; font-size: 13px; }
.activity-status { font-size: 12px; font-weight: bold; padding: 2px 6px; border-radius: 4px; margin-left: 8px; }
.activity-status-pending { background-color: #e6a23c; color: #fff; }
.activity-status-approved { background-color: #67c23a; color: #fff; }
.activity-status-rejected { background-color: #f56c6c; color: #fff; }
.empty-state { text-align: center; padding: 40px 0; }
.chat-placeholder { text-align: center; padding: 60px 0; }
.chat-notice { 
  margin-top: 20px; 
  color: #909399; 
  font-size: 16px; 
  line-height: 1.6; 
}
.manage-placeholder { text-align: center; padding: 40px 0; }
.manage-notice { 
  margin-top: 20px; 
  color: #909399; 
  font-size: 16px; 
  line-height: 1.6; 
}
.manage-features {
  margin-top: 30px;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}
.manage-features h3 {
  color: #303133;
  margin-bottom: 15px;
  font-size: 18px;
}
.manage-features ul {
  list-style: none;
  padding: 0;
}
.manage-features li {
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}
.manage-features li:last-child {
  border-bottom: none;
}
.announcement-drawer-btn {
  border-radius: 8px 0 0 8px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
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
  max-width: 320px;
  width: auto;
  height: auto;
  max-height: 220px;
  border-radius: 8px;
  border: 1px solid #eee;
  object-fit: contain;
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
.detail-info-row {
  margin-bottom: 6px;
  color: #555;
  font-size: 1rem;
}
</style>
