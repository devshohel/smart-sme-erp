package com.sme.erp.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    long countByStatus(Status status);

    long countByImageUrlIsNullOrImageUrl(String imageUrl);

    long countByUomId(Long uomId);

    @Query(value = "SELECT * FROM products WHERE is_deleted = true ORDER BY id DESC", nativeQuery = true)
    List<Product> findDeletedProducts();

    @Modifying
    @Query(value = "UPDATE products SET is_deleted = false, updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);

    @Query(value = "SELECT COALESCE(MAX(id), 0) FROM products", nativeQuery = true)
    long findMaxId();

    // ♻️ Optional (future use - deleted data)
    // @Query("SELECT p FROM Product p WHERE p.deleted = true")
    // List<Product> findDeletedProducts();
}
