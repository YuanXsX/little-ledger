package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.FinanceLedgerRequest;
import com.sme.ledger.entity.FinanceLedger;
import com.sme.ledger.service.FinanceService;
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
 * 财务流水台账接口。
 */
@RestController
@RequestMapping("/api/finance-ledger")
public class FinanceController {

    private final FinanceService service;

    public FinanceController(FinanceService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResult<PageResult<FinanceLedger>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(service.list(page, size));
    }

    @GetMapping("/{id}")
    public ApiResult<FinanceLedger> detail(@PathVariable Long id) {
        return ApiResult.ok(service.getById(id));
    }

    @PostMapping
    public ApiResult<FinanceLedger> create(@Valid @RequestBody FinanceLedgerRequest request) {
        return ApiResult.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<FinanceLedger> update(@PathVariable Long id,
                                           @Valid @RequestBody FinanceLedgerRequest request) {
        return ApiResult.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("finance:delete")
    public ApiResult<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResult.ok();
    }
}
