package com.sme.erp.product.mapper;

import com.sme.erp.product.dto.ProductBrandDTO;
import com.sme.erp.product.entity.ProductBrand;
import org.springframework.stereotype.Component;

@Component
public class ProductBrandMapper {

    public ProductBrandDTO toDTO(ProductBrand brand) {
        if (brand == null) return null;

        ProductBrandDTO dto = new ProductBrandDTO();
        dto.setId(brand.getId());
        dto.setCode(brand.getCode());
        dto.setBrandName(brand.getBrandName());
        return dto;
    }

    public ProductBrand toEntity(ProductBrandDTO dto) {
        if (dto == null) return null;

        return updateEntity(dto, new ProductBrand());
    }

    public ProductBrand updateEntity(ProductBrandDTO dto, ProductBrand brand) {
        if (dto == null || brand == null) return brand;

        if (dto.getId() != null) {
            brand.setId(dto.getId());
        }
        if (dto.getCode() != null) {
            brand.setCode(dto.getCode());
        }
        if (dto.getBrandName() != null) {
            brand.setBrandName(dto.getBrandName());
        }
        return brand;
    }
}
