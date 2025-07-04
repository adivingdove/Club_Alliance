<template>
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
      <!-- 分类Tab -->
      <el-tabs v-model="activeTab" class="club-tabs" @tab-click="filterClubs">
        <el-tab-pane label="全部社团" name="all" class="tab-left"></el-tab-pane>
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
                <el-button
                  type="primary"
                  size="small"
                  class="join-btn"
                  @click.stop="goToDetail(club.id)"
                >
                  查看详情
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </div>
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
          <el-input v-model="createForm.reason" type="textarea" placeholder="请填写建立理由">
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
          <el-input v-model="createForm.personal" type="textarea" placeholder="请填写你的姓名、联系方式等">
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
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreateClub" class="create-club-submit-btn">
          <el-icon style="margin-right:4px;"><i class="el-icon-plus"></i></el-icon>提交
        </el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'

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
  {
    img: 'https://images.unsplash.com/photo-1464983953574-0892a716854b?auto=format&fit=crop&w=1200&q=80',
    title: '2025 年武汉大学社团招新季',
    desc: '百团大战，等你来选！3月15日-3月30日，武汉大学桂操，不见不散'
  },
  {
    img: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80',
    title: '创新创业社团等你加入',
    desc: '激发你的创造力，和志同道合的伙伴一起成长'
  },
  {
    img: 'https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=crop&w=1200&q=80',
    title: '丰富多彩的文体活动',
    desc: '体育、艺术、公益，总有一款适合你'
  },
  {
    img: 'https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=1200&q=80',
    title: '结识新朋友',
    desc: '在社团中遇见志同道合的伙伴，开启大学新生活'
  },
  {
    img: 'https://images.unsplash.com/photo-1465101178521-c1a9136a3fdc?auto=format&fit=crop&w=1200&q=80',
    title: '展示自我，成就未来',
    desc: '参与社团活动，提升自我能力，收获成长与荣誉'
  }
]

// 禁止弹窗出现时页面滚动
watch(showCreateDialog, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})
</script>

<style scoped>
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
}
.search-bar-wrapper {
  width: 100%;
  background: transparent;
  padding: 32px 0 16px 0;
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
}
.banner-carousel {
  margin: 32px 0 24px 0;
  border-radius: 16px;
  overflow: hidden;
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
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 12px;
}
.banner-content p {
  font-size: 16px;
  margin-bottom: 18px;
}
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
  padding: 32px 0 16px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}
.search-bar-center {
  width: 100%;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}
.main-search-input.beautify-input {
  width: 100%;
  height: 56px;
  border-radius: 0;
  box-shadow: 0 4px 24px 0 rgba(64,158,255,0.13);
  background: #fff;
  font-size: 22px;
  padding: 0 18px 0 18px;
  transition: box-shadow 0.25s, background 0.25s;
  line-height: 56px;
}
.main-search-input.beautify-input:focus-within {
  box-shadow: 0 8px 32px 0 rgba(64,158,255,0.22);
  background: #f4faff;
}
.emoji-search-btn {
  font-size: 30px;
  cursor: pointer;
  margin-right: 2px;
  user-select: none;
  transition: transform 0.15s;
  display: flex;
  align-items: center;
}
.emoji-search-btn:hover {
  transform: scale(1.18) rotate(-8deg);
  filter: brightness(1.2);
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
.clear-btn {
  font-size: 22px;
  color: #bbb;
  cursor: pointer;
  margin-right: 6px;
  user-select: none;
  transition: color 0.18s, transform 0.15s;
  display: inline-flex;
  align-items: center;
}
.clear-btn:hover {
  color: #ff4d4f;
  transform: scale(1.18) rotate(10deg);
}
:deep(.club-tabs .el-tabs__item) {
  font-size: 20px !important;
  font-weight: bold;
  letter-spacing: 1px;
  padding: 0 55px !important;
}
.create-club-dialog >>> .el-dialog {
  position: fixed !important;
  top: 8vh !important;
  left: 0;
  right: 0;
  margin: 0 auto;
  z-index: 2000;
  max-width: 500px;
}
.create-club-dialog >>> .el-dialog__body {
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
  background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  color: #fff;
  font-weight: bold;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.13);
  transition: background 0.2s;
}
.create-club-submit-btn:hover {
  background: linear-gradient(90deg, #66b1ff 0%, #409EFF 100%);
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
</style>