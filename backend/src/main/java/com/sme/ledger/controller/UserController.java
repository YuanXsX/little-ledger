package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.dto.UserRequest;
import com.sme.ledger.entity.SysUser;
import com.sme.ledger.security.RequirePermission;
import com.sme.ledger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理接口。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequirePermission("user:view")
    public ApiResult<List<SysUser>> list() {
        return ApiResult.ok(userService.list());
    }

    @PostMapping
    @RequirePermission("user:create")
    public ApiResult<SysUser> create(@Valid @RequestBody UserRequest request) {
        return ApiResult.ok(userService.create(request));
    }
}
