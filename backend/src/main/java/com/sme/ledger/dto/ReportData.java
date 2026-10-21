package com.sme.ledger.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 智能报表统一返回结构：KPI 指标 + 多张图表数据。
 * 前端据此渲染 KPI 卡片与 ECharts 图表（折线/柱状/饼图）。
 *
 * @param reportType  报表类型标识：finance / inventory / customer / overview
 * @param reportTitle 报表中文标题：财务报表 / 库存报表 / 客户报表
 * @param metrics     KPI 指标列表
 * @param charts      图表列表
 */
public record ReportData(
        String reportType,
        String reportTitle,
        List<Metric> metrics,
        List<Chart> charts) {

    /** KPI 指标，value 为已格式化的字符串（金额保留两位小数，数量为整数）。 */
    public record Metric(String name, String value) {
    }

    /**
     * 单张图表。
     *
     * @param chartType 图表类型：line / bar / pie
     * @param labels    X 轴（或饼图分类）标签
     * @param series    数据序列，饼图通常单序列，折线/柱状可多序列
     */
    public record Chart(String name, String chartType, List<String> labels, List<Series> series) {
    }

    /** 数据序列，values 与 Chart.labels 一一对应。 */
    public record Series(String name, List<BigDecimal> values) {
    }
}
