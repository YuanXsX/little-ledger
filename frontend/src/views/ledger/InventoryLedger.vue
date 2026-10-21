<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">+ 录入商品</el-button>
        <el-button @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="sku" label="SKU" width="120" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="spec" label="规格型号" width="110" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column label="库存数量" width="100" align="right">
          <template #default="{ row }">
            <span :class="{ low: row.stockQty < row.minStock }">{{ row.stockQty }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低阈值" width="90" align="right" />
        <el-table-column prop="supplier" label="供应商" show-overflow-tooltip />
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 'HIGH'" type="warning" size="small">优先补货</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" layout="total, prev, pager, next"
        :total="total" :page-size="size" :current-page="page" @current-change="onPageChange" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '录入商品'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="商品SKU" prop="sku">
          <el-input v-model="form.sku" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.spec" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如：片、个、箱" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQty">
          <el-input-number v-model="form.stockQty" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最低库存阈值" prop="minStock">
          <el-input-number v-model="form.minStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplier" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="录入人">
          <el-input v-model="form.createdBy" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listInventory, createInventory, updateInventory, deleteInventory } from '@/api/inventory'
import { verifyPassword } from '@/api/auth'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null, sku: '', productName: '', spec: '', unit: '',
  stockQty: 0, minStock: 0, supplier: '', remark: '', createdBy: ''
})

const rules = {
  sku: [{ required: true, message: '请输入商品SKU', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  stockQty: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
  minStock: [{ required: true, message: '请输入最低库存阈值', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await listInventory({ page: page.value, size: size.value })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null, sku: '', productName: '', spec: '', unit: '',
    stockQty: 0, minStock: 0, supplier: '', remark: '', createdBy: ''
  })
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateInventory(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createInventory(form)
      ElMessage.success('录入成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除商品「${row.productName}」？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  // 敏感操作二次验密
  let password
  try {
    const res = await ElMessageBox.prompt('删除台账为敏感操作，请输入登录密码二次验证', '二次验证', {
      inputType: 'password',
      inputValidator: (v) => (v ? true : '请输入密码')
    })
    password = res.value
  } catch {
    return
  }
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  await verifyPassword({ username: user.username, password })
  await deleteInventory(row.id)
  ElMessage.success('删除成功')
  load()
}

function onPageChange(p) {
  page.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
.low {
  color: #e6a23c;
  font-weight: bold;
}
</style>
