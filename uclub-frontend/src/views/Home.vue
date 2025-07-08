<template>
  <!-- 悬浮AI助手 -->
  <div class="floating-ai" style="position: fixed; right: 40px; bottom: 120px; z-index: 1000;">
    <el-avatar size="large" class="ai-avatar-icon" @click="goToAiChat" title="点击进入 AI 助手" style="cursor: pointer; box-shadow: 0 2px 12px rgba(0,0,0,0.1);">🤖</el-avatar>
  </div>

  <!-- 艺术渐变装饰 -->
  <svg class="art-blob" width="320" height="180" viewBox="0 0 320 180" fill="none" style="position:absolute;top:-60px;left:-60px;z-index:0;">
    <ellipse cx="160" cy="90" rx="160" ry="90" fill="url(#paint0_linear)" fill-opacity="0.5"/>
    <defs>
      <linearGradient id="paint0_linear" x1="0" y1="0" x2="320" y2="180" gradientUnits="userSpaceOnUse">
        <stop stop-color="#a18cd1"/>
        <stop offset="1" stop-color="#fbc2eb"/>
      </linearGradient>
    </defs>
  </svg>

  <!-- 顶部波浪装饰 -->
  <svg class="top-wave" viewBox="0 0 1440 180" style="position:absolute;top:0;left:0;width:100vw;height:180px;z-index:0;">
    <path fill="url(#waveGradient)" fill-opacity="1" d="M0,64L80,80C160,96,320,128,480,133.3C640,139,800,117,960,117.3C1120,117,1280,139,1360,149.3L1440,160L1440,0L1360,0C1280,0,1120,0,960,0C800,0,640,0,480,0C320,0,160,0,80,0L0,0Z"></path>
    <defs>
      <linearGradient id="waveGradient" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#a18cd1"/>
        <stop offset="100%" stop-color="#fbc2eb"/>
      </linearGradient>
    </defs>
  </svg>

  <el-container class="main-container no-header-layout">
    <div class="center-content">
      <!-- 搜索栏 -->
      <div class="search-bar-wrapper">
        <div class="search-bar-center">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索社团名称或简介"
            prefix-icon="el-icon-search"
            class="main-search-input beautify-input"
            @input="onSearchInput"
            @focus="showSuggest = true"
            @blur="onInputBlur"
          >
            <template #suffix>
              <span v-if="searchKeyword" class="clear-btn" @mousedown.prevent="clearSearch" title="清空">✖️</span>
              <span class="emoji-search-btn" @mousedown.prevent="doSearch" title="搜索">🔍</span>
            </template>
          </el-input>
          <!-- 下拉建议 -->
          <ul v-if="showSuggest && suggestList.length" class="suggest-list">
            <li v-for="item in suggestList" :key="item.id" @mousedown.prevent="selectSuggest(item)">
              <span v-html="highlightMatch(item.name)"></span>
            </li>
          </ul>
          <transition name="fade">
            <div class="search-info beautify-info" v-if="searchKeyword">
              <span v-if="filteredClubs.length">🔍 共 <b>{{ filteredClubs.length }}</b> 个社团</span>
              <span v-else>未找到相关社团</span>
            </div>
          </transition>
        </div>
      </div>
      <!-- Banner -->
      <el-carousel
        class="banner-carousel"
        height="260px"
        indicator-position="outside"
        arrow="always"
        interval="10000"
      >
        <el-carousel-item v-for="(item, idx) in banners" :key="idx">
          <div class="banner-img-wrapper">
            <img :src="item.img" class="banner-img" />
            <div class="banner-content">
              <h1>{{ item.title }}</h1>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <!-- 分栏 -->
      <el-row :gutter="20" class="three-column-layout" style="margin-top:20px">
        <!-- 左侧 -->
        <el-col :xs="0" :sm="0" :md="6" :lg="6">
          <div class="card-unified">
            <div class="card-title"><el-icon><i class="el-icon-trophy"></i></el-icon> 社团热度榜</div>
            <div class="rank-list">
              <div v-for="(club, idx) in top5Clubs" :key="club.id" class="rank-card" @click="goToDetail(club.id)">
                <span class="rank-badge">{{ idx+1 }}</span>
                <div class="rank-info">
                  <div class="rank-title">{{ club.name }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
        <!-- 中间 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12"> 
        <!-- 社团筛选标签 -->
         <el-tabs v-model="activeTab" class="club-tabs club-tabs-title" @tab-click="filterClubs">
        <el-tab-pane label="全部社团" name="all" class="tab-left"></el-tab-pane>
        <el-tab-pane :label="typeMap.tech.label" name="tech"></el-tab-pane>
        <el-tab-pane :label="typeMap.art.label" name="art"></el-tab-pane>
        <el-tab-pane :label="typeMap.sport.label" name="sport"></el-tab-pane>
        <el-tab-pane :label="typeMap.public.label" name="public"></el-tab-pane>
        <el-tab-pane :label="typeMap.innovate.label" name="innovate"></el-tab-pane>
      </el-tabs>
         <el-main>
          <el-row :gutter="24" class="club-list">
            <el-col :xs="24" :sm="12" :md="12" :lg="12" v-for="club in filteredClubs" :key="club.id">
              <div class="card-unified club-card clickable-card" @click="goToDetail(club.id)">
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
                  <el-button type="primary" size="small" class="join-btn">查看详情</el-button>
                </div>
              </div>
            </el-col>
          </el-row>
         </el-main>
        </el-col>
        <!-- 右侧 -->
        <el-col :xs="0" :sm="6" :md="6" :lg="6">
          <div class="card-unified">
            <div class="card-title"><el-icon><i class="el-icon-chat-dot-round"></i></el-icon> 论坛热帖</div>
            <div class="rank-list">
              <div v-for="(post, idx) in hotTopics" :key="post.id" class="rank-card" @click="goToPost(post.id)">
                <span class="rank-badge">{{ idx+1 }}</span>
                <div class="rank-info">
                  <div class="rank-title">{{ post.title }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    <!-- 系统公告侧边栏按钮 -->
    <el-button
      class="system-announcement-drawer-btn"
      type="info"
      circle
      size="small"
      @click="showSystemAnnouncementDrawer = true"
      style="position: fixed; top: 120px; right: 0; z-index: 2000; width: 40px; height: 40px;"
    >
      📢
    </el-button>
    
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
    <!-- 系统公告弹窗 -->
    <el-dialog
      v-model="showSystemAnnouncementDrawer"
      title="系统公告"
      width="600px"
      :close-on-click-modal="false"
      class="system-announcement-dialog"
      :modal-append-to-body="false"
      :lock-scroll="false"
      :top="'8vh'"
    >
      <div v-if="systemAnnouncements && systemAnnouncements.length" class="announcement-list">
        <el-timeline>
          <el-timeline-item
            v-for="announcement in systemAnnouncements"
            :key="announcement.id"
            :timestamp="announcement.createdAt ? new Date(announcement.createdAt).toLocaleString('zh-CN') : ''"
            placement="top"
          >
            <div class="announcement-item">
              <div class="announcement-header">
                <h4>{{ announcement.title }}</h4>
              </div>
              <div class="announcement-content">
                <MarkdownIt :source="announcement.content" />
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else>
        <el-empty description="暂无系统公告" />
      </div>
      <template #footer>
        <el-button @click="showSystemAnnouncementDrawer = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新建社团弹窗表单 -->
    <el-dialog v-model="showCreateDialog" title="☀️ 新建社团" width="500px" :close-on-click-modal="false" class="create-club-dialog" 
      :modal-append-to-body="false"
      :lock-scroll="false"
      :top="'8vh'"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px" class="create-club-form">
        <el-form-item label="⭐ 社团名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入社团名称">
            <template #prefix>
              <el-icon><i class="el-icon-office-building"></i></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="✍️ 建立理由" prop="reason">
          <el-input v-model="createForm.reason" placeholder="请填写建立理由">
            <template #prefix>
              <el-icon><i class="el-icon-edit"></i></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="📚 基础活动" prop="activity">
          <el-input v-model="createForm.activity" placeholder="如：定期讲座、兴趣小组等">
            <template #prefix>
              <el-icon><i class="el-icon-notebook"></i></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="👤 个人信息" prop="personal">
          <el-input v-model="createForm.personal" placeholder="请填写你的姓名、联系方式等">
            <template #prefix>
              <el-icon><i class="el-icon-user"></i></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="🖼️ 社团主页图" prop="logoUrl">
          <el-upload
            action="http://localhost:8080/api/upload"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :show-file-list="false"
          >
            <el-button type="primary" icon="el-icon-picture">上传图片</el-button>
          </el-upload>
          <!-- 图片预览 -->
          <div v-if="createForm.logoUrl" class="club-img-preview">
            <img 
              :src="getImageUrl(createForm.logoUrl)" 
              class="club-img-preview-img"
              alt="社团图片预览"
            />
            <p class="club-img-preview-tip">图片预览</p>
          </div>
        </el-form-item>
        <el-form-item label="🏷️ 社团分类" prop="type">
          <el-select v-model="createForm.type" placeholder="请选择社团分类">
            <el-option v-for="item in clubTypes" :key="item.value" :label="item.emoji + ' ' + item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false" class="create-club-cancel-btn">
          <el-icon style="margin-right:4px"><i class="el-icon-close"></i></el-icon>取消
        </el-button>
        <el-button type="primary" @click="submitCreateClub" class="create-club-submit-btn">
          <el-icon style="margin-right:4px;"><i class="el-icon-plus"></i></el-icon>提交
        </el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import axios from 'axios'
import MarkdownIt from 'vue3-markdown-it'
import HomePage1 from '@/assets/HomePage(1).png'
import HomePage2 from '@/assets/HomePage(2).png'
import HomePage3 from '@/assets/HomePage(3).png'
import HomePage4 from '@/assets/HomePage(4).png'
import HomePage5 from '@/assets/HomePage(5).png'

const top5Clubs = ref([])
const hotTopics = ref([])

// 系统公告相关
const showSystemAnnouncementDrawer = ref(false)
const systemAnnouncements = ref([])

const activeTab = ref('all')
const router = useRouter()
const route = useRoute()
const clubId = route.params.id

const showCreateDialog = ref(false)
const createFormRef = ref()
const clubTypes = [
  { label: '学术科技', value: 1, emoji: '🧪' },
  { label: '文化艺术', value: 2, emoji: '🎨' },
  { label: '体育竞技', value: 3, emoji: '🏅' },
  { label: '公益实践', value: 4, emoji: '🤝' },
  { label: '创新创业', value: 5, emoji: '🚀' }
]


const goToAiChat = ()=>{
  router.push('/ai-chat')
}

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

const store = useStore()
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const userInfo = computed(() => store.getters.currentUser)

const fetchClubs = async () => {
  try {
    const res = await request.get('/api/clubs/active')
    if (res.data.code === 0) {
      clubs.value = (res.data.data || []).map(club => {
        let imgUrl = club.logoUrl || '/logo.png'
        if (imgUrl && imgUrl.startsWith('/uploads/')) {
          imgUrl = 'http://localhost:8080' + imgUrl
        }
        return {
          ...club,
          img: imgUrl,
          type: club.type, // Keep the numeric type from backend
          avatars: club.avatars || [],
          memberCount: club.currentMembers ?? club.current_members ?? club.memberCount ?? 0
        }
      })
    }
  } catch (e) {
    ElMessage.error('获取社团列表失败')
  }
}

// 页面加载时拉取社团列表
fetchClubs()

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'X-Requested-With': 'XMLHttpRequest',
    'Authorization': token ? `Bearer ${token}` : ''
  }
})
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
  if (error.status === 403) {
    ElMessage.error('上传失败：请先登录')
  } else {
    ElMessage.error('上传失败，请重试')
  }
}
const beforeUpload = (file) => {
  // 检查用户是否已登录
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('请先登录后再上传图片')
    return false
  }
  
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
          ElMessage.success('社团创建成功，等待审核')
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

