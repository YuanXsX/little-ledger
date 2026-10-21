package com.sme.ledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.BackupData;
import com.sme.ledger.entity.Alert;
import com.sme.ledger.entity.BackupConfig;
import com.sme.ledger.entity.BackupLog;
import com.sme.ledger.entity.Contact;
import com.sme.ledger.entity.CustomerLedger;
import com.sme.ledger.entity.EnterpriseInfo;
import com.sme.ledger.entity.FinanceLedger;
import com.sme.ledger.entity.InventoryLedger;
import com.sme.ledger.entity.LedgerCategory;
import com.sme.ledger.entity.LedgerField;
import com.sme.ledger.entity.RestoreLog;
import com.sme.ledger.repository.AlertRepository;
import com.sme.ledger.repository.BackupConfigRepository;
import com.sme.ledger.repository.BackupLogRepository;
import com.sme.ledger.repository.ContactRepository;
import com.sme.ledger.repository.CustomerLedgerRepository;
import com.sme.ledger.repository.EnterpriseInfoRepository;
import com.sme.ledger.repository.FinanceLedgerRepository;
import com.sme.ledger.repository.InventoryLedgerRepository;
import com.sme.ledger.repository.LedgerCategoryRepository;
import com.sme.ledger.repository.LedgerFieldRepository;
import com.sme.ledger.repository.RestoreLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据备份与恢复：全量 JSON 快照落盘、失败重试 3 次并预警、快速/定点恢复。
 * 存储位置 local 真实写盘；oss/dual 为演示模拟（同样落本地并记录 storage 标识，实际接入需云存储凭证）。
 */
