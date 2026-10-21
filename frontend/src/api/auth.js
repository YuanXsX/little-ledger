import request from './request'

// 认证接口
export function login(data) {
  return request.post('/auth/login', data)
}

// 敏感操作二次验密
export function verifyPassword(data) {
  return request.post('/auth/verify', data)
}

export function getMe() {
  return request.get('/auth/me')
}
