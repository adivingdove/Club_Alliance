import request from '@/utils/request'

// 测试API连接
export function testActivityApi() {
    return request({
        url: '/api/activities/test',
        method: 'get'
    })
}

// 测试简单API连接
export function testSimpleActivityApi() {
    return request({
        url: '/api/activities/test-simple',
        method: 'get'
    })
}

// 测试基础API连接
export function testBasicActivityApi() {
    return request({
        url: '/api/activities/test-basic',
        method: 'get'
    })
}

// 测试数据库连接
export function testDatabaseConnection() {
    return request({
        url: '/api/activities/test-db',
        method: 'get'
    })
}

// 测试CORS配置
export function testCorsConfig() {
    return request({
        url: '/api/activities/cors-test',
        method: 'get'
    })
}

// 获取所有活动
export function getAllActivities() {
    return request({
        url: '/api/activities',
        method: 'get'
    })
}

// 根据社团ID获取活动
export function getActivitiesByClubId(clubId) {
    return request({
        url: `/api/activities/club/${clubId}`,
        method: 'get'
    })
}

// 根据社团ID获取已通过审核的活动
export function getApprovedActivitiesByClubId(clubId) {
    return request({
        url: `/api/activities/club/${clubId}/approved`,
        method: 'get'
    })
}

// 根据创建者ID获取活动
export function getActivitiesByCreatorId(creatorId) {
    return request.get(`/api/activities/creator/${creatorId}`)
}

// 获取即将到来的活动
export function getUpcomingActivities() {
    return request({
        url: '/api/activities/upcoming',
        method: 'get'
    })
}

// 获取待审核的活动
export function getPendingActivities() {
    return request({
        url: '/api/activities/pending',
        method: 'get'
    })
}

// 根据状态获取活动
export function getActivitiesByStatus(status) {
    return request({
        url: `/api/activities/status/${status}`,
        method: 'get'
    })
}

// 搜索活动
export function searchActivities(keyword) {
    return request({
        url: '/api/activities/search',
        method: 'get',
        params: { keyword }
    })
}

// 根据日期范围获取活动
export function getActivitiesByDateRange(startDate, endDate) {
    return request({
        url: '/api/activities/date-range',
        method: 'get',
        params: { startDate, endDate }
    })
}

// 根据ID获取活动详情
export function getActivityById(id) {
    return request({
        url: `/api/activities/${id}`,
        method: 'get'
    })
}

// 创建活动
export function createActivity(data) {
    return request({
        url: '/api/activities',
        method: 'post',
        data
    })
}

// 更新活动
export function updateActivity(id, data) {
    return request({
        url: `/api/activities/${id}`,
        method: 'put',
        data
    })
}

// 删除活动
export function deleteActivity(id) {
    return request({
        url: `/api/activities/${id}`,
        method: 'delete'
    })
}

// 更新活动审核状态
export function updateActivityApplyStatus(id, applyStatus) {
    return request({
        url: `/api/activities/${id}/apply-status`,
        method: 'put',
        params: { applyStatus }
    })
}

// 用户申请加入活动
export function joinActivity(activityId, userId) {
    return request({
        url: `/api/activities/${activityId}/join`,
        method: 'post',
        params: { userId }
    })
}

// 用户退出活动
export function leaveActivity(activityId, userId) {
    return request({
        url: `/api/activities/${activityId}/leave`,
        method: 'post',
        params: { userId }
    })
}

// 获取活动的参与者列表
export function getActivityParticipants(activityId) {
    return request({
        url: `/api/activities/${activityId}/participants`,
        method: 'get'
    })
}

// 检查用户是否参与某个活动
export function isUserParticipating(activityId, userId) {
    return request({
        url: `/api/activities/${activityId}/participating`,
        method: 'get',
        params: { userId }
    })
}

// 获取活动的当前参与人数
export function getActivityParticipantCount(activityId) {
    return request({
        url: `/api/activities/${activityId}/participant-count`,
        method: 'get'
    })
}

// 获取我参与的活动
export function getActivitiesByParticipantId(userId) {
    return request.get(`/api/activities/participant/${userId}`)
} 