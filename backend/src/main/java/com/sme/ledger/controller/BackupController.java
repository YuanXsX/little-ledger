package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.entity.BackupConfig;
import com.sme.ledger.entity.BackupLog;
import com.sme.ledger.entity.RestoreLog;
import com.sme.ledger.security.RequirePermission;
import com.sme.ledger.service.BackupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据备份与恢复接口。
 */
@RestController
@RequestMapping("/api/backups")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping
    @RequirePermission("backup:view")
    public ApiResult<List<BackupLog>> list() {
        return ApiResult.ok(backupService.listBackups());
    }

    /** 手动触发备份。 */
    @PostMapping
    @RequirePermission("backup:create")
    public ApiResult<BackupLog> backup() {
        return ApiResult.ok(backupService.backup("manual"));
    }

    @GetMapping("/config")
    @RequirePermission("backup:view")
    public ApiResult<BackupConfig> config() {
        return ApiResult.ok(backupService.getConfig());
    }

    /** 配置备份频率与存储位置。 */
    @PutMapping("/config")
    @RequirePermission("backup:config")
    public ApiResult<BackupConfig> updateConfig(@RequestParam(required = false) String frequency,
                                                @RequestParam(required = false) String storage) {
        return ApiResult.ok(backupService.updateConfig(frequency, storage));
    }

    @GetMapping("/restores")
    @RequirePermission("backup:view")
    public ApiResult<List<RestoreLog>> restores() {
        return ApiResult.ok(backupService.listRestores());
    }

    /** 定点恢复：恢复到指定备份点。 */
    @PostMapping("/restore/{id}")
    @RequirePermission("backup:restore")
    public ApiResult<RestoreLog> restore(@PathVariable Long id, @RequestParam String operator) {
        return ApiResult.ok(backupService.restore(id, operator));
    }

    /** 快速恢复：恢复到最近一次成功备份。 */
    @PostMapping("/restore/latest")
    @RequirePermission("backup:restore")
    public ApiResult<RestoreLog> restoreLatest(@RequestParam String operator) {
        return ApiResult.ok(backupService.restoreLatest(operator));
    }
}
