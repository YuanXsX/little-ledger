package com.sme.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 企业资质信息入参 DTO，演示参数校验。
 */
public class EnterpriseInfoRequest {

    @NotBlank(message = "营业执照编号不能为空")
    private String licenseNo;

    private String businessLicense;

    private String businessScope;

    private String registeredAddress;

    @NotNull(message = "成立日期不能为空")
    private LocalDate establishDate;

    private String licenseFile;

    private LocalDate licenseExpireDate;

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
}
