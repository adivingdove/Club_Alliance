import request from '../utils/request'

const BASE_URL = '/api'  // 修改为相对路径

// 获取帖子列表（支持分页和筛选）
export const fetchPostList = async (params = {}) => {
  const queryParams = new URLSearchParams()
  if (params.title) queryParams.append('title', params.title)
  if (params.clubName) queryParams.append('clubName', params.clubName)
  if (params.page) queryParams.append('page', params.page)
  if (params.pageSize) queryParams.append('pageSize', params.pageSize)
  if (params.startTime) queryParams.append('startTime', params.startTime)

  const url = `${BASE_URL}/posts?${queryParams.toString()}`
  const res = await request.get(url)
  return res.data
}

// 获取热门帖子
export const fetchHotPosts = async () => {
  const res = await request.get(`${BASE_URL}/posts/hot`)
  return res.data
}

// 创建新帖子
export function createPost(data) {
  return request.post(`${BASE_URL}/posts`, data)
}

// 获取帖子详情
export function getPostDetail(id) {
  return request.get(`${BASE_URL}/posts/${id}`)
}

// 点赞帖子
export function likePost(id) {
  return request.post(`${BASE_URL}/posts/${id}/like`)
}

// 删除帖子
export function deletePost(id, userId) {
  return request.delete(`${BASE_URL}/posts/${id}`, {
    params: { userId }
  })
}

// 获取用户加入的社团列表
export function getUserClubs(userId) {
  return request.get(`${BASE_URL}/clubs/joined`, {
    params: { userId }
  })
}

