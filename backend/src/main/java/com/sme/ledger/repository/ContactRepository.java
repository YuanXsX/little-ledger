package com.sme.ledger.repository;

import com.sme.ledger.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    /** 按联系人类型筛选 */
    Page<Contact> findByContactType(String contactType, Pageable pageable);

    Page<Contact> findByContactTypeContaining(String contactType, Pageable pageable);
}
