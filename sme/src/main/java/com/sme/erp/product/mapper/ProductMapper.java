package com.sme.erp.product.mapper;

import com.sme.erp.product.dto.ProductDTO;
import com.sme.erp.product.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product product) {
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProductCode(product.getProductCode());
        dto.setProductName(product.getProductName());
        dto.setSku(product.getSku());
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSalePrice(product.getSalePrice());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getCategoryName());
        }

        if (product.getBrand() != null) {
            dto.setBrandId(product.getBrand().getId());
            dto.setBrandName(product.getBrand().getBrandName());
        }

        if (product.getUom() != null) {
            dto.setUomId(product.getUom().getId());
            dto.setUomName(product.getUom().getName());
        }

        return dto;
    }

    public Product toEntity(ProductDTO dto) {
        if (dto == null) return null;

        return updateEntity(dto, new Product());
    }

    public Product updateEntity(ProductDTO dto, Product product) {
        if (dto == null || product == null) return product;

        if (dto.getId() != null) {
            product.setId(dto.getId());
        }
        if (dto.getProductCode() != null && !dto.getProductCode().isBlank()) {
            product.setProductCode(dto.getProductCode());
        }
        if (dto.getProductName() != null) {
            product.setProductName(dto.getProductName());
        }
        if (dto.getSku() != null) {
            product.setSku(dto.getSku());
        }
        if (dto.getPurchasePrice() != null) {
            product.setPurchasePrice(dto.getPurchasePrice());
        }
        if (dto.getSalePrice() != null) {
            product.setSalePrice(dto.getSalePrice());
        }

        return product;
    }
}
