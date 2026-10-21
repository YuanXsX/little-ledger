<template>
  <div>
    <!-- 预警规则 -->
    <el-card class="block-card">
      <div class="toolbar">
        <span class="section-title">预警规则</span>
        <el-button type="primary" @click="openRuleDialog">+ 新增规则</el-button>
      </div>
      <el-table :data="rules" border>
        <el-table-column prop="ruleName" label="规则名称" width="140" />
        <el-table-column prop="ruleType" label="预警类型" width="110" />
        <el-table-column prop="target" label="涉及对象" width="110" />
        <el-table-column prop="threshold" label="阈值" width="90" />
        <el-table-column prop="notifyWay" label="通知方式" width="130" />
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 预警记录 -->
    <el-card>
      <div class="toolbar">
        <span class="section-title">预警记录</span>
        <el-button type="warning" @click="doCheck">检查库存预警</el-button>
        <el-select v-model="status" style="width: 130px" @change="loadAlerts">
          <el-option label="全部" value="" />
          <el-option label="未处理" value="UNRESOLVED" />
          <el-option label="已解决" value="RESOLVED" />
        </el-select>
        <el-button @click="load">刷新</el-button>
      </div>
      <el-table :data="alerts" v-loading="loading" border>
        <el-table-column prop="alertType" label="预警类型" width="110" />
        <el-table-column prop="target" label="涉及对象" width="140" />
        <el-table-column prop="currentVal" label="当前数值" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'UNRESOLVED' ? 'danger' : 'success'" size="small">
              {{ row.status === 'UNRESOLVED' ? '未处理' : '已解决' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'UNRESOLVED'" link type="primary" @click="doResolve(row)">
              标记解决
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增规则弹窗 -->
    <el-dialog v-model="ruleDialogVisible" title="新增预警规则" width="480px">
      <el-form :model="ruleForm" :rules="ruleRules" ref="ruleFormRef" label-width="90px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" />
        </el-form-item>
        <el-form-item label="预警类型" prop="ruleType">
          <el-input v-model="ruleForm.ruleType" placeholder="如 库存不足" />
        </el-form-item>
        <el-form-item label="涉及对象">
          <el-input v-model="ruleForm.target" placeholder="留空表示全部" />
        </el-form-item>
        <el-form-item label="阈值">
          <el-input v-model="ruleForm.threshold" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="notifyWays">
            <el-checkbox v-for="w in ['消息', '短信', '邮件']" :key="w" :value="w">{{ w }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listAlerts, resolveAlert, checkStockAlerts, listRules, createRule } from '@/api/alert'

const rules = ref([])
const alerts = ref([])
const loading = ref(false)
const saving = ref(false)
const status = ref('')
const ruleDialogVisible = ref(false)
const ruleFormRef = ref(null)

const ruleForm = reactive({ ruleName: '', ruleType: '', target: '', threshold: '' })
const notifyWays = ref(['消息'])
const ruleRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请输入预警类型', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const [r, a] = await Promise.all([listRules(), listAlerts({ status: status.value || undefined })])
    rules.value = r.data
    alerts.value = a.data
  } finally {
    loading.value = false
  }
}

function loadAlerts() {
  load()
}

function fmt(t) {
  return t ? t.replace('T', ' ').slice(0, 19) : '-'
}

function openRuleDialog() {
  Object.assign(ruleForm, { ruleName: '', ruleType: '', target: '', threshold: '' })
  notifyWays.value = ['消息']
  ruleDialogVisible.value = true
}

async function saveRule() {
  await ruleFormRef.value.validate()
  saving.value = true
  try {
    await createRule({ ...ruleForm, notifyWay: notifyWays.value.join(','), enabled: true })
    ElMessage.success('规则已新增')
    ruleDialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function doCheck() {
  saving.value = true
  try {
    const res = await checkStockAlerts()
    ElMessage.success(`检查完成，新增 ${res.data.createdAlerts} 条预警`)
    load()
  } finally {
    saving.value = false
  }
}

async function doResolve(row) {
  await resolveAlert(row.id)
  ElMessage.success('已标记解决')
  load()
}

onMounted(load)
</script>

<style scoped>
.block-card {
  margin-bottom: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.section-title {
  font-weight: bold;
  color: #303133;
}
</style>
