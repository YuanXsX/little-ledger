package com.sme.ledger.repository;

import com.sme.ledger.entity.LedgerCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerCategoryRepository extends JpaRepository<LedgerCategory, Long> {

    Optional<LedgerCategory> findByName(String name);

    boolean existsByName(String name);
}
