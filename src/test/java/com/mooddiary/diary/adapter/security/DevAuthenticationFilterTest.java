package com.mooddiary.diary.adapter.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DevAuthenticationFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DevAuthProperties properties;
    private DevAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        properties = new DevAuthProperties();
        properties.setEnabled(true);
        properties.setUsername("u");
        properties.setSubject("user");
        filter = new DevAuthenticationFilter(properties, objectMapper);
    }

    @Test
    void setsAuthenticationWhenBearerPayloadSubMatches() throws Exception {
        String token = unsignedJwt("{\"sub\":\"user\",\"preferred_username\":\"u\"}");

        var req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertEquals("u", auth.getName());
    }

    @Test
    void doesNotAuthenticateWhenSubMismatches() throws Exception {
        String token = unsignedJwt("{\"sub\":\"other\"}");

        var req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doesNotAuthenticateWithoutBearer() throws Exception {
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void skipsWhenDevAuthDisabled() throws Exception {
        properties.setEnabled(false);
        String token = unsignedJwt("{\"sub\":\"user\"}");
        var req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private static String unsignedJwt(String jsonPayload) {
        String header = base64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
        String payload = base64Url(jsonPayload);
        return header + "." + payload + ".";
    }

    private static String base64Url(String raw) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
