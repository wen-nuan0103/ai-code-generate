import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/LoginPage.vue'),
    },
    {
      path: '/',
      name: 'admin',
      component: () => import('@/layouts/BasicLayout.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/pages/Dashboard.vue'),
          meta: { title: '仪表盘' }
        },
        {
          path: 'user/list',
          name: 'users',
          component: () => import('@/pages/user/UserPage.vue'),
          meta: { title: '用户管理' }
        },
                {
          path: 'user/points',
          name: 'points',
          component: () => import('@/pages/user/PointPage.vue'),
          meta: { title: '积分记录' }
        },
        {
          path: 'ai/models',
          name: 'models',
          component: () => import('@/pages/ai/AiModelPage.vue'),
          meta: { title: 'AI 模型' }
        }
        ,
        {
          path: 'ai/keys',
          name: 'keys',
          component: () => import('@/pages/ai/AiKeyPage.vue'),
          meta: { title: '密钥池管理' }
        }
        ,
        {
          path: 'ai/prompts',
          name: 'prompts',
          component: () => import('@/pages/ai/AiPromptPage.vue'),
          meta: { title: 'Prompt 调优' }
        }
        ,
        {
          path: 'ai/logs',
          name: 'logs',
          component: () => import('@/pages/ai/AiLogPage.vue'),
          meta: { title: '生成日志' }
        }
      ]
    }
  ],
})

export default router
