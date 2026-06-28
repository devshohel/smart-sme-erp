package com.sme.erp.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.auth.service.AccessTokenBlacklistService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            AccessTokenBlacklistService accessTokenBlacklistService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            if (accessTokenBlacklistService.isBlacklisted(token)) {
                SecurityContextHolder.clearContext();
            } else {
                String username = jwtService.extractUsername(token);
                if (username == null || !jwtService.isValid(token, username) || !tokenVersionMatches(token, username)) {
                    SecurityContextHolder.clearContext();
                } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (!userDetails.isEnabled()) {
                        SecurityContextHolder.clearContext();
                    } else {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (RuntimeException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean tokenVersionMatches(String token, String username) {
        int tokenVersion = jwtService.extractTokenVersion(token);
        return userRepository.findByUsername(username)
                .map(user -> (user.getTokenVersion() == null ? 0 : user.getTokenVersion()) == tokenVersion)
                .orElse(false);
    }
}
