import request from './request'

// 智能报表接口
export function getFinanceReport(params) {
  return request.get('/reports/finance', { params })
}

export function getInventoryReport(params) {
  return request.get('/reports/inventory', { params })
}

export function getCustomerReport(params) {
  return request.get('/reports/customer', { params })
}

export function getReportOverview() {
  return request.get('/reports/overview')
}

// 导出报表（blob），返回完整 axios 响应，文件名从 Content-Disposition 解析
export function exportReport(params) {
  return request.get('/reports/export', { params, responseType: 'blob' })
}
