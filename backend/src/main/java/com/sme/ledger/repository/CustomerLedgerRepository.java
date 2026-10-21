package com.sme.ledger.repository;

import com.sme.ledger.entity.CustomerLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerLedgerRepository extends JpaRepository<CustomerLedger, Long> {

    Optional<CustomerLedger> findByCustomerName(String customerName);
}
