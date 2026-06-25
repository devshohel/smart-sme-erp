package com.sme.erp.product.service;

import com.sme.erp.product.dto.ProductBrandDTO;
import java.util.List;

public interface ProductBrandService {

    ProductBrandDTO save(ProductBrandDTO dto);

    List<ProductBrandDTO> getAll();
    List<ProductBrandDTO> getDeleted();

    ProductBrandDTO getById(Long id);

    void delete(Long id);
    ProductBrandDTO restore(Long id);
}
