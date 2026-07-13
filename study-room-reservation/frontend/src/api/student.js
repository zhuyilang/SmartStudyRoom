import request from './request'

// 获取校区列表（复用管理端接口）
export function getCampusList() {
  return request.get('/admin/campus/list')
}

// 根据校区查楼栋
export function getBuildingByCampus(campusId) {
  return request.get('/admin/building/list', { params: { campusId } })
}

// 根据楼栋查楼层
export function getFloorByBuilding(buildingId) {
  return request.get('/admin/floor/list/' + buildingId)
}

// 获取自习室列表
export function getRoomList(buildingId) {
  return request.get('/student/room/list', { params: { buildingId: buildingId } })
}

// 获取自习室详情
export function getRoomDetail(roomId) {
  return request.get('/student/room/' + roomId)
}

// 获取座位网格
export function getSeatGrid(roomId) {
  return request.get('/student/seat/grid', { params: { roomId } })
}

// 提交预约
export function createReservation(data) {
  return request.post('/student/reservation/reserve', data)
}

// 取消预约
export function cancelReservation(id) {
  return request.post('/student/reservation/cancel/' + id)
}

// 签到
export function releaseReservation(id) {
  return request.post('/student/reservation/release/' + id)
}

// 签到
export function signInReservation(id) {
  return request.post('/student/reservation/signIn/' + id)
}

// 查询我的预约
export function getMyReservation(status) {
  return request.get('/student/reservation/my', { params: { status } })
}
