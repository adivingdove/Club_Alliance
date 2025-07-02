<template>
  <div class="club-management">
    <el-card>
      <div class="header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索我的社团..."
          clearable
          @clear="fetchClubs"
          @keyup.enter="fetchClubs"
          style="width: 300px;"
        />
        <el-button type="primary" @click="fetchClubs">搜索</el-button>
        <el-button type="success" @click="openCreateDialog">新建社团</el-button>
      </div>

      <el-table :data="clubList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="社团名称" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row.id)">详情</el-button>
            <el-button size="small" type="primary" @click="editClub(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="manageMembers(row)">成员管理</el-button>
            <el-button size="small" type="danger" @click="deleteClub(row.id)">解散社团</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="社团详情" width="500px">
      <div v-if="clubDetail">
        <p><strong>名称：</strong>{{ clubDetail.name }}</p>
        <p><strong>描述：</strong>{{ clubDetail.description }}</p>
        <p><strong>标签：</strong>{{ clubDetail.tags }}</p>
        <p><strong>状态：</strong>{{ statusMap[clubDetail.status] || clubDetail.status }}</p>
        <p><strong>类型：</strong>{{ clubDetail.type }}</p>
        <p><strong>创建时间：</strong>{{ clubDetail.createdAt }}</p>
      </div>
    </el-dialog>

    <el-dialog v-model="showMemberDialog" title="成员管理" width="600px">
      <el-table :data="memberList" style="width: 100%">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-select v-model="row.role" size="small" @change="role => setMemberRole(row, role)">
              <el-option label="成员" value="成员" />
              <el-option label="干事" value="干事" />
              <el-option label="副社长" value="副社长" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button v-if="row.role !== '社长'" size="small" type="danger" @click="transferPresident(row)">转让社长</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'

const clubList = ref([])
const searchKeyword = ref('')
const showDetailDialog = ref(false)
const clubDetail = ref(null)
const showMemberDialog = ref(false)
const memberList = ref([])
let currentClubId = null

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

const fetchClubs = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (!user.id) {
      ElMessage.error('请先登录')
      return
    }
    const res = await axios.get(`/clubs/creator/${user.id}`)
    let data = res.data || []
    if (searchKeyword.value.trim()) {
      data = data.filter(club => club.name.includes(searchKeyword.value.trim()))
    }
    clubList.value = data
  } catch (error) {
    ElMessage.error('加载社团失败')
    clubList.value = []
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

const openCreateDialog = () => {
  ElMessage.info('新建社团功能待实现')
}

const editClub = (row) => {
  ElMessage.info('编辑社团功能待实现')
}

const manageMembers = async (club) => {
  currentClubId = club.id
  // 获取成员列表
  const res = await axios.get(`/clubs/${club.id}/detail`)
  memberList.value = res.data.members || []
  showMemberDialog.value = true
}

const setMemberRole = async (member, role) => {
  try {
    await axios.put(`/clubs/${currentClubId}/members/${member.id}/role`, { creatorId: memberList.value.find(m => m.role === '社长').userId, role })
    ElMessage.success('角色设置成功')
    manageMembers({ id: currentClubId })
  } catch (e) {
    ElMessage.error('角色设置失败')
  }
}

const transferPresident = async (member) => {
  try {
    // 判断当前用户是否为社长
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const president = memberList.value.find(m => m.role === '社长')
    if (!president || Number(user.id) !== Number(president.userId)) {
      ElMessage.error('只有社长本人才能转让社长身份')
      return
    }
    await axios.put(`/clubs/${currentClubId}/transfer-president`, {
      fromUserId: president.userId,
      toUserId: member.userId
    })
    ElMessage.success('社长已转让')
    manageMembers({ id: currentClubId })
    fetchClubs()
  } catch (e) {
    ElMessage.error('转让失败')
  }
}

const deleteClub = async (clubId) => {
  try {
    await ElMessageBox.confirm('确定要解散该社团吗？', '删除确认', { type: 'warning' })
    await axios.delete(`/clubs/${clubId}`)
    ElMessage.success('解散成功')
    fetchClubs()
  } catch (error) {
    ElMessage.error('解散失败')
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