const searchKeyword = ref('')
const showSuggest = ref(false)
const suggestList = ref([])

const onSearchInput = () => {
  if (searchKeyword.value) {
    const kw = searchKeyword.value.trim().toLowerCase()
    suggestList.value = clubs.value.filter(club =>
      club.name && club.name.toLowerCase().includes(kw)
    ).slice(0, 6)
    showSuggest.value = true
  } else {
    suggestList.value = []
    showSuggest.value = false
  }
}
const clearSearch = () => {
  searchKeyword.value = ''
  suggestList.value = []
  showSuggest.value = false
}

const selectSuggest = (item) => {
  searchKeyword.value = item.name
  showSuggest.value = false
  nextTick(() => {
    document.activeElement.blur()
  })
}

const onInputBlur = () => {
  setTimeout(() => {
    showSuggest.value = false
  }, 120)
}

const highlightMatch = (name) => {
  if (!searchKeyword.value) return name
  const kw = searchKeyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return name.replace(new RegExp(kw, 'ig'), m => `<span class='kw-highlight'>${m}</span>`)
}

const filteredClubs = computed(() => {
  let list = clubs.value
  // 分类过滤
  if (activeTab.value !== 'all') {
    const targetType = typeMap[activeTab.value]?.value
    list = list.filter(club => club.type === targetType)
  }
  // 搜索过滤
  if (searchKeyword.value) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter(club =>
      (club.name && club.name.toLowerCase().includes(kw)) ||
      (club.description && club.description.toLowerCase().includes(kw))
    )
  }
  return list
})

