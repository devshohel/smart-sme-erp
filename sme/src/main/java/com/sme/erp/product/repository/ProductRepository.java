package com.sme.erp.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sme.erp.product.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🔍 Find by SKU
    Optional<Product> findBySku(String sku);

    // 🔎 Search by product name
    List<Product> findByProductNameContainingIgnoreCase(String name);

    // ✅ Check SKU exists (create)
    boolean existsBySku(String sku);

    // ✅ Check SKU exists (update - excluding current id)
    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    // ♻️ Optional (future use - deleted data)
    // @Query("SELECT p FROM Product p WHERE p.deleted = true")
    // List<Product> findDeletedProducts();
}
