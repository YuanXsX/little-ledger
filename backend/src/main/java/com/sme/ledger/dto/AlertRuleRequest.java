package com.sme.ledger.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 预警规则入参。
 */
public class AlertRuleRequest {

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "预警类型不能为空")
    private String ruleType;

    private String target;

    private String threshold;

    /** 通知方式：消息 / 短信 / 邮件（逗号分隔） */
    private String notifyWay = "消息";

    private boolean enabled = true;

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
