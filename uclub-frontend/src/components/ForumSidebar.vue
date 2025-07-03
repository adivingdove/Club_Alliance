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

<!-- 热门评论区块 -->
<el-card class="hot-comment-card" style="margin-top: 20px;">
  <template #header>
    <div class="hot-comment-header">
       热门评论
    </div>
  </template>

  <div v-if="hotComments.length">
    <div v-for="comment in hotComments" :key="comment.id" class="hot-comment-item">
      <div class="hot-comment-user">
        <el-avatar :src="getUserAvatar(comment.user?.avatarUrl)" :size="30" />
        <span class="nickname">{{ comment.user?.nickname || '匿名用户' }}</span>
        <span class="likes">👍 {{ comment.likeCount }}</span>
      </div>
      <div class="hot-comment-content">
        <router-link :to="`/post/${comment.postId}#comment/${comment.id}`">
          {{ comment.content.slice(0, 30) }}{{ comment.content.length > 30 ? '...' : '' }}
        </router-link>
      </div>
    </div>
  </div>
  <div v-else style="text-align: center; color: #999;">暂无热门评论</div>
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
// 热门评论数据
const hotComments = ref([])

const loadHotComments = async () => {
  try {
    const res = await fetchHotComments()
    hotComments.value = res
  } catch (err) {
    console.error('加载热门评论失败', err)
    hotComments.value = []
  }
}
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
 async function fetchHotComments(limit = 10) {
  try {
   const res = await publicRequest.get('/api/comments/hot', {
      params: { limit }
    })
    console.log('热门评论数据:', res.data) // 
    return res.data
  } catch (err) {
    console.error('请求热门评论接口出错:', err)
    return []
  }
}
function getUserAvatar(url) {
  return url ? url : "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
}

onMounted(() => {
  fetchHotPosts()
   fetchHotClubs()
   loadHotComments() 
})

</script>

<style scoped>
.sidebar {
  padding: 0 10px;
}
.card-title {

   font-weight: bold;
  font-size: 16px;

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

.hot-comment-card {
  padding: 10px;
  font-size: 14px;
}

.hot-comment-header {
  font-weight: bold;
  font-size: 16px;
  
}

.hot-comment-item {
  margin-bottom: 10px;
  border-bottom: 1px dashed #eee;
  padding-bottom: 6px;
}

.hot-comment-user {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nickname {
  font-weight: 500;
  color: #409eff;
}

.likes {
  margin-left: auto;
  font-size: 13px;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 4px;
}




.hot-comment-content a {
  text-decoration: none;
  color: #333;
  display: inline-block;
  padding: 2px 0;
  transition: color 0.2s;
}

.hot-comment-content a:hover {
  color: #409EFF;
}

.el-avatar {
  border: 1px solid #ddd;
  border-radius: 50%;
  transition: transform 0.3s ease;
}

.el-avatar:hover {
  transform: scale(1.1);
}



</style>
