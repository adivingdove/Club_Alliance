import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import Forum from '../views/Forum.vue';
import Clubs from '../views/Clubs.vue';
import Activities from '../views/Activities.vue';
import Profile from '../views/Profile.vue';
import Search from '../views/Search.vue';
import Admin from '../views/Admin.vue';

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/forum', name: 'Forum', component: Forum },
  { path: '/clubs', name: 'Clubs', component: Clubs },
  { path: '/activities', name: 'Activities', component: Activities },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/search', name: 'Search', component: Search },
  { path: '/admin', name: 'Admin', component: Admin },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;