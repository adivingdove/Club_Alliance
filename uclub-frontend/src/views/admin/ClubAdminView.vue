<template>
  <div class="p-6">
    <el-card>
      <template #header>
        <div class="flex justify-between items-center">
          <div class="flex gap-2">
            <el-input
              v-model="searchName"
              placeholder="请输入社团名称"
              clearable
              @keyup.enter="fetchAdmins"
              style="width: 240px"
            />
            <el-button type="primary" @click="fetchAdmins">查询</el-button>
            <el-button @click="reset">重置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="adminList" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="userName" label="用户名" width="120"/>
        <el-table-column prop="clubId" label="社团ID" width="80" />
        <el-table-column prop="clubName" label="社团名称" width="120 "/>
        <el-table-column prop="role" label="角色" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="danger"
              size="small"
              @click="revokeAdmin(row.id)"
              :disabled="row.role === '成员'"
            >
              撤销身份
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'  

const adminList = ref([])
const searchName = ref('')
const loading = ref(false)

const fetchAdmins = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchName.value.trim() !== '') {
      params.clubName = searchName.value.trim()
    }

    const res = await axios.get('/admin/club-members/admins', { params })
    
    adminList.value = res.data
  } catch (err) {
    ElMessage.error('查询失败，请检查网络或接口')
  } finally {
    loading.value = false
  }
}


const reset = () => {
  searchName.value = ''
  fetchAdmins()
}

const revokeAdmin = async (memberId) => {
  try {
    await ElMessageBox.confirm('确定要撤销该管理员的身份吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await axios.put(`/admin/club-members/${memberId}/revoke`)
    ElMessage.success('撤销成功')
    fetchAdmins()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('撤销失败')
    }
  }
}

// 初始加载
onMounted(() => {
  fetchAdmins()
})
</script>

<style scoped>
.card-container {
  padding: 20px;
  max-width: 900px;
  margin: 20px auto;
  background-color: #fff;
  border-radius: 8px;
}

.search-wrapper {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.search-button {
  min-width: 90px;
}

.admin-table {
  font-size: 14px;
}
</style>