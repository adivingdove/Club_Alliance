<template>
  <nav class="navbar">
    <!-- 左侧导航 -->
    <ul class="nav-left">
      <!-- 原导航项 -->
      <li><router-link to="/" class="nav-link" exact-active-class="active-link">首页</router-link></li>
      <li><router-link to="/forum" class="nav-link" active-class="active-link">交流论坛</router-link></li>
  
      <li><router-link to="/activities" class="nav-link" active-class="active-link">社团活动</router-link></li>
      <!-- 追加的导航项（保持样式一致） -->
      <li><router-link to="/ai-chat" class="nav-link" active-class="active-link">专业咨询</router-link></li>
      <li><router-link to="/club-manage" class="nav-link" active-class="active-link">社团管理</router-link></li>
    </ul>

    <!-- 右侧操作 -->
    <div class="nav-right">
      <!-- 登录按钮/头像 -->
      <div class="login-button-container-in-header">
        <div v-if="!isLoggedIn" class="login-button" @click.stop="showLoginDialog = true">
          <el-icon><User /></el-icon>
        </div>
        <div v-else class="user-avatar" ref="avatarRef" @click="openUserMenu">
          <el-avatar :size="52" :src="userAvatar" />
        </div>
        <teleport to="body">
          <div v-if="showUserMenu" class="user-menu" :style="userMenuStyle" @click.stop>
            <div class="menu-item" @click="goToProfile">
              <el-icon><User /></el-icon>
              个人中心
            </div>
            <div class="menu-item" @click="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </div>
          </div>
        </teleport>
      </div>

      <!-- 后台管理按钮 -->
      <button @click.stop="handleAdminClick" class="admin-btn">后台管理</button>
    </div>

    <!-- 弹窗组件 -->
    <VerifyPasswordDialog v-model:show="showVerifyPasswordDialog" />
    
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
  </nav>
</template>


<script setup>
import { ref, onUnmounted, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';

// new
import { reactive, computed } from 'vue'
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

const query = ref('');
const router = useRouter();

const showLoginDialog = ref(false); // 用户登录弹窗
const showManageLogin = ref(false);      // 后台管理登录弹窗


// new 
const store = useStore()
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const showUserMenu = ref(false)
const showForgotPasswordDialog = ref(false)
const isRegister = ref(false)
const countdown = ref(0)
const registerCountdown = ref(0)
const userMenuStyle = ref({})
const avatarRef = ref(null)

// 组件挂载时初始化用户信息
onMounted(() => {
  store.dispatch('initializeApp')
  document.addEventListener('click', handleClickOutside)
})

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

// 用户头像计算属性
const userAvatar = computed(() => {
  const user = userInfo.value
  if (user.headUrl) {
    // 如果是完整的URL（以http开头），直接使用
    if (user.headUrl.startsWith('http')) {
      return user.headUrl
    }
    // 如果是相对路径，添加后端域名
    if (user.headUrl.startsWith('/')) {
      return `http://localhost:8080${user.headUrl}`
    }
    // 其他情况，添加完整路径
    return `http://localhost:8080/uploads/avatars/${user.headUrl}`
  }
  return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

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
const Role ={
  MEMBER: '普通成员',
  ADMIN: '系统管理员'
}

function handleAdminClick() {
  const role = store.state.user.role
  if (role === Role.ADMIN) {
    router.push('/admin/club-list');
  } else {
    ElMessage.warning('您没有权限访问后台管理页面');
  }
}

function goToAdmin(){
  showVerifyDialog.value = false
  router.push('/admin/club-list')
}

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
        emailCode: registerForm.emailCode,
        headUrl: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
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
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data.data))
        store.dispatch('login', res.data.data)
        showLoginDialog.value = false
        loginForm.account = ''
        loginForm.password = ''
        // 立即清除可能显示旧信息的组件状态
        showUserMenu.value = false
        
        // 触发全局事件，通知其他组件更新用户信息
        window.dispatchEvent(new CustomEvent('userLoginSuccess', { 
          detail: res.data.data 
        }))
        
        // 清空页面内容并刷新
        document.body.innerHTML = ''
        setTimeout(() => {
          window.location.reload()
        }, 1)
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

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  store.dispatch('logout')
  showUserMenu.value = false
  ElMessage.success('已退出登录')
  
  // 清空页面内容并刷新
  document.body.innerHTML = ''
  setTimeout(() => {
    window.location.reload()
  }, 1)
  router.push('/') // 跳转到首页
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

// 监听用户登出事件
const handleUserLogout = () => {
  store.dispatch('logout')
  showUserMenu.value = false
}

checkLoginStatus()

// 监听来自其他组件的登录请求
window.addEventListener('showLoginDialog', () => {
  showLoginDialog.value = true
})

// 监听用户登出事件
window.addEventListener('userLogout', handleUserLogout)

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('showLoginDialog', () => {
    showLoginDialog.value = true
  })
  window.removeEventListener('userLogout', handleUserLogout)
  document.removeEventListener('click', handleClickOutside)
})

// original functions
function openLoginModal() {
  showLoginModal.value = true;
}

function openManageLogin(){
  showManageLogin.value = true;
}

function goSearch() {
  if (query.value.trim() !== '') {
    router.push({ name: 'Search', query: { q: query.value.trim() } });
  }
}

// 添加新的方法
function goToProfile() {
  // 跳转到个人中心页面
  router.push('/profile')
  // 关闭用户菜单
  showUserMenu.value = false
}

