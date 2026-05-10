package com.sme.erp.product.mapper;

import com.sme.erp.product.dto.ProductCategoryDTO;
import com.sme.erp.product.entity.ProductCategory;

public class ProductCategoryMapper {
	// ENTITY → DTO
    public static ProductCategoryDTO toDTO(ProductCategory entity) {
        ProductCategoryDTO dto = new ProductCategoryDTO();

        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setCategoryName(entity.getCategoryName());
        dto.setDescription(entity.getDescription());

        if (entity.getParentCategory() != null) {
            dto.setParentCategoryId(entity.getParentCategory().getId());
        }

        return dto;
    }

    // DTO → ENTITY
    public static ProductCategory toEntity(ProductCategoryDTO dto) {
        return updateEntity(dto, new ProductCategory());
    }

    public static ProductCategory updateEntity(ProductCategoryDTO dto, ProductCategory entity) {
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

        return entity;
    }

}
