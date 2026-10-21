package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.InventoryLedgerRequest;
import com.sme.ledger.entity.InventoryLedger;
import com.sme.ledger.service.InventoryService;
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
 * 库存明细台账接口。
 */
@RestController
@RequestMapping("/api/inventory-ledger")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResult<PageResult<InventoryLedger>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(service.list(page, size));
    }

    @PostMapping
    public ApiResult<InventoryLedger> create(@Valid @RequestBody InventoryLedgerRequest request) {
        return ApiResult.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<InventoryLedger> update(@PathVariable Long id,
                                             @Valid @RequestBody InventoryLedgerRequest request) {
        return ApiResult.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("inventory:delete")
    public ApiResult<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResult.ok();
    }

    /** 客户↔库存关联：标记常采购商品优先级 */
    @PostMapping("/{sku}/mark-frequent")
    public ApiResult<InventoryLedger> markFrequent(@PathVariable String sku,
                                                   @RequestParam String customerName) {
        return ApiResult.ok(service.markFrequent(sku, customerName));
    }
}
