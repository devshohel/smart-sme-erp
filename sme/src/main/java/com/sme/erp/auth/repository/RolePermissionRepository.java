package com.sme.erp.auth.repository;

import com.sme.erp.auth.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);

    @Query("""
            select rp.permission.name from RolePermission rp
            where rp.role.id = :roleId
            order by rp.permission.module, rp.permission.action
            """)
    List<String> findPermissionNamesByRoleId(@Param("roleId") Long roleId);

    @Query("""
            select rp from RolePermission rp
            join fetch rp.permission p
            where rp.role.id = :roleId
            order by p.module, p.action
            """)
    List<RolePermission> findWithPermissionByRoleId(@Param("roleId") Long roleId);
}
