import request from './request'

// 各自习室日均使用率（近 7 / 14 / 30 天）
export const getDailyAvgUsage = (params) => request.get('/admin/report/daily-avg-usage', { params })

// 预约高峰时段（24h）
export const getPeakHours = () => request.get('/admin/report/peak-hours')

// 校区分布（座位数占比）
export const getUsageRate = () => request.get('/admin/report/campus-distribution')
