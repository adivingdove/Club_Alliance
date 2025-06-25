<template>
  <el-row :gutter="'2%'" justify="start">
    <el-col :span="24">
      <h2 class="page-title">我加入的社团</h2>
    </el-col>
    <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="club in myClubs" :key="club.name" class="full-col">
      <el-card shadow="hover" class="full-card">
        <div class="club-title">{{ club.name }}</div>
        <div class="club-desc">{{ club.desc }}</div>
        <div class="club-role">身份：{{ club.role }}</div>
        <div class="club-contact">联系方式：{{ club.contact }}</div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref } from 'vue'

const myClubs = ref([
  { 
    name: '篮球社',
    desc: '热爱篮球，定期举办比赛和训练。',
    contact: 'wechat: basketball_club',
    role: '成员'
  },
  {
    name: '摄影社',
    desc: '用镜头记录美好生活。',
    contact: 'wechat: photo_club',
    role: '管理员'
  }
])
</script>

<style scoped>
.page-title {
  font-size: 2vw;
  color: #409EFF;
  margin-bottom: 2vw;
  padding-left: 1%;
}
.full-col {
  padding-left: 1%;
  padding-right: 1%;
  margin-bottom: 2%;
}
.full-card {
  width: 100%;
  min-height: 180px;
  margin-bottom: 0;
}
.club-title {
  font-size: 1.3vw;
  font-weight: bold;
  margin-bottom: 1vw;
}
.club-desc {
  margin-bottom: 1vw;
  font-size: 1vw;
}
.club-role {
  color: #409EFF;
  font-size: 1vw;
  margin-bottom: 1vw;
}
.club-contact {
  color: #909399;
  font-size: 0.9vw;
}
</style> 