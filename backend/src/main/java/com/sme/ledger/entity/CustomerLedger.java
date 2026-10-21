package com.sme.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户信息台账。
 */
@Entity
@Table(name = "biz_customer_ledger")
public class CustomerLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", length = 128, nullable = false)
    private String customerName;

    @Column(name = "contact_person", length = 64)
    private String contactPerson;

    @Column(name = "phone", length = 16)
    private String phone;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "industry", length = 64)
    private String industry;

    @Column(name = "coop_start_date")
    private LocalDate coopStartDate;

    /** 累计合作金额（由财务流水关联自动更新） */
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** 应收账款金额（由财务流水关联自动更新） */
    @Column(name = "receivable", precision = 18, scale = 2)
    private BigDecimal receivable = BigDecimal.ZERO;

    @Column(name = "last_coop_time")
    private LocalDateTime lastCoopTime;

    @Column(name = "address", length = 200)
    private String address;

    /** 合作等级 A/B/C */
    @Column(name = "coop_level", length = 4)
    private String coopLevel;

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public LocalDate getCoopStartDate() {
        return coopStartDate;
    }

    public void setCoopStartDate(LocalDate coopStartDate) {
        this.coopStartDate = coopStartDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReceivable() {
        return receivable;
    }

    public void setReceivable(BigDecimal receivable) {
        this.receivable = receivable;
    }

    public LocalDateTime getLastCoopTime() {
        return lastCoopTime;
    }

    public void setLastCoopTime(LocalDateTime lastCoopTime) {
        this.lastCoopTime = lastCoopTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCoopLevel() {
        return coopLevel;
    }

    public void setCoopLevel(String coopLevel) {
        this.coopLevel = coopLevel;
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
