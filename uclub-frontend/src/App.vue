<template>
  <NavBar />
  <router-view />
  
  <!-- 全局登录/注册对话框 -->
  <el-dialog
    v-model="showLoginDialog"
    title="用户登录/注册"
    width="500px"
    :close-on-click-modal="false"
    class="global-login-dialog"
    :modal-append-to-body="false"
    :lock-scroll="false"
    :top="'10vh'"
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
        <el-button type="primary" @click="handleSubmit">
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
    class="global-forgot-password-dialog"
    :modal-append-to-body="false"
    :lock-scroll="false"
    :top="'10vh'"
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
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Message, Key } from '@element-plus/icons-vue';
import {
  login,
  register,
  sendRegisterCode as sendRegisterCodeAPI,
  sendResetCode as sendResetCodeAPI,
  resetPassword as resetPasswordAPI
} from '@/api/userApi';
import { useStore } from 'vuex';
import NavBar from './components/NavBar.vue';

const router = useRouter();
const store = useStore();

// 登录相关状态
const showLoginDialog = ref(false);
const showForgotPasswordDialog = ref(false);
const isRegister = ref(false);
const countdown = ref(0);
const registerCountdown = ref(0);

// 表单引用
const loginFormRef = ref();
const forgotPasswordFormRef = ref();
const registerFormRef = ref();

// 表单数据
const registerForm = reactive({
  account: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  emailCode: ''
});

const loginForm = reactive({
  account: '',
  password: ''
});

const forgotPasswordForm = reactive({
  username: '',
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmNewPassword: ''
});

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
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
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
};

const loginRules = {
  account: [
    { required: true, message: '请输入账号或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};

// 切换登录/注册模式
const toggleMode = () => {
  isRegister.value = !isRegister.value;
  // 清空表单
  Object.keys(registerForm).forEach(k => registerForm[k] = '');
  loginForm.account = '';
  loginForm.password = '';
};

// 处理登录/注册
const handleSubmit = async () => {
  try {
    if (isRegister.value) {
      await registerFormRef.value.validate();
      if (registerForm.password !== registerForm.confirmPassword) {
        ElMessage.error('两次输入密码不一致');
        return;
      }
      const res = await register({
        account: registerForm.account,
        email: registerForm.email,
        password: registerForm.password,
        nickname: registerForm.nickname,
        emailCode: registerForm.emailCode,
        headUrl: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
      });
      if (res.data.code === 200) {
        ElMessage.success('注册成功，请登录');
        isRegister.value = false;
        showLoginDialog.value = false;
        Object.keys(registerForm).forEach(k => registerForm[k] = '');
      } else {
        ElMessage.error(res.data.message || '注册失败');
      }
    } else {
      await loginFormRef.value.validate();
      const res = await login({
        account: loginForm.account,
        password: loginForm.password
      });
      if (res.data.code === 200) {
        ElMessage.success('登录成功');
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('user', JSON.stringify(res.data.data));
        store.dispatch('login', res.data.data);
        showLoginDialog.value = false;
        loginForm.account = '';
        loginForm.password = '';
        
        // 触发全局事件，通知其他组件更新用户信息
        window.dispatchEvent(new CustomEvent('userLoginSuccess', { 
          detail: res.data.data 
        }));
        
        // 清空页面内容并刷新
        document.body.innerHTML = '';
        setTimeout(() => {
          window.location.reload();
        }, 1);
      } else {
        ElMessage.error(res.data.message || '登录失败');
      }
    }
  } catch (error) {
    ElMessage.error('请填写所有必填项');
    console.error('登录/注册错误:', error);
  }
};

// 发送验证码（忘记密码）
const sendVerificationCode = async () => {
  try {
    if (!forgotPasswordForm.username || !forgotPasswordForm.email) {
      ElMessage.error('请先填写用户名和邮箱');
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(forgotPasswordForm.email)) {
      ElMessage.error('请输入正确的邮箱格式');
      return;
    }
    await sendResetCodeAPI({
      username: forgotPasswordForm.username,
      email: forgotPasswordForm.email
    });
    ElMessage.success('验证码已发送到您的邮箱');
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) clearInterval(timer);
    }, 1000);
  } catch (error) {
    ElMessage.error('发送验证码失败，请重试');
    console.error('发送验证码错误:', error);
  }
};

// 发送注册验证码
const sendRegisterCode = async () => {
  try {
    if (!registerForm.email) {
      ElMessage.error('请先填写邮箱');
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(registerForm.email)) {
      ElMessage.error('请输入正确的邮箱格式');
      return;
    }
    await sendRegisterCodeAPI({ email: registerForm.email });
    ElMessage.success('注册验证码已发送到您的邮箱');
    registerCountdown.value = 60;
    const timer = setInterval(() => {
      registerCountdown.value--;
      if (registerCountdown.value <= 0) clearInterval(timer);
    }, 1000);
  } catch (error) {
    ElMessage.error('发送验证码失败，请重试');
    console.error('发送注册验证码错误:', error);
  }
};

