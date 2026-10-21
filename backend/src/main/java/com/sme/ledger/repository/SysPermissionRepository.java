package com.sme.ledger.repository;

import com.sme.ledger.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    Optional<SysPermission> findByModuleAndOperation(String module, String operation);
}
