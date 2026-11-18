import request from './request'

// 财税 RAG 智能助手接口
// 注意：AI 问答耗时较长（大模型生成），单独放大超时时间
export function aiChat(question) {
  return request.post('/rag/chat', { question }, { timeout: 60000 })
}

export function getLedgerSummary() {
  return request.get('/rag/summary')
}

export function listKnowledgeDocs() {
  return request.get('/rag/documents')
}

export function uploadKnowledgeDoc(formData) {
  return request.post('/rag/documents', formData, { timeout: 60000 })
}

export function deleteKnowledgeDoc(id) {
  return request.delete(`/rag/documents/${id}`)
}
