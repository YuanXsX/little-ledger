package com.sme.ledger.repository;

import com.sme.ledger.entity.BackupConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupConfigRepository extends JpaRepository<BackupConfig, Long> {
}
