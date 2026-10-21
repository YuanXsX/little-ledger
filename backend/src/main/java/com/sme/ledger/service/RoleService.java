package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.RoleRequest;
import com.sme.ledger.entity.SysPermission;
import com.sme.ledger.entity.SysRole;
import com.sme.ledger.repository.SysPermissionRepository;
import com.sme.ledger.repository.SysRoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色管理：角色列表、新增自定义角色、分配模块+操作权限。
 */
@Service
public class RoleService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;

    public RoleService(SysRoleRepository roleRepository, SysPermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<SysRole> list() {
        return roleRepository.findAll(Sort.by("id"));
    }

    public SysRole create(RoleRequest request) {
        if (roleRepository.existsByRoleCode(request.getRoleCode())) {
            throw new BusinessException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        return roleRepository.save(role);
    }

    /** 分配权限点（覆盖式）。 */
    @Transactional
    public SysRole updatePermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        Set<SysPermission> set = new HashSet<>();
        if (permissionIds != null) {
            for (Long pid : permissionIds) {
                permissionRepository.findById(pid).ifPresent(set::add);
            }
        }
        role.setPermissions(set);
        return roleRepository.save(role);
    }
}
