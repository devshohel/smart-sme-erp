package com.sme.erp.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


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

    @Query(value = "SELECT * FROM product_categories WHERE is_deleted = true ORDER BY id DESC", nativeQuery = true)
    List<ProductCategory> findDeletedCategories();

    @Modifying
    @Query(value = "UPDATE product_categories SET is_deleted = false, updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);
}
