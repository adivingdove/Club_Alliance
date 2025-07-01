<template>
  <div class="announcement-container">
    <h2>系统公告管理</h2>

    <!-- 切换按钮 -->
    <div class="toggle-buttons">
      <el-button type="primary" @click="showHistory = false">发布公告</el-button>
      <el-button @click="loadHistory" type="info">查看历史公告</el-button>
    </div>

    <!-- 发布公告表单 -->
    <div v-if="!showHistory">
      <el-input
        v-model="title"
        placeholder="请输入公告标题"
        clearable
        class="input-title"
      />

      <el-upload
        class="upload-btn"
        action="http://localhost:8080/api/admin/upload"
        :show-file-list="false"
        accept="image/*"
        :on-success="handleUploadSuccess"
      >
        <el-button type="primary" icon="el-icon-upload">上传图片并插入</el-button>
      </el-upload>

      <div class="markdown-wrapper">
        <textarea
          v-model="content"
          placeholder="请输入公告内容（支持Markdown）"
          class="markdown-textarea"
        ></textarea>

        <div class="markdown-preview">
          <MarkdownPreview :source="content" />
        </div>
      </div>

      <el-button
        type="success"
        :disabled="!title.trim() || !content.trim()"
        @click="submitAnnouncement"
        class="submit-btn"
      >
        发布公告
      </el-button>
    </div>

    <!-- 历史公告列表 -->
    <div v-else>
      <el-table :data="historyList" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" />
        <el-table-column label="发布时间">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="viewAnnouncement(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="showDetail" title="公告详情" width="600px">
      <h3>{{ detailItem.title }}</h3>
      <div style="margin-bottom: 10px; color: gray;">
        发布时间：{{ formatTime(detailItem.createdAt) }}
      </div>
      <vue3-markdown-it :source="detailItem.content" />
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import MarkdownPreview from './MarkdownPreview.vue'
import { ElMessage } from 'element-plus'
import Vue3MarkdownIt from 'vue3-markdown-it'

const title = ref('')
const content = ref('')
const showHistory = ref(false)
const historyList = ref([])
const showDetail = ref(false)
const detailItem = ref({})


function viewAnnouncement(row) {
  detailItem.value = row
  showDetail.value = true
}


function handleUploadSuccess(res) {
  if (res && res.url) {
    let imageUrl = res.url
    if (!imageUrl.startsWith('http')) {
      imageUrl = `http://localhost:8080${imageUrl}`
    }
    content.value += `\n![](${imageUrl})\n`
  }
}

async function submitAnnouncement() {
  if (!title.value.trim() || !content.value.trim()) {
    ElMessage.error('标题和内容不能为空')
    return
  }

  const payload = {
    title: title.value,
    content: content.value,
    type: '系统',
    clubId: null,
    creatorId: 1 
  }


  try {
    const res = await fetch('http://localhost:8080/api/admin/system-announcements', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) throw new Error('发布失败')

    ElMessage.success('公告发布成功')
    title.value = ''
    content.value = ''
  } catch (error) {
    ElMessage.error(error.message || '网络错误')
  }
}

async function loadHistory() {
  try {
    const raw = await fetch('http://localhost:8080/api/admin/system-announcements')
    const result = await raw.json()
    const data = Array.isArray(result) ? result : result.data || result.body || []

    if (!Array.isArray(data)) {
      console.error('不是数组：', data)
      ElMessage.error('返回数据格式错误')
      return
    }
    historyList.value = data
    showHistory.value = true
  } catch (error) {
    ElMessage.error('加载历史公告失败')
  }
}


function formatTime(str) {
  return new Date(str).toLocaleString()
}
</script>

<style scoped>
.announcement-container {
  max-width: 900px;
  margin: 20px auto;
  padding: 10px;
}

.input-title {
  margin-bottom: 15px;
}

.upload-btn {
  margin-bottom: 15px;
}

.markdown-wrapper {
  display: flex;
  gap: 20px;
}

.markdown-textarea {
  flex: 1;
  height: 300px;
  font-family: monospace;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.markdown-preview {
  flex: 1;
  height: 300px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fafafa;
}

.submit-btn {
  margin-top: 20px;
}

.toggle-buttons {
  margin-bottom: 20px;
}
</style>
