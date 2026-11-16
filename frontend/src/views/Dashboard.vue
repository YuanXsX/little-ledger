<template>
  <div class="page dashboard" v-loading="loading">
    <!-- 顶部欢迎区：问候语 + 关键信息 + 快捷操作 -->
    <section class="hero">
      <div class="hero-left">
        <div class="hero-eyebrow">{{ today }} · {{ companyName }}</div>
        <h1>{{ greeting }}</h1>
        <p>{{ heroTip }}</p>
        <div class="hero-actions">
          <el-button type="primary" @click="go('/ledger/finance')">
            <el-icon><Plus /></el-icon>录入流水
          </el-button>
          <el-button @click="go('/report')">
            <el-icon><DataLine /></el-icon>查看报表
          </el-button>
        </div>
      </div>

      <div class="hero-right">
        <div v-for="item in miniStats" :key="item.name" class="mini">
          <div class="mini-name">{{ item.name }}</div>
          <div class="mini-value num">{{ item.value }}</div>
        </div>
      </div>
    </section>

    <!-- 核心指标 -->
    <section class="stats">
      <div v-for="(m, i) in primaryMetrics" :key="m.name" class="stat" :class="`tone-${i}`">
        <div class="stat-icon">
          <el-icon :size="20"><component :is="statIcons[i]" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ m.name }}</div>
          <div class="stat-value num">{{ m.value }}</div>
        </div>
      </div>
    </section>

    <el-empty v-if="!loading && primaryMetrics.length === 0" description="暂无经营数据，先录入一条台账记录吧">
      <el-button type="primary" @click="go('/ledger/finance')">去录入</el-button>
    </el-empty>

    <!-- 图表区 -->
    <section v-if="primaryMetrics.length" class="charts">
      <div class="card chart-card">
        <div class="card-head">
          <div>
            <div class="card-title">收支趋势</div>
            <div class="card-sub">近 14 天每日收入与支出变化</div>
          </div>
          <el-tag size="small" effect="plain" round>近 14 天</el-tag>
        </div>
        <div class="chart-wrap">
          <div ref="trendRef" class="chart-box"></div>
          <div v-if="!trendHasData" class="chart-empty">暂无财务流水数据</div>
        </div>
      </div>

      <div class="card chart-card">
        <div class="card-head">
          <div>
            <div class="card-title">支付方式占比</div>
            <div class="card-sub">资金流入流出的渠道分布</div>
          </div>
        </div>
        <div class="chart-wrap">
          <div ref="pieRef" class="chart-box"></div>
          <div v-if="!pieHasData" class="chart-empty">暂无支付方式数据</div>
        </div>
      </div>
    </section>

    <!-- 预警 + 快捷入口 -->
    <section class="bottom">
      <div class="card">
        <div class="card-head">
          <div>
            <div class="card-title">待处理预警</div>
            <div class="card-sub">库存不足等风险，需要及时跟进</div>
          </div>
          <el-button v-if="canAlert" link type="primary" @click="go('/alerts')">全部</el-button>
        </div>

        <ul v-if="alerts.length" class="alert-list">
          <li v-for="a in alerts" :key="a.id">
            <span class="alert-dot" />
            <div class="alert-main">
              <div class="alert-title">{{ a.alertType }} · {{ a.target }}</div>
              <div class="alert-desc">{{ a.currentVal }}</div>
            </div>
            <span class="alert-time">{{ shortTime(a.createdAt) }}</span>
          </li>
        </ul>
        <div v-else class="empty-tip">
          <el-icon><CircleCheck /></el-icon>
          当前没有待处理预警，经营状态良好
        </div>
      </div>

      <div class="card">
        <div class="card-head">
          <div>
            <div class="card-title">快捷入口</div>
            <div class="card-sub">常用台账与报表</div>
          </div>
        </div>
        <div class="quick-grid">
          <button v-for="q in quickActions" :key="q.path" class="quick" type="button" @click="go(q.path)">
            <span class="quick-icon"><el-icon :size="18"><component :is="q.icon" /></el-icon></span>
            <span class="quick-text">{{ q.name }}</span>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
/**
 * 工作台：经营概览 KPI + 收支趋势 / 支付方式占比图表 + 待处理预警 + 快捷入口。
 * 数据来源：/reports/overview、/reports/finance、/alerts（均按当前身份权限访问）。
 */
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts/core'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getReportOverview, getFinanceReport } from '@/api/report'
import { listAlerts } from '@/api/alert'

// 按需注册图表组件，控制打包体积
echarts.use([LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const permissions = user.permissions || []
const canAlert = permissions.includes('alert:view')

const loading = ref(false)
const metrics = ref([])
const alerts = ref([])
const trendSeries = ref([])
const trendLabels = ref([])
const pieData = ref([])

const trendRef = ref(null)
const pieRef = ref(null)
let trendChart = null
let pieChart = null

const companyName = '小微企业'

/* 根据当前时间生成问候语 */
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const today = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
}).format(new Date())

