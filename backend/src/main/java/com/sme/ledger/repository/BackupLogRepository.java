package com.sme.ledger.repository;

import com.sme.ledger.entity.BackupLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackupLogRepository extends JpaRepository<BackupLog, Long> {

    /** 最近一次成功备份（用于快速恢复） */
    Optional<BackupLog> findFirstByStatusOrderByIdDesc(String status);
}
