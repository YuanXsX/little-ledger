package com.sme.ledger.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 台账分类新增入参（自定义分类）。
 */
public class LedgerCategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
