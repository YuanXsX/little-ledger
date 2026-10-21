import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一 axios 实例：baseURL 走 Vite 代理，开发时 /api -> localhost:8080
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：附加登录 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理后端返回结构与错误提示
request.interceptors.response.use(
  (response) => {
    // 文件下载（导出）等 blob 响应：返回完整响应，由调用方读取 data 与 headers
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    // 未登录 / token 失效：清除凭证并跳转登录
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      ElMessage.error(res.message || '登录已失效，请重新登录')
      return Promise.reject(new Error(res.message || '未登录'))
    }
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
