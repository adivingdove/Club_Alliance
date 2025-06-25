<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from './utils/request.js'

const route = useRoute()
const activeMenu = ref('/')

// 监听路由变化，更新导航栏激活项
watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
}, { immediate: true })

const showLogin = ref(false)
const loginTab = ref('login')

// 登录表单
const loginForm = ref({ email: '', password: '' })
const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// 注册表单
const registerForm = ref({ email: '', password: '', confirm: '', nickname: '' })
const registerRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirm: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

// 用户状态
const currentUser = ref(null)

// 检查是否已登录
const checkLoginStatus = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr)
    } catch (e) {
      localStorage.removeItem('user')
    }
  }
}

// 初始化时检查登录状态
checkLoginStatus()

function login() {
  showLogin.value = true
}

async function handleLogin() {
  try {
    const response = await request.post('/api/users/login', {
      email: loginForm.value.email,
      password: loginForm.value.password
    })
    
    if (response.data.code === 0) {
      // 登录成功
      const user = response.data.data
      localStorage.setItem('user', JSON.stringify(user))
      currentUser.value = user
      ElMessage.success('登录成功')
      showLogin.value = false
      // 清空表单
      loginForm.value = { email: '', password: '' }
    } else {
      ElMessage.error(response.data.msg || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error('登录失败，请检查网络连接')
  }
}

async function handleRegister() {
  if (registerForm.value.password !== registerForm.value.confirm) {
    ElMessage.error('两次输入密码不一致')
    return
  }
  
  try {
    const response = await request.post('/api/users/register', {
      email: registerForm.value.email,
      password: registerForm.value.password,
      nickname: registerForm.value.nickname
    })
    
    if (response.data.code === 0) {
      // 注册成功
      const user = response.data.data
      localStorage.setItem('user', JSON.stringify(user))
      currentUser.value = user
      ElMessage.success('注册成功')
      showLogin.value = false
      // 清空表单
      registerForm.value = { email: '', password: '', confirm: '', nickname: '' }
    } else {
      ElMessage.error(response.data.msg || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
    ElMessage.error('注册失败，请检查网络连接')
  }
}

function logout() {
  localStorage.removeItem('user')
  currentUser.value = null
  ElMessage.success('已退出登录')
}
</script>

<template>
  <el-container class="full-container">
    <el-header class="full-header">
      <div class="nav-flex">
        <el-menu mode="horizontal" background-color="#409EFF" text-color="#fff" active-text-color="#ffd04b" :default-active="activeMenu" class="nav-menu" router>
          <div class="menu-items-container">
            <el-menu-item index="/" class="nav-item">主页</el-menu-item>
            <el-menu-item index="/my-clubs" class="nav-item">我加入的社团</el-menu-item>
          </div>
        </el-menu>
        <div class="nav-user-section">
          <template v-if="currentUser">
            <span class="user-info">欢迎，{{ currentUser.nickname }}</span>
            <el-button type="primary" @click="logout" class="nav-login-btn">退出</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="login" class="nav-login-btn">登录</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="full-main">
      <router-view></router-view>
    </el-main>
    <el-dialog v-model="showLogin" width="400px" :show-close="true" center class="login-dialog">
      <el-tabs v-model="loginTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" label-width="70px" @submit.prevent>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="loginForm.email" autocomplete="off" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" autocomplete="off" placeholder="请输入密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width:100%" @click="handleLogin">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" label-width="70px" @submit.prevent>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerForm.email" autocomplete="off" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="registerForm.nickname" autocomplete="off" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" autocomplete="off" placeholder="请输入密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirm">
              <el-input v-model="registerForm.confirm" type="password" autocomplete="off" placeholder="再次输入密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width:100%" @click="handleRegister">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </el-container>
</template>

<style scoped>
.full-container {
  min-height: 100vh;
  width: 100%;
  background: #f5f7fa;
  margin: 0;
  padding: 0;
}
.full-header {
  width: 100%;
  padding: 0;
  margin: 0;
  border: none;
  height: 80px;
}
.nav-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 80px;
  background: #409EFF;
}
.nav-menu {
  height: 80px;
  font-size: 1.5vw;
  font-weight: bold;
  line-height: 80px;
  flex: 1;
  background: #409EFF;
  border: none;
}
.menu-items-container {
  display: flex;
  width: 400px;
  height: 100%;
}
.nav-item {
  flex: 1;
  font-size: 1.5vw !important;
  font-weight: bold !important;
  height: 80px !important;
  line-height: 80px !important;
  text-align: center !important;
  justify-content: center !important;
  padding: 0 !important;
  display: flex !important;
  align-items: center !important;
}
.nav-item span {
  width: 100%;
  text-align: center;
}
.nav-user-section {
  display: flex;
  align-items: center;
  margin-right: 2vw;
}
.user-info {
  color: white;
  margin-right: 1vw;
  font-size: 1vw;
}
.nav-login-btn {
  font-size: 1.2vw;
  height: 48px;
  padding: 0 32px;
  margin-top: 0 !important;
  background: #409EFF;
  border: 1.5px solid #fff;
}
.full-main {
  width: 100%;
  margin: 0;
  padding: 2% 2% 0 2%;
  box-sizing: border-box;
}
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
.login-dialog .el-dialog__body {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
}
</style>
