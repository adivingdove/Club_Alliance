import { createRouter, createWebHistory } from 'vue-router'

// 原页面
import Home from '../views/Home.vue'

import Activities from '../views/Activities.vue'
import Profile from '../views/Profile.vue'
import Search from '../views/Search.vue'

// 新增页面
import ClubDetails from '../views/ClubDetails.vue'
import AiChat from '../views/AiChat.vue'
import ClubManage from '../views/ClubManage.vue'



// 新增论坛模块页面
import ForumPage from '../views/ForumPage.vue'
import PostCreate from '../views/PostCreate.vue'

// 帖子详情页（按需加载）
const PostDetail = () => import('@/views/PostDetail.vue')

// 后台入口 Layout
import AdminLayout from '../views/admin/AdminLayout.vue'

// 后台子页面
import ClubListView from '../views/admin/ClubListView.vue'
import ClubAdminView from '../views/admin/ClubAdminView.vue'
import UserQueryView from '../views/admin/UserQueryView.vue'
import SystemAnnouncementView from '../views/admin/SystemAnnouncementView.vue'
import ActivitiesAnnouncementView from '../views/admin/ActivitesAnnouncementView.vue'
import ClubAnnouncementReviewView from '../views/admin/ClubAnnouncementReviewView.vue'
import ClubCreationReviewView from '../views/admin/ClubCreationReviewView.vue'
import ActivityReviewView from '../views/admin/ActivityReviewView.vue'
import ReportHandlingView from '../views/admin/ReportHandlingView.vue'
import PostAdminView from '../views/admin/PostAdminView.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },

  // 论坛模块
  { path: '/forum', name: 'Forum', component: ForumPage },
  { path: '/post/create', name: 'PostCreate', component: PostCreate },
  { path: '/post/:id', name: 'PostDetail', component: PostDetail },

  { path: '/activities', name: 'Activities', component: Activities },
  
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/search', name: 'Search', component: Search },

  // 新增页面
  { path: '/club/:id', name: 'ClubDetails', component: ClubDetails },

  // AI问答页面
  { path: '/ai-chat', name: 'AiChat', component: AiChat },
  //社团管理页面
  { path: '/club-manage', name: 'ClubManage', component: ClubManage },
  // 后台管理入口（保留多子路由结构）
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', redirect: 'club-list' },
      { path: 'club-list', component: ClubListView },
      { path: 'club-admin', component: ClubAdminView },
      { path: 'user-query', component: UserQueryView },
      { path: 'system-announcement', component: SystemAnnouncementView },
      { path: 'activities-announcement', component: ActivitiesAnnouncementView },
      { path: 'club-announcement-review', component: ClubAnnouncementReviewView },
      { path: 'club-creation-review', component: ClubCreationReviewView },
      { path: 'activity-review', component: ActivityReviewView },
      { path: 'report-handling', component: ReportHandlingView },
      { path: 'post-admin', component: PostAdminView}
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

