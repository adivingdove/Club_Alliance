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
        <el-button type="primary" @click="handleQueryClick">查询</el-button>
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

    <el-pagination
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
    />

    
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
    <el-button type="primary" @click="updateUserStatus">确认修改</el-button>
  </template>
</el-dialog>

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
      status: '',
      
    },
    userList: [],
    // 状态修改相关
    selectedUserId: null,
    selectedStatus: '',
    statusDialogVisible: false,
    showVerifyDialog: false,
    adminEmail: localStorage.getItem('adminEmail') || '',
    dialogVisible: false,
    pendingStatusChangeUserId: null,
    newStatus: '',
    // 分页相关
    pageSize: 10,
    currentPage: 1,
    total: 0,
  };
},

  methods: {
    formatTime(row, column, cellValue) {
      if (!cellValue) return ''
      return dayjs(cellValue).format('YYYY-MM-DD HH:mm:ss')
    },
    

  async updateUserStatus() {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      this.$message.error('无权限操作，请重新登录');
      return;
    }

    const res = await axios.put(
      `http://localhost:8080/api/user/${this.selectedUserId}/status`,
      { status: this.selectedStatus },
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );

    if (res.data.code === 200) {
      this.$message.success('状态修改成功');
      this.queryUsers(this.currentPage);
    } else {
      this.$message.error(res.data.message || '状态修改失败');
    }
  } catch (err) {
    console.error(err);
    this.$message.error('请求出错');
  }
},

  handleQueryClick() {
    this.currentPage = 1;
    this.queryUsers(1);
  },
  handlePageChange(page) {
    this.currentPage = page;
    this.queryUsers(page);
  },
  async queryUsers(page = 1) {
    try {
      let url = 'http://localhost:8080/api/user?';
      if (this.formData.email) url += `email=${this.formData.email}&`;
      if (this.formData.nickname) url += `nickname=${this.formData.nickname}&`;
      if (this.formData.role) url += `role=${this.formData.role}&`;
      if (this.formData.status) url += `status=${this.formData.status}&`;

      url += `page=${page - 1}&size=${this.pageSize}`;

      const token = localStorage.getItem('token');
      if (!token) return;

      const response = await axios.get(url, {
        headers: { Authorization: `Bearer ${token}` }
      });

      this.userList = response.data.data || [];
      this.total = response.data.total || 0;
      this.pageSize = response.data.size || 10;
      this.currentPage = response.data.page + 1 || 1;
    } catch (error) {
      console.error('查询失败', error);
    }
  },

  openStatusDialog(user) {
    this.selectedUserId = user.id;
    this.selectedStatus = user.status;
    this.statusDialogVisible = true;
},
  resetQueryForm() {
    this.formData = {
      email: '',
      nickname: '',
      role: '',
      status: ''
    };
    this.queryUsers(1);
  },
},


  mounted() {
  this.queryUsers(this.currentPage);
}

};

</script>

<style scoped>
.user-query {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 0.1);
}

.user-query-form {
  margin-bottom: 24px;
}

.el-form-item {
  margin-bottom: 16px;
}

.el-button {
  min-width: 80px;
}

.el-button + .el-button {
  margin-left: 12px;
}

.el-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgb(0 0 0 / 0.1);
}

.el-table th,
.el-table td {
  padding: 12px 16px !important;
  font-size: 14px;
  color: #333;
  vertical-align: middle;
}

.el-table .el-button[type="text"] {
  color: #409EFF;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.3s ease;
}

.el-table .el-button[type="text"]:hover {
  color: #66b1ff;
}

.el-dialog__footer {
  text-align: right;
}

.el-dialog__footer .el-button + .el-button {
  margin-left: 10px;
}
</style>

