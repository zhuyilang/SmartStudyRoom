import request from './request'

export function getUsageRate(params) {
  return request.get('/report/usageRate', { params })
}

export function getPeakHours(params) {
  return request.get('/report/peakHours', { params })
}

export function getReservationTrend(params) {
  return request.get('/report/reservationTrend', { params })
}

export function getRoomUsageRank(params) {
  return request.get('/report/roomUsageRank', { params })
}

export function getStudentUsageStats(params) {
  return request.get('/report/studentUsage', { params })
}