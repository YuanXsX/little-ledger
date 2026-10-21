package com.sme.ledger.repository;

import com.sme.ledger.entity.EnterpriseInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnterpriseInfoRepository extends JpaRepository<EnterpriseInfo, Long> {

    /** 按营业执照编号查询，用于重复校验 */
    Optional<EnterpriseInfo> findByLicenseNo(String licenseNo);

    boolean existsByLicenseNo(String licenseNo);

    /** 查询有效期落在 [start, end] 区间的资质，用于到期提醒 */
    List<EnterpriseInfo> findByLicenseExpireDateBetween(LocalDate start, LocalDate end);
}
