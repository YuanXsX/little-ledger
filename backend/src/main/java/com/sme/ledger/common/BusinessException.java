package com.sme.ledger.common;

/**
 * 业务异常，用于在 service 层主动抛出可预期的错误（如重复数据、越权等）。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
