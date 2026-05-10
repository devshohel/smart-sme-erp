package com.sme.erp.product.service;

import com.sme.erp.product.dto.ProductDTO;
import java.util.List;

public interface ProductService {
    ProductDTO saveProduct(ProductDTO dto);
    List<ProductDTO> getAllProducts();
    ProductDTO getById(Long id);
    void delete(Long id);
}