package com.sme.ledger.dto;

/**
 * 知识库检索命中片段（问答回答的参考来源）。
 */
public class KnowledgeSource {

    /** 来源文档标题 */
    private String title;

    /** 命中的文本片段（截取） */
    private String snippet;

    public KnowledgeSource() {
    }

    public KnowledgeSource(String title, String snippet) {
        this.title = title;
        this.snippet = snippet;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
