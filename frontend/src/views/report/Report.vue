<template>
  <div class="page report" v-loading="loading">
    <!-- 页头：标题 + 导出操作 -->
    <div class="page-head">
      <div>
        <h2 class="page-title">智能报表</h2>
        <p class="page-sub">三大核心报表一键生成，支持可视化分析与 Excel / PDF 导出</p>
      </div>
      <div class="head-actions">
        <el-button :loading="exporting === 'xlsx'" @click="doExport('xlsx')">
          <el-icon><Document /></el-icon>导出 Excel
        </el-button>
        <el-button :loading="exporting === 'pdf'" @click="doExport('pdf')">
          <el-icon><Printer /></el-icon>导出 PDF
        </el-button>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="toolbar-card">
      <div class="seg">
        <button v-for="t in reportTypes" :key="t.value" type="button" class="seg-item"
          :class="{ active: reportType === t.value }" @click="switchType(t.value)">
          <el-icon :size="15"><component :is="t.icon" /></el-icon>
          {{ t.label }}
        </button>
      </div>

      <div class="filters">
        <el-select v-if="reportType === 'finance'" v-model="range" style="width: 120px" @change="loadReport">
          <el-option label="按日" value="day" />
          <el-option label="按周" value="week" />
          <el-option label="按月" value="month" />
          <el-option label="按季度" value="quarter" />
        </el-select>

        <el-input v-if="reportType === 'inventory'" v-model="category" placeholder="按供应商 / 商品名筛选"
          clearable style="width: 240px" @change="loadReport">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-button type="primary" @click="loadReport">
          <el-icon><Refresh /></el-icon>生成报表
        </el-button>
      </div>
    </div>

    <!-- KPI 指标 -->
    <div v-if="metrics.length" class="metrics">
      <div v-for="(m, i) in metrics" :key="m.name" class="metric" :class="`tone-${i % 4}`">
        <div class="metric-name">{{ m.name }}</div>
        <div class="metric-value num">{{ m.value }}</div>
      </div>
    </div>

    <!-- 图表 -->
    <div v-if="charts.length" class="chart-grid">
      <div v-for="(c, i) in charts" :key="i" class="card chart-card">
        <div class="card-head">
          <div class="card-title">{{ c.name }}</div>
          <el-tag size="small" effect="plain" round>{{ chartTypeLabel(c.chartType) }}</el-tag>
        </div>
        <div class="chart-wrap">
          <div :ref="(el) => setChartRef(i, el)" class="chart-box"></div>
          <div v-if="!chartHasData(c)" class="chart-empty">暂无数据</div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && charts.length === 0" description="暂无报表数据，请先录入台账">
      <el-button type="primary" @click="$router.push('/ledger/finance')">去录入</el-button>
    </el-empty>
  </div>
</template>

<script setup>
/**
 * 智能报表：切换财务 / 库存 / 客户三类报表，ECharts 渲染折线、饼图、柱状图，支持导出。
 */
import { onBeforeUnmount, onMounted, nextTick, ref } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { ElMessage } from 'element-plus'
import { getFinanceReport, getInventoryReport, getCustomerReport, exportReport } from '@/api/report'

