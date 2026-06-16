package com.sme.erp.product.mapper;

import com.sme.erp.product.dto.ProductDTO;
import com.sme.erp.product.entity.*;
import com.sme.erp.enums.Status;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.ObjectNotFoundException;
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
        dto.setBarcode(product.getBarcode());
        dto.setType(product.getType());
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setTaxPercentage(product.getTaxPercentage());
        dto.setReorderLevel(product.getReorderLevel());
        dto.setImageUrl(product.getImageUrl());
        dto.setImageOriginalFilename(product.getImageOriginalFilename());
        dto.setImageStoredFilename(product.getImageStoredFilename());
        dto.setImageContentType(product.getImageContentType());
        dto.setImageSize(product.getImageSize());
        dto.setImagePath(product.getImagePath());
        dto.setStatus(product.getStatus() != null ? product.getStatus() : Status.ACTIVE);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        mapCategory(product, dto);
        mapBrand(product, dto);
        mapUom(product, dto);

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
        if (dto.getBarcode() != null) {
            product.setBarcode(dto.getBarcode());
        }
        if (dto.getType() != null) {
            product.setType(dto.getType());
        }
        if (dto.getPurchasePrice() != null) {
            product.setPurchasePrice(dto.getPurchasePrice());
        }
        if (dto.getSalePrice() != null) {
            product.setSalePrice(dto.getSalePrice());
        }
        if (dto.getTaxPercentage() != null) {
            product.setTaxPercentage(dto.getTaxPercentage());
        }
        if (dto.getReorderLevel() != null) {
            product.setReorderLevel(dto.getReorderLevel());
        }
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
        if (dto.getImageOriginalFilename() != null) {
            product.setImageOriginalFilename(dto.getImageOriginalFilename());
        }
        if (dto.getImageStoredFilename() != null) {
            product.setImageStoredFilename(dto.getImageStoredFilename());
        }
        if (dto.getImageContentType() != null) {
            product.setImageContentType(dto.getImageContentType());
        }
        if (dto.getImageSize() != null) {
            product.setImageSize(dto.getImageSize());
        }
        if (dto.getImagePath() != null) {
            product.setImagePath(dto.getImagePath());
        }
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        return product;
    }

    private void mapCategory(Product product, ProductDTO dto) {
        try {
            if (product.getCategory() != null) {
                dto.setCategoryId(product.getCategory().getId());
                dto.setCategoryName(product.getCategory().getCategoryName());
            }
        } catch (EntityNotFoundException | ObjectNotFoundException ignored) {
            dto.setCategoryId(null);
            dto.setCategoryName(null);
        }
    }

    private void mapBrand(Product product, ProductDTO dto) {
        try {
            if (product.getBrand() != null) {
                dto.setBrandId(product.getBrand().getId());
                dto.setBrandName(product.getBrand().getBrandName());
            }
        } catch (EntityNotFoundException | ObjectNotFoundException ignored) {
            dto.setBrandId(null);
            dto.setBrandName(null);
        }
    }

    private void mapUom(Product product, ProductDTO dto) {
        try {
            if (product.getUom() != null) {
                dto.setUomId(product.getUom().getId());
                dto.setUomName(product.getUom().getName());
            }
        } catch (EntityNotFoundException | ObjectNotFoundException ignored) {
            dto.setUomId(null);
            dto.setUomName(null);
        }
    }
}
