import request from './request'

// 预约趋势（7 / 14 / 30 天）
export const getReservationTrend = (params) => request.get('/admin/report/trend', { params })

// 预约高峰时段（24h）
export const getPeakHours = () => request.get('/admin/report/peak-hours')

// 校区分布（座位数占比）
export const getUsageRate = () => request.get('/admin/report/campus-distribution')

// 自习室使用率 TOP
export const getRoomUsageRank = () => request.get('/admin/report/room-usage')
