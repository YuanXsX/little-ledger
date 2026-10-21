package com.sme.ledger.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * 客户信息录入入参。
 */
public class CustomerLedgerRequest {

    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    private String contactPerson;

    private String phone;

    private String email;

    private String industry;

    private LocalDate coopStartDate;

    private String address;

    private String coopLevel;

    private String remark;

    private String createdBy;

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
}
