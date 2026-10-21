package com.sme.ledger.controller;

import com.sme.ledger.common.ApiResult;
import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.ImportResult;
import com.sme.ledger.service.ImportService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 批量导入接口。
 */
@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    /** 批量导入财务流水（Excel） */
    @PostMapping("/{ledgerType}")
    public ApiResult<ImportResult> importData(@PathVariable String ledgerType,
                                              @RequestParam("file") MultipartFile file) {
        if (!"finance".equals(ledgerType)) {
            throw new BusinessException("暂不支持导入该台账类型：" + ledgerType);
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            throw new BusinessException("请上传 Excel(.xlsx) 文件");
        }
        return ApiResult.ok(importService.importFinance(file));
    }
}
