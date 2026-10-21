package com.sme.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 权限点（模块 + 操作），如 finance:delete、report:export。
 */
@Entity
@Table(name = "sys_permission")
public class SysPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 模块，如 finance / inventory / customer / report / user / role */
    @Column(name = "module", length = 64, nullable = false)
    private String module;

    /** 操作，如 view / create / update / delete / export / config */
    @Column(name = "operation", length = 32, nullable = false)
    private String operation;

    /** 权限描述，如「删除财务流水」 */
    @Column(name = "name", length = 128, nullable = false)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** 权限键：module:operation，如 finance:delete */
    public String getKey() {
        return module + ":" + operation;
    }
}
