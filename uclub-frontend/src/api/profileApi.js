import request from '../utils/request'

// 获取用户详细信息
export const getProfileInfo = () => {
  return request.get('/api/profile/info', {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 更新用户信息
export const updateProfile = (data) => {
  return request.put('/api/profile/update', data, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 修改密码
export const changePassword = (data) => {
  return request.put('/api/profile/change-password', data, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 获取我的社团
export const getMyClubs = () => {
  return request.get('/api/profile/my-clubs', {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 获取我的活动
export const getMyActivities = () => {
  return request.get('/api/profile/my-activities', {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 获取最近活动
export const getRecentActivities = () => {
  return request.get('/api/profile/recent-activities', {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 获取我的帖子
export const getMyPosts = () => {
  return request.get('/api/profile/my-posts', {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

// 上传头像
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/api/profile/upload/avatar', formData, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`,
      'Content-Type': 'multipart/form-data'
    }
  })
} 