<template>
  <div class="shell">
    <!-- 侧边导航：可折叠，深蓝渐变，分组展示业务模块 -->
    <aside class="sidebar" :class="{ 'is-collapsed': collapsed }">
      <div class="brand">
        <div class="brand-mark">账</div>
        <div v-show="!collapsed" class="brand-text">
          <div class="brand-name">台账管理</div>
          <div class="brand-sub">SME LEDGER</div>
        </div>
      </div>

      <el-menu :default-active="route.path" router :collapse="collapsed" :collapse-transition="false"
        unique-opened class="side-menu" background-color="transparent" text-color="#c3d0e2"
        active-text-color="#ffffff">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>

        <el-sub-menu index="base">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>企业基础信息</span>
          </template>
          <el-menu-item index="/enterprise">
            <el-icon><Medal /></el-icon>
            <template #title>资质备案</template>
          </el-menu-item>
          <el-menu-item index="/contacts">
            <el-icon><User /></el-icon>
            <template #title>联系人管理</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="ledger">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>台账管理</span>
          </template>
          <el-menu-item index="/ledger/category">
            <el-icon><Files /></el-icon>
            <template #title>台账分类</template>
          </el-menu-item>
          <el-menu-item index="/ledger/finance">
            <el-icon><Coin /></el-icon>
            <template #title>财务流水</template>
          </el-menu-item>
          <el-menu-item index="/ledger/inventory">
            <el-icon><Box /></el-icon>
            <template #title>库存明细</template>
          </el-menu-item>
          <el-menu-item index="/ledger/customer">
            <el-icon><Avatar /></el-icon>
            <template #title>客户信息</template>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/report">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>智能报表</template>
        </el-menu-item>

        <el-menu-item v-if="can('alert:view')" index="/alerts">
          <el-icon><Bell /></el-icon>
          <template #title>异常预警</template>
        </el-menu-item>

        <el-menu-item v-if="can('assistant:ask')" index="/ai">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>财税 AI 助手</template>
        </el-menu-item>

        <el-sub-menu v-if="can('user:view') || can('role:view') || can('backup:view')" index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item v-if="can('user:view')" index="/users">
            <el-icon><UserFilled /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item v-if="can('role:view')" index="/roles">
            <el-icon><Key /></el-icon>
            <template #title>角色权限</template>
          </el-menu-item>
          <el-menu-item v-if="can('backup:view')" index="/backup">
            <el-icon><FolderOpened /></el-icon>
            <template #title>备份恢复</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div v-show="!collapsed" class="sidebar-foot">
        <span class="dot" />
        {{ envLabel }} · v0.1.0
      </div>
    </aside>

    <!-- 右侧主区域 -->
    <div class="main">
      <header class="topbar">
        <div class="topbar-left">
          <button class="icon-btn" :title="collapsed ? '展开菜单' : '收起菜单'" @click="collapsed = !collapsed">
            <el-icon :size="18">
              <Expand v-if="collapsed" />
              <Fold v-else />
            </el-icon>
          </button>

          <el-breadcrumb separator="/" class="crumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="groupName">{{ groupName }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="topbar-right">
          <el-tooltip content="异常预警" placement="bottom">
            <button class="icon-btn" @click="goAlerts">
              <el-badge :value="alertCount" :hidden="!alertCount" :max="99">
                <el-icon :size="18"><Bell /></el-icon>
              </el-badge>
            </button>
          </el-tooltip>

          <el-dropdown trigger="click">
            <div class="user-chip">
              <div class="avatar">{{ initials }}</div>
              <div class="user-meta">
                <div class="user-name">{{ username }}</div>
                <div class="user-role">{{ roleNames || '—' }}</div>
              </div>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ username }}</el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
/**
 * 应用主框架：侧边导航 + 顶栏（面包屑 / 预警角标 / 用户菜单）。
 * 权限点来自登录时写入 localStorage 的用户信息，菜单按权限动态显隐。
 */
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listAlerts } from '@/api/alert'

const route = useRoute()
const router = useRouter()

