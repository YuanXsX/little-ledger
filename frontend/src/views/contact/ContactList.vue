<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-select v-model="filterType" placeholder="联系人类型" clearable style="width: 160px" @change="load">
          <el-option label="内部" value="内部" />
          <el-option label="客户" value="客户" />
          <el-option label="供应商" value="供应商" />
        </el-select>
        <el-button @click="load">查询</el-button>
        <el-button type="primary" @click="openCreate">+ 新增联系人</el-button>
        <span class="tip">手机号/邮箱已脱敏显示</span>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="contactType" label="类型" width="90" />
        <el-table-column prop="phone" label="手机号(脱敏)" width="140" />
        <el-table-column prop="email" label="邮箱(脱敏)" show-overflow-tooltip />
        <el-table-column prop="company" label="所属企业" show-overflow-tooltip />
        <el-table-column prop="position" label="职务" width="100" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑联系人' : '新增联系人'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.contactType" style="width: 100%">
            <el-option label="内部" value="内部" />
            <el-option label="客户" value="客户" />
            <el-option label="供应商" value="供应商" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="11 位数字" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="如 name@company.com" />
        </el-form-item>
        <el-form-item label="所属企业">
          <el-input v-model="form.company" />
        </el-form-item>
        <el-form-item label="职务">
          <el-input v-model="form.position" />
        </el-form-item>

        <template v-if="form.id">
          <el-form-item label="更新人" required>
            <el-input v-model="operator" />
          </el-form-item>
          <el-form-item label="更新原因">
            <el-input v-model="reason" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { listContacts, createContact, updateContact, deleteContact } from '@/api/contact'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const filterType = ref('')
const operator = ref('')
const reason = ref('')

const form = reactive({
  id: null,
  name: '',
  contactType: '',
  phone: '',
  email: '',
  company: '',
  position: ''
})

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterType.value) params.contactType = filterType.value
    const res = await listContacts(params)
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, {
    id: null, name: '', contactType: '', phone: '', email: '', company: '', position: ''
  })
  operator.value = ''
  reason.value = ''
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  operator.value = ''
  reason.value = ''
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (form.id) {
      if (!operator.value) {
        ElMessage.warning('请填写更新人')
        return
      }
      await updateContact(form.id, form, operator.value, reason.value)
      ElMessage.success('更新成功')
    } else {
      await createContact(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除联系人「${row.name}」？此操作需二次验证。`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  // TODO(权限模块)：敏感操作二次验证(输入登录密码)
  await deleteContact(row.id, '当前操作人')
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
</style>
