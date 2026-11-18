package com.sme.ledger.service;

import com.sme.ledger.dto.LedgerSummaryResponse;
import com.sme.ledger.entity.CustomerLedger;
import com.sme.ledger.entity.FinanceLedger;
import com.sme.ledger.entity.InventoryLedger;
import com.sme.ledger.repository.AlertRepository;
import com.sme.ledger.repository.CustomerLedgerRepository;
import com.sme.ledger.repository.FinanceLedgerRepository;
import com.sme.ledger.repository.InventoryLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 台账统计摘要：读取三大台账 + 未处理预警，生成经营概况文本与简易异常/关注提示。
 * 既是 AI 问答的上下文（M8-2 要求），也是助手页面的数据卡片数据源。
 */
@Service
public class LedgerSummaryService {

    private final FinanceLedgerRepository financeRepository;
    private final InventoryLedgerRepository inventoryRepository;
    private final CustomerLedgerRepository customerRepository;
    private final AlertRepository alertRepository;

    public LedgerSummaryService(FinanceLedgerRepository financeRepository,
                                InventoryLedgerRepository inventoryRepository,
                                CustomerLedgerRepository customerRepository,
                                AlertRepository alertRepository) {
        this.financeRepository = financeRepository;
        this.inventoryRepository = inventoryRepository;
        this.customerRepository = customerRepository;
        this.alertRepository = alertRepository;
    }

    public LedgerSummaryResponse build() {
        List<FinanceLedger> finances = financeRepository.findAll();
        List<InventoryLedger> inventories = inventoryRepository.findAll();
        List<CustomerLedger> customers = customerRepository.findAll();

        // 经营数据聚合（与智能报表口径一致）
        BigDecimal income = finances.stream()
                .filter(x -> "收入".equals(x.getFlowType()))
                .map(x -> x.getAmount() == null ? BigDecimal.ZERO : x.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = finances.stream()
                .filter(x -> "支出".equals(x.getFlowType()))
                .map(x -> x.getAmount() == null ? BigDecimal.ZERO : x.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = income.subtract(expense);
        BigDecimal receivable = customers.stream()
                .map(x -> x.getReceivable() == null ? BigDecimal.ZERO : x.getReceivable())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalStock = inventories.stream()
                .mapToInt(x -> x.getStockQty() == null ? 0 : x.getStockQty()).sum();
        int unresolvedAlerts = alertRepository.findByStatusOrderByIdDesc("UNRESOLVED").size();

        // 低库存商品明细（库存数量 <= 最低阈值）
        List<InventoryLedger> lowStockItems = inventories.stream()
                .filter(x -> x.getStockQty() != null && x.getMinStock() != null
                        && x.getStockQty() <= x.getMinStock())
                .toList();

        StringBuilder summary = new StringBuilder();
        summary.append("财务：总收入 ").append(money(income)).append(" 元，总支出 ")
                .append(money(expense)).append(" 元，净收入 ").append(money(net))
                .append(" 元，流水共 ").append(finances.size()).append(" 笔。\n");
        summary.append("客户：共 ").append(customers.size()).append(" 家，应收账款合计 ")
                .append(money(receivable)).append(" 元。\n");
        summary.append("库存：商品 ").append(inventories.size()).append(" 种，库存总量 ")
                .append(totalStock).append("，低库存商品 ").append(lowStockItems.size()).append(" 种。\n");
        summary.append("预警：未处理预警 ").append(unresolvedAlerts).append(" 条。");

        List<String> hints = new ArrayList<>();
        for (InventoryLedger x : lowStockItems) {
            hints.add("【库存不足】" + x.getProductName() + "：当前库存 " + x.getStockQty()
                    + "，低于最低阈值 " + x.getMinStock() + "，建议尽快补货");
        }
        if (unresolvedAlerts > 0) {
            hints.add("【预警待处理】共有 " + unresolvedAlerts + " 条未处理预警，请前往「异常预警」模块处理");
        }
        if (net.signum() < 0) {
            hints.add("【经营关注】净收入为负（" + money(net) + " 元），建议关注成本与费用控制");
        }
        if (receivable.signum() > 0) {
            hints.add("【资金关注】应收账款 " + money(receivable) + " 元，建议跟进客户回款进度");
        }
        if (hints.isEmpty()) {
            hints.add("经营数据正常，暂无异常提示");
        }
        return new LedgerSummaryResponse(summary.toString(), hints);
    }

    private static String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
