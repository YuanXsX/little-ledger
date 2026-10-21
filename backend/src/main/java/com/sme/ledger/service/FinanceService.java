package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.FinanceLedgerRequest;
import com.sme.ledger.entity.CustomerLedger;
import com.sme.ledger.entity.FinanceLedger;
import com.sme.ledger.repository.CustomerLedgerRepository;
import com.sme.ledger.repository.FinanceLedgerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务流水业务逻辑：重复校验 + 格式校验 + 客户↔财务跨台账关联。
 */
@Service
public class FinanceService {

    private final FinanceLedgerRepository repository;
    private final CustomerLedgerRepository customerRepository;

    public FinanceService(FinanceLedgerRepository repository, CustomerLedgerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    public PageResult<FinanceLedger> list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<FinanceLedger> result = repository.findAll(pageable);
        return PageResult.of(result);
    }

    public FinanceLedger getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("财务流水不存在"));
    }

    @Transactional
    public FinanceLedger create(FinanceLedgerRequest request) {
        // 自动重复校验：凭证号 + 金额 + 日期
        if (repository.existsByVoucherNoAndAmountAndOccurDate(
                request.getVoucherNo(), request.getAmount(), request.getOccurDate())) {
            throw new BusinessException("该凭证号+金额+日期已存在，请勿重复录入");
        }
        FinanceLedger entity = new FinanceLedger();
        apply(entity, request);
        entity.setCreatedAt(LocalDateTime.now());
        FinanceLedger saved = repository.save(entity);

        // 跨台账关联：财务流水 → 客户（自动更新累计合作金额与应收账款）
        if ("客户".equals(request.getRelatedType()) && request.getRelatedName() != null) {
            updateCustomerAssociation(request);
        }
        return saved;
    }

    @Transactional
    public FinanceLedger update(Long id, FinanceLedgerRequest request) {
        FinanceLedger entity = getById(id);
        apply(entity, request);
        return repository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void apply(FinanceLedger entity, FinanceLedgerRequest request) {
        entity.setVoucherNo(request.getVoucherNo());
        entity.setFlowType(request.getFlowType());
        entity.setAmount(request.getAmount());
        entity.setOccurDate(request.getOccurDate());
        entity.setPayMethod(request.getPayMethod());
        entity.setRelatedType(request.getRelatedType());
        entity.setRelatedName(request.getRelatedName());
        entity.setRemark(request.getRemark());
        entity.setCreatedBy(request.getCreatedBy());
    }

    private void updateCustomerAssociation(FinanceLedgerRequest request) {
        CustomerLedger customer = customerRepository.findByCustomerName(request.getRelatedName())
                .orElseThrow(() -> new BusinessException("关联客户不存在：" + request.getRelatedName()));
        BigDecimal amount = request.getAmount();
        if ("收入".equals(request.getFlowType())) {
            customer.setTotalAmount(customer.getTotalAmount().add(amount));
            customer.setReceivable(customer.getReceivable().add(amount));
        } else if ("支出".equals(request.getFlowType())) {
            customer.setReceivable(customer.getReceivable().subtract(amount));
        }
        customer.setLastCoopTime(request.getOccurDate().atStartOfDay());
        customerRepository.save(customer);
    }
}
