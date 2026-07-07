import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  // 管理端
  {
    path: '/admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: { requireAuth: true, role: 'ADMIN' },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'campus',
        name: 'CampusManage',
        component: () => import('../views/admin/CampusManage.vue'),
        meta: { title: '校区管理' }
      },
      {
        path: 'building',
        name: 'BuildingManage',
        component: () => import('../views/admin/BuildingManage.vue'),
        meta: { title: '楼栋管理' }
      },
      {
        path: 'floor',
        name: 'FloorManage',
        component: () => import('../views/admin/FloorManage.vue'),
        meta: { title: '楼层管理' }
      },
      {
        path: 'room',
        name: 'RoomManage',
        component: () => import('../views/admin/RoomManage.vue'),
        meta: { title: '自习室管理' }
      },
      {
        path: 'room/:roomId/seats',
        name: 'SeatManage',
        component: () => import('../views/admin/SeatManage.vue'),
        meta: { title: '座位管理' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('../views/admin/Report.vue'),
        meta: { title: '数据报表' }
      }
    ]
  },
  // 学生端
  {
    path: '/student',
    component: () => import('../views/student/Layout.vue'),
    meta: { requireAuth: true, role: 'STUDENT' },
    children: [
      {
        path: '',
        redirect: '/student/rooms'
      },
      {
        path: 'rooms',
        name: 'RoomList',
        component: () => import('../views/student/RoomList.vue'),
        meta: { title: '自习室列表' }
      },
      {
        path: 'room/:roomId',
        name: 'SeatSelect',
        component: () => import('../views/student/SeatSelect.vue'),
        meta: { title: '选择座位' }
      },
      {
        path: 'my',
        name: 'MyReservation',
        component: () => import('../views/student/MyReservation.vue'),
        meta: { title: '我的预约' }
      }
    ]
  },
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态和角色
router.beforeEach((to, from, next) => {
  const user = JSON.parse(sessionStorage.getItem('loginUser') || 'null')

  if (to.meta.requireAuth && !user) {
    // 未登录，跳转登录页
    next('/login')
  } else if (to.meta.role && user && user.role !== to.meta.role) {
    // 角色不匹配，跳回对应首页
    next(user.role === 'ADMIN' ? '/admin' : '/student')
  } else if (to.path === '/login' && user) {
    // 已登录则跳回首页
    next(user.role === 'ADMIN' ? '/admin' : '/student')
  } else {
    next()
  }
})

export default router
