package com.sme.ledger.dto;

import java.util.List;

/**
 * 批量导入结果汇总。
 */
public record ImportResult(int total, int success, int failed, List<RowError> errors) {
}
