package com.sme.ledger.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 自定义字段新增入参。
 * categoryId 由路径变量 /{id}/fields 注入，不参与 @Valid 校验。
 */
public class LedgerFieldRequest {

    private Long categoryId;

    @NotBlank(message = "字段标识不能为空")
    private String fieldKey;

    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    private String fieldType = "string";

    private boolean required;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
