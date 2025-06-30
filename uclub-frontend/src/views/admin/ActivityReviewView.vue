<template>
  <div class="club-activity-application">
    <h2>社团活动申请管理</h2>
    <el-table :data="activityApplications" stripe border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="活动申请ID" width="100" />
      <el-table-column prop="clubName" label="社团名称" width="150" />
      <el-table-column prop="activityTitle" label="活动标题" />
      <el-table-column prop="applicant" label="申请人" width="120" />
      <el-table-column prop="status" label="审核状态" width="100" />
      <el-table-column prop="submittedAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approveActivity(row)">通过</el-button>
          <el-button size="small" type="danger" @click="rejectActivity(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activityApplications = ref([
  { id: 1, clubName: '音乐社', activityTitle: '音乐晚会', applicant: '王五', status: '待审核', submittedAt: '2025-06-23 13:00' },
  { id: 2, clubName: '舞蹈社', activityTitle: '舞蹈表演', applicant: '赵六', status: '已通过', submittedAt: '2025-06-21 16:00' }
])

function approveActivity(row) {
  alert(`通过活动申请：${row.id}`)
}

function rejectActivity(row) {
  alert(`驳回活动申请：${row.id}`)
}
</script>

<style scoped>
.club-activity-application {
  padding: 20px;
}
</style>
