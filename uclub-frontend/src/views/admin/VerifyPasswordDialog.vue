<template>
  <el-dialog title="身份验证" v-model="visible" width="30%">
    <el-form :model="form" label-width="85px">
      <el-form-item label="管理员账号">
        <el-input v-model="form.account" placeholder="请输入账号" />
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
  emits: ['update:show', 'verified', 'cancel'],
  data() {
    return {
      visible: false,
      form: {
        account: '',
        password: ''
      }
    };
  },
  watch: {
    show(val) {
      this.visible = val;
    },
    visible(val) {
      this.$emit('update:show', val); // ✅ 通知父组件更新show
      if (!val) this.$emit('cancel');
    }
  },
  methods: {
    async verifyPassword() {
      try {
        const res = await axios.post('http://localhost:8080/api/auth/verify-admin', {
          account: this.form.account,
          password: this.form.password
        });

        if (res.data.code === 200) {
          this.$message.success("验证成功，欢迎系统管理员");
          localStorage.setItem("token", res.data.token);
          localStorage.setItem("userInfo", JSON.stringify(res.data.data));
          this.$emit('verified');  // ✅ 通知父组件
          this.visible = false;
        } else {
          this.$message.error(res.data.message || "验证失败");
        }
      } catch (err) {
        console.error(err);
        this.$message.error("请求出错");
      }
    }
  }
};
</script>
