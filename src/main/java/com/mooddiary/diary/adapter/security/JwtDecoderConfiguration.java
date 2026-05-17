package com.mooddiary.diary.adapter.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * JwtDecoder для Keycloak в Docker.
 * <p>
 * Frontend (браузер) использует localhost:8180 — доступен из браузера.
 * Backend (контейнер) получает JWKS по host.docker.internal:8180 — доступен из контейнера.
 * Issuer в JWT от Keycloak (KC_HOSTNAME=localhost) — http://localhost:8180/realms/mooddiary.
 */
@Configuration
@ConditionalOnProperty(name = "mooddiary.dev-auth.enabled", havingValue = "false", matchIfMissing = true)
public class JwtDecoderConfiguration {

    @Value("${KEYCLOAK_JWKS_URI:http://host.docker.internal:8180/realms/mooddiary/protocol/openid-connect/certs}")
    private String jwkSetUri;

    @Value("${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/mooddiary}")
    private String issuerUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }
}
