import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'enterprise',
        name: 'Enterprise',
        component: () => import('@/views/enterprise/EnterpriseList.vue'),
        meta: { title: '资质备案' }
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: () => import('@/views/contact/ContactList.vue'),
        meta: { title: '联系人管理' }
      },
      {
        path: 'ledger/category',
        name: 'LedgerCategory',
        component: () => import('@/views/ledger/LedgerCategory.vue'),
        meta: { title: '台账分类管理' }
      },
      {
        path: 'ledger/finance',
        name: 'FinanceLedger',
        component: () => import('@/views/ledger/FinanceLedger.vue'),
        meta: { title: '财务流水台账' }
      },
      {
        path: 'ledger/inventory',
        name: 'InventoryLedger',
        component: () => import('@/views/ledger/InventoryLedger.vue'),
        meta: { title: '库存明细台账' }
      },
      {
        path: 'ledger/customer',
        name: 'CustomerLedger',
        component: () => import('@/views/ledger/CustomerLedger.vue'),
        meta: { title: '客户信息台账' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/Report.vue'),
        meta: { title: '智能报表' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/system/UserList.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/system/RoleList.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'backup',
        name: 'Backup',
        component: () => import('@/views/system/BackupList.vue'),
        meta: { title: '数据备份与恢复' }
      },
      {
        path: 'alerts',
        name: 'Alerts',
        component: () => import('@/views/alert/AlertList.vue'),
        meta: { title: '异常预警' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 台账管理系统`
    : '小微企业数字化台账管理系统'
  // 未登录跳转登录页
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
