package com.mooddiary.diary.adapter.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class DevAuthenticationFilter extends OncePerRequestFilter {
    private final DevAuthProperties properties;

    public DevAuthenticationFilter(DevAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (properties.isEnabled() && SecurityContextHolder.getContext().getAuthentication() == null) {
            Instant now = Instant.now();
            Jwt jwt = Jwt.withTokenValue("dev-auth-token")
                    .header("alg", "none")
                    .claim("sub", properties.getSubject())
                    .claim("preferred_username", properties.getUsername())
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build();
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            SecurityContextHolder.getContext().setAuthentication(
                    new JwtAuthenticationToken(jwt, authorities, properties.getUsername())
            );
        }

        filterChain.doFilter(request, response);
    }
}
