-- =============================================================
-- 小微企业数字化台账管理系统 - 数据库初始化脚本 (MySQL 8.x)
-- 字符集统一 utf8mb4，存储脱敏后的敏感字段（禁止存身份证号/银行卡号）
-- =============================================================
CREATE DATABASE IF NOT EXISTS `ledger` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ledger`;

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 1. 权限控制：用户 / 角色 / 权限
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`      VARCHAR(64)  NOT NULL COMMENT '登录名',
  `password`      VARCHAR(255) NOT NULL COMMENT '密码(加密存储)',
  `real_name`     VARCHAR(64)  DEFAULT NULL COMMENT '姓名',
  `phone`         VARCHAR(16)  DEFAULT NULL COMMENT '手机号(脱敏显示)',
  `email`         VARCHAR(128) DEFAULT NULL COMMENT '邮箱(脱敏显示)',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1正常 0锁定',
  `fail_count`    INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数(>=3锁定)',
  `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB COMMENT='系统用户';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `role_code`   VARCHAR(64) NOT NULL COMMENT '角色编码',
  `role_name`   VARCHAR(64) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB COMMENT='角色';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB COMMENT='用户-角色关联';

DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id`        BIGINT       NOT NULL AUTO_INCREMENT,
  `module`    VARCHAR(64)  NOT NULL COMMENT '模块(如 finance/inventory/customer)',
  `operation` VARCHAR(32)  NOT NULL COMMENT '操作(如 create/update/delete/view)',
  `name`      VARCHAR(128) NOT NULL COMMENT '权限描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_module_op` (`module`, `operation`)
) ENGINE=InnoDB COMMENT='权限点(模块+操作)';

DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `role_id`       BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB COMMENT='角色-权限关联';

-- 默认四类角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`) VALUES
('OWNER',     '企业负责人', '查看所有台账与报表、审批敏感操作、配置权限与预警阈值'),
('FINANCE',   '财务负责人', '管理财务流水、生成财务报表、查看应收账款、录入收支'),
('KEEPER',    '库管',       '管理库存台账、录入出入库、查看库存报表、处理库存预警'),
('EMPLOYEE',  '普通员工',   '查看授权台账、录入非敏感数据、查看部分报表');

-- -----------------------------------------------------------
-- 2. 企业基础信息：资质备案 + 联系人
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `biz_enterprise_info`;
CREATE TABLE `biz_enterprise_info` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
  `license_no`          VARCHAR(64)  NOT NULL COMMENT '营业执照编号',
  `business_license`    VARCHAR(64)  DEFAULT NULL COMMENT '经营许可证',
  `business_scope`      VARCHAR(500) DEFAULT NULL COMMENT '经营范围',
  `registered_address`  VARCHAR(200) DEFAULT NULL COMMENT '注册地址',
  `establish_date`      DATE         DEFAULT NULL COMMENT '成立日期',
  `license_file`        VARCHAR(255) DEFAULT NULL COMMENT '资质文件扫描件路径(JPG/PDF)',
  `license_expire_date` DATE         DEFAULT NULL COMMENT '资质有效期截止日(到期前15天提醒)',
  `created_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_license_no` (`license_no`)
) ENGINE=InnoDB COMMENT='企业资质备案';

DROP TABLE IF EXISTS `biz_contact`;
CREATE TABLE `biz_contact` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `contact_type` VARCHAR(32)  NOT NULL COMMENT '联系人类型(内部/客户/供应商)',
  `name`         VARCHAR(64)  NOT NULL COMMENT '联系人姓名',
  `phone`        VARCHAR(16)  DEFAULT NULL COMMENT '手机号(原文存储，展示层脱敏)',
  `email`        VARCHAR(128) DEFAULT NULL COMMENT '邮箱(原文存储，展示层脱敏)',
  `company`      VARCHAR(128) DEFAULT NULL COMMENT '所属企业(外部联系人)',
  `position`     VARCHAR(64)  DEFAULT NULL COMMENT '职务',
  `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='联系人(展示层脱敏，禁止存身份证号/家庭住址)';

