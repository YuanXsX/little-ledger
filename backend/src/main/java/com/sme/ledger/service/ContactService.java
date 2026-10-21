package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.common.PageResult;
import com.sme.ledger.dto.ContactRequest;
import com.sme.ledger.dto.ContactResponse;
import com.sme.ledger.entity.Contact;
import com.sme.ledger.entity.UpdateLog;
import com.sme.ledger.repository.ContactRepository;
import com.sme.ledger.repository.UpdateLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 联系人业务逻辑（脱敏展示 + 更新日志）。
 */
@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final UpdateLogRepository updateLogRepository;

    public ContactService(ContactRepository contactRepository, UpdateLogRepository updateLogRepository) {
        this.contactRepository = contactRepository;
        this.updateLogRepository = updateLogRepository;
    }

    /** 分页查询，可按联系人类型筛选 */
    public PageResult<ContactResponse> list(String contactType, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Contact> result = (contactType == null || contactType.isBlank())
                ? contactRepository.findAll(pageable)
                : contactRepository.findByContactType(contactType, pageable);
        Page<ContactResponse> mapped = result.map(ContactResponse::from);
        return PageResult.of(mapped);
    }

    public ContactResponse getById(Long id) {
        return ContactResponse.from(findEntity(id));
    }

    @Transactional
    public ContactResponse create(ContactRequest request) {
        validatePhone(request.getPhone());
        validateEmail(request.getEmail());
        Contact contact = new Contact();
        apply(contact, request);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        return ContactResponse.from(contactRepository.save(contact));
    }

    @Transactional
    public ContactResponse update(Long id, ContactRequest request, String operator, String reason) {
        validatePhone(request.getPhone());
        validateEmail(request.getEmail());
        Contact contact = findEntity(id);
        // 编辑表单回填的是脱敏值，若传入值含 * 视为未修改，避免把原文覆盖成脱敏值
        applyOnlyChanged(contact, request);
        contact.setUpdatedAt(LocalDateTime.now());
        contactRepository.save(contact);

        writeLog("contact", id, operator, reason);
        return ContactResponse.from(contact);
    }

    @Transactional
    public void delete(Long id, String operator) {
        findEntity(id);
        contactRepository.deleteById(id);
        writeLog("contact", id, operator, "删除联系人");
    }

    public List<UpdateLog> logs(Long id) {
        return updateLogRepository.findByBizTypeAndBizIdOrderByCreatedAtDesc("contact", id);
    }

    private Contact findEntity(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new BusinessException("联系人不存在"));
    }

    private void apply(Contact contact, ContactRequest request) {
        contact.setContactType(request.getContactType());
        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setCompany(request.getCompany());
        contact.setPosition(request.getPosition());
    }

    private void applyOnlyChanged(Contact contact, ContactRequest request) {
        contact.setContactType(request.getContactType());
        contact.setName(request.getName());
        if (request.getPhone() != null && !request.getPhone().contains("*")) {
            contact.setPhone(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().contains("*")) {
            contact.setEmail(request.getEmail());
        }
        contact.setCompany(request.getCompany());
        contact.setPosition(request.getPosition());
    }

    /** 手机号校验：空值或脱敏值(含*)跳过，否则需为 11 位数字 */
    private void validatePhone(String phone) {
        if (phone == null || phone.isBlank() || phone.contains("*")) {
            return;
        }
        if (!phone.matches("\\d{11}")) {
            throw new BusinessException("手机号需为 11 位数字");
        }
    }

    /** 邮箱校验：空值或脱敏值(含*)跳过，否则需符合邮箱格式 */
    private void validateEmail(String email) {
        if (email == null || email.isBlank() || email.contains("*")) {
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new BusinessException("邮箱格式不正确");
        }
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
