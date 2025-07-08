<template>
  <div class="manager-bg">
    <el-card class="manager-card">
      <div class="sidebar hot-activity-card">
        <div class="hot-title">🔥 热门活动</div>
        <div class="hot-content">
          <span v-if="adminList[0]">
            <span class="hot-activity-name">社团活动“{{ adminList[0].title }}”</span> 目前是最受欢迎的活动，<br>
            参加活动总人数为 <span class="hot-activity-num">{{ adminList[0].currentParticipants }}</span> 人，欢迎尚未参加该活动的同学们踊跃报名。
          </span>
          <span v-else>暂无热门活动数据</span>
        </div>
      </div>
      <el-table :data="adminList" border stripe class="manager-table" v-loading="loading">
        <el-table-column prop="title" label="活动名称" width="150" align="center" sortable/>
        <el-table-column label="活动描述" width="200" align="center">
          <template v-slot="scope">
            <div class="description-cell" v-html="getShortDescription(scope.row.description)"></div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="活动地点" width="200" align="center"/>
        <el-table-column prop="startTime" label="开始时间" width="180" align="center" sortable>
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}  
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180" align="center" sortable>
           <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}  
          </template>
        </el-table-column>
        <el-table-column prop="maxParticipants" label="参加活动人数限额" width="180" align="center" sortable/>
        <el-table-column prop="currentParticipants" label="参加活动人数" width="180" align="center" sortable/>
        <el-table-column label="剩余名额" width="120" align="center">
          <template v-slot="scope" >
            <div class="remain-num">
              {{ (scope.row.maxParticipants - scope.row.currentParticipants) || 'N/A' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动参与率" width="120" align="center">
          <template v-slot="scope" >
            <div class="rate-num" :class="getRateClass(scope.row.currentParticipants, scope.row.maxParticipants)">
              {{ getParticipationRate(scope.row.currentParticipants, scope.row.maxParticipants) }}
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
        class="manager-pagination"
      />
    </el-card>
  </div>
</template>



<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'  

const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const searchKeyword = ref('')
const showDetailDialog = ref(false)
const adminList = ref([])
const searchName = ref('')
const loading = ref(false)
const hotactivite=ref('')

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const fetchAdmins = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchName.value.trim() !== '') {
      params.clubName = searchName.value.trim()
    }
    const res = await axios.get('/activities', { params }) 
    res.data.sort((a,b) => b.currentParticipants - a.currentParticipants)
    adminList.value = res.data
    } 
    catch (err) {
    ElMessage.error('查询失败，请检查网络或接口')
  } finally {
    loading.value = false
  }
}

// 处理活动描述显示
const getShortDescription = (desc) => {
  if (!desc) return '暂无描述'
  
  // 去除HTML标签，只保留纯文本
  const text = desc.replace(/<[^>]+>/g, '')
  
  // 限制长度，避免表格单元格过宽
  if (text.length > 50) {
    return text.slice(0, 50) + '...'
  }
  
  return text
}

// 计算参与率
const getParticipationRate = (current, max) => {
  if (!max || max === 0) return 'N/A'
  if (!current) return '0%'
  
  const rate = (current / max * 100).toFixed(1)
  return rate + '%'
}

// 获取参与率样式类
const getRateClass = (current, max) => {
  if (!max || max === 0) return ''
  if (!current) return 'rate-low'
  
  const rate = (current / max * 100)
  if (rate > 50) {
    return 'rate-high'
  } else if (rate > 20) {
    return 'rate-medium'
  } else {
    return 'rate-low'
  }
}

// 初始加载
onMounted(() => {
  fetchAdmins()
})


</script>

<style scoped>
.manager-bg {
  min-height: 100vh;
  background: #f7f8fa;
  padding: 40px 0;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
.manager-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(161,140,209,0.10);
  border: none;
  padding: 36px 32px 32px 32px;
  background: #fff;
}
.hot-activity-card {
  background: linear-gradient(90deg, #f3eaff 0%, #fbc2eb 100%);
  border-radius: 18px;
  box-shadow: 0 2px 12px rgba(161,140,209,0.08);
  padding: 24px 24px 18px 24px;
  margin-bottom: 32px;
}
.hot-title {
  font-size: 22px;
  font-weight: 700;
  color: #a18cd1;
  margin-bottom: 10px;
}
.hot-content {
  font-size: 16px;
  color: #555;
  line-height: 1.7;
}
.hot-activity-name {
  color: #a18cd1;
  font-weight: 600;
}
.hot-activity-num {
  color: #fc5c7d;
  font-weight: 700;
}
.manager-table {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(161,140,209,0.06);
  margin-bottom: 24px;
  border: 1.5px solid #f3eaff;
}
.manager-table .el-table__header th {
  background: #fafbfc;
  color: #a18cd1;
  font-weight: 700;
  font-size: 15px;
  border-bottom: 1.5px solid #e0c3fc;
}
.manager-table .el-table__row {
  font-size: 15px;
  color: #444;
}
.manager-table .el-table__row:hover {
  background: #f3eaff;
}
.remain-num {
  color: #6a82fb;
  font-weight: 600;
}
.rate-num {
  font-weight: 700;
}

.rate-high {
  color: #67c23a;
}

.rate-medium {
  color: #e6a23c;
}

.rate-low {
  color: #fc5c7d;
}

.description-cell {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: left;
  padding: 4px 8px;
  line-height: 1.4;
  color: #666;
  font-size: 14px;
}
.manager-pagination {
  margin-top: 24px;
  text-align: right;
}
.manager-pagination .el-pager li.active {
  background: linear-gradient(90deg, #a18cd1 0%, #fbc2eb 100%);
  color: #fff;
  border-radius: 8px;
}
.manager-pagination .el-pager li {
  color: #a18cd1;
  font-weight: 600;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
}
.manager-pagination .el-pager li:hover {
  background: #f3eaff;
  color: #a18cd1;
}
@media (max-width: 900px) {
  .manager-card {
    padding: 16px 4vw 16px 4vw;
    border-radius: 12px;
  }
  .hot-activity-card {
    padding: 14px 8px 10px 8px;
    border-radius: 10px;
  }
  .manager-table {
    border-radius: 8px;
  }
}
</style>
