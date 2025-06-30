import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api'  // 后端 Spring Boot 地址

// 获取帖子列表（支持分页和筛选）
export const fetchPostList = async (params = {}) => {
  const token = localStorage.getItem('token')
  const queryParams = new URLSearchParams()
  if (params.title) queryParams.append('title', params.title)
  if (params.clubName) queryParams.append('clubName', params.clubName)
  if (params.page) queryParams.append('page', params.page)
  if (params.pageSize) queryParams.append('pageSize', params.pageSize)
  if (params.startTime) queryParams.append('startTime', params.startTime)

  const url = `${BASE_URL}/posts?${queryParams.toString()}`

  const res = await axios.get(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })

  //  直接返回原始 data
  return res.data // { posts: [...], total: xxx }
}

// 获取热门帖子
export const fetchHotPosts = async () => {
  const token = localStorage.getItem('token')
  const res = await axios.get(`${BASE_URL}/posts/hot`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  return res.data
}

// 创建新帖子
export function createPost(data) {
  const token = localStorage.getItem('token')
  return axios.post(
    `${BASE_URL}/posts`,
    data,
    {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}` // 自动加token
      }
    }
  )
}

// 获取帖子详情
export function getPostDetail(id) {
  const token = localStorage.getItem('token')
  return axios.get(`${BASE_URL}/posts/${id}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 点赞帖子
export function likePost(id) {
  const token = localStorage.getItem('token')
  return axios.post(`${BASE_URL}/posts/${id}/like`, null, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 删除帖子
export function deletePost(id, userId) {
  const token = localStorage.getItem('token')
  return axios.delete(`${BASE_URL}/posts/${id}`, {
    params: { userId },
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

// 获取用户加入的社团列表
export function getUserClubs(userId) {
  const token = localStorage.getItem('token')
  return axios.get(`${BASE_URL}/clubs/joined`, {
    params: { userId },
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

