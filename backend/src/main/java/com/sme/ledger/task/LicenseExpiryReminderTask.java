package com.sme.ledger.task;

import com.sme.ledger.service.EnterpriseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 资质到期提醒定时任务：每天检查，到期前 15 天生成预警。
 */
@Component
public class LicenseExpiryReminderTask {

    private static final Logger log = LoggerFactory.getLogger(LicenseExpiryReminderTask.class);

    private final EnterpriseService enterpriseService;

    public LicenseExpiryReminderTask(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkExpiry() {
        int count = enterpriseService.checkAndAlertExpiry(15);
        if (count > 0) {
            log.info("资质到期提醒：新增 {} 条预警", count);
        }
    }
}
