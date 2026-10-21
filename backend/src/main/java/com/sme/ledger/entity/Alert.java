package com.sme.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 预警记录（资质到期、库存不足等）。
 */
@Entity
@Table(name = "sys_alert")
public class Alert {

    public static final String STATUS_UNRESOLVED = "UNRESOLVED";
    public static final String STATUS_RESOLVED = "RESOLVED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "alert_type", length = 32, nullable = false)
    private String alertType;

    @Column(name = "target", length = 128)
    private String target;

    @Column(name = "current_val", length = 64)
    private String currentVal;

    @Column(name = "status", length = 16, nullable = false)
    private String status = STATUS_UNRESOLVED;

    @Column(name = "remind_time")
    private LocalDateTime remindTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCurrentVal() {
        return currentVal;
    }

    public void setCurrentVal(String currentVal) {
        this.currentVal = currentVal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(LocalDateTime remindTime) {
        this.remindTime = remindTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
