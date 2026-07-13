<template>
  <el-card title="我的预约记录">
    <el-tabs v-model="activeTab" @tab-change="loadList">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="已预约" name="0"></el-tab-pane>
      <el-tab-pane label="已签到" name="1"></el-tab-pane>
      <el-tab-pane label="已取消" name="2"></el-tab-pane>
      <el-tab-pane label="已超时" name="3"></el-tab-pane>
    </el-tabs>

    <el-table :data="resList" border style="margin-top:20px;" v-loading="listLoading">
      <el-table-column label="自习室ID" prop="roomId" width="90" />
      <el-table-column label="座位ID" prop="seatId" width="80" />
      <el-table-column label="预约时间" min-width="220">
        <template #default="scope">
          <div>预约: {{ formatTime(scope.row.createTime) }}</div>
          <div>签到截止: {{ getSigninDeadline(scope.row.createTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)" size="small">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" size="small" @click="signIn(scope.row.id)">签到</el-button>
          <el-button v-if="scope.row.status === 0" size="small" type="danger" @click="cancel(scope.row.id)">取消</el-button>
          <el-button v-if="scope.row.status === 1" size="small" type="warning" @click="releaseSeat(scope.row.id)">释放座位</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="resList.length === 0 && !listLoading" description="暂无预约记录" />
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
  await ElMessageBox.confirm('确定取消该预约？取消后座位将释放')
  const res = await cancelReservation(id)
  if (res.code === 200) {
    ElMessage.success('已取消预约')
    loadList()
  } else ElMessage.error(res.msg)
}
</script>