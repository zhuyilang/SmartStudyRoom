<template>
  <el-row :gutter="30">
    <!-- 左侧座位网格 -->
    <el-col :span="14">
      <el-card title="座位选择" v-loading="seatLoading">
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
    <el-col :span="10">
      <el-card title="预约信息" v-if="selectSeat">
        <p>选中座位：{{ selectSeat.row }}排{{ selectSeat.col }}列</p>
        <p style="font-size:13px; color:#909399; margin-bottom:16px;">预约后请在30分钟内签到，超时将自动释放</p>
          <el-button type="primary" @click="submitReserve" :loading="submitLoading" size="large" style="width:100%">确认预约</el-button>
      </el-card>
      <el-empty v-else description="请点击左侧绿色空闲座位"></el-empty>
    </el-col>
  </el-row>
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

// 预约表单
const reserveForm = ref({
  date: '',
  startTime: '',
  endTime: ''
})

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

// 提交预约（增加时间校验）
const submitReserve = async () => {
  // 防护：未选座位直接拦截
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
.seat-grid {
  display: grid;
  gap: 8px;
  width: fit-content;
  margin: 0 auto;
}
.seat {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.free {
  background: #67c23a;
  color: #fff;
}
.used {
  background: #f56c6c;
  color: #fff;
  cursor: not-allowed;
}
.active {
  border: 3px solid #409EFF;
}
</style>