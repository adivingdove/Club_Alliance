<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, SwitchButton, Message, Key } from '@element-plus/icons-vue'
import {
  login,
  register,
  sendRegisterCode as sendRegisterCodeAPI,
  sendResetCode as sendResetCodeAPI,
  resetPassword as resetPasswordAPI
} from '@/api/userApi'
import request from '@/utils/request'
import { useStore } from 'vuex'

const store = useStore()
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const showLoginDialog = ref(false)
const showUserMenu = ref(false)
const showUserInfoDialog = ref(false)
const showForgotPasswordDialog = ref(false)
const isRegister = ref(false)
const countdown = ref(0)
const registerCountdown = ref(0)

const registerForm = reactive({
  account: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  emailCode: ''
})
const loginForm = reactive({
  account: '',
  password: ''
})

// 忘记密码表单数据
const forgotPasswordForm = reactive({
  username: '',
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmNewPassword: ''
})

// 用户信息
const userInfo = computed(() => store.getters.currentUser || { username: '', email: '', registerTime: '', avatar: '' })

// 默认头像
const userAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

// 表单验证规则
const registerRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}
const loginRules = {
  account: [
    { required: true, message: '请输入账号或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const loginFormRef = ref()
const forgotPasswordFormRef = ref()
const registerFormRef = ref()

// 切换登录/注册模式
const toggleMode = () => {
  isRegister.value = !isRegister.value
  // 清空表单
  Object.keys(registerForm).forEach(k => registerForm[k] = '')
  loginForm.account = ''
  loginForm.password = ''
}

// 处理登录/注册
const handleSubmit = async () => {
  try {
    if (isRegister.value) {
      await registerFormRef.value.validate()
      if (registerForm.password !== registerForm.confirmPassword) {
        ElMessage.error('两次输入密码不一致')
        return
      }
      const res = await register({
        account: registerForm.account,
        email: registerForm.email,
        password: registerForm.password,
        nickname: registerForm.nickname,
        emailCode: registerForm.emailCode
      })
      if (res.data.code === 200) {
        ElMessage.success('注册成功，请登录')
        isRegister.value = false
        showLoginDialog.value = false
        Object.keys(registerForm).forEach(k => registerForm[k] = '')
      } else {
        ElMessage.error(res.data.message || '注册失败')
      }
    } else {
      await loginFormRef.value.validate()
      const res = await login({
        account: loginForm.account,
        password: loginForm.password
      })
      if (res.data.code === 200) {
        ElMessage.success('登录成功')
        localStorage.setItem('token', res.data.data.token)
        localStorage.setItem('user', JSON.stringify(res.data.data))
        store.dispatch('login', res.data.data)
        showLoginDialog.value = false
        loginForm.account = ''
        loginForm.password = ''
      } else {
        ElMessage.error(res.data.message || '登录失败')
      }
    }
  } catch (error) {
    ElMessage.error('请填写所有必填项')
    console.error('登录/注册错误:', error)
  }
}

// 发送验证码（忘记密码）
const sendVerificationCode = async () => {
  try {
    if (!forgotPasswordForm.username || !forgotPasswordForm.email) {
      ElMessage.error('请先填写用户名和邮箱')
      return
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(forgotPasswordForm.email)) {
      ElMessage.error('请输入正确的邮箱格式')
      return
    }
    await sendResetCodeAPI({
      username: forgotPasswordForm.username,
      email: forgotPasswordForm.email
    })
    ElMessage.success('验证码已发送到您的邮箱')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (error) {
    ElMessage.error('发送验证码失败，请重试')
    console.error('发送验证码错误:', error)
  }
}

// 发送注册验证码
const sendRegisterCode = async () => {
  try {
    if (!registerForm.email) {
      ElMessage.error('请先填写邮箱')
      return
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(registerForm.email)) {
      ElMessage.error('请输入正确的邮箱格式')
    return
  }
    await sendRegisterCodeAPI({ email: registerForm.email })
    ElMessage.success('注册验证码已发送到您的邮箱')
    registerCountdown.value = 60
    const timer = setInterval(() => {
      registerCountdown.value--
      if (registerCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (error) {
    ElMessage.error('发送验证码失败，请重试')
    console.error('发送注册验证码错误:', error)
  }
}

// 处理重置密码
const handleResetPassword = async () => {
  try {
    await forgotPasswordFormRef.value.validate()
    if (forgotPasswordForm.newPassword !== forgotPasswordForm.confirmNewPassword) {
      ElMessage.error('两次输入的新密码不一致')
      return
    }
    await resetPasswordAPI({
      username: forgotPasswordForm.username,
      email: forgotPasswordForm.email,
      verificationCode: forgotPasswordForm.verificationCode,
      newPassword: forgotPasswordForm.newPassword
    })
    ElMessage.success('密码重置成功！请使用新密码登录')
    showForgotPasswordDialog.value = false
    forgotPasswordForm.username = ''
    forgotPasswordForm.email = ''
    forgotPasswordForm.verificationCode = ''
    forgotPasswordForm.newPassword = ''
    forgotPasswordForm.confirmNewPassword = ''
  } catch (error) {
    ElMessage.error('重置密码失败，请重试')
    console.error('重置密码错误:', error)
  }
}

// 显示用户信息
const showUserInfo = () => {
  showUserInfoDialog.value = true
  showUserMenu.value = false
}

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  store.dispatch('logout')
  showUserMenu.value = false
  ElMessage.success('已退出登录')
}

// 页面加载时检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  if (token && userStr) {
    try {
      const user = JSON.parse(userStr)
      store.dispatch('login', user)
    } catch (error) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
}
checkLoginStatus()
</script>

<template>
  <el-container class="full-container">
    <el-header class="full-header">
      <div class="nav-flex">
        <el-menu mode="horizontal" background-color="#409EFF" text-color="#fff" active-text-color="#ffd04b" :default-active="'/'" class="nav-menu" router>
          <div class="menu-items-container">
            <el-menu-item index="/" class="nav-item">主页</el-menu-item>
            <el-menu-item index="/forum" class="nav-item">帖子</el-menu-item>
            <el-menu-item index="/collect-clubs" class="nav-item">我的收藏</el-menu-item>
            <el-menu-item index="/applications" class="nav-item">申请信息</el-menu-item>
          </div>
        </el-menu>
        <!-- 登录/用户头像按钮放到导航栏右上角 -->
        <div class="login-button-container-in-header">
          <div v-if="!isLoggedIn" class="login-button" @click="showLoginDialog = true">
            <el-icon><User /></el-icon>
          </div>
          <div v-else class="user-avatar" @click="showUserMenu = !showUserMenu">
            <el-avatar :size="52" :src="userAvatar" />
            <!-- 用户菜单 -->
            <div v-if="showUserMenu" class="user-menu">
              <div class="menu-item" @click="showUserInfo">
                <el-icon><User /></el-icon>
                个人信息
              </div>
              <div class="menu-item" @click="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-header>
    <el-main class="full-main">
      <router-view></router-view>
    </el-main>
  </el-container>
  <div class="login-container">
    <!-- 登录/注册对话框 -->
    <el-dialog
        v-model="showLoginDialog"
        title="用户登录/注册"
        width="500px"
        :close-on-click-modal="false"
    >
      <template v-if="isRegister">
        <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="100px"
            size="large"
        >
          <el-form-item label="账号" prop="account">
            <el-input v-model="registerForm.account" placeholder="请输入账号" prefix-icon="User" />
          </el-form-item>
            <el-form-item label="邮箱" prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" prefix-icon="Message" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="registerForm.nickname" placeholder="请输入昵称" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" prefix-icon="Lock" show-password />
            </el-form-item>
          <el-form-item label="验证码" prop="emailCode">
            <div class="verification-code-container">
              <el-input v-model="registerForm.emailCode" placeholder="请输入邮箱验证码" prefix-icon="Key" />
              <el-button
                type="primary"
                :disabled="registerCountdown > 0"
                @click="sendRegisterCode"
                class="send-code-btn"
              >
                {{ registerCountdown > 0 ? `${registerCountdown}s` : '发送验证码' }}
              </el-button>
            </div>
            </el-form-item>
          </el-form>
      </template>
      <template v-else>
        <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-width="100px"
            size="large"
        >
          <el-form-item label="账号/邮箱" prop="account">
            <el-input v-model="loginForm.account" placeholder="请输入账号或邮箱" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
        </el-form>
      </template>

      <!-- 忘记密码链接 -->
      <div v-if="!isRegister" class="forgot-password">
        <el-link type="primary" @click="showForgotPasswordDialog = true">
          忘记密码？
        </el-link>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showLoginDialog = false">取消</el-button>
          <el-button type="primary" @click="toggleMode">
            {{ isRegister ? '去登录' : '去注册' }}
          </el-button>
          <el-button type="success" @click="handleSubmit">
            {{ isRegister ? '注册' : '登录' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 忘记密码对话框 -->
    <el-dialog
        v-model="showForgotPasswordDialog"
        title="重置密码"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="forgotPasswordFormRef"
          :model="forgotPasswordForm"
          :rules="{}"
          label-width="100px"
          size="large"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="forgotPasswordForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="forgotPasswordForm.email"
              placeholder="请输入注册邮箱"
              prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="verificationCode">
          <div class="verification-code-container">
            <el-input
                v-model="forgotPasswordForm.verificationCode"
                placeholder="请输入验证码"
                prefix-icon="Key"
            />
            <el-button
                type="primary"
                :disabled="countdown > 0"
                @click="sendVerificationCode"
                class="send-code-btn"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
            </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="forgotPasswordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              prefix-icon="Lock"
              show-password
          />
            </el-form-item>

        <el-form-item label="确认密码" prop="confirmNewPassword">
          <el-input
              v-model="forgotPasswordForm.confirmNewPassword"
              type="password"
              placeholder="请再次输入新密码"
              prefix-icon="Lock"
              show-password
          />
            </el-form-item>
          </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showForgotPasswordDialog = false">取消</el-button>
          <el-button type="success" @click="handleResetPassword">
            重置密码
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户信息对话框 -->
    <el-dialog
        v-model="showUserInfoDialog"
        title="个人信息"
        width="500px"
    >
      <div class="user-info">
        <el-avatar :size="100" :src="userAvatar" />
        <h3>{{ userInfo.username }}</h3>
        <p>邮箱：{{ userInfo.email }}</p>
        <p>注册时间：{{ userInfo.registerTime }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
#app {
  min-height: 100%;
  min-width: 100%;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  background: transparent;
}
.full-container {
  min-height: 100%;
  width: 100%;
  background: transparent;
  margin: 0;
  padding:0;
}
.full-header {
  width: 100%;
  padding: 0;
  margin: 0;
  border: none;
  height: 30px;
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
.full-main {
  width: 100%;
  margin: 0;
  padding: 2% 2% 0 2%;
  box-sizing: border-box;
}
.login-container {
  position: relative;
  min-height: 100vh;
}
.login-button-container-in-header {
  display: flex;
  align-items: center;
  height: 80px;
  margin-right: 32px;
  position: relative;
}
.login-button {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}
.login-button:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}
.login-button .el-icon {
  color: white;
  font-size: 28px;
}
.user-avatar {
  position: relative;
  cursor: pointer;
}
.user-avatar .el-avatar {
  width: 52px !important;
  height: 52px !important;
  font-size: 28px !important;
}
.user-menu {
  position: absolute;
  top: 60px;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  padding: 12px 0;
  min-width: 150px;
  z-index: 1001;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 16px;
}
.menu-item:hover {
  background-color: #f5f7fa;
}
.menu-item .el-icon {
  margin-right: 12px;
  font-size: 18px;
}
.forgot-password {
  text-align: right;
  margin-bottom: 24px;
  font-size: 16px;
}
.verification-code-container {
  display: flex;
  gap: 16px;
}
.send-code-btn {
  white-space: nowrap;
  min-width: 120px;
  font-size: 14px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 24px;
}
.dialog-footer .el-button {
  padding: 12px 24px;
  font-size: 16px;
}
.user-info {
  text-align: center;
  padding: 30px;
}
.user-info h3 {
  margin: 20px 0 12px 0;
  color: #303133;
  font-size: 24px;
}
.user-info p {
  color: #909399;
  margin: 8px 0;
  font-size: 16px;
  line-height: 1.6;
}
:deep(.el-form-item) {
  margin-bottom: 24px;
}
:deep(.el-form-item__label) {
  font-size: 16px;
  font-weight: 500;
}
:deep(.el-input__wrapper) {
  padding: 12px 16px;
}
:deep(.el-input__inner) {
  font-size: 16px;
}
:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
}
:deep(.el-dialog__body) {
  padding: 30px;
}
:deep(.el-dialog__header) {
  padding: 24px 30px 0;
}
:deep(.el-dialog__footer) {
  padding: 0 30px 24px;
}
</style>
