package com.sme.erp.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sme.erp.product.entity.Product;
import com.sme.erp.enums.Status;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

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

    long countByStatus(Status status);

    long countByImageUrlIsNullOrImageUrl(String imageUrl);

    // ♻️ Optional (future use - deleted data)
    // @Query("SELECT p FROM Product p WHERE p.deleted = true")
    // List<Product> findDeletedProducts();
}
