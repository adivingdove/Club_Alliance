<template>
  <div class="report-manage-page">
    <el-card style="width: 100%">
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="举报状态" @change="fetchReports">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="待处理" />
          <el-option label="已处理" value="已处理" />
        </el-select>
      </div>

      <el-table :data="reports" style="width: 100%" v-if="reports.length > 0">
        <el-table-column prop="reporterNickname" label="举报人昵称" width="120" />
        <el-table-column prop="reportedUserNickname" label="被举报人昵称" width="120"/>
        <el-table-column prop="targetType" label="目标类型" width="100" />
        <el-table-column prop="reason" label="举报原因" min-width="200" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="举报时间" :formatter="(row) => formatTime(row.createdAt)" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button type="success" size="small" @click="changeStatus(scope.row.id, '已处理')">设为已处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDetail" title="📋 举报详情" width="600px" class="report-dialog">
  <div v-if="selectedReport" class="report-detail">
    <div class="report-row"><span class="label">举报人昵称：</span><span class="value">{{ selectedReport.reporter.nickname }}</span></div>
    <div class="report-row"><span class="label">被举报人昵称：</span><span class="value">{{ selectedReport.reportedUser.nickname }}</span></div>
    <div class="report-row"><span class="label">举报原因：</span><span>{{ selectedReport.reason }}</span></div>
    <div class="report-row"><span class="label">举报类型：</span>{{ selectedReport.targetType }}</div>

    <div class="report-row">
      <span class="label">举报链接：</span>
      <a v-if="selectedReport.targetType === '帖子'" href="#" @click.prevent="goToPost(selectedReport.targetId)" class="link-btn" target="_blank">查看帖子</a>
      <a v-else-if="selectedReport.targetType === '评论'" href="#" @click.prevent="goToComment(selectedReport.postId, selectedReport.targetId)" class="link-btn" target="_blank">查看评论</a>
      <span v-else class="value">无链接</span>
    </div>
    <div class="report-row"><span class="label">状态：</span>
      <el-tag :type="selectedReport.status === '待处理' ? 'warning' : 'success'">{{ selectedReport.status }}</el-tag>
    </div>
    <div class="report-row"><span class="label">创建时间：</span>{{ formatTime(selectedReport.createdAt) }}</div>
    <div class="report-row">
      <span class="label">操作：</span>
      <el-button type="danger" size="small" @click="banUser(selectedReport.reportedUser.id)">封禁用户</el-button>
      <el-button type="danger" size="small" @click="deleteContent(selectedReport.id)">删除内容</el-button>
    </div>
  </div>
  <div v-else>加载中...</div>
</el-dialog>


  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { useStore} from 'vuex'

const reports = ref([])
const statusFilter = ref('')
const showDetail = ref(false)
const selectedReport = ref(null)
const router = useRouter()
const store = useStore()
const currentUserId = store.state.user.id


const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const fetchReports = async () => {
  const { data } = await axios.get('/report/admin/list', {
    params: { status: statusFilter.value }
  })
  reports.value = data
  console.log('查询到的举报记录:', data)
}

const viewDetail = async (report) => {
  selectedReport.value = null
  showDetail.value = true

  try {
    const res = await axios.get(`/report/${report.id}`)
    console.log('完整响应:', res) 
    selectedReport.value = res  
  } catch (error) {
    showDetail.value = false
    if (error.response) {
      if (error.response.status === 404) {
        ElMessage.error('举报记录不存在')
      } else {
        ElMessage.error(error.response.data?.error || '查询失败')
      }
    } else {
      ElMessage.error('请求举报详情失败')
    }
  }
}

const changeStatus = async (id, status) => {
  await axios.put(`/report/status/${id}`, null, {
    params: { status }
  })
  ElMessage.success('状态更新成功')
  fetchReports()
}

const goToPost = (postId) => {
  router.push(`/post/${postId}`)
}

const goToComment = (postId, commentId) => {
  router.push(`/post/${postId}#comment/${commentId}`)
}

const banUser = async (userId) => {
  try {
    const res = await axios.put(`/user/${userId}/status`, {
      status: '封禁'
    });
    console.log('封禁接口返回:', res);
    if (res && res.code === 200) {
      showDetail.value = false;
      changeStatus(selectedReport.value.id, '已处理');
      ElMessage.success('用户已封禁');
      
      fetchReports();
    } else {
      ElMessage.error(res.data?.message || '封禁用户失败');
    }
  } catch (error) {
    console.error('封禁用户出错:', error);
    ElMessage.error('封禁用户失败，请稍后再试');
  }
}


const deleteContent = async () => {
  if (!selectedReport.value) return;

  const { targetType, targetId } = selectedReport.value;
  
  try {
    if (targetType === '帖子') {
      await axios.delete(`/posts/${targetId}`, {
        params: {
          userId: currentUserId
        }
      });
      ElMessage.success('帖子已删除');
    } else if (targetType === '评论') {
      await axios.delete(`/posts/${selectedReport.value.postId}/comments/${targetId}`, {
        params: {
          userId: currentUserId
        }
      });
      ElMessage.success('评论已删除');
    }
    showDetail.value = false;
    changeStatus(selectedReport.value.id, '已处理');
    fetchReports();
  } catch (error) {
    ElMessage.error('删除内容失败，请稍后再试');
  }
}

onMounted(fetchReports)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 1rem;
}

.report-dialog .report-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 4px;
  font-size: 15px;
  line-height: 1.6;
}

.report-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.label {
  font-weight: 600;
  color: #606266;
  width: 120px;
  min-width: 120px;
}

.value {
  color: #409EFF;
  font-weight: 500;
}

.link-btn {
  color: #409EFF;
  text-decoration: underline;
  font-weight: 500;
  margin-left: 4px;
}

.link-btn:hover {
  color: #66b1ff;
}

</style>
