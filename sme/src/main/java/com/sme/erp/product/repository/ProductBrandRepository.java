package com.sme.erp.product.repository;

import com.sme.erp.product.entity.ProductBrand;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
