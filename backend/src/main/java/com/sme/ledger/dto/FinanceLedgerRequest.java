package com.sme.ledger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 财务流水录入入参。
 */
public class FinanceLedgerRequest {

    @NotBlank(message = "凭证号不能为空")
    private String voucherNo;

    @NotBlank(message = "收支类型不能为空")
    private String flowType;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额需为大于 0 的数字")
    @Digits(integer = 16, fraction = 2, message = "金额最多保留 2 位小数")
    private BigDecimal amount;

    @NotNull(message = "发生日期不能为空")
    private LocalDate occurDate;

    private String payMethod;

    /** 关联对象类型：客户 / 供应商 */
    private String relatedType;

    /** 关联对象名称 */
    private String relatedName;

    private String remark;

    private String createdBy;

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getOccurDate() {
        return occurDate;
    }

    public void setOccurDate(LocalDate occurDate) {
        this.occurDate = occurDate;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public String getRelatedName() {
        return relatedName;
    }

    public void setRelatedName(String relatedName) {
        this.relatedName = relatedName;
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
