package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.EnterpriseInfoRequest;
import com.sme.ledger.entity.Alert;
import com.sme.ledger.entity.EnterpriseInfo;
import com.sme.ledger.entity.UpdateLog;
import com.sme.ledger.repository.AlertRepository;
import com.sme.ledger.repository.EnterpriseInfoRepository;
import com.sme.ledger.repository.UpdateLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业资质信息业务逻辑（重复校验 + 更新日志 + 到期提醒）。
 */
@Service
public class EnterpriseService {

    private final EnterpriseInfoRepository repository;
    private final UpdateLogRepository updateLogRepository;
    private final AlertRepository alertRepository;

    public EnterpriseService(EnterpriseInfoRepository repository,
                             UpdateLogRepository updateLogRepository,
                             AlertRepository alertRepository) {
        this.repository = repository;
        this.updateLogRepository = updateLogRepository;
        this.alertRepository = alertRepository;
    }

    public PageResult<EnterpriseInfo> list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<EnterpriseInfo> result = repository.findAll(pageable);
        return PageResult.of(result);
    }

    public EnterpriseInfo getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("企业资质信息不存在"));
    }

    @Transactional
    public EnterpriseInfo create(EnterpriseInfoRequest request) {
        // 自动重复校验：营业执照编号唯一
        if (repository.existsByLicenseNo(request.getLicenseNo())) {
            throw new BusinessException("营业执照编号已存在，请勿重复录入");
        }
        EnterpriseInfo entity = toEntity(request);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    @Transactional
    public EnterpriseInfo update(Long id, EnterpriseInfoRequest request, String operator, String reason) {
        EnterpriseInfo entity = getById(id);
        entity.setBusinessLicense(request.getBusinessLicense());
        entity.setBusinessScope(request.getBusinessScope());
        entity.setRegisteredAddress(request.getRegisteredAddress());
        entity.setEstablishDate(request.getEstablishDate());
        entity.setLicenseExpireDate(request.getLicenseExpireDate());
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);

        writeLog("enterprise", id, operator, reason);
        return entity;
    }

    @Transactional
    public EnterpriseInfo updateLicenseFile(Long id, String filePath) {
        EnterpriseInfo entity = getById(id);
        entity.setLicenseFile(filePath);
        entity.setUpdatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    /** 查询未来 days 天内到期的资质 */
    public List<EnterpriseInfo> findExpiring(int days) {
        LocalDate now = LocalDate.now();
        return repository.findByLicenseExpireDateBetween(now, now.plusDays(days));
    }

    /** 检查到期资质并生成预警（未解决去重），返回新增预警数 */
    @Transactional
    public int checkAndAlertExpiry(int days) {
        int count = 0;
        for (EnterpriseInfo e : findExpiring(days)) {
            boolean exists = alertRepository.existsByAlertTypeAndTargetAndStatus(
                    "资质到期", e.getLicenseNo(), Alert.STATUS_UNRESOLVED);
            if (!exists) {
                Alert alert = new Alert();
                alert.setAlertType("资质到期");
                alert.setTarget(e.getLicenseNo());
                alert.setCurrentVal(e.getLicenseExpireDate().toString());
                alert.setStatus(Alert.STATUS_UNRESOLVED);
                alert.setCreatedAt(LocalDateTime.now());
                alertRepository.save(alert);
                count++;
            }
        }
        return count;
    }

    private EnterpriseInfo toEntity(EnterpriseInfoRequest request) {
        EnterpriseInfo entity = new EnterpriseInfo();
        entity.setLicenseNo(request.getLicenseNo());
        entity.setBusinessLicense(request.getBusinessLicense());
        entity.setBusinessScope(request.getBusinessScope());
        entity.setRegisteredAddress(request.getRegisteredAddress());
        entity.setEstablishDate(request.getEstablishDate());
        entity.setLicenseFile(request.getLicenseFile());
        entity.setLicenseExpireDate(request.getLicenseExpireDate());
        return entity;
    }

    private void writeLog(String bizType, Long bizId, String operator, String reason) {
        UpdateLog log = new UpdateLog();
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setOperator(operator);
        log.setReason(reason);
        log.setCreatedAt(LocalDateTime.now());
        updateLogRepository.save(log);
    }
}