const collapsed = ref(false)
const alertCount = ref(0)
let timer = null

const user = JSON.parse(localStorage.getItem('user') || '{}')
const permissions = user.permissions || []

const username = computed(() => user.realName || user.username || '管理员')
const roleNames = computed(() => (user.roles || []).join('、'))
const initials = computed(() => (username.value || '账').slice(0, 1))
const envLabel = import.meta.env.DEV ? '开发环境' : '生产环境'

/* 面包屑：优先取路由 meta，缺失时回退到菜单映射 */
const pageTitle = computed(() => route.meta.title || '工作台')
const groupName = computed(() => route.meta.group || '')

function can(perm) {
  return permissions.includes(perm)
}

/* 未处理预警数量：用于顶栏红点，每分钟刷新一次 */
async function loadAlertCount() {
  if (!can('alert:view')) return
  try {
    const res = await listAlerts({ status: 'UNRESOLVED' })
    alertCount.value = (res.data || []).length
  } catch {
    alertCount.value = 0
  }
}

function goAlerts() {
  if (can('alert:view')) {
    router.push('/alerts')
  }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

/* 窄屏自动收起侧栏，避免内容区被压缩 */
function handleResize() {
  if (window.innerWidth < 1200) collapsed.value = true
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
  loadAlertCount()
  timer = setInterval(loadAlertCount, 60000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.shell {
  display: flex;
  height: 100%;
  background: var(--app-bg);
}

/* ---------------- 侧栏 ---------------- */
.sidebar {
  width: 238px;
  flex: 0 0 238px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, var(--app-nav-from) 0%, var(--app-nav-to) 100%);
  transition: width 0.2s ease, flex-basis 0.2s ease;
  overflow: hidden;
}

.sidebar.is-collapsed {
  width: 68px;
  flex-basis: 68px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
  white-space: nowrap;
}

.brand-mark {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  color: #0d1f38;
  background: linear-gradient(135deg, #7dd3fc, #38bdf8);
  box-shadow: 0 6px 16px rgba(56, 189, 248, 0.35);
}

.brand-name {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 1px;
}

.brand-sub {
  font-size: 10px;
  letter-spacing: 2px;
  color: rgba(195, 208, 226, 0.6);
}

.side-menu {
  flex: 1;
  border-right: none;
  padding: 10px 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  border-radius: 9px;
  font-size: 13.5px;
}

.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff;
}

/* 选中项：浅色高亮 + 左侧亮条 */
.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(56, 189, 248, 0.22), rgba(56, 189, 248, 0.06)) !important;
  font-weight: 600;
}

.side-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 3px;
  border-radius: 3px;
  background: #38bdf8;
}

.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  position: relative;
}

.side-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.side-menu :deep(.el-sub-menu .el-menu-item) {
  height: 40px;
  line-height: 40px;
  font-size: 13px;
}

.sidebar-foot {
  padding: 12px 18px;
  font-size: 11px;
  color: rgba(195, 208, 226, 0.55);
  border-top: 1px solid rgba(255, 255, 255, 0.07);
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.18);
}

/* ---------------- 主区域 ---------------- */
.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 64px;
  flex: 0 0 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--app-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: #4b5563;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.icon-btn:hover {
  background: #eef2f9;
  color: var(--app-primary);
}

.crumb {
  font-size: 13px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 10px 5px 6px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.user-chip:hover {
  background: #eef2f9;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #0ea5a4);
}

.user-meta {
  line-height: 1.2;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text);
}

.user-role {
  font-size: 11px;
  color: var(--app-text-mute);
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chev {
  color: #9aa4b2;
}

.content {
  flex: 1;
  overflow-y: auto;
}

/* 侧栏收起时隐藏文字、居中图标 */
.sidebar.is-collapsed .brand {
  justify-content: center;
  padding: 0;
}

.sidebar.is-collapsed .side-menu {
  padding: 10px 6px;
}

@media (max-width: 768px) {
  .crumb {
    display: none;
  }

  .user-meta {
    display: none;
  }
}
</style>
