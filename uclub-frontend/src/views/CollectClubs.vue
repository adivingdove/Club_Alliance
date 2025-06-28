<template>
  <el-row :gutter="'2%'" justify="start">
    <el-col :span="24">
      <h2 class="page-title">我的收藏</h2>
    </el-col>
    <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="club in favoriteClubs" :key="club.id" class="full-col">
      <el-card shadow="hover" class="full-card" @click="goToDetail(club.id)">
        <img :src="club.img || '/logo.png'" class="club-img" />
        <div class="club-info">
          <div class="club-title">{{ club.name }}</div>
          <div class="club-desc">{{ club.description }}</div>
          <div class="club-tags">{{ club.tags }}</div>
        </div>
      </el-card>
    </el-col>
    <el-col :span="24" v-if="favoriteClubs.length === 0">
      <el-empty description="暂无收藏的社团" />
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const favoriteClubs = ref([])

// 获取用户收藏的社团
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
        let imgUrl = club.logoUrl || '/logo.png'
        if (imgUrl.startsWith('/upload/')) {
          imgUrl = 'http://localhost:8080' + imgUrl
        }
        return {
          ...club,
          img: imgUrl
        }
      })
    } else {
      ElMessage.error('获取收藏列表失败')
    }
  } catch (e) {
    console.error('获取收藏列表失败:', e)
    ElMessage.error('获取收藏列表失败')
  }
}

// 跳转到社团详情
const goToDetail = (clubId) => {
  router.push(`/club/${clubId}`)
}

onMounted(() => {
  fetchFavoriteClubs()
})
</script>

<style scoped>
.page-title {
  font-size: 2vw;
  color: #409EFF;
  margin-bottom: 2vw;
  padding-left: 1%;
}
.full-col {
  padding-left: 1%;
  padding-right: 1%;
  margin-bottom: 2%;
}
.full-card {
  width: 100%;
  min-height: 200px;
  margin-bottom: 0;
  cursor: pointer;
  transition: transform 0.2s;
}
.full-card:hover {
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
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.club-tags {
  color: #409EFF;
  font-size: 0.9vw;
  font-weight: 500;
}
</style> 