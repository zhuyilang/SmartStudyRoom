import request from './request'

// ========== 校区管理 ==========
export function getCampusList() {
  return request.get('/admin/campus/list')
}

export function getCampusById(id) {
  return request.get(`/admin/campus/${id}`)
}

export function addCampus(data) {
  return request.post('/admin/campus/add', data)
}

export function updateCampus(data) {
  return request.put('/admin/campus/update', data)
}

export function deleteCampus(id) {
  return request.delete(`/admin/campus/delete/${id}`)
}

// ========== 楼栋管理 ==========
export function getBuildingList(campusId) {
  return request.get('/admin/building/list', { params: { campusId } })
}

export function getBuildingById(id) {
  return request.get(`/admin/building/${id}`)
}

export function addBuilding(data) {
  return request.post('/admin/building/add', data)
}

export function updateBuilding(data) {
  return request.put('/admin/building/update', data)
}

export function deleteBuilding(id) {
  return request.delete(`/admin/building/delete/${id}`)
}

// ========== 楼层管理 ==========
export function getFloorList(buildingId) {
  return request.get('/admin/floor/list', { params: { buildingId } })
}

export function getFloorById(id) {
  return request.get(`/admin/floor/${id}`)
}

export function addFloor(data) {
  return request.post('/admin/floor/add', data)
}

export function updateFloor(data) {
  return request.put('/admin/floor/update', data)
}

export function deleteFloor(id) {
  return request.delete(`/admin/floor/delete/${id}`)
}

// ========== 自习室管理 ==========
export function getRoomList(params) {
  return request.get('/admin/room/list', { params })
}

export function getRoomById(id) {
  return request.get(`/admin/room/${id}`)
}

export function addRoom(data) {
  return request.post('/admin/room/add', data)
}

export function updateRoom(data) {
  return request.put('/admin/room/update', data)
}

export function deleteRoom(id) {
  return request.delete(`/admin/room/delete/${id}`)
}

// ========== 座位管理 ==========
export function getSeatList(roomId) {
  return request.get('/admin/seat/list', { params: { roomId } })
}

export function getSeatById(id) {
  return request.get(`/admin/seat/${id}`)
}

export function addSeat(data) {
  return request.post('/admin/seat/add', data)
}

export function batchAddSeats(data) {
  return request.post('/admin/seat/batchAdd', data)
}

export function updateSeat(data) {
  return request.put('/admin/seat/update', data)
}

export function updateSeatStatus(data) {
  return request.put('/admin/seat/updateStatus', data)
}

export function deleteSeat(id) {
  return request.delete(`/admin/seat/delete/${id}`)
}