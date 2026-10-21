package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.common.BusinessException;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.EnterpriseInfoRequest;
import com.sme.ledger.entity.EnterpriseInfo;
import com.sme.ledger.service.EnterpriseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 企业资质信息接口。
 */
@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    private final EnterpriseService service;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public EnterpriseController(EnterpriseService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResult<PageResult<EnterpriseInfo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(service.list(page, size));
    }

    @GetMapping("/{id}")
    public ApiResult<EnterpriseInfo> detail(@PathVariable Long id) {
        return ApiResult.ok(service.getById(id));
    }

    @PostMapping
    public ApiResult<EnterpriseInfo> create(@Valid @RequestBody EnterpriseInfoRequest request) {
        return ApiResult.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<EnterpriseInfo> update(
            @PathVariable Long id,
            @Valid @RequestBody EnterpriseInfoRequest request,
            @RequestParam String operator,
            @RequestParam(required = false) String reason) {
        return ApiResult.ok(service.update(id, request, operator, reason));
    }

    /** 上传资质扫描件，仅支持 JPG/PDF */
    @PostMapping("/{id}/file")
    public ApiResult<EnterpriseInfo> uploadFile(@PathVariable Long id,
                                                @RequestParam("file") MultipartFile file) {
        String original = file.getOriginalFilename();
        String lower = original == null ? "" : original.toLowerCase();
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".pdf"))) {
            throw new BusinessException("仅支持 JPG/PDF 格式文件");
        }
        String storedName = UUID.randomUUID() + lower.substring(lower.lastIndexOf('.'));
        // 使用绝对路径，避免 Tomcat 相对路径解析问题
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(storedName));
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败: " + e.getMessage());
        }
        EnterpriseInfo updated = service.updateLicenseFile(id, storedName);
        return ApiResult.ok(updated);
    }

    /** 查询未来 days 天内到期的资质 */
    @GetMapping("/expiring")
    public ApiResult<List<EnterpriseInfo>> expiring(@RequestParam(defaultValue = "15") int days) {
        return ApiResult.ok(service.findExpiring(days));
    }

    /** 手动触发到期检查（定时任务每日也会执行） */
    @PostMapping("/check-expiry")
    public ApiResult<Map<String, Integer>> checkExpiry(@RequestParam(defaultValue = "15") int days) {
        int count = service.checkAndAlertExpiry(days);
        return ApiResult.ok(Map.of("createdAlerts", count));
    }
}
