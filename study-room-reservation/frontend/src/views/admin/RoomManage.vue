<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">🚪 自习室管理</span>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增自习室</el-button>
      </div>
    </template>

    <el-form :inline="true" :model="filter" class="filter-bar">
      <el-form-item label="校区">
        <el-select v-model="filter.campusId" placeholder="全部" clearable style="width: 140px" @change="onCampusChange">
          <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="楼栋">
        <el-select v-model="filter.buildingId" placeholder="全部" clearable style="width: 160px" @change="onBuildingChange">
          <el-option v-for="b in filteredBuildings" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="楼层">
        <el-select v-model="filter.floorId" placeholder="全部" clearable style="width: 140px" @change="loadList">
          <el-option v-for="f in filteredFloors" :key="f.id" :label="f.name" :value="f.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键字">
        <el-input v-model="filter.keyword" placeholder="房间号" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
        <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="pagedList" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="房间号" min-width="100" />
      <el-table-column label="所属楼层" min-width="140">
        <template #default="{ row }">{{ getFloorName(row.floorId) }}</template>
      </el-table-column>
      <el-table-column label="所属楼栋" min-width="140">
        <template #default="{ row }">{{ getBuildingNameByFloor(row.floorId) }}</template>
      </el-table-column>
      <el-table-column prop="capacity" label="座位数" width="90" align="center" />
      <el-table-column label="类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'warning' : row.type === 3 ? 'danger' : 'info'" size="small">
            {{ ROOM_TYPE_LABEL[row.type] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="设施" width="120" align="center">
        <template #default="{ row }">
          <el-icon v-if="row.hasPower" color="#67c23a"><Check /></el-icon>
          <span v-else class="icon-disabled">—</span>
          <span style="margin: 0 6px">|</span>
          <el-icon v-if="row.hasNetwork" color="#67c23a"><Connection /></el-icon>
          <span v-else class="icon-disabled">—</span>
          <span style="margin-left: 4px; font-size: 12px; color: #909399">电/网</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="success" link :icon="Grid" @click="goSeats(row)">座位</el-button>
          <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      v-model:current-page="page.current"
      v-model:page-size="page.size"
      :page-sizes="[10, 20, 50]"
      :total="filteredList.length"
      background
      layout="total, sizes, prev, pager, next, jumper"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑自习室' : '新增自习室'"
      width="560px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="校区" prop="campusId">
          <el-select v-model="form.campusId" placeholder="请选择" style="width: 100%" @change="onFormCampusChange">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请先选校区" style="width: 100%" :disabled="!form.campusId" @change="onFormBuildingChange">
            <el-option v-for="b in formBuildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floorId">
          <el-select v-model="form.floorId" placeholder="请先选楼栋" style="width: 100%" :disabled="!form.buildingId">
            <el-option v-for="f in formFloors" :key="f.id" :label="f.name" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="name">
          <el-input v-model="form.name" placeholder="如：101" maxlength="10" />
        </el-form-item>
        <el-form-item label="座位数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="500" style="width: 100%" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button v-for="(label, val) in ROOM_TYPE_LABEL" :key="val" :value="Number(val)">{{ label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设施">
          <el-checkbox :model-value="!!form.hasPower" @update:model-value="v => form.hasPower = v ? 1 : 0">电源</el-checkbox>
          <el-checkbox :model-value="!!form.hasNetwork" @update:model-value="v => form.hasNetwork = v ? 1 : 0">网络</el-checkbox>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Grid, Check, Connection } from '@element-plus/icons-vue'
import {
  getRoomList, addRoom, updateRoom, deleteRoom,
  getBuildingList, getFloorList, getCampusList
} from '../../api/admin'
import { formatTime } from '../../utils/format'
import { ROOM_TYPE_LABEL } from '../../constants'

const router = useRouter()
const list = ref([])
const buildings = ref([])
const floors = ref([])
const campuses = ref([])
const loading = ref(false)
const filter = reactive({ campusId: null, buildingId: null, floorId: null, keyword: '' })
const page = reactive({ current: 1, size: 10 })

const dialogVisible = ref(false)
const editing = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, campusId: null, buildingId: null, floorId: null,
  name: '', capacity: 60, type: 0, hasPower: 1, hasNetwork: 1, description: ''
})
const rules = {
  campusId: [{ required: true, message: '请选择校区', trigger: 'change' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }],
  name: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入座位数', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const filteredBuildings = computed(() => filter.campusId ? buildings.value.filter(b => b.campusId === filter.campusId) : buildings.value)
const filteredFloors = computed(() => filter.buildingId ? floors.value.filter(f => f.buildingId === filter.buildingId) : floors.value)
const formBuildings = computed(() => form.campusId ? buildings.value.filter(b => b.campusId === form.campusId) : [])
const formFloors = computed(() => form.buildingId ? floors.value.filter(f => f.buildingId === form.buildingId) : [])

const filteredList = computed(() => {
  let l = list.value
  if (filter.floorId) l = l.filter(r => r.floorId === filter.floorId)
  if (filter.keyword) {
    const k = filter.keyword.toLowerCase()
    l = l.filter(r => r.name.toLowerCase().includes(k))
  }
  return l
})
const pagedList = computed(() => {
  const start = (page.current - 1) * page.size
  return filteredList.value.slice(start, start + page.size)
})

function getFloorName(id) { return floors.value.find(f => f.id === id)?.name || '-' }
function getBuildingNameByFloor(fid) {
  const f = floors.value.find(x => x.id === fid)
  return buildings.value.find(b => b.id === f?.buildingId)?.name || '-'
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await getRoomList()
    list.value = data
  } finally { loading.value = false }
}
async function loadAux() {
  const [{ data: cs }, { data: bs }, { data: fs }] = await Promise.all([getCampusList(), getBuildingList(), getFloorList()])
  campuses.value = cs; buildings.value = bs; floors.value = fs
}
function resetFilter() {
  Object.assign(filter, { campusId: null, buildingId: null, floorId: null, keyword: '' })
  page.current = 1
  loadList()
}
function onCampusChange() { filter.buildingId = null; filter.floorId = null }
function onBuildingChange() { filter.floorId = null }
function onFormCampusChange() { form.buildingId = null; form.floorId = null }
function onFormBuildingChange() { form.floorId = null }
function openAdd() { editing.value = false; dialogVisible.value = true }
function openEdit(row) {
  editing.value = true
  Object.assign(form, row)
  // 反推 campus/building
  const f = floors.value.find(x => x.id === row.floorId)
  if (f) {
    form.floorId = f.id
    form.buildingId = f.buildingId
    const b = buildings.value.find(x => x.id === f.buildingId)
    if (b) form.campusId = b.campusId
  }
  dialogVisible.value = true
}
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null, campusId: null, buildingId: null, floorId: null,
    name: '', capacity: 60, type: 0, hasPower: 1, hasNetwork: 1, description: ''
  })
}
async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editing.value) {
      await updateRoom(form)
      ElMessage.success('更新成功')
    } else {
      const { id, campusId, buildingId, ...payload } = form
      await addRoom(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally { submitting.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除自习室「${row.name}」？座位会一并清理。`, '提示', { type: 'warning' })
  await deleteRoom(row.id)
  ElMessage.success('已删除')
  loadList()
}
function goSeats(row) {
  router.push(`/admin/room/${row.id}/seats`)
}

onMounted(async () => {
  await loadAux()
  await loadList()
})
</script>

<style scoped>
.page-card { border-radius: 10px; }
.page-header { display: flex; align-items: center; justify-content: space-between; }
.page-title { font-size: 18px; font-weight: 600; }
.filter-bar { margin-bottom: 12px; }
.pager { margin-top: 16px; justify-content: flex-end; display: flex; }
.icon-disabled { color: #c0c4cc; }
</style>
