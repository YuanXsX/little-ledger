package com.sme.ledger.repository;

import com.sme.ledger.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    /** 判断某对象某类型是否已有未解决预警，避免重复提醒 */
    boolean existsByAlertTypeAndTargetAndStatus(String alertType, String target, String status);

    List<Alert> findByStatusOrderByIdDesc(String status);
}