-- -----------------------------------------------------------
-- 3. 三大台账
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `biz_finance_ledger`;
CREATE TABLE `biz_finance_ledger` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `voucher_no`    VARCHAR(64)   NOT NULL COMMENT '凭证号',
  `flow_type`     VARCHAR(16)   NOT NULL COMMENT '收支类型(收入/支出)',
  `amount`        DECIMAL(18,2) NOT NULL COMMENT '金额(元)',
  `occur_date`    DATE          NOT NULL COMMENT '发生日期',
  `pay_method`    VARCHAR(32)   DEFAULT NULL COMMENT '支付方式(现金/银行转账/微信/支付宝)',
  `related_type`  VARCHAR(32)   DEFAULT NULL COMMENT '关联对象类型(客户/供应商)',
  `related_name`  VARCHAR(128)  DEFAULT NULL COMMENT '关联对象名称',
  `remark`        VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `created_by`    VARCHAR(64)   DEFAULT NULL COMMENT '录入人',
  `created_at`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  PRIMARY KEY (`id`),
  KEY `idx_voucher` (`voucher_no`, `amount`, `occur_date`)
) ENGINE=InnoDB COMMENT='财务流水台账';

DROP TABLE IF EXISTS `biz_inventory_ledger`;
CREATE TABLE `biz_inventory_ledger` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `sku`           VARCHAR(64)  NOT NULL COMMENT '商品SKU',
  `product_name`  VARCHAR(128) NOT NULL COMMENT '商品名称',
  `spec`          VARCHAR(128) DEFAULT NULL COMMENT '规格型号',
  `unit`          VARCHAR(16)  DEFAULT NULL COMMENT '单位',
  `stock_qty`     INT          NOT NULL DEFAULT 0 COMMENT '库存数量',
  `in_date`       DATE         DEFAULT NULL COMMENT '入库日期',
  `in_qty`        INT          DEFAULT NULL COMMENT '入库数量',
  `out_date`      DATE         DEFAULT NULL COMMENT '出库日期',
  `out_qty`       INT          DEFAULT NULL COMMENT '出库数量',
  `supplier`      VARCHAR(128) DEFAULT NULL COMMENT '供应商',
  `min_stock`     INT          NOT NULL DEFAULT 0 COMMENT '最低库存阈值(低于触发预警)',
  `priority`      VARCHAR(16)  DEFAULT 'NORMAL' COMMENT '库存优先级(HIGH常采购/NORMAL)',
  `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_by`    VARCHAR(64)  DEFAULT NULL COMMENT '录入人',
  `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  PRIMARY KEY (`id`),
  KEY `idx_sku` (`sku`)
) ENGINE=InnoDB COMMENT='库存明细台账';

DROP TABLE IF EXISTS `biz_customer_ledger`;
CREATE TABLE `biz_customer_ledger` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `customer_name`    VARCHAR(128)  NOT NULL COMMENT '客户名称',
  `contact_person`   VARCHAR(64)   DEFAULT NULL COMMENT '联系人',
  `phone`            VARCHAR(16)   DEFAULT NULL COMMENT '手机号(展示层脱敏)',
  `email`            VARCHAR(128)  DEFAULT NULL COMMENT '邮箱(展示层脱敏)',
  `industry`         VARCHAR(64)   DEFAULT NULL COMMENT '所属行业',
  `coop_start_date`  DATE          DEFAULT NULL COMMENT '合作起始日期',
  `total_amount`     DECIMAL(18,2) DEFAULT 0 COMMENT '累计合作金额(元)',
  `receivable`       DECIMAL(18,2) DEFAULT 0 COMMENT '应收账款金额(元)',
  `last_coop_time`   DATETIME      DEFAULT NULL COMMENT '最近合作时间',
  `address`          VARCHAR(200)  DEFAULT NULL COMMENT '客户地址(省/市)',
  `coop_level`       VARCHAR(4)    DEFAULT NULL COMMENT '合作等级(A/B/C)',
  `remark`           VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `created_by`       VARCHAR(64)   DEFAULT NULL COMMENT '录入人',
  `created_at`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer` (`customer_name`)
) ENGINE=InnoDB COMMENT='客户信息台账';

