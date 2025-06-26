<template>
  <el-card class="post-create">
    <h2>📝 发布新帖</h2>

    <!-- 标题 -->
    <el-input
      v-model="form.title"
      placeholder="请输入标题"
      size="large"
      style="margin: 20px 0"
    />

    <!-- 社团选择 -->
    <el-select
      v-model="form.club_id"
      placeholder="请选择社团"
      style="width: 300px; margin-bottom: 20px"
    >
      <el-option v-for="club in clubs" :key="club.id" :label="club.name" :value="club.id" />
    </el-select>

    <!-- 内容编辑区 -->
    <el-input
      v-model="form.content"
      type="textarea"
      :rows="10"
      placeholder="请输入 Markdown 格式正文内容"
    />

    <!-- 实时预览 -->
    <el-divider>实时预览</el-divider>
    <vue3-markdown-it :source="form.content" />

    <!-- 图片上传 -->
    <el-upload
      action="/api/upload"
      list-type="picture-card"
      :limit="5"
      :on-success="handleUploadSuccess"
      :on-remove="handleUploadRemove"
      multiple
    >
      <i class="el-icon-plus" />
    </el-upload>

    <!-- 提交按钮 -->
    <div style="margin-top: 20px; text-align: right">
      <el-button type="primary" @click="submitPost">发布帖子</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import Vue3MarkdownIt from 'vue3-markdown-it'
import { createPost } from '@/api/forum'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({
  club_id: '',
  title: '',
  content: '',
  image_urls: [],
  user_id: 1001, //  临时测试用户 ID
})

const clubs = ref([])

const loadClubs = () => {
  clubs.value = [
    { id: 1, name: '摄影社' },
    { id: 2, name: '编程协会' },
  ]
}

onMounted(loadClubs)

const handleUploadSuccess = (res, file) => {
  form.value.image_urls.push(res.url)
}

const handleUploadRemove = (file) => {
  const url = file.response?.url
  form.value.image_urls = form.value.image_urls.filter((img) => img !== url)
}

const submitPost = async () => {
  const payload = {
    ...form.value,
    image_urls: JSON.stringify(form.value.image_urls)  
  }

  console.log('准备发送 POST 请求，数据为：', payload) // 打印请求体

  try {
    const res = await createPost(payload)
    console.log('后端返回结果：', res) //  打印返回数据

    if (res.data.code === 200) {
      ElMessage.success('发布成功')
      // 跳转
      router.push('/forum')

      form.value = { club_id: '', title: '', content: '', image_urls: [], user_id: 1001 }
    } else {
      ElMessage.error(res.data.message || '发布失败')
    }
  } catch (err) {
    //  打印详细错误信息
    console.error('请求失败：', err)
    if (err.response) {
      console.error('状态码：', err.response.status)
      console.error('响应体：', err.response.data)
      ElMessage.error(`后端错误 ${err.response.status}：${JSON.stringify(err.response.data)}`)
    } else if (err.request) {
      console.error('请求已发送但无响应：', err.request)
      ElMessage.error('请求已发送但后端无响应')
    } else {
      console.error('配置错误或其他异常：', err.message)
      ElMessage.error('请求配置异常：' + err.message)
    }
  }
}


</script>

<style scoped>
.post-create {
  padding: 30px;
}
</style>
