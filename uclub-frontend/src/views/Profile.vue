<template>
    <div class="profile">
        <h1>个人中心</h1>
        <p>页面占位符</p>
    </div>
</template>

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

<style scoped>
.profile {
  padding: 20px;
}
</style>