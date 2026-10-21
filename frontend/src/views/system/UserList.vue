<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreate">+ 新增用户</el-button>
        <el-button @click="load">刷新</el-button>
      </div>

      <el-table :data="users" v-loading="loading" border>
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column label="角色">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r.id" size="small" class="role-tag">{{ r.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : 'success'" size="small">
              {{ row.status === 0 ? '已锁定' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增用户" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="分配角色">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
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
import { ElMessage } from 'element-plus'
import { listUsers, createUser } from '@/api/user'
import { listRoles } from '@/api/role'

const users = ref([])
const roles = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({ username: '', password: '', realName: '', phone: '', email: '', roleIds: [] })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const [u, r] = await Promise.all([listUsers(), listRoles()])
    users.value = u.data
    roles.value = r.data
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { username: '', password: '', realName: '', phone: '', email: '', roleIds: [] })
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    await createUser(form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
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
.role-tag {
  margin-right: 4px;
}
</style>
