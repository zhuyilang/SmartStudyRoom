<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">🚪 自习室列表</span>
      </div>
    </template>

    <!-- 筛选栏 -->
    <el-form :inline="true" :model="form" class="filter-bar">
      <el-form-item label="校区">
        <el-select v-model="form.campusId" placeholder="选择校区" @change="campusChange" :loading="campusLoading" style="width: 160px">
          <el-option v-for="item in campusList" :key="item.id" :label="item.name" :value="item.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="楼栋">
        <el-select v-model="form.buildingId" placeholder="选择楼栋" @change="buildingChange" :loading="buildingLoading" style="width: 160px">
          <el-option v-for="item in buildingList" :key="item.id" :label="item.name" :value="item.id"/>
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 自习室卡片 -->
    <el-row :gutter="16" v-loading="roomLoading" v-if="form.buildingId">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="room in roomList" :key="room.roomId">
        <el-card shadow="hover" class="room-card" @click="$router.push(`/student/seat/${room.roomId}`)">
          <div class="room-header">
            <span class="room-name">{{ room.roomName }}</span>
            <el-tag :type="room.freeSeats > 0 ? 'success' : 'danger'" size="small">
              {{ room.freeSeats > 0 ? '可预约' : '已满' }}
            </el-tag>
          </div>
          <div class="room-info">
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span>{{ room.campusName }} {{ room.buildingName }}</span>
            </div>
            <div class="info-item">
              <el-icon><Grid /></el-icon>
              <span>总座位：{{ room.totalRows * room.totalCols }}</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>空闲：{{ room.freeSeats }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="form.buildingId && roomList.length === 0 && !roomLoading" description="暂无自习室数据" />
    <el-empty v-else-if="!form.buildingId" description="请先选择楼栋查看自习室" />
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCampusList, getBuildingByCampus, getRoomList } from '../../api/student'

const form = ref({ campusId: '', buildingId: '' })
const campusList = ref([])
const buildingList = ref([])
const roomList = ref([])
const campusLoading = ref(false)
const buildingLoading = ref(false)
const roomLoading = ref(false)

// 加载校区
const loadCampus = async () => {
  campusLoading.value = true
  const res = await getCampusList()
  campusLoading.value = false
  if (res.code === 200) {
    campusList.value = res.data.records || res.data
  } else {
    ElMessage.error('加载校区失败：' + res.msg)
  }
}
loadCampus()

// 切换校区
const campusChange = async () => {
  form.value.buildingId = ''
  roomList.value = []
  try {
    buildingLoading.value = true
    const res = await getBuildingByCampus(form.value.campusId)
    if (res.code === 200) {
      buildingList.value = res.data.records || res.data
    } else {
      ElMessage.error('加载楼栋失败：' + res.msg)
    }
  } catch (e) {
    ElMessage.error('网络异常，请检查后端是否启动')
  } finally {
    buildingLoading.value = false
  }
}

// 切换楼栋 - 直接加载自习室
const buildingChange = async () => {
  roomList.value = []
  try {
    roomLoading.value = true
    const res = await getRoomList(form.value.buildingId)
    if (res.code === 200) {
      roomList.value = res.data
    } else {
      ElMessage.error('加载自习室失败：' + res.msg)
    }
  } catch (e) {
    ElMessage.error('网络异常，请检查后端是否启动')
  } finally {
    roomLoading.value = false
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

.filter-bar {
  margin-bottom: 16px;
}

.room-card {
  cursor: pointer;
  margin-bottom: 16px;
  border-radius: 10px;
  transition: all 0.3s;
}

.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.room-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.room-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.info-item .el-icon {
  color: #909399;
}
</style>