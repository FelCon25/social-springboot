package com.social.backend.auth.util;

import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestMetadata {
    private RequestMetadata() {

    }

    public static String userAgent(HttpServletRequest request) {
        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        return (ua == null || ua.isBlank()) ? "unknown" : ua;
    }

    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String remote = request.getRemoteAddr();
        return (remote == null || remote.isBlank()) ? "unknown" : remote;
    }
}