// 按需注册，减小打包体积
echarts.use([LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const reportTypes = [
  { value: 'finance', label: '财务报表', icon: 'Wallet' },
  { value: 'inventory', label: '库存报表', icon: 'Box' },
  { value: 'customer', label: '客户报表', icon: 'Avatar' }
]

const reportType = ref('finance')
const range = ref('month')
const category = ref('')
const metrics = ref([])
const charts = ref([])
const loading = ref(false)
const exporting = ref('')

const chartRefs = []
let chartInstances = []

const PALETTE = ['#2563eb', '#0ea5a4', '#f59e0b', '#7c3aed', '#ef4444', '#64748b']

function setChartRef(i, el) {
  chartRefs[i] = el
}

function chartTypeLabel(type) {
  return { line: '折线图', pie: '饼图', bar: '柱状图' }[type] || '图表'
}

/* 判断某个图表是否有非零数据，全零时展示空状态而不是空坐标轴 */
function chartHasData(c) {
  return (c.series || []).some((s) => (s.values || []).some((v) => Number(v) !== 0))
}

function switchType(type) {
  if (reportType.value === type) return
  reportType.value = type
  loadReport()
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
    metrics.value = res.data.metrics || []
    charts.value = res.data.charts || []
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  // 先快照当前 DOM 引用再销毁旧实例：dispose 会清空 chartRefs，顺序反了会取不到元素
  const els = charts.value.map((_, i) => chartRefs[i])
  disposeCharts()
  charts.value.forEach((c, i) => {
    const el = els[i]
    if (!el) return
    const instance = echarts.init(el)
    instance.setOption(buildOption(c))
    chartInstances.push(instance)
  })
}

function buildOption(c) {
  if (c.chartType === 'pie') {
    return {
      color: PALETTE,
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#6b7280' } },
      series: [{
        type: 'pie',
        radius: ['46%', '68%'],
        center: ['50%', '46%'],
        label: { show: false },
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
        data: c.labels.map((l, i) => ({ name: l, value: Number(c.series[0].values[i]) }))
      }]
    }
  }
  const isLine = c.chartType === 'line'
  return {
    color: PALETTE,
    tooltip: { trigger: 'axis', axisPointer: { type: isLine ? 'line' : 'shadow' } },
    legend: { top: 0, right: 0, icon: 'roundRect', itemWidth: 10, itemHeight: 10, textStyle: { color: '#6b7280' } },
    grid: { left: 6, right: 16, top: 36, bottom: 6, containLabel: true },
    xAxis: {
      type: 'category',
      data: c.labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e5e9f2' } },
      axisLabel: { color: '#8a94a6', fontSize: 11, rotate: c.labels.length > 6 ? 30 : 0 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f3f8' } },
      axisLabel: { color: '#8a94a6', fontSize: 11 }
    },
    series: c.series.map((s) => ({
      name: s.name,
      type: isLine ? 'line' : 'bar',
      smooth: true,
      // 单调平滑，避免曲线在峰值处过冲
      smoothMonotone: 'x',
      // 数据点较少时显示圆点，否则单点数据画不出线
      showSymbol: isLine && c.labels.length <= 10,
      symbolSize: 6,
      barMaxWidth: 36,
      itemStyle: { borderRadius: isLine ? 0 : [6, 6, 0, 0] },
      areaStyle: isLine ? { opacity: 0.1 } : undefined,
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
  exporting.value = format
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
  } finally {
    exporting.value = ''
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
.report {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--app-text);
}

.page-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--app-text-mute);
}

.head-actions {
  display: flex;
  gap: 10px;
}

/* 工具栏 */
.toolbar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 12px 16px;
  background: var(--app-card);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow);
}

.seg {
  display: inline-flex;
  padding: 4px;
  gap: 4px;
  background: #f2f4f9;
  border-radius: 10px;
}

.seg-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--app-text-sub);
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
}

.seg-item:hover {
  color: var(--app-primary);
}

.seg-item.active {
  background: #fff;
  color: var(--app-primary);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(16, 24, 40, 0.08);
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 指标 */
.metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.metric {
  padding: 16px 18px;
  background: var(--app-card);
  border: 1px solid var(--app-border);
  border-left: 3px solid var(--app-primary);
  border-radius: var(--app-radius-sm);
  box-shadow: var(--app-shadow);
}

.metric.tone-0 { border-left-color: #2563eb; }
.metric.tone-1 { border-left-color: #0ea5a4; }
.metric.tone-2 { border-left-color: #f59e0b; }
.metric.tone-3 { border-left-color: #7c3aed; }

.metric-name {
  font-size: 12.5px;
  color: var(--app-text-sub);
}

.metric-value {
  margin-top: 6px;
  font-size: 20px;
  font-weight: 650;
  color: var(--app-text);
}

/* 图表 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(420px, 1fr));
  gap: 16px;
}

.card {
  background: var(--app-card);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow);
  padding: 16px 18px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-title {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--app-text);
}

.chart-wrap {
  position: relative;
}

.chart-box {
  width: 100%;
  height: 320px;
  margin-top: 8px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  color: var(--app-text-mute);
  font-size: 13px;
  border-radius: var(--app-radius-sm);
}

@media (max-width: 768px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .page-head {
    flex-direction: column;
  }
}
</style>
