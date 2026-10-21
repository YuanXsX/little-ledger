package com.sme.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 恢复操作日志（恢复人、备份点、恢复时间）。
 */
@Entity
@Table(name = "sys_restore_log")
public class RestoreLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 恢复人 */
    @Column(name = "operator", length = 64, nullable = false)
    private String operator;

    /** 备份点 ID */
    @Column(name = "backup_id")
    private Long backupId;

    /** 备份点时间 */
    @Column(name = "backup_time")
    private LocalDateTime backupTime;

    /** 恢复时间 */
    @Column(name = "restored_at")
    private LocalDateTime restoredAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Long getBackupId() {
        return backupId;
    }

    public void setBackupId(Long backupId) {
        this.backupId = backupId;
    }

    public LocalDateTime getBackupTime() {
        return backupTime;
    }

    public void setBackupTime(LocalDateTime backupTime) {
        this.backupTime = backupTime;
    }

    public LocalDateTime getRestoredAt() {
        return restoredAt;
    }

    public void setRestoredAt(LocalDateTime restoredAt) {
        this.restoredAt = restoredAt;
    }
}