function filterClubs() {
  // 这里不再需要额外API请求，直接依赖filteredClubs
}

function goToDetail(id) {
  router.push(`/club/${id}`)
}

const doSearch = () => {
  // 触发搜索时可聚焦并显示建议，也可做其他扩展
  onSearchInput()
}

const getImageUrl = (url) => {
  if (url && url.startsWith('/uploads/')) {
    return 'http://localhost:8080' + url
  }
  return url
}

const joinClub = async (club) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (!user.id) {
    ElMessage.error('请先登录')
    return
  }
  try {
    // 调用后端申请加入社团接口
    const res = await request.post(`/api/clubs/${club.id}/apply`, {
      userId: user.id
      // 可根据后端需要补充申请理由、联系方式等
    })
    if (res.data.code === 0) {
      ElMessage.success('申请已提交，等待社长审批')
      // 不要修改 club.memberCount
    } else {
      ElMessage.error(res.data.message || '申请失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，申请失败')
  }
}

// Banner 轮播图数据
const banners = [
  { img: HomePage1, title: '2025 年武汉大学社团招新季', desc: '百团大战，等你来选！9月15日全天，武汉大学桂园操场，不见不散' },
  { img: HomePage2, title: '创新创业社团等你加入', desc: '激发你的创造力，和志同道合的伙伴一起成长' },
  { img: HomePage3, title: '丰富多彩的文体活动', desc: '体育、艺术、公益，总有一款适合你' },
  { img: HomePage4, title: '结识新朋友', desc: '在社团中遇见志同道合的伙伴，开启大学新生活' },
  { img: HomePage5, title: '展示自我，成就未来', desc: '参与社团活动，提升自我能力，收获成长与荣誉' }
]

