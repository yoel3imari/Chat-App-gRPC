import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/user/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import ChatView from '@/views/user/ChatView.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import UsersView from '@/views/admin/UsersView.vue'
import ReportsView from '@/views/admin/ReportsView.vue'
import SettingsView from '@/views/admin/SettingsView.vue'
import MainLayout from '@/layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      name: 'main',
      path: "/chat",
      component: MainLayout,
      children: [
        {
          path: '/chat/:id',
          name: 'chat',
          component: ChatView,
        },
        {
          path: '/',
          name: 'home',
          component: HomeView,
        },
      ],
    },
    {
      path: '/admin/dashboard',
      name: 'dashboard',
      component: AdminLayout,
      children: [
        { name: 'users', path: 'users', component: UsersView },
        { name: 'reports', path: 'reports', component: ReportsView },
        { name: 'settings', path: 'settings', component: SettingsView },
      ],
    },
  ],
})

export default router
