package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查接口，用于联调时确认服务已启动。
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/ping")
    public ApiResult<Map<String, Object>> ping() {
        return ApiResult.ok(Map.of("status", "up", "service", "ledger-backend"));
    }
}
