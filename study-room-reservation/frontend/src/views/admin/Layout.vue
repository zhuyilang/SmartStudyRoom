<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="20"><School /></el-icon>
        <span>自习室管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#c9d1d9"
        active-text-color="#fff"
        class="sidebar-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/campus">
          <el-icon><School /></el-icon>
          <span>校区管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/building">
          <el-icon><OfficeBuilding /></el-icon>
          <span>楼栋管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/floor">
          <el-icon><Grid /></el-icon>
          <span>楼层管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/room">
          <el-icon><HomeFilled /></el-icon>
          <span>自习室管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/admin-manage">
          <el-icon><User /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/report">

          <el-icon><TrendCharts /></el-icon>
          <span>数据报表</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title || '管理后台' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip :content="`数据来源：后端接口（${autoRefresh ? '每 5s 自动刷新' : '已暂停刷新'}）`" placement="bottom">
            <el-tag :type="autoRefresh ? 'success' : 'info'" effect="plain" size="small">
              <el-icon style="vertical-align: middle" v-if="autoRefresh"><Loading /></el-icon>
              {{ autoRefresh ? '实时' : '已暂停' }}
            </el-tag>
          </el-tooltip>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="28" style="background: #409eff">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ userStore.user?.realName || userStore.user?.username || '管理员' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>角色：{{ userStore.user?.role === ROLE.ADMIN ? '管理员' : '学生' }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, provide, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { logout } from '../../api/auth'
import { useUserStore } from '../../stores/user'
import { ROLE } from '../../constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const avatarText = computed(() => {
  const n = userStore.user?.realName || userStore.user?.username || 'A'
  return n.charAt(0).toUpperCase()
})

// 全局 autoRefresh 状态：供 Dashboard 等页面读取
const autoRefresh = ref(true)
provide('autoRefresh', autoRefresh)
provide('toggleAutoRefresh', () => {
  autoRefresh.value = !autoRefresh.value
  ElMessage.info(autoRefresh.value ? '已开启实时刷新' : '已暂停实时刷新')
})

async function handleLogout() {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  await logout()
  userStore.clearUser()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.sidebar {
  background: #001529;
  overflow: hidden;
  transition: width 0.2s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: #002140;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.sidebar-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 6px;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #1890ff !important;
  color: #fff !important;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.05) !important;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #303133;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-name {
  font-size: 14px;
}

.content {
  padding: 16px;
  background: #f0f2f5;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
