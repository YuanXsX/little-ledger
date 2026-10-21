package com.sme.ledger.config;

import com.sme.ledger.entity.AlertRule;
import com.sme.ledger.entity.LedgerCategory;
import com.sme.ledger.repository.AlertRuleRepository;
import com.sme.ledger.repository.LedgerCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化预设台账分类（H2 内存库不执行 init.sql，故在此幂等种子）。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final LedgerCategoryRepository categoryRepository;
    private final AlertRuleRepository alertRuleRepository;

    public DataInitializer(LedgerCategoryRepository categoryRepository,
                           AlertRuleRepository alertRuleRepository) {
        this.categoryRepository = categoryRepository;
        this.alertRuleRepository = alertRuleRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new LedgerCategory("财务流水台账", true, "记录企业资金收支明细"));
            categoryRepository.save(new LedgerCategory("库存明细台账", true, "管控企业商品库存"));
            categoryRepository.save(new LedgerCategory("客户信息台账", true, "管理客户基础信息及合作数据"));
        }
        if (alertRuleRepository.count() == 0) {
            AlertRule rule = new AlertRule();
            rule.setRuleName("库存不足预警");
            rule.setRuleType("库存不足");
            rule.setNotifyWay("消息,短信");
            rule.setEnabled(true);
            alertRuleRepository.save(rule);
        }
    }
}
