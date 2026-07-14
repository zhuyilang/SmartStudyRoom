<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">📋 我的预约记录</span>
        <el-button type="primary" @click="$router.push('/student/roomList')">
          <el-icon><Plus /></el-icon>
          新预约
        </el-button>
      </div>
    </template>

    <el-tabs v-model="activeTab" @tab-change="loadList" class="status-tabs">
      <el-tab-pane label="全部" name="all">
        <template #label>
          <span><el-icon><List /></el-icon> 全部</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已预约" name="0">
        <template #label>
          <span><el-icon><Clock /></el-icon> 已预约</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已签到" name="1">
        <template #label>
          <span><el-icon><Check /></el-icon> 已签到</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已取消" name="2">
        <template #label>
          <span><el-icon><Close /></el-icon> 已取消</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已超时" name="3">
        <template #label>
          <span><el-icon><Warning /></el-icon> 已超时</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <el-table :data="resList" border stripe style="width: 100%" v-loading="listLoading" class="data-table">
      <el-table-column prop="roomId" label="自习室ID" width="90" align="center" />
      <el-table-column prop="seatId" label="座位ID" width="80" align="center" />
      <el-table-column label="预约时间" min-width="220">
        <template #default="scope">
          <div class="time-info">
            <div class="time-row">
              <el-icon><Calendar /></el-icon>
              <span>预约: {{ formatTime(scope.row.createTime) }}</span>
            </div>
            <div class="time-row deadline">
              <el-icon><Clock /></el-icon>
              <span>签到截止: {{ getSigninDeadline(scope.row.createTime) }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)" size="small">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" type="success" size="small" :icon="Check" @click="signIn(scope.row.id)">签到</el-button>
          <el-button v-if="scope.row.status === 0" type="danger" size="small" :icon="Close" @click="cancel(scope.row.id)">取消</el-button>
          <el-button v-if="scope.row.status === 1" type="warning" size="small" :icon="Unlock" @click="releaseSeat(scope.row.id)">释放</el-button>
          <span v-if="scope.row.status !== 0 && scope.row.status !== 1" class="no-action">-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="resList.length === 0 && !listLoading" description="暂无预约记录">
      <template #image>
        <el-icon size="60" color="#c0c4cc"><Document /></el-icon>
      </template>
    </el-empty>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyReservation, cancelReservation, signInReservation, releaseReservation } from '../../api/student'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatTime } from '../../utils/format'

const activeTab = ref('all')
const resList = ref([])
const listLoading = ref(false)

const loadList = async () => {
  listLoading.value = true
  const status = activeTab.value === 'all' ? '' : activeTab.value
  const res = await getMyReservation(status)
  if (res.code === 200) resList.value = res.data
  listLoading.value = false
}
onMounted(loadList)

const getStatusText = (s) => {
  const map = {0:'已预约',1:'已签到',2:'已取消',3:'已超时',4:'已完成'}
  return map[s]
}
const getStatusTag = (s) => {
  const map = {0:'warning',1:'success',2:'info',3:'danger',4:'primary'}
  return map[s]
}

function getSigninDeadline(ts) {
  if (!ts) return "-"
  if (Array.isArray(ts)) {
    const [y, m, d, h, min, s = 0] = ts
    const date = new Date(y, m - 1, d, h || 0, (min || 0) + 30, s || 0)
    const pad = n => String(n).padStart(2, "0")
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  }
  let dateStr = ts
  if (typeof ts === "string" && ts.includes(" ") && ts.includes("-")) {
    dateStr = ts.replace(" ", "T")
  }
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return ts
  d.setMinutes(d.getMinutes() + 30)
  const pad = n => String(n).padStart(2, "0")
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const releaseSeat = async (id) => {
  const res = await releaseReservation(id)
  if (res.code === 200) {
    ElMessage.success('座位已释放')
    loadList()
  } else ElMessage.error(res.msg)
}

const signIn = async (id) => {
  const res = await signInReservation(id)
  if (res.code === 200) {
    ElMessage.success('签到成功')
    loadList()
  } else ElMessage.error(res.msg)
}

const cancel = async (id) => {
  await ElMessageBox.confirm('确定取消该预约？取消后座位将释放', '提示', { type: 'warning' })
  const res = await cancelReservation(id)
  if (res.code === 200) {
    ElMessage.success('已取消预约')
    loadList()
  } else ElMessage.error(res.msg)
}
</script>

<style scoped>
.page-card {
  border-radius: 10px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.status-tabs {
  margin-bottom: 16px;
}

.status-tabs :deep(.el-tabs__item) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.data-table {
  border-radius: 8px;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.time-row .el-icon {
  color: #909399;
}

.time-row.deadline {
  color: #e6a23c;
}

.time-row.deadline .el-icon {
  color: #e6a23c;
}

.no-action {
  color: #c0c4cc;
}
</style>