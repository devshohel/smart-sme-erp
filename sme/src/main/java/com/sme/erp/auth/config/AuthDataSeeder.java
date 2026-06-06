package com.sme.erp.auth.config;

import com.sme.erp.auth.entity.Role;
import com.sme.erp.auth.entity.Permission;
import com.sme.erp.auth.entity.RolePermission;
import com.sme.erp.auth.repository.PermissionRepository;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.RoleRepository;
import com.sme.erp.auth.repository.RolePermissionRepository;
import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.enums.Status;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AuthDataSeeder implements CommandLineRunner {
    private static final List<String> ADMIN_PERMISSIONS = List.of(
            "PRODUCT_VIEW", "PRODUCT_CREATE", "PRODUCT_EDIT", "PRODUCT_DELETE",
            "INVENTORY_VIEW", "INVENTORY_CREATE", "INVENTORY_EDIT", "INVENTORY_DELETE",
            "CUSTOMER_VIEW", "CUSTOMER_CREATE", "CUSTOMER_EDIT", "CUSTOMER_DELETE",
            "SUPPLIER_VIEW", "SUPPLIER_CREATE", "SUPPLIER_EDIT", "SUPPLIER_DELETE",
            "PURCHASE_VIEW", "PURCHASE_CREATE", "PURCHASE_EDIT", "PURCHASE_DELETE",
            "SALES_VIEW", "SALES_CREATE", "SALES_EDIT", "SALES_DELETE",
            "DASHBOARD_VIEW", "REPORT_VIEW",
            "USER_VIEW", "USER_CREATE", "USER_EDIT", "USER_DELETE",
            "ROLE_VIEW", "ROLE_EDIT");

    private static final List<String> MANAGER_PERMISSIONS = List.of(
            "DASHBOARD_VIEW",
            "PRODUCT_VIEW", "PRODUCT_CREATE", "PRODUCT_EDIT",
            "INVENTORY_VIEW", "INVENTORY_CREATE", "INVENTORY_EDIT",
            "CUSTOMER_VIEW", "CUSTOMER_CREATE", "CUSTOMER_EDIT",
            "SUPPLIER_VIEW", "SUPPLIER_CREATE", "SUPPLIER_EDIT",
            "PURCHASE_VIEW", "PURCHASE_CREATE", "PURCHASE_EDIT",
            "SALES_VIEW", "SALES_CREATE", "SALES_EDIT",
            "REPORT_VIEW");

    private static final List<String> STAFF_PERMISSIONS = List.of(
            "DASHBOARD_VIEW", "PRODUCT_VIEW", "INVENTORY_VIEW", "CUSTOMER_VIEW",
            "SUPPLIER_VIEW", "PURCHASE_VIEW", "SALES_VIEW", "SALES_CREATE", "REPORT_VIEW");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = seedRole("ADMIN", "System administrator");
        Role managerRole = seedRole("MANAGER", "Department manager");
        Role staffRole = seedRole("STAFF", "Operational staff");
        seedPermissions();
        seedRolePermissions(adminRole, ADMIN_PERMISSIONS);
        seedRolePermissions(managerRole, MANAGER_PERMISSIONS);
        seedRolePermissions(staffRole, STAFF_PERMISSIONS);

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setUsername("admin");
            admin.setEmail("admin@nexaone.local");
            admin.setPhone(null);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            admin.setStatus(Status.ACTIVE);
            admin.setDeleted(false);
            userRepository.save(admin);
        }
    }

    private Role seedRole(String roleName, String description) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private void seedPermissions() {
        ADMIN_PERMISSIONS.forEach(name -> {
            if (!permissionRepository.existsByName(name)) {
                Permission permission = new Permission();
                permission.setName(name);
                permission.setModule(name.substring(0, name.lastIndexOf('_')));
                permission.setAction(name.substring(name.lastIndexOf('_') + 1));
                permission.setDescription(name.replace('_', ' '));
                permissionRepository.save(permission);
            }
        });
    }

    private void seedRolePermissions(Role role, List<String> permissionNames) {
        List<String> existing = rolePermissionRepository.findPermissionNamesByRoleId(role.getId());
        permissionRepository.findByNameIn(permissionNames).forEach(permission -> {
            if (!existing.contains(permission.getName())) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);
                rolePermissionRepository.save(rolePermission);
            }
        });
    }
}
