import request, { publicRequest } from '@/utils/request'

// 用户登录
export function login(data) {
    return request.post('/api/user/login', data)
}

// 用户注册
export function register(data) {
    return publicRequest.post('/api/user/register', data)
}

// 发送注册验证码
export function sendRegisterCode(data) {
    return publicRequest.post('/api/user/sendRegisterCode', data)
}

// 发送重置密码验证码
export function sendResetCode(data) {
    return publicRequest.post('/api/user/sendResetCode', data)
}

// 重置密码
export function resetPassword(data) {
    return publicRequest.post('/api/user/resetPassword', data)
}

// 获取用户信息
export function getUserInfo() {
    return request.get('/api/user/info')
}

// 更新用户信息
export function updateUserInfo(data) {
    return request.put('/api/user/update', data)
}

// 退出登录
export function logout() {
    return request.post('/api/user/logout')
}

// 数据库连接测试
export function testDatabase() {
    return request.get('/mysql-test')
}

// 社团成员管理相关API
export function getClubMembers(clubId) {
  return request.get(`/api/club/${clubId}/members`)
}
export function updateMemberRole(clubId, memberId, role) {
  return request.put(`/api/club/${clubId}/member/${memberId}/role`, { role })
}
export function removeClubMember(clubId, memberId) {
  return request.delete(`/api/club/${clubId}/member/${memberId}`)
}
export function getClubApplications(clubId) {
  return request.get(`/api/club/${clubId}/applications`)
}
export function approveApplication(clubId, applicationId) {
  return request.post(`/api/club/${clubId}/application/${applicationId}/approve`)
}
export function rejectApplication(clubId, applicationId) {
  return request.post(`/api/club/${clubId}/application/${applicationId}/reject`)
}