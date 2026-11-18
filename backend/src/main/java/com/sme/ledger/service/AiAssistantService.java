package com.sme.ledger.service;

import com.sme.ledger.dto.AiChatResponse;
import com.sme.ledger.dto.KnowledgeSource;
import com.sme.ledger.dto.LedgerSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 财税 AI 助手编排：
 * 提问 → 知识库关键词检索 TopK → 拼接台账摘要 → LLM 生成回答；
 * LLM 未配置/超时/异常时降级为「检索原文 + 台账规则提示」。
 */
@Service
public class AiAssistantService {

    /** 系统提示词：限定回答依据，防止编造法规 */
    private static final String SYSTEM_PROMPT = """
            你是「小微企业数字化台账管理系统」的财税 AI 助手，服务对象是不具备专业财税知识的小微企业经营者。
            请遵守以下规则：
            1. 优先依据【知识库资料】回答财税、库存管理问题，并在回答末尾用「来源：文档标题」列出依据；
            2. 知识库资料中没有的内容，必须明确说明「知识库中未收录相关内容，建议咨询专业会计或税务师」，严禁编造法规条款、税率、政策文件号；
            3. 用户问到经营情况时，结合【企业台账摘要】中的真实数据回答，并主动提示其中的异常项（库存不足、未处理预警、净收入为负等）；
            4. 回答使用中文，简明扼要，适当分点，金额带「元」单位。
            """;

    private final KnowledgeSearchService searchService;
    private final LedgerSummaryService summaryService;
    private final LlmClient llmClient;

    public AiAssistantService(KnowledgeSearchService searchService,
                              LedgerSummaryService summaryService,
                              LlmClient llmClient) {
        this.searchService = searchService;
        this.summaryService = summaryService;
        this.llmClient = llmClient;
    }

    /** AI 问答主流程 */
    public AiChatResponse chat(String question) {
        List<KnowledgeSource> sources = searchService.search(question);
        LedgerSummaryResponse summary = summaryService.build();

        AiChatResponse resp = new AiChatResponse();
        resp.setSources(sources);
        resp.setSummary(summary.getSummary());
        resp.setHints(summary.getHints());

        if (llmClient.configured()) {
            String answer = llmClient.chat(SYSTEM_PROMPT, buildUserPrompt(question, sources, summary));
            if (answer != null && !answer.isBlank()) {
                resp.setAnswer(answer);
                resp.setDegraded(false);
                return resp;
            }
        }
        resp.setAnswer(degradedAnswer(question, sources, summary));
        resp.setDegraded(true);
        return resp;
    }

    /** 拼接给大模型的用户消息：知识库片段 + 台账摘要 + 提问 */
    private static String buildUserPrompt(String question,
                                          List<KnowledgeSource> sources,
                                          LedgerSummaryResponse summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("【知识库资料】\n");
        if (sources.isEmpty()) {
            sb.append("（无检索到相关内容）\n");
        } else {
            for (int i = 0; i < sources.size(); i++) {
                sb.append('[').append(i + 1).append("] 《").append(sources.get(i).getTitle())
                        .append("》\n").append(sources.get(i).getSnippet()).append('\n');
            }
        }
        sb.append("\n【企业台账摘要】\n").append(summary.getSummary()).append('\n');
        sb.append("\n【用户提问】\n").append(question);
        return sb.toString();
    }

    /** 降级回答：知识库检索原文 + 台账规则提示（无 LLM 依赖，保证演示可用） */
    private static String degradedAnswer(String question,
                                         List<KnowledgeSource> sources,
                                         LedgerSummaryResponse summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("（离线模式：未配置或无法连接大模型，以下回答由知识库检索与台账规则生成）\n\n");
        if (sources.isEmpty()) {
            sb.append("知识库中未检索到与「").append(question)
                    .append("」直接相关的内容。建议：\n")
                    .append("1. 在「知识库管理」页上传财税政策或库存规范文档后重试；\n")
                    .append("2. 配置大模型 API Key 获得更智能的回答；\n")
                    .append("3. 涉及具体申报、缴税事项，请咨询专业会计或税务师。");
        } else {
            sb.append("与您的问题相关的知识库资料如下：\n");
            for (int i = 0; i < sources.size(); i++) {
                sb.append('[').append(i + 1).append("] 《").append(sources.get(i).getTitle())
                        .append("》\n").append(sources.get(i).getSnippet()).append("\n\n");
            }
        }
        sb.append("\n【当前台账异常/关注提示】\n");
        summary.getHints().forEach(h -> sb.append("· ").append(h).append('\n'));
        return sb.toString();
    }
}
