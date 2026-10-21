import request from './request'

// 异常预警接口
export function listAlerts(params) {
  return request.get('/alerts', { params })
}

export function resolveAlert(id) {
  return request.put(`/alerts/${id}/resolve`)
}

export function checkStockAlerts() {
  return request.post('/alerts/check')
}

export function listRules() {
  return request.get('/alert-rules')
}

export function createRule(data) {
  return request.post('/alert-rules', data)
}

export function listMessages() {
  return request.get('/messages')
}
