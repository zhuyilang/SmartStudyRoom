/** 工具函数：时间格式化 */
export function formatTime(ts, withSec = true) {
  if (!ts) return "-"
  
  // 处理后端返回的 LocalDateTime 数组 [year, month, day, hour, minute, second]
  if (Array.isArray(ts)) {
    const [y, m, d, h = 0, min = 0, s = 0] = ts
    const pad = (n) => String(n).padStart(2, "0")
    return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}${withSec ? ":" + pad(s) : ""}`
  }

  // 处理 "yyyy-MM-dd HH:mm:ss" 格式（替换空格为T）
  let dateStr = ts
  if (typeof ts === "string" && ts.includes(" ") && ts.includes("-")) {
    dateStr = ts.replace(" ", "T")
  }

  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return ts // 解析失败则返回原文

  const pad = (n) => String(n).padStart(2, "0")
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}${withSec ? ":" + pad(d.getSeconds()) : ""}`
}

export function formatDate(ts) {
  if (!ts) return "-"

  // 处理数组
  if (Array.isArray(ts)) {
    const [y, m, d] = ts
    const pad = (n) => String(n).padStart(2, "0")
    return `${y}-${pad(m)}-${pad(d)}`
  }

  let dateStr = ts
  if (typeof ts === "string" && ts.includes(" ") && ts.includes("-")) {
    dateStr = ts.replace(" ", "T")
  }

  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return ts
  const pad = (n) => String(n).padStart(2, "0")
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
