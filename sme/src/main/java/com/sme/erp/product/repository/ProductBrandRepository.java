package com.sme.erp.product.repository;

import com.sme.erp.product.entity.ProductBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface ProductBrandRepository extends JpaRepository<ProductBrand, Long> {

	//Find by code  
    Optional<ProductBrand> findByCode(String code);

    //Search by brand name
    List<ProductBrand> findByBrandNameContainingIgnoreCase(String name);

    //Check duplicate code//  Check duplicate code
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query(value = "SELECT * FROM product_brands WHERE is_deleted = true ORDER BY id DESC", nativeQuery = true)
    List<ProductBrand> findDeletedBrands();

    @Modifying
    @Query(value = "UPDATE product_brands SET is_deleted = false, updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);

    @Query(value = "SELECT COALESCE(MAX(id), 0) FROM product_brands", nativeQuery = true)
    long findMaxId();
}
