package com.sme.ledger.service;

import com.sme.ledger.common.BusinessException;
import com.sme.ledger.dto.LedgerCategoryRequest;
import com.sme.ledger.dto.LedgerFieldRequest;
import com.sme.ledger.entity.LedgerCategory;
import com.sme.ledger.entity.LedgerField;
import com.sme.ledger.repository.LedgerCategoryRepository;
import com.sme.ledger.repository.LedgerFieldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 台账分类与自定义字段业务逻辑。
 */
@Service
public class LedgerCategoryService {

    private final LedgerCategoryRepository categoryRepository;
    private final LedgerFieldRepository fieldRepository;

    public LedgerCategoryService(LedgerCategoryRepository categoryRepository,
                                 LedgerFieldRepository fieldRepository) {
        this.categoryRepository = categoryRepository;
        this.fieldRepository = fieldRepository;
    }

    public List<LedgerCategory> listCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public LedgerCategory createCategory(LedgerCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("分类名称已存在");
        }
        LedgerCategory category = new LedgerCategory();
        category.setName(request.getName());
        category.setPreset(false);
        category.setDescription(request.getDescription());
        category.setCreatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        LedgerCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        if (category.isPreset()) {
            throw new BusinessException("预设分类不可删除");
        }
        categoryRepository.deleteById(id);
    }

    public List<LedgerField> listFields(Long categoryId) {
        return fieldRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public LedgerField createField(LedgerFieldRequest request) {
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new BusinessException("所属分类不存在");
        }
        LedgerField field = new LedgerField();
        field.setCategoryId(request.getCategoryId());
        field.setFieldKey(request.getFieldKey());
        field.setFieldName(request.getFieldName());
        field.setFieldType(request.getFieldType());
        field.setRequired(request.isRequired());
        return fieldRepository.save(field);
    }

    @Transactional
    public void deleteField(Long id) {
        fieldRepository.deleteById(id);
    }
}
