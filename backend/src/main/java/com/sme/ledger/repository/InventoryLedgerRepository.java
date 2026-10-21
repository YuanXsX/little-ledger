package com.sme.ledger.repository;

import com.sme.ledger.entity.InventoryLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryLedgerRepository extends JpaRepository<InventoryLedger, Long> {

    Optional<InventoryLedger> findBySku(String sku);
}
