package com.mooddiary.diary.adapter.http.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientRequestMetadataResolver {
    public RequestAuditMetadata resolve(HttpServletRequest request) {
        String cfConnectingIp = header(request, "CF-Connecting-IP");
        String xForwardedFor = header(request, "X-Forwarded-For");
        String xRealIp = header(request, "X-Real-IP");
        String userAgent = header(request, "User-Agent");

        String clientIp = resolveClientIp(cfConnectingIp, xForwardedFor, xRealIp, request.getRemoteAddr());
        String forwardedHeaders = buildForwardedHeaders(cfConnectingIp, xForwardedFor, xRealIp);

        return new RequestAuditMetadata(clientIp, forwardedHeaders, userAgent);
    }

    private String resolveClientIp(String cfConnectingIp, String xForwardedFor, String xRealIp, String remoteAddr) {
        if (StringUtils.hasText(cfConnectingIp)) {
            return cfConnectingIp;
        }
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        return remoteAddr;
    }

    private String buildForwardedHeaders(String cfConnectingIp, String xForwardedFor, String xRealIp) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(cfConnectingIp)) {
            sb.append("CF-Connecting-IP=").append(cfConnectingIp);
        }
        if (StringUtils.hasText(xForwardedFor)) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("X-Forwarded-For=").append(xForwardedFor);
        }
        if (StringUtils.hasText(xRealIp)) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("X-Real-IP=").append(xRealIp);
        }
        return sb.toString();
    }

    private String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

