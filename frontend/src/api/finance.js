import request from './request'

// 财务流水台账接口
export function listFinance(params) {
  return request.get('/finance-ledger', { params })
}

export function createFinance(data) {
  return request.post('/finance-ledger', data)
}

export function updateFinance(id, data) {
  return request.put(`/finance-ledger/${id}`, data)
}

export function deleteFinance(id) {
  return request.delete(`/finance-ledger/${id}`)
}

export function importFinance(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/import/finance', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
