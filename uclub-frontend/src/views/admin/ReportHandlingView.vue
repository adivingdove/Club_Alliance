<template>
  <div class="report-handling">
    <h2>举报受理</h2>
    <el-table :data="reports" stripe border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="举报ID" width="100" />
      <el-table-column prop="reporter" label="举报人" width="120" />
      <el-table-column prop="target" label="被举报对象" width="150" />
      <el-table-column prop="reason" label="举报原因" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleReport(row)">处理</el-button>
          <el-button size="small" type="danger" @click="rejectReport(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const reports = ref([
  // 模拟数据，后续改成接口请求
  { id: 1, reporter: '用户A', target: '帖子123', reason: '垃圾广告', status: '待处理' },
  { id: 2, reporter: '用户B', target: '用户C', reason: '恶意攻击', status: '处理中' }
])

function handleReport(row) {
  alert(`处理举报：${row.id}`)
}

function rejectReport(row) {
  alert(`驳回举报：${row.id}`)
}
</script>

<style scoped>
.report-handling {
  padding: 20px;
}
</style>
