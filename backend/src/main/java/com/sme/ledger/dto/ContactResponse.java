package com.sme.ledger.dto;

import com.sme.ledger.common.MaskUtil;
import com.sme.ledger.entity.Contact;

import java.time.LocalDateTime;

/**
 * 联系人出参（手机号/邮箱脱敏）。
 */
public record ContactResponse(
        Long id,
        String contactType,
        String name,
        String phone,
        String email,
        String company,
        String position,
        LocalDateTime createdAt
) {

    /** 由实体转换，敏感字段脱敏 */
    public static ContactResponse from(Contact c) {
        return new ContactResponse(
                c.getId(),
                c.getContactType(),
                c.getName(),
                MaskUtil.maskPhone(c.getPhone()),
                MaskUtil.maskEmail(c.getEmail()),
                c.getCompany(),
                c.getPosition(),
                c.getCreatedAt()
        );
    }
}
