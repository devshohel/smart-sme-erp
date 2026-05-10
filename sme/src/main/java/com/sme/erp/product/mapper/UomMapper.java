package com.sme.erp.product.mapper;

import com.sme.erp.product.dto.UomDTO;
import com.sme.erp.product.entity.Uom;
import org.springframework.stereotype.Component;

@Component
public class UomMapper {

    public UomDTO toDTO(Uom uom) {
        if (uom == null) return null;

        UomDTO dto = new UomDTO();
        dto.setId(uom.getId());
        dto.setCode(uom.getCode());
        dto.setName(uom.getName());
        dto.setType(uom.getType());
        dto.setConversionFactor(uom.getConversionFactor());
        return dto;
    }

    public Uom toEntity(UomDTO dto) {
        if (dto == null) return null;

        return updateEntity(dto, new Uom());
    }

    public Uom updateEntity(UomDTO dto, Uom uom) {
        if (dto == null || uom == null) return uom;

        if (dto.getId() != null) {
            uom.setId(dto.getId());
        }
        if (dto.getCode() != null) {
            uom.setCode(dto.getCode());
        }
        if (dto.getName() != null) {
            uom.setName(dto.getName());
        }
        if (dto.getType() != null) {
            uom.setType(dto.getType());
        }
        if (dto.getConversionFactor() != null) {
            uom.setConversionFactor(dto.getConversionFactor());
        }
        return uom;
    }
}
