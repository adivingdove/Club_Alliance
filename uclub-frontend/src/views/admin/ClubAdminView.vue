<template>
  <div class="p-6">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="成员管理" name="members">
          <div>成员管理功能已集成到社团详情页</div>
        </el-tab-pane>
        <el-tab-pane label="申请管理" name="applications">
          <div>申请管理功能已集成到社团详情页</div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const activeTab = ref('members')
const clubId = 1 // TODO: 从路由或父组件获取当前社团ID
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