@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final int MAX_RETRY = 3;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ObjectMapper objectMapper;
    private final String backupDir;

    private final EnterpriseInfoRepository enterpriseRepository;
    private final ContactRepository contactRepository;
    private final LedgerCategoryRepository categoryRepository;
    private final LedgerFieldRepository fieldRepository;
    private final FinanceLedgerRepository financeRepository;
    private final InventoryLedgerRepository inventoryRepository;
    private final CustomerLedgerRepository customerRepository;
    private final AlertRepository alertRepository;
    private final BackupLogRepository backupLogRepository;
    private final RestoreLogRepository restoreLogRepository;
    private final BackupConfigRepository backupConfigRepository;

    public BackupService(ObjectMapper objectMapper,
                         @Value("${app.backup-dir:./backups}") String backupDir,
                         EnterpriseInfoRepository enterpriseRepository,
                         ContactRepository contactRepository,
                         LedgerCategoryRepository categoryRepository,
                         LedgerFieldRepository fieldRepository,
                         FinanceLedgerRepository financeRepository,
                         InventoryLedgerRepository inventoryRepository,
                         CustomerLedgerRepository customerRepository,
                         AlertRepository alertRepository,
                         BackupLogRepository backupLogRepository,
                         RestoreLogRepository restoreLogRepository,
                         BackupConfigRepository backupConfigRepository) {
        this.objectMapper = objectMapper;
        this.backupDir = backupDir;
        this.enterpriseRepository = enterpriseRepository;
        this.contactRepository = contactRepository;
        this.categoryRepository = categoryRepository;
        this.fieldRepository = fieldRepository;
        this.financeRepository = financeRepository;
        this.inventoryRepository = inventoryRepository;
        this.customerRepository = customerRepository;
        this.alertRepository = alertRepository;
        this.backupLogRepository = backupLogRepository;
        this.restoreLogRepository = restoreLogRepository;
        this.backupConfigRepository = backupConfigRepository;
    }

    /** 执行备份（自动/手动），失败重试 3 次，仍失败记录并预警通知管理员。 */
    public BackupLog backup(String backupType) {
        BackupConfig config = getConfig();
        BackupLog record = new BackupLog();
        record.setBackupType(backupType);
        record.setStorage(config.getStorage());
        record.setBackupTime(LocalDateTime.now());

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                String filePath = doBackup(record.getBackupTime());
                record.setFilePath(filePath);
                record.setStatus("success");
                record.setRetryCount(attempt - 1);
                return backupLogRepository.save(record);
            } catch (Exception e) {
                record.setRetryCount(attempt);
                record.setErrorMsg(e.getMessage());
                log.warn("备份失败（第 {} 次）：{}", attempt, e.getMessage());
                if (attempt == MAX_RETRY) {
                    record.setStatus("failed");
                    backupLogRepository.save(record);
                    notifyAdmin(e);
                    return record;
                }
            }
        }
        return record;
    }

    public List<BackupLog> listBackups() {
        return backupLogRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<RestoreLog> listRestores() {
        return restoreLogRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public BackupConfig getConfig() {
        return backupConfigRepository.findAll().stream().findFirst()
                .orElseGet(() -> backupConfigRepository.save(new BackupConfig()));
    }

    public BackupConfig updateConfig(String frequency, String storage) {
        BackupConfig config = getConfig();
        if (frequency != null && !frequency.isBlank()) {
            config.setFrequency(frequency);
        }
        if (storage != null && !storage.isBlank()) {
            config.setStorage(storage);
        }
        return backupConfigRepository.save(config);
    }

    /** 定点恢复：恢复到指定备份点。 */
    @Transactional
    public RestoreLog restore(Long backupId, String operator) {
        BackupLog record = backupLogRepository.findById(backupId)
                .orElseThrow(() -> new BusinessException("备份记录不存在"));
        if (!"success".equals(record.getStatus())) {
            throw new BusinessException("该备份点不可用（状态：" + record.getStatus() + "）");
        }
        applyRestore(readBackup(record.getFilePath()));
        return recordRestore(record, operator);
    }

    /** 快速恢复：恢复到最近一次成功备份。 */
    @Transactional
    public RestoreLog restoreLatest(String operator) {
        BackupLog record = backupLogRepository.findFirstByStatusOrderByIdDesc("success")
                .orElseThrow(() -> new BusinessException("暂无可用备份"));
        applyRestore(readBackup(record.getFilePath()));
        return recordRestore(record, operator);
    }

    private String doBackup(LocalDateTime time) throws IOException {
        BackupData data = new BackupData(
                enterpriseRepository.findAll(),
                contactRepository.findAll(),
                categoryRepository.findAll(),
                fieldRepository.findAll(),
                financeRepository.findAll(),
                inventoryRepository.findAll(),
                customerRepository.findAll());
        Path dir = Paths.get(backupDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path file = dir.resolve("backup-" + time.format(TS) + ".json");
        Files.writeString(file, objectMapper.writeValueAsString(data));
        return file.toString();
    }

    private BackupData readBackup(String filePath) {
        try {
            return objectMapper.readValue(Paths.get(filePath).toFile(), BackupData.class);
        } catch (IOException e) {
            throw new BusinessException(500, "备份文件读取失败: " + e.getMessage());
        }
    }

    /** 恢复：清空现有业务数据后按依赖顺序重新导入，并重映射分类外键。 */
    private void applyRestore(BackupData data) {
        // 用 deleteAllInBatch 立即批量删除，避免 Hibernate 默认「先 insert 后 delete」触发唯一约束冲突
        fieldRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
        inventoryRepository.deleteAllInBatch();
        financeRepository.deleteAllInBatch();
        contactRepository.deleteAllInBatch();
        enterpriseRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();

        resetIds(data.enterprises());
        enterpriseRepository.saveAll(data.enterprises());
        resetIds(data.contacts());
        contactRepository.saveAll(data.contacts());

        Map<Long, Long> categoryIdMap = new HashMap<>();
        for (LedgerCategory c : data.categories()) {
            Long oldId = c.getId();
            c.setId(null);
            categoryIdMap.put(oldId, categoryRepository.save(c).getId());
        }
        for (LedgerField f : data.fields()) {
            Long oldCatId = f.getCategoryId();
            f.setId(null);
            if (oldCatId != null && categoryIdMap.containsKey(oldCatId)) {
                f.setCategoryId(categoryIdMap.get(oldCatId));
            }
            fieldRepository.save(f);
        }

        resetIds(data.finances());
        financeRepository.saveAll(data.finances());
        resetIds(data.inventories());
        inventoryRepository.saveAll(data.inventories());
        resetIds(data.customers());
        customerRepository.saveAll(data.customers());
    }

    private RestoreLog recordRestore(BackupLog record, String operator) {
        RestoreLog restore = new RestoreLog();
        restore.setOperator(operator);
        restore.setBackupId(record.getId());
        restore.setBackupTime(record.getBackupTime());
        restore.setRestoredAt(LocalDateTime.now());
        return restoreLogRepository.save(restore);
    }

    /** 备份失败通知管理员：生成系统内预警（消息中心后续模块消费）。 */
    private void notifyAdmin(Exception e) {
        try {
            Alert alert = new Alert();
            alert.setAlertType("备份失败");
            alert.setTarget("全量数据备份");
            alert.setCurrentVal(truncate(e.getMessage(), 64));
            alert.setStatus(Alert.STATUS_UNRESOLVED);
            alertRepository.save(alert);
        } catch (Exception ex) {
            log.error("写入备份失败预警失败", ex);
        }
    }

    /** 清空实体 id，使 save 走 insert 生成新主键（IDENTITY 忽略外部 id）。 */
    private static void resetIds(List<?> entities) {
        for (Object entity : entities) {
            try {
                java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(entity, null);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                // 无 id 字段则忽略
            }
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
