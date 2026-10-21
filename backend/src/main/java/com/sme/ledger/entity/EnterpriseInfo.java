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
 * 企业资质备案信息（企业基础信息模块）。
 */
@Entity
@Table(name = "biz_enterprise_info")
public class EnterpriseInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 营业执照编号 */
    @Column(name = "license_no", length = 64)
    private String licenseNo;

    /** 经营许可证 */
    @Column(name = "business_license", length = 64)
    private String businessLicense;

    /** 经营范围 */
    @Column(name = "business_scope", length = 500)
    private String businessScope;

    /** 注册地址 */
    @Column(name = "registered_address", length = 200)
    private String registeredAddress;

    /** 成立日期 */
    @Column(name = "establish_date")
    private LocalDate establishDate;

    /** 资质文件扫描件路径（仅支持 JPG/PDF） */
    @Column(name = "license_file", length = 255)
    private String licenseFile;

    /** 资质有效期截止日（到期前 15 天触发提醒） */
    @Column(name = "license_expire_date")
    private LocalDate licenseExpireDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }

    public String getBusinessScope() {
        return businessScope;
    }

    public void setBusinessScope(String businessScope) {
        this.businessScope = businessScope;
    }

    public String getRegisteredAddress() {
        return registeredAddress;
    }

    public void setRegisteredAddress(String registeredAddress) {
        this.registeredAddress = registeredAddress;
    }

    public LocalDate getEstablishDate() {
        return establishDate;
    }

    public void setEstablishDate(LocalDate establishDate) {
        this.establishDate = establishDate;
    }

    public String getLicenseFile() {
        return licenseFile;
    }

    public void setLicenseFile(String licenseFile) {
        this.licenseFile = licenseFile;
    }

    public LocalDate getLicenseExpireDate() {
        return licenseExpireDate;
    }

    public void setLicenseExpireDate(LocalDate licenseExpireDate) {
        this.licenseExpireDate = licenseExpireDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
