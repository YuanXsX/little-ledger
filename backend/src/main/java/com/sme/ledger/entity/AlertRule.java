package com.sme.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 预警规则（异常预警模块）：可配置的预警场景与通知方式。
 */
@Entity
@Table(name = "sys_alert_rule")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", length = 64, nullable = false)
    private String ruleName;

    /** 预警类型，如 库存不足 / 资质到期 */
    @Column(name = "rule_type", length = 32, nullable = false)
    private String ruleType;

    /** 涉及对象（如 SKU，空表示全部） */
    @Column(name = "target", length = 64)
    private String target;

    /** 阈值 */
    @Column(name = "threshold", length = 64)
    private String threshold;

    /** 通知方式：消息 / 短信 / 邮件（逗号分隔） */
    @Column(name = "notify_way", length = 64)
    private String notifyWay = "消息";

    /** 是否启用 */
    @Column(name = "enabled")
    private boolean enabled = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public String getNotifyWay() {
        return notifyWay;
    }

    public void setNotifyWay(String notifyWay) {
        this.notifyWay = notifyWay;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
