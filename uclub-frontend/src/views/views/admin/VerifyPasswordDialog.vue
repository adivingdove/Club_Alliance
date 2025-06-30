<template>
  <el-dialog title="身份验证" v-model="visible" width="30%">
    <el-form :model="form" label-width="75px">
      <el-form-item label="管理员邮箱">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="登录密码">
        <el-input type="password" v-model="form.password" placeholder="请输入密码" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="verifyPassword">验证并继续</el-button>
    </template>
  </el-dialog>
</template>

<script>
import axios from 'axios';

export default {
  name: 'VerifyPasswordDialog',
  props: {
    show: Boolean
  },
  emits: ['verified', 'cancel'],
  data() {
    return {
      visible: false,
      form: {
        email: '',
        password: ''
      }
    };
  },
  watch: {
    show(val) {
      this.visible = val;
    },
    visible(val) {
      if (!val) this.$emit('cancel');
    }
  },
  methods: {
    async verifyPassword() {
      try {
        const res = await axios.post('http://localhost:8080/api/auth/verify-password', {
          email: this.form.email,
          password: this.form.password
        });

        if (res.data === true) {
          this.$message.success("验证成功");
          this.visible = false;
          this.$emit('verified');
        } else {
          this.$message.error("验证失败，请检查邮箱和密码");
        }
      } catch (err) {
        console.error(err);
        this.$message.error("请求出错");
      }
    }
  }
};
</script>
