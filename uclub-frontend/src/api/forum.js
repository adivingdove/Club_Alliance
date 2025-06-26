import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api'  // 后端 Spring Boot 地址

export const fetchPostList = async () => {
  return axios.get(`${BASE_URL}/posts`)
}

export function createPost(data) {
  return axios.post('http://localhost:8080/api/posts', data, {
    headers: {
      'Content-Type': 'application/json'
    },
    withCredentials: false // 若服务端不需要 cookie
  })
}