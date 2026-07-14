<template>
  <div class="report">
    <!-- 筛选 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="时间范围">
          <el-radio-group v-model="filter.days" @change="loadAll">
            <el-radio-button :value="1">近 1 天</el-radio-button>
            <el-radio-button :value="7">近 7 天</el-radio-button>
            <el-radio-button :value="14">近 14 天</el-radio-button>
            <el-radio-button :value="30">近 30 天</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="校区">
          <el-select v-model="filter.campusId" placeholder="全部" clearable style="width: 160px" @change="loadPie">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Refresh" @click="loadAll">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>📊 各自习室日均使用率</span></template>
          <div ref="dailyRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><span>⏰ 预约高峰时段</span></template>
          <div ref="peakRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><span>🏫 校区分布（按座位数）</span></template>
          <div ref="campusRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { getDailyAvgUsage, getPeakHours, getUsageRate } from '../../api/report'
import { getCampusList } from '../../api/admin'

const filter = reactive({ days: 1, campusId: null })
const campuses = ref([])

const dailyRef = ref(null)
const peakRef = ref(null)
const campusRef = ref(null)
let cDaily, cPeak, cCampus

async function loadDailyAvgUsage() {
  const { data } = await getDailyAvgUsage({ days: filter.days })
  cDaily.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 100, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: data.map(d => d.roomName).reverse(), axisLabel: { width: 90, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: data.map(d => d.avgDailyRate).reverse(),
      itemStyle: {
        color: p => p.value >= 80 ? '#f56c6c' : p.value >= 50 ? '#e6a23c' : '#67c23a',
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', formatter: '{c}%' },
      barWidth: 16
    }]
  })
}
async function loadPeak() {
  const { data } = await getPeakHours()
  cPeak.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: data.map(d => d.hour) },
    yAxis: { type: 'value', name: '预约量' },
    series: [{
      name: '预约',
      type: 'bar',
      data: data.map(d => ({
        value: Math.round(d.value),
        itemStyle: {
          color: d.value >= 80 ? '#f56c6c' : d.value >= 50 ? '#e6a23c' : '#67c23a',
          borderRadius: [4, 4, 0, 0]
        }
      })),
      barWidth: '50%',
      markLine: {
        data: [{ type: 'average', name: '均值' }],
        lineStyle: { color: '#909399', type: 'dashed' }
      }
    }]
  })
}
async function loadPie() {
  const { data } = await getUsageRate()
  let items = data
  if (filter.campusId) items = items.filter(d => {
    const c = campuses.value.find(c => c.id === filter.campusId)
    return c && d.name === c.name
  })
  cCampus.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>座位数：{c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{c}座' },
      data: items.map(d => ({
        name: d.name,
        value: d.value,
        itemStyle: { color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'][items.indexOf(d) % 5] }
      }))
    }]
  })
}

async function loadAll() {
  await Promise.all([loadDailyAvgUsage(), loadPeak(), loadPie()])
}

function init() {
  cDaily = echarts.init(dailyRef.value)
  cPeak = echarts.init(peakRef.value)
  cCampus = echarts.init(campusRef.value)
  window.addEventListener('resize', resize)
}
function resize() { cDaily?.resize(); cPeak?.resize(); cCampus?.resize() }

onMounted(async () => {
  await nextTick()
  init()
  const res = await getCampusList()
  campuses.value = res.data.records || res.data
  await loadAll()
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  cDaily?.dispose(); cPeak?.dispose(); cCampus?.dispose()
})
</script>

<style scoped>
.report { display: flex; flex-direction: column; gap: 16px; }
.filter-card { border-radius: 10px; }
.row { margin-bottom: 0 !important; }
.chart { height: 320px; width: 100%; }
</style>
