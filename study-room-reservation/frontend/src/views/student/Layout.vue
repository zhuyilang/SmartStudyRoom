<template>
  <div class="student-layout">
    <!-- 顶部导航 -->
    <div class="top-nav">
      <span class="nav-title">🏫 校园自习室预约</span>
      <el-menu :default-active="activeMenu" mode="horizontal" router
               background-color="#409eff" text-color="#fff" active-text-color="#fff"
               class="nav-menu">
        <el-menu-item index="/student/rooms">自习室列表</el-menu-item>
        <el-menu-item index="/student/my">我的预约</el-menu-item>
      </el-menu>
      <div class="nav-right">
        <span class="user-name">👤 {{ userStore.user?.realName || '同学' }}</span>
        <el-button text style="color: #fff" @click="handleLogout">退出</el-button>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { logout } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

async function handleLogout() {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  await logout()
  userStore.clearUser()
  router.push('/login')
}
</script>

<style scoped>
.student-layout {
  min-height: 100vh;
  background: #f0f2f5;
}

.top-nav {
  display: flex;
  align-items: center;
  background: #409eff;
  padding: 0 24px;
  height: 60px;
}

.nav-title {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  margin-right: 32px;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;
}

.nav-menu .el-menu-item {
  border-bottom: none !important;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  white-space: nowrap;
}

.content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}
</style>
