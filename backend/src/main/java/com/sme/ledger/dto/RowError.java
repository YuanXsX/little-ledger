package com.sme.ledger.dto;

/**
 * 批量导入的单行错误。
 */
public record RowError(int row, String message) {
}
