package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.FinanceLedgerRequest;
import com.sme.ledger.dto.ImportResult;
import com.sme.ledger.dto.RowError;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入服务：解析 Excel，逐行校验后入库，返回成功/失败明细。
 *
 * 财务流水 Excel 列（对应附录表 1.1，跳过序号与录入时间）：
 *   1 凭证号  2 收支类型  3 金额  4 发生日期  5 支付方式
 *   6 关联对象类型  7 关联对象名称  8 备注  9 录入人
 */
@Service
public class ImportService {

    private final FinanceService financeService;

    public ImportService(FinanceService financeService) {
        this.financeService = financeService;
    }

    public ImportResult importFinance(MultipartFile file) {
        List<RowError> errors = new ArrayList<>();
        int success = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int dataRows = Math.max(sheet.getLastRowNum(), 0); // 第 0 行为表头
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isEmptyRow(row)) {
                    continue;
                }
                try {
                    FinanceLedgerRequest request = parseRow(row);
                    financeService.create(request);
                    success++;
                } catch (BusinessException e) {
                    errors.add(new RowError(i + 1, e.getMessage()));
                } catch (Exception e) {
                    errors.add(new RowError(i + 1, "格式错误：" + e.getMessage()));
                }
            }
            return new ImportResult(success + errors.size(), success, errors.size(), errors);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文件解析失败，请上传 .xlsx 格式文件");
        }
    }

    private FinanceLedgerRequest parseRow(Row row) {
        FinanceLedgerRequest request = new FinanceLedgerRequest();
        request.setVoucherNo(getString(row, 1));
        request.setFlowType(getString(row, 2));
        request.setAmount(getAmount(row, 3));
        request.setOccurDate(getDate(row, 4));
        request.setPayMethod(getString(row, 5));
        request.setRelatedType(getString(row, 6));
        request.setRelatedName(getString(row, 7));
        request.setRemark(getString(row, 8));
        request.setCreatedBy(getString(row, 9));

        // 手动格式校验（导入不走 @Valid）
        if (isBlank(request.getVoucherNo())) {
            throw new BusinessException("凭证号不能为空");
        }
        if (isBlank(request.getFlowType())) {
            throw new BusinessException("收支类型不能为空");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额需为大于 0 的数字");
        }
        if (request.getOccurDate() == null) {
            throw new BusinessException("发生日期不能为空");
        }
        return request;
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && getString(row, c) != null && !getString(row, c).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getString(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return cell.toString().trim();
    }

    private BigDecimal getAmount(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String s = getString(row, idx);
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("金额格式错误：" + s);
        }
    }

    private LocalDate getDate(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            String s = cell.toString().trim();
            if (s.contains(" ")) {
                s = s.substring(0, s.indexOf(" "));
            }
            return LocalDate.parse(s);
        } catch (Exception e) {
            throw new BusinessException("发生日期格式错误(应为 YYYY-MM-DD)");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