// 处理重置密码
const handleResetPassword = async () => {
  try {
    await forgotPasswordFormRef.value.validate();
    if (forgotPasswordForm.newPassword !== forgotPasswordForm.confirmNewPassword) {
      ElMessage.error('两次输入的新密码不一致');
      return;
    }
    await resetPasswordAPI({
      username: forgotPasswordForm.username,
      email: forgotPasswordForm.email,
      verificationCode: forgotPasswordForm.verificationCode,
      newPassword: forgotPasswordForm.newPassword
    });
    ElMessage.success('密码重置成功！请使用新密码登录');
    showForgotPasswordDialog.value = false;
    forgotPasswordForm.username = '';
    forgotPasswordForm.email = '';
    forgotPasswordForm.verificationCode = '';
    forgotPasswordForm.newPassword = '';
    forgotPasswordForm.confirmNewPassword = '';
  } catch (error) {
    ElMessage.error('重置密码失败，请重试');
    console.error('重置密码错误:', error);
  }
};

// 监听来自其他组件的登录请求
const handleShowLoginDialog = () => {
  showLoginDialog.value = true;
};

onMounted(() => {
  window.addEventListener('showLoginDialog', handleShowLoginDialog);
});

onUnmounted(() => {
  window.removeEventListener('showLoginDialog', handleShowLoginDialog);
});
</script>

<style scoped>
/* 全局登录弹窗样式 */
.global-login-dialog :deep(.el-dialog),
.global-forgot-password-dialog :deep(.el-dialog) {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgb(0 0 0 / 0.2);
}

.global-login-dialog :deep(.el-dialog__header),
.global-forgot-password-dialog :deep(.el-dialog__header) {
  background: white;
  padding: 20px 30px 10px 30px;
  margin: 0;
  border-bottom: 1px solid #e2e8f0;
}

.global-login-dialog :deep(.el-dialog__title),
.global-forgot-password-dialog :deep(.el-dialog__title) {
  color: #1e293b;
  font-size: 20px;
  font-weight: 600;
}

.global-login-dialog :deep(.el-dialog__headerbtn),
.global-forgot-password-dialog :deep(.el-dialog__headerbtn) {
  color: #64748b;
  font-size: 18px;
}

.global-login-dialog :deep(.el-dialog__body),
.global-forgot-password-dialog :deep(.el-dialog__body) {
  background: white;
  padding: 20px 30px;
  margin: 0;
}

.global-login-dialog :deep(.el-dialog__footer),
.global-forgot-password-dialog :deep(.el-dialog__footer) {
  background: #f8fafc;
  padding: 15px 30px;
  border-top: 1px solid #e2e8f0;
  margin: 0;
}

/* 表单样式 */
.el-form-item {
  margin-bottom: 16px;
}

.el-form-item__label {
  font-weight: 600;
  color: #334155;
}

.el-input,
.el-input__inner {
  border-radius: 6px;
  border: 1.5px solid #cbd5e1;
  transition: border-color 0.2s ease;
}

.el-input:focus-within,
.el-input__inner:focus {
  border-color: #3b82f6;
  outline: none;
}

/* 验证码容器 */
.verification-code-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.verification-code-container .el-input {
  flex: 1;
}

.send-code-btn {
  white-space: nowrap;
  background-color: #3b82f6;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 600;
  color: white;
  transition: background-color 0.3s ease;
}

.send-code-btn:hover {
  background-color: #2563eb;
}

.send-code-btn:disabled {
  background-color: #e2e8f0;
  color: #64748b;
  cursor: not-allowed;
}

/* 忘记密码链接 */
.forgot-password {
  text-align: center;
  margin-top: 12px;
}

.forgot-password .el-link {
  color: #3b82f6;
  font-weight: 500;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  border-radius: 6px;
  font-weight: 600;
  padding: 8px 20px;
  font-size: 14px;
  border: none;
  transition: background-color 0.3s ease;
}

.dialog-footer .el-button--default {
  background-color: #e2e8f0;
  color: #475569;
}

.dialog-footer .el-button--default:hover {
  background-color: #cbd5e1;
}

.dialog-footer .el-button--primary {
  background-color: #3b82f6;
  color: white;
}

.dialog-footer .el-button--primary:hover {
  background-color: #2563eb;
}

.dialog-footer .el-button--success {
  background-color: #10b981;
  color: white;
}

.dialog-footer .el-button--success:hover {
  background-color: #059669;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-login-dialog :deep(.el-dialog),
  .global-forgot-password-dialog :deep(.el-dialog) {
    width: 90vw !important;
    margin: 0 auto;
  }
  
  .global-login-dialog :deep(.el-dialog__body),
  .global-forgot-password-dialog :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  .verification-code-container {
    flex-direction: column;
    gap: 8px;
  }
  
  .send-code-btn {
    width: 100%;
  }
  
  .dialog-footer {
    flex-direction: column;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
}
</style>
