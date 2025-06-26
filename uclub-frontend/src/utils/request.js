import axios from 'axios'

const request = axios.create({
  baseURL: '/api', // 由 Vite 代理到 Spring Boot
  timeout: 5000
})

request.interceptors.response.use(
  res => res.data,
  err => {
    console.error('请求失败:', err)
    return Promise.reject(err)
  }
)

export default request
