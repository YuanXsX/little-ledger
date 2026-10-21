import request from './request'

// 台账分类与自定义字段接口
export function listCategories() {
  return request.get('/ledger-categories')
}

export function createCategory(data) {
  return request.post('/ledger-categories', data)
}

export function deleteCategory(id) {
  return request.delete(`/ledger-categories/${id}`)
}

export function listFields(categoryId) {
  return request.get(`/ledger-categories/${categoryId}/fields`)
}

export function addField(categoryId, data) {
  return request.post(`/ledger-categories/${categoryId}/fields`, data)
}

export function deleteField(fieldId) {
  return request.delete(`/ledger-categories/fields/${fieldId}`)
}
