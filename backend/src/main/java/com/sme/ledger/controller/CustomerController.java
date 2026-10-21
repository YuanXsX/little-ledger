package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.CustomerLedgerRequest;
import com.sme.ledger.entity.CustomerLedger;
import com.sme.ledger.service.CustomerService;
import com.sme.ledger.security.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户信息台账接口。
 */
@RestController
@RequestMapping("/api/customer-ledger")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResult<PageResult<CustomerLedger>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(service.list(page, size));
    }

    @GetMapping("/{id}")
    public ApiResult<CustomerLedger> detail(@PathVariable Long id) {
        return ApiResult.ok(service.getById(id));
    }

    @PostMapping
    public ApiResult<CustomerLedger> create(@Valid @RequestBody CustomerLedgerRequest request) {
        return ApiResult.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<CustomerLedger> update(@PathVariable Long id,
                                            @Valid @RequestBody CustomerLedgerRequest request) {
        return ApiResult.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("customer:delete")
    public ApiResult<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResult.ok();
    }
}
