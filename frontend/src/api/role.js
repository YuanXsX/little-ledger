import request from './request'

// 角色管理接口
export function listRoles() {
  return request.get('/roles')
}

export function createRole(data) {
  return request.post('/roles', data)
}

// 分配模块+操作权限（覆盖式，permissionIds 为权限点 id 数组）
export function updateRolePermissions(id, permissionIds) {
  return request.put(`/roles/${id}/permissions`, permissionIds)
}

export function listPermissions() {
  return request.get('/permissions')
}
