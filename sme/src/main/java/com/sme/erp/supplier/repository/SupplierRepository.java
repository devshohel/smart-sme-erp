package com.sme.erp.supplier.repository;

import com.sme.erp.enums.Status;
import com.sme.erp.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findTopByOrderByIdDesc();

    boolean existsBySupplierCode(String supplierCode);

    boolean existsBySupplierCodeAndIdNot(String supplierCode, Long id);

    @Query(value = "SELECT COALESCE(MAX(id), 0) FROM suppliers", nativeQuery = true)
    Long findMaxIdIncludingDeleted();

    @Query(value = "SELECT COUNT(*) FROM suppliers WHERE supplier_code = :supplierCode", nativeQuery = true)
    Long countBySupplierCodeIncludingDeleted(@Param("supplierCode") String supplierCode);

    @Query(value = "SELECT COUNT(*) FROM suppliers WHERE supplier_code = :supplierCode AND id <> :id", nativeQuery = true)
    Long countBySupplierCodeAndIdNotIncludingDeleted(@Param("supplierCode") String supplierCode, @Param("id") Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    List<Supplier> findByStatus(Status status);

    @Query("""
            SELECT s
            FROM Supplier s
            WHERE (:status IS NULL OR s.status = :status)
              AND (
                    :keyword IS NULL
                    OR LOWER(COALESCE(s.supplierCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(s.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(s.companyName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(s.contactPerson, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(s.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(s.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(s.city, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    List<Supplier> search(@Param("keyword") String keyword, @Param("status") Status status);
}
