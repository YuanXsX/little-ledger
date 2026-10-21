package com.sme.ledger.repository;

import com.sme.ledger.entity.RestoreLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestoreLogRepository extends JpaRepository<RestoreLog, Long> {
}
