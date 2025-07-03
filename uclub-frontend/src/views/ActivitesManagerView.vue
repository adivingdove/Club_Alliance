<template>
  <div class="p-6">
    <el-card>
      <template #header>
        <div class="flex justify-between items-center">
          <h1 style="font-family:楷体;font-size:50px;">社团活动</h1>        
        </div>
      </template>

    <div class="sidebar">
      <el-card class=".card">
        <div style="font-size:20px;color:red;">
              热门活动:
        </div>
        <div style="font-size:15px;color:red;">
            &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
            社团活动“{{ adminList[0] ? adminList[0].title : '无数据' }}”目前是最受欢迎的活动，
            参加活动总人数为{{ adminList[0] ? adminList[0].currentParticipants : '无数据' }}人，
            欢迎尚未参加该活动的同学们涌跃报名。
                    
        </div>
      </el-card>       
    </div>
      <el-table :data="adminList" border stripe style="width: 100%" v-loading="loading">
      
        <el-table-column prop="title" label="活动名称" width="150" align="center" sortable/>
        <el-table-column prop="description" label="活动描述" width="200" align="center"/>
        <el-table-column prop="location" label="活动地点" width="200" align="center"/>
        <el-table-column prop="startTime" label="开始时间" width="180" align="center" sortable/>
        <el-table-column prop="endTime" label="结束时间" width="180" align="center" sortable/>
        <el-table-column prop="maxParticipants" label="参加活动人数限额" width="180" align="center" sortable/>
        <el-table-column prop="currentParticipants" label="参加活动人数" width="180" align="center" sortable/>
        <el-table-column label="活动参与率" width="200" align="center">
            <template slot-scope="scope" >
    
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
        style="margin-top: 20px; text-align: right;"
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

const fetchAdmins = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchName.value.trim() !== '') {
      params.clubName = searchName.value.trim()
    }

    const res = await axios.get('/activities', { params }) 
       adminList.value = res.data
      
    } catch (err) {
    ElMessage.error('查询失败，请检查网络或接口')
  } finally {
    loading.value = false
  }
}

// 初始加载
onMounted(() => {
  fetchAdmins()
})


</script>



<style scoped>
.sidebar {
  padding: 0 10px;
}
.card-title {
  font-weight: bold;
  font-color:red;
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

.post-list-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-code:red;
  font-weight: bold;
  margin-bottom: 15px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}


.forum-container {
  padding: 20px;
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.post-list-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

</style>
