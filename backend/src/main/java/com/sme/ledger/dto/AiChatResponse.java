package com.sme.ledger.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 问答出参：回答文本 + 是否降级 + 参考来源 + 台账摘要与异常提示。
 */
public class AiChatResponse {

    private String answer;

    /** true 表示大模型不可用，回答由「知识库检索 + 台账规则提示」降级生成 */
    private boolean degraded;

    /** 知识库参考来源 */
    private List<KnowledgeSource> sources = new ArrayList<>();

    /** 企业台账统计摘要（文本） */
    private String summary;

    /** 基于台账数据的简易异常/关注提示 */
    private List<String> hints = new ArrayList<>();

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public void setDegraded(boolean degraded) {
        this.degraded = degraded;
    }

    public List<KnowledgeSource> getSources() {
        return sources;
    }

    public void setSources(List<KnowledgeSource> sources) {
        this.sources = sources;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getHints() {
        return hints;
    }

    public void setHints(List<String> hints) {
        this.hints = hints;
    }
}
