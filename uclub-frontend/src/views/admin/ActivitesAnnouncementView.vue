<template>
  <div class="announcement-container">
    <h2>社团活动公告管理</h2>

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

      <!-- 富文本编辑器区域 -->
      <div class="editor-container">
        <div class="editor-toolbar">
          <el-button-group class="font-family-group">
            <el-select v-model="fontFamily" placeholder="选择字体" size="small">
              <el-option v-for="font in fontOptions" :key="font" :value="font" :label="font" />
            </el-select>
          </el-button-group>
          
          <el-button-group class="font-size-group">
            <el-select v-model="fontSize" placeholder="字号" size="small">
              <el-option v-for="size in sizeOptions" :key="size" :value="size" :label="size" />
            </el-select>
          </el-button-group>
          
          <el-button-group>
            <el-button size="small" @click="formatText('bold')"><i class="icon-bold"></i></el-button>
            <el-button size="small" @click="formatText('italic')"><i class="icon-italic"></i></el-button>
            <el-button size="small" @click="formatText('underline')"><i class="icon-underline"></i></el-button>
            <el-button size="small" @click="formatText('strikeThrough')"><i class="icon-strikethrough"></i></el-button>
          </el-button-group>
          
          <el-button-group>
            <el-button size="small" @click="formatText('justifyLeft')"><i class="icon-align-left"></i></el-button>
            <el-button size="small" @click="formatText('justifyCenter')"><i class="icon-align-center"></i></el-button>
            <el-button size="small" @click="formatText('justifyRight')"><i class="icon-align-right"></i></el-button>
          </el-button-group>
          
          <el-button-group>
            <el-button size="small" @click="formatText('insertUnorderedList')"><i class="icon-list-ul"></i></el-button>
            <el-button size="small" @click="formatText('insertOrderedList')"><i class="icon-list-ol"></i></el-button>
          </el-button-group>
          
          <el-button-group>
            <el-button size="small" @click="insertImage"><i class="icon-image"></i> 图片</el-button>
          </el-button-group>
          
          <el-button-group>
            <el-button size="small" @click="formatText('createLink')" title="插入链接"><i class="icon-link"></i></el-button>
          </el-button-group>
        </div>
        
        <div 
          ref="editorContent"
          class="editor-content"
          contenteditable="true"
          @input="updateContent"
          @paste="handlePaste"
        ></div>
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
    
    <!-- 图片上传对话框 -->
    <el-dialog v-model="imageDialogVisible" title="插入图片" width="500px">
      <el-upload
        class="upload-demo"
        action="http://localhost:8080/api/admin/upload"
        :on-success="handleUploadSuccess"
        :show-file-list="false"
      >
        <el-button type="primary">点击上传</el-button>
        <div slot="tip" class="el-upload__tip">支持JPG/PNG格式，大小不超过2MB</div>
      </el-upload>
      <div class="image-preview" v-if="imagePreviewUrl">
        <img :src="imagePreviewUrl" alt="预览" />
      </div>
    </el-dialog>
    
    <!-- 公告详情对话框 -->
    <el-dialog v-model="showDetail" title="公告详情" width="600px">
      <h3>{{ detailItem.title }}</h3>
      <div style="margin-bottom: 10px; color: gray;">
        发布时间：{{ formatTime(detailItem.createdAt) }}
      </div>
      <div class="announcement-content" v-html="detailItem.content"></div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'

const title = ref('')
const content = ref('')
const showHistory = ref(false)
const historyList = ref([])
const showDetail = ref(false)
const detailItem = ref({})
const editorContent = ref(null)
const fontFamily = ref('')
const fontSize = ref('')
const imageDialogVisible = ref(false)
const imagePreviewUrl = ref('')

// 字体选项
const fontOptions = ref([
  '宋体', '微软雅黑', '黑体', '楷体', 'Arial', 'Times New Roman'
])

// 字号选项
const sizeOptions = ref([
  '12px', '14px', '16px', '18px', '24px', '32px', '48px'
])

// 格式化文本
function formatText(command, value = null) {
  document.execCommand(command, false, value)
  editorContent.value.focus()
}

