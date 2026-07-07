// 业务常量（与后端 enum 保持一致）

// 座位状态：0 空闲 1 已预约 2 使用中 3 维修
export const SEAT_STATUS = {
  FREE: 0,
  RESERVED: 1,
  IN_USE: 2,
  MAINTENANCE: 3
}
export const SEAT_STATUS_LABEL = {
  [SEAT_STATUS.FREE]: '空闲',
  [SEAT_STATUS.RESERVED]: '已预约',
  [SEAT_STATUS.IN_USE]: '使用中',
  [SEAT_STATUS.MAINTENANCE]: '维修'
}
export const SEAT_STATUS_COLOR = {
  [SEAT_STATUS.FREE]: '#67c23a',
  [SEAT_STATUS.RESERVED]: '#409eff',
  [SEAT_STATUS.IN_USE]: '#e6a23c',
  [SEAT_STATUS.MAINTENANCE]: '#909399'
}

// 自习室类型：0 普通 1 静音 2 讨论 3 24h
export const ROOM_TYPE = {
  NORMAL: 0,
  SILENT: 1,
  DISCUSSION: 2,
  ALL_NIGHT: 3
}
export const ROOM_TYPE_LABEL = {
  [ROOM_TYPE.NORMAL]: '普通',
  [ROOM_TYPE.SILENT]: '静音',
  [ROOM_TYPE.DISCUSSION]: '讨论',
  [ROOM_TYPE.ALL_NIGHT]: '24小时'
}

// 角色
export const ROLE = {
  STUDENT: 'STUDENT',
  ADMIN: 'ADMIN'
}
export const ROLE_LABEL = {
  [ROLE.STUDENT]: '学生',
  [ROLE.ADMIN]: '管理员'
}
