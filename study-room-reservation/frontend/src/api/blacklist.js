import request from './request'

export function getBlacklist(params) {
  return request.get('/admin/blacklist/list', { params })
}

export function removeBan(userId) {
  return request.post(`/admin/blacklist/remove/${userId}`)
}

export function addToBlacklist(data) {
  return request.post('/admin/blacklist/add', data)
}