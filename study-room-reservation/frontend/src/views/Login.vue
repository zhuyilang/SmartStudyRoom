<template>
  <div class="login-container">
    <!-- 背景轮播层 -->
    <div class="bg-slide bg-slide-1"></div>
    <div class="bg-slide bg-slide-2"></div>
    <div class="bg-slide bg-slide-3"></div>

    <!-- 背景装饰球 -->
    <div class="bg-blob bg-blob-1"></div>
    <div class="bg-blob bg-blob-2"></div>
    <div class="bg-blob bg-blob-3"></div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="system-title">校园自习室预约系统</div>
      <div class="card-header">
        <div class="logo-icon">📚</div>
        <h1 class="login-title">欢迎回来</h1>
        <p class="login-subtitle">登录您的账号以继续使用</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            class="custom-input"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            class="custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form.username, form.password)
    userStore.setUser(res.data)
    ElMessage.success('登录成功')
    if (res.data.role === 'ADMIN') {
      router.push('/admin/dashboard')
    } else {
      router.push('/student/roomList')
    }
  } catch (e) {
    ElMessage.error(e?.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 页面容器 ===== */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* ===== 背景轮播层 ===== */
.bg-slide {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0;
  animation: bgFade 24s infinite;
  z-index: 0;
}

.bg-slide-1 {
  background-image: url('@/assets/images/login-bg1.jpg');
  animation-delay: 0s;
}

.bg-slide-2 {
  background-image: url('@/assets/images/login-bg2.jpg');
  animation-delay: 8s;
}

.bg-slide-3 {
  background-image: url('@/assets/images/login-bg3.jpg');
  animation-delay: 16s;
}

@keyframes bgFade {
  0% {
    opacity: 0;
  }
  5% {
    opacity: 1;
  }
  33.33% {
    opacity: 1;
  }
  38.33% {
    opacity: 0;
  }
  100% {
    opacity: 0;
  }
}

/* ===== 背景装饰球 ===== */
.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.5;
  pointer-events: none;
  z-index: 1;
}
.bg-blob-1 {
  width: 500px;
  height: 500px;
  background: #818cf8;
  top: -200px;
  right: -100px;
}
.bg-blob-2 {
  width: 400px;
  height: 400px;
  background: #c084fc;
  bottom: -150px;
  left: -100px;
}
.bg-blob-3 {
  width: 300px;
  height: 300px;
  background: #f472b6;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.12;
  filter: blur(120px);
}

/* ===== 登录卡片 ===== */
.login-card {
  width: 420px;
  padding: 48px 40px 40px;
  background: rgba(255, 255, 255, 0.30);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
  position: relative;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
}

.login-card:hover {
  box-shadow: 0 30px 100px rgba(0, 0, 0, 0.15);
}

/* ===== 系统标题 ===== */
.system-title {
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: #4a5568;
  letter-spacing: 3px;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
}

/* ===== 卡片头部 ===== */
.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 48px;
  margin-bottom: 12px;
  display: inline-block;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
}

.login-subtitle {
  color: #718096;
  font-size: 15px;
  margin: 0;
}

/* ===== 表单 ===== */
.login-form {
  margin-top: 4px;
}

/* 自定义输入框样式 */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 16px;
  height: 48px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.25s ease;
  background-color: #f7fafc;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e0 inset;
  background-color: #ffffff;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #667eea inset !important;
  background-color: #ffffff;
}

.custom-input :deep(.el-input__inner) {
  height: 48px;
  font-size: 15px;
}

.custom-input :deep(.el-input__prefix) {
  color: #a0aec0;
  font-size: 18px;
  margin-right: 8px;
}

/* ===== 登录按钮 ===== */
.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 1px;
  color: #fff;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.40);
}

.login-btn:active {
  transform: translateY(0px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.30);
}

.login-btn.is-loading {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* ===== 卡片底部 ===== */
.card-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #718096;
  padding-top: 20px;
  border-top: 1px solid #edf2f7;
}

.card-footer a {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: all 0.2s;
}

.card-footer a:hover {
  color: #5a67d8;
  text-decoration: underline;
}

/* ===== 响应式 ===== */
@media (max-width: 480px) {
  .login-card {
    width: 92%;
    padding: 32px 20px;
    border-radius: 20px;
  }
  .login-title {
    font-size: 24px;
  }
  .login-btn {
    height: 46px;
    font-size: 15px;
  }
}
</style>