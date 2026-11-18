<template>
  <div class="ai-page">
    <el-tabs v-model="tab">
      <!-- 智能问答 -->
      <el-tab-pane label="智能问答" name="chat">
        <el-row :gutter="16">
          <el-col :span="17">
            <el-card class="chat-card">
              <!-- 消息列表 -->
              <div ref="msgBox" class="msg-box">
                <el-empty v-if="messages.length === 0" description="您好，我是财税 AI 助手。可以咨询财税政策、库存规范，或让我分析公司的经营数据" :image-size="80" />
                <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role">
                  <div class="bubble">
                    <div class="role-name">{{ m.role === 'user' ? '我' : '财税 AI 助手' }}</div>
                    <div class="bubble-text">{{ m.content }}</div>
                    <el-alert v-if="m.degraded" class="degraded-tip" type="warning" :closable="false"
                      title="当前为离线模式（未配置或无法连接大模型），以上内容由知识库检索与台账规则生成" />
                    <div v-if="m.sources && m.sources.length" class="sources">
                      <div class="sources-title">参考来源：</div>
                      <div v-for="(s, si) in m.sources" :key="si" class="source-item">
                        <el-tag size="small" type="info">《{{ s.title }}》</el-tag>
                        <span class="snippet">{{ s.snippet }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="sending" class="msg-row assistant">
                  <div class="bubble"><div class="role-name">财税 AI 助手</div><el-skeleton :rows="2" animated /></div>
                </div>
              </div>
              <!-- 快捷提问 -->
              <div class="quick-questions">
                <el-tag v-for="q in quickQuestions" :key="q" class="quick-tag" @click="send(q)">{{ q }}</el-tag>
              </div>
              <!-- 输入区 -->
              <div class="input-row">
                <el-input v-model="question" type="textarea" :rows="2" resize="none"
                  placeholder="输入您想咨询的财税或经营问题…" @keydown.enter.exact.prevent="send()" />
                <el-button type="primary" :loading="sending" @click="send()">发送</el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 台账摘要与异常提示 -->
          <el-col :span="7">
            <el-card class="side-card">
              <div class="toolbar">
                <span class="section-title">台账摘要</span>
                <el-button link type="primary" @click="loadSummary">刷新</el-button>
              </div>
              <div class="summary-text">{{ summary || '加载中…' }}</div>
            </el-card>
            <el-card class="side-card">
              <div class="toolbar"><span class="section-title">异常 / 关注提示</span></div>
              <el-alert v-for="(h, i) in hints" :key="i" class="hint-item"
                :type="h.includes('库存不足') ? 'error' : 'warning'" :closable="false" :title="h" />
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 知识库管理（管理员） -->
      <el-tab-pane v-if="can('assistant:doc')" label="知识库管理" name="docs">
        <el-card>
          <div class="toolbar">
            <span class="section-title">财税知识库</span>
            <el-upload :show-file-list="false" :auto-upload="false" :limit="1"
              accept=".txt,.md,.docx,.pdf" :on-change="doUpload">
              <el-button type="primary" :loading="uploading">+ 上传文档</el-button>
            </el-upload>
            <el-button @click="loadDocs">刷新</el-button>
          </div>
          <div class="upload-tip">支持 txt / md / docx / pdf；上传后自动解析分块入库，供 AI 问答检索</div>
          <el-table :data="docs" v-loading="docsLoading" border>
            <el-table-column prop="title" label="文档标题" min-width="220" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdBy" label="上传人" width="110" />
            <el-table-column label="创建时间" width="170">
              <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="danger" @click="doDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiChat, getLedgerSummary, listKnowledgeDocs, uploadKnowledgeDoc, deleteKnowledgeDoc } from '@/api/rag'

const tab = ref('chat')
const question = ref('')
const messages = ref([])
const sending = ref(false)
const msgBox = ref(null)

const summary = ref('')
const hints = ref([])

const docs = ref([])
const docsLoading = ref(false)
const uploading = ref(false)

const quickQuestions = [
  '小规模纳税人要交哪些税？',
  '月销售额超过10万怎么办？',
  '哪些商品库存不足？',
  '分析一下公司近期的经营情况'
]

const user = JSON.parse(localStorage.getItem('user') || '{}')
const permissions = user.permissions || []
function can(perm) {
  return permissions.includes(perm)
}

function scrollToBottom() {
  nextTick(() => {
    if (msgBox.value) {
      msgBox.value.scrollTop = msgBox.value.scrollHeight
    }
  })
}

async function send(preset) {
  const text = (preset || question.value || '').trim()
  if (!text || sending.value) return
  question.value = ''
  messages.value.push({ role: 'user', content: text })
  scrollToBottom()
  sending.value = true
  try {
    const res = await aiChat(text)
    messages.value.push({
      role: 'assistant',
      content: res.data.answer,
      degraded: res.data.degraded,
      sources: res.data.sources || []
    })
    // 问答后同步刷新摘要与提示（低库存等数据可能变化）
    loadSummary()
  } catch (e) {
    // 拦截器已统一弹错
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

async function loadSummary() {
  try {
    const res = await getLedgerSummary()
    summary.value = res.data.summary
    hints.value = res.data.hints || []
  } catch (e) {
    // 拦截器已统一弹错
  }
}

async function loadDocs() {
  docsLoading.value = true
  try {
    const res = await listKnowledgeDocs()
    docs.value = res.data
  } finally {
    docsLoading.value = false
  }
}

async function doUpload(file) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    formData.append('title', file.name)
    const res = await uploadKnowledgeDoc(formData)
    const doc = res.data
    if (doc.status === 'PARSED') {
      ElMessage.success(`《${doc.title}》解析入库成功`)
    } else {
      ElMessage.warning(`《${doc.title}》解析失败：${doc.remark || '请检查文件内容'}`)
    }
    loadDocs()
  } finally {
    uploading.value = false
  }
}

async function doDelete(row) {
  await ElMessageBox.confirm(`确定删除知识库文档《${row.title}》吗？删除后问答将不再检索该文档`, '删除确认', { type: 'warning' })
  await deleteKnowledgeDoc(row.id)
  ElMessage.success('删除成功')
  loadDocs()
}

function statusType(status) {
  return { PARSED: 'success', FAILED: 'danger', PENDING: 'warning' }[status] || 'info'
}
function statusText(status) {
  return { PARSED: '已入库', FAILED: '解析失败', PENDING: '待解析' }[status] || status
}
function fmt(t) {
  return t ? t.replace('T', ' ') : ''
}

onMounted(() => {
  loadSummary()
  loadDocs()
})
</script>

<style scoped>
.ai-page {
  height: 100%;
}
.chat-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 190px);
}
.msg-box {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}
.msg-row {
  display: flex;
  margin-bottom: 14px;
}
.msg-row.user {
  justify-content: flex-end;
}
.msg-row.user .bubble {
  background: #409eff;
  color: #fff;
  max-width: 70%;
}
.msg-row.user .role-name {
  color: #d9ecff;
}
.msg-row.assistant .bubble {
  background: #f4f6f9;
  max-width: 88%;
}
.bubble {
  border-radius: 8px;
  padding: 10px 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
.role-name {
  font-size: 12px;
  margin-bottom: 4px;
  color: #909399;
}
.bubble-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  font-size: 14px;
}
.degraded-tip {
  margin-top: 8px;
}
.sources {
  margin-top: 8px;
  border-top: 1px dashed #dcdfe6;
  padding-top: 8px;
}
.sources-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.source-item {
  margin-bottom: 4px;
}
.snippet {
  font-size: 12px;
  color: #606266;
  margin-left: 6px;
}
.quick-questions {
  padding: 8px 4px 0;
}
.quick-tag {
  margin: 0 6px 6px 0;
  cursor: pointer;
}
.input-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding-top: 8px;
}
.side-card {
  margin-bottom: 16px;
}
.summary-text {
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 13px;
  color: #303133;
}
.hint-item {
  margin-bottom: 8px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.section-title {
  font-weight: 600;
  font-size: 15px;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}
</style>
