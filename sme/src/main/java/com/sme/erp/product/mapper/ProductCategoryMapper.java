package com.sme.erp.product.mapper;

import com.sme.erp.product.dto.ProductCategoryDTO;
import com.sme.erp.enums.Status;
import com.sme.erp.product.entity.ProductCategory;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoryMapper {

    public ProductCategoryDTO toDTO(ProductCategory entity) {
        if (entity == null) {
            return null;
        }

        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setCategoryName(entity.getCategoryName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus() : Status.ACTIVE);

        try {
            if (entity.getParentCategory() != null) {
                dto.setParentCategoryId(entity.getParentCategory().getId());
                dto.setParentCategoryName(entity.getParentCategory().getCategoryName());
            }
        } catch (EntityNotFoundException | ObjectNotFoundException ignored) {
            dto.setParentCategoryId(null);
            dto.setParentCategoryName(null);
        }

        return dto;
    }

    public ProductCategory toEntity(ProductCategoryDTO dto) {
        return updateEntity(dto, new ProductCategory());
    }

    public ProductCategory updateEntity(ProductCategoryDTO dto, ProductCategory entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        if (dto.getCode() != null) {
            entity.setCode(dto.getCode());
        }
        if (dto.getCategoryName() != null) {
            entity.setCategoryName(dto.getCategoryName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        return entity;
    }
}
