import request from './request'

export function getRoomList(params) {
  return request.get('/student/room/list', { params })
}

export function getRoomDetail(roomId) {
  return request.get(`/student/room/${roomId}`)
}

export function getSeatGrid(roomId, date) {
  return request.get('/student/seat/grid', { params: { roomId, date } })
}

export function reserveSeat(data) {
  return request.post('/student/reservation/reserve', data)
}

export function cancelReservation(reservationId) {
  return request.post(`/student/reservation/cancel/${reservationId}`)
}

export function signIn(reservationId) {
  return request.post(`/student/reservation/signIn/${reservationId}`)
}

export function getMyReservations(params) {
  return request.get('/student/reservation/my', { params })
}