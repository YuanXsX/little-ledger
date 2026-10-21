package com.sme.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 备份记录（数据备份与恢复模块）。
 */
@Entity
@Table(name = "sys_backup_log")
public class BackupLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 备份类型：auto 自动 / manual 手动 */
    @Column(name = "backup_type", length = 32, nullable = false)
    private String backupType;

    /** 存储位置：local / oss / dual */
    @Column(name = "storage", length = 32, nullable = false)
    private String storage;

    @Column(name = "file_path", length = 255)
    private String filePath;

    /** 状态：success / failed */
    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "backup_time")
    private LocalDateTime backupTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBackupType() {
        return backupType;
    }

    public void setBackupType(String backupType) {
        this.backupType = backupType;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getBackupTime() {
        return backupTime;
    }

    public void setBackupTime(LocalDateTime backupTime) {
        this.backupTime = backupTime;
    }
}
