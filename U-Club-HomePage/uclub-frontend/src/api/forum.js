import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api'  // 后端 Spring Boot 地址

// 获取帖子列表（支持分页和筛选）
export const fetchPostList = async (params = {}) => {
  const queryParams = new URLSearchParams()
  
  // 添加筛选参数
  if (params.title) queryParams.append('title', params.title)
  if (params.clubName) queryParams.append('clubName', params.clubName)
  if (params.timeRange) queryParams.append('timeRange', params.timeRange)
  if (params.page) queryParams.append('page', params.page)
  if (params.pageSize) queryParams.append('pageSize', params.pageSize)
  if (params.startTime) queryParams.append('startTime', params.startTime)
  
  const url = `${BASE_URL}/posts${queryParams.toString() ? '?' + queryParams.toString() : ''}`
  return axios.get(url)
}

// 创建新帖子
export function createPost(data) {
  return axios.post(`${BASE_URL}/posts`, data, {
    headers: {
      'Content-Type': 'application/json'
    },
    withCredentials: false
  })
}

// 获取帖子详情
export function getPostDetail(id) {
  return axios.get(`${BASE_URL}/posts/${id}`)
}

// 点赞帖子
export function likePost(id) {
  return axios.post(`${BASE_URL}/posts/${id}/like`)
}

// 删除帖子
export function deletePost(id, userId) {
  return axios.delete(`${BASE_URL}/posts/${id}`, {
    params: { userId }
  })
}

// 获取用户加入的社团列表
export function getUserClubs(userId) {
  return axios.get(`${BASE_URL}/clubs/joined`, { params: { userId } })
}
