package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.dto.AlertRuleRequest;
import com.sme.ledger.entity.AlertRule;
import com.sme.ledger.security.RequirePermission;
import com.sme.ledger.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 预警规则接口。
 */
@RestController
@RequestMapping("/api/alert-rules")
public class AlertRuleController {

    private final AlertService alertService;

    public AlertRuleController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @RequirePermission("alert:config")
    public ApiResult<List<AlertRule>> list() {
        return ApiResult.ok(alertService.listRules());
    }

    @PostMapping
    @RequirePermission("alert:config")
    public ApiResult<AlertRule> create(@Valid @RequestBody AlertRuleRequest request) {
        return ApiResult.ok(alertService.createRule(request));
    }
}
