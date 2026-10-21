package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.CustomerLedgerRequest;
import com.sme.ledger.entity.CustomerLedger;
import com.sme.ledger.repository.CustomerLedgerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 客户信息台账业务逻辑。
 */
@Service
public class CustomerService {

    private final CustomerLedgerRepository repository;

    public CustomerService(CustomerLedgerRepository repository) {
        this.repository = repository;
    }

    public PageResult<CustomerLedger> list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<CustomerLedger> result = repository.findAll(pageable);
        return PageResult.of(result);
    }

    public CustomerLedger getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在"));
    }

    public CustomerLedger create(CustomerLedgerRequest request) {
        if (repository.findByCustomerName(request.getCustomerName()).isPresent()) {
            throw new BusinessException("客户名称已存在，请勿重复录入");
        }
        validatePhone(request.getPhone());
        CustomerLedger entity = new CustomerLedger();
        apply(entity, request);
        entity.setCreatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    public CustomerLedger update(Long id, CustomerLedgerRequest request) {
        CustomerLedger entity = getById(id);
        validatePhone(request.getPhone());
        apply(entity, request);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void apply(CustomerLedger entity, CustomerLedgerRequest request) {
        entity.setCustomerName(request.getCustomerName());
        entity.setContactPerson(request.getContactPerson());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setIndustry(request.getIndustry());
        entity.setCoopStartDate(request.getCoopStartDate());
        entity.setAddress(request.getAddress());
        entity.setCoopLevel(request.getCoopLevel());
        entity.setRemark(request.getRemark());
        entity.setCreatedBy(request.getCreatedBy());
    }

    /** 手机号格式校验：空值或脱敏值(含*)跳过，否则需 11 位数字 */
    private void validatePhone(String phone) {
        if (phone == null || phone.isBlank() || phone.contains("*")) {
            return;
        }
        if (!phone.matches("\\d{11}")) {
            throw new BusinessException("手机号需为 11 位数字");
        }
    }
}