const statIcons = ['Wallet', 'TrendCharts', 'Money', 'Warning']

/* 从概览指标中按名称取数，缺失时降级为空串 */
function pick(name) {
  return metrics.value.find((m) => m.name === name)?.value ?? '—'
}

const primaryMetrics = computed(() => {
  const wanted = ['总收入', '净收入', '应收账款总额', '低库存商品数']
  const picked = wanted.map((name) => ({ name, value: pick(name) }))
  // 后端指标名变化时兜底：直接展示前 4 个
  return primaryMetricsFallback(picked)
})

function primaryMetricsFallback(picked) {
  if (metrics.value.length === 0) return []
  const allPlaceholder = picked.every((p) => p.value === '—')
  return allPlaceholder ? metrics.value.slice(0, 4) : picked
}

const miniStats = computed(() => [
  { name: '总支出', value: pick('总支出') },
  { name: '客户总数', value: pick('客户总数') },
  { name: '库存总量', value: pick('库存总量') }
])

/* 图表是否有实际数据（全为 0 时用空状态覆盖，避免显示空白坐标轴/空心环） */
const trendHasData = computed(() =>
  trendSeries.value.some((s) => (s.values || []).some((v) => Number(v) > 0))
)
const pieHasData = computed(() => pieData.value.some((d) => Number(d.value) > 0))

const heroTip = computed(() => {
  const low = pick('低库存商品数')
  if (low !== '—' && Number(low) > 0) {
    return `有 ${low} 项商品库存低于阈值，建议尽快补货，避免影响经营。`
  }
  return '经营数据已同步，今天也从一份清晰的台账开始。'
})

const quickActions = [
  { name: '财务流水', path: '/ledger/finance', icon: 'Coin' },
  { name: '库存明细', path: '/ledger/inventory', icon: 'Box' },
  { name: '客户信息', path: '/ledger/customer', icon: 'Avatar' },
  { name: '智能报表', path: '/report', icon: 'DataAnalysis' }
]

function go(path) {
  router.push(path)
}

/**
 * 近 14 天的起止日期。
 * 后端 range=day 时按天聚合，配合 startDate/endDate 即可得到逐日趋势；
 * 日期用本地时区格式化，避免 toISOString 的 UTC 偏移造成跨天。
 */
function trendWindow() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 13)
  const fmt = (d) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return { startDate: fmt(start), endDate: fmt(end) }
}

function shortTime(t) {
  return t ? String(t).replace('T', ' ').slice(5, 16) : ''
}

/* ---------------- 数据加载 ---------------- */

