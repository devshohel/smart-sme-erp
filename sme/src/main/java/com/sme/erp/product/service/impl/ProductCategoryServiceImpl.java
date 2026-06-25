package com.sme.erp.product.service.impl;

import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
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
    private final ProductCategoryMapper mapper;

    public ProductCategoryServiceImpl(ProductCategoryRepository repository,
                                      ProductCategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ✅ SAVE
    @Override
    @Transactional
    public ProductCategoryDTO save(ProductCategoryDTO dto) {
        dto.setCode(RequestValueUtils.normalizeRequired(dto.getCode(), "Category code"));
        dto.setCategoryName(RequestValueUtils.normalizeRequired(dto.getCategoryName(), "Category name"));
        dto.setDescription(RequestValueUtils.normalize(dto.getDescription()));
        validateCodeUnique(dto.getCode(), dto.getId());

        ProductCategory entity = getCategoryForSave(dto.getId());
        mapper.updateEntity(dto, entity);

        if (dto.getParentCategoryId() != null) {
            entity.setParentCategory(findCategoryById(dto.getParentCategoryId(), "Parent category"));
        } else {
            entity.setParentCategory(null);
        }

        ProductCategory saved = repository.save(entity);

        return mapper.toDTO(saved);
    }

    // ✅ GET ALL
    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> getDeleted() {
        return repository.findDeletedCategories()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ DELETE
    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findCategoryById(id, "Category"));
    }

    @Override
    @Transactional
    public ProductCategoryDTO restore(Long id) {
        int updated = repository.restoreById(id);
        if (updated == 0) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        return mapper.toDTO(findCategoryById(id, "Category"));
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
}
