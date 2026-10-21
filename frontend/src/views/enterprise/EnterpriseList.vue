<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">+ 新增资质</el-button>
        <el-button @click="load">刷新</el-button>
        <span class="tip">资质到期前 15 天系统自动提醒</span>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="licenseNo" label="营业执照编号" width="180" />
        <el-table-column prop="businessScope" label="经营范围" show-overflow-tooltip />
        <el-table-column prop="establishDate" label="成立日期" width="120" />
        <el-table-column label="有效期截止" width="130">
          <template #default="{ row }">
            <span :class="{ expiring: isExpiring(row) }">
              {{ row.licenseExpireDate || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="licenseFile" label="资质文件" width="120">
          <template #default="{ row }">{{ row.licenseFile ? '已上传' : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑资质' : '新增资质'" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="营业执照编号" required>
          <el-input v-model="form.licenseNo" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="经营许可证">
          <el-input v-model="form.businessLicense" />
        </el-form-item>
        <el-form-item label="经营范围">
          <el-input v-model="form.businessScope" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="注册地址">
          <el-input v-model="form.registeredAddress" />
        </el-form-item>
        <el-form-item label="成立日期" required>
          <el-date-picker v-model="form.establishDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="有效期截止">
          <el-date-picker v-model="form.licenseExpireDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>

        <!-- 仅编辑时可上传扫描件 -->
        <el-form-item v-if="form.id" label="资质扫描件">
          <el-upload :auto-upload="false" :limit="1" :on-change="onFileChange">
            <el-button>选择文件(JPG/PDF)</el-button>
          </el-upload>
        </el-form-item>

        <!-- 编辑需填写更新日志 -->
        <template v-if="form.id">
          <el-form-item label="更新人" required>
            <el-input v-model="operator" placeholder="如：张三" />
          </el-form-item>
          <el-form-item label="更新原因">
            <el-input v-model="reason" placeholder="如：更正经营范围" />
          </el-form-item>
        </template>
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
import { ElMessage } from 'element-plus'
import {
  listEnterprise,
  createEnterprise,
  updateEnterprise,
  uploadEnterpriseFile
} from '@/api/enterprise'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const operator = ref('')
const reason = ref('')
const pendingFile = ref(null)

const form = reactive({
  id: null,
  licenseNo: '',
  businessLicense: '',
  businessScope: '',
  registeredAddress: '',
  establishDate: '',
  licenseExpireDate: ''
})

async function load() {
  loading.value = true
  try {
    const res = await listEnterprise({ page: page.value, size: size.value })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function isExpiring(row) {
  if (!row.licenseExpireDate) return false
  const days = (new Date(row.licenseExpireDate) - Date.now()) / 86400000
  return days >= 0 && days <= 15
}

function openCreate() {
  Object.assign(form, {
    id: null, licenseNo: '', businessLicense: '', businessScope: '',
    registeredAddress: '', establishDate: '', licenseExpireDate: ''
  })
  operator.value = ''
  reason.value = ''
  pendingFile.value = null
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  operator.value = ''
  reason.value = ''
  pendingFile.value = null
  dialogVisible.value = true
}

function onFileChange(file) {
  pendingFile.value = file.raw
}

async function save() {
  saving.value = true
  try {
    if (form.id) {
      if (!operator.value) {
        ElMessage.warning('请填写更新人')
        return
      }
      await updateEnterprise(form.id, form, operator.value, reason.value)
      if (pendingFile.value) {
        await uploadEnterpriseFile(form.id, pendingFile.value)
      }
      ElMessage.success('更新成功')
    } else {
      await createEnterprise(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
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
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.tip {
  color: #999;
  font-size: 12px;
}
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
.expiring {
  color: #e6a23c;
  font-weight: bold;
}
</style>
