package com.sme.erp.auth.config;

import com.sme.erp.auth.entity.Role;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.RoleRepository;
import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.enums.Status;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthDataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = seedRole("ADMIN", "System administrator");
        seedRole("MANAGER", "Department manager");
        seedRole("STAFF", "Operational staff");

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
}
