package com.sme.ledger.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 联系人新增/更新入参。
 * 注意：手机号/邮箱的格式校验在 Service 层进行，因为编辑时前端会回传脱敏值(含 *)，
 * 需跳过对脱敏值的格式校验（见 ContactService）。
 */
public class ContactRequest {

    @NotBlank(message = "联系人类型不能为空")
    private String contactType;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String phone;

    private String email;

    private String company;

    private String position;

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
