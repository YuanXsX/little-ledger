package com.sme.ledger.repository;

import com.sme.ledger.entity.LedgerField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerFieldRepository extends JpaRepository<LedgerField, Long> {

    List<LedgerField> findByCategoryId(Long categoryId);
}
