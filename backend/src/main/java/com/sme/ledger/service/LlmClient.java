package com.sme.ledger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 大模型客户端：OpenAI 兼容协议（默认 DeepSeek，可换通义千问等兼容服务）。
 *
 * 降级策略：未配置 API Key、请求超时或返回异常时，{@link #chat} 返回 null，
 * 由 {@link AiAssistantService} 降级为「知识库检索原文 + 台账规则提示」。
 */
@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public LlmClient(@Value("${app.ai.base-url:https://api.deepseek.com}") String baseUrl,
                     @Value("${app.ai.api-key:}") String apiKey,
                     @Value("${app.ai.model:deepseek-chat}") String model,
                     @Value("${app.ai.timeout-seconds:30}") int timeoutSeconds) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(timeoutSeconds, 5));
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.restClient = RestClient.builder()
                .baseUrl(this.baseUrl)
                .requestFactory(factory)
                .build();
    }

    /** 是否已配置 API Key（未配置则问答直接走降级模式） */
    public boolean configured() {
        return !apiKey.isBlank();
    }

    /**
     * 调用对话补全接口。
     *
     * @return 模型回答文本；任何异常（超时/限流/网络故障等）返回 null，走降级
     */
    @SuppressWarnings("unchecked")
    public String chat(String systemPrompt, String userMessage) {
        if (!configured()) {
            log.info("未配置大模型 API Key，问答将使用降级模式");
            return null;
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)),
                    "temperature", 0.3,
                    "max_tokens", 1024);

            Map<String, Object> resp = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (resp == null) {
                return null;
            }
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            Object content = message == null ? null : message.get("content");
            return content == null ? null : content.toString().trim();
        } catch (Exception e) {
            // 大模型不可用不阻断业务：记录日志后由上层降级
            log.warn("大模型调用失败，进入降级模式: {}", e.getMessage());
            return null;
        }
    }
}
