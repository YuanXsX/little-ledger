package com.sme.ledger.task;

import com.sme.ledger.entity.BackupLog;
import com.sme.ledger.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自动备份定时任务：每天凌晨 2 点全量备份。
 * 备份频率配置存储于 sys_backup_config（演示版固定每日调度，可按需改为动态调度）。
 */
@Component
public class BackupTask {

    private static final Logger log = LoggerFactory.getLogger(BackupTask.class);

    private final BackupService backupService;

    public BackupTask(BackupService backupService) {
        this.backupService = backupService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void autoBackup() {
        BackupLog record = backupService.backup("auto");
        log.info("自动备份完成：status={}, file={}", record.getStatus(), record.getFilePath());
    }
}
