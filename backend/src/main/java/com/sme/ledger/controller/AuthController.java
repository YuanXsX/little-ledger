package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.dto.LoginRequest;
import com.sme.ledger.dto.LoginResponse;
import com.sme.ledger.dto.UserInfo;
import com.sme.ledger.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录、二次验密、当前用户信息。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResult.ok(authService.login(request.getUsername(), request.getPassword()));
    }

    /** 敏感操作二次验密。 */
    @PostMapping("/verify")
    public ApiResult<Void> verify(@Valid @RequestBody LoginRequest request) {
        authService.verify(request.getUsername(), request.getPassword());
        return ApiResult.ok();
    }

    /** 当前登录用户信息（username 由 JwtAuthFilter 注入请求属性）。 */
    @GetMapping("/me")
    public ApiResult<UserInfo> me(@RequestAttribute(name = "username", required = false) String username) {
        if (username == null) {
            return ApiResult.error(401, "未登录");
        }
        return ApiResult.ok(authService.me(username));
    }
}
