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
        <el-table-column prop="userName" label="用户名" width="200"/>
        <el-table-column prop="clubName" label="社团名称" width="250 "/>
        <el-table-column prop="role" label="角色" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
           <el-button
                type="warning"
                size="small"
                @click="openChangeRoleDialog(row.id, row.role)"
              >修改身份
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showRoleDialog" title="修改身份" width="30%">
      <el-form label-position="left">
        <el-form-item label="请选择新角色">
          <el-select v-model="selectedRole" placeholder="请选择角色" style="width: 100%">
            <el-option label="成员" value="成员" />
            <el-option label="干事" value="干事" />
            <el-option label="副社长" value="副社长" />
            <el-option label="社长" value="社长" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showRoleDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmChangeRole">确认修改</el-button>
        </div>
      </template>
    </el-dialog>

</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'  

const adminList = ref([])
const searchName = ref('')
const loading = ref(false)

const showRoleDialog = ref(false)
const selectedRole = ref('')
const currentMemberId = ref(null)

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

const openChangeRoleDialog = (memberId, currentRole) => {
  currentMemberId.value = memberId
  selectedRole.value = currentRole
  showRoleDialog.value = true
}

const confirmChangeRole = async () => {
  if (!currentMemberId.value || selectedRole.value === '') {
    ElMessage.warning('请选择有效的角色')
    return
  }

  try {
    // 拼接查询参数
    const url = `/admin/club-members/${currentMemberId.value}/change-role?newRole=${selectedRole.value}`

    const token = localStorage.getItem('token')

    await axios.put(url, null, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    ElMessage.success('角色修改成功')
    showRoleDialog.value = false
    fetchAdmins()
  } catch (err) {
    ElMessage.error('修改失败，请重试')
    console.error('修改角色错误:', err)
  }
}

const reset = () => {
  searchName.value = ''
  fetchAdmins()
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