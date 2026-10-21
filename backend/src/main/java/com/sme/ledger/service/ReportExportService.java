package com.sme.ledger.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.ReportData;
import com.sme.ledger.dto.ReportData.Chart;
import com.sme.ledger.dto.ReportData.Metric;
import com.sme.ledger.dto.ReportData.Series;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * 报表导出：Excel（Apache POI）与 PDF（OpenPDF，内嵌中文字体）。
 */
@Service
public class ReportExportService {

    /** 中文字体候选路径（优先黑体 simhei.ttf）。 */
    private static final String[] CJK_FONT_CANDIDATES = {
            "C:/Windows/Fonts/simhei.ttf",
            "C:/Windows/Fonts/simsunb.ttf",
            "C:/Windows/Fonts/simkai.ttf",
            "C:/Windows/Fonts/NotoSansSC-VF.ttf"
    };

    /** 导出入口：format 为 pdf 时生成 PDF，否则生成 xlsx。 */
    public byte[] export(ReportData data, String format) {
        return "pdf".equalsIgnoreCase(format) ? toPdf(data) : toExcel(data);
    }

    private byte[] toExcel(ReportData data) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet(data.reportTitle());
            int rowIdx = 0;

            XSSFRow titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue(data.reportTitle());
            rowIdx++;

            if (data.metrics() != null) {
                for (Metric m : data.metrics()) {
                    XSSFRow r = sheet.createRow(rowIdx++);
                    r.createCell(0).setCellValue(m.name());
                    r.createCell(1).setCellValue(m.value());
                }
            }
            rowIdx++;

            if (data.charts() != null) {
                for (Chart c : data.charts()) {
                    XSSFRow header = sheet.createRow(rowIdx++);
                    header.createCell(0).setCellValue(c.name());
                    for (int si = 0; si < c.series().size(); si++) {
                        header.createCell(1 + si).setCellValue(c.series().get(si).name());
                    }
                    for (int li = 0; li < c.labels().size(); li++) {
                        XSSFRow r = sheet.createRow(rowIdx++);
                        r.createCell(0).setCellValue(c.labels().get(li));
                        for (int si = 0; si < c.series().size(); si++) {
                            BigDecimal v = c.series().get(si).values().get(li);
                            r.createCell(1 + si).setCellValue(v == null ? 0 : v.doubleValue());
                        }
                    }
                    rowIdx++;
                }
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "Excel 导出失败: " + e.getMessage());
        }
    }

    private byte[] toPdf(ReportData data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            BaseFont bf = loadCjkFont();
            Font titleFont = new Font(bf, 16, Font.BOLD);
            Font headFont = new Font(bf, 11, Font.BOLD);
            Font bodyFont = new Font(bf, 10, Font.NORMAL);

            Paragraph title = new Paragraph(data.reportTitle(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12);
            doc.add(title);

            if (data.metrics() != null) {
                for (Metric m : data.metrics()) {
                    doc.add(new Paragraph(m.name() + "：" + m.value(), bodyFont));
                }
            }

            if (data.charts() != null) {
                for (Chart c : data.charts()) {
                    doc.add(new Paragraph(" "));
                    doc.add(new Paragraph(c.name(), headFont));
                    PdfPTable table = new PdfPTable(1 + c.series().size());
                    table.setWidthPercentage(100);
                    table.addCell(new PdfPCell(new Paragraph("分类", headFont)));
                    for (Series s : c.series()) {
                        table.addCell(new PdfPCell(new Paragraph(s.name(), headFont)));
                    }
                    for (int li = 0; li < c.labels().size(); li++) {
                        table.addCell(new PdfPCell(new Paragraph(c.labels().get(li), bodyFont)));
                        for (Series s : c.series()) {
                            BigDecimal v = s.values().get(li);
                            table.addCell(new PdfPCell(new Paragraph(
                                    v == null ? "0" : v.stripTrailingZeros().toPlainString(), bodyFont)));
                        }
                    }
                    doc.add(table);
                }
            }
            doc.close();
        } catch (Exception e) {
            throw new BusinessException(500, "PDF 导出失败: " + e.getMessage());
        }
        return out.toByteArray();
    }

    /** 加载内嵌中文字体；找不到时回退到 Helvetica（中文会乱码，但导出不报错）。 */
    private BaseFont loadCjkFont() throws Exception {
        for (String path : CJK_FONT_CANDIDATES) {
            if (new File(path).exists()) {
                try {
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                } catch (Exception ignored) {
                    // 尝试下一个候选字体
                }
            }
        }
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    }
}
