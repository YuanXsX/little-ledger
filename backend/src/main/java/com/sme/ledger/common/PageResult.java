package com.sme.ledger.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页返回结构。
 */
public record PageResult<T>(List<T> list, long total, int page, int size) {

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements(), page.getNumber() + 1, page.getSize());
    }
}
