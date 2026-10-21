<template>
  <div>
    <el-card class="welcome">
      <h3>欢迎使用小微企业数字化台账管理系统</h3>
      <p>脚手架已就绪。前后端联通状态：</p>
      <el-tag :type="connected ? 'success' : 'danger'">
        {{ connected ? '后端已连接' : '后端未连接（请先启动 backend）' }}
      </el-tag>
    </el-card>

    <el-row :gutter="16" class="stats">
      <el-col :span="8">
        <el-card><div class="stat">财务流水台账</div></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><div class="stat">库存明细台账</div></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><div class="stat">客户信息台账</div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/api/request'

const connected = ref(false)

// 启动时调用后端健康检查，验证前后端联通
onMounted(async () => {
  try {
    const res = await request.get('/ping')
    connected.value = res.code === 200
  } catch {
    connected.value = false
  }
})
</script>

<style scoped>
.welcome {
  margin-bottom: 16px;
}
.stats .stat {
  text-align: center;
  font-size: 16px;
  padding: 20px 0;
}
</style>
