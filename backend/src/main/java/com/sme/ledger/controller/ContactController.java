package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.ContactRequest;
import com.sme.ledger.dto.ContactResponse;
import com.sme.ledger.entity.UpdateLog;
import com.sme.ledger.service.ContactService;
import com.sme.ledger.security.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 联系人信息接口。
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResult<PageResult<ContactResponse>> list(
            @RequestParam(required = false) String contactType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(service.list(contactType, page, size));
    }

    @GetMapping("/{id}")
    public ApiResult<ContactResponse> detail(@PathVariable Long id) {
        return ApiResult.ok(service.getById(id));
    }

    @PostMapping
    public ApiResult<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        return ApiResult.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<ContactResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request,
            @RequestParam String operator,
            @RequestParam(required = false) String reason) {
        return ApiResult.ok(service.update(id, request, operator, reason));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("contact:delete")
    public ApiResult<Void> delete(
            @PathVariable Long id,
            @RequestParam String operator) {
        service.delete(id, operator);
        return ApiResult.ok();
    }

    @GetMapping("/{id}/logs")
    public ApiResult<List<UpdateLog>> logs(@PathVariable Long id) {
        return ApiResult.ok(service.logs(id));
    }
}
