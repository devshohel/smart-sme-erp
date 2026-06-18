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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AuthDataSeeder implements CommandLineRunner {
    private static final List<String> BASE_ADMIN_PERMISSIONS = List.of(
            "PRODUCT_VIEW", "PRODUCT_CREATE", "PRODUCT_EDIT", "PRODUCT_DELETE",
            "INVENTORY_VIEW", "INVENTORY_CREATE", "INVENTORY_EDIT", "INVENTORY_DELETE",
            "CUSTOMER_VIEW", "CUSTOMER_CREATE", "CUSTOMER_EDIT", "CUSTOMER_DELETE",
            "SUPPLIER_VIEW", "SUPPLIER_CREATE", "SUPPLIER_EDIT", "SUPPLIER_DELETE",
            "PURCHASE_VIEW", "PURCHASE_CREATE", "PURCHASE_EDIT", "PURCHASE_DELETE",
            "SALES_VIEW", "SALES_CREATE", "SALES_EDIT", "SALES_DELETE",
            "DASHBOARD_VIEW", "REPORT_VIEW",
            "ACCOUNTING_VIEW", "ACCOUNTING_CREATE", "ACCOUNTING_EDIT", "ACCOUNTING_DELETE", "ACCOUNTING_POST",
            "USER_VIEW", "USER_CREATE", "USER_EDIT", "USER_DELETE",
            "ROLE_VIEW", "ROLE_EDIT",
            "AUDIT_VIEW", "ACTIVITY_VIEW",
            "SETTINGS_VIEW", "SETTINGS_EDIT");

    private static final List<String> BASE_MANAGER_PERMISSIONS = List.of(
            "DASHBOARD_VIEW",
            "PRODUCT_VIEW", "PRODUCT_CREATE", "PRODUCT_EDIT",
            "INVENTORY_VIEW", "INVENTORY_CREATE", "INVENTORY_EDIT",
            "CUSTOMER_VIEW", "CUSTOMER_CREATE", "CUSTOMER_EDIT",
            "SUPPLIER_VIEW", "SUPPLIER_CREATE", "SUPPLIER_EDIT",
            "PURCHASE_VIEW", "PURCHASE_CREATE", "PURCHASE_EDIT",
            "SALES_VIEW", "SALES_CREATE", "SALES_EDIT",
            "ACCOUNTING_VIEW", "ACCOUNTING_CREATE", "ACCOUNTING_EDIT", "ACCOUNTING_POST",
            "REPORT_VIEW",
            "SETTINGS_VIEW");

    private static final List<String> BASE_STAFF_PERMISSIONS = List.of(
            "DASHBOARD_VIEW", "PRODUCT_VIEW", "INVENTORY_VIEW", "CUSTOMER_VIEW",
            "SUPPLIER_VIEW", "PURCHASE_VIEW", "SALES_VIEW", "SALES_CREATE", "ACCOUNTING_VIEW", "REPORT_VIEW");

    private static final List<String> ENTERPRISE_PERMISSIONS = List.of(
            "PRODUCT_PRINT", "PRODUCT_EXPORT", "PRODUCT_BARCODE",
            "CATEGORY_VIEW", "CATEGORY_CREATE", "CATEGORY_EDIT", "CATEGORY_DELETE",
            "BRAND_VIEW", "BRAND_CREATE", "BRAND_EDIT", "BRAND_DELETE",
            "UOM_VIEW", "UOM_CREATE", "UOM_EDIT", "UOM_DELETE",
            "STOCK_ADJUSTMENT_VIEW", "STOCK_ADJUSTMENT_CREATE", "STOCK_ADJUSTMENT_EDIT", "STOCK_ADJUSTMENT_DELETE",
            "STOCK_ADJUSTMENT_SUBMIT", "STOCK_ADJUSTMENT_APPROVE", "STOCK_ADJUSTMENT_REJECT",
            "TRANSFER_VIEW", "TRANSFER_CREATE", "TRANSFER_EDIT", "TRANSFER_APPROVE", "TRANSFER_REJECT",
            "TRANSFER_SEND", "TRANSFER_RECEIVE", "TRANSFER_CANCEL",
            "CUSTOMER_EXPORT", "CUSTOMER_LEDGER_VIEW", "CUSTOMER_AGING_VIEW", "CUSTOMER_STATEMENT_PRINT",
            "CUSTOMER_RECEIPT_VIEW", "CUSTOMER_RECEIPT_CREATE", "CUSTOMER_RECEIPT_EDIT",
            "CUSTOMER_RECEIPT_POST", "CUSTOMER_RECEIPT_CANCEL", "CUSTOMER_RECEIPT_PRINT", "CUSTOMER_RECEIPT_EXPORT",
            "SUPPLIER_EXPORT", "SUPPLIER_LEDGER_VIEW", "SUPPLIER_PAYMENT_VIEW", "SUPPLIER_PAYMENT_CREATE",
            "SUPPLIER_PAYMENT_EDIT", "SUPPLIER_PAYMENT_POST", "SUPPLIER_PAYMENT_CANCEL", "SUPPLIER_STATEMENT_PRINT",
            "SALES_ORDER_APPROVE", "SALES_ORDER_REJECT", "SALES_ORDER_PRINT", "SALES_ORDER_CONVERT",
            "SALES_INVOICE_PRINT", "SALES_INVOICE_EXPORT", "SALES_INVOICE_MARK_PAID", "SALES_INVOICE_RETURN",
            "SALES_RETURN_APPROVE", "SALES_RETURN_REJECT", "SALES_RETURN_PRINT",
            "PURCHASE_APPROVE", "PURCHASE_REJECT", "PURCHASE_RECEIVE", "PURCHASE_PRINT", "PURCHASE_EXPORT",
            "PURCHASE_RETURN_APPROVE", "PURCHASE_RETURN_REJECT", "PURCHASE_RETURN_PRINT",
            "EXPENSE_VIEW", "EXPENSE_CREATE", "EXPENSE_EDIT", "EXPENSE_POST", "EXPENSE_CANCEL",
            "EXPENSE_SUBMIT", "EXPENSE_APPROVE", "EXPENSE_REJECT", "EXPENSE_REVERSE",
            "EXPENSE_REPORT_VIEW", "EXPENSE_REPORT_EXPORT", "EXPENSE_PRINT",
            "JOURNAL_POST", "JOURNAL_CANCEL", "JOURNAL_PRINT",
            "REPORT_EXPORT", "REPORT_PRINT", "REPORT_EXPORT_PDF", "REPORT_EXPORT_EXCEL", "REPORT_EXPORT_CSV",
            "AUDIT_EXPORT", "LOGIN_HISTORY_VIEW");

    private static final List<String> ADMIN_ACTION_PERMISSIONS = List.of(
            "PRODUCT_PRINT", "PRODUCT_EXPORT", "PRODUCT_BARCODE",
            "CATEGORY_VIEW", "CATEGORY_CREATE", "CATEGORY_EDIT", "CATEGORY_DELETE",
            "BRAND_VIEW", "BRAND_CREATE", "BRAND_EDIT", "BRAND_DELETE",
            "UOM_VIEW", "UOM_CREATE", "UOM_EDIT", "UOM_DELETE",
            "STOCK_ADJUSTMENT_VIEW", "STOCK_ADJUSTMENT_CREATE", "STOCK_ADJUSTMENT_EDIT", "STOCK_ADJUSTMENT_DELETE",
            "STOCK_ADJUSTMENT_SUBMIT", "STOCK_ADJUSTMENT_APPROVE", "STOCK_ADJUSTMENT_REJECT",
            "TRANSFER_VIEW", "TRANSFER_CREATE", "TRANSFER_EDIT", "TRANSFER_APPROVE", "TRANSFER_REJECT",
            "TRANSFER_SEND", "TRANSFER_RECEIVE", "TRANSFER_CANCEL",
            "CUSTOMER_EXPORT", "CUSTOMER_LEDGER_VIEW", "CUSTOMER_AGING_VIEW", "CUSTOMER_STATEMENT_PRINT",
            "CUSTOMER_RECEIPT_VIEW", "CUSTOMER_RECEIPT_CREATE", "CUSTOMER_RECEIPT_EDIT",
            "CUSTOMER_RECEIPT_POST", "CUSTOMER_RECEIPT_CANCEL", "CUSTOMER_RECEIPT_PRINT", "CUSTOMER_RECEIPT_EXPORT",
            "SUPPLIER_EXPORT", "SUPPLIER_LEDGER_VIEW", "SUPPLIER_PAYMENT_VIEW", "SUPPLIER_PAYMENT_CREATE",
            "SUPPLIER_PAYMENT_EDIT", "SUPPLIER_PAYMENT_POST", "SUPPLIER_PAYMENT_CANCEL", "SUPPLIER_STATEMENT_PRINT",
            "SALES_ORDER_APPROVE", "SALES_ORDER_REJECT", "SALES_ORDER_PRINT", "SALES_ORDER_CONVERT",
            "SALES_INVOICE_PRINT", "SALES_INVOICE_EXPORT", "SALES_INVOICE_MARK_PAID", "SALES_INVOICE_RETURN",
            "SALES_RETURN_APPROVE", "SALES_RETURN_REJECT", "SALES_RETURN_PRINT",
            "PURCHASE_APPROVE", "PURCHASE_REJECT", "PURCHASE_RECEIVE", "PURCHASE_PRINT", "PURCHASE_EXPORT",
            "PURCHASE_RETURN_APPROVE", "PURCHASE_RETURN_REJECT", "PURCHASE_RETURN_PRINT",
            "EXPENSE_VIEW", "EXPENSE_CREATE", "EXPENSE_EDIT", "EXPENSE_POST", "EXPENSE_CANCEL",
            "EXPENSE_SUBMIT", "EXPENSE_APPROVE", "EXPENSE_REJECT", "EXPENSE_REVERSE",
            "EXPENSE_REPORT_VIEW", "EXPENSE_REPORT_EXPORT", "EXPENSE_PRINT",
            "JOURNAL_POST", "JOURNAL_CANCEL", "JOURNAL_PRINT",
            "REPORT_EXPORT", "REPORT_PRINT", "REPORT_EXPORT_PDF", "REPORT_EXPORT_EXCEL", "REPORT_EXPORT_CSV",
            "AUDIT_EXPORT", "LOGIN_HISTORY_VIEW");

    private static final List<String> MANAGER_ACTION_PERMISSIONS = List.of(
            "PRODUCT_PRINT", "PRODUCT_EXPORT", "PRODUCT_BARCODE",
            "CATEGORY_VIEW", "CATEGORY_CREATE", "CATEGORY_EDIT",
            "BRAND_VIEW", "BRAND_CREATE", "BRAND_EDIT",
            "UOM_VIEW", "UOM_CREATE", "UOM_EDIT",
            "STOCK_ADJUSTMENT_VIEW", "STOCK_ADJUSTMENT_CREATE", "STOCK_ADJUSTMENT_EDIT", "STOCK_ADJUSTMENT_SUBMIT",
            "TRANSFER_VIEW", "TRANSFER_CREATE", "TRANSFER_EDIT",
            "CUSTOMER_EXPORT", "CUSTOMER_LEDGER_VIEW", "CUSTOMER_AGING_VIEW", "CUSTOMER_STATEMENT_PRINT",
            "CUSTOMER_RECEIPT_VIEW", "CUSTOMER_RECEIPT_CREATE", "CUSTOMER_RECEIPT_EDIT",
            "CUSTOMER_RECEIPT_PRINT", "CUSTOMER_RECEIPT_EXPORT",
            "SUPPLIER_EXPORT", "SUPPLIER_LEDGER_VIEW", "SUPPLIER_PAYMENT_VIEW", "SUPPLIER_PAYMENT_CREATE",
            "SUPPLIER_PAYMENT_EDIT", "SUPPLIER_STATEMENT_PRINT",
            "SALES_ORDER_PRINT", "SALES_ORDER_CONVERT", "SALES_INVOICE_PRINT", "SALES_INVOICE_EXPORT",
            "SALES_RETURN_PRINT",
            "PURCHASE_PRINT", "PURCHASE_EXPORT", "PURCHASE_RETURN_PRINT",
            "EXPENSE_VIEW", "EXPENSE_CREATE", "EXPENSE_EDIT", "EXPENSE_POST", "EXPENSE_CANCEL",
            "EXPENSE_SUBMIT", "EXPENSE_APPROVE", "EXPENSE_REJECT", "EXPENSE_REVERSE",
            "EXPENSE_REPORT_VIEW", "EXPENSE_REPORT_EXPORT", "EXPENSE_PRINT", "JOURNAL_POST", "JOURNAL_PRINT",
            "REPORT_EXPORT", "REPORT_PRINT", "REPORT_EXPORT_PDF", "REPORT_EXPORT_EXCEL", "REPORT_EXPORT_CSV");

    private static final List<String> STAFF_ACTION_PERMISSIONS = List.of(
            "CUSTOMER_CREATE", "SUPPLIER_CREATE", "PURCHASE_CREATE",
            "CATEGORY_VIEW", "BRAND_VIEW", "UOM_VIEW",
            "STOCK_ADJUSTMENT_VIEW", "TRANSFER_VIEW",
            "CUSTOMER_RECEIPT_VIEW", "SUPPLIER_PAYMENT_VIEW",
            "SALES_INVOICE_PRINT", "PURCHASE_PRINT");

    private static final List<String> AUDITOR_PERMISSIONS = List.of(
            "DASHBOARD_VIEW", "PRODUCT_VIEW", "INVENTORY_VIEW", "CUSTOMER_VIEW", "SUPPLIER_VIEW",
            "PURCHASE_VIEW", "SALES_VIEW", "ACCOUNTING_VIEW", "REPORT_VIEW", "SETTINGS_VIEW",
            "USER_VIEW", "ROLE_VIEW", "AUDIT_VIEW", "ACTIVITY_VIEW",
            "CATEGORY_VIEW", "BRAND_VIEW", "UOM_VIEW",
            "STOCK_ADJUSTMENT_VIEW", "TRANSFER_VIEW",
            "CUSTOMER_LEDGER_VIEW", "CUSTOMER_AGING_VIEW", "CUSTOMER_RECEIPT_VIEW",
            "SUPPLIER_LEDGER_VIEW", "SUPPLIER_PAYMENT_VIEW",
            "REPORT_EXPORT", "REPORT_PRINT", "REPORT_EXPORT_PDF", "REPORT_EXPORT_EXCEL", "REPORT_EXPORT_CSV",
            "LOGIN_HISTORY_VIEW");

    private static final List<String> ALL_PERMISSIONS = merge(BASE_ADMIN_PERMISSIONS, ENTERPRISE_PERMISSIONS);
    private static final List<String> ADMIN_PERMISSIONS = merge(BASE_ADMIN_PERMISSIONS, ADMIN_ACTION_PERMISSIONS);
    private static final List<String> MANAGER_PERMISSIONS = merge(BASE_MANAGER_PERMISSIONS, MANAGER_ACTION_PERMISSIONS);
    private static final List<String> STAFF_PERMISSIONS = merge(BASE_STAFF_PERMISSIONS, STAFF_ACTION_PERMISSIONS);

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
        Role superAdminRole = seedRole("SUPER_ADMIN", "Enterprise super administrator");
        Role adminRole = seedRole("ADMIN", "System administrator");
        Role managerRole = seedRole("MANAGER", "Department manager");
        Role staffRole = seedRole("STAFF", "Operational staff");
        Role auditorRole = seedRole("AUDITOR", "Read-only auditor");
        seedPermissions();
        seedRolePermissions(superAdminRole, ALL_PERMISSIONS);
        seedRolePermissions(adminRole, ADMIN_PERMISSIONS);
        seedRolePermissions(managerRole, MANAGER_PERMISSIONS);
        seedRolePermissions(staffRole, STAFF_PERMISSIONS);
        seedRolePermissions(auditorRole, AUDITOR_PERMISSIONS);

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
        ALL_PERMISSIONS.forEach(name -> {
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

    @SafeVarargs
    private static List<String> merge(List<String>... permissionGroups) {
        Set<String> permissions = new LinkedHashSet<>();
        for (List<String> permissionGroup : permissionGroups) {
            permissions.addAll(permissionGroup);
        }
        return new ArrayList<>(permissions);
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
