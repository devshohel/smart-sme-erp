package com.sme.erp.product.service.impl;

import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
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
    public synchronized ProductBrandDTO save(ProductBrandDTO dto) {
        String normalizedCode = RequestValueUtils.normalize(dto.getCode());
        if (dto.getId() == null && normalizedCode == null) {
            normalizedCode = generateCode();
        }
        dto.setCode(RequestValueUtils.normalizeRequired(normalizedCode, "Brand code"));
        dto.setBrandName(RequestValueUtils.normalizeRequired(dto.getBrandName(), "Brand name"));
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
    @Transactional(readOnly = true)
    public List<ProductBrandDTO> getDeleted() {
        return repository.findDeletedBrands()
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

    @Override
    @Transactional
    public ProductBrandDTO restore(Long id) {
        int updated = repository.restoreById(id);
        if (updated == 0) {
            throw new ResourceNotFoundException("Brand not found with id: " + id);
        }
        return mapper.toDTO(findBrandById(id));
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

    private String generateCode() {
        long sequence = repository.findMaxId() + 1;
        String code;
        do {
            code = "BRD-" + String.format("%04d", sequence++);
        } while (repository.existsByCode(code));
        return code;
    }
}
