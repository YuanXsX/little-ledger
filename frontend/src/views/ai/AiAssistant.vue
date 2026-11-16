<template>
  <div class="page ai">
    <div class="page-head">
      <div>
        <h2 class="page-title">财税 AI 助手</h2>
        <p class="page-sub">基于财税知识库的检索增强问答（RAG），当前处于规划阶段</p>
      </div>
      <el-tag type="warning" effect="light" round>模块规划中</el-tag>
    </div>

    <div class="grid">
      <div v-for="f in features" :key="f.title" class="card">
        <div class="icon" :class="f.tone">
          <el-icon :size="20"><component :is="f.icon" /></el-icon>
        </div>
        <div class="title">{{ f.title }}</div>
        <div class="desc">{{ f.desc }}</div>
        <div class="state">状态：{{ f.state }}</div>
      </div>
    </div>

    <div class="card empty">
      <el-empty description="知识库尚未接入，问答能力将在后续版本开放">
        <el-button type="primary" plain disabled>上传知识库文档</el-button>
      </el-empty>
      <div class="hint">
        规划：管理员上传财税政策文档 → 文本切分与向量化 → 检索 TopK 片段 → 大模型生成带引用的回答；
        同时读取台账统计摘要，输出库存、应收等异常提示。
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 财税 AI 助手占位页：说明模块规划，避免菜单出现失效链接。
 * 后续接入 RAG 后，此页面替换为知识库维护 + 对话界面。
 */
const features = [
  {
    icon: 'FolderOpened',
    tone: 'blue',
    title: '知识库维护',
    desc: '上传财税政策、库存规范文档，解析入库并可预览切片。',
    state: '待开发'
  },
  {
    icon: 'ChatDotRound',
    tone: 'teal',
    title: 'AI 问答',
    desc: '检索知识库片段，生成带来源引用的财税咨询建议。',
    state: '待开发'
  },
  {
    icon: 'DataAnalysis',
    tone: 'amber',
    title: '台账摘要提示',
    desc: '读取当前企业台账统计，给出库存、应收等异常提示。',
    state: '待开发'
  }
]
</script>

<style scoped>
.ai {
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
}

.page-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--app-text-mute);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.card {
  background: var(--app-card);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow);
  padding: 20px;
}

.icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.icon.blue { color: #2563eb; background: #eaf1ff; }
.icon.teal { color: #0ea5a4; background: #e6f7f6; }
.icon.amber { color: #f59e0b; background: #fef4e4; }

.title {
  font-size: 14.5px;
  font-weight: 600;
}

.desc {
  margin-top: 6px;
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--app-text-sub);
}

.state {
  margin-top: 14px;
  font-size: 11.5px;
  color: var(--app-text-mute);
}

.empty {
  padding: 10px 20px 24px;
}

.hint {
  margin-top: -8px;
  font-size: 12.5px;
  line-height: 1.8;
  color: var(--app-text-mute);
  text-align: center;
  max-width: 720px;
  margin-left: auto;
  margin-right: auto;
}
</style>
