<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">🏢 楼栋管理</span>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增楼栋</el-button>
      </div>
    </template>

    <el-form :inline="true" :model="filter" class="filter-bar">
      <el-form-item label="校区">
        <el-select v-model="filter.campusId" placeholder="全部" clearable style="width: 180px" @change="loadList">
          <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="filter.keyword" placeholder="按名称模糊搜索" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
        <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="pagedList" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="楼栋名称" min-width="160" />
      <el-table-column label="所属校区" min-width="140">
        <template #default="{ row }">{{ getCampusName(row.campusId) }}</template>
      </el-table-column>
      <el-table-column prop="floorCount" label="楼层数" width="100" align="center" />
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
      :title="editing ? '编辑楼栋' : '新增楼栋'"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="校区" prop="campusId">
          <el-select v-model="form.campusId" placeholder="请选择" style="width: 100%">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：第一教学楼" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="楼层数" prop="floorCount">
          <el-input-number v-model="form.floorCount" :min="1" :max="50" style="width: 100%" />
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
import { getBuildingList, addBuilding, updateBuilding, deleteBuilding, getCampusList } from '../../api/admin'
import { formatTime } from '../../utils/format'

const list = ref([])
const campuses = ref([])
const loading = ref(false)
const filter = reactive({ campusId: null, keyword: '' })
const page = reactive({ current: 1, size: 10 })

const dialogVisible = ref(false)
const editing = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, campusId: null, name: '', floorCount: 1, description: '' })
const rules = {
  campusId: [{ required: true, message: '请选择校区', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  floorCount: [{ required: true, message: '请输入楼层数', trigger: 'blur' }]
}

const filteredList = computed(() => {
  let l = list.value
  if (filter.campusId) l = l.filter(b => b.campusId === filter.campusId)
  if (filter.keyword) {
    const k = filter.keyword.toLowerCase()
    l = l.filter(b => b.name.toLowerCase().includes(k))
  }
  return l
})
const pagedList = computed(() => {
  const start = (page.current - 1) * page.size
  return filteredList.value.slice(start, start + page.size)
})

function getCampusName(id) {
  return campuses.value.find(c => c.id === id)?.name || '-'
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await getBuildingList()
    list.value = data.records || data
  } finally {
    loading.value = false
  }
}
async function loadCampuses() {
  const { data } = await getCampusList()
  campuses.value = data.records || data
}
function resetFilter() {
  filter.campusId = null
  filter.keyword = ''
  page.current = 1
  loadList()
}
function openAdd() {
  editing.value = false
  dialogVisible.value = true
}
function openEdit(row) {
  editing.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { id: null, campusId: null, name: '',  description: '' })
}
async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editing.value) {
      await updateBuilding(form)
      ElMessage.success('更新成功')
    } else {
      const { id, ...payload } = form
      await addBuilding(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除楼栋「${row.name}」？其下楼层/自习室/座位也会被清理。`, '提示', { type: 'warning' })
  await deleteBuilding(row.id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(async () => {
  await loadCampuses()
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
