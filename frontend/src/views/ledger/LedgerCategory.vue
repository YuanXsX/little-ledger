<template>
  <div class="category-page">
    <el-row :gutter="16">
      <!-- 左侧：台账分类列表 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>台账分类</span>
              <el-button type="primary" size="small" @click="openCreateCategory">+ 新增分类</el-button>
            </div>
          </template>

          <el-table :data="categories" v-loading="loading" highlight-current-row
            @current-change="onSelectCategory">
            <el-table-column prop="name" label="分类名称" />
            <el-table-column label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.preset ? 'info' : 'success'" size="small">
                  {{ row.preset ? '预设' : '自定义' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ row }">
                <el-button v-if="!row.preset" link type="danger" @click.stop="onDeleteCategory(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：自定义字段 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>自定义字段{{ current ? '（' + current.name + '）' : '' }}</span>
              <el-button type="primary" size="small" :disabled="!current" @click="openAddField">+ 新增字段</el-button>
            </div>
          </template>

          <el-empty v-if="!current" description="请选择左侧分类查看字段" />
          <el-table v-else :data="fields" border>
            <el-table-column prop="fieldKey" label="字段标识" width="140" />
            <el-table-column prop="fieldName" label="字段名称" />
            <el-table-column prop="fieldType" label="类型" width="90" />
            <el-table-column label="必填" width="70">
              <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ row }">
                <el-button link type="danger" @click="onDeleteField(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增分类弹窗 -->
    <el-dialog v-model="categoryDialog" title="新增台账分类" width="460px">
      <el-form :model="categoryForm" label-width="90px">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="如：供应商台账、销售订单台账" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增字段弹窗 -->
    <el-dialog v-model="fieldDialog" title="新增自定义字段" width="460px">
      <el-form :model="fieldForm" label-width="90px">
        <el-form-item label="字段标识" required>
          <el-input v-model="fieldForm.fieldKey" placeholder="如：taxRate" />
        </el-form-item>
        <el-form-item label="字段名称" required>
          <el-input v-model="fieldForm.fieldName" placeholder="如：增值税税率、物流单号" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="fieldForm.fieldType" style="width: 100%">
            <el-option label="文本" value="string" />
            <el-option label="数字" value="number" />
            <el-option label="日期" value="date" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否必填">
          <el-switch v-model="fieldForm.required" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fieldDialog = false">取消</el-button>
        <el-button type="primary" @click="saveField">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listCategories, createCategory, deleteCategory,
  listFields, addField, deleteField
} from '@/api/ledger'

const categories = ref([])
const fields = ref([])
const current = ref(null)
const loading = ref(false)
const categoryDialog = ref(false)
const fieldDialog = ref(false)

const categoryForm = reactive({ name: '', description: '' })
const fieldForm = reactive({ fieldKey: '', fieldName: '', fieldType: 'string', required: false })

async function loadCategories() {
  loading.value = true
  try {
    const res = await listCategories()
    categories.value = res.data
  } finally {
    loading.value = false
  }
}

async function onSelectCategory(row) {
  current.value = row
  if (!row) { fields.value = []; return }
  const res = await listFields(row.id)
  fields.value = res.data
}

function openCreateCategory() {
  categoryForm.name = ''
  categoryForm.description = ''
  categoryDialog.value = true
}

async function saveCategory() {
  if (!categoryForm.name) {
    ElMessage.warning('请填写分类名称')
    return
  }
  await createCategory(categoryForm)
  ElMessage.success('新增成功')
  categoryDialog.value = false
  loadCategories()
}

async function onDeleteCategory(row) {
  try {
    await ElMessageBox.confirm(`确认删除自定义分类「${row.name}」？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  if (current.value?.id === row.id) { current.value = null; fields.value = [] }
  loadCategories()
}

function openAddField() {
  fieldForm.fieldKey = ''
  fieldForm.fieldName = ''
  fieldForm.fieldType = 'string'
  fieldForm.required = false
  fieldDialog.value = true
}

async function saveField() {
  if (!fieldForm.fieldKey || !fieldForm.fieldName) {
    ElMessage.warning('请填写字段标识和名称')
    return
  }
  await addField(current.value.id, fieldForm)
  ElMessage.success('新增成功')
  fieldDialog.value = false
  onSelectCategory(current.value)
}

async function onDeleteField(row) {
  try {
    await ElMessageBox.confirm(`确认删除字段「${row.fieldName}」？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await deleteField(row.id)
  ElMessage.success('删除成功')
  onSelectCategory(current.value)
}

onMounted(loadCategories)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
