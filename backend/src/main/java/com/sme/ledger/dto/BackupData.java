package com.sme.ledger.dto;

import com.sme.ledger.entity.Contact;
import com.sme.ledger.entity.CustomerLedger;
import com.sme.ledger.entity.EnterpriseInfo;
import com.sme.ledger.entity.FinanceLedger;
import com.sme.ledger.entity.InventoryLedger;
import com.sme.ledger.entity.LedgerCategory;
import com.sme.ledger.entity.LedgerField;

import java.util.List;

/**
 * 全量备份数据快照：聚合七张业务表（台账 + 企业信息 + 联系人 + 分类/字段）。
 * 序列化为 JSON 文件落盘；用户/角色/权限属系统配置，由启动种子重建，不纳入数据备份。
 */
public record BackupData(
        List<EnterpriseInfo> enterprises,
        List<Contact> contacts,
        List<LedgerCategory> categories,
        List<LedgerField> fields,
        List<FinanceLedger> finances,
        List<InventoryLedger> inventories,
        List<CustomerLedger> customers) {
}
