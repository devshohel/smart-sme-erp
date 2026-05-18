package com.sme.erp.product.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.product.dto.UomDTO;
import com.sme.erp.product.entity.Uom;
import com.sme.erp.product.mapper.UomMapper;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.product.service.UomService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UomServiceImpl implements UomService {

    private final UomRepository repository;
    private final UomMapper mapper;

    public UomServiceImpl(UomRepository repository, UomMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public UomDTO save(UomDTO dto) {
        dto.setCode(RequestValueUtils.normalizeRequired(dto.getCode(), "UOM code"));
        dto.setName(RequestValueUtils.normalizeRequired(dto.getName(), "UOM name"));
        dto.setType(RequestValueUtils.normalize(dto.getType()));
        validateBusinessRules(dto);
        validateCodeUnique(dto.getCode(), dto.getId());
        Uom uom = getUomForSave(dto.getId());
        mapper.updateEntity(dto, uom);
        Uom saved = repository.save(uom);
        return mapper.toDTO(saved);
    }

    @Override
    public List<UomDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UomDTO getById(Long id) {
        return mapper.toDTO(findUomById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findUomById(id));
    }

    private void validateCodeUnique(String code, Long currentId) {
        boolean exists = currentId == null
                ? repository.existsByCode(code)
                : repository.existsByCodeAndIdNot(code, currentId);

        if (exists) {
            throw new DuplicateResourceException("UOM code already exists: " + code);
        }
    }

    private Uom getUomForSave(Long id) {
        return id == null ? new Uom() : findUomById(id);
    }

    private Uom findUomById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UOM not found with id: " + id));
    }

    private void validateBusinessRules(UomDTO dto) {
        if (dto.getConversionFactor() != null && dto.getConversionFactor().signum() <= 0) {
            throw new BadRequestException("Conversion factor must be positive");
        }
    }
}
