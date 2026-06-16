package com.sme.erp.product.service;

import com.sme.erp.product.dto.ProductDTO;
import com.sme.erp.product.dto.ProductPageDTO;
import com.sme.erp.product.dto.ProductStatsDTO;
import com.sme.erp.enums.Status;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductDTO saveProduct(ProductDTO dto);
    ProductDTO saveProduct(ProductDTO dto, MultipartFile image);
    List<ProductDTO> getAllProducts();
    ProductPageDTO searchProducts(String keyword, Long categoryId, Long brandId, Status status, int page, int size, String sort, String direction);
    ProductStatsDTO getStats();
    int updateStatusBulk(List<Long> productIds, Status status);
    ProductDTO getById(Long id);
    void delete(Long id);
}
