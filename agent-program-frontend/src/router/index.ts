import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/code-master',
      name: 'code-master',
      component: () => import('@/views/CodeMasterView.vue')
    },
    {
      path: '/super-agent',
      name: 'super-agent',
      component: () => import('@/views/SuperAgentView.vue')
    }
  ]
})

export default router
