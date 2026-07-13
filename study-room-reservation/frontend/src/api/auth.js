import request from './request'

// 登录
export function login(username, password, role) {
  return request.post('/auth/login', { username, password })
}

// 注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 退出登录
export function logout() {
  return request.get('/auth/logout')
}

// 当前用户信息
export function getCurrentUser() {
  return request.get('/auth/currentUser')
}

// 当前用户信息
export function getProfile() {
  return request.get('/auth/profile')
}
