import request from './request'

export function login(username, password, role) {
  return request.post('/auth/login', { username, password, role })
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function logout() {
  return request.get('/auth/logout')
}

export function getCurrentUser() {
  return request.get('/auth/currentUser')
}
