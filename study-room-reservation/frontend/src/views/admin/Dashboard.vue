<template>
  <div class="dashboard">
    <!-- 4 张统计卡 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card" :style="{ background: card.bg }">
          <div class="stat-icon">
            <el-icon :size="32"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">
              <span>{{ card.value }}</span>
              <span class="stat-unit" v-if="card.unit">{{ card.unit }}</span>
            </div>
            <div class="stat-sub">{{ card.sub }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📈 今日预约趋势（按小时）</span>
              <el-radio-group v-model="trendRange" size="small" @change="loadHourly">
                <el-radio-button label="today">今日</el-radio-button>
                <el-radio-button label="week">近 7 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="hourlyRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>🥧 实时座位状态分布</span></template>
          <div ref="pieRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>🔥 座位使用热力图（近 7 天）</span>
              <el-select v-model="heatmapRoomId" size="small" style="width: 240px" @change="loadHeatmap">
                <el-option
                  v-for="r in roomOptions"
                  :key="r.roomId"
                  :label="r.roomName"
                  :value="r.roomId"
                />
              </el-select>
            </div>
          </template>
          <div ref="heatmapRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>📊 各自习室实时使用率</span></template>
          <div ref="roomBarRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <div class="footer-tip">
      <el-icon><InfoFilled /></el-icon>
      <span>数据每 {{ refreshSec }} 秒自动刷新 · 点击顶部"已暂停"可暂停</span>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, reactive, inject, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverallStats, getTodayStats, getSeatStatus, getHeatmap } from '../../api/dashboard'
import { SEAT_STATUS, SEAT_STATUS_LABEL, SEAT_STATUS_COLOR } from '../../constants'

const autoRefresh = inject('autoRefresh', ref(true))
const refreshSec = 5
let timer = null

const stats = reactive({})
const statCards = computed(() => [
  {
    label: '总座位数',
    value: stats.totalSeats ?? '-',
    sub: `${stats.totalCampuses ?? 0} 校区 · ${stats.totalBuildings ?? 0} 楼栋`,
    icon: 'Grid',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    label: '使用中 / 已预约',
    value: stats.occupiedSeats ?? '-',
    sub: `空闲 ${stats.availableSeats ?? 0} 个`,
    icon: 'UserFilled',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    label: '今日预约',
    value: stats.todayReservations ?? '-',
    sub: '包含取消与签到',
    icon: 'Calendar',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    label: '使用率',
    value: stats.utilizationRate ?? 0,
    unit: '%',
    sub: stats.utilizationRate >= 80 ? '🔥 高位运行' : stats.utilizationRate >= 50 ? '正常' : '空闲较多',
    icon: 'TrendCharts',
    bg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

const trendRange = ref('today')
const heatmapRoomId = ref(null)
const roomOptions = ref([])

const hourlyRef = ref(null)
const pieRef = ref(null)
const heatmapRef = ref(null)
const roomBarRef = ref(null)
let chartHourly, chartPie, chartHeat, chartRoomBar

async function loadOverall() {
  const { data } = await getOverallStats()
  Object.assign(stats, data)
}

async function loadHourly() {
  const { data } = await getTodayStats()
  const xs = trendRange.value === 'today' ? data.hourly.map(h => h.hour) : await loadWeek()
  const ys = trendRange.value === 'today' ? data.hourly.map(h => h.count) : (await loadWeek()).map(d => d.reservations)
  const series = trendRange.value === 'today'
    ? [{ name: '预约数', type: 'line', smooth: true, data: ys, areaStyle: { opacity: 0.2 }, itemStyle: { color: '#409eff' } }]
    : [
        { name: '预约', type: 'line', smooth: true, data: ys, itemStyle: { color: '#409eff' } },
        { name: '签到', type: 'line', smooth: true, data: (await loadWeek()).map(d => d.checkins), itemStyle: { color: '#67c23a' } },
        { name: '取消', type: 'line', smooth: true, data: (await loadWeek()).map(d => d.cancels), itemStyle: { color: '#f56c6c' } }
      ]
  chartHourly.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: 40, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: xs, axisLine: { lineStyle: { color: '#dcdfe6' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    series
  })
}

async function loadWeek() {
  const { data } = await getOverallStats()
  return data.weekTrend
}

async function loadPie() {
  const { data } = await getTodayStats()
  const items = Object.entries(data.statusDist).map(([k, v]) => ({
    name: SEAT_STATUS_LABEL[k],
    value: v,
    itemStyle: { color: SEAT_STATUS_COLOR[k] }
  }))
  chartPie.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: items
    }]
  })
}

