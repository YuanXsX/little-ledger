package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.dto.LedgerCategoryRequest;
import com.sme.ledger.dto.LedgerFieldRequest;
import com.sme.ledger.entity.LedgerCategory;
import com.sme.ledger.entity.LedgerField;
import com.sme.ledger.service.LedgerCategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 台账分类与自定义字段接口。
 */
@RestController
@RequestMapping("/api/ledger-categories")
public class LedgerCategoryController {

    private final LedgerCategoryService service;

    public LedgerCategoryController(LedgerCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResult<List<LedgerCategory>> list() {
        return ApiResult.ok(service.listCategories());
    }

    @PostMapping
    public ApiResult<LedgerCategory> create(@Valid @RequestBody LedgerCategoryRequest request) {
        return ApiResult.ok(service.createCategory(request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        service.deleteCategory(id);
        return ApiResult.ok();
    }

    @GetMapping("/{id}/fields")
    public ApiResult<List<LedgerField>> fields(@PathVariable Long id) {
        return ApiResult.ok(service.listFields(id));
    }

    @PostMapping("/{id}/fields")
    public ApiResult<LedgerField> addField(@PathVariable Long id,
                                           @Valid @RequestBody LedgerFieldRequest request) {
        request.setCategoryId(id);
        return ApiResult.ok(service.createField(request));
    }

    @DeleteMapping("/fields/{fieldId}")
    public ApiResult<Void> deleteField(@PathVariable Long fieldId) {
        service.deleteField(fieldId);
        return ApiResult.ok();
    }
}
