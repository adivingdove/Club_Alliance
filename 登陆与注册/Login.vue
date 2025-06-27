<template>
  <div class="login-container">
    <!-- 右上角登录按钮/用户头像 -->
    <div class="login-button-container">
      <div v-if="!isLoggedIn" class="login-button" @click="showLoginDialog = true">
        <el-icon><User /></el-icon>
      </div>
      <div v-else class="user-avatar" @click="showUserMenu = !showUserMenu">
        <el-avatar :size="40" :src="getAvatarUrl(userInfo.avatar)" />
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

    <!-- 登录/注册对话框 -->
    <el-dialog
      v-model="showLoginDialog"
      title="用户登录/注册"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item v-if="isRegister" label="邮箱" prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item v-if="isRegister" label="验证码" prop="emailCode">
          <div class="verification-code-container">
            <el-input
              v-model="loginForm.emailCode"
              placeholder="请输入邮箱验证码"
              prefix-icon="Key"
            />
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
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item v-if="isRegister" label="确认密码" prop="confirmPassword">
          <el-input
            v-model="loginForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
      </el-form>

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
        :rules="forgotPasswordRules"
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
        <el-avatar :size="100" :src="getAvatarUrl(userInfo.avatar)" />
        <h3>{{ userInfo.username }}</h3>
        <p>邮箱：{{ userInfo.email }}</p>
        <p>注册时间：{{ userInfo.registerTime }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, SwitchButton, Message, Key } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { 
  login, 
  register, 
  sendRegisterCode as sendRegisterCodeAPI, 
  sendResetCode as sendResetCodeAPI, 
  resetPassword as resetPasswordAPI 
} from '@/api/user'
import request from '@/utils/request'
import axios from 'axios'

const router = useRouter()

// 响应式数据
const isLoggedIn = ref(false)
const showLoginDialog = ref(false)
const showUserMenu = ref(false)
const showUserInfoDialog = ref(false)
const showForgotPasswordDialog = ref(false)
const isRegister = ref(false)
const countdown = ref(0)
const registerCountdown = ref(0)

// 表单数据
const loginForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  emailCode: ''
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
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 默认头像
const userAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

// 表单验证规则
const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== loginForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 忘记密码表单验证规则
const forgotPasswordRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotPasswordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const loginFormRef = ref()
const forgotPasswordFormRef = ref()

// 切换登录/注册模式
const toggleMode = () => {
  isRegister.value = !isRegister.value
  // 清空表单
  loginForm.username = ''
  loginForm.email = ''
  loginForm.password = ''
  loginForm.confirmPassword = ''
  loginForm.emailCode = ''
}

