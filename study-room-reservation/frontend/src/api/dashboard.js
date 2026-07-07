import request from './request'

// 总览数据（顶部 4 张统计卡 + 折线 + 饼图）
export const getOverallStats = () => request.get('/admin/dashboard/overall')

// 今日分时数据（24h 折线 + 状态分布饼）
export const getTodayStats = () => request.get('/admin/dashboard/today')

// 座位状态总览（房间占用率横向条形图）
export const getSeatStatus = () => request.get('/admin/dashboard/seat-status')

// 单个自习室的热力图（7 天 × 行 × 列）
export const getHeatmap = (roomId) => request.get(`/admin/dashboard/heatmap/${roomId}`)
