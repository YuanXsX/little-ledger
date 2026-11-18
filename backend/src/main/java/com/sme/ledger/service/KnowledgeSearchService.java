package com.sme.ledger.service;

import com.sme.ledger.dto.KnowledgeSource;
import com.sme.ledger.entity.KnowledgeChunk;
import com.sme.ledger.entity.KnowledgeDoc;
import com.sme.ledger.repository.KnowledgeChunkRepository;
import com.sme.ledger.repository.KnowledgeDocRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 轻量关键词检索（架构文档中的「轻量向量库」落地方案）：
 * 中文按二元组(bigram)分词、英文数字按词切分，TF-IDF 打分取 TopK。
 * 免依赖、免 embedding API，小微企业知识库规模（几十篇文档）下效果足够。
 */
@Service
public class KnowledgeSearchService {

    /** 检索返回的命中数量 */
    private static final int TOP_K = 3;

    /** 二元组权重高于单词，中文查询靠它匹配 */
    private static final double BIGRAM_WEIGHT = 2.0;

    /** 分块命中后，摘要片段的上下文半径（字符） */
    private static final int SNIPPET_RADIUS = 80;

    private final KnowledgeDocRepository docRepository;
    private final KnowledgeChunkRepository chunkRepository;

    public KnowledgeSearchService(KnowledgeDocRepository docRepository,
                                  KnowledgeChunkRepository chunkRepository) {
        this.docRepository = docRepository;
        this.chunkRepository = chunkRepository;
    }

    /** 检索结果：分块 + 得分 */
    public record Hit(KnowledgeChunk chunk, double score) {
    }

    /**
     * 检索与问题最相关的知识库片段。
     *
     * @return 带文档标题的命中片段列表（按相关度降序）
     */
    public List<KnowledgeSource> search(String question) {
        List<KnowledgeChunk> chunks = chunkRepository.findAll();
        if (chunks.isEmpty()) {
            return List.of();
        }
        // 文档标题映射，用于组装来源
        Map<Long, KnowledgeDoc> docById = new HashMap<>();
        docRepository.findAll().forEach(d -> docById.put(d.getId(), d));

        // 1. 查询分词（带权重），计算逆文档频率
        Map<String, Double> queryTerms = tokenize(question);
        int n = chunks.size();
        Map<String, Double> idf = new HashMap<>();
        for (String term : queryTerms.keySet()) {
            long df = chunks.stream().filter(c -> c.getContent().contains(term)).count();
            idf.put(term, Math.log(1.0 + (double) n / (1.0 + df)));
        }

        // 2. 逐分块打分：Σ 词权重 × 词频 × idf，按分块长度开方归一，避免长分块占优
        List<Hit> hits = new ArrayList<>();
        for (KnowledgeChunk chunk : chunks) {
            String content = chunk.getContent();
            double score = 0;
            for (Map.Entry<String, Double> e : queryTerms.entrySet()) {
                int tf = countOccurrences(content, e.getKey());
                if (tf > 0) {
                    score += e.getValue() * tf * idf.getOrDefault(e.getKey(), 0.0);
                }
            }
            // 整句/长子串直接命中给强加分（精确匹配 > 词粒度过小漏配）
            score += substringBonus(content, question);
            score /= Math.sqrt(Math.max(content.length(), 1));
            if (score > 0) {
                hits.add(new Hit(chunk, score));
            }
        }

        // 3. TopK + 组装来源片段
        return hits.stream()
                .sorted(Comparator.comparingDouble(Hit::score).reversed())
                .limit(TOP_K)
                .map(h -> {
                    KnowledgeDoc doc = docById.get(h.chunk().getDocId());
                    String title = doc == null ? "未知文档" : doc.getTitle();
                    return new KnowledgeSource(title, snippet(h.chunk().getContent(), question));
                })
                .toList();
    }

    /** 查询分词：中文连续串取二元组（权重 2.0）+ 单字兜底（权重 0.5）；英文/数字按词切分 */
    private static Map<String, Double> tokenize(String text) {
        Map<String, Double> terms = new HashMap<>();
        StringBuilder cjk = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                cjk.append(c);
            } else {
                flushCjk(cjk, terms);
                if (Character.isLetterOrDigit(c)) {
                    int j = i;
                    while (j < text.length() && Character.isLetterOrDigit(text.charAt(j))) {
                        j++;
                    }
                    String word = text.substring(i, j).toLowerCase();
                    if (word.length() >= 2) {
                        terms.merge(word, 1.0, Double::sum);
                    }
                    i = j - 1;
                }
            }
        }
        flushCjk(cjk, terms);
        return terms;
    }

    /** 把中文连续串展开为二元组 + 单字兜底（应对超短词，如「税」） */
    private static void flushCjk(StringBuilder cjk, Map<String, Double> terms) {
        if (cjk.isEmpty()) {
            return;
        }
        String s = cjk.toString();
        cjk.setLength(0);
        if (s.length() == 1) {
            terms.merge(s, BIGRAM_WEIGHT, Double::sum);
            return;
        }
        for (int i = 0; i < s.length() - 1; i++) {
            terms.merge(s.substring(i, i + 2), BIGRAM_WEIGHT, Double::sum);
        }
        // 长词截断为 4 字窗口，兼顾「增值税减免」这类长词
        if (s.length() >= 4) {
            for (int i = 0; i + 4 <= s.length(); i += 2) {
                terms.merge(s.substring(i, i + 4), BIGRAM_WEIGHT * 1.5, Double::sum);
            }
        }
    }

    /** 统计词在文本中的出现次数（线性扫描，知识库规模小，够用） */
    private static int countOccurrences(String text, String term) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(term, idx)) >= 0) {
            count++;
            idx += term.length();
        }
        return count;
    }

    /** 原问题（去标点后 ≥6 字）整段命中时加分 */
    private static double substringBonus(String content, String question) {
        String q = question.replaceAll("[\\s，。？！、；：,.?!;:'\"]+", "");
        if (q.length() < 6) {
            return 0;
        }
        double bonus = 0;
        // 尝试整段命中与子串命中（6 字步长递减），命中越长加分越高
        for (int len = Math.min(q.length(), 24); len >= 6; len--) {
            for (int i = 0; i + len <= q.length(); i++) {
                if (content.contains(q.substring(i, i + len))) {
                    bonus = Math.max(bonus, 2.0 * len);
                }
            }
        }
        return bonus;
    }

    /** 截取第一个命中的词/子串附近的片段 */
    private static String snippet(String content, String question) {
        String q = question.replaceAll("[\\s，。？！、；：,.?!;:'\"]+", "");
        int pos = -1;
        for (int len = Math.min(q.length(), 8); len >= 2 && pos < 0; len--) {
            for (int i = 0; i + len <= q.length() && pos < 0; i++) {
                pos = content.indexOf(q.substring(i, i + len));
            }
        }
        if (pos < 0) {
            return content.substring(0, Math.min(content.length(), SNIPPET_RADIUS * 2)) + "…";
        }
        int start = Math.max(0, pos - SNIPPET_RADIUS);
        int end = Math.min(content.length(), pos + SNIPPET_RADIUS);
        String s = content.substring(start, end).replaceAll("\\s+", " ").trim();
        return (start > 0 ? "…" : "") + s + (end < content.length() ? "…" : "");
    }
}