// 插入图片
function insertImage() {
  imageDialogVisible.value = true
  imagePreviewUrl.value = ''
}

// 更新内容
function updateContent(e) {
  content.value = e.target.innerHTML
}

// 处理粘贴事件（过滤格式）
function handlePaste(e) {
  e.preventDefault()
  const text = (e.clipboardData || window.clipboardData).getData('text/plain')
  document.execCommand('insertText', false, text)
}

// 图片上传成功处理
function handleUploadSuccess(res) {
  if (res && res.url) {
    let imageUrl = res.url
    if (!imageUrl.startsWith('http')) {
      imageUrl = `http://localhost:8080${imageUrl}`
    }
    
    imagePreviewUrl.value = imageUrl
    
    // 插入图片到编辑器
    setTimeout(() => {
      document.execCommand('insertImage', false, imageUrl)
      imageDialogVisible.value = false
      editorContent.value.focus()
    }, 300)
  }
}

// 查看公告详情
function viewAnnouncement(row) {
  detailItem.value = row
  showDetail.value = true
}

// 提交公告
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
    creatorId: 1 // 示例，实际应从登录状态获取
  }

  try {
    const res = await fetch('http://localhost:8080/api/admin/announcements/system', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) throw new Error('发布失败')

    ElMessage.success('公告发布成功')
    title.value = ''
    content.value = ''
    editorContent.value.innerHTML = ''
  } catch (error) {
    ElMessage.error(error.message || '网络错误')
  }
}

// 加载历史公告
async function loadHistory() {
  try {
    const raw = await fetch('http://localhost:8080/api/admin/announcements/system')
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

// 格式化时间
function formatTime(str) {
  return new Date(str).toLocaleString()
}

// 监听字体变化
watch(fontFamily, (newVal) => {
  if (newVal) {
    document.execCommand('fontName', false, newVal)
    editorContent.value.focus()
  }
})

// 监听字号变化
watch(fontSize, (newVal) => {
  if (newVal) {
    document.execCommand('fontSize', false, 7) // 7是最大字号
    // 找到所有font size=7的元素并设置自定义字号
    const elements = editorContent.value.querySelectorAll('font[size="7"]')
    elements.forEach(el => {
      el.removeAttribute('size')
      el.style.fontSize = newVal
    })
    editorContent.value.focus()
  }
})

onMounted(() => {
  // 初始化编辑器内容
  nextTick(() => {
    if (editorContent.value) {
      editorContent.value.innerHTML = content.value
    }
  })
})
</script>

<style scoped>
.announcement-container {
  max-width: 900px;
  margin: 20px auto;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #2c3e50;
}

.toggle-buttons {
  margin-bottom: 25px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.input-title {
  margin-bottom: 20px;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.editor-toolbar {
  background: #f5f7fa;
  padding: 8px 12px;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.editor-content {
  min-height: 300px;
  padding: 15px;
  overflow-y: auto;
  outline: none;
  line-height: 1.6;
}

.editor-content:focus {
  border-color: #409eff;
}

.font-family-group, .font-size-group {
  margin-right: 10px;
}

.submit-btn {
  display: block;
  margin: 20px auto 0;
  padding: 10px 30px;
  font-size: 16px;
}

.upload-demo {
  margin-bottom: 20px;
}

.image-preview {
  margin-top: 20px;
  text-align: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.announcement-content {
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  min-height: 200px;
  line-height: 1.6;
}

/* 图标样式 */
.icon-bold:before { content: 'B'; font-weight: bold; }
.icon-italic:before { content: 'I'; font-style: italic; }
.icon-underline:before { content: 'U'; text-decoration: underline; }
.icon-strikethrough:before { content: 'S'; text-decoration: line-through; }
.icon-align-left:before { content: 'L'; }
.icon-align-center:before { content: 'C'; }
.icon-align-right:before { content: 'R'; }
.icon-list-ul:before { content: '•'; }
.icon-list-ol:before { content: '1.'; }
.icon-image:before { content: '🖼️'; }
.icon-link:before { content: '🔗'; }

/* 历史公告表格样式 */
.el-table {
  border-radius: 8px;
  overflow: hidden;
}
</style>
