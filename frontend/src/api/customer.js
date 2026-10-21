import request from './request'

// 客户信息台账接口
export function listCustomer(params) {
  return request.get('/customer-ledger', { params })
}

export function createCustomer(data) {
  return request.post('/customer-ledger', data)
}

export function updateCustomer(id, data) {
  return request.put(`/customer-ledger/${id}`, data)
}

export function deleteCustomer(id) {
  return request.delete(`/customer-ledger/${id}`)
}
