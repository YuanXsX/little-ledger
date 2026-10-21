package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.UserRequest;
import com.sme.ledger.entity.SysRole;
import com.sme.ledger.entity.SysUser;
import com.sme.ledger.repository.SysRoleRepository;
import com.sme.ledger.repository.SysUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户管理：用户列表、新增用户并分配角色。
 */
@Service
public class UserService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserRepository userRepository, SysRoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<SysUser> list() {
        return userRepository.findAll(Sort.by("id"));
    }

    @Transactional
    public SysUser create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setFailCount(0);
        user.setCreatedAt(LocalDateTime.now());

        Set<SysRole> roles = new HashSet<>();
        if (request.getRoleIds() != null) {
            for (Long roleId : request.getRoleIds()) {
                roleRepository.findById(roleId).ifPresent(roles::add);
            }
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }
}