// 禁止弹窗出现时页面滚动
watch(showCreateDialog, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = '  '
  }
})

// 监听系统公告弹窗
watch(showSystemAnnouncementDrawer, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

// 热门社团和帖子数据

const fetchHotClubs = async () => {
  try {
    const res = await axios.get('/api/clubs/hot')
    // 如果后端返回的是 { code: 200, data: [...], message: "" }
    top5Clubs.value = res.data.data?.slice(0, 5) || []
  } catch (err) {
    console.error('获取热门社团失败：', err)
  }
}

const fetchHotPosts = async () => {
  try {
    const res = await axios.get('/api/posts/hot')
    // res.data 是帖子列表
    hotTopics.value = res.data?.slice(0, 5) || []
  } catch (err) {
    console.error('获取热门帖子失败：', err)
  }
}

// 获取系统公告
const fetchSystemAnnouncements = async () => {
  try {
    const response = await request.get('/api/admin/system-announcements')
    if (response.data && Array.isArray(response.data)) {
      systemAnnouncements.value = response.data
    } else {
      systemAnnouncements.value = []
    }
  } catch (error) {
    console.error('获取系统公告失败:', error)
    systemAnnouncements.value = []
  }
}

const goToPost = (id: number) => {
  router.push(`/post/${id}`)
}

onMounted(() =>{
  fetchHotClubs()
  fetchHotPosts()
  fetchSystemAnnouncements()
})

</script>

<style scoped>
body, .main-container {
  min-height: 100vh;
  background: linear-gradient(120deg, #a18cd1 0%, #fbc2eb 100%);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  position: relative;
}

/* 艺术渐变装饰SVG */
.art-blob {
  pointer-events: none;
}
.top-wave {
  pointer-events: none;
}

/* 玻璃拟态卡片风格 */
.card-unified, .club-card, .rank-card {
  background: rgba(255,255,255,0.7);
  border-radius: 32px;
  box-shadow: 0 8px 32px 0 rgba(161,140,209,0.10);
  backdrop-filter: blur(12px);
  border: 1.5px solid rgba(255,255,255,0.3);
  padding: 18px 16px;
  margin-bottom: 24px;
  transition: box-shadow 0.18s, transform 0.18s;
}
.card-unified:hover, .club-card:hover, .rank-card:hover {
  box-shadow: 0 16px 40px 0 rgba(251,194,235,0.18);
  transform: translateY(-4px) scale(1.03);
}

/* 玻璃拟态搜索栏 */
.main-search-input.beautify-input {
  width: 100%;
  max-width: 420px;
  height: 40px;
  border-radius: 32px;
  background: rgba(255,255,255,0.7);
  border: 1.5px solid #e0c3fc;
  box-shadow: 0 2px 12px 0 rgba(161,140,209,0.08);
  font-size: 18px;
  color: #333;
  padding: 0 20px;
  backdrop-filter: blur(8px);
  transition: border 0.2s, background 0.2s;
}
.main-search-input.beautify-input:focus-within {
  border: 1.5px solid #fbc2eb;
  background: rgba(255,255,255,0.85);
}

/* 搜索icon主色为紫蓝渐变 */
.emoji-search-btn, .clear-btn {
  font-size: 20px;
  color: #a18cd1;
  margin-right: 2px;
  transition: color 0.18s;
}
.emoji-search-btn:hover, .clear-btn:hover {
  color: #fbc2eb;
}

/* tab激活色呼应主色 */
.club-tabs-title .el-tabs__item.is-active {
  color: #a18cd1 !important;
}
.club-tabs-title .el-tabs__item.is-active::after {
  content: '';
  display: block;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: -8px;
  width: 48px;
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, #a18cd1 0%, #fbc2eb 100%);
}
.club-tabs-title .el-tabs__item:not(.is-active):hover {
  color: #fbc2eb !important;
}

.main-container.no-header-layout {
  background: #fff;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
}
.center-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow-x: hidden;
}
.search-bar-wrapper {
  width: 100%;
  background: transparent;
  padding: 16px 0 8px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}
.el-main {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  padding-left: 0;
  padding-right: 0;
  overflow-x: hidden;
}
.banner-carousel {
  margin: 32px 0 24px 0;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px 0 rgba(64,158,255,0.13);
}
.banner-img-wrapper {
  position: relative;
  width: 100%;
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
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 4px 24px rgba(0,0,0,0.18);
}
.banner-content p {
  font-size: 16px;
  margin-bottom: 18px;
}
/* 社团列表 */
.club-tabs {
  width: 100%;
  max-width: none;
  margin-left: 0;
  margin-right: 0;
  border-radius: 0;
  background: transparent;
}
.club-tabs .el-tabs__header {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 ;
  padding: 0 ;
}
.club-tabs .el-tabs__nav {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0;
  padding: 0;
}
.club-tabs .el-tabs__item,
.club-tabs .el-tabs__item.el-tabs__item {
  flex: 1;
  text-align: center;
  font-size: 42px !important;
  padding: 0 !important;
  height: 100px !important;
  line-height: 100px !important;
  margin: 0 !important;
  font-weight: bold !important;
  letter-spacing: 2px !important;
}
.club-list {
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  overflow-x: hidden;
}
@media (max-width: 900px) {
  .banner,
  .club-tabs .el-tabs__item{
    font-size: 28px !important;
    height: 80px !important;
    line-height: 80px !important;
  }
}
.search-bar-wrapper {
  width: 100%;
  background: transparent;
  padding: 16px 0 8px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}
.search-bar-center {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}
.main-search-input.beautify-input {
  width: 100%;
  max-width: 420px;
  height: 40px;
  border-radius: 20px;
  background: #f6f8fa;
  border: 1px solid #e0e6ed;
  box-shadow: none;
  font-size: 16px;
  color: #333;
  padding: 0 14px;
  transition: border 0.2s, background 0.2s;
}
.main-search-input.beautify-input:focus-within {
  border: 1.5px solid #b3d8fd;
  background: #fafdff;
}
.emoji-search-btn, .clear-btn {
  font-size: 20px;
  color: #b0b8c9;
  margin-right: 2px;
  transition: color 0.18s;
}
.emoji-search-btn:hover, .clear-btn:hover {
  color: #409EFF;
}
.suggest-list {
  position: absolute;
  top: 48px;
  left: 0;
  width: 100%;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.13);
  z-index: 10;
  margin: 0;
  padding: 4px 0;
  list-style: none;
  max-height: 220px;
  overflow-y: auto;
}
.suggest-list li {
  padding: 10px 24px;
  cursor: pointer;
  font-size: 16px;
  color: #333;
  transition: background 0.18s;
}
.suggest-list li:hover {
  background: #f0f7ff;
}
.kw-highlight {
  color: #409EFF;
  font-weight: bold;
  background: #eaf4ff;
  border-radius: 3px;
  padding: 0 2px;
}
.beautify-info {
  margin-top: 10px;
  font-size: 16px;
  color: #409EFF;
  font-weight: bold;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px #e3f0ff;
}
@media (max-width: 600px) {
  .search-bar-center {
    max-width: 98vw;
  }
  .main-search-input.beautify-input {
    font-size: 16px;
    height: 40px;
    border-radius: 20px;
    line-height: 40px;
    padding: 0 10px 0 10px;
  }
  .emoji-search-btn {
    font-size: 20px;
  }
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
.club-tabs {
  margin: 0 0 24px 0;
  background: transparent;
  border-radius: 8px;
  padding-left: 24px;
}
.club-list {
  margin-top: 0;
}
.card-unified {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.10);
  padding: 18px 16px;
  margin-bottom: 24px;
  transition: box-shadow 0.18s, transform 0.18s;
}
.card-unified:hover {
  box-shadow: 0 12px 32px 0 rgba(64,158,255,0.18);
  transform: translateY(-4px) scale(1.03);
}
.card-title {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 700;
  color: #222;
  margin-bottom: 16px;
}
.card-title .el-icon {
  font-size: 22px;
  margin-right: 8px;
  color: #a18cd1;
}
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 8px;
}
.rank-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.10);
  padding: 16px;
  margin-bottom: 0;
  transition: box-shadow 0.18s, transform 0.18s;
  cursor: pointer;
}
.rank-card:hover {
  box-shadow: 0 12px 32px 0 rgba(64,158,255,0.18);
  transform: translateY(-4px) scale(1.03);
}
.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-weight: bold;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  color: #fff;
  background: linear-gradient(135deg, #6a82fb 0%, #fc5c7d 100%);
}
.rank-info {
  flex: 1;
  text-align: left;
}
.rank-title {
  font-size: 16px;
  font-weight: 600;
}
.rank-desc {
  font-size: 13px;
  color: #888;
  margin-top: 2px;
}
.club-card {
  display: flex;
  align-items: flex-start;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.10);
  padding: 0;
  margin-bottom: 18px;
  transition: box-shadow 0.18s, transform 0.18s;
}
.club-card:hover {
  box-shadow: 0 12px 32px 0 rgba(64,158,255,0.18);
  transform: translateY(-4px) scale(1.03);
}
.club-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 16px;
  margin: 16px;
}
.club-info {
  flex: 1;
  padding: 16px 8px 6px 0;
}
.club-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.club-title {
  font-size: 16px;
  font-weight: bold;
}
.club-desc {
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
  min-height: 32px;
}
.club-members {
  margin-bottom: 8px;
}
.member-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid #fff;
  margin-right: -10px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.10);
}
.join-btn, .el-button, .create-club-submit-btn {
  background: linear-gradient(90deg, #6a82fb 0%, #fc5c7d 100%);
  border: none;
  color: #fff;
  font-weight: bold;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(161,140,209,0.13);
  transition: background 0.2s;
  width:auto;
  margin-top: 4px;
}
.join-btn:hover, .el-button:hover, .create-club-submit-btn:hover {
  background: linear-gradient(90deg, #fc5c7d 0%, #6a82fb 100%);
}

.join-btn, .el-button, .create-club-cancel-btn{
  background: linear-gradient(90deg, #6a82fb 0%, #fc5c7d 100%);
  border: none;
  color: #fff;
  font-weight: bold;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(161,140,209,0.13);
  transition: background 0.2s;
  width: auto;
  margin-top: 4px;
}
.join-btn:hover, .el-button:hover, .create-club-cancel-btn:hover {
  background: linear-gradient(90deg, #fc5c7d 0%, #6a82fb 100%);
}
.floating-ai {
  position: fixed;
  right: 40px;
  bottom: 120px;
  z-index: 1000;
}

/* 创建社团 */
.fab-create-club {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 1000;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 8px 32px 0 rgba(64,158,255,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  transition: transform 0.2s;
}
.fab-create-club:hover {
  transform: scale(1.12) rotate(-8deg);
}
.create-club-dialog :deep(.el-dialog) {
  position: fixed !important;
  top: 8vh !important;
  left: 0;
  right: 0;
  margin: 0 auto;
  z-index: 2000;
  max-width: 500px;
}
.create-club-dialog :deep(.el-dialog__body) {
  background: linear-gradient(135deg, #f4faff 0%, #e3f0ff 100%);
  border-radius: 18px;
  box-shadow: 0 8px 32px 0 rgba(64,158,255,0.13);
}
.create-club-form {
  padding: 10px 0 0 0;
}
.create-club-form .el-form-item {
  border-radius: 10px;
  background: #fff;
  margin-bottom: 18px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.06);
  padding: 12px 16px 6px 16px;
}
.create-club-form .el-input,
.create-club-form .el-textarea {
  border-radius: 8px;
  background: #f8fbff;
}
.create-club-form .el-input__inner,
.create-club-form .el-textarea__inner {
  background: #f8fbff;
  border-radius: 8px;
}
.create-club-form .el-input__prefix {
  color: #409EFF;
}
.create-club-form .el-input__icon {
  color: #409EFF;
}
.create-club-submit-btn {
  background: linear-gradient(90deg, #6a82fb 0%, #fc5c7d 100%);
  border: none;
  color: #fff;
  font-weight: bold;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(161,140,209,0.13);
  transition: background 0.2s;
}
.create-club-submit-btn:hover {
  background: linear-gradient(90deg, #fc5c7d 0%, #6a82fb 100%);
}
.club-img-preview {
  margin-top: 10px;
  text-align: center;
}
.club-img-preview-img {
  max-width: 200px;
  max-height: 150px;
  border-radius: 12px;
  border: 2px solid #409EFF;
  box-shadow: 0 4px 16px rgba(64,158,255,0.13);
}
.club-img-preview-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

/* AI图像 */
.floating-ai {
  position: fixed;
  z-index: 999;
  cursor: move;
  user-select: none;
}

.ai-avatar-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6a82fb 0%, #fc5c7d 100%);
  color: white;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px 0 rgba(161,140,209,0.18);
  transition: transform 0.2s, background 0.2s;
  border-radius: 50%;
  border: 2px solid white;
}

.ai-avatar-icon:hover {
  transform: scale(1.15) rotate(8deg);
  background: linear-gradient(135deg, #fc5c7d 0%, #6a82fb 100%);
}

.left-sidebar, .right-sidebar {
  background: #fff;
  padding: 16px;
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.08);
  min-height: 320px;
}

.sidebar-list {
  list-style: none;
  padding-left: 0;
  font-size: 16px;
  color: #333;
}

.sidebar-list li {
  padding: 10px 0;
  border-radius: 8px;
  transition: background 0.18s;
}
.sidebar-list li:hover {
  background: #f4faff;
}
.sidebar-list li::before {
  content: attr(data-rank);
  display: inline-block;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  color: #fff;
  border-radius: 50%;
  text-align: center;
  margin-right: 10px;
  font-weight: bold;
}

h3 {
  margin-bottom: 12px;
  font-size: 18px;
}

h4 {
  margin: 8px 0 6px;
  font-size: 15px;
  font-weight: normal;
  color: #666;
}

.clickable-item {
  cursor: pointer;
  transition: all 0.2s;
  color: #409EFF;
}

.clickable-item:hover {
  text-decoration: underline;
  color: #66b1ff;
}

.sidebar-card {
  background: linear-gradient(135deg, #f4faff 0%, #e3f0ff 100%);
  border-radius: 20px;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.10);
  padding: 24px 18px;
  margin-bottom: 24px;
  text-align: center;
}
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 18px;
}
.rank-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
  padding: 12px 16px;
  cursor: pointer;
  transition: box-shadow 0.18s, transform 0.18s;
}
.rank-card:hover {
  box-shadow: 0 8px 24px rgba(64,158,255,0.18);
  transform: translateY(-2px) scale(1.03);
}
.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-weight: bold;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  color: #fff;
}
.rank-1 { background: linear-gradient(135deg, #FFD700 0%, #FFB300 100%); }
.rank-2 { background: linear-gradient(135deg, #C0C0C0 0%, #B0B0B0 100%); }
.rank-3 { background: linear-gradient(135deg, #CD7F32 0%, #B87333 100%); }
.rank-4, .rank-5 { background: linear-gradient(135deg, #6a82fb 0%, #fc5c7d 100%); }
.rank-info {
  flex: 1;
  text-align: left;
}
.rank-title {
  font-size: 16px;
  font-weight: 600;
}
.rank-desc {
  font-size: 13px;
  color: #888;
  margin-top: 2px;
}
.floating-ai {
  position: fixed;
  right: 40px;
  bottom: 120px;
  z-index: 1000;
}

.club-num {
  color: #909399;
  font-size: 14px;
  font-weight: 500;
  margin-left: 8px;
}

/* 美化中间社团部分顶部title */
.club-tabs-title .el-tabs__header {
  background: transparent;
  border-bottom: none;
  margin-bottom: 18px;
}
.club-tabs-title .el-tabs__nav {
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  border-bottom: none;
}
.club-tabs-title .el-tabs__item {
  font-size: 24px !important;
  font-weight: 700 !important;
  color: #222 !important;
  letter-spacing: 2px;
  padding: 0 32px !important;
  background: transparent;
  transition: color 0.2s;
  position: relative;
}
.club-tabs-title .el-tabs__item.is-active {
  color: #409EFF !important;
}
.club-tabs-title .el-tabs__item.is-active::after {
  content: '';
  display: block;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: -8px;
  width: 48px;
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%);
}
.club-tabs-title .el-tabs__item:not(.is-active):hover {
  color: #66b1ff !important;
}

/* 系统公告样式 */
.system-announcement-drawer-btn {
  border-radius: 50% !important;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  min-width: auto;
  height: auto;
  line-height: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.system-announcement-dialog :deep(.el-dialog) {
  position: fixed !important;
  top: 8vh !important;
  left: 0;
  right: 0;
  margin: 0 auto;
  z-index: 2000;
  max-width: 600px;
}

.system-announcement-dialog :deep(.el-dialog__body) {
  background: linear-gradient(135deg, #f4faff 0%, #e3f0ff 100%);
  border-radius: 18px;
  box-shadow: 0 8px 32px 0 rgba(64,158,255,0.13);
  max-height: 60vh;
  overflow-y: auto;
}

.announcement-list {
  padding: 10px 0;
}

.announcement-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.announcement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.announcement-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.announcement-content {
  color: #666;
  line-height: 1.6;
}

.announcement-content :deep(h1),
.announcement-content :deep(h2),
.announcement-content :deep(h3),
.announcement-content :deep(h4),
.announcement-content :deep(h5),
.announcement-content :deep(h6) {
  margin: 12px 0 8px 0;
  color: #333;
  font-weight: bold;
}

.announcement-content :deep(p) {
  margin: 8px 0;
}

.announcement-content :deep(ul),
.announcement-content :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.announcement-content :deep(li) {
  margin: 4px 0;
}

.announcement-content :deep(blockquote) {
  margin: 8px 0;
  padding: 8px 12px;
  border-left: 4px solid #409EFF;
  background: #f8f9fa;
  color: #666;
}

.announcement-content :deep(code) {
  background: #f1f3f4;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.announcement-content :deep(pre) {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}

.announcement-content :deep(pre code) {
  background: none;
  padding: 0;
}

.announcement-content :deep(strong) {
  font-weight: bold;
  color: #333;
}

.announcement-content :deep(em) {
  font-style: italic;
}

.announcement-content :deep(a) {
  color: #409EFF;
  text-decoration: none;
}

.announcement-content :deep(a:hover) {
  text-decoration: underline;
}

.announcement-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0;
}

.announcement-content :deep(th),
.announcement-content :deep(td) {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.announcement-content :deep(th) {
  background: #f8f9fa;
  font-weight: bold;
}

/* 弹窗底部按钮对齐 */
.create-club-dialog :deep(.el-dialog__footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-right: 24px;
  padding-bottom: 16px;
}

/* 让按钮宽度自适应内容 */
.create-club-cancel-btn,
.create-club-submit-btn {
  width: auto !important;
  min-width: 90px;
  margin-top: 0;
}

</style>