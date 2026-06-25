package com.sme.erp.product.service;

import com.sme.erp.product.dto.ProductCategoryDTO;

import java.util.List;

public interface ProductCategoryService {
    ProductCategoryDTO save(ProductCategoryDTO dto);

    List<ProductCategoryDTO> getAll();
    List<ProductCategoryDTO> getDeleted();

    void delete(Long id);
    ProductCategoryDTO restore(Long id);
}
