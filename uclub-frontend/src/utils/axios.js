// src/utils/axios.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080/api', // 可配置 .env
  timeout: 10000
})

// 请求拦截器：自动加 token
instance.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器：统一错误处理
instance.interceptors.response.use(response => {
  return response.data // 只取 data 部分
}, error => {
  if (error.response) {
    const code = error.response.status
    if (code === 401) {
      ElMessage.error('未登录或登录过期')
      window.location.href = '/login'
    } else if (code === 403) {
      ElMessage.error('无权限访问')
    } else {
      ElMessage.error(error.response.data.message || '请求失败')
    }
  } else {
    ElMessage.error('网络错误或服务器无响应')
  }
  return Promise.reject(error)
})

export default instance
