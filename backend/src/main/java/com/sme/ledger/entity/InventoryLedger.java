package com.sme.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存明细台账。
 */
@Entity
@Table(name = "biz_inventory_ledger")
public class InventoryLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", length = 64, nullable = false)
    private String sku;

    @Column(name = "product_name", length = 128, nullable = false)
    private String productName;

    @Column(name = "spec", length = 128)
    private String spec;

    @Column(name = "unit", length = 16)
    private String unit;

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty = 0;

    @Column(name = "in_date")
    private LocalDate inDate;

    @Column(name = "in_qty")
    private Integer inQty;

    @Column(name = "out_date")
    private LocalDate outDate;

    @Column(name = "out_qty")
    private Integer outQty;

    @Column(name = "supplier", length = 128)
    private String supplier;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 0;

    /** 库存优先级：HIGH(常采购，优先补货) / NORMAL */
    @Column(name = "priority", length = 16)
    private String priority = "NORMAL";

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public LocalDate getInDate() {
        return inDate;
    }

    public void setInDate(LocalDate inDate) {
        this.inDate = inDate;
    }

    public Integer getInQty() {
        return inQty;
    }

    public void setInQty(Integer inQty) {
        this.inQty = inQty;
    }

    public LocalDate getOutDate() {
        return outDate;
    }

    public void setOutDate(LocalDate outDate) {
        this.outDate = outDate;
    }

    public Integer getOutQty() {
        return outQty;
    }

    public void setOutQty(Integer outQty) {
        this.outQty = outQty;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
