package com.sme.ledger.task;

import com.sme.ledger.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 异常预警定时任务：定时检查库存不足；未处理预警每日重复提醒。
 */
@Component
public class StockAlertTask {

    private static final Logger log = LoggerFactory.getLogger(StockAlertTask.class);

    private final AlertService alertService;

    public StockAlertTask(AlertService alertService) {
        this.alertService = alertService;
    }

    /** 每小时第 30 分检查库存不足。 */
    @Scheduled(cron = "0 30 * * * ?")
    public void checkStock() {
        int count = alertService.checkStockAlerts();
        if (count > 0) {
            log.info("库存不足预警：新增 {} 条", count);
        }
    }

    /** 每天 10 点重复提醒未处理预警，直至标记解决。 */
    @Scheduled(cron = "0 0 10 * * ?")
    public void remind() {
        int count = alertService.remindUnresolved();
        log.info("重复提醒未处理预警 {} 条", count);
    }
}
