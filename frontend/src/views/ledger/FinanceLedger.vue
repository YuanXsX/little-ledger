<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">+ 录入流水</el-button>
        <el-button @click="openImport">批量导入</el-button>
        <el-button @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="voucherNo" label="凭证号" width="140" />
        <el-table-column prop="flowType" label="收支类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.flowType === '收入' ? 'success' : 'danger'" size="small">{{ row.flowType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120" align="right" />
        <el-table-column prop="occurDate" label="发生日期" width="120" />
        <el-table-column prop="payMethod" label="支付方式" width="110" />
        <el-table-column prop="relatedName" label="关联对象" show-overflow-tooltip />
        <el-table-column prop="createdBy" label="录入人" width="90" />
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

    <!-- 录入/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑流水' : '录入流水'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="凭证号" prop="voucherNo">
          <el-input v-model="form.voucherNo" />
        </el-form-item>
        <el-form-item label="收支类型" prop="flowType">
          <el-select v-model="form.flowType" style="width: 100%">
            <el-option label="收入" value="收入" />
            <el-option label="支出" value="支出" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额(元)" prop="amount">
          <el-input-number v-model="form.amount" :precision="2" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发生日期" prop="occurDate">
          <el-date-picker v-model="form.occurDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="form.payMethod" style="width: 100%">
            <el-option label="现金" value="现金" />
            <el-option label="银行转账" value="银行转账" />
            <el-option label="微信" value="微信" />
            <el-option label="支付宝" value="支付宝" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联对象类型">
          <el-select v-model="form.relatedType" clearable style="width: 100%">
            <el-option label="客户" value="客户" />
            <el-option label="供应商" value="供应商" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联对象名称">
          <el-input v-model="form.relatedName" />
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

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="importVisible" title="批量导入财务流水" width="520px">
      <el-upload :auto-upload="false" :limit="1" accept=".xlsx,.xls" :on-change="onFileChange">
        <el-button type="primary">选择 Excel 文件</el-button>
        <template #tip>
          <div class="tip">列顺序：凭证号 / 收支类型 / 金额 / 发生日期 / 支付方式 / 关联对象类型 / 关联对象名称 / 备注 / 录入人</div>
        </template>
      </el-upload>
      <div class="import-actions">
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </div>
      <div v-if="importResult" class="import-result">
        <el-alert :type="importResult.failed === 0 ? 'success' : 'warning'" :closable="false">
          共 {{ importResult.total }} 行，成功 {{ importResult.success }}，失败 {{ importResult.failed }}
        </el-alert>
        <ul v-if="importResult.errors.length" class="error-list">
          <li v-for="(e, i) in importResult.errors" :key="i">第 {{ e.row }} 行：{{ e.message }}</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFinance, createFinance, updateFinance, deleteFinance, importFinance } from '@/api/finance'
import { verifyPassword } from '@/api/auth'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const importVisible = ref(false)
const importing = ref(false)
const formRef = ref(null)
const pendingFile = ref(null)
const importResult = ref(null)

const form = reactive({
  id: null, voucherNo: '', flowType: '', amount: null, occurDate: '',
  payMethod: '', relatedType: '', relatedName: '', remark: '', createdBy: ''
})

const rules = {
  voucherNo: [{ required: true, message: '请输入凭证号', trigger: 'blur' }],
  flowType: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  occurDate: [{ required: true, message: '请选择发生日期', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const res = await listFinance({ page: page.value, size: size.value })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null, voucherNo: '', flowType: '', amount: null, occurDate: '',
    payMethod: '', relatedType: '', relatedName: '', remark: '', createdBy: ''
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
      await updateFinance(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createFinance(form)
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
    await ElMessageBox.confirm(`确认删除凭证「${row.voucherNo}」？`, '删除确认', { type: 'warning' })
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
  await deleteFinance(row.id)
  ElMessage.success('删除成功')
  load()
}

function onPageChange(p) {
  page.value = p
  load()
}

function openImport() {
  importResult.value = null
  pendingFile.value = null
  importVisible.value = true
}

function onFileChange(file) {
  pendingFile.value = file.raw
}

async function doImport() {
  if (!pendingFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  importing.value = true
  try {
    const res = await importFinance(pendingFile.value)
    importResult.value = res.data
    ElMessage.success('导入完成')
    load()
  } finally {
    importing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.tip {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
.import-actions {
  margin-top: 12px;
}
.import-result {
  margin-top: 12px;
}
.error-list {
  margin: 8px 0 0;
  padding-left: 20px;
  color: #f56c6c;
  font-size: 12px;
  max-height: 160px;
  overflow: auto;
}
</style>
