package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.dto.ReportData;
import com.sme.ledger.service.ReportExportService;
import com.sme.ledger.service.ReportService;
import com.sme.ledger.security.RequirePermission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 智能报表接口：三大报表数据 + 经营概览 + 导出（Excel/PDF）。
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService exportService;

    @Value("${app.enterprise-name:小微企业}")
    private String enterpriseName;

    public ReportController(ReportService reportService, ReportExportService exportService) {
        this.reportService = reportService;
        this.exportService = exportService;
    }

    /** 财务报表：收支趋势 / 支付方式占比 / 收支对比。 */
    @GetMapping("/finance")
    public ApiResult<ReportData> finance(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResult.ok(reportService.financeReport(range, startDate, endDate));
    }

    /** 库存报表：商品库存对比 / 供应商分布 / 出入库对比。category 为商品类别筛选。 */
    @GetMapping("/inventory")
    public ApiResult<ReportData> inventory(@RequestParam(required = false) String category) {
        return ApiResult.ok(reportService.inventoryReport(category));
    }

    /** 客户报表：累计合作金额 / 应收账款 / 合作等级占比。 */
    @GetMapping("/customer")
    public ApiResult<ReportData> customer() {
        return ApiResult.ok(reportService.customerReport());
    }

    /** 经营概览：一键汇总三大台账核心 KPI。 */
    @GetMapping("/overview")
    public ApiResult<ReportData> overview() {
        return ApiResult.ok(reportService.overview());
    }

    /** 导出报表，format 支持 xlsx / pdf，文件按「企业名称+年月+报表名」自动命名。 */
    @GetMapping("/export")
    @RequirePermission("report:export")
    public ResponseEntity<byte[]> export(
            @RequestParam String type,
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category) {
        ReportData data = switch (type) {
            case "inventory" -> reportService.inventoryReport(category);
            case "customer" -> reportService.customerReport();
            default -> reportService.financeReport(range, startDate, endDate);
        };
        byte[] bytes = exportService.export(data, format);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType(format));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename(data.reportTitle(), format), StandardCharsets.UTF_8)
                .build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    private String filename(String reportTitle, String format) {
        String ext = "pdf".equalsIgnoreCase(format) ? "pdf" : "xlsx";
        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return enterpriseName + " " + ym + " " + reportTitle + "." + ext;
    }

    private MediaType mediaType(String format) {
        return "pdf".equalsIgnoreCase(format)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
