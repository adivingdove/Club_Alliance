<template>
  <el-container class="main-container no-header-layout">
    <!-- ✅ 加入导航栏 -->
    <el-header class="full-header">
      <div class="nav-flex">
        <el-menu
          mode="horizontal"
          background-color="#409EFF"
          text-color="#fff"
          active-text-color="#ffd04b"
          :default-active="'/'"
          class="nav-menu"
          router
        >
          <div class="menu-items-container">
            <el-menu-item index="/" class="nav-item">主页</el-menu-item>
            <el-menu-item index="/forum" class="nav-item">帖子</el-menu-item>
            <el-menu-item index="/collect-clubs" class="nav-item">我的收藏</el-menu-item>
            <el-menu-item index="/applications" class="nav-item">申请信息</el-menu-item>
          </div>
        </el-menu>
      </div>
    </el-header>

    <!-- 原有页面内容保持不变 -->
    <div class="center-content">
      <!-- 搜索栏 -->
      <div class="search-bar-wrapper">
        <!-- ... 省略部分，保持原样 ... -->
      </div>

      <!-- Banner -->
      <div class="banner">
        <!-- ... -->
      </div>

      <!-- 分类 Tab -->
      <el-tabs v-model="activeTab" class="club-tabs" @tab-click="filterClubs">
        <!-- ... -->
      </el-tabs>

      <!-- 社团卡片区 -->
      <el-main>
        <el-row :gutter="24" class="club-list">
          <!-- ... -->
        </el-row>
      </el-main>
    </div>

    <!-- 悬浮按钮、弹窗等 -->
    <el-button
      class="fab-create-club"
      type="primary"
      circle
      @click="createClub"
      style="position: fixed; right: 40px; bottom: 40px; z-index: 1000; width: 60px; height: 60px; box-shadow: 0 4px 16px rgba(64,158,255,0.2); display: flex; align-items: center; justify-content: center; font-size: 28px;"
    >
      <span style="font-size: 32px;">+</span>
    </el-button>

    <!-- 新建社团弹窗 -->
    <el-dialog v-model="showCreateDialog" title="新建社团" width="500px" :close-on-click-modal="false">
      <!-- ... -->
    </el-dialog>
  </el-container>
</template>


<script setup>

</script>

<style scoped>
.clubs {
  padding: 20px;
}
</style>