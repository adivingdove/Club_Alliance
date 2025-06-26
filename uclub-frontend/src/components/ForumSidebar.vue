<template>
  <div class="sidebar">
    <el-card class="card">
      <div class="card-title">热门社团</div>
      <el-tag
        v-for="club in hotClubs"
        :key="club"
        type="success"
        class="tag"
      >
        {{ club }}
      </el-tag>
    </el-card>

    <el-card class="card" style="margin-top: 20px;">
      <div class="card-title">热门帖子</div>
      <ol class="hot-posts">
        <li v-for="(post, index) in hotPosts" :key="index">
          <span class="index">{{ index + 1 }}.</span>
          <a href="#" class="title">{{ post }}</a>
        </li>
      </ol>
    </el-card>
  </div>
</template>

<script setup>
const hotClubs = ['康腾实践中心', '茶艺协会', '摄影兴趣组']
const hotPosts = [
  '一个社区面板调整',
  '推理社招新',
  '茶艺社招新',
  '社团嘉年华'
]
</script>

<style scoped>
.sidebar {
  padding: 0 10px;
}
.card-title {
  font-weight: bold;
  margin-bottom: 10px;
}
.tag {
  margin: 5px 5px 0 0;
}
.hot-posts {
  padding-left: 0;
  list-style: none;
}
.hot-posts li {
  font-size: 14px;
  line-height: 24px;
}
.title {
  color: #333;
  text-decoration: none;
}
.index {
  color: #f56c6c;
  margin-right: 5px;
}
</style>
