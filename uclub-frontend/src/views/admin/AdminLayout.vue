<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed }">
      <el-menu
        :collapse="collapsed"
        router
        default-active="/admin/club-list"
        class="el-menu-vertical-demo"
        background-color="#e3eafc"
        text-color="#185a9d"
        active-text-color="#43cea2"
      >
        <el-sub-menu index="1">
          <template #title>
            <el-icon><User /></el-icon>
            <span v-if="!collapsed">社团管理</span>
          </template>
          <el-menu-item index="/admin/club-list">
            <el-icon><Menu /></el-icon>
            <span v-if="!collapsed">社团列表</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="3">
          <template #title>
            <el-icon><User /></el-icon>
            <span v-if="!collapsed">用户管理</span>
          </template>
          <el-menu-item index="/admin/user-query">
            <el-icon><Search /></el-icon>
            <span v-if="!collapsed">用户查询</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="4">
          <template #title>
            <el-icon><Notification /></el-icon>
            <span v-if="!collapsed">发布管理</span>
          </template>
          <el-menu-item index="/admin/system-announcement">
            <el-icon><Edit /></el-icon>
            <span v-if="!collapsed">系统公告发布</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="5">
          <template #title>
            <el-icon><Checked /></el-icon>
            <span v-if="!collapsed">审核管理</span>
          </template>
          <el-menu-item index="/admin/club-creation-review">
            <el-icon><Plus /></el-icon>
            <span v-if="!collapsed">社团创建申请</span>
          </el-menu-item>
          <el-menu-item index="/admin/activity-review">
            <el-icon><Calendar /></el-icon>
            <span v-if="!collapsed">社团活动申请</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="6">
          <template #title>
            <el-icon><Warning /></el-icon>
            <span v-if="!collapsed">举报管理</span>
          </template>
          <el-menu-item index="/admin/report-handling">
            <el-icon><Ticket /></el-icon>
            <span v-if="!collapsed">举报受理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="7">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span v-if="!collapsed">论坛管理</span>
          </template>
          <el-menu-item index="/admin/post-admin">
            <el-icon><Edit /></el-icon>
            <span v-if="!collapsed">帖子管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>

    <el-button
        class="collapse-btn"
        circle
        icon="Menu"
        @click="collapsed = !collapsed"
        :title="collapsed ? '展开菜单' : '折叠菜单'"
      >
        <el-icon><Menu /></el-icon>
      </el-button>
    </aside>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  User,
  Menu,
  Lock,
  UserFilled,
  Search,
  Notification,
  Edit,
  Document,
  Checked,
  Plus,
  Calendar,
  Warning,
  Ticket,
  Setting
} from '@element-plus/icons-vue'

const collapsed = ref(false)
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.sidebar {
  width: 220px;
  background: linear-gradient(120deg, #e3eafc 0%, #e8f0fe 100%);
  color: #185a9d;
  position: relative;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  border-radius: 0 18px 18px 0;
  box-shadow: 0 8px 32px 0 rgba(67, 206, 162, 0.08);
}

.sidebar.collapsed {
  width: 64px;
}

.el-menu-vertical-demo {
  background-color: transparent;
  border-right: none;
  flex: 1;
  overflow: auto;
}

/* flex布局，图标和文字水平排列 */
.el-sub-menu__title,
.el-menu-item {
  display: flex !important;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  transition: backgrounde 0.2s;
}

/* hover效果 */
.el-sub-menu__title:hover{
  background: rgba(67, 206, 162, 0.08) !important;
  color: #185a9d !important;
}
.el-menu-item:hover {
  background: rgba(67, 206, 162, 0.08) !important;
  color: #185a9d !important;
}

.el-menu-item.is-active {
  background-color: #e3eafc !important;
  color: #43cea2 !important;
  border-radius: 12px;
  font-weight: bold;
}

/* 图标固定大小 */
.el-sub-menu__title > .el-icon,
.el-menu-item > .el-icon {
  flex-shrink: 0;
  width: 14px;
  height: px;
}

/* 折叠状态隐藏文字 */
.sidebar.collapsed .el-sub-menu__title span,
.sidebar.collapsed .el-menu-item span {
  display: none;
}

/* 折叠状态图标居中 */
.sidebar.collapsed .el-menu-item,
.sidebar.collapsed .el-sub-menu__title {
  justify-content: center !important;
}

/* 按钮样式 */
.collapse-btn {
  margin: 12px auto;
  color: white;
  background-color: #409eff;
  border: none;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.3s;
}

.collapse-btn:hover {
  background-color: #66b1ff;
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f5f7fa;
}

</style>
