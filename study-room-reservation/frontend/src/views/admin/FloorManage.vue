<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">📐 楼层管理</span>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增楼层</el-button>
      </div>
    </template>

    <el-form :inline="true" :model="filter" class="filter-bar">
      <el-form-item label="校区">
        <el-select v-model="filter.campusId" placeholder="全部" clearable style="width: 160px" @change="onCampusChange">
          <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="楼栋">
        <el-select v-model="filter.buildingId" placeholder="全部" clearable style="width: 180px" @change="loadList">
          <el-option v-for="b in filteredBuildings" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="filter.keyword" placeholder="按名称模糊搜索" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
        <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="pagedList" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="楼层名称" min-width="120" />
      <el-table-column label="所属楼栋" min-width="160">
        <template #default="{ row }">{{ getBuildingName(row.buildingId) }}</template>
      </el-table-column>
      <el-table-column label="所属校区" min-width="120">
        <template #default="{ row }">{{ getCampusNameByBuilding(row.buildingId) }}</template>
      </el-table-column>
      <el-table-column prop="floorNumber" label="楼层号" width="100" align="center" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="{ row }">
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
      :title="editing ? '编辑楼层' : '新增楼层'"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="校区" prop="campusId">
          <el-select v-model="form.campusId" placeholder="请选择" style="width: 100%" @change="onFormCampusChange">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请先选校区" style="width: 100%" :disabled="!form.campusId">
            <el-option v-for="b in formBuildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：3层" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="楼层号" prop="floorNumber">
          <el-input-number v-model="form.floorNumber" :min="1" :max="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { getFloorList, addFloor, updateFloor, deleteFloor, getBuildingList, getCampusList } from '../../api/admin'
import { formatTime } from '../../utils/format'

const list = ref([])
const buildings = ref([])
const campuses = ref([])
const loading = ref(false)
const filter = reactive({ campusId: null, buildingId: null, keyword: '' })
const page = reactive({ current: 1, size: 10 })

const dialogVisible = ref(false)
const editing = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, campusId: null, buildingId: null, name: '', floorNumber: 1, description: '' })
const rules = {
  campusId: [{ required: true, message: '请选择校区', trigger: 'change' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  floorNumber: [{ required: true, message: '请输入楼层号', trigger: 'blur' }]
}

const filteredBuildings = computed(() => {
  if (!filter.campusId) return buildings.value
  return buildings.value.filter(b => b.campusId === filter.campusId)
})
const formBuildings = computed(() => {
  if (!form.campusId) return []
  return buildings.value.filter(b => b.campusId === form.campusId)
})

const filteredList = computed(() => {
  let l = list.value
  if (filter.buildingId) l = l.filter(f => f.buildingId === filter.buildingId)
  if (filter.keyword) {
    const k = filter.keyword.toLowerCase()
    l = l.filter(f => f.name.toLowerCase().includes(k))
  }
  return l
})
const pagedList = computed(() => {
  const start = (page.current - 1) * page.size
  return filteredList.value.slice(start, start + page.size)
})

function getBuildingName(id) { return buildings.value.find(b => b.id === id)?.name || '-' }
function getCampusNameByBuilding(buildingId) {
  const b = buildings.value.find(x => x.id === buildingId)
  return campuses.value.find(c => c.id === b?.campusId)?.name || '-'
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await getFloorList()
    list.value = data
  } finally { loading.value = false }
}
async function loadAux() {
  const [{ data: cs }, { data: bs }] = await Promise.all([getCampusList(), getBuildingList()])
  campuses.value = cs
  buildings.value = bs
}
function resetFilter() {
  filter.campusId = null
  filter.buildingId = null
  filter.keyword = ''
  page.current = 1
  loadList()
}
function onCampusChange() { filter.buildingId = null }
function onFormCampusChange() { form.buildingId = null }
function openAdd() { editing.value = false; dialogVisible.value = true }
function openEdit(row) {
  editing.value = true
  Object.assign(form, row)
  // 反推校区
  const b = buildings.value.find(x => x.id === row.buildingId)
  if (b) form.campusId = b.campusId
  dialogVisible.value = true
}
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { id: null, campusId: null, buildingId: null, name: '', floorNumber: 1, description: '' })
}
async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editing.value) {
      await updateFloor(form)
      ElMessage.success('更新成功')
    } else {
      const { id, campusId, ...payload } = form
      await addFloor(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally { submitting.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除楼层「${row.name}」？其下自习室/座位也会被清理。`, '提示', { type: 'warning' })
  await deleteFloor(row.id)
  ElMessage.success('已删除')
  loadList()
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
</style>
