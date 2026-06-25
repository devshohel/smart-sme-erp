package com.sme.erp.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.sme.erp.product.entity.Uom;

import java.util.Optional;

public interface UomRepository extends JpaRepository<Uom, Long> {

    //  Find by code
    Optional<Uom> findByCode(String code);

    //  Check duplicate code
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query(value = "SELECT * FROM uoms WHERE is_deleted = true ORDER BY id DESC", nativeQuery = true)
    java.util.List<Uom> findDeletedUoms();

    @Modifying
    @Query(value = "UPDATE uoms SET is_deleted = false, updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);
}
