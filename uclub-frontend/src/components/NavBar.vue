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
        <div v-if="!isLoggedIn" class="login-button" @click.stop="showLoginDialog()">
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
    

  </nav>
</template>


<script setup>
import { ref, onUnmounted, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';

import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User, SwitchButton } from '@element-plus/icons-vue'
import { useStore } from 'vuex'

const query = ref('');
const router = useRouter();

const store = useStore()
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const showUserMenu = ref(false)
const userMenuStyle = ref({})
const avatarRef = ref(null)

// 组件挂载时初始化用户信息
onMounted(() => {
  store.dispatch('initializeApp')
  document.addEventListener('click', handleClickOutside)
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

const Role = {
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

// 触发全局登录弹窗
const showLoginDialog = () => {
  window.dispatchEvent(new CustomEvent('showLoginDialog'));
};

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

      if(user.role === '系统管理员'){
        router.push('/admin/club-list') // 跳转到管理员页面
      }else{
        router.push('/') // 跳转到首页
      }
    } catch (error) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
}



checkLoginStatus()

// 组件卸载时移除事件监听器
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

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
/* 导航栏样式 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 40px;
  background: linear-gradient(120deg, #a18cd1 0%, #fbc2eb 100%);
  box-shadow: 0 8px 32px 0 rgba(161,140,209,0.10);
  border-radius: 24px 0px 0px 24px;
  backdrop-filter: blur(8px);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: visible;
  position: relative;
  z-index: 10;
}

.nav-left {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  gap: 30px;
}

.nav-link {
  color: #fff;
  font-weight: 600;
  font-size: 18px;
  letter-spacing: 1px;
  padding: 6px 18px;
  border-radius: 18px;
  transition: background 0.2s, color 0.2s;
  text-decoration: none;
  position: relative;
}

.nav-link:hover, .active-link {
  background: linear-gradient(90deg, #6a82fb 0%, #fc5c7d 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(161,140,209,0.10);
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
  height: 60px;            
  margin-right: 16px;
  position: relative;
}

.login-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255,255,255,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(161,140,209,0.10);
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: scale(1.05);
  background: linear-gradient(90deg, #6a82fb 0%, #fc5c7d 100%);
  box-shadow: 0 6px 14px rgba(251,194,235,0.18);
}

.login-button .el-icon {
  color: #a18cd1;
  font-size: 20px;
}

.admin-btn, .login-btn {
  margin-left: 20px;
  background: rgba(255,255,255,0.7);
  color: #a18cd1;
  border: none;
  border-radius: 18px;
  padding: 8px 22px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(161,140,209,0.10);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.admin-btn:hover, .login-btn:hover {
  background: linear-gradient(90deg, #6a82fb 0%, #fc5c7d 100%);
  color: #fff;
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