import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true  // 携带 Cookie，保证 Session 可用
})

// 标记后端是否可达，避免每个请求都弹一次"网络异常"
let backendDown = false
let downTimer = null

function showBackendDown() {
  if (backendDown) return
  backendDown = true
  ElMessage({
    message: '后端未启动或无法连接，请检查后端服务（默认 http://localhost:8081）',
    type: 'error',
    duration: 0,           // 不自动关闭
    showClose: true
  })
  if (downTimer) clearTimeout(downTimer)
  // 30s 后允许再次提示
  downTimer = setTimeout(() => { backendDown = false }, 30000)
}

// 请求拦截器：携带 token
request.interceptors.request.use((config) => {
  // 预留：将来若有 token 鉴权
  return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => {
    const data = response.data
    // 后端返回 Result 格式
    if (data && typeof data === 'object' && data.code && data.code !== 200) {
      if (data.code === 401) {
        ElMessage.error('请先登录')
        sessionStorage.removeItem('loginUser')
        window.location.href = '/login'
      } else {
        ElMessage.error(data.msg || '请求失败')
      }
      return Promise.reject(new Error(data.msg || '请求失败'))
    }
    return data
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('请先登录')
        sessionStorage.removeItem('loginUser')
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('没有权限')
      } else if (status === 404) {
        ElMessage.error('接口不存在：' + error.config?.url)
      } else if (status >= 500) {
        showBackendDown()
      } else {
        ElMessage.error('请求失败：' + status)
      }
    } else {
      // 网络断开 / 后端未启动 / 超时
      showBackendDown()
    }
    return Promise.reject(error)
  }
)

export default request
