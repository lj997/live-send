import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'files',
        name: 'Files',
        component: () => import('../views/Files.vue'),
        meta: { title: '文件管理', icon: 'Folder' }
      },
      {
        path: 'notes',
        name: 'Notes',
        component: () => import('../views/Notes.vue'),
        meta: { title: '备忘录', icon: 'Notebook' }
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: () => import('../views/Contacts.vue'),
        meta: { title: '联系人', icon: 'User' }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('../views/Tasks.vue'),
        meta: { title: '发送任务', icon: 'Clock' }
      },
      {
        path: 'checkin',
        name: 'CheckIn',
        component: () => import('../views/CheckIn.vue'),
        meta: { title: '签到', icon: 'CircleCheck' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '设置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - Live Send` : 'Live Send'
  next()
})

export default router