async function load() {
  loading.value = true
  try {
    const [overview, finance] = await Promise.all([
      getReportOverview().catch(() => null),
      getFinanceReport({ range: 'day', ...trendWindow() }).catch(() => null)
    ])

    if (overview?.data?.metrics) {
      metrics.value = overview.data.metrics
    }

    const charts = finance?.data?.charts || []
    const line = charts.find((c) => c.chartType === 'line')
    const pie = charts.find((c) => c.chartType === 'pie')

    if (line) {
      trendLabels.value = line.labels
      trendSeries.value = line.series
    }
    if (pie) {
      pieData.value = pie.labels.map((l, i) => ({
        name: l,
        value: Number(pie.series[0]?.values?.[i] ?? 0)
      }))
    }

    if (canAlert) {
      const res = await listAlerts({ status: 'UNRESOLVED' }).catch(() => null)
      alerts.value = (res?.data || []).slice(0, 5)
    }

    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

/* ---------------- 图表渲染 ---------------- */

function renderCharts() {
  if (trendRef.value && trendSeries.value.length) {
    trendChart = trendChart || echarts.init(trendRef.value)
    trendChart.setOption(buildTrendOption(), true)
  }
  if (pieRef.value && pieData.value.length) {
    pieChart = pieChart || echarts.init(pieRef.value)
    pieChart.setOption(buildPieOption(), true)
  }
}

function buildTrendOption() {
  return {
    color: ['#2563eb', '#f59e0b'],
    grid: { left: 6, right: 14, top: 34, bottom: 6, containLabel: true },
    tooltip: { trigger: 'axis', axisPointer: { type: 'line' } },
    legend: { top: 0, right: 0, icon: 'roundRect', itemWidth: 10, itemHeight: 10, textStyle: { color: '#6b7280' } },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendLabels.value,
      axisLine: { lineStyle: { color: '#e5e9f2' } },
      axisTick: { show: false },
      axisLabel: { color: '#8a94a6', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f3f8' } },
      axisLabel: { color: '#8a94a6', fontSize: 11 }
    },
    series: trendSeries.value.map((s) => ({
      name: s.name,
      type: 'line',
      smooth: true,
      // 单调平滑，避免贝塞尔曲线在峰值处过冲、显得比真实数据高
      smoothMonotone: 'x',
      // 数据点较少时显示圆点，否则单点数据画不出线
      showSymbol: trendLabels.value.length <= 10,
      symbolSize: 6,
      lineStyle: { width: 2.4 },
      areaStyle: { opacity: 0.1 },
      data: s.values.map((v) => Number(v))
    }))
  }
}

function buildPieOption() {
  return {
    color: ['#2563eb', '#0ea5a4', '#f59e0b', '#7c3aed', '#ef4444', '#64748b'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#6b7280', fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['48%', '70%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: true,
      label: { show: false },
      itemStyle: { borderColor: '#fff', borderWidth: 3, borderRadius: 6 },
      data: pieData.value
    }]
  }
}

function onResize() {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  pieChart?.dispose()
  trendChart = null
  pieChart = null
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ---------------- 顶部欢迎区 ---------------- */
.hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 26px 28px;
  border-radius: var(--app-radius);
  color: #eaf2ff;
  background: radial-gradient(600px 240px at 0% 0%, #245196 0%, transparent 60%),
    linear-gradient(120deg, #16294a 0%, #1d3c69 100%);
  box-shadow: 0 12px 30px rgba(18, 41, 74, 0.18);
}

.hero-eyebrow {
  font-size: 12.5px;
  color: rgba(195, 214, 240, 0.78);
  letter-spacing: 0.4px;
}

.hero h1 {
  margin: 10px 0 8px;
  font-size: 25px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.hero p {
  margin: 0;
  font-size: 13px;
  color: rgba(200, 216, 238, 0.8);
  max-width: 520px;
  line-height: 1.7;
}

.hero-actions {
  margin-top: 18px;
  display: flex;
  gap: 10px;
}

.hero-right {
  display: flex;
  gap: 26px;
  align-items: center;
  flex-wrap: wrap;
}

.mini {
  min-width: 96px;
}

.mini-name {
  font-size: 12px;
  color: rgba(195, 214, 240, 0.7);
}

.mini-value {
  margin-top: 6px;
  font-size: 19px;
  font-weight: 600;
  color: #fff;
}

/* ---------------- 指标卡 ---------------- */
.stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: var(--app-card);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.stat:hover {
  transform: translateY(-2px);
  box-shadow: var(--app-shadow-lg);
}

.stat-icon {
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tone-0 .stat-icon {
  color: #2563eb;
  background: #eaf1ff;
}

.tone-1 .stat-icon {
  color: #0ea5a4;
  background: #e6f7f6;
}

.tone-2 .stat-icon {
  color: #f59e0b;
  background: #fef4e4;
}

.tone-3 .stat-icon {
  color: #ef4444;
  background: #feecec;
}

.stat-label {
  font-size: 12.5px;
  color: var(--app-text-sub);
}

.stat-value {
  margin-top: 4px;
  font-size: 21px;
  font-weight: 650;
  color: var(--app-text);
  letter-spacing: 0.2px;
}

/* ---------------- 卡片通用 ---------------- */
.card {
  background: var(--app-card);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow);
  padding: 18px 20px;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text);
}

.card-sub {
  margin-top: 3px;
  font-size: 12px;
  color: var(--app-text-mute);
}

/* ---------------- 图表 ---------------- */
.charts {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 16px;
}

.chart-wrap {
  position: relative;
}

.chart-box {
  width: 100%;
  height: 300px;
  margin-top: 6px;
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

/* ---------------- 底部两栏 ---------------- */
.bottom {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 16px;
}

.alert-list {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
}

.alert-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 4px;
  border-bottom: 1px dashed #eef1f6;
}

.alert-list li:last-child {
  border-bottom: none;
}

.alert-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12);
  flex: 0 0 7px;
}

.alert-main {
  flex: 1;
  min-width: 0;
}

.alert-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text);
}

.alert-desc {
  font-size: 12px;
  color: var(--app-text-mute);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alert-time {
  font-size: 11.5px;
  color: var(--app-text-mute);
}

.empty-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 28px 4px;
  color: var(--app-text-sub);
  font-size: 13px;
}

.empty-tip .el-icon {
  color: #16a34a;
}

/* ---------------- 快捷入口 ---------------- */
.quick-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.quick {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid var(--app-border);
  border-radius: 11px;
  background: #fbfcfe;
  cursor: pointer;
  font-family: inherit;
  font-size: 13px;
  color: var(--app-text);
  transition: all 0.15s ease;
}

.quick:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
  transform: translateY(-1px);
}

.quick-icon {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-primary);
  background: #eaf1ff;
}

/* ---------------- 响应式 ---------------- */
@media (max-width: 1280px) {
  .charts,
  .bottom {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1100px) {
  .stats {
    grid-template-columns: 1fr 1fr;
  }

  .hero {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .stats {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
