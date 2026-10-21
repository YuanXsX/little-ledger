import request from './request'

// 库存明细台账接口
export function listInventory(params) {
  return request.get('/inventory-ledger', { params })
}

export function createInventory(data) {
  return request.post('/inventory-ledger', data)
}

export function updateInventory(id, data) {
  return request.put(`/inventory-ledger/${id}`, data)
}

export function deleteInventory(id) {
  return request.delete(`/inventory-ledger/${id}`)
}
