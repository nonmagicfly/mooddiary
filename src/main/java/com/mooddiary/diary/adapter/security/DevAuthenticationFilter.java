package com.mooddiary.diary.adapter.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Режим без Keycloak: аутентификация по Bearer-токену из фронта ({@code alg: none}, полезная нагрузка с {@code sub}).
 * Пароль проверяется только на клиенте; сервер доверяет токену, если {@code sub} совпадает с настроенным subject.
 */
@Component
public class DevAuthenticationFilter extends OncePerRequestFilter {
    private final DevAuthProperties properties;
    private final ObjectMapper objectMapper;

    public DevAuthenticationFilter(DevAuthProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (!subjectFromUnsignedJwt(token).filter(properties.getSubject()::equals).isPresent()) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue(token)
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

        filterChain.doFilter(request, response);
    }

    private Optional<String> subjectFromUnsignedJwt(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return Optional.empty();
        }
        try {
            byte[] json = base64UrlDecode(parts[1]);
            JsonNode node = objectMapper.readTree(json);
            String sub = node.path("sub").asText(null);
            if (sub == null || sub.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(sub);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static byte[] base64UrlDecode(String s) {
        int pad = (4 - s.length() % 4) % 4;
        String padded = s + "=".repeat(pad);
        return Base64.getUrlDecoder().decode(padded);
    }
}
