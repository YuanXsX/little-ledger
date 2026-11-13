<template>
  <div v-loading="loading">
    <el-card class="welcome">
      <div class="welcome-title">欢迎回来，{{ username }}</div>
      <div class="welcome-sub">以下是当前企业经营概览</div>
    </el-card>

    <!-- KPI 指标卡片 -->
    <el-row :gutter="16" class="metrics">
      <el-col v-for="m in metrics" :key="m.name" :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-name">{{ m.name }}</div>
          <div class="metric-value">{{ m.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && metrics.length === 0" description="暂无经营数据，请先录入台账" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getReportOverview } from '@/api/report'

const metrics = ref([])
const loading = ref(false)

const user = JSON.parse(localStorage.getItem('user') || '{}')
const username = computed(() => user.realName || user.username || '管理员')

async function load() {
  loading.value = true
  try {
    const res = await getReportOverview()
    metrics.value = res.data.metrics
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.welcome {
  margin-bottom: 16px;
}
.welcome-title {
  font-size: 18px;
  font-weight: bold;
  color: #1f3a5f;
}
.welcome-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}
.metric-card {
  text-align: center;
  margin-bottom: 12px;
}
.metric-name {
  color: #909399;
  font-size: 13px;
}
.metric-value {
  font-size: 22px;
  font-weight: bold;
  margin-top: 6px;
  color: #1f3a5f;
}
</style>
