<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">小微企业数字化台账管理系统</h2>
      <el-form :model="form" label-width="0" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            @keyup.enter="onLogin"
          >
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="onLogin">
          登 录
        </el-button>
        <div class="tips">演示账号：admin / admin123（企业负责人）</div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function onLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f3a5f 0%, #2d5f8a 100%);
}
.login-card {
  width: 400px;
  padding: 20px;
}
.title {
  text-align: center;
  font-size: 20px;
  margin-bottom: 24px;
  color: #1f3a5f;
}
.login-btn {
  width: 100%;
}
.tips {
  margin-top: 12px;
  text-align: center;
  color: #999;
  font-size: 12px;
}
</style>
