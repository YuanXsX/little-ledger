import request from './request'

// 联系人信息接口
export function listContacts(params) {
  return request.get('/contacts', { params })
}

export function createContact(data) {
  return request.post('/contacts', data)
}

export function updateContact(id, data, operator, reason) {
  return request.put(`/contacts/${id}`, data, {
    params: { operator, reason }
  })
}

export function deleteContact(id, operator) {
  return request.delete(`/contacts/${id}`, { params: { operator } })
}
