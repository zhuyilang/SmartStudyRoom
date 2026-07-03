import request from './request'

export function getSeatStatus() {
  return request.get('/dashboard/seatStatus')
}

export function getRoomDetail(roomId) {
  return request.get(`/dashboard/room/${roomId}`)
}

export function getOverallStats() {
  return request.get('/dashboard/overall')
}

export function getTodayStats() {
  return request.get('/dashboard/today')
}