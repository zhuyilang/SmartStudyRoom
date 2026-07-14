<template>
  <div class="seat-page">
    <el-card shadow="hover" class="header-card">
      <div class="seat-header">
        <div class="title-area">
          <el-button :icon="ArrowLeft" @click="goBack" circle />
          <div>
            <div class="page-title">💺 座位管理 · {{ roomName }}</div>
            <div class="page-sub">{{ roomPath }} · 容量 {{ room?.capacity || 0 }} · 已有 {{ seats.length }} 个座位</div>
          </div>
        </div>
        <div class="actions">
          <el-tag :type="autoRefresh ? 'success' : 'info'" effect="plain" size="small" style="margin-right: 8px">
            {{ autoRefresh ? '实时刷新中' : '已暂停' }}
          </el-tag>
          <el-button :icon="Refresh" @click="loadSeats">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="openBatch">批量添加</el-button>
        </div>
      </div>
      <!-- 状态图例 -->
      <div class="legend">
        <span v-for="(label, val) in SEAT_STATUS_LABEL" :key="val" class="legend-item">
          <span class="legend-dot" :style="{ background: SEAT_STATUS_COLOR[val] }"></span>{{ label }}
        </span>
      </div>
    </el-card>

    <el-card shadow="hover" class="seat-card">
      <template #header>
        <div class="card-header">
          <span>座位布局（点击座位切换状态）</span>
          <span class="dim">提示：维修中的座位学生不可预约</span>
        </div>
      </template>

      <div v-loading="loading" class="seat-grid-wrapper">
        <div v-if="!seats.length" class="empty">
          <el-empty description="该自习室暂无座位，点击右上角&quot;批量添加&quot;开始创建" />
        </div>
        <div v-else class="seat-grid" :style="{ gridTemplateColumns: `60px repeat(${cols}, 1fr)` }">
          <div v-for="r in rows" :key="`r-${r}`" class="row-label">第{{ r }}排</div>
          <template v-for="r in rows" :key="`row-${r}`">
            <div v-for="c in cols" :key="`s-${r}-${c}`" class="seat-wrapper">
              <div
                v-if="getSeat(r, c)"
                class="seat"
                :style="{ background: SEAT_STATUS_COLOR[getSeat(r, c).status] }"
                :title="`${getSeat(r, c).label} - ${SEAT_STATUS_LABEL[getSeat(r, c).status]}`"
                @click="onSeatClick(getSeat(r, c))"
              >
                <span class="seat-num">{{ c }}</span>
              </div>
              <div v-else class="seat seat-empty">+</div>
            </div>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 状态切换弹窗 -->
    <el-dialog v-model="statusDialogVisible" :title="`切换座位状态：${currentSeat?.label}`" width="420px">
      <el-radio-group v-model="newStatus" style="display: flex; flex-direction: column; gap: 8px">
        <el-radio v-for="(label, val) in SEAT_STATUS_LABEL" :key="val" :value="Number(val)" :disabled="Number(val) === currentSeat?.status">
          <span class="status-radio">
            <span class="legend-dot" :style="{ background: SEAT_STATUS_COLOR[val] }"></span>
            {{ label }}
          </span>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStatus">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量添加 -->
    <el-dialog v-model="batchDialogVisible" title="批量添加座位" width="480px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="起始行">
          <el-input-number v-model="batchForm.startRow" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="行数">
          <el-input-number v-model="batchForm.rows" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="起始列">
          <el-input-number v-model="batchForm.startCol" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="列数">
          <el-input-number v-model="batchForm.cols" :min="1" :max="20" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon>
          将自动跳过已存在的座位（按行×列判断）
        </el-alert>
        <div class="batch-preview">预计新增：<b>{{ batchForm.rows * batchForm.cols }}</b> 个座位</div>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatch">开始添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, inject, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh, Plus } from '@element-plus/icons-vue'
import { getSeatList, updateSeatStatus, deleteSeat, batchAddSeats } from '../../api/admin'
import { getRoomById } from '../../api/admin'
import { SEAT_STATUS_LABEL, SEAT_STATUS_COLOR } from '../../constants'

