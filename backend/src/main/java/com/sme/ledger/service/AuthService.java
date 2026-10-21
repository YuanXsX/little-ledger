package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.LoginResponse;
import com.sme.ledger.dto.UserInfo;
import com.sme.ledger.entity.SysPermission;
import com.sme.ledger.entity.SysRole;
import com.sme.ledger.entity.SysUser;
import com.sme.ledger.repository.SysUserRepository;
import com.sme.ledger.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证逻辑：登录（连续 3 次失败锁定）、二次验密、当前用户信息。
 */
@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(String username, String password) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("账号或密码错误"));
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已锁定，请联系管理员");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            int fail = (user.getFailCount() == null ? 0 : user.getFailCount()) + 1;
            user.setFailCount(fail);
            if (fail >= 3) {
                user.setStatus(0);
                userRepository.save(user);
                throw new BusinessException("连续失败 3 次，账号已锁定");
            }
            userRepository.save(user);
            throw new BusinessException("账号或密码错误(剩余次数 " + (3 - fail) + ")");
        }
        // 登录成功：重置失败计数
        user.setFailCount(0);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), authorities(user));
        return new LoginResponse(token, toUserInfo(user));
    }

    /** 二次验密：敏感操作前校验密码。 */
    public void verify(String username, String password) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已锁定");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误，验证失败");
        }
    }

    public UserInfo me(String username) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        return toUserInfo(user);
    }

    private UserInfo toUserInfo(SysUser user) {
        List<String> roleNames = user.getRoles().stream()
                .map(SysRole::getRoleName).sorted().toList();
        return new UserInfo(user.getUsername(), user.getRealName(), roleNames, authorities(user));
    }

    /** 汇总用户所有角色的权限键。 */
    private List<String> authorities(SysUser user) {
        Set<String> set = new LinkedHashSet<>();
        for (SysRole role : user.getRoles()) {
            for (SysPermission p : role.getPermissions()) {
                set.add(p.getKey());
            }
        }
        return new ArrayList<>(set);
    }
}
