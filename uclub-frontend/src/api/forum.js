import request from '@/utils/request'

// 获取帖子详情
export function fetchPostDetail(id) {
  return request({
    url: `/posts/${id}`,
    method: 'get'
  })
}

// 点赞帖子
export function likePost(id) {
  return request({
    url: `/posts/${id}/like`,
    method: 'post'
  })
}
// 分页获取帖子列表
export function fetchPostList(params) {
  return request({
    url: '/posts',
    method: 'get',
    params
  })
}

// 发布帖子
export function createPost(data) {
  return request({
    url: '/posts',
    method: 'post',
    data
  })
}
