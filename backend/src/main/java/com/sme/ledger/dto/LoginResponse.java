package com.sme.ledger.dto;

/**
 * 登录成功返回：JWT token + 用户信息。
 */
public record LoginResponse(String token, UserInfo user) {
}
