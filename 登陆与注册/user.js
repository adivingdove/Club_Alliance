import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 发送注册验证码
export function sendRegisterCode(data) {
  return request({
    url: '/user/sendRegisterCode',
    method: 'post',
    data
  })
}

// 发送重置密码验证码
export function sendResetCode(data) {
  return request({
    url: '/user/sendResetCode',
    method: 'post',
    data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/user/resetPassword',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 数据库连接测试
export function testDatabase() {
  return request({
    url: '/mysql-test',
    method: 'get'
  })
}

// 获取用户社团列表
export function getUserClubs() {
  return request({
    url: '/user/clubs',
    method: 'get'
  })
}

// 获取用户收藏列表
export function getUserFavorites() {
  return request({
    url: '/user/favorites',
    method: 'get'
  })
}

// 删除用户收藏
export function removeUserFavorite(id) {
  return request({
    url: `/user/favorites/${id}`,
    method: 'delete'
  })
}

// 获取用户贴子列表
export function getUserPosts() {
  return request({
    url: '/user/posts',
    method: 'get'
  })
}

// 删除用户贴子
export function deleteUserPost(id) {
  return request({
    url: `/user/posts/${id}`,
    method: 'delete'
  })
}

// 获取用户浏览历史
export function getUserHistory() {
  return request({
    url: '/user/history',
    method: 'get'
  })
}

// 清空用户浏览历史
export function clearUserHistory() {
  return request({
    url: '/user/history/clear',
    method: 'delete'
  })
}

// 上传用户头像
export function uploadUserAvatar(data) {
  return request({
    url: '/user/uploadAvatar',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 