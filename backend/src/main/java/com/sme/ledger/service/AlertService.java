package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.AlertRuleRequest;
import com.sme.ledger.entity.Alert;
import com.sme.ledger.entity.AlertRule;
import com.sme.ledger.entity.InventoryLedger;
import com.sme.ledger.repository.AlertRepository;
import com.sme.ledger.repository.AlertRuleRepository;
import com.sme.ledger.repository.InventoryLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 异常预警：库存不足检测（去重）、多方式通知（短信/邮件为演示模拟）、
 * 预警处理跟踪、规则配置、未处理重复提醒。
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final InventoryLedgerRepository inventoryRepository;

    public AlertService(AlertRepository alertRepository,
                        AlertRuleRepository alertRuleRepository,
                        InventoryLedgerRepository inventoryRepository) {
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Alert> listAlerts(String status) {
        if (status != null && !status.isBlank()) {
            return alertRepository.findByStatusOrderByIdDesc(status);
        }
        return alertRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** 消息中心：未处理预警。 */
    public List<Alert> unresolvedMessages() {
        return alertRepository.findByStatusOrderByIdDesc(Alert.STATUS_UNRESOLVED);
    }

    @Transactional
    public Alert resolve(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预警不存在"));
        alert.setStatus(Alert.STATUS_RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    public List<AlertRule> listRules() {
        return alertRuleRepository.findAll(Sort.by("id"));
    }

    public AlertRule createRule(AlertRuleRequest request) {
        AlertRule rule = new AlertRule();
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(request.getRuleType());
        rule.setTarget(request.getTarget());
        rule.setThreshold(request.getThreshold());
        rule.setNotifyWay(request.getNotifyWay());
        rule.setEnabled(request.isEnabled());
        return alertRuleRepository.save(rule);
    }

    /** 库存不足检测：库存 <= 最低阈值时触发预警（未解决去重），返回新增预警数。 */
    @Transactional
    public int checkStockAlerts() {
        String notifyWay = defaultNotifyWay();
        int count = 0;
        for (InventoryLedger inv : inventoryRepository.findAll()) {
            if (inv.getStockQty() == null || inv.getMinStock() == null || inv.getStockQty() > inv.getMinStock()) {
                continue;
            }
            String target = inv.getProductName();
            boolean exists = alertRepository.existsByAlertTypeAndTargetAndStatus(
                    "库存不足", target, Alert.STATUS_UNRESOLVED);
            if (exists) {
                continue;
            }
            Alert alert = new Alert();
            alert.setAlertType("库存不足");
            alert.setTarget(target);
            alert.setCurrentVal("库存 " + inv.getStockQty() + " / 阈值 " + inv.getMinStock());
            alert.setStatus(Alert.STATUS_UNRESOLVED);
            alert.setCreatedAt(LocalDateTime.now());
            alertRepository.save(alert);
            notify(alert, notifyWay);
            count++;
        }
        return count;
    }

    /** 未处理预警重复提醒（定时任务调用），返回提醒条数。 */
    @Transactional
    public int remindUnresolved() {
        List<Alert> unresolved = alertRepository.findByStatusOrderByIdDesc(Alert.STATUS_UNRESOLVED);
        for (Alert alert : unresolved) {
            notify(alert, defaultNotifyWay());
            alert.setRemindTime(LocalDateTime.now());
            alertRepository.save(alert);
        }
        return unresolved.size();
    }

    private String defaultNotifyWay() {
        return alertRuleRepository.findByRuleType("库存不足").stream().findFirst()
                .map(AlertRule::getNotifyWay)
                .orElse("消息");
    }

    /** 多方式通知：短信/邮件为演示模拟（打印日志），消息即系统内预警记录本身。 */
    private void notify(Alert alert, String notifyWay) {
        String way = notifyWay == null || notifyWay.isBlank() ? "消息" : notifyWay;
        String content = "预警类型=" + alert.getAlertType() + ", 对象=" + alert.getTarget()
                + ", 当前值=" + alert.getCurrentVal();
        if (way.contains("短信")) {
            log.info("[模拟短信] {}", content);
        }
        if (way.contains("邮件")) {
            log.info("[模拟邮件] {}", content);
        }
        if (way.contains("消息")) {
            log.info("[系统消息] {}", content);
        }
    }
}
