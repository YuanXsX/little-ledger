package com.sme.ledger.config;

import com.sme.ledger.entity.SysPermission;
import com.sme.ledger.entity.SysRole;
import com.sme.ledger.entity.SysUser;
import com.sme.ledger.repository.SysPermissionRepository;
import com.sme.ledger.repository.SysRoleRepository;
import com.sme.ledger.repository.SysUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限种子数据：默认权限点、四类角色及其权限映射、演示用户。
 * H2 内存库不执行 init.sql，故在此幂等初始化。
 */
@Component
public class PermissionInitializer implements CommandLineRunner {

    /** 权限点定义：{模块, 操作, 描述} */
    private static final String[][] PERMISSIONS = {
            {"finance", "view", "查看财务流水"},
            {"finance", "create", "录入财务流水"},
            {"finance", "update", "编辑财务流水"},
            {"finance", "delete", "删除财务流水"},
            {"finance", "export", "导出财务报表"},
            {"inventory", "view", "查看库存明细"},
            {"inventory", "create", "录入库存"},
            {"inventory", "update", "编辑库存"},
            {"inventory", "delete", "删除库存"},
            {"inventory", "export", "导出库存报表"},
            {"customer", "view", "查看客户信息"},
            {"customer", "create", "录入客户"},
            {"customer", "update", "编辑客户"},
            {"customer", "delete", "删除客户"},
            {"contact", "view", "查看联系人"},
            {"contact", "create", "录入联系人"},
            {"contact", "update", "编辑联系人"},
            {"contact", "delete", "删除联系人"},
            {"enterprise", "view", "查看企业资质"},
            {"enterprise", "update", "更新企业资质"},
            {"category", "view", "查看台账分类"},
            {"category", "create", "新增台账分类"},
            {"report", "view", "查看报表"},
            {"report", "export", "导出报表"},
            {"user", "view", "查看用户"},
            {"user", "create", "新增用户"},
            {"role", "view", "查看角色"},
            {"role", "create", "新增角色"},
            {"role", "config", "配置角色权限"},
            {"backup", "view", "查看备份记录"},
            {"backup", "create", "手动备份"},
            {"backup", "restore", "恢复数据"},
            {"backup", "config", "配置备份策略"},
            {"alert", "view", "查看预警"},
            {"alert", "resolve", "处理预警"},
            {"alert", "config", "配置预警规则"}
    };

    private static final Map<String, String> ROLE_NAMES = Map.of(
            "OWNER", "企业负责人", "FINANCE", "财务负责人", "KEEPER", "库管", "EMPLOYEE", "普通员工");

    private static final Map<String, String> ROLE_DESCS = Map.of(
            "OWNER", "查看所有台账与报表、审批敏感操作、配置权限与预警阈值",
            "FINANCE", "管理财务流水、生成财务报表、查看应收账款、录入收支",
            "KEEPER", "管理库存台账、录入出入库、查看库存报表、处理库存预警",
            "EMPLOYEE", "查看授权台账、录入非敏感数据、查看部分报表");

    private final SysPermissionRepository permissionRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PermissionInitializer(SysPermissionRepository permissionRepository,
                                 SysRoleRepository roleRepository,
                                 SysUserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (permissionRepository.count() > 0) {
            return;
        }
        Map<String, SysPermission> perms = seedPermissions();
        List<String> allKeys = new ArrayList<>(perms.keySet());
        Map<String, List<String>> rolePerms = rolePermissionMap(allKeys);
        for (Map.Entry<String, List<String>> entry : rolePerms.entrySet()) {
            seedRole(entry.getKey(), entry.getValue(), perms);
        }
        seedUsers();
    }

    private Map<String, SysPermission> seedPermissions() {
        Map<String, SysPermission> map = new HashMap<>();
        for (String[] p : PERMISSIONS) {
            SysPermission perm = new SysPermission();
            perm.setModule(p[0]);
            perm.setOperation(p[1]);
            perm.setName(p[2]);
            permissionRepository.save(perm);
            map.put(perm.getKey(), perm);
        }
        return map;
    }

    private Map<String, List<String>> rolePermissionMap(List<String> allKeys) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("OWNER", allKeys);
        map.put("FINANCE", List.of(
                "finance:view", "finance:create", "finance:update", "finance:delete", "finance:export",
                "customer:view", "report:view", "report:export"));
        map.put("KEEPER", List.of(
                "inventory:view", "inventory:create", "inventory:update", "inventory:delete", "inventory:export",
                "customer:view", "report:view", "alert:view", "alert:resolve"));
        map.put("EMPLOYEE", List.of("finance:view", "inventory:view", "customer:view", "report:view"));
        return map;
    }

    private void seedRole(String code, List<String> permKeys, Map<String, SysPermission> perms) {
        SysRole role = roleRepository.findByRoleCode(code).orElseGet(() -> {
            SysRole r = new SysRole();
            r.setRoleCode(code);
            r.setRoleName(ROLE_NAMES.getOrDefault(code, code));
            r.setDescription(ROLE_DESCS.getOrDefault(code, ""));
            return r;
        });
        Set<SysPermission> set = new HashSet<>();
        for (String key : permKeys) {
            SysPermission p = perms.get(key);
            if (p != null) {
                set.add(p);
            }
        }
        role.setPermissions(set);
        roleRepository.save(role);
    }

    private void seedUsers() {
        createUser("admin", "admin123", "系统管理员", Set.of("OWNER"));
        createUser("finance", "finance123", "财务负责人", Set.of("FINANCE"));
        createUser("keeper", "keeper123", "库管员", Set.of("KEEPER"));
        createUser("employee", "employee123", "普通员工", Set.of("EMPLOYEE"));
    }

    private void createUser(String username, String rawPwd, String realName, Set<String> roleCodes) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPwd));
        user.setRealName(realName);
        user.setStatus(1);
        user.setFailCount(0);
        user.setCreatedAt(LocalDateTime.now());
        Set<SysRole> roles = new HashSet<>();
        for (String code : roleCodes) {
            roleRepository.findByRoleCode(code).ifPresent(roles::add);
        }
        user.setRoles(roles);
        userRepository.save(user);
    }
}
