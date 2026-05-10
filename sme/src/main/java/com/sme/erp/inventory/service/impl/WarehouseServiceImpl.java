package com.sme.erp.inventory.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.inventory.dto.WarehouseDTO;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.mapper.WarehouseMapper;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.WarehouseService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;

    public WarehouseServiceImpl(WarehouseRepository repository, WarehouseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public WarehouseDTO save(WarehouseDTO dto) {
        dto.setCode(normalizeRequired(dto.getCode(), "Warehouse code"));
        dto.setName(normalizeRequired(dto.getName(), "Warehouse name"));
        dto.setLocation(normalize(dto.getLocation()));
        dto.setDescription(normalize(dto.getDescription()));
        validateCodeUnique(dto.getCode(), dto.getId());

        Warehouse entity = getWarehouseForSave(dto.getId());
        mapper.updateEntity(dto, entity);
        Warehouse saved = repository.save(entity);

        return mapper.toDTO(saved);
    }

    @Override
    public List<WarehouseDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseDTO getById(Long id) {
        return mapper.toDTO(findWarehouseById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findWarehouseById(id));
    }

    private void validateCodeUnique(String code, Long currentId) {
        boolean exists = currentId == null
                ? repository.existsByWarehouseCode(code)
                : repository.existsByWarehouseCodeAndIdNot(code, currentId);

        if (exists) {
            throw new DuplicateResourceException("Warehouse code already exists: " + code);
        }
    }

    private Warehouse getWarehouseForSave(Long id) {
        return id == null ? new Warehouse() : findWarehouseById(id);
    }

    private Warehouse findWarehouseById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
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
