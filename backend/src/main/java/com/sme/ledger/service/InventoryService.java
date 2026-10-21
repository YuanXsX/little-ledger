package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.InventoryLedgerRequest;
import com.sme.ledger.entity.InventoryLedger;
import com.sme.ledger.repository.InventoryLedgerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 库存明细业务逻辑，含「客户↔库存」跨台账关联（常采购商品优先级）。
 */
@Service
public class InventoryService {

    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_NORMAL = "NORMAL";

    private final InventoryLedgerRepository repository;

    public InventoryService(InventoryLedgerRepository repository) {
        this.repository = repository;
    }

    public PageResult<InventoryLedger> list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<InventoryLedger> result = repository.findAll(pageable);
        return PageResult.of(result);
    }

    public InventoryLedger getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("库存商品不存在"));
    }

    public InventoryLedger create(InventoryLedgerRequest request) {
        if (repository.findBySku(request.getSku()).isPresent()) {
            throw new BusinessException("SKU 已存在，请勿重复录入");
        }
        InventoryLedger entity = new InventoryLedger();
        apply(entity, request);
        entity.setPriority(PRIORITY_NORMAL);
        entity.setCreatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    public InventoryLedger update(Long id, InventoryLedgerRequest request) {
        InventoryLedger entity = getById(id);
        apply(entity, request);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void apply(InventoryLedger entity, InventoryLedgerRequest request) {
        entity.setSku(request.getSku());
        entity.setProductName(request.getProductName());
        entity.setSpec(request.getSpec());
        entity.setUnit(request.getUnit());
        entity.setStockQty(request.getStockQty());
        entity.setMinStock(request.getMinStock());
        entity.setSupplier(request.getSupplier());
        entity.setRemark(request.getRemark());
        entity.setCreatedBy(request.getCreatedBy());
    }

    /**
     * 客户↔库存关联：将某 SKU 标记为常采购商品（库存优先级 HIGH）。
     * 完整自动统计（按该客户出库记录计数）在 M3 数据录入阶段实现。
     */
    public InventoryLedger markFrequent(String sku, String customerName) {
        InventoryLedger entity = repository.findBySku(sku)
                .orElseThrow(() -> new BusinessException("SKU 不存在：" + sku));
        entity.setPriority(PRIORITY_HIGH);
        entity.setRemark((entity.getRemark() == null ? "" : entity.getRemark() + "；")
                + "客户[" + customerName + "]常采购，优先补货");
        return repository.save(entity);
    }
}
