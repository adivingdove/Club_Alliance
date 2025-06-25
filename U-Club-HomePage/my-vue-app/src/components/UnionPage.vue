<template>
  <el-container class="main-container">
    <!-- 顶部导航栏 -->
    <el-header class="nav-header">
      <div class="nav-left">
        <img src="/logo.png" class="logo" alt="logo" />
        <span class="site-title">WHU社团</span>
      </div>
      <el-menu mode="horizontal" :default-active="'/'" class="nav-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/clubs">社团</el-menu-item>
        <el-menu-item index="/activities">活动</el-menu-item>
        <el-menu-item index="/rank">排行榜</el-menu-item>
      </el-menu>
      <div class="nav-right">
        <el-input placeholder="搜索社团、活动" prefix-icon="el-icon-search" class="search-input" />
        <el-button type="primary" class="login-btn">登录</el-button>
      </div>
    </el-header>

    <!-- Banner -->
    <div class="banner">
      <img src="https://images.unsplash.com/photo-1464983953574-0892a716854b?auto=format&fit=crop&w=1200&q=80" class="banner-img" />
      <div class="banner-content">
        <h1>2025 年武汉大学社团招新季</h1>
        <p>百团大战，等你来选！3月15日-3月30日，武汉大学桂操，不见不散</p>
        <el-button type="primary" size="large">了解更多</el-button>
      </div>
    </div>

    <!-- 分类Tab -->
    <el-tabs v-model="activeTab" class="club-tabs" @tab-click="filterClubs">
      <el-tab-pane label="全部社团" name="all"></el-tab-pane>
      <el-tab-pane :label="typeMap.tech.label" name="tech"></el-tab-pane>
      <el-tab-pane :label="typeMap.art.label" name="art"></el-tab-pane>
      <el-tab-pane :label="typeMap.sport.label" name="sport"></el-tab-pane>
      <el-tab-pane :label="typeMap.public.label" name="public"></el-tab-pane>
      <el-tab-pane :label="typeMap.innovate.label" name="innovate"></el-tab-pane>
    </el-tabs>

    <!-- 社团卡片区 -->
    <el-main>
      <el-row :gutter="24" class="club-list">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="club in filteredClubs" :key="club.id">
          <el-card
            class="club-card clickable-card"
            @click="goToDetail(club.id)"
          >
            <img :src="club.img" class="club-img" />
            <div class="club-info">
              <div class="club-title-row">
                <span class="club-title">{{ club.name }}</span>
                <span class="club-num">{{ club.memberCount }}人</span>
              </div>
              <div class="club-desc">{{ club.description }}</div>
              <div class="club-members">
                <img v-for="(avatar, idx) in club.avatars" :key="idx" :src="avatar" class="member-avatar" />
              </div>
              <el-button type="primary" size="small" class="join-btn">加入社团</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>

  <!-- 悬浮建立社团按钮 -->
  <el-button
    class="fab-create-club"
    type="primary"
    circle
    @click="createClub"
    style="position: fixed; right: 40px; bottom: 40px; z-index: 1000; width: 60px; height: 60px; box-shadow: 0 4px 16px rgba(64,158,255,0.2); display: flex; align-items: center; justify-content: center; font-size: 28px;"
  >
    <span style="font-size: 32px;">+</span>
  </el-button>

  <!-- 新建社团弹窗表单 -->
  <el-dialog v-model="showCreateDialog" title="新建社团" width="500px" :close-on-click-modal="false">
    <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
      <el-form-item label="社团名称" prop="name">
        <el-input v-model="createForm.name" placeholder="请输入社团名称" />
      </el-form-item>
      <el-form-item label="建立理由" prop="reason">
        <el-input v-model="createForm.reason" type="textarea" placeholder="请填写建立理由" />
      </el-form-item>
      <el-form-item label="基础活动" prop="activity">
        <el-input v-model="createForm.activity" placeholder="如：定期讲座、兴趣小组等" />
      </el-form-item>
      <el-form-item label="个人信息" prop="personal">
        <el-input v-model="createForm.personal" type="textarea" placeholder="请填写你的姓名、联系方式等" />
      </el-form-item>
      <el-form-item label="社团主页图片" prop="logoUrl">
        <el-upload
          action="http://localhost:8080/api/upload"
          :headers="{ 'X-Requested-With': 'XMLHttpRequest' }"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :show-file-list="false"
        >
          <el-button type="primary">上传图片</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="社团分类" prop="type">
        <el-select v-model="createForm.type" placeholder="请选择社团分类">
          <el-option v-for="item in clubTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreateDialog = false">取消</el-button>
      <el-button type="primary" @click="submitCreateClub">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('all')
const router = useRouter()
const route = useRoute()
const clubId = route.params.id

const showCreateDialog = ref(false)
const createFormRef = ref()
const clubTypes = [
  { label: '学术科技', value: 1 },
  { label: '文化艺术', value: 2 },
  { label: '体育竞技', value: 3 },
  { label: '公益实践', value: 4 },
  { label: '创新创业', value: 5 }
]