const openUserMenu = async (event) => {
  await nextTick();
  let rect;
  if (avatarRef.value) {
    rect = avatarRef.value.getBoundingClientRect();
  } else if (event && event.target) {
    rect = event.target.getBoundingClientRect();
  }
  showUserMenu.value = true;
  await nextTick();
  // 获取菜单实际宽度
  const menu = document.querySelector('.user-menu');
  let menuWidth = menu ? menu.offsetWidth : 0;
  if (rect) {
    userMenuStyle.value = {
      position: 'absolute',
      left: `${rect.right - menuWidth}px`,
      top: `${rect.bottom + 8}px`,
      zIndex: 99999,
    };
  }
};

const closeUserMenu = () => {
  showUserMenu.value = false;
};

// 点击空白处关闭菜单
const handleClickOutside = (e) => {
  if (showUserMenu.value) {
    // 判断点击是否在菜单或头像内
    const menu = document.querySelector('.user-menu');
    const avatar = avatarRef.value;
    if (menu && (menu.contains(e.target) || (avatar && avatar.contains(e.target)))) {
      return;
    }
    closeUserMenu();
  }
};

</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 30px;
  background-color: #1e293b; /* 深蓝色背景 */
  box-shadow: 0 2px 6px rgb(0 0 0 / 0.1);
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  overflow: visible;
}

.nav-left {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  gap: 30px;
}

.nav-link {
  color: #cbd5e1; /* 浅灰色 */
  font-weight: 500;
  font-size: 16px;
  position: relative;
  text-decoration: none;
  padding: 4px 0;
  transition: color 0.3s ease;
}

.nav-link::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: -4px;
  width: 0%;
  height: 2px;
  background-color: #60a5fa; /* 蓝色下划线 */
  transition: width 0.3s ease;
  border-radius: 2px;
}

.nav-link:hover {
  color: #60a5fa;
}

.nav-link:hover::after {
  width: 100%;
}

.active-link {
  color: #3b82f6;
  font-weight: 600;
}

.active-link::after {
  width: 100%;
  background-color: #3b82f6;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-left:auto;
}

.nav-right input {
  padding: 6px 12px;
  font-size: 14px;
  border-radius: 4px 0 0 4px;
  border: none;
  outline: none;
  width: 200px;
  transition: box-shadow 0.2s ease;
}

.nav-right input:focus {
  box-shadow: 0 0 6px #3b82f6;
}

.search-btn {
  background-color: #3b82f6;
  border: none;
  padding: 7px 12px;
  cursor: pointer;
  border-radius: 0 4px 4px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s ease;
  color: white;
}

.search-btn:hover {
  background-color: #2563eb;
}

.search-btn svg {
  fill: currentColor;
}

.login-container {
  position: relative;
  min-height: 100vh;
}

.login-button-container-in-header {
  display: flex;
  align-items: center;
  height: 60px;            /* 原来是 80px，更贴合 navbar */
  margin-right: 16px;      /* 缩小右边距 */
  position: relative;
}

.login-button {
  width: 40px;             /* 原为 56px */
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);   /* 轻微减弱阴影 */
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: scale(1.05);   /* 缩小缩放幅度 */
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.2);
}

.login-button .el-icon {
  color: white;
  font-size: 20px;         /* 原为 28px */
}


.admin-btn {
  margin-left: 20px;
  background-color: transparent;
  border: 2px solid #60a5fa;
  color: #60a5fa;
  padding: 6px 16px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.admin-btn:hover {
  background-color: #60a5fa;
  color: white;
}

/* 新增登录按钮样式 */
.login-btn {
  margin-left: 20px;
  background-color: #2563eb;
  border: none;
  color: white;
  padding: 6px 16px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.login-btn:hover {
  background-color: #409EFF;
  color: white;
}

/* 弹窗遮罩 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* 弹窗主体 */
.modal {
  background-color: white;
  border-radius: 10px;
  padding: 30px 30px 20px 30px;
  width: 320px;
  box-shadow: 0 8px 24px rgb(0 0 0 / 0.2);
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

.modal h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #1e293b;
  text-align: center;
}

.modal label {
  display: block;
  margin-bottom: 6px;
  color: #334155;
  font-weight: 600;
}

.modal input {
  width: 100%;
  padding: 8px 10px;
  margin-bottom: 16px;
  border: 1.5px solid #cbd5e1;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.modal input:focus {
  border-color: #3b82f6;
  outline: none;
}

.error-msg {
  color: #dc2626;
  margin-bottom: 12px;
  text-align: center;
  font-weight: 600;
  font-size: 13px;
}

.modal-buttons {
  display: flex;
  justify-content: space-between;
}

.btn-submit,
.btn-cancel {
  padding: 8px 20px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  font-size: 14px;
  border: none;
  transition: background-color 0.3s ease;
}

.btn-submit {
  background-color: #3b82f6;
  color: white;
}

.btn-submit:hover {
  background-color: #2563eb;
}

.btn-cancel {
  background-color: #e2e8f0;
  color: #475569;
}

.btn-cancel:hover {
  background-color: #cbd5e1;
}

/* 用户头像样式 */
.user-avatar {
  position: relative;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

/* 用户菜单样式 */
.user-menu {
  position: absolute;
  right: 0;
  top: 60px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.12);
  z-index: 99999 !important;
  padding: 8px 0;
  width: max-content;
  min-width: 0;
  padding-left: 16px;
  padding-right: 16px;
}

.user-menu::before {
  content: '';
  position: absolute;
  top: -6px;
  right: 20px;
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 6px solid white;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid #f1f5f9;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background-color: #f8fafc;
}

.menu-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #64748b;
}

:deep(.el-dropdown-menu__item),
:deep(.el-dropdown-menu__item *) {
  cursor: pointer !important;
}

:deep(.el-dropdown-menu) {
  z-index: 3000 !important;
}
</style>