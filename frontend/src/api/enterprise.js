import request from './request'

// 企业资质信息接口
export function listEnterprise(params) {
  return request.get('/enterprise', { params })
}

export function getEnterprise(id) {
  return request.get(`/enterprise/${id}`)
}

export function createEnterprise(data) {
  return request.post('/enterprise', data)
}

export function updateEnterprise(id, data, operator, reason) {
  return request.put(`/enterprise/${id}`, data, {
    params: { operator, reason }
  })
}

export function uploadEnterpriseFile(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/enterprise/${id}/file`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function listExpiring(days = 15) {
  return request.get('/enterprise/expiring', { params: { days } })
}
