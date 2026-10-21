package com.sme.ledger.repository;

import com.sme.ledger.entity.FinanceLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FinanceLedgerRepository extends JpaRepository<FinanceLedger, Long> {

    /** 财务流水重复校验：凭证号 + 金额 + 日期 */
    boolean existsByVoucherNoAndAmountAndOccurDate(String voucherNo, BigDecimal amount, LocalDate occurDate);
}
