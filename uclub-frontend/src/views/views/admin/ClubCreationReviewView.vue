<template>
  <div class="club-creation-application">
    <h2>社团创建申请管理</h2>
    <el-table :data="applications" stripe border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="申请ID" width="100" />
      <el-table-column prop="applicant" label="申请人" width="120" />
      <el-table-column prop="clubName" label="申请社团名称" width="180" />
      <el-table-column prop="reason" label="申请理由" />
      <el-table-column prop="status" label="审核状态" width="100" />
      <el-table-column prop="submittedAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approveApplication(row)">通过</el-button>
          <el-button size="small" type="danger" @click="rejectApplication(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const applications = ref([
  { id: 1, applicant: '张三', clubName: '文学社', reason: '热爱文学', status: '待审核', submittedAt: '2025-06-20 09:00' },
  { id: 2, applicant: '李四', clubName: '编程社', reason: '技术交流', status: '已驳回', submittedAt: '2025-06-22 14:00' }
])

function approveApplication(row) {
  alert(`通过申请：${row.id}`)
}

function rejectApplication(row) {
  alert(`驳回申请：${row.id}`)
}
</script>

<style scoped>
.club-creation-application {
  padding: 20px;
}
</style>
