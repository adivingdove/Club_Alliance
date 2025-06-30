import axios from 'axios'
import { ElMessage } from 'element-plus'


const request = axios.create({
  baseURL: 'http://localhost:8080', // 后端服务地址
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 不需要 token 的接口
    const noAuthUrls = [
      '/api/user/sendRegisterCode',
      '/api/user/register',
      '/api/user/login',
      '/api/user/sendResetCode'
    ]
    // 只有不在 noAuthUrls 里的接口才加 token
    if (!noAuthUrls.some(url => config.url && config.url.includes(url))) {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response
  },
  error => {
    console.error('响应错误:', error)
    console.error('错误详情:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      headers: error.response?.headers,
      config: {
        url: error.config?.url,
        method: error.config?.method,
        headers: error.config?.headers
      }
    })
    
    if (error.response) {
      // 服务器返回错误状态码
      const { status, data } = error.response
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('user')
          localStorage.removeItem('token')
          break
        case 403:
          ElMessage.error('拒绝访问 - 请检查您的权限或重新登录')
          console.error('403错误详情:', {
            url: error.config?.url,
            token: error.config?.headers?.Authorization ? '存在' : '不存在',
            tokenValue: error.config?.headers?.Authorization?.substring(0, 20) + '...'
          })
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.msg || '请求失败')
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 其他错误
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

// 封装常用的请求方法
export const http = {
  get(url, params) {
    return request.get(url, { params })
  },
  
  post(url, data) {
    return request.post(url, data)
  },
  
  put(url, data) {
    return request.put(url, data)
  },
  
  delete(url) {
    return request.delete(url)
  }
}


export default request
