<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">🏫 校区管理</span>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增校区</el-button>
      </div>
    </template>

    <!-- 筛选 -->
    <el-form :inline="true" :model="filter" class="filter-bar">
      <el-form-item label="名称">
        <el-input v-model="filter.keyword" placeholder="按名称模糊搜索" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
        <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="pagedList" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="校区名称" min-width="140" />
      <el-table-column prop="address" label="地址" min-width="240" show-overflow-tooltip />
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑校区' : '新增校区'"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：主校区" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="详细地址" maxlength="60" />
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
import { getCampusList, addCampus, updateCampus, deleteCampus } from '../../api/admin'
import { formatTime } from '../../utils/format'

const list = ref([])
const loading = ref(false)
const filter = reactive({ keyword: '' })
const page = reactive({ current: 1, size: 10 })

const dialogVisible = ref(false)
const editing = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, name: '', address: '', description: '' })
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const filteredList = computed(() => {
  if (!filter.keyword) return list.value
  const k = filter.keyword.toLowerCase()
  return list.value.filter(c => c.name.toLowerCase().includes(k) || (c.address || '').toLowerCase().includes(k))
})
const pagedList = computed(() => {
  const start = (page.current - 1) * page.size
  return filteredList.value.slice(start, start + page.size)
})

async function loadList() {
  loading.value = true
  try {
    const { data } = await getCampusList()
    list.value = data
  } finally {
    loading.value = false
  }
}
function resetFilter() {
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
  Object.assign(form, { id: null, name: '', address: '', description: '' })
}
async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editing.value) {
      await updateCampus(form)
      ElMessage.success('更新成功')
    } else {
      await addCampus({ name: form.name, address: form.address, description: form.description })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除校区「${row.name}」？相关楼栋/楼层/自习室/座位也会被清理。`, '提示', { type: 'warning' })
  await deleteCampus(row.id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.page-card { border-radius: 10px; }
.page-header { display: flex; align-items: center; justify-content: space-between; }
.page-title { font-size: 18px; font-weight: 600; }
.filter-bar { margin-bottom: 12px; }
.pager { margin-top: 16px; justify-content: flex-end; display: flex; }
</style>
