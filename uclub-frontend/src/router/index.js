import { createRouter, createWebHistory } from 'vue-router'

// 原页面
import Home from '../views/Home.vue'
import Clubs from '../views/Clubs.vue'
import Activities from '../views/Activities.vue'
import Profile from '../views/Profile.vue'
import Search from '../views/Search.vue'

import Admin from '../views/Admin.vue'

// 新增论坛模块页面
import ForumPage from '../views/ForumPage.vue'
import PostCreate from '../views/PostCreate.vue'


// 后台入口 Layout
import AdminLayout from '../views/admin/AdminLayout.vue'

// 后台子页面
import ClubListView from '../views/admin/ClubListView.vue'
import ClubAdminView from '../views/admin/ClubAdminView.vue'
import UserQueryView from '../views/admin/UserQueryView.vue'
import SystemAnnouncementView from '../views/admin/SystemAnnouncementView.vue'
import ClubAnnouncementReviewView from '../views/admin/ClubAnnouncementReviewView.vue'
import ClubCreationReviewView from '../views/admin/ClubCreationReviewView.vue'
import ActivityReviewView from '../views/admin/ActivityReviewView.vue'
import ReportHandlingView from '../views/admin/ReportHandlingView.vue'




const routes = [
  { path: '/', name: 'Home', component: Home },

  // 论坛模块
  { path: '/forum', name: 'Forum', component: ForumPage },
  { path: '/post/create', name: 'PostCreate', component: PostCreate },

  { path: '/clubs', name: 'Clubs', component: Clubs },
  { path: '/activities', name: 'Activities', component: Activities },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/search', name: 'Search', component: Search },

  // 后台管理入口
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', redirect: 'club-list' },
      { path: 'club-list', component: ClubListView },
      { path: 'club-admin', component: ClubAdminView },
      { path: 'user-query', component: UserQueryView },
      { path: 'system-announcement', component: SystemAnnouncementView },
      { path: 'club-announcement-review', component: ClubAnnouncementReviewView },
      { path: 'club-creation-review', component: ClubCreationReviewView },
      { path: 'activity-review', component: ActivityReviewView },
      { path: 'report-handling', component: ReportHandlingView }
    ]
  }
]


const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
