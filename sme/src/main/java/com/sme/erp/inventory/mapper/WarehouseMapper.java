package com.sme.erp.inventory.mapper;

import com.sme.erp.inventory.dto.WarehouseDTO;
import com.sme.erp.inventory.entity.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {

    public WarehouseDTO toDTO(Warehouse entity) {
        if (entity == null) return null;

        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getWarehouseCode());
        dto.setName(entity.getName());
        dto.setLocation(entity.getLocation());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive() != null ? entity.getActive() : Boolean.TRUE);

        return dto;
    }

    public Warehouse toEntity(WarehouseDTO dto) {
        if (dto == null) return null;

        return updateEntity(dto, new Warehouse());
    }

    public Warehouse updateEntity(WarehouseDTO dto, Warehouse entity) {
        if (dto == null || entity == null) return entity;

        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        if (dto.getCode() != null) {
            entity.setWarehouseCode(dto.getCode());
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getLocation() != null) {
            entity.setLocation(dto.getLocation());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        return entity;
    }
}
