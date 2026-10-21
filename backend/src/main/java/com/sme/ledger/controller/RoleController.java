package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.dto.RoleRequest;
import com.sme.ledger.entity.SysRole;
import com.sme.ledger.security.RequirePermission;
import com.sme.ledger.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口。
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @RequirePermission("role:view")
    public ApiResult<List<SysRole>> list() {
        return ApiResult.ok(roleService.list());
    }

    @PostMapping
    @RequirePermission("role:create")
    public ApiResult<SysRole> create(@Valid @RequestBody RoleRequest request) {
        return ApiResult.ok(roleService.create(request));
    }

    /** 分配模块+操作权限（覆盖式）。 */
    @PutMapping("/{id}/permissions")
    @RequirePermission("role:config")
    public ApiResult<SysRole> updatePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        return ApiResult.ok(roleService.updatePermissions(id, permissionIds));
    }
}
