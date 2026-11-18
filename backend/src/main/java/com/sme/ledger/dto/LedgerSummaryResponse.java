package com.sme.ledger.dto;

import java.util.List;

/**
 * 台账统计摘要（AI 助手页面的异常提示数据源）。
 */
public class LedgerSummaryResponse {

    private String summary;

    private List<String> hints;

    public LedgerSummaryResponse() {
    }

    public LedgerSummaryResponse(String summary, List<String> hints) {
        this.summary = summary;
        this.hints = hints;
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
