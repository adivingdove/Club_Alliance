<template>
  <div class="club-management">
    <el-card>
      <div class="header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索社团名称..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          style="width: 300px;"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="clubList" style="width: 100%" border>
        <el-table-column prop="name" label="社团名称" />
        <el-table-column prop="creatorNickname" label="创建者昵称" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="changeStatus(row.id, '正常')">通过</el-button>
            <el-button size="small" type="warning" @click="changeStatus(row.id, '已封禁')">封禁</el-button>
            <el-button size="small" type="primary" @click="viewDetail(row.id)">详情</el-button>
            <el-button size="small" type="danger" @click="deleteClub(row.id)">删除</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="社团详情" width="500px">
      <div v-if="clubDetail">
        <p><strong>名称：</strong>{{ clubDetail.name }}</p>
        <p><strong>描述：</strong>{{ clubDetail.description }}</p>
        <p><strong>标签：</strong>{{ clubDetail.tags }}</p>
        <p><strong>创建者昵称：</strong>{{ clubDetail.creatorNickname }}</p>
        <p><strong>状态：</strong>{{ statusMap[clubDetail.status] || clubDetail.status }}</p>
        <p><strong>类型：</strong>{{ clubDetail.type }}</p>
        <p><strong>创建时间：</strong>{{ clubDetail.createdAt }}</p>
        <!-- 其他字段根据需要补充 -->
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios' // 导入axios

const clubList = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const searchKeyword = ref('')
const showDetailDialog = ref(false)
const clubDetail = ref(null)

const statusMap = {
  正常: '正常',
  待审核: '待审核',
  已封禁: '已封禁',
}

const getStatusTagType = (status) => {
  switch (status) {
    case '正常': return 'success'
    case '待审核': return 'warning'
    case '已封禁': return 'danger'
    default: return ''
  }
}

// 拉取分页数据（带搜索关键字）
const fetchClubs = async () => {
  try {
    const res = await axios.get('/clubs/page', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        keyword: searchKeyword.value.trim() || null
      }
    })

    clubList.value = res.content || []
    total.value = res.totalElements || 0
    pageSize.value = res.size || 10
    currentPage.value = res.number + 1 

  } catch (error) {
    ElMessage.error('加载社团失败')
    clubList.value = []
    total.value = 0
  }
}

// 页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchClubs()
}

// 点击搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchClubs()
}

const changeStatus = async (clubId, status) => {
  try {
    await axios.put(`/clubs/${clubId}/status`, null, { params: { status } })
    ElMessage.success('状态修改成功')
    fetchClubs()
  } catch (error) {
    ElMessage.error('状态修改失败')
  }
}

const deleteClub = async (clubId) => {
  try {
    await ElMessageBox.confirm('确定要删除该社团吗？', '删除确认', { type: 'warning' })
    await axios.delete(`/clubs/${clubId}`)
    ElMessage.success('删除成功')
    fetchClubs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const viewDetail = async (clubId) => {
  try {
    const res = await axios.get(`/clubs/${clubId}/detail`)
    if (res.code === 0) {
      clubDetail.value = res.data
      showDetailDialog.value = true
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}


onMounted(() => {
  fetchClubs()
})
</script>

<style scoped>
.club-management {
  padding: 20px;
}
.header {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;
}
</style>
