package com.sme.ledger.repository;

import com.sme.ledger.entity.UpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UpdateLogRepository extends JpaRepository<UpdateLog, Long> {

    /** 查询某业务对象的更新日志 */
    List<UpdateLog> findByBizTypeAndBizIdOrderByCreatedAtDesc(String bizType, Long bizId);
}
