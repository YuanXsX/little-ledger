package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.dto.AiChatRequest;
import com.sme.ledger.dto.AiChatResponse;
import com.sme.ledger.dto.LedgerSummaryResponse;
import com.sme.ledger.entity.KnowledgeDoc;
import com.sme.ledger.security.RequirePermission;
import com.sme.ledger.service.AiAssistantService;
import com.sme.ledger.service.KnowledgeDocService;
import com.sme.ledger.service.LedgerSummaryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 财税 RAG 智能助手接口（M8）。
 * 接口路径遵循《04-接口设计》2.8 节约定。
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final AiAssistantService aiAssistantService;
    private final KnowledgeDocService knowledgeDocService;
    private final LedgerSummaryService ledgerSummaryService;

    public RagController(AiAssistantService aiAssistantService,
                         KnowledgeDocService knowledgeDocService,
                         LedgerSummaryService ledgerSummaryService) {
        this.aiAssistantService = aiAssistantService;
        this.knowledgeDocService = knowledgeDocService;
        this.ledgerSummaryService = ledgerSummaryService;
    }

    /** AI 问答（JSON 一次性返回；流式为预留能力，超时/异常自动降级） */
    @PostMapping("/chat")
    @RequirePermission("assistant:ask")
    public ApiResult<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ApiResult.ok(aiAssistantService.chat(request.getQuestion()));
    }

    /** 台账统计摘要（异常提示数据源） */
    @GetMapping("/summary")
    @RequirePermission("assistant:ask")
    public ApiResult<LedgerSummaryResponse> summary() {
        return ApiResult.ok(ledgerSummaryService.build());
    }

    /** 知识库文档列表 */
    @GetMapping("/documents")
    @RequirePermission("assistant:doc")
    public ApiResult<List<KnowledgeDoc>> documents() {
        return ApiResult.ok(knowledgeDocService.list());
    }

    /** 上传知识库文档（txt/md/docx/pdf），解析成功后可供问答检索 */
    @PostMapping("/documents")
    @RequirePermission("assistant:doc")
    public ApiResult<KnowledgeDoc> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam(required = false) String title,
                                          HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return ApiResult.ok(knowledgeDocService.upload(file, title, username));
    }

    /** 删除知识库文档及其分块 */
    @DeleteMapping("/documents/{id}")
    @RequirePermission("assistant:doc")
    public ApiResult<Void> delete(@PathVariable Long id) {
        knowledgeDocService.delete(id);
        return ApiResult.ok();
    }
}
