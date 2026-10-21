package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.entity.SysPermission;
import com.sme.ledger.repository.SysPermissionRepository;
import com.sme.ledger.security.RequirePermission;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限点列表接口（供角色权限矩阵配置使用）。
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final SysPermissionRepository permissionRepository;

    public PermissionController(SysPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    @RequirePermission("role:view")
    public ApiResult<List<SysPermission>> list() {
        return ApiResult.ok(permissionRepository.findAll(Sort.by("id")));
    }
}
