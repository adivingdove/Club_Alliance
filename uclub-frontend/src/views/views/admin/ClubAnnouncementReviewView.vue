<template>
  <div class="announcement-review">
    <h2>社团公告审核</h2>
    <el-table :data="announcements" stripe border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="公告ID" width="100" />
      <el-table-column prop="clubName" label="社团名称" width="150" />
      <el-table-column prop="title" label="公告标题" />
      <el-table-column prop="status" label="审核状态" width="120" />
      <el-table-column prop="createdAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approveAnnouncement(row)">通过</el-button>
          <el-button size="small" type="danger" @click="rejectAnnouncement(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const announcements = ref([
  { id: 1, clubName: '摄影社', title: '夏季摄影大赛', status: '待审核', createdAt: '2025-06-25 10:00' },
  { id: 2, clubName: '篮球社', title: '球赛报名通知', status: '已通过', createdAt: '2025-06-24 15:30' }
])

function approveAnnouncement(row) {
  alert(`通过公告：${row.id}`)
}

function rejectAnnouncement(row) {
  alert(`驳回公告：${row.id}`)
}
</script>

<style scoped>
.announcement-review {
  padding: 20px;
}
</style>
