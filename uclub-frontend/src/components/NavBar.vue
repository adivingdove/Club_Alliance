<template>
  <nav class="navbar">
    <ul class="nav-left">
      <li>
        <router-link to="/" class="nav-link" exact-active-class="active-link">首页</router-link>
      </li>
      <li>
        <router-link to="/forum" class="nav-link" active-class="active-link">交流论坛</router-link>
      </li>
      <li>
        <router-link to="/clubs" class="nav-link" active-class="active-link">社团</router-link>
      </li>
      <li>
        <router-link to="/activities" class="nav-link" active-class="active-link">社团活动</router-link>
      </li>
      <li>
        <router-link to="/profile" class="nav-link" active-class="active-link">个人中心</router-link>
      </li>
    </ul>
    <div class="nav-right">
      <input
        type="text"
        placeholder="搜索社团/活动"
        @keydown.enter="goSearch"
        v-model="query"
        aria-label="搜索框"
      />
      <button @click="goSearch" aria-label="搜索按钮" class="search-btn">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          fill="currentColor"
          viewBox="0 0 16 16"
        >
          <path
            d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85zm-5.242 1.658a5 5 0 1 1 0-10 5 5 0 0 1 0 10z"
          />
        </svg>
      </button>

      <!-- 登录按钮 -->
      <button @click="openLoginModal" class="login-btn">登录</button>
      <!-- 后台管理按钮 -->
      <button @click="openManageLogin" class="admin-btn">后台管理</button>
    </div>

    <LoginFloatingWindow v-model:visible = "showLoginModal"/>
    <ManageFloatingWindow v-model:visible = "showManageLogin"/>
  </nav>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import LoginFloatingWindow from '../views/LoginFloatingWindow.vue'; 
import ManageFloatingWindow from '../views/ManageFloatingWindow.vue';

const query = ref('');
const router = useRouter();


const showLoginModal = ref(false); // 用户登录弹窗
const showManageLogin = ref(false);      // 后台管理登录弹窗

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
  background-color: #1e40af;
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
</style>
