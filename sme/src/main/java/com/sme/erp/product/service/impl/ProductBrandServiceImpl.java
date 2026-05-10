package com.sme.erp.product.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.product.dto.ProductBrandDTO;
import com.sme.erp.product.entity.ProductBrand;
import com.sme.erp.product.mapper.ProductBrandMapper;
import com.sme.erp.product.repository.ProductBrandRepository;
import com.sme.erp.product.service.ProductBrandService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductBrandServiceImpl implements ProductBrandService {

    private final ProductBrandRepository repository;
    private final ProductBrandMapper mapper;

    public ProductBrandServiceImpl(ProductBrandRepository repository,
                                   ProductBrandMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ProductBrandDTO save(ProductBrandDTO dto) {
        dto.setCode(normalizeRequired(dto.getCode(), "Brand code"));
        dto.setBrandName(normalizeRequired(dto.getBrandName(), "Brand name"));
        validateCodeUnique(dto.getCode(), dto.getId());

        ProductBrand brand = getBrandForSave(dto.getId());
        mapper.updateEntity(dto, brand);
        ProductBrand saved = repository.save(brand);

        return mapper.toDTO(saved);
    }

    @Override
    public List<ProductBrandDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductBrandDTO getById(Long id) {
        return mapper.toDTO(findBrandById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findBrandById(id));
    }

    private void validateCodeUnique(String code, Long currentId) {
        boolean exists = currentId == null
                ? repository.existsByCode(code)
                : repository.existsByCodeAndIdNot(code, currentId);

        if (exists) {
            throw new DuplicateResourceException("Brand code already exists: " + code);
        }
    }

    private ProductBrand getBrandForSave(Long id) {
        return id == null ? new ProductBrand() : findBrandById(id);
    }

    private ProductBrand findBrandById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
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
