<template>
  <div class="room-page">
    <!-- 筛选栏 -->
    <el-card style="margin-bottom:20px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="form.campusId" placeholder="选择校区" @change="campusChange" :loading="campusLoading">
            <el-option v-for="item in campusList" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="form.buildingId" placeholder="选择楼栋" @change="buildingChange" :loading="buildingLoading">
            <el-option v-for="item in buildingList" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-col>
      </el-row>
    </el-card>

    <!-- 自习室卡片 -->
    <el-row :gutter="20" v-loading="roomLoading" v-if="form.buildingId">
      <el-col :span="6" v-for="room in roomList" :key="room.roomId">
        <el-card shadow="hover" class="room-card" @click="$router.push(`/student/seat/${room.roomId}`)">
          <h3>{{ room.roomName }}</h3>
          <p>位置：{{ room.campusName }} {{ room.buildingName }}</p>
          <p>总座位：{{ room.totalRows * room.totalCols }}</p>
          <el-tag :type="room.freeSeats > 0 ? 'success' : 'info'">
            {{ room.freeSeats > 0 ? '可预约' : '已满' }}
          </el-tag>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="form.buildingId && roomList.length === 0 && !roomLoading" description="暂无自习室数据" />
    <el-empty v-else description="请先选择楼栋查看自习室" />
  </div>
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
.room-card {
  cursor: pointer;
}
.room-card:hover {
  transform: translateY(-4px);
  transition: 0.3s;
}
</style>