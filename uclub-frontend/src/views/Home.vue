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
                <el-button type="primary" size="small" class="join-btn">加入社团</el-button>
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
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :show-file-list="false"
          >
            <el-button type="primary">上传图片</el-button>
          </el-upload>
          <!-- 图片预览 -->
          <div v-if="createForm.logoUrl" style="margin-top: 10px;">
            <img 
              :src="getImageUrl(createForm.logoUrl)" 
              style="max-width: 200px; max-height: 150px; border-radius: 8px; border: 1px solid #ddd;"
              alt="社团图片预览"
            />
            <p style="margin-top: 5px; font-size: 12px; color: #666;">图片预览</p>
          </div>
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
  </el-container>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
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

const store = useStore()
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const userInfo = computed(() => store.getters.currentUser)

const fetchClubs = async () => {
  try {
    const res = await request.get('/api/clubs')
    if (res.data.code === 0) {
      clubs.value = (res.data.data || []).map(club => {
        let imgUrl = club.logoUrl || '/logo.png'
        console.log('原始logoUrl:', club.logoUrl)
        if (imgUrl && imgUrl.startsWith('/uploads/')) {
          imgUrl = 'http://localhost:8080' + imgUrl
          console.log('处理后的imgUrl:', imgUrl)
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

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'X-Requested-With': 'XMLHttpRequest',
    'Authorization': token ? `Bearer ${token}` : ''
  }
})
const uploadAction = 'http://localhost:8080/api/upload' // 你需要有后端上传接口
const handleUploadSuccess = (response) => {
  console.log('上传成功响应:', response)
  if (response.code === 0 && response.url) {
    createForm.value.logoUrl = response.url
    console.log('设置logoUrl:', response.url)
    ElMessage.success('图片上传成功')
  } else {
    console.error('上传响应格式错误:', response)
    ElMessage.error('图片上传失败')
  }
}
const handleUploadError = (error) => {
  console.error('上传错误详情:', error)
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
.banner {
  width: 100%;
  max-width: none;
  margin-left: 0;
  margin-right: 0;
  border-radius: 0;
  background: #fff;
}
.club-tabs {
  width: 100%;
  max-width: none;
  margin-left: 0;
  margin-right: 0;
  border-radius: 0;
  /* 让标签均匀分布 */
  display: flex;
  justify-content: space-between;
  background: transparent;
}
.club-tabs .el-tabs__header {
  width: 100%;
  display: flex;
  justify-content: center;
}
.club-tabs .el-tabs__nav {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
.club-tabs .el-tabs__item {
  flex: 0 1 auto;
  min-width: 0 !important;
  text-align: center;
  font-size: 26px;
  padding: 0 0 !important;
  height: 64px;
  line-height: 64px;
  margin: 0;
}
.club-tabs .el-tabs__item.tab-left {
  margin-right: auto !important;
}
.club-list {
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}
@media (max-width: 900px) {
  .banner,
  .club-tabs .el-tabs__item{
    font-size: 18px;
    height: 44px;
    line-height: 44px;
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
  border-radius: 36px;
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
@media (max-width: 900px) {
  .club-tabs .el-tabs__item {
    font-size: 18px;
    height: 44px;
    line-height: 44px;
  }
}
.club-tabs .el-tabs__item:first-child {
  margin-right: auto !important;
}
</style>