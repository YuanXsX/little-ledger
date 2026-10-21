package com.sme.ledger.common;

/**
 * 敏感信息脱敏工具。
 *
 * 规则：
 *  手机号 -> 前 3 位 + **** + 后 4 位（如 138****1234）
 *  邮箱   -> 首字母 + *** + 末字母 + @域名（如 w***g@kechuang.com）
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return email;
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }
}
