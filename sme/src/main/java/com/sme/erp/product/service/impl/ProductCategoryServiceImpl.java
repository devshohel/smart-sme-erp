package com.sme.erp.product.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.product.dto.ProductCategoryDTO;
import com.sme.erp.product.entity.ProductCategory;
import com.sme.erp.product.mapper.ProductCategoryMapper;
import com.sme.erp.product.repository.ProductCategoryRepository;
import com.sme.erp.product.service.ProductCategoryService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository repository;

    public ProductCategoryServiceImpl(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    // ✅ SAVE
    @Override
    @Transactional
    public ProductCategoryDTO save(ProductCategoryDTO dto) {
        dto.setCode(normalizeRequired(dto.getCode(), "Category code"));
        dto.setCategoryName(normalizeRequired(dto.getCategoryName(), "Category name"));
        dto.setDescription(normalize(dto.getDescription()));
        validateCodeUnique(dto.getCode(), dto.getId());

        ProductCategory entity = getCategoryForSave(dto.getId());
        ProductCategoryMapper.updateEntity(dto, entity);

        if (dto.getParentCategoryId() != null) {
            entity.setParentCategory(findCategoryById(dto.getParentCategoryId(), "Parent category"));
        }

        ProductCategory saved = repository.save(entity);

        return ProductCategoryMapper.toDTO(saved);
    }

    // ✅ GET ALL
    @Override
    public List<ProductCategoryDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(ProductCategoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ DELETE
    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findCategoryById(id, "Category"));
    }

    private void validateCodeUnique(String code, Long currentId) {
        boolean exists = currentId == null
                ? repository.existsByCode(code)
                : repository.existsByCodeAndIdNot(code, currentId);

        if (exists) {
            throw new DuplicateResourceException("Category code already exists: " + code);
        }
    }

    private ProductCategory getCategoryForSave(Long id) {
        return id == null ? new ProductCategory() : findCategoryById(id, "Category");
    }

    private ProductCategory findCategoryById(Long id, String label) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found with id: " + id));
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new BadRequestException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
