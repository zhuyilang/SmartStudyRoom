<template>
  <el-container style="height: 100vh;">
    <el-header style="display: flex; justify-content: space-between; align-items: center; background:#409EFF; color:#fff; padding:0 30px;">
      <div style="display: flex;align-items:center;gap:12px;">
        <el-icon size="24"><School /></el-icon>
        <span style="font-size:20px; font-weight:bold; color:#fff;">智能校园自习室预约</span>
        <el-button type="primary" plain size="small" @click="$router.push('/student/roomList')" style="color:#fff;border-color:#fff;background:transparent;margin-left:24px;">自习室列表</el-button>
        <el-button type="primary" plain size="small" @click="$router.push('/student/myRes')" style="color:#fff;border-color:#fff;background:transparent;">我的预约</el-button>
      </div>
      <div style="display:flex;align-items:center;gap:12px; color:#fff;">
        <span style="font-size:14px;">{{ userInfo.username ?? '加载中' }}</span>
        <el-button text type="info" style="color:#fff;" @click="logoutHandle">退出登录</el-button>
      </div>
    </el-header>
    <el-main style="background:#f5f7fa;padding:20px;">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, logout } from '../../api/auth'

import { ElMessage } from 'element-plus'

const router = useRouter()
const userInfo = ref({})

// 获取当前登录用户
const getUser = async () => {
  const res = await getCurrentUser()
  if (res.code === 200) {
    userInfo.value = res.data
  } else {
    router.push('/login')
  }
}
onMounted(getUser)

// 退出登录
const logoutHandle = async () => {
  await logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>