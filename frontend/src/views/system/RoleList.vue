<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">+ 新增角色</el-button>
        <el-button @click="load">刷新</el-button>
      </div>

      <el-table :data="roles" v-loading="loading" border>
        <el-table-column prop="roleCode" label="角色编码" width="120" />
        <el-table-column prop="roleName" label="角色名称" width="140" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="权限数" width="90" align="center">
          <template #default="{ row }">{{ row.permissions.length }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openConfig(row)">配置权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增角色 -->
    <el-dialog v-model="createVisible" title="新增角色" width="460px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="如 SALES" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="如 销售员" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 权限矩阵 -->
    <el-dialog v-model="configVisible" :title="`配置权限 - ${currentRole.roleName || ''}`" width="760px">
      <el-table :data="MODULES" border>
        <el-table-column label="模块" width="110">
          <template #default="{ row }">{{ row.name }}</template>
        </el-table-column>
        <el-table-column v-for="op in OPERATIONS" :key="op.key" :label="op.label" align="center" width="88">
          <template #default="{ row }">
            <el-checkbox v-if="perm(row.key, op.key)" v-model="checkedIds" :value="perm(row.key, op.key).id" />
            <span v-else class="na">—</span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveConfig">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listRoles, createRole, updateRolePermissions, listPermissions } from '@/api/role'

const MODULES = [
  { key: 'finance', name: '财务流水' },
  { key: 'inventory', name: '库存明细' },
  { key: 'customer', name: '客户信息' },
  { key: 'contact', name: '联系人' },
  { key: 'enterprise', name: '企业资质' },
  { key: 'category', name: '台账分类' },
  { key: 'report', name: '智能报表' },
  { key: 'user', name: '用户管理' },
  { key: 'role', name: '角色管理' }
]
const OPERATIONS = [
  { key: 'view', label: '查看' },
  { key: 'create', label: '新增' },
  { key: 'update', label: '编辑' },
  { key: 'delete', label: '删除' },
  { key: 'export', label: '导出' },
  { key: 'config', label: '配置' }
]

const roles = ref([])
const permissions = ref([])
const loading = ref(false)
const saving = ref(false)
const createVisible = ref(false)
const configVisible = ref(false)
const currentRole = ref({})
const checkedIds = ref([])
const formRef = ref(null)

const form = reactive({ roleCode: '', roleName: '', description: '' })
const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const [r, p] = await Promise.all([listRoles(), listPermissions()])
    roles.value = r.data
    permissions.value = p.data
  } finally {
    loading.value = false
  }
}

function perm(module, op) {
  return permissions.value.find((p) => p.module === module && p.operation === op) || null
}

function openCreate() {
  Object.assign(form, { roleCode: '', roleName: '', description: '' })
  createVisible.value = true
}

async function saveCreate() {
  await formRef.value.validate()
  saving.value = true
  try {
    await createRole(form)
    ElMessage.success('新增成功')
    createVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openConfig(row) {
  currentRole.value = row
  checkedIds.value = row.permissions.map((p) => p.id)
  configVisible.value = true
}

async function saveConfig() {
  saving.value = true
  try {
    await updateRolePermissions(currentRole.value.id, checkedIds.value)
    ElMessage.success('权限已保存')
    configVisible.value = false
    load()
  } finally {
    saving.value = false
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
.na {
  color: #ccc;
}
</style>
