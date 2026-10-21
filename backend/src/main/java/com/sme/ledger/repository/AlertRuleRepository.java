package com.sme.ledger.repository;

import com.sme.ledger.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    List<AlertRule> findByRuleType(String ruleType);
}
