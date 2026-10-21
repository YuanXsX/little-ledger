package com.sme.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 备份策略配置（单行）：自动备份频率与存储位置。
 */
@Entity
@Table(name = "sys_backup_config")
public class BackupConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 备份频率：daily / hourly / weekly */
    @Column(name = "frequency", length = 16, nullable = false)
    private String frequency = "daily";

    /** 存储位置：local / oss / dual */
    @Column(name = "storage", length = 16, nullable = false)
    private String storage = "local";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
