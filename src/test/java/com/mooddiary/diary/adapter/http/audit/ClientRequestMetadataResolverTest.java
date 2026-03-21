package com.mooddiary.diary.adapter.http.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientRequestMetadataResolverTest {
    @Test
    void shouldResolveClientIpFromCloudflare() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("CF-Connecting-IP", "1.1.1.1");
        request.addHeader("X-Forwarded-For", "2.2.2.2, 3.3.3.3");
        request.addHeader("X-Real-IP", "4.4.4.4");
        request.addHeader("User-Agent", "ua");

        ClientRequestMetadataResolver resolver = new ClientRequestMetadataResolver();
        RequestAuditMetadata metadata = resolver.resolve(request);

        assertEquals("1.1.1.1", metadata.clientIp());
        assertEquals("CF-Connecting-IP=1.1.1.1; X-Forwarded-For=2.2.2.2, 3.3.3.3; X-Real-IP=4.4.4.4", metadata.forwardedHeaders());
        assertEquals("ua", metadata.userAgent());
    }

    @Test
    void shouldResolveClientIpFromXForwardedForFirstValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "2.2.2.2, 3.3.3.3");

        ClientRequestMetadataResolver resolver = new ClientRequestMetadataResolver();
        RequestAuditMetadata metadata = resolver.resolve(request);

        assertEquals("2.2.2.2", metadata.clientIp());
        assertEquals("X-Forwarded-For=2.2.2.2, 3.3.3.3", metadata.forwardedHeaders());
    }

    @Test
    void shouldResolveClientIpFromRemoteAddrWhenNoHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");

        ClientRequestMetadataResolver resolver = new ClientRequestMetadataResolver();
        RequestAuditMetadata metadata = resolver.resolve(request);

        assertEquals("10.0.0.1", metadata.clientIp());
        assertEquals("", metadata.forwardedHeaders());
    }
}

