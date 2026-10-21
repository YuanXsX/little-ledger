import request from './request'

// 数据备份与恢复接口
export function listBackups() {
  return request.get('/backups')
}

export function triggerBackup() {
  return request.post('/backups')
}

export function getBackupConfig() {
  return request.get('/backups/config')
}

export function updateBackupConfig(params) {
  return request.put('/backups/config', null, { params })
}

export function listRestores() {
  return request.get('/backups/restores')
}

export function restoreBackup(id, operator) {
  return request.post(`/backups/restore/${id}`, null, { params: { operator } })
}

export function restoreLatest(operator) {
  return request.post('/backups/restore/latest', null, { params: { operator } })
}
