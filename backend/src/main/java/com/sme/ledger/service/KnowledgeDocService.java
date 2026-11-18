package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.entity.KnowledgeChunk;
import com.sme.ledger.entity.KnowledgeDoc;
import com.sme.ledger.repository.KnowledgeChunkRepository;
import com.sme.ledger.repository.KnowledgeDocRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 财税知识库维护：文档上传 → 文本解析 → 分块入库（PENDING → PARSED / FAILED）。
 * 支持 txt / md / docx / pdf 四种格式。
 */
@Service
public class KnowledgeDocService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeDocService.class);

    /** 分块大小（字符）与块间重叠，兼顾检索粒度与上下文完整 */
    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 60;

    private static final Set<String> ALLOWED_EXT = Set.of(".txt", ".md", ".docx", ".pdf");

    private final KnowledgeDocRepository docRepository;
    private final KnowledgeChunkRepository chunkRepository;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public KnowledgeDocService(KnowledgeDocRepository docRepository,
                               KnowledgeChunkRepository chunkRepository) {
        this.docRepository = docRepository;
        this.chunkRepository = chunkRepository;
    }

    /** 知识库文档列表（含解析状态），新上传的在前 */
    public List<KnowledgeDoc> list() {
        return docRepository.findAllByOrderByIdDesc();
    }

    /**
     * 上传并解析知识库文档。
     * 解析成功 → PARSED；失败 → FAILED（记录失败原因，不阻断其他文档使用）。
     */
    @Transactional
    public KnowledgeDoc upload(MultipartFile file, String title, String username) {
        String original = file.getOriginalFilename();
        String lower = original == null ? "" : original.toLowerCase();
        String ext = lower.substring(lower.lastIndexOf('.'));
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 txt / md / docx / pdf 格式文档");
        }
        if (file.isEmpty()) {
            throw new BusinessException("文件内容为空");
        }

        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(title == null || title.isBlank() ? original : title.trim());
        doc.setCreatedBy(username);
        doc.setCreatedAt(LocalDateTime.now());

        try {
            // 保存源文件（与资质文件同一上传目录）
            String storedName = UUID.randomUUID() + ext;
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(storedName));
            doc.setFilePath(storedName);

            // 解析文本并分块入库
            String text = extractText(dir.resolve(storedName), ext);
            if (text.isBlank()) {
                throw new BusinessException("未能从文档中提取到文本内容");
            }
            List<String> pieces = chunk(text);
            doc.setStatus(KnowledgeDoc.STATUS_PARSED);
            docRepository.save(doc);
            for (int i = 0; i < pieces.size(); i++) {
                chunkRepository.save(newChunk(doc.getId(), i, pieces.get(i)));
            }
            log.info("知识库文档入库成功: {}（{} 个分块）", doc.getTitle(), pieces.size());
        } catch (Exception e) {
            doc.setStatus(KnowledgeDoc.STATUS_FAILED);
            doc.setRemark("解析失败: " + e.getMessage());
            log.warn("知识库文档解析失败: {}", e.getMessage());
        }
        return docRepository.save(doc);
    }

    /** 删除文档及其全部分块（源文件保留在磁盘，可人工清理） */
    @Transactional
    public void delete(Long id) {
        KnowledgeDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文档不存在"));
        chunkRepository.deleteByDocId(id);
        docRepository.delete(doc);
    }

    /**
     * 内置示例知识入库（无源文件）。标题重复时跳过，保证幂等。
     *
     * @return 是否新增
     */
    @Transactional
    public boolean saveFromText(String title, String content, String createdBy) {
        if (docRepository.existsByTitle(title)) {
            return false;
        }
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(title);
        doc.setStatus(KnowledgeDoc.STATUS_PARSED);
        doc.setCreatedBy(createdBy);
        doc.setRemark("系统内置示例文档");
        doc.setCreatedAt(LocalDateTime.now());
        docRepository.save(doc);
        List<String> pieces = chunk(content);
        for (int i = 0; i < pieces.size(); i++) {
            chunkRepository.save(newChunk(doc.getId(), i, pieces.get(i)));
        }
        return true;
    }

    // ---------- 文本解析 ----------

    /** 按扩展名解析文档为纯文本 */
    private String extractText(Path file, String ext) throws IOException {
        if (ext.equals(".txt") || ext.equals(".md")) {
            return readPlainText(file);
        }
        if (ext.equals(".docx")) {
            return extractDocx(file);
        }
        return extractPdf(file);
    }

    /** txt/md：优先 UTF-8，出现乱码替换符时回退 GBK（中文环境常见编码） */
    private static String readPlainText(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (utf8.contains("�")) {
            return new String(bytes, java.nio.charset.Charset.forName("GBK"));
        }
        return utf8;
    }

    /** docx：POI 提取正文（段落 + 表格文本） */
    private static String extractDocx(Path file) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(Files.newInputStream(file));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    /** pdf：PDFBox 提取全文 */
    private static String extractPdf(Path file) throws IOException {
        try (var pdf = Loader.loadPDF(file.toFile())) {
            return new PDFTextStripper().getText(pdf);
        }
    }

    // ---------- 分块 ----------

    /** 固定长度分块（带重叠），优先在换行/句号处断开，保证语义完整 */
    static List<String> chunk(String text) {
        List<String> pieces = new ArrayList<>();
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        int len = normalized.length();
        if (len == 0) {
            return pieces;
        }
        int start = 0;
        while (start < len) {
            int end = Math.min(start + CHUNK_SIZE, len);
            if (end < len) {
                // 向后回退到最近的断句点（最多回退 CHUNK_SIZE/4）
                int cut = end;
                for (int i = end; i > end - CHUNK_SIZE / 4 && i > start; i--) {
                    char c = normalized.charAt(i - 1);
                    if (c == '\n' || c == '。' || c == '！' || c == '？') {
                        cut = i;
                        break;
                    }
                }
                end = cut;
            }
            String piece = normalized.substring(start, end).trim();
            if (!piece.isEmpty()) {
                pieces.add(piece);
            }
            if (end >= len) {
                break;
            }
            start = Math.max(end - CHUNK_OVERLAP, start + 1);
        }
        return pieces;
    }

    private static KnowledgeChunk newChunk(Long docId, int index, String content) {
        KnowledgeChunk c = new KnowledgeChunk();
        c.setDocId(docId);
        c.setChunkIndex(index);
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
}
