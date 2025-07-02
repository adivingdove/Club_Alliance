<template>
  <div class="sidebar">
    <el-card class="card">
      <div class="card-title">热门社团</div>
      <el-tag
    v-for="club in hotClubs"
    :key="club.id"
    type="success"
    class="tag"
    @click="goToClub(club.id)"
    style="cursor: pointer"
  >
       {{ club.name }}
      </el-tag>
    </el-card>

<el-card class="card" style="margin-top: 20px;">
  <div class="card-title">热门帖子</div>
  <ol class="hot-posts">
    <li v-for="(post, index) in hotPosts" :key="post.id" @click="goToPost(post.id)" style="cursor: pointer">
      <span class="index">{{ index + 1 }}.</span>
      <span class="title">{{ post.title }}</span>
    </li>
  </ol>
</el-card>

  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { publicRequest } from '@/utils/request'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

const hotClubs = ref([]) // 修改为响应式

const hotPosts = ref([])

const router = useRouter()

async function fetchHotPosts() {
  try {
    const res = await request.get('/api/posts/hot')
    hotPosts.value = res.data
  } catch (err) {
    console.error('获取热门帖子失败:', err)
  }
}
function goToClub(id) {
  router.push(`/club/${id}`)
}

function goToPost(id) {
  router.push(`/post/${id}`)
}

async function fetchHotClubs() {
  try {
    const res = await publicRequest.get('/api/clubs/hot')
    if (res.data?.code === 0) {
      hotClubs.value = res.data.data || []
    } else {
      console.error('获取热门社团失败:', res.data?.message)
    }
  } catch (err) {
    console.error('获取热门社团失败:', err)
  }
}

onMounted(() => {
  fetchHotPosts()
   fetchHotClubs()
})

</script>

<style scoped>
.sidebar {
  padding: 0 10px;
}
.card-title {
  font-weight: bold;
  margin-bottom: 10px;
}
.tag {
  margin: 5px 5px 0 0;
}
.hot-posts {
  padding-left: 0;
  list-style: none;
}
.hot-posts li {
  font-size: 14px;
  line-height: 24px;
}
.hot-posts li:hover {
  background-color: #f5f7fa;
}

.title {
  color: #333;
  text-decoration: none;
}
.index {
  color: #f56c6c;
  margin-right: 5px;
}
.tag:hover {
  background-color: #e1f3d8;
}

</style>