async function loadRoomBar() {
  const { data } = await getSeatStatus()
  roomOptions.value = data
  if (!heatmapRoomId.value && data.length) heatmapRoomId.value = data[0].roomId
  const top = data.slice().sort((a, b) => b.rate - a.rate).slice(0, 10)
  chartRoomBar.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>使用率：{c}%' },
    grid: { left: 100, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: top.map(d => d.roomName), axisLabel: { width: 90, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: top.map(d => ({
        value: d.rate,
        itemStyle: {
          color: d.rate >= 80 ? '#f56c6c' : d.rate >= 50 ? '#e6a23c' : '#67c23a',
          borderRadius: [0, 4, 4, 0]
        }
      })),
      label: { show: true, position: 'right', formatter: '{c}%' },
      barWidth: 16
    }]
  })
}

async function loadHeatmap() {
  if (!heatmapRoomId.value) return
  const { data } = await getHeatmap(heatmapRoomId.value)
  // x: 列, y: 行, value: 7天累计
  // 改造成 行 × 列 矩阵
  const days = data.days
  const matrix = []
  for (let r = 0; r < data.rows; r++) {
    const row = []
    for (let c = 0; c < data.cols; c++) {
      let v = 0
      for (let day = 0; day < days.length; day++) {
        const found = data.data.find(d => d[0] === c && d[1] === r)
        if (found) v += found[2]
      }
      row.push([r, c, v])
    }
    matrix.push(...row)
  }
  chartHeat.setOption({
    tooltip: {
      formatter: p => `第${p.value[0] + 1}排 第${p.value[1] + 1}列<br/>7天使用：<b>${p.value[2]}</b> 次`
    },
    grid: { left: 60, right: 20, top: 30, bottom: 60 },
    xAxis: {
      type: 'category',
      data: Array.from({ length: data.cols }, (_, i) => `列${i + 1}`),
      splitArea: { show: true }
    },
    yAxis: {
      type: 'category',
      data: Array.from({ length: data.rows }, (_, i) => `行${i + 1}`),
      splitArea: { show: true }
    },
    visualMap: {
      min: 0,
      max: 14,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 10,
      inRange: { color: ['#50e3c2', '#ffd666', '#f56c6c'] }
    },
    series: [{
      name: '使用频次',
      type: 'heatmap',
      data: matrix,
      label: { show: true, color: '#fff', fontSize: 10 },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

function initCharts() {
  chartHourly = echarts.init(hourlyRef.value)
  chartPie = echarts.init(pieRef.value)
  chartHeat = echarts.init(heatmapRef.value)
  chartRoomBar = echarts.init(roomBarRef.value)
  window.addEventListener('resize', resize)
}
function resize() {
  chartHourly?.resize()
  chartPie?.resize()
  chartHeat?.resize()
  chartRoomBar?.resize()
}

async function refreshAll() {
  await Promise.all([loadOverall(), loadHourly(), loadPie(), loadRoomBar(), loadHeatmap()])
}

onMounted(async () => {
  await nextTick()
  initCharts()
  await refreshAll()
  timer = setInterval(() => {
    if (autoRefresh.value) refreshAll()
  }, refreshSec * 1000)
})

onBeforeUnmount(() => {
  clearInterval(timer)
  window.removeEventListener('resize', resize)
  chartHourly?.dispose()
  chartPie?.dispose()
  chartHeat?.dispose()
  chartRoomBar?.dispose()
})
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 16px; }

.stat-row { margin-bottom: 0 !important; }
.chart-row { margin-bottom: 0 !important; }
.stat-card {
  height: 110px;
  border-radius: 10px;
  color: #fff;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-2px); }
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-label { font-size: 13px; opacity: 0.9; }
.stat-value { font-size: 28px; font-weight: bold; line-height: 1.2; }
.stat-unit { font-size: 14px; font-weight: normal; margin-left: 2px; opacity: 0.85; }
.stat-sub { font-size: 12px; opacity: 0.85; margin-top: 2px; }

.chart-card { border-radius: 10px; }
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.chart { height: 320px; width: 100%; }
.footer-tip {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 8px;
}
</style>