-- -----------------------------------------------------------
-- 4. 自定义台账分类与字段
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `biz_ledger_category`;
CREATE TABLE `biz_ledger_category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64)  NOT NULL COMMENT '台账分类名称',
  `is_preset`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否系统预设(1是 0自定义)',
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB COMMENT='台账分类(含自定义扩展)';

INSERT INTO `biz_ledger_category` (`name`, `is_preset`, `description`) VALUES
('财务流水台账', 1, '记录企业资金收支明细'),
('库存明细台账', 1, '管控企业商品库存'),
('客户信息台账', 1, '管理客户基础信息及合作数据');

DROP TABLE IF EXISTS `biz_ledger_field`;
CREATE TABLE `biz_ledger_field` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT       NOT NULL COMMENT '所属台账分类',
  `field_key`   VARCHAR(64)  NOT NULL COMMENT '字段标识',
  `field_name`  VARCHAR(64)  NOT NULL COMMENT '字段显示名(如增值税税率/物流单号)',
  `field_type`  VARCHAR(32)  DEFAULT 'string' COMMENT '字段类型',
  `required`    TINYINT      DEFAULT 0 COMMENT '是否必填',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='自定义字段';

-- -----------------------------------------------------------
-- 5. 数据备份与恢复
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_backup_log`;
CREATE TABLE `sys_backup_log` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `backup_type`  VARCHAR(32)  NOT NULL COMMENT '备份类型(auto/manual)',
  `storage`      VARCHAR(32)  NOT NULL COMMENT '存储位置(local/oss/dual)',
  `file_path`    VARCHAR(255) DEFAULT NULL COMMENT '备份文件路径',
  `status`       VARCHAR(16)  NOT NULL COMMENT '状态(success/failed)',
  `retry_count`  INT          DEFAULT 0 COMMENT '重试次数',
  `error_msg`    VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `backup_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '备份时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='备份记录';

-- -----------------------------------------------------------
-- 6. 异常预警
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_alert_rule`;
CREATE TABLE `sys_alert_rule` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `rule_name`   VARCHAR(64) NOT NULL COMMENT '规则名称',
  `rule_type`   VARCHAR(32) NOT NULL COMMENT '预警类型(如库存不足)',
  `target`      VARCHAR(64) DEFAULT NULL COMMENT '涉及对象(如SKU)',
  `threshold`   VARCHAR(64) DEFAULT NULL COMMENT '阈值',
  `notify_way`  VARCHAR(64) DEFAULT NULL COMMENT '通知方式(消息/短信/邮件，逗号分隔)',
  `enabled`     TINYINT     DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='预警规则';

DROP TABLE IF EXISTS `sys_alert`;
CREATE TABLE `sys_alert` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `rule_id`     BIGINT       DEFAULT NULL COMMENT '关联规则',
  `alert_type`  VARCHAR(32)  NOT NULL COMMENT '预警类型',
  `target`      VARCHAR(128) DEFAULT NULL COMMENT '涉及对象',
  `current_val` VARCHAR(64)  DEFAULT NULL COMMENT '当前数值',
  `status`      VARCHAR(16)  NOT NULL DEFAULT 'UNRESOLVED' COMMENT '状态(UNRESOLVED/RESOLVED)',
  `remind_time` DATETIME     DEFAULT NULL COMMENT '下次提醒时间(未处理重复提醒)',
  `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME     DEFAULT NULL COMMENT '解决时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='预警记录';

-- -----------------------------------------------------------
-- 7. 更新日志 / 知识库
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_update_log`;
CREATE TABLE `sys_update_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `biz_type`    VARCHAR(32)  NOT NULL COMMENT '业务类型(如enterprise/contact)',
  `biz_id`      BIGINT       DEFAULT NULL COMMENT '业务主键',
  `operator`    VARCHAR(64)  NOT NULL COMMENT '更新人',
  `reason`      VARCHAR(255) DEFAULT NULL COMMENT '更新原因',
  `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='信息更新日志';

DROP TABLE IF EXISTS `biz_knowledge_doc`;
CREATE TABLE `biz_knowledge_doc` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `title`      VARCHAR(128) NOT NULL COMMENT '文档标题',
  `file_path`  VARCHAR(255) DEFAULT NULL COMMENT '源文件路径',
  `status`     VARCHAR(16)  DEFAULT 'PENDING' COMMENT '解析状态(PENDING/PARSED/FAILED)',
  `created_by` VARCHAR(64)  DEFAULT NULL COMMENT '上传人',
  `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='财税知识库文档(用于RAG向量化)';

SET FOREIGN_KEY_CHECKS = 1;
