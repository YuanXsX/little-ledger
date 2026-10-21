package com.sme.ledger.service;

import com.sme.ledger.dto.ReportData;
import com.sme.ledger.dto.ReportData.Chart;
import com.sme.ledger.dto.ReportData.Metric;
import com.sme.ledger.dto.ReportData.Series;
import com.sme.ledger.entity.CustomerLedger;
import com.sme.ledger.entity.FinanceLedger;
import com.sme.ledger.entity.InventoryLedger;
import com.sme.ledger.repository.CustomerLedgerRepository;
import com.sme.ledger.repository.FinanceLedgerRepository;
import com.sme.ledger.repository.InventoryLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 智能报表聚合：基于三大台账原始数据，按维度聚合出 KPI 指标与图表数据。
 * 数据量小（小微企业），采用内存聚合，简单直观；后续可下沉为数据库聚合查询。
 */
@Service
public class ReportService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final FinanceLedgerRepository financeRepository;
    private final InventoryLedgerRepository inventoryRepository;
    private final CustomerLedgerRepository customerRepository;

    public ReportService(FinanceLedgerRepository financeRepository,
                         InventoryLedgerRepository inventoryRepository,
                         CustomerLedgerRepository customerRepository) {
        this.financeRepository = financeRepository;
        this.inventoryRepository = inventoryRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * 财务报表：收入/支出趋势（折线）、支付方式占比（饼图）、收支对比（柱状）。
     * 时间范围作用于财务发生日期。
     */
    public ReportData financeReport(String range, LocalDate start, LocalDate end) {
        String r = resolveRange(range);
        LocalDate from = start != null ? start : defaultStart(r);
        LocalDate to = end != null ? end : LocalDate.now();

        List<FinanceLedger> rows = financeRepository.findAll().stream()
                .filter(x -> x.getOccurDate() != null)
                .filter(x -> !x.getOccurDate().isBefore(from) && !x.getOccurDate().isAfter(to))
                .toList();

        BigDecimal totalIncome = ZERO;
        BigDecimal totalExpense = ZERO;
        TreeMap<String, BigDecimal> incomeByPeriod = new TreeMap<>();
        TreeMap<String, BigDecimal> expenseByPeriod = new TreeMap<>();
        Map<String, BigDecimal> byPayMethod = new LinkedHashMap<>();

        for (FinanceLedger x : rows) {
            BigDecimal amt = x.getAmount() == null ? ZERO : x.getAmount();
            if ("收入".equals(x.getFlowType())) {
                totalIncome = totalIncome.add(amt);
                incomeByPeriod.merge(periodKey(x.getOccurDate(), r), amt, BigDecimal::add);
            } else if ("支出".equals(x.getFlowType())) {
                totalExpense = totalExpense.add(amt);
                expenseByPeriod.merge(periodKey(x.getOccurDate(), r), amt, BigDecimal::add);
            }
            String pm = blank(x.getPayMethod()) ? "未填写" : x.getPayMethod();
            byPayMethod.merge(pm, amt, BigDecimal::add);
        }

        List<Metric> metrics = List.of(
                new Metric("总收入", money(totalIncome)),
                new Metric("总支出", money(totalExpense)),
                new Metric("净收入", money(totalIncome.subtract(totalExpense))),
                new Metric("流水笔数", String.valueOf(rows.size()))
        );

        List<Chart> charts = new ArrayList<>();

        TreeSet<String> periods = new TreeSet<>(incomeByPeriod.keySet());
        periods.addAll(expenseByPeriod.keySet());
        List<String> periodLabels = new ArrayList<>(periods);
        charts.add(new Chart("收支趋势", "line", periodLabels, List.of(
                new Series("收入", periodLabels.stream().map(p -> incomeByPeriod.getOrDefault(p, ZERO)).toList()),
                new Series("支出", periodLabels.stream().map(p -> expenseByPeriod.getOrDefault(p, ZERO)).toList())
        )));

        List<String> payMethods = new ArrayList<>(byPayMethod.keySet());
        charts.add(new Chart("支付方式占比", "pie", payMethods, List.of(
                new Series("金额", payMethods.stream().map(byPayMethod::get).toList())
        )));

        charts.add(new Chart("收支对比", "bar", List.of("收入", "支出"), List.of(
                new Series("金额", List.of(totalIncome, totalExpense))
        )));

        return new ReportData("finance", "财务报表", metrics, charts);
    }

    /**
     * 库存报表：商品库存对比（柱状）、供应商库存分布（饼图）、入库/出库对比（柱状）。
     * category 作为商品类别筛选，匹配供应商或商品名称（当前库存台账无独立“类别”字段，以供应商近似）。
     */
    public ReportData inventoryReport(String category) {
        List<InventoryLedger> rows = inventoryRepository.findAll().stream()
                .filter(x -> category == null || category.isBlank() || matchesCategory(x, category))
                .toList();

        int totalStock = rows.stream().mapToInt(x -> x.getStockQty() == null ? 0 : x.getStockQty()).sum();
        int lowStock = (int) rows.stream()
                .filter(x -> x.getStockQty() != null && x.getMinStock() != null && x.getStockQty() <= x.getMinStock())
                .count();
        int totalIn = rows.stream().mapToInt(x -> x.getInQty() == null ? 0 : x.getInQty()).sum();
        int totalOut = rows.stream().mapToInt(x -> x.getOutQty() == null ? 0 : x.getOutQty()).sum();

        List<Metric> metrics = List.of(
                new Metric("商品种类数", String.valueOf(rows.size())),
                new Metric("库存总量", String.valueOf(totalStock)),
                new Metric("低库存商品数", String.valueOf(lowStock)),
                new Metric("入库总量", String.valueOf(totalIn)),
                new Metric("出库总量", String.valueOf(totalOut))
        );

        List<Chart> charts = new ArrayList<>();

        List<String> names = rows.stream().map(InventoryLedger::getProductName).toList();
        charts.add(new Chart("商品库存对比", "bar", names, List.of(
                new Series("库存数量", rows.stream()
                        .map(x -> BigDecimal.valueOf(x.getStockQty() == null ? 0 : x.getStockQty()))
                        .toList())
        )));

        Map<String, Integer> bySupplier = new LinkedHashMap<>();
        for (InventoryLedger x : rows) {
            String s = blank(x.getSupplier()) ? "未填写" : x.getSupplier();
            bySupplier.merge(s, x.getStockQty() == null ? 0 : x.getStockQty(), Integer::sum);
        }
        List<String> suppliers = new ArrayList<>(bySupplier.keySet());
        charts.add(new Chart("供应商库存分布", "pie", suppliers, List.of(
                new Series("库存数量", suppliers.stream()
                        .map(s -> BigDecimal.valueOf(bySupplier.get(s)))
                        .toList())
        )));

        charts.add(new Chart("入库出库对比", "bar", List.of("入库", "出库"), List.of(
                new Series("数量", List.of(BigDecimal.valueOf(totalIn), BigDecimal.valueOf(totalOut)))
        )));

        return new ReportData("inventory", "库存报表", metrics, charts);
    }

    /**
     * 客户报表：客户累计合作金额（柱状）、应收账款（柱状）、合作等级占比（饼图）。
     */
    public ReportData customerReport() {
        List<CustomerLedger> rows = customerRepository.findAll();

        BigDecimal totalAmount = rows.stream()
                .map(x -> x.getTotalAmount() == null ? ZERO : x.getTotalAmount())
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalReceivable = rows.stream()
                .map(x -> x.getReceivable() == null ? ZERO : x.getReceivable())
                .reduce(ZERO, BigDecimal::add);

        List<Metric> metrics = List.of(
                new Metric("客户总数", String.valueOf(rows.size())),
                new Metric("累计合作金额", money(totalAmount)),
                new Metric("应收账款总额", money(totalReceivable))
        );

        List<Chart> charts = new ArrayList<>();

        List<String> names = rows.stream().map(CustomerLedger::getCustomerName).toList();
        charts.add(new Chart("客户累计合作金额", "bar", names, List.of(
                new Series("合作金额", rows.stream()
                        .map(x -> x.getTotalAmount() == null ? ZERO : x.getTotalAmount())
                        .toList())
        )));
        charts.add(new Chart("客户应收账款", "bar", names, List.of(
                new Series("应收金额", rows.stream()
                        .map(x -> x.getReceivable() == null ? ZERO : x.getReceivable())
                        .toList())
        )));

        Map<String, Long> byLevel = new LinkedHashMap<>();
        for (CustomerLedger x : rows) {
            String l = blank(x.getCoopLevel()) ? "未评级" : x.getCoopLevel();
            byLevel.merge(l, 1L, Long::sum);
        }
        List<String> levels = new ArrayList<>(byLevel.keySet());
        charts.add(new Chart("合作等级占比", "pie", levels, List.of(
                new Series("客户数", levels.stream().map(l -> BigDecimal.valueOf(byLevel.get(l))).toList())
        )));

        return new ReportData("customer", "客户报表", metrics, charts);
    }

    /** 经营概览：一键汇总三大台账的核心 KPI（全量，不限时间范围）。 */
    public ReportData overview() {
        List<FinanceLedger> finances = financeRepository.findAll();
        List<InventoryLedger> inventories = inventoryRepository.findAll();
        List<CustomerLedger> customers = customerRepository.findAll();

        BigDecimal income = finances.stream()
                .filter(x -> "收入".equals(x.getFlowType()))
                .map(x -> x.getAmount() == null ? ZERO : x.getAmount())
                .reduce(ZERO, BigDecimal::add);
        BigDecimal expense = finances.stream()
                .filter(x -> "支出".equals(x.getFlowType()))
                .map(x -> x.getAmount() == null ? ZERO : x.getAmount())
                .reduce(ZERO, BigDecimal::add);
        int totalStock = inventories.stream().mapToInt(x -> x.getStockQty() == null ? 0 : x.getStockQty()).sum();
        long lowStock = inventories.stream()
                .filter(x -> x.getStockQty() != null && x.getMinStock() != null && x.getStockQty() <= x.getMinStock())
                .count();
        BigDecimal receivable = customers.stream()
                .map(x -> x.getReceivable() == null ? ZERO : x.getReceivable())
                .reduce(ZERO, BigDecimal::add);

        List<Metric> metrics = List.of(
                new Metric("总收入", money(income)),
                new Metric("总支出", money(expense)),
                new Metric("净收入", money(income.subtract(expense))),
                new Metric("客户总数", String.valueOf(customers.size())),
                new Metric("应收账款总额", money(receivable)),
                new Metric("库存总量", String.valueOf(totalStock)),
                new Metric("低库存商品数", String.valueOf(lowStock))
        );

        return new ReportData("overview", "经营概览", metrics, List.of());
    }

    /** 归一化时间粒度，缺省按月。 */
    private static String resolveRange(String range) {
        return (range == null || range.isBlank()) ? "month" : range;
    }

    /** 未指定起止时间时，按粒度回推起始日。 */
    private static LocalDate defaultStart(String range) {
        LocalDate today = LocalDate.now();
        return switch (range) {
            case "day" -> today;
            case "week" -> today.minusDays(6);
            case "quarter" -> today.minusMonths(3);
            default -> today.minusMonths(1);
        };
    }

    /** 按粒度把日期归一到可排序的分组键。 */
    private static String periodKey(LocalDate d, String range) {
        return switch (range) {
            case "day" -> d.toString();
            case "week" -> d.get(IsoFields.WEEK_BASED_YEAR) + "-W"
                    + String.format("%02d", d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case "quarter" -> d.getYear() + "-Q" + ((d.getMonthValue() - 1) / 3 + 1);
            default -> d.getYear() + "-" + String.format("%02d", d.getMonthValue());
        };
    }

    private static boolean matchesCategory(InventoryLedger x, String category) {
        String c = category.trim().toLowerCase();
        return (x.getSupplier() != null && x.getSupplier().toLowerCase().contains(c))
                || (x.getProductName() != null && x.getProductName().toLowerCase().contains(c));
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
