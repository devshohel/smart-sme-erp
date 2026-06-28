package com.sme.erp.auth.security;

import com.sme.erp.auth.entity.Role;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.RolePermissionRepository;
import com.sme.erp.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RolePermissionRepository rolePermissionRepository;

    @Test
    void loadUser_normalizesRoleWithoutPrefix() {
        UserDetails result = loadUser("admin");

        assertThat(result.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder("ROLE_ADMIN", "DASHBOARD_VIEW");
    }

    @Test
    void loadUser_doesNotDuplicateExistingRolePrefix() {
        UserDetails result = loadUser("ROLE_ADMIN");

        assertThat(result.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder("ROLE_ADMIN", "DASHBOARD_VIEW");
    }

    private UserDetails loadUser(String roleName) {
        Role role = new Role();
        role.setId(2L);
        role.setRoleName(roleName);

        User user = new User();
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(role);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(rolePermissionRepository.findPermissionNamesByRoleId(2L))
                .thenReturn(List.of("DASHBOARD_VIEW"));

        return new CustomUserDetailsService(userRepository, rolePermissionRepository)
                .loadUserByUsername("admin");
    }
}
