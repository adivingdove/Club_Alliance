<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request' // 你的 axios 封装
import { ElMessage } from 'element-plus'

// 假数据，实际可通过API获取
const clubsData = [
  {
    id: 1,
    name: '摄影爱好者协会',
    description: '致力于培养摄影艺术人才，为摄影爱好者提供交流平台。我们定期组织外拍活动、举办摄影展，让每位成员都能在实践中提升摄影技巧。',
    img: 'https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=800&q=80',
    activities: [
      { id: 1, title: '城市夜景摄影实践', date: '2024-03-15 19:00', place: '滨江大道', people: 25, img: 'https://images.unsplash.com/photo-1464983953574-0892a716854b?auto=format&fit=crop&w=400&q=80' },
      { id: 2, title: '人像摄影工作坊', date: '2024-03-20 14:00', place: '创意园区', people: 18, img: 'https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=400&q=80' },
      { id: 3, title: '自然风光采风', date: '2024-03-25 08:00', place: '山水国家公园', people: 30, img: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400&q=80' }
    ],
    members: [
      { id: 1, name: '林婷婷', role: '社长', avatar: 'https://randomuser.me/api/portraits/women/1.jpg' },
      { id: 2, name: '张志远', role: '摄影导师', avatar: 'https://randomuser.me/api/portraits/men/2.jpg' },
      { id: 3, name: '赵雨薇', role: '活动总监', avatar: 'https://randomuser.me/api/portraits/women/3.jpg' },
      { id: 4, name: '王建国', role: '技术组', avatar: 'https://randomuser.me/api/portraits/men/4.jpg' }
    ]
  }
  // 你可以添加更多社团
]

const route = useRoute()
const club = ref({})
const isFavorited = ref(false)
const applicationStatus = ref(null)

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

// 封装获取社团详情的方法
const fetchClub = async (id) => {
  try {
    const response = await request.get(`/api/clubs/${id}/detail`)
    if (response.data.code === 0) {
      const data = response.data.data
      // 兼容主页图片逻辑
      let imgUrl = data.logoUrl || DEFAULT_IMG
      if (imgUrl.startsWith('/upload/')) {
        imgUrl = 'http://localhost:8080' + imgUrl
      }
      data.img = imgUrl
      
      // 处理成员头像
      if (Array.isArray(data.members)) {
        data.members.forEach(m => {
          if (!m.avatar || m.avatar.startsWith('/upload/')) {
            m.avatar = m.avatar ? 'http://localhost:8080' + m.avatar : DEFAULT_IMG
          }
        })
      } else {
        data.members = []
      }
      
      // 兜底活动图
      if (Array.isArray(data.activities)) {
        data.activities.forEach(a => a.img = DEFAULT_IMG)
      } else {
        data.activities = []
      }
      
      club.value = data
      
      // 检查收藏状态
      await checkFavoriteStatus(id)
      // 检查申请状态
      await checkApplicationStatus(id)
    } else {
      club.value = { img: DEFAULT_IMG, activities: [], members: [] }
    }
  } catch (e) {
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
        </div>
      </div>
    </div>

    <!-- 活动列表 -->
    <div class="section">
      <h2>社团活动</h2>
      <el-row :gutter="24">
        <el-col :span="8" v-for="activity in club.activities" :key="activity.id">
          <el-card class="activity-card">
            <img :src="activity.img || '/logo.png'" class="activity-img" />
            <div class="activity-info">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-meta">
                <span>{{ activity.date }}</span>
                <span>{{ activity.place }}</span>
              </div>
              <div class="activity-meta">
                <span>👥 {{ activity.people }}人参与</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 成员列表 -->
    <div class="section">
      <h2>成员列表</h2>
      <el-row :gutter="16">
        <el-col :span="4" v-for="member in club.members" :key="member.id">
          <el-card class="member-card">
            <img :src="member.avatar || '/logo.png'" class="member-avatar" />
            <div class="member-name">{{ member.name }}</div>
            <div class="member-role">{{ member.role }}</div>
          </el-card>
        </el-col>
      </el-row>
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
.activity-card { border-radius: 10px; overflow: hidden; }
.activity-img { width: 100%; height: 120px; object-fit: cover; }
.activity-title { font-size: 16px; font-weight: bold; margin: 8px 0; }
.activity-meta { color: #909399; font-size: 13px; margin-bottom: 2px; }
.member-card { text-align: center; border-radius: 10px; }
.member-avatar { width: 48px; height: 48px; border-radius: 50%; margin-bottom: 8px; }
.member-name { font-weight: bold; }
.member-role { color: #409EFF; font-size: 13px; }
</style>
