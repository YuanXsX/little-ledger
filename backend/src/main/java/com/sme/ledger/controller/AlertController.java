package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.entity.Alert;
import com.sme.ledger.security.RequirePermission;
import com.sme.ledger.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 预警记录接口。
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @RequirePermission("alert:view")
    public ApiResult<List<Alert>> list(@RequestParam(required = false) String status) {
        return ApiResult.ok(alertService.listAlerts(status));
    }

    /** 标记预警已解决。 */
    @PutMapping("/{id}/resolve")
    @RequirePermission("alert:resolve")
    public ApiResult<Alert> resolve(@PathVariable Long id) {
        return ApiResult.ok(alertService.resolve(id));
    }

    /** 手动触发库存不足检查。 */
    @PostMapping("/check")
    @RequirePermission("alert:config")
    public ApiResult<Map<String, Integer>> checkStock() {
        int count = alertService.checkStockAlerts();
        return ApiResult.ok(Map.of("createdAlerts", count));
    }
}
