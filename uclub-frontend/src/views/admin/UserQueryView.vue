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
      <el-table-column
        prop="createdAt"
        label="创建时间"
        width="180"
        :formatter="formatTime"
      />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="text" size="small" @click="openStatusDialog(scope.row)">修改状态</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog title="修改用户状态" v-model="statusDialogVisible" width="30%">
  <el-form label-width="80px">
    <el-form-item label="新状态">
      <el-select v-model="selectedStatus" placeholder="请选择新状态">
        <el-option label="正常" value="正常" />
        <el-option label="禁言" value="禁言" />
        <el-option label="封禁" value="封禁" />
      </el-select>
    </el-form-item>
  </el-form>
  <template #footer>
    <el-button @click="statusDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="showVerifyDialog = true">下一步</el-button>
  </template>
</el-dialog>


<verify-password-dialog
  :show="showVerifyDialog"
  @verified="onPasswordVerified"
  @cancel="showVerifyDialog = false"
/>

  </div>
</template>

<script>
import { ref } from 'vue';
import axios from 'axios';
import VerifyPasswordDialog from '@/views/admin/VerifyPasswordDialog.vue';
import dayjs from 'dayjs'

export default {
  name: 'UserQueryView',
  components: {
    VerifyPasswordDialog
  },
  data() {
  return {
    formData: {
      email: '',
      nickname: '',
      role: '',
      status: ''
    },
    userList: [],
    // 状态修改相关
    selectedUserId: null,
    selectedStatus: '',
    statusDialogVisible: false,
    showVerifyDialog: false,
    adminEmail: localStorage.getItem('adminEmail') || '',
    dialogVisible: false,
    showVerifyDialog: false,
    pendingStatusChangeUserId: null,
    newStatus: ''
  };
},

  methods: {
    formatTime(row, column, cellValue) {
      if (!cellValue) return ''
      return dayjs(cellValue).format('YYYY-MM-DD HH:mm:ss')
    },
     isPasswordRecentlyVerified() {
    const lastVerify = localStorage.getItem('lastPasswordVerifyTime');
    if (!lastVerify) return false;

    const now = Date.now();
    const diff = now - parseInt(lastVerify); // 毫秒差

    return diff < 10 * 60 * 1000; // 10分钟以内有效
  },

async queryUsers() {
  try {
    let url = 'http://localhost:8080/api/user?';
    if (this.formData.email) url += `email=${this.formData.email}&`;
    if (this.formData.nickname) url += `nickname=${this.formData.nickname}&`;
    if (this.formData.role) url += `role=${this.formData.role}&`;
    if (this.formData.status) url += `status=${this.formData.status}&`;

    const token = localStorage.getItem('token'); // 从localStorage读取token
    if (!token) {
      console.warn('没有找到token，请先登录');
      return;
    }

    const response = await axios.get(url, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    this.userList = response.data.data; // 你的后端接口返回格式，用户列表在data字段
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
    },

    openStatusDialog(user) {
      this.selectedUserId = user.id;
      this.newStatus = user.status;
      this.statusDialogVisible = true;
    },

  async onPasswordVerified() {
      try {
        // 缓存10分钟
        localStorage.setItem('lastPasswordVerifyTime', Date.now().toString());
        // 关闭验证框
        this.showVerifyDialog = false;

        // 调用接口更新状态
        await axios.put(`http://localhost:8080/api/user/${this.selectedUserId}/status`, {
          status: this.selectedStatus
        });

        this.$message.success("状态更新成功");
        this.statusDialogVisible = false;
        this.queryUsers();
      } catch (err) {
        this.$message.error("更新失败");
      }
    },


  }
};
</script>

<style scoped>
.user-query-form {
  margin-bottom: 20px;
}
</style>
