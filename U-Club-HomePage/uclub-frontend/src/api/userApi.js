import request from '@/utils/request'

// 用户登录
export function login(data) {
    return request({
        url: '/api/user/login',
        method: 'post',
        data
    })
}

// 用户注册
export function register(data) {
    return request({
        url: '/api/user/register',
        method: 'post',
        data
    })
}

// 发送注册验证码
export function sendRegisterCode(data) {
    return request({
        url: '/api/user/sendRegisterCode',
        method: 'post',
        data
    })
}

// 发送重置密码验证码
export function sendResetCode(data) {
    return request({
        url: '/api/user/sendResetCode',
        method: 'post',
        data
    })
}

// 重置密码
export function resetPassword(data) {
    return request({
        url: '/api/user/resetPassword',
        method: 'post',
        data
    })
}

// 获取用户信息
export function getUserInfo() {
    return request({
        url: '/api/user/info',
        method: 'get'
    })
}

// 更新用户信息
export function updateUserInfo(data) {
    return request({
        url: '/api/user/update',
        method: 'put',
        data
    })
}

// 退出登录
export function logout() {
    return request({
        url: '/api/user/logout',
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