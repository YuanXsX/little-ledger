<template>
  <div>
    <el-card class="config-card">
      <div class="config-row">
        <span class="config-label">备份策略</span>
        <el-select v-model="configForm.frequency" style="width: 140px">
          <el-option label="每天" value="daily" />
          <el-option label="每小时" value="hourly" />
          <el-option label="每周" value="weekly" />
        </el-select>
        <el-select v-model="configForm.storage" style="width: 150px">
          <el-option label="本地服务器" value="local" />
          <el-option label="云端(OSS)" value="oss" />
          <el-option label="双存储" value="dual" />
        </el-select>
        <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
      </div>
    </el-card>

    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="doBackup">立即备份</el-button>
        <el-button type="warning" @click="doRestoreLatest">快速恢复(最近备份)</el-button>
        <el-button @click="load">刷新</el-button>
      </div>

      <el-table :data="backups" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.backupType === 'auto' ? '自动' : '手动' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storage" label="存储" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试" width="70" />
        <el-table-column label="备份时间" width="170">
          <template #default="{ row }">{{ fmt(row.backupTime) }}</template>
        </el-table-column>
        <el-table-column prop="filePath" label="文件路径" show-overflow-tooltip />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status !== 'success'" @click="doRestore(row)">
              定点恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <div class="section-title">恢复日志</div>
      <el-table :data="restores" border>
        <el-table-column prop="operator" label="恢复人" width="130" />
        <el-table-column prop="backupId" label="备份点" width="90" />
        <el-table-column label="备份时间" width="180">
          <template #default="{ row }">{{ fmt(row.backupTime) }}</template>
        </el-table-column>
        <el-table-column label="恢复时间" width="180">
          <template #default="{ row }">{{ fmt(row.restoredAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listBackups, triggerBackup, getBackupConfig, updateBackupConfig,
  listRestores, restoreBackup, restoreLatest
} from '@/api/backup'

const backups = ref([])
const restores = ref([])
const loading = ref(false)
const saving = ref(false)
const configForm = reactive({ frequency: 'daily', storage: 'local' })

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')

async function load() {
  loading.value = true
  try {
    const [b, r, c] = await Promise.all([listBackups(), listRestores(), getBackupConfig()])
    backups.value = b.data
    restores.value = r.data
    configForm.frequency = c.data.frequency
    configForm.storage = c.data.storage
  } finally {
    loading.value = false
  }
}

function fmt(t) {
  return t ? t.replace('T', ' ').slice(0, 19) : '-'
}

async function saveConfig() {
  saving.value = true
  try {
    await updateBackupConfig({ frequency: configForm.frequency, storage: configForm.storage })
    ElMessage.success('配置已保存')
  } finally {
    saving.value = false
  }
}

async function doBackup() {
  saving.value = true
  try {
    const res = await triggerBackup()
    const d = res.data
    if (d.status === 'success') {
      ElMessage.success('备份成功')
    } else {
      ElMessage.error(`备份失败：${d.errorMsg || '未知错误'}`)
    }
    load()
  } finally {
    saving.value = false
  }
}

async function doRestoreLatest() {
  try {
    await ElMessageBox.confirm('快速恢复将覆盖当前数据，是否确认？', '恢复确认', { type: 'warning' })
  } catch {
    return
  }
  await restoreLatest(currentUser.username)
  ElMessage.success('恢复成功')
  load()
}

async function doRestore(row) {
  try {
    await ElMessageBox.confirm(
      `恢复到备份点 #${row.id}（${fmt(row.backupTime)}）将覆盖当前数据，是否确认？`,
      '恢复确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  await restoreBackup(row.id, currentUser.username)
  ElMessage.success('恢复成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.config-card {
  margin-bottom: 16px;
}
.config-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.config-label {
  font-weight: bold;
  color: #303133;
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.section-title {
  font-weight: bold;
  margin-bottom: 12px;
}
</style>
