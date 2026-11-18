package com.sme.ledger.repository;

import com.sme.ledger.entity.KnowledgeDoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {

    /** 知识库文档列表，新上传的在前 */
    List<KnowledgeDoc> findAllByOrderByIdDesc();

    boolean existsByTitle(String title);
}
