import { createRouter, createWebHistory } from 'vue-router'
import UnionPage from '../components/UnionPage.vue'
import ClubDetails from '../components/ClubDetails.vue'
// import MyClubs from '../components/MyClubs.vue' // 如果MyClubs.vue已删除，也请注释或删除

const routes = [
  {
    path: '/',
    name: 'home',
    component: UnionPage
  },
  {
    path: '/club/:id',
    name: 'club-detail',
    component: ClubDetails
  }
  // 如有其他实际存在的页面，可在此添加
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 