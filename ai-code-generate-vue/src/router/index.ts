import { createRouter, createWebHistory } from 'vue-router'
import ACCESS_ENUM from '@/access/accessEnum'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: () => import('@/pages/HomePage.vue'),
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('@/pages/user/UserLoginPage.vue'),
      meta: {
        hideHeader: true,
        hideFooter: true,
      },
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('@/pages/user/UserRegisterPage.vue'),
      meta: {
        hideHeader: true,
        hideFooter: true,
      },
    },
    {
      path: '/user/profile',
      name: '个人信息',
      component: () => import('@/pages/user/UserProfilePage.vue'),
      meta: {
        hideFooter: true,
      },
    },
    {
      path: '/user/edit',
      name: '修改个人信息',
      component: () => import('@/pages/user/UserEditPage.vue'),
      meta: {
        hideFooter: true,
      },
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: () => import('@/pages/app/AppChatPage.vue'),
      meta: {
        hideLayout: true,
      },
    },
    {
      path: '/app/edit/:id',
      name: '编辑应用',
      component: () => import('@/pages/app/AppEditPage.vue'),
      meta: {
        hideFooter: true,
      },
    },
    {
      path: '/admin/userManage',
      name: 'user_manage',
      component: () => import('@/pages/admin/UserManagePage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    return { top: 0 }
  },
})

export default router
