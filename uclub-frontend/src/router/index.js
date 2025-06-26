
import { createRouter, createWebHistory } from 'vue-router'

// 原有页面
import Home from '../views/Home.vue'
import Clubs from '../views/Clubs.vue'
import Activities from '../views/Activities.vue'
import Profile from '../views/Profile.vue'
import Search from '../views/Search.vue'
import Admin from '../views/Admin.vue'

// 新增论坛模块页面
import ForumPage from '../views/ForumPage.vue'
import PostCreate from '../views/PostCreate.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },

  // 论坛模块
  { path: '/forum', name: 'Forum', component: ForumPage },
  { path: '/post/create', name: 'PostCreate', component: PostCreate },


  { path: '/clubs', name: 'Clubs', component: Clubs },
  { path: '/activities', name: 'Activities', component: Activities },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/search', name: 'Search', component: Search },
  { path: '/admin', name: 'Admin', component: Admin },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
