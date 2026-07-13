import request from './request'

// ============== 校区 ==============
export const getCampusList = () => request.get('/admin/campus/list')
export const addCampus = (data) => request.post('/admin/campus', data)
export const updateCampus = (data) => request.put('/admin/campus', data)
export const deleteCampus = (id) => request.delete(`/admin/campus/${id}`)

// ============== 楼栋 ==============
export const getBuildingList = (params) => request.get('/admin/building/list', { params })
export const addBuilding = (data) => request.post('/admin/building', data)
export const updateBuilding = (data) => request.put('/admin/building', data)
export const deleteBuilding = (id) => request.delete(`/admin/building/${id}`)

// ============== 楼层 ==============
export const getFloorList = (params) => request.get('/admin/floor/list', { params })
export const addFloor = (data) => request.post('/admin/floor', data)
export const updateFloor = (data) => request.put('/admin/floor', data)
export const deleteFloor = (id) => request.delete(`/admin/floor/${id}`)

// ============== 自习室 ==============
export const getRoomList = (params) => request.get('/admin/room/list', { params })
export const getRoomById = (id) => request.get(`/admin/room/${id}`)
export const addRoom = (data) => request.post('/admin/room', data)
export const updateRoom = (data) => request.put('/admin/room', data)
export const deleteRoom = (id) => request.delete(`/admin/room/${id}`)

// ============== 座位 ==============
export const getSeatList = (roomId) => request.get(`/admin/room/${roomId}/seats`)
export const addSeat = (data) => request.post('/admin/seat', data)
export const batchAddSeats = (data) => request.post('/admin/seat/batch', data)
export const updateSeat = (data) => request.put('/admin/seat', data)
export const updateSeatStatus = (data) => request.patch('/admin/seat/status', data)
export const deleteSeat = (id) => request.delete(`/admin/seat/${id}`)

// ============== 用户 ==============
export const getUserList = (params) => request.get('/admin/user/list', { params })
