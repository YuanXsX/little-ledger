package com.sme.ledger.repository;

import com.sme.ledger.entity.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk> findByDocIdOrderByChunkIndex(Long docId);

    void deleteByDocId(Long docId);
}
