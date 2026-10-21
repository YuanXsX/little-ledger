package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.entity.Alert;
import com.sme.ledger.security.RequirePermission;
import com.sme.ledger.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统内消息（消息中心）：未处理预警记录。
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final AlertService alertService;

    public MessageController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @RequirePermission("alert:view")
    public ApiResult<List<Alert>> list() {
        return ApiResult.ok(alertService.unresolvedMessages());
    }
}
