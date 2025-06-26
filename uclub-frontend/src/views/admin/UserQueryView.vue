<template>
  <div class="user-query">
    <el-form :model="formData" label-width="120px" class="user-query-form">   
      <el-form-item label="邮箱">
        <el-input v-model="formData.email" placeholder="输入邮箱查询"></el-input>
      </el-form-item>
      <el-form-item label="昵称">
         <el-input v-model="formData.nickname" placeholder="输入昵称查询"></el-input>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="formData.role" placeholder="选择角色">
          <el-option label="普通用户" value="普通用户"></el-option>
          <el-option label="社团管理员" value="社团管理员"></el-option>
          <el-option label="系统管理员" value="系统管理员"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="账号状态">
        <el-select v-model="formData.status" placeholder="选择状态">
          <el-option label="正常" value="正常"></el-option>
          <el-option label="禁言" value="禁言"></el-option>
          <el-option label="封禁" value="封禁"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="queryUsers">查询</el-button>
        <el-button @click="resetQueryForm" style="margin-left: 10px;">清除</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="userList" style="width: 100%">
      <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
      <el-table-column prop="nickname" label="昵称" width="180"></el-table-column>
      <el-table-column prop="role" label="角色" width="120"></el-table-column>
      <el-table-column prop="status" label="状态" width="120"></el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
    </el-table>
  </div>
</template>

<script>
import { ref } from 'vue';
import axios from 'axios';

export default {
  name: 'UserQueryView',
  data() {
  return {
    formData: {
      email: '',
      nickname: '',
      role: '',
      status: ''
    },
    userList: [],
    total: 0,
    pageSize: 10,
    currentPage: 1
  };
},

  methods: {
    async queryUsers() {
      try {
        let url = 'http://localhost:8080/api/users?';
        if (this.formData.email) {
          url += `email=${this.formData.email}&`;
        }
        if(this.formData.nickname) {
          url += `nickname=${this.formData.nickname}&`;
        }
        if (this.formData.role) {
          url += `role=${this.formData.role}&`;
        }
        if (this.formData.status) {
          url += `status=${this.formData.status}&`;
        }
        

        const response = await axios.get(url);
        this.userList = response.data;
      } catch (error) {
        console.error('查询失败', error);
      }
    },
    resetQueryForm() {
      this.formData = {
        nickname: '',
        email: '',
        role: '',
        status: ''
      };
    }

  }
};
</script>

<style scoped>
.user-query-form {
  margin-bottom: 20px;
}
</style>
