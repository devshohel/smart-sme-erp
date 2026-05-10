package com.sme.erp.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.sme.erp.product.entity.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    // Find by code
    Optional<ProductCategory> findByCode(String code);

    //  Search by category name
    List<ProductCategory> findByCategoryNameContainingIgnoreCase(String name);

    //  Check duplicate code
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
