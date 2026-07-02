import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true  // 携带 Cookie，保证 Session 可用
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => {
    const data = response.data
    // 后端返回 Result 格式
    if (data.code && data.code !== 200) {
      ElMessage.error(data.msg || '请求失败')
      return Promise.reject(new Error(data.msg))
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
      } else {
        ElMessage.error('服务器异常：' + status)
      }
    } else {
      ElMessage.error('网络异常，请检查连接')
    }
    return Promise.reject(error)
  }
)

export default request
