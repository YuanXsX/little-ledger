<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">+ 录入客户</el-button>
        <el-button @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="customerName" label="客户名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="90" />
        <el-table-column label="手机号(脱敏)" width="130">
          <template #default="{ row }">{{ maskPhone(row.phone) }}</template>
        </el-table-column>
        <el-table-column prop="industry" label="所属行业" width="110" />
        <el-table-column prop="coopStartDate" label="合作起始" width="110" />
        <el-table-column prop="totalAmount" label="累计金额(元)" width="120" align="right" />
        <el-table-column prop="receivable" label="应收账款(元)" width="120" align="right" />
        <el-table-column prop="coopLevel" label="等级" width="70" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑客户' : '录入客户'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11 位数字" maxlength="11" />
        </el-form-item>
        <el-form-item label="所属行业">
          <el-input v-model="form.industry" />
        </el-form-item>
        <el-form-item label="合作起始日期">
          <el-date-picker v-model="form.coopStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="客户地址">
          <el-input v-model="form.address" placeholder="省/市" />
        </el-form-item>
        <el-form-item label="合作等级">
          <el-select v-model="form.coopLevel" style="width: 100%">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { listCustomer, createCustomer, updateCustomer, deleteCustomer } from '@/api/customer'
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
  id: null, customerName: '', contactPerson: '', phone: '', industry: '',
  coopStartDate: '', address: '', coopLevel: '', remark: ''
})

const rules = {
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  phone: [
    { validator: (_r, value, cb) => {
        if (!value) return cb()
        if (!/^\d{11}$/.test(value)) return cb(new Error('手机号需为 11 位数字'))
        cb()
      }, trigger: 'blur' }
  ]
}

function maskPhone(phone) {
  if (!phone) return '-'
  return phone.length >= 7 ? phone.slice(0, 3) + '****' + phone.slice(-4) : phone
}

async function load() {
  loading.value = true
  try {
    const res = await listCustomer({ page: page.value, size: size.value })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null, customerName: '', contactPerson: '', phone: '', industry: '',
    coopStartDate: '', address: '', coopLevel: '', remark: ''
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
      await updateCustomer(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createCustomer(form)
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
    await ElMessageBox.confirm(`确认删除客户「${row.customerName}」？`, '删除确认', { type: 'warning' })
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
  await deleteCustomer(row.id)
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
</style>
