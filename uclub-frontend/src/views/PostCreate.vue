<template>
  <el-card class="post-create">
    <h2>📝 发布新帖</h2>

    <!-- 标题 -->
    <el-input
      v-model="form.title"
      placeholder="请输入标题"
      size="large"
      style="margin: 20px 0"
      aria-label="帖子标题"
    />

    <!-- 社团选择（只能选择我加入的）-->
    <el-select
      v-model="form.clubId"
      placeholder="请选择社团"
      style="width: 300px; margin-bottom: 20px"
      aria-label="选择社团"
    >
      <el-option
        v-for="club in clubs"
        :key="club.id"
        :label="club.name"
        :value="club.id"
      />
    </el-select>

    <!-- 内容编辑区（富文本）-->
    <div class="editor-container">
      <QuillEditor
        ref="quillEditor"
        v-model:content="form.content"
        :options="editorOptions"
        @ready="onEditorReady"
        @textChange="onEditorChange"
        aria-label="帖子内容"
      />
    </div>

    <!-- 提交按钮 -->
    <div style="margin-top: 20px; text-align: right">
      <el-button @click="$router.back()" aria-label="取消">取消</el-button>
      <el-button type="primary" @click="submitPost" :loading="submitting" aria-label="发布帖子">
        发布帖子
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { createPost } from '@/api/forum'
import { getMyClubs } from '@/api/profileApi'
import Quill from 'quill'
import ImageResize from 'quill-image-resize-module-plus'

// 注册图片调整模块
Quill.register('modules/imageResize', ImageResize)

// Store & Router
const store = useStore()
const router = useRouter()

// 编辑器配置
const editorOptions = {
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],        // 文字格式
      ['blockquote', 'code-block'],                     // 引用和代码块
      [{ 'header': [1, 2, 3, 4, 5, 6, false] }],       // 标题级别
      [{ 'list': 'ordered'}, { 'list': 'bullet' }],    // 有序和无序列表
      [{ 'script': 'sub'}, { 'script': 'super' }],     // 上标/下标
      [{ 'indent': '-1'}, { 'indent': '+1' }],         // 缩进
      [{ 'direction': 'rtl' }],                         // 文字方向
      [{ 'size': ['small', false, 'large', 'huge'] }], // 字体大小
      [{ 'header': [1, 2, 3, 4, 5, 6, false] }],       // 标题
      [{ 'color': [] }, { 'background': [] }],         // 字体颜色和背景颜色
      [{ 'font': [] }],                                // 字体系列
      [{ 'align': [] }],                               // 对齐方式
      ['clean'],                                        // 清除格式
      ['link', 'image']                                // 链接和图片
    ],
    imageResize: {
      displaySize: true,   // 显示图片尺寸
      modules: ['Resize', 'DisplaySize', 'Toolbar']  // 启用调整大小、显示尺寸和工具栏
    }
  },
  theme: 'snow',
  placeholder: '请输入内容...'
}

// 当前用户ID
const userId = computed(() => store.getters.currentUser?.id || null)

// 表单数据
const form = ref({
  clubId: '',
  title: '',
  content: '',
  userId: userId.value
})

// 编辑器ref
const quillEditor = ref(null)

// 用户加入的社团列表
const clubs = ref([])

// 加载状态
const submitting = ref(false)

// 加载用户的社团列表
const loadClubs = async () => {
  try {
    const res = await getMyClubs()
    if (res.data?.code === 200) {
      clubs.value = (res.data.data || []).map(c => ({
        id: c.id,
        name: c.name
      }))
    } else {
      ElMessage.error(res.data?.message || '加载社团列表失败')
    }
  } catch (error) {
    console.error('[加载社团失败]', error)
    ElMessage.error('无法加载社团列表')
  }
}

// 编辑器相关
const onEditorReady = (editor) => {
  console.log('Editor is ready!')
  
  // 配置链接格式化
  editor.clipboard.addMatcher(Node.ELEMENT_NODE, function(node, delta) {
    let ops = []
    delta.ops.forEach(op => {
      if (op.attributes && op.attributes.link) {
        let href = op.attributes.link;
        if (!href.startsWith('http://') && !href.startsWith('https://')) {
          href = 'https://' + href;
        }
        ops.push({
          insert: op.insert,
          attributes: {
            link: href
          }
        });
      } else {
        ops.push(op);
      }
    });
    delta.ops = ops;
    return delta;
  });
}

const onEditorChange = ({ html, text }) => {
  console.log('编辑器内容变化:', html) // 调试用
  form.value.content = html || ''
}

// 提交帖子
const submitPost = async () => {
  // 获取最新的编辑器内容
  const editorContent = quillEditor.value?.getHTML() || form.value.content || ''
  
  // 表单验证
  const title = form.value.title || ''
  const content = editorContent
  const clubId = form.value.clubId

  console.log('提交时的内容:', { title, content, clubId }) // 调试用

  if (!title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!clubId) {
    ElMessage.warning('请选择社团')
    return
  }
  if (!content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  try {
    submitting.value = true
    // 构造请求数据
    const postData = {
      title: title.trim(),
      content: content.trim(),
      clubId: clubId,
      userId: userId.value,
      imageUrlList: [], // 保持空数组
      status: 'active'
    }

    console.log('发送的数据:', postData) // 调试用

    const res = await createPost(postData)
    
    if (res.data?.code === 200) {
      ElMessage.success('发布成功')
      router.push('/forum')
    } else {
      ElMessage.error(res.data?.message || '发布失败')
    }
  } catch (error) {
    console.error('[发布失败]', error)
    if (error.response?.status === 403) {
      ElMessage.error('没有权限发布，请先登录')
    } else {
      ElMessage.error('发布失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 生命周期
onMounted(() => {
  loadClubs()
})
</script>

<style scoped>
.post-create {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
}

.editor-container {
  margin-bottom: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.editor-container :deep(.ql-container) {
  height: 300px;
}

/* 图片和视频调整相关样式 */
.editor-container :deep(.ql-editor) img,
.editor-container :deep(.ql-editor) video {
  display: block;
  max-width: 100%;
  height: auto;
}

.editor-container :deep(.ql-editor) .image-resizer {
  border: 1px dashed #999;
}

.editor-container :deep(.ql-editor) .image-resizer .image-resizer-handle {
  background: #fff;
  border: 1px solid #999;
  border-radius: 2px;
}


/* 图片工具栏样式 */
.editor-container :deep(.ql-toolbar-wrap) {
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 3px;
  padding: 5px;
  margin: 5px;
}

/* 图片尺寸显示样式 */
.editor-container :deep(.image-size-label) {
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

:deep(.el-upload--picture-card) {
  --el-upload-picture-card-size: 100px;
}
</style>