const route = useRoute()
const router = useRouter()
const autoRefresh = inject('autoRefresh', ref(true))
const roomId = computed(() => Number(route.params.roomId))

const room = ref(null)
const seats = ref([])
const loading = ref(false)
let timer = null

const rows = computed(() => Math.max(0, ...seats.value.map(s => s.row)))
const cols = computed(() => Math.max(0, ...seats.value.map(s => s.col)))

const statusDialogVisible = ref(false)
const currentSeat = ref(null)
const newStatus = ref(0)

const batchDialogVisible = ref(false)
const batchForm = reactive({ startRow: 1, rows: 5, startCol: 1, cols: 5 })

function getSeat(r, c) {
  return seats.value.find(s => s.row === r && s.col === c)
}

const roomPath = computed(() => {
  if (!room.value) return ''
  // 这里简化：只显示房间名
  return room.value.name
})
const roomName = computed(() => room.value?.name || '加载中...')

async function loadRoom() {
  const { data } = await getRoomById(roomId.value)
  room.value = data
}
async function loadSeats() {
  loading.value = true
  try {
    const { data } = await getSeatList(roomId.value)
    seats.value = data
  } finally { loading.value = false }
}

function onSeatClick(seat) {
  currentSeat.value = seat
  newStatus.value = seat.status
  statusDialogVisible.value = true
}
async function confirmStatus() {
  await updateSeatStatus({ id: currentSeat.value.id, status: newStatus.value })
  ElMessage.success('状态已更新')
  statusDialogVisible.value = false
  await loadSeats()
}
async function shuffleStatus() {
  // 后端无对应接口时，仅刷新一次以等待真实数据
  ElMessage.info('请等待后端推送实时数据…')
  await loadSeats()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除座位 ${row.label}？`, '提示', { type: 'warning' })
  await deleteSeat(row.id)
  ElMessage.success('已删除')
  loadSeats()
}

async function openBatch() { batchDialogVisible.value = true }
async function confirmBatch() {
  const { data } = await batchAddSeats({ roomId: roomId.value, ...batchForm })
  ElMessage.success(`新增 ${data.length} 个座位`)
  batchDialogVisible.value = false
  await loadSeats()
}

function goBack() { router.push('/admin/room') }

onMounted(async () => {
  await nextTick()
  await loadRoom()
  await loadSeats()
  timer = setInterval(() => {
    if (autoRefresh.value) loadSeats()
  }, 5000)
})
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.seat-page { display: flex; flex-direction: column; gap: 16px; }
.header-card, .seat-card { border-radius: 10px; }
.seat-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.title-area { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 18px; font-weight: 600; }
.page-sub { font-size: 12px; color: #909399; margin-top: 2px; }
.actions { display: flex; align-items: center; }
.legend { display: flex; gap: 16px; margin-top: 12px; flex-wrap: wrap; }
.legend-item { display: inline-flex; align-items: center; gap: 6px; font-size: 13px; color: #606266; }
.legend-dot { width: 14px; height: 14px; border-radius: 3px; display: inline-block; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.dim { font-size: 12px; color: #909399; }
.seat-grid-wrapper { padding: 16px 0; min-height: 200px; }
.seat-grid { display: grid; gap: 8px; align-items: center; }
.row-label { color: #909399; font-size: 12px; text-align: right; padding-right: 8px; }
.seat-wrapper { display: flex; align-items: center; justify-content: center; }
.seat {
  width: 44px; height: 44px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 13px; font-weight: 500; cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  user-select: none;
}
.seat:hover { transform: scale(1.08); box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15); }
.seat-empty {
  background: #f4f4f5 !important;
  color: #c0c4cc !important;
  border: 1px dashed #dcdfe6;
  cursor: default;
}
.seat-empty:hover { transform: none; box-shadow: none; }
.seat-num { font-size: 13px; }
.empty { padding: 40px 0; }
.status-radio { display: inline-flex; align-items: center; gap: 6px; }
.batch-preview { margin-top: 12px; color: #606266; font-size: 13px; }
</style>
