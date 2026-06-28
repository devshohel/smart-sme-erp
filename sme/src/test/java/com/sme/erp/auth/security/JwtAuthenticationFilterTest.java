package com.sme.erp.auth.security;

import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.auth.service.AccessTokenBlacklistService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private AccessTokenBlacklistService accessTokenBlacklistService;
    @Mock private UserRepository userRepository;
    @Mock private FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestWithoutBearerTokenDoesNotClearExistingContext() throws Exception {
        UsernamePasswordAuthenticationToken existing =
                new UsernamePasswordAuthenticationToken("existing-user", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        filter().doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verify(jwtService, never()).extractUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void invalidBearerTokenClearsContextAndContinuesChainOnce() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("stale-user", null, java.util.List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        when(accessTokenBlacklistService.isBlacklisted("invalid-token")).thenReturn(false);
        when(jwtService.extractUsername("invalid-token")).thenThrow(new IllegalArgumentException("invalid"));

        filter().doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private JwtAuthenticationFilter filter() {
        return new JwtAuthenticationFilter(
                jwtService,
                userDetailsService,
                accessTokenBlacklistService,
                userRepository);
    }
}
