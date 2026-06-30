<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <el-menu :default-active="activeMenu" router background-color="#304156"
             text-color="#bfcbd9" active-text-color="#409eff" class="sidebar">
      <div class="logo">
        <span>🏫 自习室管理后台</span>
      </div>
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
      <el-menu-item index="/admin/report">
        <el-icon><TrendCharts /></el-icon>
        <span>数据报表</span>
      </el-menu-item>
    </el-menu>

    <!-- 右侧内容 -->
    <div class="main-area">
      <div class="header">
        <span class="header-title">{{ $route.meta.title || '管理后台' }}</span>
        <div class="header-right">
          <span class="user-name">👤 {{ userStore.user?.realName || '管理员' }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
      <div class="content">
        <router-view />
      </div>
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
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning'
  })
  await logout()
  userStore.clearUser()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-name {
  color: #606266;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>
