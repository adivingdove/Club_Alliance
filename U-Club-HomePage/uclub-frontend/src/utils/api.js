import { http } from './request'

const BASE_URL = 'http://localhost:3000/api'

export const clubApi = {
  // 获取所有俱乐部
  getAllClubs() {
    return http.get(`${BASE_URL}/clubs`)
  },

  // 获取俱乐部成员统计
  getClubStats() {
    return http.get(`${BASE_URL}/clubs/stats`)
  },

  // 获取活动参与度
  getClubActivities() {
    return http.get(`${BASE_URL}/clubs/activities`)
  },

  // 获取俱乐部类型分布
  getClubTypes() {
    return http.get(`${BASE_URL}/clubs/types`)
  }
} 