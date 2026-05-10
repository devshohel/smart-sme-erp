package com.sme.erp.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.sme.erp.product.entity.Uom;

import java.util.Optional;

public interface UomRepository extends JpaRepository<Uom, Long> {

    //  Find by code
    Optional<Uom> findByCode(String code);

    //  Check duplicate code
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
