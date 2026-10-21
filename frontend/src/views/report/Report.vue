<template>
  <div v-loading="loading">
    <!-- 筛选工具栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <el-radio-group v-model="reportType" @change="loadReport">
          <el-radio-button label="finance">财务报表</el-radio-button>
          <el-radio-button label="inventory">库存报表</el-radio-button>
          <el-radio-button label="customer">客户报表</el-radio-button>
        </el-radio-group>

        <el-select v-if="reportType === 'finance'" v-model="range" style="width: 110px" @change="loadReport">
          <el-option label="按日" value="day" />
          <el-option label="按周" value="week" />
          <el-option label="按月" value="month" />
          <el-option label="按季度" value="quarter" />
        </el-select>

        <el-input v-if="reportType === 'inventory'" v-model="category" placeholder="商品类别筛选(供应商/商品名)"
          clearable style="width: 220px" @change="loadReport" />

        <div class="spacer" />
        <el-button type="primary" @click="loadReport">生成报表</el-button>
        <el-button @click="doExport('xlsx')">导出 Excel</el-button>
        <el-button @click="doExport('pdf')">导出 PDF</el-button>
      </div>
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

    <!-- 图表区 -->
    <el-row :gutter="16">
      <el-col v-for="(c, i) in charts" :key="i" :span="12">
        <el-card class="chart-card">
          <div class="chart-title">{{ c.name }}</div>
          <div :ref="(el) => setChartRef(i, el)" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && charts.length === 0" description="暂无报表数据，请先录入台账" />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, nextTick, ref } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { ElMessage } from 'element-plus'
import { getFinanceReport, getInventoryReport, getCustomerReport, exportReport } from '@/api/report'

// 按需注册，减小打包体积
echarts.use([LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const reportType = ref('finance')
const range = ref('month')
const category = ref('')
const metrics = ref([])
const charts = ref([])
const loading = ref(false)

const chartRefs = []
let chartInstances = []

function setChartRef(i, el) {
  chartRefs[i] = el
}

async function loadReport() {
  loading.value = true
  try {
    let res
    if (reportType.value === 'inventory') {
      res = await getInventoryReport({ category: category.value || undefined })
    } else if (reportType.value === 'customer') {
      res = await getCustomerReport()
    } else {
      res = await getFinanceReport({ range: range.value })
    }
    metrics.value = res.data.metrics
    charts.value = res.data.charts
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  disposeCharts()
  charts.value.forEach((c, i) => {
    const el = chartRefs[i]
    if (!el) return
    const instance = echarts.init(el)
    instance.setOption(buildOption(c))
    chartInstances.push(instance)
  })
}

function buildOption(c) {
  if (c.chartType === 'pie') {
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['38%', '62%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        data: c.labels.map((l, i) => ({ name: l, value: Number(c.series[0].values[i]) }))
      }]
    }
  }
  const isLine = c.chartType === 'line'
  return {
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: 50, right: 20, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: c.labels,
      axisLabel: { rotate: c.labels.length > 6 ? 30 : 0 }
    },
    yAxis: { type: 'value' },
    series: c.series.map((s) => ({
      name: s.name,
      type: isLine ? 'line' : 'bar',
      smooth: true,
      barMaxWidth: 40,
      data: s.values.map((v) => Number(v))
    }))
  }
}

function disposeCharts() {
  chartInstances.forEach((inst) => inst.dispose())
  chartInstances = []
  chartRefs.length = 0
}

async function doExport(format) {
  try {
    const res = await exportReport({
      type: reportType.value,
      format,
      range: range.value,
      category: category.value || undefined
    })
    const blob = res.data
    const disposition = res.headers['content-disposition'] || ''
    let filename = `报表.${format}`
    const star = disposition.match(/filename\*=UTF-8''([^;]+)/i)
    if (star) {
      filename = decodeURIComponent(star[1])
    } else {
      const plain = disposition.match(/filename="?([^";]+)"?/i)
      if (plain) filename = plain[1]
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    // 拦截器已提示错误
  }
}

function onResize() {
  chartInstances.forEach((inst) => inst.resize())
}

onMounted(() => {
  loadReport()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  disposeCharts()
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.toolbar-card {
  margin-bottom: 16px;
}
.spacer {
  flex: 1;
}
.metrics {
  margin-bottom: 16px;
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
.chart-card {
  margin-bottom: 16px;
}
.chart-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
}
.chart-box {
  width: 100%;
  height: 320px;
}
</style>
