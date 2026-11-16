<template>
  <div class="login">
    <!-- 左侧品牌区：产品定位与核心能力，宽屏展示、窄屏隐藏 -->
    <section class="brand-panel">
      <div class="brand-head">
        <div class="brand-mark">账</div>
        <div>
          <div class="brand-title">小微企业数字化台账管理系统</div>
          <div class="brand-sub">SME DIGITAL LEDGER</div>
        </div>
      </div>

      <div class="brand-copy">
        <h1>轻量化 · 易操作 · 高安全</h1>
        <p>财务、库存、客户三大台账集中管控，校验自动化、预警实时化、报表一键生成。</p>
      </div>

      <ul class="feature-list">
        <li v-for="f in features" :key="f.title">
          <span class="feature-icon"><el-icon><component :is="f.icon" /></el-icon></span>
          <div>
            <div class="feature-title">{{ f.title }}</div>
            <div class="feature-desc">{{ f.desc }}</div>
          </div>
        </li>
      </ul>

      <div class="brand-foot">
        <span class="version">v0.1.0</span>
        <span>演示环境 · 数据每日自动备份</span>
      </div>
    </section>

    <!-- 右侧登录表单 -->
    <section class="form-panel">
      <div class="form-card">
        <div class="form-head">
          <h2>欢迎登录</h2>
          <p>请使用企业分配的账号进入工作台</p>
        </div>

        <el-form :model="form" label-position="top" @submit.prevent="onLogin">
          <el-form-item label="账号">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码">
            <el-input v-model="form.password" size="large" type="password" placeholder="请输入密码"
              show-password @keyup.enter="onLogin">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <div class="form-extra">
            <el-checkbox v-model="remember">记住账号</el-checkbox>
            <span class="link">忘记密码？联系管理员</span>
          </div>

          <el-button type="primary" size="large" class="submit" :loading="loading" @click="onLogin">
            登 录
          </el-button>
        </el-form>

        <!-- 演示账号：点击直接填入，方便答辩演示 -->
        <div class="demo">
          <div class="demo-title">演示账号（点击填入）</div>
          <div class="demo-chips">
            <button v-for="d in demoAccounts" :key="d.username" class="chip" type="button"
              @click="fill(d)">
              <span class="chip-role">{{ d.role }}</span>
              <span class="chip-account">{{ d.username }}</span>
            </button>
          </div>
        </div>
      </div>

      <div class="copyright">© 2026 小微企业数字化台账管理系统 · 仅用于教学与演示</div>
    </section>
  </div>
</template>

<script setup>
/**
 * 登录页：左侧品牌介绍 + 右侧登录表单，演示账号一键填充。
 * 认证成功后把 token 与用户信息（含权限点）写入 localStorage。
 */
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const remember = ref(true)

const features = [
  { icon: 'Checked', title: '数据校验自动化', desc: '重复凭证、金额格式、手机号规则自动校验' },
  { icon: 'Bell', title: '风险预警实时化', desc: '库存低于阈值自动触发多渠道提醒' },
  { icon: 'Lock', title: '数据安全可保障', desc: '敏感字段脱敏、定时备份与一键恢复' }
]

const demoAccounts = [
  { role: '企业负责人', username: 'admin', password: 'admin123' },
  { role: '财务负责人', username: 'finance', password: 'finance123' },
  { role: '库管', username: 'keeper', password: 'keeper123' },
  { role: '普通员工', username: 'employee', password: 'employee123' }
]

function fill(account) {
  form.username = account.username
  form.password = account.password
}

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
    if (remember.value) {
      localStorage.setItem('lastUsername', form.username)
    }
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const last = localStorage.getItem('lastUsername')
  if (last) form.username = last
})
</script>

<style scoped>
.login {
  height: 100%;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  background: #f6f8fc;
}

/* ---------------- 左侧品牌区 ---------------- */
.brand-panel {
  position: relative;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
  color: #e6eefc;
  background: radial-gradient(1200px 600px at 10% 0%, #1d4b86 0%, transparent 55%),
    linear-gradient(155deg, #12294a 0%, #0b1a2e 100%);
  overflow: hidden;
}

/* 背景网格纹理 */
.brand-panel::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.045) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: radial-gradient(circle at 30% 20%, #000 0%, transparent 70%);
  pointer-events: none;
}

.brand-head {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  color: #0d1f38;
  background: linear-gradient(135deg, #7dd3fc, #38bdf8);
  box-shadow: 0 8px 22px rgba(56, 189, 248, 0.35);
}

.brand-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.brand-sub {
  font-size: 10px;
  letter-spacing: 2.4px;
  color: rgba(195, 208, 226, 0.65);
}

.brand-copy {
  margin-top: 64px;
  position: relative;
  z-index: 1;
}

.brand-copy h1 {
  margin: 0 0 14px;
  font-size: 34px;
  letter-spacing: 2px;
  line-height: 1.3;
  color: #fff;
}

.brand-copy p {
  margin: 0;
  max-width: 440px;
  font-size: 14px;
  line-height: 1.9;
  color: rgba(214, 227, 245, 0.78);
}

.feature-list {
  margin: 40px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 18px;
  position: relative;
  z-index: 1;
}

.feature-list li {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.feature-icon {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7dd3fc;
  background: rgba(56, 189, 248, 0.12);
  border: 1px solid rgba(56, 189, 248, 0.22);
}

.feature-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #e8f1ff;
}

.feature-desc {
  font-size: 12.5px;
  color: rgba(195, 208, 226, 0.7);
  margin-top: 2px;
}

.brand-foot {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: rgba(195, 208, 226, 0.55);
  position: relative;
  z-index: 1;
}

.version {
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid rgba(125, 211, 252, 0.28);
  color: #7dd3fc;
}

/* ---------------- 右侧表单 ---------------- */
.form-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.form-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 20px;
  padding: 36px 34px 26px;
  box-shadow: 0 24px 60px rgba(16, 24, 40, 0.08);
}

.form-head h2 {
  margin: 0;
  font-size: 24px;
  color: var(--app-text);
}

.form-head p {
  margin: 8px 0 26px;
  font-size: 13px;
  color: var(--app-text-mute);
}

.form-extra {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 18px;
  font-size: 12.5px;
}

.link {
  color: var(--app-text-mute);
  cursor: default;
}

.submit {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
  border-radius: 10px;
}

.demo {
  margin-top: 26px;
  padding-top: 18px;
  border-top: 1px dashed var(--app-border);
}

.demo-title {
  font-size: 12px;
  color: var(--app-text-mute);
  margin-bottom: 10px;
}

.demo-chips {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.chip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  padding: 8px 10px;
  border: 1px solid var(--app-border);
  border-radius: 9px;
  background: #fbfcfe;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: inherit;
}

.chip:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
  transform: translateY(-1px);
}

.chip-role {
  font-size: 12px;
  color: var(--app-text-sub);
}

.chip-account {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--app-primary);
  font-family: 'SFMono-Regular', Consolas, monospace;
}

.copyright {
  margin-top: 22px;
  font-size: 11.5px;
  color: var(--app-text-mute);
}

/* ---------------- 响应式 ---------------- */
@media (max-width: 1024px) {
  .login {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    display: none;
  }
}
</style>
