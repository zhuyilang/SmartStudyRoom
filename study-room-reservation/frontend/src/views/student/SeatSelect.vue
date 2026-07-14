<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">🪑 座位选择</span>
        <el-button type="primary" link @click="$router.push('/student/roomList')">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>
    </template>

    <el-row :gutter="24">
      <!-- 左侧座位网格 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="seat-card" v-loading="seatLoading">
          <template #header>
            <div class="card-header">
              <span>座位分布</span>
              <div class="legend">
                <span class="legend-item"><span class="dot free"></span>空闲</span>
                <span class="legend-item"><span class="dot used"></span>占用</span>
                <span class="legend-item"><span class="dot active"></span>已选</span>
              </div>
            </div>
          </template>
          <div class="seat-grid" :style="{gridTemplateRows: `repeat(${roomInfo.totalRows}, 40px)`, gridTemplateColumns: `repeat(${roomInfo.totalCols}, 40px)`}">
            <div
              v-for="s in seatData"
              :key="`${s.row}-${s.col}`"
              class="seat"
              :class="{
                free: s.status === 0,
                used: s.status === 1 || s.status === 2,
                active: selectSeat && selectSeat.row === s.row && selectSeat.col === s.col
              }"
              @click="selectHandle(s)"
            >
              {{ s.row }}-{{ s.col }}
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧预约面板 -->
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover" class="reserve-card" v-if="selectSeat">
          <template #header>
            <div class="card-header">
              <span>预约信息</span>
            </div>
          </template>
          <div class="reserve-info">
            <div class="info-row">
              <el-icon><Location /></el-icon>
              <span>选中座位：{{ selectSeat.row }}排 {{ selectSeat.col }}列</span>
            </div>
            <div class="tip-text">
              <el-icon><InfoFilled /></el-icon>
              <span>预约后请在30分钟内签到，超时将自动释放</span>
            </div>
          </div>
          <el-button type="primary" @click="submitReserve" :loading="submitLoading" size="large" class="reserve-btn">
            <el-icon><Check /></el-icon>
            确认预约
          </el-button>
        </el-card>
        <el-card shadow="hover" class="reserve-card empty-card" v-else>
          <el-empty description="请点击左侧绿色空闲座位">
            <template #image>
              <el-icon size="60" color="#c0c4cc"><Pointer /></el-icon>
            </template>
          </el-empty>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSeatGrid, createReservation } from '../../api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const roomId = route.params.roomId
const roomInfo = ref({ totalRows: 0, totalCols: 0, openTime: '', closeTime: '' })
const seatData = ref([])
const selectSeat = ref(null)
const seatLoading = ref(false)
const submitLoading = ref(false)

// 加载座位网格
const loadSeat = async () => {
  try {
    seatLoading.value = true
    const res = await getSeatGrid(roomId)
    if (res.code === 200) {
      roomInfo.value = {
        totalRows: res.data.rows,
        totalCols: res.data.cols
      }
      seatData.value = res.data.seats
    } else {
      ElMessage.error('加载座位失败：' + res.msg)
    }
  } catch (e) {
    ElMessage.error('网络异常，无法加载座位数据')
  } finally {
    seatLoading.value = false
  }
}
onMounted(() => loadSeat())

// 点击座位
const selectHandle = (seat) => {
  if (seat.status !== 0) {
    ElMessage.warning('该座位已被占用，无法选择')
    return
  }
  selectSeat.value = seat
}

// 提交预约
const submitReserve = async () => {
  if (!selectSeat.value) {
    ElMessage.warning('请先选择一个空闲座位')
    return
  }
  submitLoading.value = true
  const params = {
    seatId: selectSeat.value.id,
    roomId: roomId
  }
  const res = await createReservation(params)
  submitLoading.value = false
  if (res.code === 200) {
    ElMessage.success('预约成功！')
    router.push('/student/myRes')
  } else {
    ElMessage.error(res.msg)
  }
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

.seat-card,
.reserve-card {
  border-radius: 10px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.free {
  background: #67c23a;
}

.dot.used {
  background: #f56c6c;
}

.dot.active {
  background: #409eff;
  border: 2px solid #409eff;
}

.seat-grid {
  display: grid;
  gap: 8px;
  width: fit-content;
  margin: 0 auto;
  padding: 16px;
}

.seat {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.seat.free {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
}

.seat.free:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
}

.seat.used {
  background: #f56c6c;
  color: #fff;
  cursor: not-allowed;
  opacity: 0.8;
}

.seat.active {
  border: 3px solid #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transform: scale(1.1);
}

.reserve-info {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #303133;
  margin-bottom: 12px;
}

.tip-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  background: #f4f4f5;
  padding: 12px;
  border-radius: 6px;
}

.reserve-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.empty-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}
</style>