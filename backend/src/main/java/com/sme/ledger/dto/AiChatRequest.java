package com.sme.ledger.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * AI 问答入参。
 */
public class AiChatRequest {

    @NotBlank(message = "提问内容不能为空")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
