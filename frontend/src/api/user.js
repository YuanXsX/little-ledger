import request from './request'

// 用户管理接口
export function listUsers() {
  return request.get('/users')
}

export function createUser(data) {
  return request.post('/users', data)
}
