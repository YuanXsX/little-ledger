<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">台账管理系统</div>
      <el-menu :default-active="activeMenu" router background-color="#1f3a5f"
        text-color="#cdd7e4" active-text-color="#fff">
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <el-sub-menu index="base">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>企业基础信息</span>
          </template>
          <el-menu-item index="/enterprise">资质备案</el-menu-item>
          <el-menu-item index="/contacts">联系人管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="ledger">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>台账管理</span>
          </template>
          <el-menu-item index="/ledger/category">台账分类管理</el-menu-item>
          <el-menu-item index="/ledger/finance">财务流水台账</el-menu-item>
          <el-menu-item index="/ledger/inventory">库存明细台账</el-menu-item>
          <el-menu-item index="/ledger/customer">客户信息台账</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/report">
          <el-icon><DataAnalysis /></el-icon>
          <span>智能报表</span>
        </el-menu-item>

        <el-sub-menu v-if="can('user:view') || can('role:view') || can('backup:view')" index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item v-if="can('user:view')" index="/users">用户管理</el-menu-item>
          <el-menu-item v-if="can('role:view')" index="/roles">角色管理</el-menu-item>
          <el-menu-item v-if="can('backup:view')" index="/backup">数据备份与恢复</el-menu-item>
        </el-sub-menu>

        <el-menu-item v-if="can('alert:view')" index="/alerts">
          <el-icon><Bell /></el-icon>
          <span>异常预警</span>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><ChatDotRound /></el-icon>
          <span>财税 AI 助手</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span>小微企业数字化台账管理系统</span>
        <el-dropdown>
          <span class="user">{{ username }} <el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>{{ roleNames }}</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)

const user = JSON.parse(localStorage.getItem('user') || '{}')
const username = computed(() => user.realName || user.username || '管理员')
const roleNames = computed(() => (user.roles || []).join('、'))
const permissions = user.permissions || []

function can(perm) {
  return permissions.includes(perm)
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background-color: #1f3a5f;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  background: #fff;
}
.user {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.main {
  background: #f5f7fa;
}
</style>