const createForm = ref({
  name: '',
  reason: '',
  activity: '',
  personal: '',
  logoUrl: '',
  type: null
})
const createRules = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写建立理由', trigger: 'blur' }],
  activity: [{ required: true, message: '请填写基础活动', trigger: 'blur' }],
  personal: [{ required: true, message: '请填写个人信息', trigger: 'blur' }],
  logoUrl: [{ required: true, message: '请上传社团主页图片', trigger: 'blur' }],
  type: [{ required: true, message: '请选择社团分类', trigger: 'change' }]
}
const createClub = () => {
  showCreateDialog.value = true
}
const clubs = ref([])

// Update type mappings to be more descriptive
const typeMap = {
  tech: { value: 1, label: '学术科技' },
  art: { value: 2, label: '文化艺术' },
  sport: { value: 3, label: '体育竞技' },
  public: { value: 4, label: '公益实践' },
  innovate: { value: 5, label: '创新创业' }
}

const typeMapReverse = {
  1: 'tech',
  2: 'art',
  3: 'sport',
  4: 'public',
  5: 'innovate'
}

const fetchClubs = async () => {
  try {
    const res = await request.get('/api/clubs')
    if (res.data.code === 0) {
      clubs.value = (res.data.data || []).map(club => {
        let imgUrl = club.logoUrl || '/logo.png'
        if (imgUrl.startsWith('/upload/')) {
          imgUrl = 'http://localhost:8080' + imgUrl
        }
        return {
          ...club,
          img: imgUrl,
          type: club.type, // Keep the numeric type from backend
          avatars: club.avatars || [],
          memberCount: club.memberCount || 1
        }
      })
    }
  } catch (e) {
    ElMessage.error('获取社团列表失败')
  }
}

// 页面加载时拉取社团列表
fetchClubs()

const uploadHeaders = { 'X-Requested-With': 'XMLHttpRequest' }
const uploadAction = 'http://localhost:8080/api/upload' // 你需要有后端上传接口
const handleUploadSuccess = (response) => {
  if (response.code === 0 && response.url) {
    createForm.value.logoUrl = response.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}
const handleUploadError = (error) => {
  ElMessage.error('上传失败，请重试')
}
const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPG) {
    ElMessage.error('上传图片必须是 JPG 或 PNG 格式')
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB')
  }
  return isJPG && isLt5M
}

const submitCreateClub = () => {
  createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        if (!user.id) {
          ElMessage.error('请先登录')
          return
        }
        const res = await request.post('/api/clubs', {
          name: createForm.value.name,
          description: createForm.value.reason,
          creatorId: user.id,
          logoUrl: createForm.value.logoUrl,
          type: createForm.value.type,
          tags: ''
        })
        if (res.data.code === 0) {
          ElMessage.success('社团创建成功')
          showCreateDialog.value = false
          fetchClubs()
        } else {
          ElMessage.error(res.data.message || '社团创建失败')
        }
      } catch (e) {
        ElMessage.error('网络错误，创建失败')
      }
    }
  })
}

const filteredClubs = computed(() => {
  if (activeTab.value === 'all') return clubs.value
  
  // Convert tab name to numeric type value for filtering
  const targetType = typeMap[activeTab.value]?.value
  return clubs.value.filter(club => club.type === targetType)
})

function filterClubs() {
  // 这里不再需要额外API请求，直接依赖filteredClubs
}

function goToDetail(id) {
  router.push(`/club/${id}`)
}
</script>

<style scoped>
.main-container {
  background: #f7f8fa;
  min-height: 100vh;
}
.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  height: 64px;
  box-shadow: 0 2px 8px #f0f1f2;
  padding: 0 40px;
}
.logo {
  height: 36px;
  margin-right: 12px;
}
.site-title {
  font-size: 22px;
  font-weight: bold;
  color: #409EFF;
}
.nav-menu {
  flex: 1;
  margin-left: 40px;
}
.nav-right {
  display: flex;
  align-items: center;
}
.search-input {
  width: 200px;
  margin-right: 16px;
}
.login-btn {
  margin-left: 8px;
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
.club-tabs {
  margin: 0 0 24px 0;
  background: #fff;
  border-radius: 8px;
  padding-left: 24px;
}
.club-list {
  margin-top: 0;
}
.club-card {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  background: #fff;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.06);
  transition: box-shadow 0.2s;
}
.club-card:hover {
  box-shadow: 0 6px 24px 0 rgba(0,0,0,0.12);
}
.club-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  cursor: pointer;
}
.club-info {
  padding: 16px 8px 8px 8px;
}
.club-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.club-title {
  font-size: 18px;
  font-weight: bold;
}
.club-num {
  color: #909399;
  font-size: 14px;
}
.club-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
  min-height: 36px;
}
.club-members {
  margin-bottom: 8px;
}
.member-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid #fff;
  margin-right: -8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}
.join-btn {
  width: 100%;
  margin-top: 4px;
}
.clickable-card {
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.clickable-card:hover {
  box-shadow: 0 6px 24px 0 rgba(0,0,0,0.12);
}
.fab-create-club {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 1000;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(64,158,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}
</style>