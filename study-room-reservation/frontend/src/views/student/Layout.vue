<template>
  <el-container class="student-layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="20"><School /></el-icon>
        <span>自习室预约系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#c9d1d9"
        active-text-color="#fff"
        class="sidebar-menu"
      >
        <el-menu-item index="/student/roomList">
          <el-icon><HomeFilled /></el-icon>
          <span>自习室列表</span>
        </el-menu-item>
        <el-menu-item index="/student/myRes">
          <el-icon><Calendar /></el-icon>
          <span>我的预约</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/student/roomList' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title || '自习室预约' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="28" style="background: #409eff">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ userInfo.realName || userInfo.username || '学生' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>角色：学生</el-dropdown-item>
                <el-dropdown-item divided @click.stop="handleLogout">退出登录</el-dropdown-item>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser, logout } from '../../api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userInfo = ref({})

const activeMenu = computed(() => route.path)
const avatarText = computed(() => {
  const n = userInfo.value.realName || userInfo.value.username || 'S'
  return n.charAt(0).toUpperCase()
})

// 获取当前登录用户
const getUser = async () => {
  const res = await getCurrentUser()
  if (res.code === 200) {
    userInfo.value = res.data
  } else {
    router.push('/login')
  }
}

// 退出登录
async function handleLogout() {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  await logout()
  userStore.clearUser()
  router.push('/login')
}

onMounted(getUser)
</script>

<style scoped>
.student-layout {
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