// 处理登录/注册
const handleSubmit = async () => {
  try {
    await loginFormRef.value.validate()
    
    if (isRegister.value) {
      // 注册逻辑
      if (loginForm.password !== loginForm.confirmPassword) {
        ElMessage.error('两次输入密码不一致')
        return
      }
      
      try {
        const response = await register({
          username: loginForm.username,
          email: loginForm.email,
          password: loginForm.password,
          emailCode: loginForm.emailCode
        })
        
        ElMessage.success('注册成功！')
        isRegister.value = false
        showLoginDialog.value = false
        
        // 假设后端返回的 userInfo.avatar 是 /uploads/xxx.png
        const userInfo = response.data.userInfo
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        // 也可以单独存头像
        localStorage.setItem('avatar', userInfo.avatar)
        
      } catch (error) {
        console.error('注册错误:', error)
      }
      
    } else {
      // 登录逻辑
      try {
        const response = await login({
          username: loginForm.username,
          password: loginForm.password
        })
        
        // 保存用户信息和token
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
        
        isLoggedIn.value = true
        userInfo.value.username = response.data.userInfo.username
        userInfo.value.email = response.data.userInfo.email
        userInfo.value.registerTime = response.data.userInfo.registerTime
        userInfo.value.avatar = response.data.userInfo.avatar || userAvatar.value
        
        ElMessage.success('登录成功！')
        showLoginDialog.value = false
        
      } catch (error) {
        console.error('登录错误:', error)
      }
    }
    
    // 清空表单
    loginForm.username = ''
    loginForm.email = ''
    loginForm.password = ''
    loginForm.confirmPassword = ''
    loginForm.emailCode = ''
    
  } catch (error) {
    console.error('表单验证失败:', error)
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
    const res = await request.post('/user/sendResetCode', {
      username :forgotPasswordForm.username,
      email :forgotPasswordForm.email
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
    if (!loginForm.email) {
      ElMessage.error('请先填写邮箱')
      return
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(loginForm.email)) {
      ElMessage.error('请输入正确的邮箱格式')
      return
    }
    const res = await request.post('/user/sendRegisterCode', {
      email :loginForm.email
    })
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
    
    // 验证新密码一致性
    if (forgotPasswordForm.newPassword !== forgotPasswordForm.confirmNewPassword) {
      ElMessage.error('两次输入的新密码不一致')
      return
    }
    
    try {
      await resetPasswordAPI({
        username: forgotPasswordForm.username,
        email: forgotPasswordForm.email,
        verificationCode: forgotPasswordForm.verificationCode,
        newPassword: forgotPasswordForm.newPassword
      })
      
      ElMessage.success('密码重置成功！请使用新密码登录')
      showForgotPasswordDialog.value = false
      
      // 清空表单
      forgotPasswordForm.username = ''
      forgotPasswordForm.email = ''
      forgotPasswordForm.verificationCode = ''
      forgotPasswordForm.newPassword = ''
      forgotPasswordForm.confirmNewPassword = ''
      
    } catch (error) {
      console.error('重置密码错误:', error)
    }
    
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 显示用户信息
const showUserInfo = () => {
  // 跳转到用户中心页面
  router.push('/userMessage')
  showUserMenu.value = false
}

// 退出登录
const logout = () => {
  // 清除本地存储的token和用户信息
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  
  isLoggedIn.value = false
  userInfo.value.username = ''
  userInfo.value.email = ''
  userInfo.value.registerTime = ''
  showUserMenu.value = false
  ElMessage.success('已退出登录')
}

// 页面加载时检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  
  if (token && userInfoStr) {
    try {
      const user = JSON.parse(userInfoStr)
      isLoggedIn.value = true
      userInfo.value.username = user.username
      userInfo.value.email = user.email
      userInfo.value.registerTime = user.registerTime
      userInfo.value.avatar = user.avatar || userAvatar.value
    } catch (error) {
      console.error('解析用户信息失败:', error)
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
}

// 页面加载时执行
checkLoginStatus()

function handleAvatarUploadSuccess(response) {
  if (response.code === 200) {
    const token = localStorage.getItem('token')
    axios.get('/api/user/info', {
      headers: { Authorization: `Bearer ${token}` }
    }).then(res => {
      if (res.data.code === 200) {
        localStorage.setItem('userInfo', JSON.stringify(res.data.data))
        userInfo.value.avatar = res.data.data.avatar
      }
    })
    ElMessage.success('头像上传成功')
  }
}

onMounted(() => {
  const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (stored.avatar) {
    userInfo.value.avatar = stored.avatar
  }
})

onActivated(() => {
  const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (stored.avatar) {
    userInfo.value.avatar = stored.avatar
  }
})

function getAvatarUrl(avatar) {
  if (!avatar) return userAvatar.value
  if (avatar.startsWith('http')) return avatar
  // 拼接后端域名
  return 'http://localhost:8080' + avatar
}

onMounted(refreshUserInfo)
onActivated(refreshUserInfo)

function refreshUserInfo() {
  const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (stored.avatar) {
    userInfo.value.avatar = stored.avatar
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  min-height: 100vh;
}

.login-button-container {
  position: fixed;
  top: 30px;
  right: 30px;
  z-index: 1000;
}

.login-button {
  width: 50px;
  height: 50px;
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
  font-size: 24px;
}

.user-avatar {
  position: relative;
  cursor: pointer;
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

/* 增加表单元素的间距 */
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
