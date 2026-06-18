package com.sme.erp.accounting.repository;
import com.sme.erp.accounting.entity.CostCenter; import com.sme.erp.enums.Status; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface CostCenterRepository extends JpaRepository<CostCenter,Long>{
 boolean existsByCodeIgnoreCase(String code); Optional<CostCenter> findByCodeIgnoreCase(String code);
 @Query("select c from CostCenter c where (:q is null or lower(c.code) like lower(concat('%',:q,'%')) or lower(c.name) like lower(concat('%',:q,'%'))) and (:status is null or c.status=:status)") Page<CostCenter> search(@Param("q") String q,@Param("status") Status status,Pageable p);
 List<CostCenter> findAllByOrderByCode();
}
